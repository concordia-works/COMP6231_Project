package Servers;

import Utils.Config;
import Utils.Configuration;
import Utils.Request;
import Utils.Response;
import org.omg.CORBA.DCMS;
import org.omg.CORBA.DCMSHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class ReplicaManager implements Runnable {
    private Config.ARCHITECTURE.REPLICAS replicaManagerID;
    private int fromFrontEndPort;
    private int toFrontEndPort;
    private int fromLeaderPort;
    private int toLeaderPort;
    private int heartBeatPort;
    private boolean isLeader;
    private ORB orb;
    private org.omg.CORBA.Object namingContextObj;
    private NamingContextExt namingContextRef;
    private FIFO fifo;

    public ReplicaManager(Config.ARCHITECTURE.REPLICAS replicaManagerID) {
        try {
            this.replicaManagerID = replicaManagerID;
            switch (replicaManagerID) {
                case MINH:
                    isLeader = true;
                    this.fromFrontEndPort = Config.UDP.PORT_FRONT_END_TO_LEADER;
                    this.toFrontEndPort = Config.UDP.PORT_LEADER_TO_FRONT_END;
                    this.fromLeaderPort = Config.UDP.PORT_LEADER_TO_BACKUPS;
                    this.toLeaderPort = Config.UDP.PORT_BACKUPS_TO_LEADER;
                    this.heartBeatPort = Config.UDP.PORT_HEART_BEAT;
                    break;
                case KAMAL:
                    isLeader = false;
                    this.fromFrontEndPort = 2 * Config.UDP.PORT_FRONT_END_TO_LEADER;
                    this.toFrontEndPort = 2 * Config.UDP.PORT_LEADER_TO_FRONT_END;
                    this.fromLeaderPort = 2 * Config.UDP.PORT_LEADER_TO_BACKUPS;
                    this.toLeaderPort = 2 * Config.UDP.PORT_BACKUPS_TO_LEADER;
                    this.heartBeatPort = 2 * Config.UDP.PORT_HEART_BEAT;
                    break;
                case KEN_RO:
                    isLeader = false;
                    this.fromFrontEndPort = 3 * Config.UDP.PORT_FRONT_END_TO_LEADER;
                    this.toFrontEndPort = 3 * Config.UDP.PORT_LEADER_TO_FRONT_END;
                    this.fromLeaderPort = 3 * Config.UDP.PORT_LEADER_TO_BACKUPS;
                    this.toLeaderPort = 3 * Config.UDP.PORT_BACKUPS_TO_LEADER;
                    this.heartBeatPort = 3 * Config.UDP.PORT_HEART_BEAT;
                    break;
                default:
                    // Do nothing
                    break;
            }
            fifo = new FIFO();
            prepareORB();
        } catch (Exception e) {
            e.printStackTrace(System.out);
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
                // Do nothing
                break;
        }

        if (isLeader) { // If this is leader, listen to FrontEndImpl
            new Thread(new Runnable() {
                @Override
                public void run() {
                    listenToFrontEnd();
                }
            }).start();
        } else { // If this is backup, listen to Leader
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
            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references(Config.CORBA.ROOT_POA));
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
            org.omg.CORBA.Object objRef = orb.resolve_initial_references(Config.CORBA.NAME_SERVICE);
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

    private void startKamalReplica() {
    }

    private void startKenroReplica() {
        try {
            // Initiate local ORB object
            ORB orb = ORB.init(Config.CORBA.ORB_PARAMETERS.split(" "), null);

            // Get reference to RootPOA and get POAManager
            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references(Config.CORBA.ROOT_POA));
            rootPOA.the_POAManager().activate();

            // create servant and register it with the ORB
            HelloImpl helloImplMTL = new HelloImpl("DDO");
            helloImplMTL.setORB(orb);
            HelloImpl helloImplLVL = new HelloImpl("DDO");
            helloImplLVL.setORB(orb);
            HelloImpl helloImplDDO = new HelloImpl("DDO");
            helloImplDDO.setORB(orb);

            // get object reference from the servant
            org.omg.CORBA.Object refMTL = rootpoa.servant_to_reference(helloImplMTL);
            Hello hrefMTL = HelloHelper.narrow(refMTL);
            org.omg.CORBA.Object refLVL = rootpoa.servant_to_reference(helloImplLVL);
            Hello hrefLVL = HelloHelper.narrow(refLVL);
            org.omg.CORBA.Object refDDO = rootpoa.servant_to_reference(helloImplDDO);
            Hello hrefDDO = HelloHelper.narrow(refDDO);


            // get the root naming context
            // NameService invokes the name service
            org.omg.CORBA.Object objRef = orb.resolve_initial_references(Config.CORBA.NAME_SERVICE);
            // Use NamingContextExt which is part of the Interoperable
            // Naming Service (INS) specification.
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // bind the Object Reference in Naming
            String nameMTL = "MTL";
            String nameLVL = "LVL";
            String nameDDO = "DDO";
            NameComponent pathMTL[] = ncRef.to_name(nameMTL);
            NameComponent pathLVL[] = ncRef.to_name(nameLVL);
            NameComponent pathDDO[] = ncRef.to_name(nameDDO);
            ncRef.rebind(pathMTL, href);
            ncRef.rebind(pathLVL, href);
            ncRef.rebind(pathDDO, href);

            System.out.println("MTL Server is ready and waiting ...");
            System.out.println("LVL Server is ready and waiting ...");
            System.out.println("DDO Server is ready and waiting ...");


            // start UDP server
            new Thread(new Runnable() {
                @Override
                public void run() {
                    helloImpl.serverUDP(6789);
                }
            }).start();
            System.out.println("ServerUDP MTL is running ...");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    helloImpl.serverUDP(6788);
                }
            }).start();
            System.out.println("ServerUDP LVL is running ...");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    helloImpl.serverUDP(6787);
                }
            }).start();
            System.out.println("ServerUDP DDO is running ...");

            // wait for invocations from clients
            orb.run();
        } catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
        }

    }

    private void listenToFrontEnd() {
        DatagramSocket fromFrontEndSocket = null;
        try {
            fromFrontEndSocket = new DatagramSocket(fromFrontEndPort);

            while (true) {
                byte[] buffer = new byte[1000];

                // Get the request
                DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
                fromFrontEndSocket.receive(requestPacket);

                // Each request will be handled by a thread to improve performance
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // Rebuild the request object
                        Request request = Config.deserializeRequest(requestPacket.getData());
                        String managerID = request.getManagerID();
                        Response response;
                        if (request.getSequenceNumber() == fifo.getExpectedRequestNumber(managerID)) { // This request is the one expected
                            // Add the request to the queue, so it can be executed in the next step
                            fifo.holdRequest(managerID, request);

                            // Execute this request and the continuous chain of requests after it hold in the queue
                            while (true) {
                                Request currentRequest = fifo.popNextRequest(managerID);

                                // Increase the expected sequence number by 1
                                fifo.generateRequestNumber(managerID);

                                // Handle the request
                                response = executeRequest(currentRequest);

                                /**
                                 * Only broadcast requests if the leader executes the request successfully
                                 * If the leader succeeds, the response to client will be successful
                                 * As long as a RM can proceed the request, clients still get the successful result
                                 * Leader waits for acknowledgements from both backups then answers FrontEndImpl
                                 */
                                // Send the result to backups & wait for acknowledgements
                                if (response.isSuccess())
                                    broadcastAndGetAcknowledgement(requestPacket.getData());

                                // Response to FrontEndImpl
                                responseToFrontEnd(response);

                                // If the next request on hold doesn't have the expected sequence number, the loop will end
                                if (fifo.peekFirstRequestHoldNumber(managerID) != fifo.getExpectedRequestNumber(managerID))
                                    break;
                            }
                        } else if (request.getSequenceNumber() > fifo.getExpectedRequestNumber(managerID)) { // There're other requests must come before this request
                            // Save the request to the holdback queue
                            fifo.holdRequest(managerID, request);

                            /**
                             * How to take care of the situation
                             * When the request is put to the queue
                             * But will never be execute until a new request is sent???
                             */
                        } // Else the request is duplicated, ignore it
                    }
                }).start();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (fromFrontEndSocket != null)
                fromFrontEndSocket.close();
        }
    }

    private void listenToLeader() {
        DatagramSocket leaderSocket = null;
        try {
            leaderSocket = new DatagramSocket(fromLeaderPort);

            while (true) {
                byte[] buffer = new byte[1000];

                // Get the request
                DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
                leaderSocket.receive(requestPacket);

                // Each request will be handled by a thread to improve performance
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        /**
                         * To improve performance, backups will send acknowledgement to the leader
                         * Right when it receive the message, don't need to wait for the processing
                         */
                        // Send acknowledgement to the leader

                        // Rebuild the request object
                        Request request = Config.deserializeRequest(requestPacket.getData());
                        String managerID = request.getManagerID();
                        if (request.getSequenceNumber() == fifo.getExpectedRequestNumber(managerID)) { // This request is the one expected
                            // Add the request to the queue, so it can be executed in the next step
                            fifo.holdRequest(managerID, request);

                            // Execute this request and the continuous chain of requests after it hold in the queue
                            while (true) {
                                Request currentRequest = fifo.popNextRequest(managerID);

                                // Increase the expected sequence number by 1
                                fifo.generateRequestNumber(managerID);

                                // Handle the request
                                executeRequest(currentRequest);

                                // If the next request on hold doesn't have the expected sequence number, the loop will end
                                if (fifo.peekFirstRequestHoldNumber(managerID) != fifo.getExpectedRequestNumber(managerID))
                                    break;
                            }
                        } else if (request.getSequenceNumber() > fifo.getExpectedRequestNumber(managerID)) { // There're other requests must come before this request
                            // Save the request to the holdback queue
                            fifo.holdRequest(managerID, request);

                            /**
                             * How to take care of the situation
                             * When the request is put to the queue
                             * But will never be execute until a new request is sent???
                             */
                        } // Else the request is duplicated, ignore it
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        } finally {
            if (leaderSocket != null)
                leaderSocket.close();
        }
    }

    private Response executeRequest(Request request) {
        Response response = null;
        try {
            String managerID = request.getManagerID();
            Config.ARCHITECTURE.SERVER_ID serverID = Config.ARCHITECTURE.SERVER_ID.valueOf(managerID.substring(0, 3).toUpperCase());

            // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
            DCMS dcmsServer = DCMSHelper.narrow(namingContextRef.resolve_str(serverID.name()));
            String result = "";
            boolean isSuccess = false;
            switch (request.getMethodName()) {
                case createTRecord: {
                    String createdRecordID = dcmsServer.createTRecord(managerID, request.getFirstName(), request.getLastName(), request.getAddress(), request.getPhone(), request.getSpecialization(), request.getLocation());
                    if (createdRecordID.compareTo("") != 0) {
                        result = String.format(Config.RESPONSE.CREATE_T_RECORD, createdRecordID);
                        isSuccess = true;
                    }
                    break;
                }

                case createSRecord: {
                    String createdRecordID = dcmsServer.createSRecord(managerID, request.getFirstName(), request.getLastName(), request.getCoursesRegistered(), request.getStatus());
                    if (createdRecordID.compareTo("") != 0) {
                        result = String.format(Config.RESPONSE.CREATE_S_RECORD, createdRecordID);
                        isSuccess = true;
                    }
                    break;
                }

                case getRecordsCount: {
                    result = dcmsServer.getRecordCounts(managerID);
                    if (result.compareTo("") != 0)
                        isSuccess = true;
                    break;
                }

                case editRecord: {
                    isSuccess = dcmsServer.editRecord(managerID, request.getRecordID(), request.getFieldName(), request.getNewValue());
                    if (isSuccess)
                        result = String.format(Config.RESPONSE.EDIT_RECORD, request.getRecordID());
                    break;
                }

                case transferRecord: {
                    isSuccess = dcmsServer.transferRecord(managerID, request.getRecordID(), request.getRemoteCenterServerName());
                    if (isSuccess)
                        result = String.format(Config.RESPONSE.TRANSFER_RECORD, request.getRecordID());
                    break;
                }

                case printRecord: {
                    result = dcmsServer.printRecord(managerID, request.getRecordID());
                    if (result.compareTo("") != 0)
                        isSuccess = true;
                    break;
                }

                case printAllRecords: {
                    result = dcmsServer.printAllRecords(managerID);
                    if (result.compareTo("") != 0)
                        isSuccess = true;
                    break;
                }

                default: {
                    // Do nothing
                    break;
                }
            }
            response = new Response(request, isSuccess, result);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return response;
    }

    private void broadcastAndGetAcknowledgement(byte[] data) {
        // Broadcast using FIFO multicast

        // Listen to backups' acknowledgement
    }

    private void prepareORB() throws Exception {
        // Initiate client ORB
        orb = ORB.init(Config.CORBA.ORB_PARAMETERS.split(" "), null);

        // Get object reference to the Naming Service
        namingContextObj = orb.resolve_initial_references(Config.CORBA.NAME_SERVICE);

        // Narrow the NamingContext object reference to the proper type to be usable (like any CORBA object)
        namingContextRef = NamingContextExtHelper.narrow(namingContextObj);
    }

    private Request deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return (Request) is.readObject();
    }

    private void responseToFrontEnd(Response response) {
        DatagramSocket toFrontEndSocket = null;
        try {
            toFrontEndSocket = new DatagramSocket(toFrontEndPort);
            String managerID = response.getManagerID();

            if (response.getSequenceNumber() == fifo.getExpectedResponseNumber(managerID)) { // This response is the one expected
                // Add the response to the queue, so it can be forwarded in the next step
                fifo.holdResponse(managerID, response);

                // Forward this response and the continuous chain of other responses after it hold in the queue
                while (true) {
                    Response currentResponse = fifo.popNextResponse(managerID);

                    // Increase the expected sequence number by 1
                    fifo.generateResponseNumber(managerID);

                    // Forward the response using FIFO's reliable unicast

                    // If the next response on hold doesn't have the expected sequence number, the loop will end
                    if (fifo.peekFirstResponseHoldNumber(managerID) != fifo.getExpectedResponseNumber(managerID))
                        break;
                }
            } else if (response.getSequenceNumber() > fifo.getExpectedResponseNumber(managerID)) { // There's other responses must come before this response
                // Save the response to the holdback queue
                fifo.holdResponse(managerID, response);

                /**
                 * How to take care of the situation
                 * When the response is put to the queue
                 * But will never be forwarded until a new response is sent???
                 */
            } // Else the response is duplicated, ignore it
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            if (toFrontEndSocket != null)
                toFrontEndSocket.close();
        }
    }
}
