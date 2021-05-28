//  MOCell.java
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
import jmetal.util.Distance;
import jmetal.util.JMException;
import jmetal.util.Neighborhood;
import jmetal.util.Ranking;
import jmetal.util.archive.Archive;
import jmetal.util.archive.HypervolumeArchive;
import jmetal.util.comparators.CrowdingComparator;
import jmetal.util.comparators.DominanceComparator;

import java.util.Comparator;

/**
 * This class represents an asynchronous version of MOCell algorithm, combining
 * aMOCell2 and aMOCell3. It is the aMOCell4 variant described in:
 * A.J. Nebro, J.J. Durillo, F. Luna, B. Dorronsoro, E. Alba
 * "Design Issues in a Multiobjective Cellular Genetic Algorithm."
 * Evolutionary Multi-Criterion Optimization. 4th International Conference,
 * EMO 2007. Sendai/Matsushima, Japan, March 2007.
 */
public class MOCellhv extends Algorithm {

    public MOCellhv(Problem problem) {
        super(problem);
    }

    /**
     * Execute the algorithm
     *
     * @throws JMException
     */
    public SolutionSet execute() throws JMException, ClassNotFoundException {
        //Init the parameters
        int populationSize, archiveSize, maxEvaluations, evaluations;
        Operator mutationOperator, crossoverOperator, selectionOperator;
        SolutionSet currentPopulation;
        Archive archive;
        SolutionSet[] neighbors;
        Neighborhood neighborhood;
        Comparator dominance = new DominanceComparator();
        Comparator crowdingComparator = new CrowdingComparator();
        Distance distance = new Distance();

        // Read the parameters
        populationSize = ((Integer) getInputParameter("populationSize")).intValue();
        archiveSize = ((Integer) getInputParameter("archiveSize")).intValue();
        maxEvaluations = ((Integer) getInputParameter("maxEvaluations")).intValue();

        // Read the operators
        mutationOperator = operators_.get("mutation");
        crossoverOperator = operators_.get("crossover");
        selectionOperator = operators_.get("selection");

        // Initialize the variables
        currentPopulation = new SolutionSet(populationSize);
        archive = new HypervolumeArchive(archiveSize, problem_.getNumberOfObjectives());
        evaluations = 0;
        neighborhood = new Neighborhood(populationSize);
        neighbors = new SolutionSet[populationSize];

        // Create the initial population
        for (int i = 0; i < populationSize; i++) {
            Solution individual = new Solution(problem_);
            problem_.evaluate(individual);
            problem_.evaluateConstraints(individual);
            currentPopulation.add(individual);
            individual.setLocation(i);
            evaluations++;
        }

        // Main loop
        while (evaluations < maxEvaluations) {
            //System.out.println("Evals: " + evaluations) ;
            for (int ind = 0; ind < currentPopulation.size(); ind++) {
                Solution individual = new Solution(currentPopulation.get(ind));

                Solution[] parents = new Solution[2];
                Solution[] offSpring;

                //neighbors[ind] = neighborhood.getFourNeighbors(currentPopulation,ind);
                neighbors[ind] = neighborhood.getEightNeighbors(currentPopulation, ind);
                neighbors[ind].add(individual);

                // parents
                parents[0] = (Solution) selectionOperator.execute(neighbors[ind]);
                if (archive.size() > 0) {
                    parents[1] = (Solution) selectionOperator.execute(archive);
                } else {
                    parents[1] = (Solution) selectionOperator.execute(neighbors[ind]);
                }

                // Create a new individual, using genetic operators mutation and crossover
                offSpring = (Solution[]) crossoverOperator.execute(parents);
                mutationOperator.execute(offSpring[0]);

                // Evaluate individual an his constraints
                problem_.evaluate(offSpring[0]);
                problem_.evaluateConstraints(offSpring[0]);
                evaluations++;

                int flag = dominance.compare(individual, offSpring[0]);

                if (flag == 1) { //The new individual dominates
                    offSpring[0].setLocation(individual.getLocation());
                    currentPopulation.replace(offSpring[0].getLocation(), offSpring[0]);
                    archive.add(new Solution(offSpring[0]));
                } else if (flag == 0) { //The new individual is non-dominated
                    neighbors[ind].add(offSpring[0]);
                    offSpring[0].setLocation(-1);
                    Ranking rank = new Ranking(neighbors[ind]);
                    for (int j = 0; j < rank.getNumberOfSubfronts(); j++) {
                        distance.crowdingDistanceAssignment(rank.getSubfront(j),
                                problem_.getNumberOfObjectives());
                    }
                    Solution worst = neighbors[ind].worst(crowdingComparator);

                    if (worst.getLocation() == -1) { //The worst is the offspring
                        archive.add(new Solution(offSpring[0]));
                    } else {
                        offSpring[0].setLocation(worst.getLocation());
                        currentPopulation.replace(offSpring[0].getLocation(), offSpring[0]);
                        archive.add(new Solution(offSpring[0]));
                    }
                }
            }
        }
        return archive;
    } // while
} // MOCell

