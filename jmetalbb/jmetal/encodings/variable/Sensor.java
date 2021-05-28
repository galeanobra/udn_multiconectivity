/**
 * Real.java
 *
 * @author juanjo durillo 
 * @version 1.0
 */
package jmetal.encodings.variable;

import jmetal.core.Variable;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;


/**
 * This class implements a Real value decision variable
 */
public class Sensor extends Variable
{

	/**
	 * Stores the value of the coordinate
	 */
	private int x_;
	private int y_;

	/**
	 * Stores whether the sensor is enabled or not
	 */
	private boolean deployed_;
	
	/**
	 * Stores whether the sensor is active or not
	 */
	private int actived_;

	/**
	 * Stores the lower bound of the real variable
	 */
	private int xSize_;

	/**
	 * Stores the upper bound of the real variable
	 */
	private int ySize_;

	/**
	 * Stores the lower bound of the real variable
	 */
	private double lowerBound_;

	/**
	 * Stores the upper bound of the real variable coordinate X
	 */
	private double upperBoundX_;
	
	/**
	 * Stores the upper bound of the real variable coordinate Y
	 */
	private double upperBoundY_;
	

	/**
	 * Constructor
	 */
	public Sensor() {
	} // Real


	/**
	 * Constructor
	 */
	public Sensor(double lowerBound, double upperBoundX, double upperBoundY){
		this.xSize_ = (int) upperBoundX;
		this.ySize_ = (int) upperBoundY;
		this.x_ = PseudoRandom.randInt(0, xSize_-1);
		this.y_ = PseudoRandom.randInt(0, ySize_-1);
		if (PseudoRandom.randDouble() < 0.5)
			this.deployed_ = true;
		else
			this.deployed_ = false;

		this.lowerBound_ = lowerBound;
		this.upperBoundX_ = upperBoundX;
		this.upperBoundY_ = upperBoundY;
		
	} //Sensor


	/** 
	 * Copy constructor.
	 * @param variable The variable to copy.
	 * @throws JMException 
	 */
	public Sensor(Variable variable) throws JMException{
		xSize_ = ((Sensor) variable).getXSize();
		ySize_ = ((Sensor) variable).getYSize();
		x_ = ((Sensor) variable).getX();
		y_ = ((Sensor) variable).getY();
		deployed_ = ((Sensor) variable).isDeployed();
		lowerBound_ = variable.getLowerBound();
		upperBoundX_ = ((Sensor) variable).getUpperBoundX();
		upperBoundY_ = ((Sensor) variable).getUpperBoundY();
	} //Real




	/** 
	 * Returns a exact copy of the <code>Real</code> variable
	 * @return the copy
	 */
	public Variable deepCopy(){
		try {
			return new Sensor(this);
		} catch (JMException e) {
			Configuration.logger_.severe("Real.deepCopy.execute: JMException");
			return null ;
		}
	} // deepCopy


	/**
	 * Gets the lower bound of the variable.
	 * @return the lower bound.
	 */
	public double getLowerBound() {
		return lowerBound_;
	} //getLowerBound
        
        public double getUpperBound() {
		return Math.min(upperBoundX_, upperBoundY_);
	} //getLowerBound

	/**
	 * Gets the upper bound of the variable.
	 * @return the X upper bound.
	 */
	public double getUpperBoundX() {
		return upperBoundX_;
	} // getUpperBound
	
	/**
	 * Gets the upper bound of the variable.
	 * @return the Y upper bound.
	 */
	public double getUpperBoundY() {
		return upperBoundY_;
	} // getUpperBound


	/**
	 * Sets the lower bound of the variable.
	 * @param lowerBound The lower bound.
	 */
	public void setLowerBound(double lowerBound)  {
		lowerBound_ = lowerBound;
	} // setLowerBound

	/**
	 * Sets the upper X bound of the variable.
	 * @param upperBound The upper bound.
	 */
	public void setUpperBoundX(double upperBoundX) {
		upperBoundX_ = upperBoundX;
	} // setUpperBound

	/**
	 * Sets the upper Y bound of the variable.
	 * @param upperBound The upper bound.
	 */
	public void setUpperBoundY(double upperBoundY) {
		upperBoundY_ = upperBoundY;
	} // setUpperBound


	/**
	 * Returns a string representing the object
	 * @return the string
	 */
	public String toString()
	{
		if (this.deployed_)
			//return "("+x_+","+y_+")";
			return "S[1,"+x_+","+y_ +"]";
		else
			return "S[0,"+x_+","+y_ +"]";
	} //toString


	public boolean isDeployed() {
		return deployed_;
	}


	public void setDeployed(boolean deployed) {
		this.deployed_ = deployed;
	}


	public int getX() {
		return x_;
	}


	public void setX(int x) {
		this.x_ = x;
	}


	public int getY() {
		return y_;
	}


	public void setY(int y) {
		this.y_ = y;
	}


	public int getXSize() {
		return xSize_;
	}


	public void setXSize(int size) {
		this.xSize_ = size;
	}


	public int getYSize() {
		return ySize_;
	}


	public void setYSize(int size) {
		this.ySize_ = size;
	}


	public int isActived() {
		return actived_;
	}


	public void setActived(int actived) {
		this.actived_ = actived;
	}

} // Real
