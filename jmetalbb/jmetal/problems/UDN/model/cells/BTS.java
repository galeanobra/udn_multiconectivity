/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmetal.problems.UDN.model.cells;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLNumericArray;
import jmetal.problems.UDN.model.Point;
import jmetal.problems.UDN.model.UDN;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
//import static jmetal.problems.UDN.model.cells.Sector.patternFileLoaded_;

/**
 * @author paco
 */
public class BTS {

    protected static int uniqueId_ = 0;
    MLNumericArray antennaArray_;
    static boolean patternFileLoaded_ = false;
    int id_;

    //location
    int x_;
    int y_;
    int z_;
    Point point;

    // Sectors
    List<Sector> sectors_;
    double workingFrequency_;

    UDN udn_;
    String name;
    UDN.CellType type_;


    public BTS(int x, int y, int z, String typeName, double workingFrequency, String radiationPatternFile, UDN udn, String name) {
        this.id_ = uniqueId_;
        uniqueId_++;

        this.x_ = x;
        this.y_ = y;
        this.z_ = z;
        this.workingFrequency_ = workingFrequency;
        this.udn_ = udn;
        this.point = udn_.getGridPoint(x_, y_, z_);
        this.name = name;
        this.sectors_ = new LinkedList<>();
        
        //Assign BTS type
        if (typeName.equalsIgnoreCase("femto")) {
            this.type_ = UDN.CellType.FEMTO;
        } else if (typeName.equalsIgnoreCase("pico")) {
            this.type_ = UDN.CellType.PICO;
        } else if (typeName.equalsIgnoreCase("micro")) {
            this.type_ = UDN.CellType.MICRO;
        } else if (typeName.equalsIgnoreCase("macro")) {
            this.type_ = UDN.CellType.MACRO;
        }
       
        //Load propagation antenna only once
        MatFileReader reader = null;
        try {
            reader = new MatFileReader(radiationPatternFile);
        } catch (IOException ex) {
            Logger.getLogger(BTS.class.getName()).log(Level.SEVERE, null, ex);
        }
        antennaArray_ = (MLNumericArray) reader.getContent().get(radiationPatternFile.substring(0, radiationPatternFile.length() - 4));
        patternFileLoaded_ = true;
    }

    public BTS(BTS b) {
        this.id_ = b.id_;
        this.x_ = b.x_;
        this.y_ = b.y_;
        this.z_ = b.z_;
        this.workingFrequency_ = b.workingFrequency_;
        this.point = b.point;
        this.udn_ = b.udn_;
        this.name = b.name;
        this.sectors_ = b.sectors_;
        this.type_ = b.type_;
        this.antennaArray_ = b.antennaArray_;
    }

    public void addSectors(String type,
                           String name,
                           int numChainsTX,
                           int numSectors,
                           double transmittedPower,
                           double alfa, double beta, double delta,
                           double transmitterGain,
                           double receptorGain,
                           double workingFrequency,
                           double coverageRadius) {

        for (int i = 0; i < numSectors; i++) {
            //create sectors
            sectors_.add(new Sector(x_, y_, z_,
                    udn_,
                    this,
                    type,
                    name,
                    numChainsTX,
                    transmittedPower,
                    alfa,
                    beta,
                    delta,
                    transmitterGain,
                    receptorGain,
                    workingFrequency,
                    coverageRadius
            ));
        }
    }

    public int getId() {
        return id_;
    }

    public int getX() {
        return x_;
    }

    public int getY() {
        return y_;
    }

    public int getZ() {
        return z_;
    }

    public List<Sector> getSectors() {
        return sectors_;
    }

    public double getWorkingFrequency() {
        return workingFrequency_;
    }

    public Point getPoint() {
        return point;
    }

    public void setPoint(Point point) {
        this.point = point;
    }

    public int getNumberOfActiveCells(){
        int num = 0;
        for (Sector sector : this.sectors_){
            for(Cell cell : sector.cells_){
                if(cell.active_){
                    num++;
                }
            }
        }
        return num;
    }


    @Override
    public String toString() {
        return "BTS(" + x_ + "," + y_ + ')';
    }
}
