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
public class SensorSBXCrossover extends Crossover {
  /**
   * EPS defines the minimum difference allowed between real values
   */
  protected static final double EPS= 1.0e-14;
                                                                                      
  public static final double ETA_C_DEFAULT_ = 20.0;  
  private Double crossoverProbability_ = 0.9 ;
  private double distributionIndex_ = ETA_C_DEFAULT_;

  /**
   * Valid solution types to apply this operator 
   */
  private static List VALID_TYPES = Arrays.asList(SensorSolutionType.class) ;
  
  /** 
   * Constructor
   * Create a new SBX crossover operator whit a default
   * index given by <code>DEFAULT_INDEX_CROSSOVER</code>
   */
  public SensorSBXCrossover(HashMap<String, Object> parameters) {
  	super (parameters) ;
  	
  	if (parameters.get("probability") != null)
  		crossoverProbability_ = (Double) parameters.get("probability") ;  		
  	if (parameters.get("distributionIndex") != null)
  		distributionIndex_    = (Double) parameters.get("distributionIndex") ;  		
  } // SBXCrossover
    
  /**
   * Perform the crossover operation. 
   * @param probability Crossover probability
   * @param parent1 The first parent
   * @param parent2 The second parent
   * @return An array containing the two offsprings
   */
  public Solution[] doCrossover(double probability, 
                                Solution parent1, 
                                Solution parent2) throws JMException {
    
    Solution [] offSpring = new Solution[2];

    offSpring[0] = new Solution(parent1);
    offSpring[1] = new Solution(parent2);
                    
    int i;
    double rand;
    double y1, y2, yL, yu;
    double c1, c2;
    double alpha, beta, betaq;
    double valueX1,valueX2;
    if (PseudoRandom.randDouble() <= probability)
    {
      for (i=0; i<parent1.getDecisionVariables().length; i++)
      {
    	  //retrieve the sensor
		  Sensor s1 = ((Sensor) parent1.getDecisionVariables()[i]);
		  Sensor s2 = ((Sensor) parent2.getDecisionVariables()[i]);
    	  if (PseudoRandom.randDouble()<=0.5 )
    	  {
    		  //if no parent has this sensor deployed, then undeploy them in the two offspring
    		  if (!s1.isDeployed() && !s2.isDeployed())
    		  {
    			  Sensor o1 = ((Sensor) offSpring[0].getDecisionVariables()[i]);
    			  Sensor o2 = ((Sensor) offSpring[1].getDecisionVariables()[i]);
    			  o1.setDeployed(false);
    			  o2.setDeployed(false);
    		  }
    		  else if (s1.isDeployed() && !s2.isDeployed())
    		  { //if paren1 has the senson deployed and parent2 does not
    			//the offsprings will have this sensor deployed with 50% probability
    			  //copy to offspring1 from parent1
    			  if (PseudoRandom.randDouble()<=0.5 )
    			  {
    				  Sensor s = new Sensor(s1);
    				  offSpring[0].getDecisionVariables()[i] = s;
    			  }
    			  //copy to offspring2 from parent1
    			  if (PseudoRandom.randDouble()<=0.5 )
    			  {
    				  Sensor s = new Sensor(s1);
    				  offSpring[1].getDecisionVariables()[i] = s;
    			  }
    		  }
    		  else if (!s1.isDeployed() && s2.isDeployed())
    		  { //if paren2 has the senson deployed and parent1 does not
    			//the offsprings will have this sensor deployed with 50% probability
    			  //copy to offspring1 from parent1
    			  if (PseudoRandom.randDouble()<=0.5 )
    			  {
    				  Sensor s = new Sensor(s2);
    				  offSpring[0].getDecisionVariables()[i] = s;
    			  }
    			  //copy to offspring2 from parent1
    			  if (PseudoRandom.randDouble()<=0.5 )
    			  {
    				  Sensor s = new Sensor(s2);
    				  offSpring[1].getDecisionVariables()[i] = s;
    			  }
    			  
    		  }
    		  else  //the sensor in the two parents is deployed -> then SBX to the two coordinates
    		  {
    			  //SBX on the x coordinate
    		   	  valueX1 = s1.getX();
    	    	  valueX2 = s2.getX();
    			  if (java.lang.Math.abs(valueX1- valueX2) > EPS)
    			  {

    				  if (valueX1 < valueX2){
    					  y1 = valueX1;
    					  y2 = valueX2;
    				  } else {
    					  y1 = valueX2;
    					  y2 = valueX1;
    				  } // if                       

    				  yL = s1.getLowerBound();
    				  yu = s1.getUpperBound();
    				  rand = PseudoRandom.randDouble();
    				  beta = 1.0 + (2.0*(y1-yL)/(y2-y1));
    				  alpha = 2.0 - java.lang.Math.pow(beta,-(distributionIndex_+1.0));

    				  if (rand <= (1.0/alpha)){
    					  betaq = java.lang.Math.pow ((rand*alpha),(1.0/(distributionIndex_+1.0)));
    				  } else {
    					  betaq = java.lang.Math.pow ((1.0/(2.0 - rand*alpha)),(1.0/(distributionIndex_+1.0)));
    				  } // if

    				  c1 = 0.5*((y1+y2)-betaq*(y2-y1));
    				  beta = 1.0 + (2.0*(yu-y2)/(y2-y1));
    				  alpha = 2.0 - java.lang.Math.pow(beta,-(distributionIndex_+1.0));

    				  if (rand <= (1.0/alpha)){
    					  betaq = java.lang.Math.pow ((rand*alpha),(1.0/(distributionIndex_+1.0)));
    				  } else {
    					  betaq = java.lang.Math.pow ((1.0/(2.0 - rand*alpha)),(1.0/(distributionIndex_+1.0)));
    				  } // if

    				  c2 = 0.5*((y1+y2)+betaq*(y2-y1));

    				  if (c1<yL) c1=yL;
    				  if (c2<yL) c2=yL;
    				  if (c1>yu) c1=yu;
    				  if (c2>yu) c2=yu;                        

    				  if (PseudoRandom.randDouble()<=0.5) {
    					  ((Sensor) offSpring[0].getDecisionVariables()[i]).setX((int)c2);
    					  ((Sensor) offSpring[1].getDecisionVariables()[i]).setX((int)c1);
    				  } else {
    					  ((Sensor) offSpring[0].getDecisionVariables()[i]).setX((int)c1);
    					  ((Sensor) offSpring[1].getDecisionVariables()[i]).setX((int)c2);                            
    				  } // if
    			  } else {
    				  ((Sensor) offSpring[0].getDecisionVariables()[i]).setX((int)valueX1);
    				  ((Sensor) offSpring[1].getDecisionVariables()[i]).setX((int)valueX2);                        
    			  } // if
    			  
    			  //SBX on the Y coordinate
    		   	  valueX1 = s1.getY();
    	    	  valueX2 = s2.getY();
    			  if (java.lang.Math.abs(valueX1- valueX2) > EPS)
    			  {

    				  if (valueX1 < valueX2){
    					  y1 = valueX1;
    					  y2 = valueX2;
    				  } else {
    					  y1 = valueX2;
    					  y2 = valueX1;
    				  } // if                       

    				  yL = s1.getLowerBound();
    				  yu = s1.getUpperBound();
    				  rand = PseudoRandom.randDouble();
    				  beta = 1.0 + (2.0*(y1-yL)/(y2-y1));
    				  alpha = 2.0 - java.lang.Math.pow(beta,-(distributionIndex_+1.0));

    				  if (rand <= (1.0/alpha)){
    					  betaq = java.lang.Math.pow ((rand*alpha),(1.0/(distributionIndex_+1.0)));
    				  } else {
    					  betaq = java.lang.Math.pow ((1.0/(2.0 - rand*alpha)),(1.0/(distributionIndex_+1.0)));
    				  } // if

    				  c1 = 0.5*((y1+y2)-betaq*(y2-y1));
    				  beta = 1.0 + (2.0*(yu-y2)/(y2-y1));
    				  alpha = 2.0 - java.lang.Math.pow(beta,-(distributionIndex_+1.0));

    				  if (rand <= (1.0/alpha)){
    					  betaq = java.lang.Math.pow ((rand*alpha),(1.0/(distributionIndex_+1.0)));
    				  } else {
    					  betaq = java.lang.Math.pow ((1.0/(2.0 - rand*alpha)),(1.0/(distributionIndex_+1.0)));
    				  } // if

    				  c2 = 0.5*((y1+y2)+betaq*(y2-y1));

    				  if (c1<yL) c1=yL;
    				  if (c2<yL) c2=yL;
    				  if (c1>yu) c1=yu;
    				  if (c2>yu) c2=yu;                        

    				  if (PseudoRandom.randDouble()<=0.5) {
    					  ((Sensor) offSpring[0].getDecisionVariables()[i]).setY((int)c2);
    					  ((Sensor) offSpring[1].getDecisionVariables()[i]).setY((int)c1);
    				  } else {
    					  ((Sensor) offSpring[0].getDecisionVariables()[i]).setY((int)c1);
    					  ((Sensor) offSpring[1].getDecisionVariables()[i]).setY((int)c2);                            
    				  } // if
    			  } else {
    				  ((Sensor) offSpring[0].getDecisionVariables()[i]).setY((int)valueX1);
    				  ((Sensor) offSpring[1].getDecisionVariables()[i]).setY((int)valueX2);                        
    			  } // if
    		  } 
    	  } else {
    		  Sensor s = new Sensor(s2);
			  offSpring[0].getDecisionVariables()[i] = s;
			  s = new Sensor(s1);
			  offSpring[1].getDecisionVariables()[i] = s;
    	  } // if
      } // if
    } // if

    return offSpring;   
  } // doCrossover
  
  
  /**
  * Executes the operation
  * @param object An object containing an array of two parents
  * @return An object containing the offSprings
  */
  public Object execute(Object object) throws JMException {
    Solution [] parents = (Solution [])object;    	
    	
    if (parents.length != 2) {
      Configuration.logger_.severe("SBXCrossover.execute: operator needs two " +
          "parents");
      Class cls = java.lang.String.class;
      String name = cls.getName(); 
      throw new JMException("Exception in " + name + ".execute()") ;      
    } // if

    if (!(VALID_TYPES.contains(parents[0].getType().getClass())  &&
          VALID_TYPES.contains(parents[1].getType().getClass())) ) {
      Configuration.logger_.severe("SBXCrossover.execute: the solutions " +
					"type " + parents[0].getType() + " is not allowed with this operator");

      Class cls = java.lang.String.class;
      String name = cls.getName(); 
      throw new JMException("Exception in " + name + ".execute()") ;
    } // if 
         
    Solution [] offSpring;
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
