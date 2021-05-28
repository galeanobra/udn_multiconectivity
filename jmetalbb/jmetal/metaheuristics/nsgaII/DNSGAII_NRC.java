//  NSGAII.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.
package jmetal.metaheuristics.nsgaII;

import jmetal.core.*;
import jmetal.problems.UDN.CSO;
import jmetal.problems.UDN.DynamicCSO;
import jmetal.qualityIndicator.QualityIndicator;
import jmetal.util.Distance;
import jmetal.util.JMException;
import jmetal.util.Ranking;
import jmetal.util.comparators.CrowdingComparator;

import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic NSGA-II with no reaction to change
 *
 * @author paco
 */
public class DNSGAII_NRC extends Algorithm {

    List<SolutionSet> frontAtEpoch_;

    /**
     * Constructor
     *
     * @param problem Problem to solve
     */
    public DNSGAII_NRC(Problem problem) {
        super(problem);

        frontAtEpoch_ = new ArrayList<SolutionSet>();
    } // NSGAII

    /**
     * Runs the NSGA-II algorithm.
     *
     * @return a <code>SolutionSet</code> that is a set of non dominated
     * solutions as a result of the algorithm execution
     * @throws JMException
     */
    public SolutionSet execute() throws JMException, ClassNotFoundException {
        int populationSize;
        int maxEvaluations;
        int evaluations;

        QualityIndicator indicators; // QualityIndicator object
        // indicators object (see below)

        SolutionSet population;
        SolutionSet offspringPopulation;
        SolutionSet union;

        Operator mutationOperator;
        Operator crossoverOperator;
        Operator selectionOperator;

        Distance distance = new Distance();

        //Read the parameters
        populationSize = ((Integer) getInputParameter("populationSize")).intValue();
        maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();
        indicators = (QualityIndicator) getInputParameter("indicators");

        //Initialize the variables
        population = new SolutionSet(populationSize);
        evaluations = 0;

        //Read the operators
        mutationOperator = operators_.get("mutation");
        crossoverOperator = operators_.get("crossover");
        selectionOperator = operators_.get("selection");

        // Create the initial solutionSet
        Solution newSolution;
        for (int i = 0; i < populationSize; i++) {
            newSolution = new Solution(problem_);
            problem_.evaluate(newSolution);
            problem_.evaluateConstraints(newSolution);
            evaluations++;
            population.add(newSolution);
        } //for

        // Run the NSGA-II main loop for each epoc
        for (int epoch = 0; epoch <= ((DynamicCSO) problem_).getNumberOfEpochs(); epoch++) {
            long initTime = System.currentTimeMillis();
            // Generations
            while (evaluations < maxEvaluations) {
                // Create the offSpring solutionSet
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
                        problem_.evaluateConstraints(offSpring[0]);
                        problem_.evaluate(offSpring[1]);
                        problem_.evaluateConstraints(offSpring[1]);
                        offspringPopulation.add(offSpring[0]);
                        offspringPopulation.add(offSpring[1]);
                        evaluations += 2;
                        //System.out.println(evaluations + ": " + offSpring[0]);

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

                // Obtain the next front
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
                    front.sort(new CrowdingComparator());
                    for (int k = 0; k < remain; k++) {
                        population.add(front.get(k));
                    } // for

                    remain = 0;
                } // if

                if ((evaluations % 250000) == 0) {
                    int run = ((CSO) problem_).getRun();
                    front.printObjectivesToFile("nsga2.FUN." + evaluations + "." + epoch + "." + run);
                }

            } // while

            // Return the first non-dominated front
            Ranking ranking = new Ranking(population);

            SolutionSet aux = ranking.getSubfront(0);
            for (int i = 0; i < aux.size(); i++) {
                problem_.evaluate(aux.get(i));
            }

            SolutionSet frontAtEpoch = ranking.getSubfront(0);

            //save the approximated front at epoch i
            frontAtEpoch_.add(frontAtEpoch);

            long estimatedTime = System.currentTimeMillis() - initTime;
            System.out.println("Total execution time of epoch " + epoch + ": " + estimatedTime + "ms");

            // Pass on to the next epoch
            ((DynamicCSO) problem_).nextEpoch();

            //Initialize the variables
            evaluations = 0;

            // Create the SolutionSet for the next generation
            for (int i = 0; i < populationSize; i++) {
                //generate restartSolutions new solutions 
                newSolution = new Solution(problem_);
                problem_.evaluate(newSolution);
                problem_.evaluateConstraints(newSolution);
                evaluations++;
                population.replace(i, newSolution);

            } //for

        }

        // Return the first non-dominated front of the last epoch
        Ranking ranking = new Ranking(population);

        SolutionSet aux = ranking.getSubfront(0);
        for (int i = 0; i < aux.size(); i++) {
            problem_.evaluate(aux.get(i));
        }
        return ranking.getSubfront(0);

    } // execute

    /**
     * Getter
     *
     * @return The list of approximated fronts for all the epochs of a dynamic
     * problem
     */
    List<SolutionSet> getFrontsAtEpochs() {
        return this.frontAtEpoch_;
    }
} // NSGA-II
