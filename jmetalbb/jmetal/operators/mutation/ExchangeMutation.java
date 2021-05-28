//  BitFlipMutation.java
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
package jmetal.operators.mutation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.variable.*;

/**
 * This class implements the m-Exchange mutation operator. ﻿ M. M. Drugan and D.
 * Thierens, “Path-Guided Mutation for Stochastic Pareto Local Search
 * Algorithms,” in Parallel Problem Solving from Nature, PPSN XI, vol. 6238
 * LNCS, no. PART 1, Berlin, Heidelberg: Springer Berlin Heidelberg, 2010, pp.
 * 485–495.
 */
public class ExchangeMutation extends Mutation {

    /**
     * Valid solution types to apply this operator
     */
    private static List VALID_TYPES = Arrays.asList(BinarySolutionType.class);

    private Integer mValue_ = null;

    /**
     * Constructor Creates a new instance of the Bit Flip mutation operator
     */
    public ExchangeMutation(HashMap<String, Object> parameters) {
        super(parameters);
        if (parameters.get("m") != null) {
            mValue_ = ((Integer) parameters.get("m")).intValue();
        }
    } // BitFlipMutation

    /**
     * Perform the mutation operation
     *
     * @param probability Mutation probability
     * @param solution The solution to mutate
     * @throws JMException
     */
    public void doMutation(int m, Solution solution) throws JMException {
        try {
            if (solution.getType().getClass() == BinarySolutionType.class) {
                //Creates the permutation
                jmetal.util.Permutation perm = new jmetal.util.Permutation();
                int[] p = perm.intPermutation(solution.getNumberOfBits());

                Binary bits = (Binary) solution.getDecisionVariables()[0];
                boolean temp;// 

                if (PseudoRandom.randDouble() < 0.5) {
                    temp = bits.getIth(p[0]);
                    for (int i = 0; i < m-1; i++) {
                        bits.setIth(p[i], bits.getIth(p[i+1]));
                    }
                    bits.setIth(p[m-1], temp);
                }
                else {
                    temp = bits.getIth(p[m-1]);
                    for (int i = m-1; i > 0; i--) {
                        bits.setIth(p[i], bits.getIth(p[i-1]));
                    }
                    bits.setIth(p[0], temp);
                }

            }
        } catch (ClassCastException e1) {
            Configuration.logger_.severe("ExchangeMutation.doMutation: "
                    + "ClassCastException error" + e1.getMessage());
            Class cls = java.lang.String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".doMutation()");
        }
    } // doMutation

    /**
     * Executes the operation
     *
     * @param object An object containing a solution to mutate
     * @return An object containing the mutated solution
     * @throws JMException
     */
    public Object execute(Object object) throws JMException {
        Solution solution = (Solution) object;

        if (!VALID_TYPES.contains(solution.getType().getClass())) {
            Configuration.logger_.severe("ExchangeMutation.execute: the solution "
                    + "is not of the right type. The type should be 'Binary', "
                    + ", but " + solution.getType() + " is obtained");

            Class cls = java.lang.String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".execute()");
        } // if 

        doMutation(mValue_, solution);
        return solution;
    } // execute
} // BitFlipMutation
