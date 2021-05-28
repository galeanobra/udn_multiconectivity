//  SBXCrossover.java
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
package jmetal.operators.crossover;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import jmetal.core.*;
import jmetal.encodings.solutionType.SensorSolutionType;
import jmetal.encodings.variable.*;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

/**
 * This class allows to apply a SBX crossover operator using two parent
 * solutions.
 */
public class SensorRectangularCrossover extends Crossover {

    /**
     * EPS defines the minimum difference allowed between real values
     */


   
    private Double crossoverProbability_ = 0.9;
   

    /**
     * Valid solution types to apply this operator
     */
    private static List VALID_TYPES = Arrays.asList(SensorSolutionType.class);

    /**
     * Constructor Create a new SBX crossover operator whit a default index
     * given by <code>DEFAULT_INDEX_CROSSOVER</code>
     */
    public SensorRectangularCrossover(HashMap<String, Object> parameters) {
        super(parameters);

        if (parameters.get("probability") != null) {
            crossoverProbability_ = (Double) parameters.get("probability");
        }
        
    } // SBXCrossover

    /**
     * Perform the crossover operation.
     *
     * @param probability Crossover probability
     * @param parent1 The first parent
     * @param parent2 The second parent
     * @return An array containing the two offsprings
     */
    public Solution[] doCrossover(double probability,
            Solution parent0,
            Solution parent1) throws JMException {

        Solution[] offspring = new Solution[2];

        offspring[0] = new Solution(parent0);
        offspring[1] = new Solution(parent1);

        if (PseudoRandom.randDouble() <= probability) {
            //get the terrainSide
            int terrainSideX = (int) ((Sensor) parent0.getDecisionVariables()[0]).getUpperBoundX();
            int terrainSideY = (int) ((Sensor) parent0.getDecisionVariables()[0]).getUpperBoundY();
            //compute the side of the radius in which the exchange
            int squareSideX = PseudoRandom.randInt(30, terrainSideX / 2);
            int squareSideY = PseudoRandom.randInt(30, terrainSideY / 2);
            //locate the center of the square
            int x = PseudoRandom.randInt(0, terrainSideX - 1);
            int y = PseudoRandom.randInt(0, terrainSideY - 1);

            //compute the limits of the squares
            int x0 = Math.max(0, x - squareSideX / 2);
            int xN = Math.min(terrainSideX - 1, x + squareSideX / 2);
            int y0 = Math.max(0, y - squareSideY / 2);
            int yN = Math.min(terrainSideY - 1, y + squareSideY / 2);

//    	System.out.println("x = " + x);
//    	System.out.println("y = " + y);
//    	System.out.println("sqX = " + squareSideX);
//    	System.out.println("sqY = " + squareSideY);
//    	System.out.println("Antes:");
//    	printSolution(parent0, x0, y0, xN, yN);
//    	printSolution(parent1, x0, y0, xN, yN);
            //undeploy sensors in offsprings and return their number
            undeploy(offspring[0], x0, y0, xN, yN);
            undeploy(offspring[1], x0, y0, xN, yN);

            //locate the first free slot into the chromosome
            int freeSlot0 = findFreeSlot(offspring[0], 0);
            int freeSlot1 = findFreeSlot(offspring[1], 0);

            for (int s = 0; s < parent0.getDecisionVariables().length; s++) {
                //retrieve the sensor from the first parent
                Sensor s0 = ((Sensor) parent0.getDecisionVariables()[s]);
                int xS0 = s0.getX();
                int yS0 = s0.getY();
                boolean isS0Deployed = s0.isDeployed();

                //if it is inside the square and it is deployed, then deploy on the offspring 1
                if ((xS0 >= x0) && (xS0 <= xN) && (yS0 >= y0) && (yS0 <= yN) && (isS0Deployed)) {
                    //if there is any free slot, then insert
                    Sensor newSensor = new Sensor(s0);
                    if (freeSlot1 < offspring[1].getDecisionVariables().length) {
                        offspring[1].getDecisionVariables()[freeSlot1] = newSensor;
                        freeSlot1 = findFreeSlot(offspring[1], freeSlot1);
                    } else {
                        offspring[0].getDecisionVariables()[freeSlot0] = newSensor;
                        freeSlot0 = findFreeSlot(offspring[0], freeSlot0);
                    }
                }

                //retrieve the sensor from the second parent
                Sensor s1 = ((Sensor) parent1.getDecisionVariables()[s]);
                int xS1 = s1.getX();
                int yS1 = s1.getY();
                boolean isS1Deployed = s1.isDeployed();

                //if it is inside the square and it is deployed, then deploy on the offspring 1
                if ((xS1 >= x0) && (xS1 <= xN) && (yS1 >= y0) && (yS1 <= yN) && (isS1Deployed)) {
                    //if there is any free slot, then insert
                    Sensor newSensor = new Sensor(s1);
                    if (freeSlot0 < offspring[0].getDecisionVariables().length) {
                        offspring[0].getDecisionVariables()[freeSlot0] = newSensor;
                        freeSlot0 = findFreeSlot(offspring[0], freeSlot0);
                    } else {
                        offspring[1].getDecisionVariables()[freeSlot1] = newSensor;
                        freeSlot1 = findFreeSlot(offspring[1], freeSlot1);
                    }
                }
            }
//    	System.out.println("Despues:");
//    	printSolution(offspring[0], x0, y0, xN, yN);
//    	printSolution(offspring[1], x0, y0, xN, yN);
//    	System.out.println();

        } // if

        return offspring;
    } // doCrossover

    private int findFreeSlot(Solution solution, int position) {
        int i = position;
        while (i < solution.getDecisionVariables().length
                && ((Sensor) solution.getDecisionVariables()[i]).isDeployed()) {
            i++;
        }
        return i;
    }

    private int undeploy(Solution solution, int x0, int y0, int xN, int yN) {
        int undeployed = 0;

        //undeploy
        for (int i = 0; i < solution.getDecisionVariables().length; i++) {
            //retrieve the sensor
            Sensor s1 = ((Sensor) solution.getDecisionVariables()[i]);
            int tempX = s1.getX();
            int tempY = s1.getY();
            boolean isDeployed = s1.isDeployed();
            if ((tempX >= x0) && (tempX <= xN) && (tempY >= y0) && (tempY <= yN)) {
                if (isDeployed) {
                    s1.setDeployed(false);
                    undeployed++;
                }
            }
        }

        return undeployed;
    }

    /**
     * Executes the operation
     *
     * @param object An object containing an array of two parents
     * @return An object containing the offSprings
     */
    public Object execute(Object object) throws JMException {
        Solution[] parents = (Solution[]) object;

        if (parents.length != 2) {
            Configuration.logger_.severe("SBXCrossover.execute: operator needs two "
                    + "parents");
            Class cls = java.lang.String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".execute()");
        } // if

        if (!(VALID_TYPES.contains(parents[0].getType().getClass())
                && VALID_TYPES.contains(parents[1].getType().getClass()))) {
            Configuration.logger_.severe("SBXCrossover.execute: the solutions "
                    + "type " + parents[0].getType() + " is not allowed with this operator");

            Class cls = java.lang.String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".execute()");
        } // if 

        Solution[] offSpring;
        offSpring = doCrossover(crossoverProbability_,
                parents[0],
                parents[1]);

        //for (int i = 0; i < offSpring.length; i++)
        //{
        //  offSpring[i].setCrowdingDistance(0.0);
        //  offSpring[i].setRank(0);
        //} 
        return offSpring;
    } // execute 
} // SBXCrossover
