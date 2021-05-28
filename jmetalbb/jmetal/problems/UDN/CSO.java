package jmetal.problems.UDN;

import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.encodings.variable.Binary;
import jmetal.problems.UDN.model.Point;
import jmetal.problems.UDN.model.UDN;
import jmetal.problems.UDN.model.UDN.CellType;
import jmetal.problems.UDN.model.cells.BTS;
import jmetal.problems.UDN.model.cells.Cell;
import jmetal.problems.UDN.model.cells.Sector;
import jmetal.problems.UDN.model.users.User;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static jmetal.problems.UDN.model.UDN.CellType.*;

/**
 * Class representing problem ZDT1
 */
public abstract class CSO extends Problem {

    //The underlying UDN
    UDN udn_;
    TreeMap QoE_;

    //Operator Configuration

    //The seed to generate the instance
    int run_;

    public int getTotalNumberOfActivableCells() {
        return udn_.getTotalNumberOfActivableCells();
    }

    int pointsWithStatsComputed() {
        return udn_.pointsWithStatsComputed();
    }

    double powerConsumptionBasedOnTransmittedPower() {
        double sum = 0.0;

        for (List<Cell> cells : udn_.cells_.values()) {
            for (Cell c : cells) {
                if (c.isActive()) {
                    sum += 4.7 * c.getSector().getTransmittedPower();
                } else {
                    //residual consuption in sleep mode (mW)
                    sum += 160;
                }
            }
        }
        //mW -> W -> kW -> MW
        sum /= 1000000000;

        return sum;
    }

    /**
     * Calculates the power consumption taking into account the total traffic demand
     *
     * @return
     */
    double powerConsumptionPiovesan() {
        double sum = 0.0;

        for (List<Cell> cells : udn_.cells_.values()) {
            for (Cell c : cells) {
                Sector sector = c.getSector();
                if (c.isActive()) {
                    //sum += c.getBTS().getBaseConsumedPower() * 4.7 * c.getBTS().getTransmittedPower();
//                    double td = c.getTrafficDemand();
                    sum += sector.getTransmittedPower() * sector.getAlfa() + sector.getBeta() + sector.getDelta() * c.getTrafficDemand() + 10;
                } else {
                    //residual consuption in sleep mode (mW)
                    sum += sector.getTransmittedPower() * 0.01;
                }
            }
        }
        //mW -> W -> kW -> MW
        sum /= 1000000000;
        //System.out.println("Consumed power = " + sum);
        return sum;
    }

    double[][] loadH(BTS bts) {
        double[][] H = null;

        return H;
    }

    void saveCellStatus(Solution s) {
        Binary cso = ((Binary) s.getDecisionVariables()[0]);

        //map the activation to the udn
        udn_.copyCellActivation(cso);
    }

    /**
     * Max capacity of the 5G network. At each point, it returns the best SINR
     * for each of the different operating frequencies.
     *
     * @return
     */
    double networkCapacity(Solution solution) {
        /*
          For the dynamic problem addressing
         */
        List<Cell> assignment = new ArrayList<>();

        double capacity = 0.0;

        //0.- Reset number of users assigned to cells
        udn_.resetNumberOfUsersAssignedToCells();

        //1.- Assign users to cells, to compute the BW allocated to them
        for (User u : this.udn_.getUsers()) {
            u.setServingCell(udn_.getGridPoint(u.getX(), u.getY(), u.getZ()).getCellWithHigherSINR());
            u.getServingCell().addUserAssigned();

            //dynamic
            // assignment.add(u.getServingCell().getID());
            assignment.add(u.getServingCell());
        }

        //save the assignment into the solution
        solution.setUEsToCellAssignment(assignment);

        //1.- computes the Mbps allocated to each user
        for (User u : this.udn_.getUsers()) {
            double allocatedBW = u.getServingCell().getSharedBWForAssignedUsers();

            //computes the Mbps
            //double c = u.capacity(this.udn_, allocatedBW);
            double c = u.capacityMIMO(this.udn_, allocatedBW);
            //3.- Set the transmission rate to the user [Mbps]
            u.setTransmissionRate(c / 1000.0);//Mbps
            capacity += c / 1000.0;
        }

        //udn_.validateUserAssigned();
        return capacity;
    }

    /**
     * Max capacity of the 5G network. At each point, it returns the N signals
     * with best SINR for each of the different operating frequencies.
     *
     * @return
     */
    double networkCapacityWithMulticonnectivity(Solution solution, int NumberOfSignals) {
        /**
         * For the dynamic problem addressing
         */
        List<Cell> assignments = new ArrayList<>();
        ArrayList<Cell> NCell;
        double capacity = 0.0;

        //0.- Reset number of users assigned to cells
        udn_.resetNumberOfUsersAssignedToCells();
        udn_.resetUsersServingCellsAssigned();

        //1.- Assign users to cells, to compute the BW allocated to them
        for (User u : this.udn_.getUsers()) {

            Point p = udn_.getGridPoint(u.getX(), u.getY(), u.getZ());
            NCell = p.getCellsWithHigherSINR(NumberOfSignals, u);

            for (Cell c : NCell) {
                c.addUserAssigned();
                u.setServingCells(c);
                //dynamic
                assignments.add(c);
            }

        }

        //save the assignment into the solution
        solution.setUEsToCellAssignment(assignments);

        for (User u : this.udn_.getUsers()) {

            NCell = u.getServingCells();
            //1.- Initialize the variables
            double Total_Capacity_of_User = 0.0;
            double capacityofcell;

            //2.- Computes the Mbps allocated to each user for each serving cell
            for (Cell c : NCell) {

                //3.- computes the Mbps allocated to each user
                double allocatedBW = c.getSharedBWForAssignedUsers();

                //computes the Mbps of one cell
                capacityofcell = u.capacityMultiMIMO(this.udn_, allocatedBW, c);

                //adding to the user's total capacity
                Total_Capacity_of_User += capacityofcell;

            }
            //3.- Set the transmission rate to the user [Mbps]
            u.setTransmissionRate(Total_Capacity_of_User / 1000.0);//Mbps
            //4.- Adding to the global capacity
            capacity += Total_Capacity_of_User / 1000.0; //Mbps
        }

        return capacity;
    }

    /**
     * Max capacity of the 5G network. At each point, it returns the N signals
     * with best SINR for each of the different operating frequencies. If the QoE of a user
     * is below a threshold, Multi-connectivity will be applied for that user.
     *
     * @return
     */
    double networkCapacityIntelligentMulticonnectivity(Solution solution, int NumberOfSignals) {
        /**
         * For the dynamic problem addressing
         */
        List<Cell> assignments = new ArrayList<>();
        ArrayList<Cell> NCell;
        ArrayList<Cell> ServingCells;
        double capacity = 0.0;
        double Total_Capacity_of_User = 0.0;

        //0.- Reset number of users assigned to cells
        udn_.resetNumberOfUsersAssignedToCells();
        udn_.resetUsersServingCellsAssigned();

        //1.- Assign users to cells, to compute the BW allocated to them
        for (User u : this.udn_.getUsers()) {

            Point p = udn_.getGridPoint(u.getX(), u.getY(), u.getZ());
            NCell = p.getCellsWithHigherSINR(NumberOfSignals, u);

            for (Cell c : NCell) {
                c.addUserAssigned();
                u.setServingCells(c);
                //dynamic
                assignments.add(c);
            }

        }

        //save the assignment into the solution
        solution.setUEsToCellAssignment(assignments);

        for (User u : this.udn_.getUsers()) {
            NCell = u.getServingCells();
            Total_Capacity_of_User = 0.0;
            //1.- Computes the Mbps allocated to each user for each serving cell
            for (Cell c : NCell) {
                //3.- computes the Mbps allocated to each user
                double allocatedBW = c.getSharedBWForAssignedUsers();
                //computes the Mbps of one cell
                double capacityofcell = u.capacityMultiMIMO(this.udn_, allocatedBW, c);
                //adding to the user's total capacity
                Total_Capacity_of_User += capacityofcell;
            }
            //3.- Set the transmission rate to the user [Mbps]
            u.setTransmissionRate(Total_Capacity_of_User / 1000.0);//Mbps
            //4.- Adding to the global capacity
            capacity += Total_Capacity_of_User / 1000.0; //Mbps

        }
        return capacity;
    }

    public int getRun() {
        return this.run_;
    }

    private double numberOfActiveCells() {
        int count = 0;

        for (List<Cell> cells : udn_.cells_.values()) {
            for (Cell c : cells) {
                if (c.isActive()) {
                    count++;
                }

            }
        }

        return count;
    }

    /**
     * Function to evaluate Quality of Experience in 5G network.
     * Only considered 4 different services which can be grouped in eMBB.
     * For each user, the function calculates the MOS (0-5) for his demanded service.
     * The function returns each service´s average value
     */
    public void evaluateQoE() {

        TreeMap<String, Double> MOS = new TreeMap<>();
        String service;
        double FTP_media = 0.0, WEB_media = 0.0, NRT_media = 0.0, RT_media = 0.0;
        int[] Cont_User = {0, 0, 0, 0}; //Cont User for each service
        double aux_double = 0.0;
        double Mbps_standard = 2.00; //Mbps needed for good quality videocall
        double BTT = 884.736; //buffer threshold in kb (5,4% of 16384 kb) for HD quality
        double Rb;// Transmission Rate

        for (User u : this.udn_.getUsers()) {
            service = u.getServiceDemanded();
            Rb = u.getTransmissionRate() * 1000; // convert to Kbps
            switch (service) {
                case "FTP":
                    aux_double = (0.0065 * Rb - 0.54);
                    FTP_media += Math.min(aux_double, 5);
                    Cont_User[0]++;
                    break;
                case "WEB":
                    aux_double = 5 - (578 / (1 + ((Math.pow((Rb + 541.1) / 45.98, 2)))));
                    WEB_media += Math.min(aux_double, 5);
                    Cont_User[1]++;
                    break;
                case "NRT": //VoD: Youtube
                    aux_double = (1 + (5 - 1) * ((Rb) / BTT));
                    RT_media += Math.min(aux_double, 5);
                    Cont_User[2]++;
                    break;
                case "RT": //video-streaming: Skype
                    aux_double = (1 + (5 - 1) * ((Rb / 1000) / Mbps_standard));
                    NRT_media += Math.min(aux_double, 5);
                    Cont_User[3]++;
                    break;
                default:
                    break;
            }
            u.setQoE(Math.min(Math.round(aux_double * 100.0) / 100.0, 5));
        }
        // Set MOS: rounding to two decimal and not more than 5
        MOS.put("FTP", Math.min(Math.round((FTP_media / Cont_User[0]) * 100.0) / 100.0, 5));
        MOS.put("WEB", Math.min(Math.round((WEB_media / Cont_User[1]) * 100.0) / 100.0, 5));
        MOS.put("NRT", Math.min(Math.round((NRT_media / Cont_User[2]) * 100.0) / 100.0, 5));
        MOS.put("RT", Math.min(Math.round((RT_media / Cont_User[3]) * 100.0) / 100.0, 5));
        this.QoE_ = MOS;
    }


    /**
     * Sort a given list of Points by it SINR, being the worse the first
     *
     * @param l : list to sort
     * @return sorted list
     */
    public List<Point> sortList(List<Point> l) {
        double[] sinr_list = new double[l.size()];
        List<Point> sortedList = new ArrayList<>();
        double min_sinr = 5;

        for (int i = 0; i < l.size(); i++) {
            Point p = l.get(i);
            Cell c = p.getCellWithHigherSINR();
            double sinr = p.computeSINR(c);
            sinr_list[i] = sinr;

        }
        Arrays.sort(sinr_list);
        int index = 0;
        for (int i = 0; i < l.size(); i++) {
            for (int j = 0; j < l.size(); j++) {
                Point p_ = l.get(j);
                Cell c_ = p_.getCellWithHigherSINR();
                double sinr_ = p_.computeSINR(c_);
                if (Double.compare(sinr_, sinr_list[i]) == 0) {
                    index = j;
                    break;
                }
            }
            sortedList.add(i, l.get(index));
        }
        return sortedList;
    }

    /**
     * Given a user attached to the macrocell, it can be assigned to other cell
     * in case a certain condition is fulfilled.
     *
     * @param points
     */
    public void macro2Op(Map<Double, List<Point>> points) {
        double threshold = 6;
        double sinr_limit = 12;

        //get the macrocell
        double macro_f = 2000;
        Cell macro;
        macro = udn_.cells_.get(macro_f).get(0);
        List<User> macro_users = new ArrayList<>();

        //get the users assigned to the macrocell
        for (User u : this.udn_.getUsers()) {
            if (u.getServingCell() == macro) {
                Point p = udn_.getGridPoint(u.getX(), u.getY(), u.getZ());
                if (p.computeSINR(macro) < sinr_limit) {
                    macro_users.add(u);
                }
            }
        }
        //apply the operator for these users

        int count = 0;

        if (!macro_users.isEmpty()) {
            for (User u : macro_users) {
                Point p_ = udn_.getGridPoint(u.getX(), u.getY(), u.getZ());
                double sinr_macro = p_.computeSINR(macro);
                Cell other = p_.getCellWithHigherSINRButMacro();
                double sinr_other = p_.computeSINR(other);
                //Here comes the condition
                if ((sinr_macro - sinr_other) < threshold) {
                    u.setServingCell(other);
                    count++;
                }
            }
        }

        System.out.println("The operator macro2 has been applied " + count + " times");
    }

    /**
     * If more than a certain amount of users are connected to a cell, one of
     * them will be switched to the next better one
     */
    public void tooManyUsersOp() {
        int count = 0;
        int threshold = 3; //calcular la media de todos y que haya como máximo 2 veces la media
        Cell alternative = null;
        Point user_location;
        Map<Double, Cell> bestCells;

        for (User u : this.udn_.getUsers()) {
            user_location = udn_.getGridPoint(u.getX(), u.getY(), u.getZ());
            if (u.getServingCell().getAssignedUsers() >= threshold) {

                bestCells = user_location.getCellsWithBestSINRs();
                //get the 2nd best cell
                int i = 1;
                for (Map.Entry<Double, Cell> actualEntry : bestCells.entrySet()) {
                    if (i == 2) {
                        alternative = actualEntry.getValue();
                        break;
                    } else {
                        i++;
                    }
                }
                u.setServingCell(alternative);
                count++;
            }
        }
        System.out.println("The operator tooManyUsers has been applied " + count + " times");
    }

    /**
     * Switch on those femtocells that can serve UEs.
     *
     * @param solution
     */
    public void priorizeFemtoOp(double rate, Solution solution) {
        if (PseudoRandom.randDouble() < rate) {
            Binary cso = ((Binary) solution.getDecisionVariables()[0]);

            //map the activation to the udn
            udn_.setCellActivation(cso);

            //recompute the signaling
            udn_.computeSignaling();

            //reset the UEs assigned to celss
            udn_.resetNumberOfUsersAssignedToCells();

            //Assign users to cells, to compute the BW allocated to them
            for (User u : this.udn_.getUsers()) {
                Point p = udn_.getGridPoint(u.getX(), u.getY(), u.getZ());

                Cell c = p.getCellWithHigherSINR();

                c.addUserAssigned();

                u.setServingCell(c);
            }

            //Look for the the candidate femtocells
            double threshold = 1; //6 y 9 podrían valer: depende del tipo de celda origen: 6 dB por cada salto
            Cell alternative;
            Cell current;
            Point user_location;
            Map<Double, Cell> bestCells;

            for (User u : this.udn_.getUsers()) {
                if ((u.getServingCell().getType() != FEMTO) || (u.getServingCell().getType() != PICO)) {
                    current = u.getServingCell();
                    user_location = udn_.getGridPoint(u.getX(), u.getY(), u.getZ());
                    bestCells = user_location.getCellsWithBestSINRs();
                    for (Map.Entry<Double, Cell> actualEntry : bestCells.entrySet()) {
                        alternative = actualEntry.getValue();
                        if (user_location.computeSINR(alternative) > threshold) {
                            if ((alternative.getType() == FEMTO) || (alternative.getType() == PICO)) {
                                u.setServingCell(alternative);
                                alternative.addUserAssigned();
                                current.removeUserAssigned();
                                if (current.getAssignedUsers() == 0)
                                    current.setActivation(false);
                                alternative.setActivation(true);
                                break;
                            }
                        }
                    }
                }//IF
            }//FOR

            //apply CSO -> switch off the remaining cells not serving any UE
            //        for (double frequency : this.udn_.cells_.keySet()) {
            //            if (udn_.cells_.containsKey(frequency)) {
            //                List<Cell> l = udn_.cells_.get(frequency);
            //                for (Cell c : l) {
            //                    if (c.getAssignedUsers() == 0) {
            //                        c.setActivation(false);
            //                    }
            //                }
            //            }
            //
            //        }

            //Copy the modifications to the solution
            modifySolution(solution);
        }
    }


    /**
     * Switch on those small cells (pico and femto) that can serve UEs.
     *
     * @param solution
     */
    public void priorizeSmallCellsOp(double rate, Solution solution) {
        if (PseudoRandom.randDouble() < rate) {
            Binary cso = ((Binary) solution.getDecisionVariables()[0]);

            //map the activation to the udn
            udn_.setCellActivation(cso);

            //recompute the signaling
            udn_.computeSignaling();

            //reset the UEs assigned to celss
            udn_.resetNumberOfUsersAssignedToCells();

            //Assign users to cells, to compute the BW allocated to them
            for (User u : this.udn_.getUsers()) {
                Point p = udn_.getGridPoint(u.getX(), u.getY(), u.getZ());

                Cell c = p.getCellWithHigherSINR();

                c.addUserAssigned();

                u.setServingCell(c);
            }

            //Look for the the candidate femtocells
            double threshold = 1; //6 y 9 podrían valer: depende del tipo de celda origen: 6 dB por cada salto
            Cell alternative;
            Cell current;
            Point user_location;
            Map<Double, Cell> bestCells;

            for (User u : this.udn_.getUsers()) {
                if ((u.getServingCell().getType() != FEMTO) || (u.getServingCell().getType() != PICO)) {
                    current = u.getServingCell();
                    user_location = udn_.getGridPoint(u.getX(), u.getY(), u.getZ());
                    bestCells = user_location.getCellsWithBestSINRs();
                    for (Map.Entry<Double, Cell> actualEntry : bestCells.entrySet()) {
                        alternative = actualEntry.getValue();
                        if (user_location.computeSINR(alternative) > threshold) {
                            if ((alternative.getType() == FEMTO) || (alternative.getType() == PICO)) {
                                u.setServingCell(alternative);
                                alternative.addUserAssigned();
                                current.removeUserAssigned();
                                if (current.getAssignedUsers() == 0)
                                    current.setActivation(false);
                                alternative.setActivation(true);

                                //recompute the signaling
                                udn_.computeSignaling();

                                //reset the UEs assigned to celss
                                udn_.resetNumberOfUsersAssignedToCells();

                                //Assign users to cells, to compute the BW allocated to them
                                for (User us : this.udn_.getUsers()) {
                                    Point p = udn_.getGridPoint(us.getX(), us.getY(), us.getZ());

                                    Cell c = p.getCellWithHigherSINR();

                                    c.addUserAssigned();

                                    us.setServingCell(c);
                                }
                                break;
                            }
                        }
                    }
                }//IF
            }//FOR

            //apply CSO -> switch off the remaining cells not serving any UE
            for (double frequency : this.udn_.cells_.keySet()) {
                if (udn_.cells_.containsKey(frequency)) {
                    List<Cell> l = udn_.cells_.get(frequency);
                    for (Cell c : l) {
                        if (c.getAssignedUsers() == 0) {
                            c.setActivation(false);
                        }
                    }
                }

            }

            //Copy the modifications to the solution
            modifySolution(solution);
        }//if
    }


    /**
     * Turn off those BTSs that only have one active cell, saving the maintenance power
     *
     * @param rate     : application probability
     * @param solution : Solution to be modified by the operator
     */
    public void maintenancePowerOp(double rate, Solution solution) {
        if (PseudoRandom.randDouble() < rate) {
            for (List<BTS> btss : udn_.btss_.values()) {
                for (BTS bts : btss) {
                    if (bts.getNumberOfActiveCells() == 1) {
                        //Turn off the active cell
                        for (Sector sector : bts.getSectors()) {
                            for (Cell cell : sector.getCells()) {
                                if (cell.isActive()) {
                                    cell.setActivation(false);
                                    break;
                                }
                            }
                        }
                    }
                }
            }//for
            //System.out.println("The maintenancePowerOperator has turned off "+count+" cells");

            //Copy the modifications to the solution
            modifySolution(solution);
        }//if
    }


//    
//    /**
//     * Given a user attached to the a macro or microcell, it can be assigned to an small cell (femto, pico)
//     * in case the SINR received from the other is bigger than a certain
//     * threshold value.
//     *
//     * @param rate
//     * @param solution
//     */
//    public void smallCellsOp(double rate, Solution solution) {
//        int count = 0;
//        if (PseudoRandom.randDouble() < rate) {
//            Binary cso = ((Binary) solution.getDecisionVariables()[0]);
//
//            //map the activation to the udn
//            udn_.setCellActivation(cso);
//
//            //recompute the signaling
//            udn_.computeSignaling();
//
//            //reset the UEs assigned to celss
//            udn_.resetNumberOfUsersAssignedToCells();
//
//            //Assign users to cells, to compute the BW allocated to them
//            for (User u : this.udn_.getUsers()) {
//                Point p = udn_.getGridPoint(u.getX(), u.getY(), u.getZ());
//
//                Cell c = p.getCellWithHigherSINR();
//
//                c.addUserAssigned();
//
//                u.setServingCell(c);
//            }
//
//            double threshold = 1;
//
//            List<User> micro_users = new ArrayList<>();
//
//            //get the users assigned to a microcell
//            for (User u : this.udn_.getUsers()) {
//                Cell uCell = u.getServingCell();
//                if (uCell.getType() == CellType.MICRO) {
//                    Point p = udn_.getGridPoint(u.getX(), u.getY(), u.getZ());
//                    micro_users.add(u);
//                }
//            }
//            //apply the operator for these users
//
//            if (!micro_users.isEmpty()) {
//                for (int i = 0; i < micro_users.size(); i++) {
//                    User u = micro_users.get(i);
//                    Cell current = u.getServingCell();
//                    Point p_ = udn_.getGridPoint(u.getX(), u.getY(), u.getZ());
//                    Cell alternative = p_.getSmallCellWithHigherSINR();
//                    double sinr_alternative = p_.computeSINR(alternative);
//                    if (sinr_alternative > threshold) {
//                        u.setServingCell(alternative);
//                        alternative.addUserAssigned();
//                        current.removeUserAssigned();
//                        count++;
//                    }
//                }
//            }
//            //Copy the modifications to the solution
//            modifySolution(solution);
//        }//if
//        
//       // System.out.println("The operator smallCellOp has been applied " + count + " times");
//        
//    }

    /**
     * Activates/deactivates BTSs in the solution according to the information
     * enclosed in the modified network of the problem
     *
     * @param solution
     */
    public void modifySolution(Solution solution) {
        Binary cso = ((Binary) solution.getDecisionVariables()[0]);
        int bts = 0;

        for (List<Cell> cells : udn_.cells_.values()) {
            for (Cell c : cells) {
                if (c.getType() != CellType.MACRO) {
                    cso.setIth(bts, c.isActive());
                    bts++;
                }
            }
        }
    }

} // CSO
