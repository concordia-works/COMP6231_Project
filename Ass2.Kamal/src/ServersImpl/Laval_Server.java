package ServersImpl;//Provides the implementation of Laval Server

import DSassg2.ServerInterfaceHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Laval_Server
{
	public static void main(String args[]) throws IOException
	{	 Logger logger;
	     Handler fileHandler;
	     SimpleFormatter plainText;
	     logger = Logger.getLogger(Logger.class.getName());		//Instance of Logger class is created
	        logger.setUseParentHandlers(false);
	        fileHandler = new FileHandler("D:/JAVA/DSassg2/Lavallog.txt",true);//Log file opened in append mode
			plainText = new SimpleFormatter();
	        fileHandler.setFormatter(plainText);
	        logger.addHandler(fileHandler);
	

	try {
		ORB orb = ORB.init(args, null);
		// get reference to rootpoa & activate the POAManager
		POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
		rootpoa.the_POAManager().activate();
		// create servant and register it with the ORB
		Server_Imp ls =new Server_Imp("LVL");
		ls.setOrb(orb);
		org.omg.CORBA.Object ref = rootpoa.servant_to_reference(ls);
		DSassg2.ServerInterface serverref = ServerInterfaceHelper.narrow(ref);

		org.omg.CORBA.Object objRef =
				orb.resolve_initial_references("NameService");

		NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
		String name = "Laval";
		NameComponent path[] = ncRef.to_name( name );
		ncRef.rebind(path, serverref);							//object reference binding
		System.out.println("Laval Server statred...");
		logger.info("Laval Server started ");
		logger.info("UDP Server started "+System.currentTimeMillis()+"at port"+ls.get_udp_port());
		System.out.println("UDP Server started at port"+ls.get_udp_port());

		ls.UDPServer(ls.get_udp_port());//udp server is started
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}



	}
}
