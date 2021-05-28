//  sMOCell2.java
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

package jmetal.metaheuristics.mocell;

import jmetal.core.*;
import jmetal.util.*;
import jmetal.util.archive.CrowdingArchive;
import jmetal.util.comparators.CrowdingComparator;
import jmetal.util.comparators.DominanceComparator;
import jmetal.util.offspring.Offspring;
import jmetal.util.offspring.PolynomialMutationOffspring;

import java.util.Comparator;

/**
 * This class represents a synchronous version of MOCell algorithm, which
 * applies an archive feedback through parent selection.
 */
public class sMOCell2Random extends Algorithm {

    /**
     * Constructor
     *
     * @param problem Problem to solve
     */
    public sMOCell2Random(Problem problem) {
        super(problem);
    } //sMOCell2

    /**
     * Runs of the sMOCell2 algorithm.
     *
     * @return a <code>SolutionSet</code> that is a set of non dominated solutions
     * as a result of the algorithm execution
     * @throws JMException
     */
    public SolutionSet execute() throws JMException, ClassNotFoundException {
        int populationSize, archiveSize, maxEvaluations, evaluations, feedBack;
        Operator mutationOperator, crossoverOperator, selectionOperator;
        SolutionSet currentSolutionSet, newSolutionSet;
        CrowdingArchive archive;
        SolutionSet[] neighbors;
        Neighborhood neighborhood;
        Comparator dominance = new DominanceComparator(),
                crowding = new CrowdingComparator();
        Distance distance = new Distance();

        double contrDE = 0;
        double contrSBX = 0;
        double contrBLXA = 0;
        double contrPol = 0;
        double contrTotalDE = 0;
        double contrTotalSBX = 0;
        double contrTotalPol = 0;

        int[] contributionCounter_; // contribution per crossover operator
        double[] contribution_; // contribution per crossover operator

        //Read the params
        populationSize = ((Integer) getInputParameter("populationSize")).intValue();
        archiveSize = ((Integer) getInputParameter("archiveSize")).intValue();
        maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();

        Offspring[] getOffspring;
        int N_O; // number of offspring objects

        getOffspring = ((Offspring[]) getInputParameter("offspringsCreators"));
        N_O = getOffspring.length;

        //Read the operators
        mutationOperator = operators_.get("mutation");
        crossoverOperator = operators_.get("crossover");
        selectionOperator = operators_.get("selection");


        //Initialize the variables
        currentSolutionSet = new SolutionSet(populationSize);
        newSolutionSet = new SolutionSet(populationSize);
        archive = new CrowdingArchive(archiveSize, problem_.getNumberOfObjectives());
        evaluations = 0;
        neighborhood = new Neighborhood(populationSize);
        neighbors = new SolutionSet[populationSize];

        contribution_ = new double[N_O];
        contributionCounter_ = new int[N_O];

        contribution_[0] = populationSize / (double) N_O / (double) populationSize;
        for (int i = 1; i < N_O; i++) {
            contribution_[i] = populationSize / (double) N_O / (double) populationSize + contribution_[i - 1];
        }

        for (int i = 0; i < N_O; i++) {
            System.out.println(getOffspring[i].configuration());
            System.out.println("Contribution: " + contribution_[i]);
        }

        //Create the initial population
        for (int i = 0; i < populationSize; i++) {
            Solution solution = new Solution(problem_);
            problem_.evaluate(solution);
            problem_.evaluateConstraints(solution);
            currentSolutionSet.add(solution);
            solution.setLocation(i);
            evaluations++;
        }
        //
        int iterations = 0;

        while (evaluations < maxEvaluations) {
            newSolutionSet = new SolutionSet(populationSize);
            for (int ind = 0; ind < currentSolutionSet.size(); ind++) {
                Solution individual = new Solution(currentSolutionSet.get(ind));

                Solution[] parents = new Solution[2];
                Solution offSpring = null;

                //neighbors[ind] = neighborhood.getFourNeighbors(currentSolutionSet,ind);
                neighbors[ind] = neighborhood.getEightNeighbors(currentSolutionSet, ind);
                neighbors[ind].add(individual);

                //parents
                parents[0] = (Solution) selectionOperator.execute(neighbors[ind]);
                if (archive.size() > 0) {
                    parents[1] = (Solution) selectionOperator.execute(archive);
                } else {
                    parents[1] = (Solution) selectionOperator.execute(neighbors[ind]);
                }

                int selected = 0;
                boolean found = false;
                double rnd = PseudoRandom.randDouble();
                for (selected = 0; selected < N_O; selected++) {

                    if (!found && (rnd <= contribution_[selected])) {
                        if ("DE".equals(getOffspring[selected].id())) {
                            offSpring = getOffspring[selected].getOffspring(parents, individual);
                            //contrDE++;
                        } else if ("SBXCrossover".equals(getOffspring[selected].id())) {
                            offSpring = getOffspring[selected].getOffspring(parents);
                            //contrSBX++;
                        } else if ("PolynomialMutation".equals(getOffspring[selected].id())) {
                            offSpring = ((PolynomialMutationOffspring) getOffspring[selected]).getOffspring(individual);
                            //contrPol++;
                        } else {
                            System.out.println("Error in sMOCellAdaptive. Operator " + offSpring + " does not exist");
                        }

                        offSpring.setFitness(selected);
                        found = true;
                    } // if
                } // for

                //->Evaluate solution an his constraints
                problem_.evaluate(offSpring);
                problem_.evaluateConstraints(offSpring);
                evaluations++;
                //<-Individual evaluated

                int flag = dominance.compare(individual, offSpring);

                if (flag == -1) {
                    newSolutionSet.add(new Solution(currentSolutionSet.get(ind)));
                }

                if (flag == 1) {//The new indivudlas dominate
                    offSpring.setLocation(individual.getLocation());
                    //currentSolutionSet.reemplace(offSpring.getLocation(),offSpring);
                    newSolutionSet.add(offSpring);
                    archive.add(new Solution(offSpring));
                } else if (flag == 0) { //The individuals are non-dominates
                    neighbors[ind].add(offSpring);
                    //(new Spea2Fitness(neighbors[ind])).fitnessAssign();
                    //neighbors[ind].sort(new FitnessAndCrowdingDistanceComparator()); //Create a new comparator;
                    Ranking rank = new Ranking(neighbors[ind]);
                    for (int j = 0; j < rank.getNumberOfSubfronts(); j++) {
                        distance.crowdingDistanceAssignment(rank.getSubfront(j), problem_.getNumberOfObjectives());
                    }
                    boolean deleteMutant = true;

                    int compareResult = crowding.compare(individual, offSpring);
                    if (compareResult == 1) { //The offSpring is better
                        deleteMutant = false;
                    }

                    if (!deleteMutant) {
                        offSpring.setLocation(individual.getLocation());
                        //currentSolutionSet.reemplace(offSpring.getLocation(),offSpring);
                        newSolutionSet.add(offSpring);
                        archive.add(new Solution(offSpring));
                    } else {
                        newSolutionSet.add(new Solution(currentSolutionSet.get(ind)));
                        archive.add(new Solution(offSpring));
                    }
                }
            }
            currentSolutionSet = newSolutionSet;
        }
        return archive;
    } // execute
} // sMOCell2

