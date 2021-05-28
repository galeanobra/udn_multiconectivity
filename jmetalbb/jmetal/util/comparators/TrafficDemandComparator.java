//  SolutionComparator.java
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
package jmetal.util.comparators;

import java.util.Comparator;

import jmetal.problems.UDN.model.users.User;
import jmetal.util.JMException;

/**
 * This class implements a <code>Comparator</code> (a method for comparing
 * <code>Solution</code> objects) based on the values of the variables.
 */
public class TrafficDemandComparator implements Comparator {

    /**
     * Establishes a value of allowed dissimilarity
     */
    private static final double EPSILON = 1e-10;

    /**
     * Compares two solutions.
     *
     * @param o1 Object representing the first <code>Solution</code>.
     * @param o2 Object representing the second <code>Solution</code>.
     * @return 0, if both solutions are equals with a certain dissimilarity, -1
     * otherwise.
     * @throws JMException
     * @throws JMException
     */
    public int compare(Object o1, Object o2) {
        User u1, u2;
        u1 = (User) o1;
        u2 = (User) o2;

        if (u1.getTrafficDemand() < u2.getTrafficDemand()) {
            return 1;
        } else if (u1.getTrafficDemand() > u2.getTrafficDemand()) {
            return -1;
        } else {
            return 0;
        }

    } // compare
} // SolutionComparator
