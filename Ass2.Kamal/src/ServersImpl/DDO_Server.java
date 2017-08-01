package ServersImpl;//provides the implementation DDO server

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

public class DDO_Server
{
	public static void main(String args[]) throws IOException
	{
		 	Logger logger;
	     	Handler fileHandler;
	     	SimpleFormatter plainText;
			logger = Logger.getLogger(Logger.class.getName());
	        logger.setUseParentHandlers(false);
	        fileHandler = new FileHandler("D:/JAVA/DSassg2/Ddolog.txt",true); //log file created in append mode

	        plainText = new SimpleFormatter();
	        fileHandler.setFormatter(plainText);
	        logger.addHandler(fileHandler);
		  			//Server is started with server name
	try {
		ORB orb = ORB.init(args, null);
		// get reference to rootpoa & activate the POAManager
		POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
		rootpoa.the_POAManager().activate();
		// create servant and register it with the ORB
		Server_Imp ds =new Server_Imp("DDO");
		ds.setOrb(orb);
		org.omg.CORBA.Object ref = rootpoa.servant_to_reference(ds);
		DSassg2.ServerInterface serverref = ServerInterfaceHelper.narrow(ref);

		org.omg.CORBA.Object objRef =orb.resolve_initial_references("NameService");

		NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
		String name = "DDO";
		NameComponent path[] = ncRef.to_name( name );
		ncRef.rebind(path, serverref);					//object ds is bind to Ddo reference
		System.out.println(" DDO Server statred...");
		logger.info("DDO Server started ");
		logger.info("UDP Server started at port"+ds.get_udp_port());
		System.out.println("UDP Server started at port"+ds.get_udp_port());
		ds.UDPServer(ds.get_udp_port());

	} catch (Exception e)
	{
		e.printStackTrace();
	}
								//starts the udp server



	}
}
