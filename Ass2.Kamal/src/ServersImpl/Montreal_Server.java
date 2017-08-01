package ServersImpl;//Provides the implementation of Montreal Server

import DSassg2.*;
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

public class Montreal_Server
{
	public static void main(String args[]) throws IOException
		{
			final Logger logger;                            	//Refrence to Logger class is generated
			Handler fileHandler;
		    SimpleFormatter plainText;
			logger = Logger.getLogger(Logger.class.getName());
			logger.setUseParentHandlers(false);
			fileHandler = new FileHandler("D:/JAVA/DSassg2/Montreallog.txt",true);//File for writting log is opened in append mode
			plainText = new SimpleFormatter();
			fileHandler.setFormatter(plainText);
			logger.addHandler(fileHandler);


			try {

				// create and initialize the ORB
				ORB orb = ORB.init(args, null);
				// get reference to rootpoa & activate the POAManager
				POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
				rootpoa.the_POAManager().activate();
				// create servant and register it with the ORB
				Server_Imp ms =new Server_Imp("MTL");
				ms.setOrb(orb);
				org.omg.CORBA.Object ref = rootpoa.servant_to_reference(ms);
				DSassg2.ServerInterface serverref = ServerInterfaceHelper.narrow(ref);

					org.omg.CORBA.Object objRef =
					orb.resolve_initial_references("NameService");

				NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
				String name = "Montreal";
				NameComponent path[] = ncRef.to_name( name );
				ncRef.rebind(path, serverref);

				System.out.println("Montreal Server statred...");
				logger.info("Montreal Server started ");
				logger.info("UDP Server started at port"+ms.get_udp_port());

				System.out.println("UDP Server started at port"+ms.get_udp_port());
				ms.UDPServer(ms.get_udp_port());
		} catch (Exception e)
		{
			e.printStackTrace();
		}
											//UDP Server is started



		}
	
	}

