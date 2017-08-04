package FrontEnd;

import Utils.Config;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

public class FrontEndDriver {
    public static void main(String args[]) {
        try {
            // Initiate local ORB object
            ORB orb = ORB.init(args, null);

            // Get reference to RootPOA and get POAManager
            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references(Config.CORBA.ROOT_POA));
            rootPOA.the_POAManager().activate();

            // Create servant and register it with the ORB
            FrontEndImpl servant = new FrontEndImpl();
            servant.setORB(orb);

            // Get object reference from the servant
            org.omg.CORBA.Object ref = rootPOA.servant_to_reference(servant);
            FE frontEnd = FEHelper.narrow(ref);

            // Get the root Naming Context
            org.omg.CORBA.Object objRef = orb.resolve_initial_references(Config.CORBA.NAME_SERVICE);
            NamingContextExt namingContextRef = NamingContextExtHelper.narrow(objRef);

            // Bind the object reference to the Naming Context
            NameComponent path[] = namingContextRef.to_name(Config.CORBA.FRONT_END_NAME);
            namingContextRef.rebind(path, frontEnd);

            // Run the server
            System.out.println("Front End is running ...");
            orb.run();
        } catch (Exception e) {
            System.out.println("ERROR: " + e);
            e.printStackTrace(System.out);
        }
    }
}
