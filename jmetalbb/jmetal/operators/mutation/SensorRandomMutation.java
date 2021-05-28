//  PolynomialMutation.java
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
import jmetal.encodings.solutionType.SensorSolutionType;
import jmetal.encodings.variable.Sensor;

/**
 * This class implements a polynomial mutation operator.
 */
public class SensorRandomMutation extends Mutation {

    private Double mutationProbability_ = null;

    /**
     * Valid solution types to apply this operator
     */
    private static List VALID_TYPES = Arrays.asList(SensorSolutionType.class);

    /**
     * Constructor Creates a new instance of the polynomial mutation operator
     */
    public SensorRandomMutation(HashMap<String, Object> parameters) {
        super(parameters);
        if (parameters.get("probability") != null) {
            mutationProbability_ = (Double) parameters.get("probability");
        }
    } // PolynomialMutation

    /**
     * Perform the mutation operation
     *
     * @param probability Mutation probability
     * @param solution The solution to mutate
     * @throws JMException
     */
    public void doMutation(double probability, Solution solution) throws JMException {
        for (int var=0; var < solution.getDecisionVariables().length; var++)
    {
      if (PseudoRandom.randDouble() <= probability)
      {
    	  Sensor s = ((Sensor) solution.getDecisionVariables()[var]);
    	  //if the sensor is not deployed, then deploy
    	  if (!s.isDeployed())
    	  {
    		  Sensor newSensor = new Sensor(s.getLowerBound(),s.getUpperBoundX(),s.getUpperBoundY());
    		  //deploy the sensor
    		  newSensor.setDeployed(true);
    		  solution.getDecisionVariables()[var] = newSensor;
    	  }
    	  else 
    	  {//the sensor is deployed, so either move or remove it
    		  if (PseudoRandom.randDouble() < 0.5)
    		  { //remove
    			  s.setDeployed(false);
    		  }
    		  else
    		  { //move

    			  //get terrainSide
    			  int terrainSide = (int) ((Sensor) solution.getDecisionVariables()[0]).getUpperBoundX();
    			  //apply Polynomial Mutation to coordinate X
    			  int coordinate = PseudoRandom.randInt(0, terrainSide -1); 
    			  ((Sensor) solution.getDecisionVariables()[var]).setX(coordinate);
    			  //apply Polynomial Mutation to to coordinate y
    			  terrainSide = (int) ((Sensor) solution.getDecisionVariables()[0]).getUpperBoundY();
    			  coordinate = PseudoRandom.randInt(0, terrainSide -1);
    			  ((Sensor) solution.getDecisionVariables()[var]).setY(coordinate);
    		  }
    	  }
      }
    }            
    } // doMutation

    /**
     * Executes the operation
     *
     * @param object An object containing a solution
     * @return An object containing the mutated solution
     * @throws JMException
     */
    public Object execute(Object object) throws JMException {
        Solution solution = (Solution) object;

        if (!VALID_TYPES.contains(solution.getType().getClass())) {
            Configuration.logger_.severe("PolynomialMutation.execute: the solution "
                    + "type " + solution.getType() + " is not allowed with this operator");

            Class cls = java.lang.String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".execute()");
        } // if 

        doMutation(mutationProbability_, solution);
        return solution;
    } // execute

} // PolynomialMutation
