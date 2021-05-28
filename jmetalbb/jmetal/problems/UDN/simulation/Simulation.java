/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmetal.problems.UDN.simulation;

import jmetal.util.PPP;

import java.util.List;

import jmetal.problems.UDN.model.UDN;
import jmetal.problems.UDN.model.users.User;

/**
 * @author paco
 */
public class Simulation {
    //simulation parameters
    UDN udn_;
    int t_;
    double tics_;
    int simulationTime_;

    /**
     * Parametrized constructor
     *
     * @param udn
     * @param simulationTime
     * @param tics
     */
    public Simulation(UDN udn, int simulationTime, double tics) {
        this.udn_ = udn;
        this.simulationTime_ = simulationTime;
        this.tics_ = tics;

        //initial user deployment
        initialUserPlacement();
    }

    public void run() {

    }

    private void initialUserPlacement() {
        List<User> users = this.udn_.getUsers();

        //create independent Poison Point Processes for deploying
        //the BTSs
        PPP btsPPP = new PPP(UDN.random_);
        //the social attractors
        PPP saPPP = new PPP(UDN.random_);
        //the users
        PPP userPPP = new PPP(UDN.random_);

        //deploy BTSs
        //deploy(btsPPP, 100);
    }
}
