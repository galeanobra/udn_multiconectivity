package jmetal.problems.UDN;

import jmetal.core.Solution;
import jmetal.encodings.solutionType.BinarySolutionType;
import jmetal.encodings.variable.Binary;
import jmetal.problems.UDN.model.StaticUDN;
import jmetal.problems.UDN.model.cells.BTS;
import jmetal.problems.UDN.model.cells.Cell;
import jmetal.problems.UDN.model.cells.Sector;
import jmetal.util.JMException;

import java.util.List;
import java.util.TreeMap;

/**
 * Class representing problem ZDT1
 */
public class StaticCSO extends CSO {


    /**
     * Creates an instance of the Static CSO problem
     */
    public StaticCSO(String mainConfig, int run) throws ClassNotFoundException {

        //Create the UDN model
        udn_ = new StaticUDN(mainConfig, run);

        numberOfVariables_ = 1;
        numberOfObjectives_ = 2;
        numberOfConstraints_ = 0;
        problemName_ = "StaticCSO";
        run_ = run;

//        lowerLimit_ = new double[]{0};
//        upperLimit_ = new double[]{1};

        solutionType_ = new BinarySolutionType(this);

        length_ = new int[numberOfVariables_];
        length_[0] = udn_.getTotalNumberOfActivableCells();

        //udn_.printXVoronoi();
        //System.exit(-1);
    }

    public StaticCSO(String problemconf) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public StaticCSO(String mainconf, int run, int epochs) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

        boolean noActiveCells = true;
        for (int i = 0; i < cso.getNumberOfBits(); i++) {
            if (cso.getIth(i)) {
                noActiveCells = false;
                break;
            }
        }

        if (!noActiveCells) {
            //map the activation to the udn
            udn_.setCellActivation(cso);

            //update the avera
            udn_.computeSignaling();

            double capacity = networkCapacity(solution);
            double powerConsumption = powerConsumptionStatic();
            evaluateQoE();
            solution.setObjective(0, powerConsumption);
            solution.setObjective(1, -capacity);
            //System.out.println("Network capacity: " + capacity);
        } else {
            solution.setObjective(0, 0);
            solution.setObjective(1, 0);
        }
    } // evaluate

    /**
     * Calculates the power consumption taking into account the total traffic demand
     * and the maintenance power, in the case of small cells (pico, femto)
     *
     * @return
     */
    double powerConsumptionStatic() {
        double sum = 0.0;
        boolean hasActiveCells = false;
        double maintenancePower = 2000; //mW

        for (List<BTS> btss : udn_.btss_.values()) {
            for (BTS bts : btss) {
                hasActiveCells = false;
                for (Sector sector : bts.getSectors()) {
                    for (Cell cell : sector.getCells()) {
                        if (cell.isActive()) {
                            hasActiveCells = true;
                            sum += sector.getTransmittedPower() * sector.getAlfa() + sector.getBeta() + sector.getDelta() * cell.getTrafficDemand() + 10;
                        } else {
                            //residual consuption in sleep mode (mW)
                            sum += sector.getTransmittedPower() * 0.01;
                        }
                    }
                }
                if (hasActiveCells) {
                    sum += maintenancePower;
                }
            }
        }

        //mW -> W -> kW -> MW
        sum /= 1000000000;
        //System.out.println("Consumed power = " + sum);

        return sum;
    }// powerConsumptionStatic

    /**
     * Evaluates a solution.
     *
     * @param solution The solution to evaluate.
     * @throws JMException
     */

    public void evaluateMulti(Solution solution) throws JMException {
        Binary cso = ((Binary) solution.getDecisionVariables()[0]);
        TreeMap<String, Integer> QoE = new TreeMap<>();


        int N_signals = udn_.getN_signals();

        boolean noActiveCells = true;
        for (int i = 0; i < cso.getNumberOfBits(); i++) {
            if (cso.getIth(i)) {
                noActiveCells = false;
                break;
            }
        }

        if (!noActiveCells) {
            //map the activation to the udn
            udn_.setCellActivation(cso);

            //update the avera
            udn_.computeSignaling();

            double capacity = networkCapacityIntelligentMulticonnectivity(solution, N_signals);
            double powerConsumption = powerConsumptionStatic();
            evaluateQoE();
            solution.setObjective(0, powerConsumption);
            solution.setObjective(1, -capacity);

            //System.out.println("Network capacity: " + capacity);
        } else {
            solution.setObjective(0, 0);
            solution.setObjective(1, 0);
        }

    } // evaluateMulti

    public TreeMap getQoE() {
        return this.QoE_;
    }

    public Object getUDN() {

        return this.udn_;
    }
} // Planning UDN
