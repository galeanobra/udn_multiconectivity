package jmetal.encodings.solutionType;

import java.io.Serializable;
import jmetal.core.Problem;
import jmetal.core.SolutionType;
import jmetal.core.Variable;
import jmetal.encodings.variable.Sensor;

public class SensorSolutionType extends SolutionType implements Serializable {
	private double upperLimitX_;
	private double upperLimitY_;
	
	public SensorSolutionType(Problem problem, double upperLimitX, double upperLimitY) throws ClassNotFoundException {
		super(problem);
		this.upperLimitX_=upperLimitX;
		this.upperLimitY_=upperLimitY;
		problem.setSolutionType(this) ;
	}

	
        @Override
	public Variable[] createVariables() throws ClassNotFoundException {
		Variable[] variables = new Variable[problem_.getNumberOfVariables()];
		for (int var = 0; var < problem_.getNumberOfVariables(); var++){
			variables[var] = new Sensor(problem_.getLowerLimit(var),
					upperLimitX_,upperLimitY_); 
		}
		return variables ;

	}

}
