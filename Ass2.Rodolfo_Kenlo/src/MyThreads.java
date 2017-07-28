import HelloApp.Hello;
import HelloApp.HelloHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import HelloApp.*;

import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import org.omg.CORBA.*;

public class MyThreads extends Thread {
    String managerID;

	//CTOR
	public MyThreads(String managerID) {
		this.managerID = managerID;
	}
    public String getManagerID(){
		return managerID;
    }

    // helloImpl is an instance of the Hello class
    Hello helloImpl;

//	@Override
	public void start(String[] args) {

        try {
            // create and initialize the ORB
            ORB orb = ORB.init(args, null);
            // get the root naming context
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            // Use NamingContextExt instead of NamingContext. This is
            // part of the Interoperable naming Service.
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            // resolve the Object Reference in Naming
            String name = "MTL";
            helloImpl = HelloHelper.narrow(ncRef.resolve_str(name));
            System.out.println("Obtained a handle on server object: " + helloImpl);

            //multiThreaded record Creaction
            String[] specs = new String[2];
            specs[0]="mathematics";
            specs[1]="computer science";
            String createResult = helloImpl.createTRecord(managerID, managerID.toString(), managerID.toString(), managerID.toString(), managerID.toString(), specs, "MTL");
            System.out.println(createResult);

            //multiThreaded record Transfer
            String transferResult = helloImpl.transferRecord(managerID,"TR00001", "LVL");
            System.out.println(transferResult);


            // helloImpl.shutdown();
        } catch (Exception e) {
            System.out.println("ERROR : " + e);
            e.printStackTrace(System.out);
        }

    }
}
