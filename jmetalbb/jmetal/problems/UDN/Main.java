/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmetal.problems.UDN;

import jmetal.core.Solution;
import jmetal.encodings.variable.Binary;
import jmetal.util.JMException;

/**
 * @author paco
 */
public class Main {

    public static void main(String args[]) throws ClassNotFoundException, JMException {


        //UDN udn = new UDN("main.conf", "cells.conf", "users.conf");
        /*PlanningUDN p = new PlanningUDN("main.conf", "cells.conf", "socialAttractors.conf", "users.conf");
        
        //udn.printPropagationRegion();

        //udn.printVoronoi();
        Solution planning = new Solution(p);
        p.setBasicPlanning(planning);
        p.evaluate(planning);
        p.setHigherCapacityPlanning(planning);
        p.evaluate(planning);
//        Simulation sim = new Simulation(udn,60,1.0);
        //udn.printUsers(); */
        int simTime = 0;
        StaticCSO cso = new StaticCSO("main.conf", 0);

        //Solution s = new Solution(cso);
        Binary vars = new Binary(cso.getNumberOfBits());
        for (int i = 0; i < vars.getNumberOfBits(); i++) {
            vars.setIth(i, true);
        }

        Solution s = new Solution(cso, new Binary[]{vars});

        cso.evaluate(s);
        System.out.println("Number of active cells: " + cso.udn_.getTotalNumberOfActiveCells());
        cso.udn_.printGridNew();   // Comment for no debug info
        //cso.evaluate(s);
         //cso.udn_.printGridNew(); 

        //System.out.println("s = " + s);
        //System.out.println("visited points = " + cso.pointsWithStatsComputed());
        System.out.println("Maximum Capacity of the network = " + s.getObjective(1));
        System.out.println("Power Consumption of the network = " + s.getObjective(0));
        System.out.println("Quality of Experience: " + cso.getQoE());
        System.out.println("visited points = " + cso.pointsWithStatsComputed());

        System.out.println();
        System.out.println("With Multi-Connectivity: ");

        cso.evaluateMulti(s);
        System.out.println("Maximum Capacity of the network = " + s.getObjective(1));
        System.out.println("Power Consumption of the network = " + s.getObjective(0));
        System.out.println("Quality of Experience: " + cso.getQoE());
        System.out.println("visited points = " + cso.pointsWithStatsComputed());

    }
}
