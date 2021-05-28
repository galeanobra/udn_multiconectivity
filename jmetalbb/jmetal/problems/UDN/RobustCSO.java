package jmetal.problems.UDN;

import java.lang.String;
import jmetal.core.Solution;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.variable.Binary;
import jmetal.problems.UDN.model.DynamicUDN;
import jmetal.util.JMException;

/**
 * Class representing problem ZDT1
 */
public class RobustCSO extends CSO {

    // The value for the H sample
    int samplingSize_;
    int currentSample_;

    //for updating the user positions
    boolean shanghai_ = false;

    /**
     * Creates an instance of the UDN planning problems.
     *
     * @throws java.lang.ClassNotFoundException
     */
    public RobustCSO(String mainConfig, int run, int sampleSize) throws ClassNotFoundException {

        //Create the UDN model
        udn_ = new DynamicUDN(mainConfig, run);

        numberOfVariables_ = 1;
        numberOfObjectives_ = 4;
        numberOfConstraints_ = 0;
        problemName_ = "RobustCSO";
        run_ = run;
        samplingSize_ = sampleSize;

        solutionType_ = new BinarySolutionType(this);

        length_ = new int[numberOfVariables_];
        length_[0] = udn_.getTotalNumberOfActivableCells();

        //get mobility info for updating the users position
        if (udn_.getMobilityType().equalsIgnoreCase("shanghai")) {
            shanghai_ = true;
        }
    }

    /**
     * Evaluates a solution.
     *
     * @param solution The solution to evaluate.
     * @throws JMException
     */
    @Override
    public void evaluate(Solution solution) throws JMException {
        Binary cso = ((Binary) solution.getDecisionVariables()[0]);

        //map the activation to the udn
        udn_.setCellActivation(cso);

        //update the avera
        udn_.computeSignaling();

        double sumCapacity = 0.0, sqSumCapacity = 0.0;
        double sumPower = 0.0, sqSumPower = 0.0;
        double capacity, powerConsumption;

        //Reset the sampling
        this.resetSampling();

        for (int t = 0; t < this.samplingSize_; t++) {
            capacity = networkCapacity(solution);
            powerConsumption = powerConsumptionPiovesan();

            sumCapacity += capacity;
            sqSumCapacity += capacity * capacity;

            sumPower += powerConsumption;
            sqSumPower += powerConsumption * powerConsumption;

            this.nextSample();

            //recompute signaling
            udn_.computeSignaling();
        }

        double meanCapacity = sumCapacity / this.samplingSize_;
        double varianceCapacity = (sqSumCapacity - (sumCapacity * sumCapacity) / this.samplingSize_) / (this.samplingSize_ - 1);

        double meanPower = sumPower / this.samplingSize_;
        double variancePower = (sqSumPower - (sumPower * sumPower) / this.samplingSize_) / (this.samplingSize_ - 1);

        solution.setObjective(0, meanPower);
        solution.setObjective(1, -variancePower);
        solution.setObjective(2, -meanCapacity);
        solution.setObjective(3, -varianceCapacity);

    } // evaluate

    public void nextSample() {
        this.currentSample_++;
        if (shanghai_)
            ((DynamicUDN)this.udn_).updateUsersPositionShanghai(this.currentSample_);
        else
            ((DynamicUDN)this.udn_).updateUsersPositionFromMatrix(this.currentSample_);

        //saving memory: recompute only interesiting points for the new epoch
        this.udn_.emptyMapsAtPoints();
    }

    private void resetSampling() {
        if (shanghai_)
            ((DynamicUDN)this.udn_).updateUsersPositionShanghai(0);
        else
            ((DynamicUDN)this.udn_).updateUsersPositionFromMatrix(0);

        //saving memory: recompute only interesiting points for the new epoch
        this.udn_.emptyMapsAtPoints();
    }






} // Planning UDN
