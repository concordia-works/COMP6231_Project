package Servers;

import Utils.Config;
import org.omg.CORBA.DCMS;
import org.omg.CORBA.DCMSHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

/**
 * Created by quocminhvu on 2017-05-26.
 */

public class MTLServer {
    public static void main(String args[]) {
        try {
            // Initiate local ORB object
            ORB orb = ORB.init(args, null);

            // Get reference to RootPOA and get POAManager
            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references(Config.CORBA.ROOT_POA));
            rootPOA.the_POAManager().activate();

            // Create servant and register it with the ORB
            CenterServer servant = new CenterServer(Config.Server_ID.MTL);
            servant.setORB(orb);

            // Get object reference from the servant
            org.omg.CORBA.Object ref = rootPOA.servant_to_reference(servant);
            DCMS dcmsServer = DCMSHelper.narrow(ref);

            // Get the root Naming Context
            org.omg.CORBA.Object objRef = orb.resolve_initial_references(Config.CORBA.NAME_SERVICE);
            NamingContextExt namingContextRef = NamingContextExtHelper.narrow(objRef);

            // Bind the object reference to the Naming Context
            NameComponent path[] = namingContextRef.to_name(Config.Server_ID.MTL.name());
            namingContextRef.rebind(path, dcmsServer);

            // Run the server
            dcmsServer.startUDPServer();
            System.out.println("Server " + Config.Server_ID.MTL.name() + " is running ...");
            orb.run();
        } catch (Exception e) {
            System.out.println("ERROR: " + e);
            e.printStackTrace(System.out);
        }
    }
}
