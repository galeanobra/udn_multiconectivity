package jmetal.metaheuristics.nsgaII;

import jmetal.core.*;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.qualityIndicator.R2;
import jmetal.util.Distance;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import jmetal.util.Ranking;
import jmetal.util.comparators.CrowdingComparator;
import jmetal.util.comparators.DominanceComparator;
import jmetal.util.offspring.Offspring;
import jmetal.util.offspring.PolynomialMutationOffspring;

import java.util.Comparator;

public class NSGAIIR2 extends Algorithm {
    public int populationSize_;
    public SolutionSet population_;
    public SolutionSet offspringPopulation_;
    public SolutionSet union_;

    int maxEvaluations_;
    int evaluations_;
    boolean applyMutation_; // Polynomial mutation
    double distributionIndexForMutation_;
    double distributionIndexForCrossover_;
    double crossoverProbability_;
    double mutationProbability_;
    double CR_;
    double F_;
    R2 r2_;


    int[] contributionCounter_; // contribution per crossover operator
    double[] contribution_; // contribution per crossover operator
    double total = 0.0;

    int[][] contributionArchiveCounter_;
    public double mincontribution = 0.30;

    final boolean TRAZA = false;

    private QualityIndicator indicators_;

    public NSGAIIR2(Problem problem) {
        super(problem);
    }

    public SolutionSet execute() throws JMException, ClassNotFoundException {
        double contrDE = 0;
        double contrSBX = 0;
        double contrBLXA = 0;
        double contrPol = 0;
        double contrTotalDE = 0;
        double contrTotalSBX = 0;
        double contrTotalPol = 0;
        SolutionSet[] operatorPops;

        double[] contrReal = {0.0, 0.0, 0.0};


        Comparator dominance = new DominanceComparator();
        Comparator crowdingComparator = new CrowdingComparator();
        Distance distance = new Distance();
        Operator selectionOperator;

        //Read parameter values
        populationSize_ = ((Integer) getInputParameter("populationSize")).intValue();
        //CR_ = ((Double) getInputParameter("CR")).doubleValue();
        //F_ = ((Double) getInputParameter("F")).doubleValue();
        maxEvaluations_ = ((Integer) getInputParameter("maxEvaluations")).intValue();
        indicators_ = (QualityIndicator) getInputParameter("indicators");
        String file = (String) getInputParameter("Weights");
        if (file != null) {
            r2_ = new R2(problem_.getNumberOfObjectives(), file);
        } else if ((problem_.getNumberOfObjectives() > 2) && (file == null)) {
            System.out.println("Option not valid. A file containing weights has to be indicated when the number of objective is bigger than two"); // This has to be a log not anything else!
            System.exit(0);
        } else {
            r2_ = new R2(problem_.getNumberOfObjectives());
        }

        //Init the variables
        population_ = new SolutionSet(populationSize_);
        evaluations_ = 0;

        selectionOperator = operators_.get("selection");

        Offspring[] getOffspring;
        int N_O; // number of offpring objects

        getOffspring = ((Offspring[]) getInputParameter("offspringsCreators"));
        N_O = getOffspring.length;
        operatorPops = new SolutionSet[N_O];
        // allocate space for the populations of each operator

        contribution_ = new double[N_O];
        contributionCounter_ = new int[N_O];
        contribution_[0] = populationSize_ / (double) N_O / (double) populationSize_;
        for (int i = 1; i < N_O; i++) {
            contribution_[i] = populationSize_ / (double) N_O / (double) populationSize_ + contribution_[i - 1];
        }

        for (int i = 0; i < N_O; i++) {
            System.out.println(getOffspring[i].configuration());
            System.out.println("Contribution: " + contribution_[i]);
        }

        // Create the initial solutionSet
        Solution newSolution;
        for (int i = 0; i < populationSize_; i++) {
            newSolution = new Solution(problem_);
            problem_.evaluate(newSolution);
            problem_.evaluateConstraints(newSolution);
            evaluations_++;
            newSolution.setLocation(i);
            population_.add(newSolution);
        } //for

        while (evaluations_ < maxEvaluations_) {

            // Create the offSpring solutionSet
            offspringPopulation_ = new SolutionSet(populationSize_);
            Solution[] parents = new Solution[2];
            for (int i = 0; i < (populationSize_ / 1); i++) {
                if (evaluations_ < maxEvaluations_) {
                    Solution individual = new Solution(population_.get(PseudoRandom.randInt(0, populationSize_ - 1)));
                    //Solution individual = new Solution(population_.get(i));
                    int selected = 0;
                    int realSelected = 0;
                    boolean found = false;
                    Solution offSpring = null;
                    double rnd = PseudoRandom.randDouble();
                    for (selected = 0; selected < N_O; selected++) {
                        if (!found && (rnd <= contribution_[selected])) {
                            if ("DE".equals(getOffspring[selected].id())) {
                                offSpring = getOffspring[selected].getOffspring(population_, i);
                                System.out.println("DE");
                                contrDE++;
                            } else if ("SBXCrossover".equals(getOffspring[selected].id())) {
                                offSpring = getOffspring[selected].getOffspring(population_);
                                System.out.println("SBX");
                                contrSBX++;
                            } else if ("PolynomialMutation".equals(getOffspring[selected].id())) {
                                offSpring = ((PolynomialMutationOffspring) getOffspring[selected]).getOffspring(individual);
                                System.out.println("Pol");
                                contrPol++;
                            } else {
                                System.out.println("Error in NSGAIIAdaptive. Operator " + offSpring + " does not exist");
                            }
                            offSpring.setFitness(selected);
                            found = true;
                            realSelected = selected;
                        } // if

                    } // for

                    System.out.println(evaluations_ + " " + realSelected);
                    problem_.evaluate(offSpring);
                    offspringPopulation_.add(offSpring);
                    evaluations_ += 1;
                } // if
            } // for

            // Create the solutionSet union of solutionSet and offSpring
            union_ = population_.union(offspringPopulation_);

            // Ranking the union


            /***** OPTION WITH PARETO DOMINANCE *******/
/*
      Ranking ranking = new Ranking(union_);

      int remain = populationSize_;
      int index = 0;
      SolutionSet front = null;
      population_.clear();

      // Obtain the next front
      front = ranking.getSubfront(index);

      while ((remain > 0) && (remain >= front.size())) {
        
        //Add the individuals of this front
        for (int k = 0; k < front.size(); k++) {
          population_.add(front.get(k));
        } // for

        //Decrement remain
        remain = remain - front.size();

        //Obtain the next front
        index++;
        if (remain > 0) {
          front = ranking.getSubfront(index);
        } // if        
      } // while

      // Remain is less than front(index).size, insert only the best one
      if (remain > 0) {  // front contains individuals to insert                        
        int [] indexes = r2_.getNBest(front, remain);
        for (int j = 0; j < remain; j++) {
            population_.add(front.get(indexes[j])); // only the best solutions 
                                                    // according to the R2 are
                                                    // inserted in the pop.
        }

        remain = 0;
      } // if                 
      
      
      /***** END OF OPTION WITH PARETO DOMINANCE ******/


            /***** OPTION WITHOUT PARETO DOMINANCE **********/


            int remain = populationSize_;
            population_.clear();

            // Obtain the next front
            while (population_.size() < populationSize_) {
                int index = r2_.getBest(union_);
                population_.add(union_.get(index));
                union_.remove(index);
            }

      /*
      int [] indexes = r2_.getNBest(union_, remain);
      for (int j = 0; j < remain; j++) {
        population_.add(union_.get(indexes[j])); // only the best solutions 
                                                 // according to the R2 are
                                                 // inserted in the pop.
      } */

            /****** END OPTION WITHOUT PARETO DOMINANCE *********/


            //------------------------------------------------------------------------
            // COMPUTING THE CONTRIBUTION OF EACH OPERATOR
            // First: reset contribution counter
            //------------------------------------------------------------------------
            for (int i = 0; i < N_O; i++) {
                contributionCounter_[i] = 0;
            }

            // Create a population for each operator
            for (int i = 0; i < N_O; i++) {
                operatorPops[i] = new SolutionSet(populationSize_);
            }

            // Determine which solution of the new population has been created for each operator (in ranking 0);
            Ranking r = new Ranking(population_);

            for (int i = 0; i < r.getSubfront(0).size(); i++) {
                if ((int) r.getSubfront(0).get(i).getFitness() != -1) {
                    //contributionCounter_[(int) population_.get(i).getFitness()] += 1;
                    operatorPops[(int) r.getSubfront(0).get(i).getFitness()].add(r.getSubfront(0).get(i));
                }
                //population_.get(i).setFitness(-1);
            }

            for (int i = 0; i < N_O; i++) {
                operatorPops[i].printObjectivesToFile("FUN." + i);
                operatorPops[i].printVariablesToFile("VAR." + i);
            }


            contrTotalDE += contributionCounter_[0];
            contrTotalSBX += contributionCounter_[1];
            contrTotalPol += contributionCounter_[2];


            // computing the total contribution of each operator
            double totalContributionCounter = 0.0;

            // counting how many solutions do we have of each operator
            double[] operatorsPopSize = new double[N_O];
            for (int i = 0; i < N_O; i++) {
                if (operatorPops[i].size() < 10.0) {
                    operatorsPopSize[i] = 10.0; // minimum contribution
                } else {
                    operatorsPopSize[i] = operatorPops[i].size();
                }
            }


            for (int i = 0; i < N_O; i++) {
                totalContributionCounter += operatorsPopSize[i];
            }

            //System.out.println(totalContributionCounter);

            // Third: calculating contribution
            contribution_[0] = operatorsPopSize[0] / totalContributionCounter;
            for (int i = 1; i < N_O; i++) {
                contribution_[i] = contribution_[i - 1] + operatorsPopSize[i] / totalContributionCounter;
            }

            System.out.println(contribution_[0] + "\t" + contribution_[1] + "\t" + contribution_[2] + "\t" + totalContributionCounter);
        } // while

    /*FileUtils.createEmtpyFile("contDETotal") ;
    FileUtils.createEmtpyFile("contSBXTotal") ;
    FileUtils.createEmtpyFile("contPolTotal") ;
    jmetal.util.FileUtils.appendObjectToFile("contDETotal", contrTotalDE) ;
    jmetal.util.FileUtils.appendObjectToFile("contSBXTotal", contrTotalSBX) ;
    jmetal.util.FileUtils.appendObjectToFile("contPolTotal", contrTotalPol) ;
    FileUtils.createEmtpyFile("contDE") ;
    FileUtils.createEmtpyFile("contSBX") ;
    FileUtils.createEmtpyFile("contPol") ;
    jmetal.util.FileUtils.appendObjectToFile("contDE", contrDE) ;
    jmetal.util.FileUtils.appendObjectToFile("contSBX", contrSBX) ;
    jmetal.util.FileUtils.appendObjectToFile("contPol", contrPol) ;*/

        // Return the first non-dominated front
        Ranking ranking = new Ranking(population_);
        return ranking.getSubfront(0);
    }
}
