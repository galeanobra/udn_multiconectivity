//  SolutionSet.Java
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
package jmetal.core;

import java.io.*;
import java.util.*;

import jmetal.util.comparators.DominanceComparator;
import jmetal.util.comparators.EqualSolutions;

/**
 * Class representing a SolutionSet (a set of solutions)
 */
public class UnboundedSolutionSet extends SolutionSet implements Serializable {

    DominanceComparator dominance_;
    Comparator equals_;

    /**
     * Constructor. Creates an unbounded solution set.
     */
    public UnboundedSolutionSet() {
        solutionsList_ = new ArrayList<Solution>();
        dominance_ = new DominanceComparator();
        equals_ = new EqualSolutions();
    } // SolutionSet

    /**
     * Inserts a new solution into the SolutionSet.
     *
     * @param solution The <code>Solution</code> to store
     * @return True If the <code>Solution</code> has been inserted, false
     * otherwise.
     */
    @Override
    public boolean add(Solution solution) {
        int flag = 0;
        int i = 0;
        Solution aux; //Store an solution temporally
        while (i < solutionsList_.size()) {
            aux = solutionsList_.get(i);

            flag = dominance_.compare(solution, aux);
            if (flag == 1) {               // The solution to add is dominated
//                System.out.println("solution[" + i + "] dominates current");
                return false;                // Discard the new solution
            } else if (flag == -1) {       // A solution in the archive is dominated
//                System.out.println("removing solution[" + i + "]");
                solutionsList_.remove(i);    // Remove it from the population            
            } else {
                boolean equal = true;
                int j = 0;
                while ((j < solution.numberOfObjectives()) && equal) {
                    if (aux.objective_[j] != solution.objective_[j])
                        equal = false;

                    j++;
                }
                if (equal) {
//                    System.out.println("solutions are equal");
                    return false;
                }
//
//                if (equals_.compare(aux, solution) == 0) { // There is an equal solution 
//                    // in the population
//                    return false; // Discard the new solution
//                }  // if
                i++;
            }
        }
        // Insert the solution into the archive
//        System.out.println("current added to the archive");
        solutionsList_.add(solution);
        return true;
    }

} // UnboundedSolutionSet

