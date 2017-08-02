package ServersImpl;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Manager_Thread extends Thread {
    static String locationname;
    String function = "";
    String thread_name = "";
    static String[] arg;

    Manager_Thread(String[] args, String name, String test) {
        thread_name = name;
        function = test;
        arg = args;
    }
    //Reference to Logger class is generated

    Boolean success = false;


    public void run()                                                //client thread statrs execution
    {
        try {

            Logger logger = Logger.getLogger(Manager_Thread.class.getName());
            logger.setUseParentHandlers(false);
            File f = new File("D:/JAVA/DSassg2/" + thread_name + ".txt");
            if (!f.exists())
                f.createNewFile();

            Handler fileHandler = new FileHandler("D:/JAVA/DSassg2/" + thread_name + ".txt", true);//File for writting log is opened in append mode
            SimpleFormatter plainText = new SimpleFormatter();
            fileHandler.setFormatter(plainText);
            logger.addHandler(fileHandler);
            logger.info("User " + thread_name + "logged in");

            locationname = thread_name.substring(0, 3);
            DSassg2.ServerInterface server = connectToServer(arg, locationname);

            switch (function) {
                case "newrecord":
                    recordcreation(server, thread_name, logger);
                    break;
                case "edit": {
                    int count = 1;
                    while (count < 30) {
                        editrecord(server, thread_name, count, logger);
                        count++;
                    }
                }
                break;
                case "count":
                    getcount(server, thread_name, logger);
                    break;
                case "transfer": {
                    int count = 1;
                    while (count < 2) {
                        transfer(server, thread_name, logger, count);
                        count++;
                    }
                }
                break;
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }


    public static void main(String arg[]) throws RemoteException, NotBoundException, Exception {

        String username = "";

        for (int i = 1; i <= 30; i++) {
            String formatted = String.format("%05d", i);
            if (i <= 10) {
                username = "LVL" + formatted;
            } else if (i > 10 && i <= 20) {
                username = "DDO" + formatted;
            } else if (i > 20 && i <= 30) {
                username = "MTL" + formatted;
            }

            Manager_Thread c = new Manager_Thread(arg, username, "newrecord");            //run the new client thread
            c.start();
        }

        sleep(5000);

        DSassg2.ServerInterface server = connectToServer(arg, "MTL");
        System.out.println(server.printAllRecords("MTL00001"));
        DSassg2.ServerInterface server1 = connectToServer(arg, "LVL");
        System.out.println(server1.printAllRecords("LVL00001"));
        DSassg2.ServerInterface server2 = connectToServer(arg, "DDO");
        System.out.println(server2.printAllRecords("DDO00001"));
/*
        for(int i=1;i<=1;i++)
		{
			String formatted = String.format("%05d", i);
			if(i<=1) {	username = "LVL" + formatted;	}

			else if(i>1&&i<=20)	{username = "DDO" + formatted;}

			else	{username = "MTL" + formatted;}

			Manager_Thread c = new Manager_Thread(arg,username,"count");
			c.start();
		}
/*
		for(int i=1;i<=30;i++)
		{
			String formatted = String.format("%05d", i);
			if(i<=10) {	username = "LVL" + formatted;	}

			else if(i>10&&i<=20)	{username = "DDO" + formatted;}

			else	{username = "MTL" + formatted;}

			Manager_Thread c = new Manager_Thread(arg,username,"edit");            //run the new client thread
			c.start();
		}

		sleep(6000);

		System.out.println(server.printAllRecords("MTL00001"));
		System.out.println(server1.printAllRecords("LVL00001"));
		System.out.println(server2.printAllRecords("DDO00001"));




		for(int i=1;i<4;i++)
		{
			String formatted = String.format("%05d", i);
			if(i<=1) {	username = "LVL" + formatted;	}

			else if(i==2)	{username = "DDO" + formatted;}

			else	{username = "MTL" + formatted;}

			Manager_Thread c = new Manager_Thread(arg ,username,"transfer");            //run the new client thread
			c.start();
		}

		sleep(7000);
		DSassg2.ServerInterface server3 = connectToServer(arg,"MTL");
		System.out.println(server3.printAllRecords("MTL00001"));
		DSassg2.ServerInterface server4 = connectToServer(arg,"LVL");
		System.out.println(server4.printAllRecords("LVL00001"));
		DSassg2.ServerInterface server5 = connectToServer(arg,"DDO");
		System.out.println(server5.printAllRecords("DDO00001"));
*/
        sleep(2000);
        DSassg2.ServerInterface server7 = connectToServer(arg, "MTL");
        System.out.println(server7.printRecord("MTL00001", "SR00003"));

    }


    public static DSassg2.ServerInterface connectToServer(String[] args, String location) {
        DSassg2.ServerInterface serint = null;
        try {
            {
                if (location.equals("MTL"))                                //checks which server's manager logged in
                {
                    ORB orb = ORB.init(args, null);                 // get the root naming context
                    org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
                    NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
                    String name = "Montreal";
                    serint = DSassg2.ServerInterfaceHelper.narrow(ncRef.resolve_str(name));

                } else if (location.equals("LVL")) {
                    ORB orb = ORB.init(args, null);            // get the root naming context
                    org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
                    NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
                    String name = "Laval";
                    serint = DSassg2.ServerInterfaceHelper.narrow(ncRef.resolve_str(name));

                } else if (location.equals("DDO")) {
                    ORB orb = ORB.init(args, null);
                    org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
                    NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
                    String name = "DDO";
                    serint = DSassg2.ServerInterfaceHelper.narrow(ncRef.resolve_str(name));
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return serint;
    }


    void getcount(DSassg2.ServerInterface object, String username, Logger log) throws Exception {
        String arr;
        arr = object.getRecordCount(username);
        System.out.println(arr + username);
        log.info("getRecordCount method performed");
    }


    void recordcreation(DSassg2.ServerInterface object, String username, Logger log) throws Exception {
        String fname = "";
        String success;
        if (username.contains("MTL"))
            fname = "mtl-f_name";
        else if (username.contains("LVL"))
            fname = "lvl_f_name";
        else if (username.contains("DDO"))
            fname = "ddo_f_name";
        success = object.createSRecord(username, fname, "last_name1", "course1/course2", "active");
        if (!success.equals(""))
            log.info("student record created");
        success = object.createTRecord(username, fname, "last_name1", "#12 ,Vancouver", "+1 514 333 2435", "Physics", "Mtl");
        if (!success.equals(""))
            log.info("Teacher record created ");
    }


    void editrecord(DSassg2.ServerInterface object, String username, int x, Logger log) throws Exception {
        String formatted = String.format("%05d", x);
        String record_id = "SR" + formatted;
        success = object.edit(username, record_id, "status", "deactive");

        record_id = "TR" + formatted;
        success = object.edit(username, record_id, "phone", "000000000");
        //success=object.edit(record_id, "courses_Registered", courses ,username);
        if (success)
            log.info("record edited by " + username);
    }


    void transfer(DSassg2.ServerInterface object, String username, Logger log, int x) throws Exception {
        String formatted = String.format("%05d", x);
        String record_id = "TR" + formatted;
        String remoteservername = "";
        if (username.contains("MTL")) {
            record_id = "SR00003";
            remoteservername = "LVL";
        } else if (username.contains("LVL")) {
            record_id = "SR00004";
            remoteservername = "DDO";
        } else if (username.contains("DDO")) {
            remoteservername = "MTL";
            record_id = "SR00005";
        }
        success = object.transferRecord(username, record_id, remoteservername);
        //System.out.println(success+username);
        if (success)
            log.info("record transfered by " + username);

    }

}



