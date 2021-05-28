/**
 * NsgaII.java
 *
 * @author Juan J. Durillo
 * @version 1.0
 */
package jmetal.metaheuristics.nsgaII;

import jmetal.core.*;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.Distance;
import jmetal.util.JMException;
import jmetal.util.Ranking;

/**
 * This class implements the NSGA-II algorithm. 
 */
public class NSGAII_WSN extends Algorithm {

    /**
     * stores the problem  to solve
     */
    //private Problem problem_;

    /**
     * Constructor
     * @param problem Problem to solve
     */
    public NSGAII_WSN(Problem problem) {
        super(problem);
        //problem_ = problem;
    } // NSGAII

    /**
     * Runs the NSGA-II algorithm.
     * @return a <code>SolutionSet</code> that is a set of non dominated solutions
     * as a result of the algorithm execution
     * @throws JMException
     * @throws ClassNotFoundException
     */
    public SolutionSet execute() throws JMException, ClassNotFoundException {
        int populationSize;
        int maxEvaluations;
        int evaluations;
        int realEvals = 0;

        QualityIndicator indicators; // QualityIndicator object
        int requiredEvaluations; // Use in the example of use of the
        // indicators object (see below)

        SolutionSet population;
        SolutionSet offspringPopulation;
        SolutionSet union;

        Operator mutationOperator;
        Operator crossoverOperator;
        Operator selectionOperator;
        Operator improvement;
        Operator minievalsOperator;

        Distance distance = new Distance();

        //Read the parameters
        populationSize = ((Integer) getInputParameter("populationSize")).intValue();
        maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();
        indicators = (QualityIndicator) getInputParameter("indicators");


        //Initialize the variables
        population = new SolutionSet(populationSize);
        evaluations = 0;

        requiredEvaluations = 0;

        //Read the operators
        mutationOperator = operators_.get("mutation");
        crossoverOperator = operators_.get("crossover");
        selectionOperator = operators_.get("selection");
        improvement = operators_.get("improvement");
        minievalsOperator = operators_.get("minievaluator");

        // Create the initial solutionSet
        Solution newSolution;
        for (int i = 0; i < populationSize; i++) {
            newSolution = new Solution(problem_);
            problem_.evaluate(newSolution);
            realEvals++;
            evaluations++;
            if (minievalsOperator != null) {
                int[] r = (int[]) minievalsOperator.execute(newSolution);
                evaluations = evaluations + r[1];
            }
            // System.out.println(evaluations);
            population.add(newSolution);
        } //for

        // Generations ...
        while (evaluations < maxEvaluations) {
            // Create the offSpring solutionSet
            if ((realEvals % 5000) == 0) {
                System.out.println(realEvals + "\t" + evaluations);
            }
            offspringPopulation = new SolutionSet(populationSize);
            Solution[] parents = new Solution[2];
            for (int i = 0; i < (populationSize / 2); i++) {
                if (evaluations < maxEvaluations) {
                    //obtain parents
                    parents[0] = (Solution) selectionOperator.execute(population);
                    parents[1] = (Solution) selectionOperator.execute(population);
                    Solution[] offSpring = (Solution[]) crossoverOperator.execute(parents);
                    mutationOperator.execute(offSpring[0]);
                    mutationOperator.execute(offSpring[1]);

                    problem_.evaluate(offSpring[0]);
                    problem_.evaluate(offSpring[1]);
                    evaluations += 2;
                    offspringPopulation.add(offSpring[0]);
                    offspringPopulation.add(offSpring[1]);


//          //begin PACO evaluation
//          System.out.println("Eval " + evaluations + ": ");
//          Solution s = new Solution(offSpring[0]);
//          improvement.execute(s);
//          System.out.print(" -> from " + "(" + offSpring[0].getObjective(0) + "," + offSpring[0].getObjective(1) + ") ");
//          System.out.println("to " + "(" + s.getObjective(0) + "," + s.getObjective(1) + ") eval = " + evaluations);
//          
//          if (evaluations % 10000 == 0)
//          {
//          	System.out.println(offSpring[0].getDecisionVariables().toString());
//          	System.out.println(s.getDecisionVariables().toString());
//          	System.out.println();
//          }
//          // end PACO evalution
//          
//          evaluations += 1;
//          
//          //begin PACO evaluation
//          System.out.println("Eval " + evaluations + ": ");
//          s = new Solution(offSpring[1]);
//          improvement.execute(s);
//          System.out.print(" -> from " + "(" + offSpring[1].getObjective(0) + "," + offSpring[1].getObjective(1) + ") ");
//          System.out.println("to " + "(" + s.getObjective(0) + "," + s.getObjective(1) + ") eval = " + evaluations);
//          
//          if (evaluations % 10000 == 0)
//          {
//          	System.out.println(offSpring[1].getDecisionVariables().toString());
//          	System.out.println(s.getDecisionVariables().toString());
//          	System.out.println();
//          }
//          // end PACO evalution
                } // if
            } // for


            // Create the solutionSet union of solutionSet and offSpring
            union = population.union(offspringPopulation);

            // Ranking the union
            Ranking ranking = new Ranking(union);

            int remain = populationSize;
            int index = 0;
            SolutionSet front = null;
            population.clear();

            //Obtain the next front
            front = ranking.getSubfront(index);

            while ((remain > 0) && (remain >= front.size())) {
                //Assign crowding distance to individuals
                distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
                //Add the individuals of this front
                for (int k = 0; k < front.size(); k++) {
                    population.add(front.get(k));
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
                distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
                front.sort(new jmetal.util.comparators.CrowdingComparator());
                for (int k = 0; k < remain; k++) {
                    population.add(front.get(k));
                } // for

                remain = 0;
            } // if

            // This piece of code shows how to use the indicator object into the code
            // of NSGA-II. In particular, it finds the number of evaluations required
            // by the algorithm to obtain a Pareto front with a hypervolume higher
            // than the hypervolume of the true Pareto front.
            if ((indicators != null) &&
                    (requiredEvaluations == 0)) {
                double HV = indicators.getHypervolume(population);
                if (HV >= (0.98 * indicators.getTrueParetoFrontHypervolume())) {
                    requiredEvaluations = evaluations;
                } // if
            } // if

            if ((evaluations % 100) == 0) {
                population.printObjectivesToFile("FUN." + evaluations);
                population.printVariablesToFile("VAR." + evaluations);
            }

//      if ((evaluations % 25000) == 0)
//      {
//    	  population.printTrack("TRACK." + evaluations);
//    	  
//      }
        } // while

        // Return as output parameter the required evaluations
        setOutputParameter("evaluations", requiredEvaluations);

        // Return the first non-dominated front
        Ranking ranking = new Ranking(population);
        return ranking.getSubfront(0);
    } // execute
} // NSGA-II
