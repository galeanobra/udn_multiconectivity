package jmetal.problems.UDN;

import jmetal.problems.UDN.model.UDN;
import jmetal.problems.UDN.model.users.User;
import java.io.FileWriter;
import java.io.PrintWriter;


public class SaveOnFile {
    public SaveOnFile(UDN udn, String filename){

        FileWriter file = null;
        PrintWriter pw;

        try
        {

            file = new FileWriter("Results\\Results "+ filename + ".txt");
            pw = new PrintWriter(file);

            pw.println("User\t\tTransmission Rate(Mbps)\t\t\tService Demanded\t\tQoE");
            for (User u: udn.getUsers()){
                pw.println( u + "\t\t" + u.getTransmissionRate()  + "\t\t\t\t\t"
                        + u.getServiceDemanded() + "\t\t\t\t" + u.getQoE());
            }
            //Math.round((u.getTransmissionRate())*100.0)/100.0
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != file)
                    file.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }
}
