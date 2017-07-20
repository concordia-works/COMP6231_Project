package Servers;

import Utils.Config;
import Utils.Configuration;
import org.omg.CORBA.DCMS;
import org.omg.CORBA.DCMSHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ReplicaManager implements Runnable {
    private Config.ARCHITECTURE.REPLICAS replicaManagerID;
    private int fromFrontEndPort;
    private int fromLeaderPort;
    private int heartBeatPort;
    private boolean isLeader;
    
    public ReplicaManager(Config.ARCHITECTURE.REPLICAS replicaManagerID) {
        this.replicaManagerID = replicaManagerID;
        switch (replicaManagerID) {
            case MINH:
                isLeader = true;
                this.fromFrontEndPort = Config.UDP.PORT_FRONT_END_TO_LEADER;
                this.fromLeaderPort = Config.UDP.PORT_LEADER_TO_BACKUPS;
                this.heartBeatPort = Config.UDP.PORT_HEART_BEAT;
                break;
            case KAMAL:
                isLeader = false;
                this.fromFrontEndPort = 2 * Config.UDP.PORT_FRONT_END_TO_LEADER;
                this.fromLeaderPort = 2 * Config.UDP.PORT_LEADER_TO_BACKUPS;
                this.heartBeatPort = 2 * Config.UDP.PORT_HEART_BEAT;
                break;
            case KEN_RO:
                isLeader = false;
                this.fromFrontEndPort = 3 * Config.UDP.PORT_FRONT_END_TO_LEADER;
                this.fromLeaderPort = 3 * Config.UDP.PORT_LEADER_TO_BACKUPS;
                this.heartBeatPort = 3 * Config.UDP.PORT_HEART_BEAT;
                break;
            default:
                break;
        }
    }

    @Override
    public void run() {
        // Start all the server
        switch (replicaManagerID) {
            case MINH:
                startMinhReplica();
                break;
            case KAMAL:
                startKamalReplica();
                break;
            case KEN_RO:
                startKenroReplica();
                break;
            default:
                break;
        }

        if (isLeader) { // If this is leader, listen to FrontEnd
            new Thread(new Runnable() {
                @Override
                public void run() {
                    listenToFrontEnd();
                }
            }).start();
        }
        else { // If this is backup, listen to Leader
            new Thread(new Runnable() {
                @Override
                public void run() {
                    listenToLeader();
                }
            }).start();
        }
    }

    // Getters & Setters
    public Config.ARCHITECTURE.REPLICAS getReplicaManagerID() {
        return replicaManagerID;
    }

    public int getFromFrontEndPort() {
        return fromFrontEndPort;
    }

    public int getFromLeaderPort() {
        return fromLeaderPort;
    }

    public int getHeartBeatPort() {
        return heartBeatPort;
    }

    public boolean isLeader() {
        return isLeader;
    }

    //Helper functions
    private void startMinhReplica() {
        try {
            // Initiate local ORB object
            ORB orb = ORB.init(Config.CORBA.ORB_PARAMETERS.split(" "), null);

            // Get reference to RootPOA and get POAManager
            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references(Configuration.CORBA.ROOT_POA));
            rootPOA.the_POAManager().activate();

            // Create servant and register it with the ORB
            CenterServer mtlServer = new CenterServer(Configuration.Server_ID.MTL);
            mtlServer.setORB(orb);
            CenterServer lvlServer = new CenterServer(Configuration.Server_ID.LVL);
            lvlServer.setORB(orb);
            CenterServer ddoServer = new CenterServer(Configuration.Server_ID.DDO);
            ddoServer.setORB(orb);

            // Get object reference from the servant
            org.omg.CORBA.Object mtlRef = rootPOA.servant_to_reference(mtlServer);
            org.omg.CORBA.Object lvlRef = rootPOA.servant_to_reference(lvlServer);
            org.omg.CORBA.Object ddoRef = rootPOA.servant_to_reference(ddoServer);
            DCMS mtlDcmsServer = DCMSHelper.narrow(mtlRef);
            DCMS lvlDcmsServer = DCMSHelper.narrow(lvlRef);
            DCMS ddoDcmsServer = DCMSHelper.narrow(ddoRef);

            // Get the root Naming Context
            org.omg.CORBA.Object objRef = orb.resolve_initial_references(Configuration.CORBA.NAME_SERVICE);
            NamingContextExt namingContextRef = NamingContextExtHelper.narrow(objRef);

            // Bind the object reference to the Naming Context
            NameComponent path[] = namingContextRef.to_name(Configuration.Server_ID.MTL.name());
            namingContextRef.rebind(path, mtlDcmsServer);
            path = namingContextRef.to_name(Configuration.Server_ID.LVL.name());
            namingContextRef.rebind(path, mtlDcmsServer);
            path = namingContextRef.to_name(Configuration.Server_ID.DDO.name());
            namingContextRef.rebind(path, mtlDcmsServer);

            // Run the server
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mtlDcmsServer.startUDPServer();
                }
            }).start();
            System.out.println("Server " + Configuration.Server_ID.MTL.name() + " is running ...");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    lvlDcmsServer.startUDPServer();
                }
            }).start();
            System.out.println("Server " + Configuration.Server_ID.LVL.name() + " is running ...");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    ddoDcmsServer.startUDPServer();
                }
            }).start();
            System.out.println("Server " + Configuration.Server_ID.DDO.name() + " is running ...");

            orb.run();
        } catch (Exception e) {
            System.out.println("ERROR: " + e);
            e.printStackTrace(System.out);
        }
    }

    private void startKamalReplica() {}

    private void startKenroReplica() {}

    private void listenToFrontEnd() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(fromFrontEndPort);
            byte[] buffer = new byte[1000];

            while (true) {
                // Get the request
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                // Each request will be handled by a thread to improve performance
                DatagramSocket finalSocket = socket;
                ReplicaManager finalReplica = this;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // Handle the request
                        executeRequest();

                        // Send the result to backups & wait for acknowledgements
                        broadcastResult();

                        // Response to FrontEnd
                    }
                }).start();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (socket != null)
                socket.close();
        }
    }

    private void listenToLeader() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(fromLeaderPort);
            byte[] buffer = new byte[1000];

            while (true) {
                // Get the request
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                // Each request will be handled by a thread to improve performance
                DatagramSocket finalSocket = socket;
                ReplicaManager finalReplica = this;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // Update the result to HashMap
                        updateResult();

                        // Send acknowledgement to leader
                    }
                }).start();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (socket != null)
                socket.close();
        }
    }

    private void executeRequest() {}

    private void updateResult() {}

    private void broadcastResult() {}
}
