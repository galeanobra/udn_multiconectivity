/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmetal.problems.UDN.model.users;

/**
 * @author paco
 */
public abstract class MobilityModel {

    abstract public void move(User u);

    public abstract double getMinV_();

    public abstract double getMaxV_();
}
