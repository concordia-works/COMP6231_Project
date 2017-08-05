package Servers;

import Ass2CORBA.DCMS;
import Ass2CORBA.DCMSHelper;
import HelloServers.KR_CenterServer;
import ServersImpl.KM_CenterServer;
import Utils.*;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.sleep;

public class ReplicaManager implements Runnable {
    private Config.ARCHITECTURE.REPLICAS replicaManagerID;
    private int fromFrontEndPort;
    private int fromLeaderPort;
    private int fromBackupPort;
    private int heartBeatPort;
    private int noOfAliveRM;
    private ORB orb;
    private org.omg.CORBA.Object namingContextObj;
    private NamingContextExt namingContextRef;
    private FIFO fifo;
    private final Object fifoLock = new Object();
    private Config.ARCHITECTURE.REPLICAS leaderID;
    private Map<Integer, Integer> acknowledgementMap;
    private final Object acknowledgeLock = new Object();
    private HeartBeat heartBeat;
    private Map<String, Map<Integer, Integer>> frontEndPortsMap; // to know the port that FrontEnd is using to wait for a response
    private final Object frontEndPortsLock = new Object();
    private final Object fileWriterLock = new Object();
    private String fileName;

    public ReplicaManager(Config.ARCHITECTURE.REPLICAS replicaManagerID) {
        try {
            prepareORB();
            this.replicaManagerID = replicaManagerID;
            this.acknowledgementMap = Collections.synchronizedMap(new HashMap<Integer, Integer>());
            this.frontEndPortsMap = Collections.synchronizedMap(new HashMap<>());
            this.fifo = new FIFO();

            switch (replicaManagerID) {
                case KEN_RO:
                    this.fromFrontEndPort = Config.ARCHITECTURE.REPLICAS.KEN_RO.getCoefficient() * Config.UDP.PORT_FRONT_END_TO_LEADER;
                    this.fromLeaderPort = Config.ARCHITECTURE.REPLICAS.KEN_RO.getCoefficient() * Config.UDP.PORT_LEADER_TO_BACKUPS;
                    this.fromBackupPort = Config.ARCHITECTURE.REPLICAS.KEN_RO.getCoefficient() * Config.UDP.PORT_BACKUPS_TO_LEADER;
                    this.heartBeatPort = Config.ARCHITECTURE.REPLICAS.KEN_RO.getCoefficient() * Config.UDP.PORT_HEART_BEAT;
                    this.heartBeat = new HeartBeat(Config.ARCHITECTURE.REPLICAS.KEN_RO, this.heartBeatPort);
                    break;
                case KAMAL:
                    this.fromFrontEndPort = Config.ARCHITECTURE.REPLICAS.KAMAL.getCoefficient() * Config.UDP.PORT_FRONT_END_TO_LEADER;
                    this.fromLeaderPort = Config.ARCHITECTURE.REPLICAS.KAMAL.getCoefficient() * Config.UDP.PORT_LEADER_TO_BACKUPS;
                    this.fromBackupPort = Config.ARCHITECTURE.REPLICAS.KAMAL.getCoefficient() * Config.UDP.PORT_BACKUPS_TO_LEADER;
                    this.heartBeatPort = Config.ARCHITECTURE.REPLICAS.KAMAL.getCoefficient() * Config.UDP.PORT_HEART_BEAT;
                    this.heartBeat = new HeartBeat(Config.ARCHITECTURE.REPLICAS.KAMAL, this.heartBeatPort);
                    break;
                case MINH:
                    this.fromFrontEndPort = Config.ARCHITECTURE.REPLICAS.MINH.getCoefficient() * Config.UDP.PORT_FRONT_END_TO_LEADER;
                    this.fromLeaderPort = Config.ARCHITECTURE.REPLICAS.MINH.getCoefficient() * Config.UDP.PORT_LEADER_TO_BACKUPS;
                    this.fromBackupPort = Config.ARCHITECTURE.REPLICAS.MINH.getCoefficient() * Config.UDP.PORT_BACKUPS_TO_LEADER;
                    this.heartBeatPort = Config.ARCHITECTURE.REPLICAS.MINH.getCoefficient() * Config.UDP.PORT_HEART_BEAT;
                    this.heartBeat = new HeartBeat(Config.ARCHITECTURE.REPLICAS.MINH, this.heartBeatPort);
                    break;
                default:
                    // Do nothing
                    break;
            }

            fileName = replicaManagerID.name() + ".txt";

            new Thread(() -> listenNewLeader()).start();

            new Thread(this.heartBeat).start();
        } catch (Exception e) {
            e.printStackTrace(System.err);
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
            QM_CenterServer mtlServer = new QM_CenterServer(Configuration.Server_ID.QM_MTL);
            mtlServer.setORB(orb);
            QM_CenterServer lvlServer = new QM_CenterServer(Configuration.Server_ID.QM_LVL);
            lvlServer.setORB(orb);
            QM_CenterServer ddoServer = new QM_CenterServer(Configuration.Server_ID.QM_DDO);
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
            NameComponent path[] = namingContextRef.to_name(Configuration.Server_ID.QM_MTL.name());
            namingContextRef.rebind(path, mtlDcmsServer);
            path = namingContextRef.to_name(Configuration.Server_ID.QM_LVL.name());
            namingContextRef.rebind(path, mtlDcmsServer);
            path = namingContextRef.to_name(Configuration.Server_ID.QM_DDO.name());
            namingContextRef.rebind(path, mtlDcmsServer);

            // Run the server
            new Thread(() -> {
                mtlDcmsServer.startUDPServer();
//                    System.out.println(String.format(Config.LOGGING.UDP_START, Config.ARCHITECTURE.REPLICAS.MINH.name(), Config.ARCHITECTURE.SERVER_ID.QM_MTL.name(), Configuration.getUDPPortByServerID(Configuration.Server_ID.QM_MTL)));
            }).start();
//            System.out.println(String.format(Config.LOGGING.SERVER_START, Config.ARCHITECTURE.REPLICAS.MINH.name(), Config.ARCHITECTURE.SERVER_ID.QM_MTL.name()));

            new Thread(() -> {
                lvlDcmsServer.startUDPServer();
//                    System.out.println(String.format(Config.LOGGING.UDP_START, Config.ARCHITECTURE.REPLICAS.MINH.name(), Config.ARCHITECTURE.SERVER_ID.QM_LVL.name(), Configuration.getUDPPortByServerID(Configuration.Server_ID.QM_LVL)));
            }).start();
//            System.out.println(String.format(Config.LOGGING.SERVER_START, Config.ARCHITECTURE.REPLICAS.MINH.name(), Config.ARCHITECTURE.SERVER_ID.QM_LVL.name()));

            new Thread(() -> {
                ddoDcmsServer.startUDPServer();
//                    System.out.println(String.format(Config.LOGGING.UDP_START, Config.ARCHITECTURE.REPLICAS.MINH.name(), Config.ARCHITECTURE.SERVER_ID.QM_DDO.name(), Configuration.getUDPPortByServerID(Configuration.Server_ID.QM_DDO)));
            }).start();
//            System.out.println(String.format(Config.LOGGING.SERVER_START, Config.ARCHITECTURE.REPLICAS.MINH.name(), Config.ARCHITECTURE.SERVER_ID.QM_DDO.name()));

            startListening();
            restoreHashMap();
            orb.run();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private void startKamalReplica() {
        try {
            ORB orb = ORB.init(Config.CORBA.ORB_PARAMETERS.split(" "), null);
            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references(Config.CORBA.ROOT_POA));
            rootPOA.the_POAManager().activate();

            KM_CenterServer mtlServer = new KM_CenterServer(Config.ARCHITECTURE.KAMAL_SERVER_ID.KM_MTL.name());
            mtlServer.setOrb(orb);
            KM_CenterServer lvlServer = new KM_CenterServer(Config.ARCHITECTURE.KAMAL_SERVER_ID.KM_LVL.name());
            lvlServer.setOrb(orb);
            KM_CenterServer ddoServer = new KM_CenterServer(Config.ARCHITECTURE.KAMAL_SERVER_ID.KM_DDO.name());
            ddoServer.setOrb(orb);

            org.omg.CORBA.Object mtlObj = rootPOA.servant_to_reference(mtlServer);
            org.omg.CORBA.Object lvlObj = rootPOA.servant_to_reference(lvlServer);
            org.omg.CORBA.Object ddoObj = rootPOA.servant_to_reference(ddoServer);

            DCMS mtlDcmsServer = DCMSHelper.narrow(mtlObj);
            DCMS lvlDcmsServer = DCMSHelper.narrow(lvlObj);
            DCMS ddoDcmsServer = DCMSHelper.narrow(ddoObj);

            org.omg.CORBA.Object objRef = orb.resolve_initial_references(Config.CORBA.NAME_SERVICE);
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            NameComponent path[] = ncRef.to_name(Config.ARCHITECTURE.KAMAL_SERVER_ID.KM_MTL.name());
            ncRef.rebind(path, mtlDcmsServer);
            path = ncRef.to_name(Config.ARCHITECTURE.KAMAL_SERVER_ID.KM_LVL.name());
            ncRef.rebind(path, lvlDcmsServer);
            path = ncRef.to_name(Config.ARCHITECTURE.KAMAL_SERVER_ID.KM_DDO.name());
            ncRef.rebind(path, ddoDcmsServer);

            new Thread(() -> {
                mtlDcmsServer.startUDPServer();
//                        System.out.println(String.format(Config.LOGGING.UDP_START, Config.ARCHITECTURE.REPLICAS.KAMAL.name(), Config.ARCHITECTURE.SERVER_ID.QM_MTL.name(), mtlServer.get_udp_port()));
            }).start();
//            System.out.println(String.format(Config.LOGGING.SERVER_START, Config.ARCHITECTURE.REPLICAS.KAMAL.name(), Config.ARCHITECTURE.SERVER_ID.QM_MTL.name()));

            new Thread(() -> {
                lvlDcmsServer.startUDPServer();
//                        System.out.println(String.format(Config.LOGGING.UDP_START, Config.ARCHITECTURE.REPLICAS.KAMAL.name(), Config.ARCHITECTURE.SERVER_ID.QM_LVL.name(), lvlServer.get_udp_port()));
            }).start();
//            System.out.println(String.format(Config.LOGGING.SERVER_START, Config.ARCHITECTURE.REPLICAS.KAMAL.name(), Config.ARCHITECTURE.SERVER_ID.QM_LVL.name()));

            new Thread(() -> {
                ddoDcmsServer.startUDPServer();
//                        System.out.println(String.format(Config.LOGGING.UDP_START, Config.ARCHITECTURE.REPLICAS.KAMAL.name(), Config.ARCHITECTURE.SERVER_ID.QM_DDO.name(), ddoServer.get_udp_port()));
            }).start();
//            System.out.println(String.format(Config.LOGGING.SERVER_START, Config.ARCHITECTURE.REPLICAS.KAMAL.name(), Config.ARCHITECTURE.SERVER_ID.QM_DDO.name()));

            startListening();
            restoreHashMap();
            orb.run();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private void startKenroReplica() {
        try {
            // Initiate local ORB object
            ORB orb = ORB.init(Config.CORBA.ORB_PARAMETERS.split(" "), null);

            // Get reference to RootPOA and get POAManager
            POA rootPOA = POAHelper.narrow(orb.resolve_initial_references(Config.CORBA.ROOT_POA));
            rootPOA.the_POAManager().activate();

            // create servant and register it with the ORB
            KR_CenterServer helloImplMTL = new KR_CenterServer(Config.ARCHITECTURE.KENRO_SERVER_ID.KR_MTL.name());
            helloImplMTL.setORB(orb);
            KR_CenterServer helloImplLVL = new KR_CenterServer(Config.ARCHITECTURE.KENRO_SERVER_ID.KR_LVL.name());
            helloImplLVL.setORB(orb);
            KR_CenterServer helloImplDDO = new KR_CenterServer(Config.ARCHITECTURE.KENRO_SERVER_ID.KR_DDO.name());
            helloImplDDO.setORB(orb);

            // get object reference from the servant
            org.omg.CORBA.Object refMTL = rootPOA.servant_to_reference(helloImplMTL);
            DCMS mtlDcmsServer = DCMSHelper.narrow(refMTL);
            org.omg.CORBA.Object refLVL = rootPOA.servant_to_reference(helloImplLVL);
            DCMS lvlDcmsServer = DCMSHelper.narrow(refLVL);
            org.omg.CORBA.Object refDDO = rootPOA.servant_to_reference(helloImplDDO);
            DCMS ddoDcmsServer = DCMSHelper.narrow(refDDO);

            org.omg.CORBA.Object objRef = orb.resolve_initial_references(Config.CORBA.NAME_SERVICE);
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // bind the Object Reference in Naming
            NameComponent pathMTL[] = ncRef.to_name(Config.ARCHITECTURE.KENRO_SERVER_ID.KR_MTL.name());
            NameComponent pathLVL[] = ncRef.to_name(Config.ARCHITECTURE.KENRO_SERVER_ID.KR_LVL.name());
            NameComponent pathDDO[] = ncRef.to_name(Config.ARCHITECTURE.KENRO_SERVER_ID.KR_DDO.name());
            ncRef.rebind(pathMTL, mtlDcmsServer);
            ncRef.rebind(pathLVL, lvlDcmsServer);
            ncRef.rebind(pathDDO, ddoDcmsServer);

            // start UDP server
            new Thread(() -> {
                try {
                    mtlDcmsServer.startUDPServer();
//                        System.out.println(String.format(Config.LOGGING.UDP_START, Config.ARCHITECTURE.REPLICAS.KEN_RO.name(), Config.ARCHITECTURE.SERVER_ID.QM_MTL.name(), Config.UDP.KENRO_UDP_MTL));
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }).start();
//            System.out.println(String.format(Config.LOGGING.SERVER_START, Config.ARCHITECTURE.REPLICAS.KEN_RO.name(), Config.ARCHITECTURE.SERVER_ID.QM_MTL.name()));

            new Thread(() -> {
                try {
                    lvlDcmsServer.startUDPServer();
//                        System.out.println(String.format(Config.LOGGING.UDP_START, Config.ARCHITECTURE.REPLICAS.KEN_RO.name(), Config.ARCHITECTURE.SERVER_ID.QM_LVL.name(), Config.UDP.KENRO_UDP_LVL));
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }).start();
//            System.out.println(String.format(Config.LOGGING.SERVER_START, Config.ARCHITECTURE.REPLICAS.KEN_RO.name(), Config.ARCHITECTURE.SERVER_ID.QM_LVL.name()));

            new Thread(() -> {
                try {
                    ddoDcmsServer.startUDPServer();
//                        System.out.println(String.format(Config.LOGGING.UDP_START, Config.ARCHITECTURE.REPLICAS.KEN_RO.name(), Config.ARCHITECTURE.SERVER_ID.QM_DDO.name(), Config.UDP.KENRO_UDP_DDO));
                } catch (Exception e) {
                    e.printStackTrace(System.err);
                }
            }).start();
//            System.out.println(String.format(Config.LOGGING.SERVER_START, Config.ARCHITECTURE.REPLICAS.KEN_RO.name(), Config.ARCHITECTURE.SERVER_ID.QM_DDO.name()));

            startListening();
            restoreHashMap();
            orb.run();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

    }

    private void listenToFrontEnd() {
        DatagramSocket fromFrontEndSocket = null;
        try {
//            System.out.println(replicaManagerID.name() + " listen to FrontEnd at port " + fromFrontEndPort);
            fromFrontEndSocket = new DatagramSocket(fromFrontEndPort);

            while (true) {
                byte[] buffer = new byte[1000];

                // Get the request
                DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
                fromFrontEndSocket.receive(requestPacket);

                // Each request will be handled by a thread to improve performance
                new Thread(() -> {
                    // Rebuild the request object
                    Request request = Config.deserializeRequest(requestPacket.getData());

                    System.out.println(replicaManagerID.name() + ": receives " + request.getSequenceNumber() + " " + request.getFullInvocation() + " from FrontEnd");

                    synchronized (frontEndPortsLock) {
                        Map<Integer, Integer> portsMap = frontEndPortsMap.getOrDefault(request.getManagerID(), new HashMap<>());
                        portsMap.put(request.getSequenceNumber(), requestPacket.getPort());
                        frontEndPortsMap.put(request.getManagerID(), portsMap);
                    }

                    String managerID = request.getManagerID();
                    Response response;
                    synchronized (fifoLock) {
                        // This request is the one expected
                        if (request.getSequenceNumber() == fifo.getExpectedRequestNumber(managerID)) {
                            // Add the request to the queue, so it can be executed in the next step
                            fifo.holdRequest(managerID, request);

                            // Execute this request and the continuous chain of requests after it hold in the queue
                            while (true) {
                                Request currentRequest = fifo.popNextRequest(managerID);

                                // Increase the expected sequence number by 1
                                fifo.generateRequestNumber(managerID);

                                // Handle the request
                                response = executeRequest(currentRequest);
                                System.out.println(replicaManagerID.name() + ": executes " + request.getSequenceNumber() + " " + request.getFullInvocation());

                                // Write the request to the file
                                writeRequestToFile(currentRequest);
//                            /**
//                             * Only broadcast requests if the leader executes the request successfully
//                             * If the leader succeeds, the response to client will be successful
//                             * As long as a RM can proceed the request, clients still get the successful result
//                             * Leader waits for acknowledgements from both backups then answers FrontEndImpl
//                             */
//                            // Send the result to backups & wait for acknowledgements
//                            if (response.isSuccess()) {
                                // Broadcast using FIFO multicast
                                broadcastToGroup(currentRequest);
                                System.out.println(replicaManagerID.name() + " broadcasts " + request.getSequenceNumber() + " " + request.getFullInvocation());

                                // Check backups' acknowledgement
                                waitForEnoughAcknowledgement(currentRequest);
//                                System.out.println(replicaManagerID.name() + " receives all acknowledgements of " + request.getSequenceNumber() + " " + request.getFullInvocation());
//                            }

                                // Response to FrontEndImpl
                                responseToFrontEnd(response);

                                // If the next request on hold doesn't have the expected sequence number, the loop will end
                                if (fifo.peekFirstRequestHoldNumber(managerID) != fifo.getExpectedRequestNumber(managerID))
                                    break;
                            }
                        }
                        // There're other requests must come before this request
                        else if (request.getSequenceNumber() > fifo.getExpectedRequestNumber(managerID)) {
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
            e.printStackTrace(System.err);
        } finally {
            if (fromFrontEndSocket != null)
                fromFrontEndSocket.close();
        }
    }

    private void responseToFrontEnd(Response response) {
        DatagramSocket toFrontEndSocket = null;
        try {
            toFrontEndSocket = new DatagramSocket();
            String managerID = response.getManagerID();

            // This response is the one expected
            if (response.getSequenceNumber() == fifo.getExpectedResponseNumber(managerID)) {
                // Add the response to the queue, so it can be forwarded in the next step
                fifo.holdResponse(managerID, response);

                // Forward this response and the continuous chain of other responses after it hold in the queue
                while (true) {
                    Response currentResponse = fifo.popNextResponse(managerID);

                    // Increase the expected sequence number by 1
                    fifo.generateResponseNumber(managerID);

                    // Forward the response using FIFO's reliable unicast
                    int portFrontEnd;
                    synchronized (frontEndPortsLock) {
                        Map<Integer, Integer> portsMap = frontEndPortsMap.get(response.getManagerID());
                        portFrontEnd = portsMap.get(currentResponse.getSequenceNumber());
                        portsMap.remove(currentResponse.getSequenceNumber());
                    }
                    byte[] buffer = currentResponse.serialize();
                    DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length, InetAddress.getLocalHost(), portFrontEnd);
                    toFrontEndSocket.send(responsePacket);
                    System.out.println(replicaManagerID.name() + " responses " + currentResponse.getSequenceNumber() + " " + response.getContent() + " back to FrontEnd");

                    // If the next response on hold doesn't have the expected sequence number, the loop will end
                    if (fifo.peekFirstResponseHoldNumber(managerID) != fifo.getExpectedResponseNumber(managerID))
                        break;
                }
            }
            // There's other responses must come before this response
            else if (response.getSequenceNumber() > fifo.getExpectedResponseNumber(managerID)) {
                // Save the response to the holdback queue
                fifo.holdResponse(managerID, response);

                /**
                 * How to take care of the situation
                 * When the response is put to the queue
                 * But will never be forwarded until a new response is sent???
                 */
            } // Else the response is duplicated, ignore it
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            if (toFrontEndSocket != null)
                toFrontEndSocket.close();
        }
    }

    private Response executeRequest(Request request) {
        Response response = null;
        try {
            String managerID = request.getManagerID();
            Config.ARCHITECTURE.SERVER_ID serverID = Config.ARCHITECTURE.SERVER_ID.valueOf(managerID.substring(0, 3).toUpperCase());

            // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
            String serverName = "";
            switch (replicaManagerID) {
                case MINH:
                    serverName = "QM_" + serverID.name();
                    break;
                case KAMAL:
                    serverName = "KM_" + serverID.name();
                    break;
                case KEN_RO:
                    serverName = "KR_" + serverID.name();
                    break;
                default:
                    break;
            }

            DCMS dcmsServer = DCMSHelper.narrow(namingContextRef.resolve_str(serverName));
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
            e.printStackTrace(System.err);
        }
        return response;
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
                new Thread(() -> {
                    /**
                     * To improve performance, backups will send acknowledgement to the leader
                     * Right when it receive the message, don't need to wait for the processing
                     */
                    // Rebuild the request object
                    Request request = Config.deserializeRequest(requestPacket.getData());
                    String managerID = request.getManagerID();

                    /**
                     * Lock fifo to prevent the case backup receive the re-broadcast request
                     * but hasn't execute the 1st request and increase expected sequence number
                     */
                    synchronized (fifoLock) {
                        // This request is the one expected
                        if (request.getSequenceNumber() == fifo.getExpectedRequestNumber(managerID)) {
                            System.out.println(replicaManagerID.name() + ": receives " + request.getSequenceNumber() + " " + request.getFullInvocation() + " from Leader");
                            // Send acknowledgement to the leader
                            acknowledgeToLeader(request);
                            System.out.println(replicaManagerID.name() + ": acknowledges " + request.getSequenceNumber() + " " + request.getFullInvocation());

                            // Re-broadcast the request to the group
                            broadcastToGroup(request);
                            System.out.println(replicaManagerID.name() + ": re-broadcasts " + request.getSequenceNumber() + " " + request.getFullInvocation());

                            // Add the request to the queue, so it can be executed in the next step
                            fifo.holdRequest(managerID, request);

                            // Execute this request and the continuous chain of requests after it hold in the queue
                            while (true) {
                                Request currentRequest = fifo.popNextRequest(managerID);

                                // Increase the expected sequence number by 1
                                fifo.generateRequestNumber(managerID);

                                // Handle the request
                                executeRequest(currentRequest);
                                System.out.println(replicaManagerID.name() + ": executes " + request.getSequenceNumber() + " " + request.getFullInvocation());

                                // Write the request to the file
                                writeRequestToFile(request);

                                // If the next request on hold doesn't have the expected sequence number, the loop will end
                                if (fifo.peekFirstRequestHoldNumber(managerID) != fifo.getExpectedRequestNumber(managerID))
                                    break;
                            }
                        }
                        // There're other requests must come before this request
                        else if (request.getSequenceNumber() > fifo.getExpectedRequestNumber(managerID)) {
                            // Send acknowledgement to the leader
                            acknowledgeToLeader(request);
                            System.out.println(replicaManagerID.name() + ": acknowledges " + request.getSequenceNumber() + " " + request.getFullInvocation());

                            // Re-broadcast the request to the group
                            broadcastToGroup(request);
                            System.out.println(replicaManagerID.name() + ": re-broadcasts " + request.getSequenceNumber() + " " + request.getFullInvocation());

                            // Save the request to the holdback queue
                            fifo.holdRequest(managerID, request);

                            /**
                             * How to take care of the situation
                             * When the request is put to the queue
                             * But will never be execute until a new request is sent???
                             */
                        } else {
                            // Else the request is duplicated, ignore it
                            System.out.println(replicaManagerID.name() + ": re-receives " + request.getSequenceNumber() + " " + request.getFullInvocation() + " from Leader");
                        }
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            if (leaderSocket != null)
                leaderSocket.close();
        }
    }

    private void acknowledgeToLeader(Request request) {
        Unicast unicast = null;
        try {
            int leaderPort = leaderID.getCoefficient() * Config.UDP.PORT_BACKUPS_TO_LEADER;
            unicast = new Unicast(leaderPort);
            unicast.send(String.valueOf(request.getSequenceNumber()).getBytes());
        } catch (SocketException e) {
            e.printStackTrace(System.err);
        } finally {
            if (unicast != null && unicast.isSocketOpen())
                unicast.closeSocket();
        }
    }

    private void listenToAcknowledgements() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(fromBackupPort);
//            System.out.println(this.replicaManagerID + " starts listening to acks");
            while (true) {
                byte[] buffer = new byte[1000];
                DatagramPacket acknowledgement = new DatagramPacket(buffer, buffer.length);
                socket.receive(acknowledgement);
                new Thread(() -> {
                    int sequenceNumber = Integer.valueOf(new String(acknowledgement.getData()).trim());
//                    System.out.println(this.replicaManagerID.name() + " received an ack of " + sequenceNumber);
                    synchronized (acknowledgeLock) {
                        int noOfAck = acknowledgementMap.getOrDefault(sequenceNumber, 0);
                        acknowledgementMap.put(sequenceNumber, ++noOfAck);
//                        System.out.println(sequenceNumber + " now has " + noOfAck + " acks");
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            if (socket != null)
                socket.close();
        }
    }

    private void prepareORB() throws Exception {
        // Initiate client ORB
        orb = ORB.init(Config.CORBA.ORB_PARAMETERS.split(" "), null);

        // Get object reference to the Naming Service
        namingContextObj = orb.resolve_initial_references(Config.CORBA.NAME_SERVICE);

        // Narrow the NamingContext object reference to the proper type to be usable (like any CORBA object)
        namingContextRef = NamingContextExtHelper.narrow(namingContextObj);
    }

    private void broadcastToGroup(Request request) {
        // Prepare the list of Backups
        ArrayList<Integer> ports = new ArrayList<>();
        for (Config.ARCHITECTURE.REPLICAS replica : Config.ARCHITECTURE.REPLICAS.values()) {
            if (replica != this.replicaManagerID && replica != leaderID)
                ports.add(replica.getCoefficient() * Config.UDP.PORT_LEADER_TO_BACKUPS);
        }

        // Broadcast using FIFO multicast
        fifo.multiCast(ports, request);
    }

    private void waitForEnoughAcknowledgement(Request request) {
        int sequenceNumber = request.getSequenceNumber();
        while (true) {
            try {
                synchronized (acknowledgeLock) {
                    int noOfAck = acknowledgementMap.getOrDefault(sequenceNumber, 0);
//                    System.out.println(sequenceNumber + " has " + noOfAck + " acks, waiting for " + (noOfAliveRM - 1) + " acks");
                    if (noOfAck >= (noOfAliveRM - 1)) {
                        acknowledgementMap.remove(sequenceNumber);
                        break;
                    }

                }
                sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }
    }

    private void listenNewLeader() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(this.replicaManagerID.getCoefficient() * Config.UDP.PORT_NEW_LEADER);
            while (true) {
                byte[] buffer = new byte[1000];
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
//                System.out.println(replicaManagerID + ": listening to new leader");
                socket.receive(datagramPacket);

                new Thread(() -> {
                    String[] datagramContent = new String(datagramPacket.getData()).trim().split(",");
                    leaderID = Config.ARCHITECTURE.REPLICAS.valueOf(datagramContent[1]);
                    heartBeat.updateLeaderID(leaderID);
                    noOfAliveRM = Integer.valueOf(datagramContent[0]);
                    System.out.println(this.replicaManagerID.name() + " updates " + noOfAliveRM + " RMs alive, new leader is " + leaderID.name());
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            if (socket != null)
                socket.close();
        }
    }

    private void startListening() {
        // If this is leader, listen to FrontEndImpl
        new Thread(() -> listenToFrontEnd()).start();

        // and to Backups
        new Thread(() -> listenToAcknowledgements()).start();

        // If this is backup, listen to Leader
        new Thread(() -> listenToLeader()).start();
    }

    private void writeRequestToFile(Request request) {
        synchronized (fileWriterLock) {
            BufferedWriter bw = null;
            FileWriter fw = null;
            try {
                File file = new File(fileName);
                if (!file.exists()) {
                    file.createNewFile();
                }
                fw = new FileWriter(file.getAbsoluteFile(), true);
                bw = new BufferedWriter(fw);
                bw.write(request.toString() + System.lineSeparator());
            } catch (IOException e) {
                e.printStackTrace(System.err);
            } finally {
                try {
                    if (bw != null)
                        bw.close();
                    if (fw != null)
                        fw.close();
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
            }
        }
    }

    private void restoreHashMap() {
        synchronized (fileWriterLock) {
            try {
                File file = new File(fileName);
                if (file.exists()) {
                    FileReader fileReader = new FileReader(file);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    StringBuffer stringBuffer = new StringBuffer();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
//                        System.out.println(line);
                        String[] requestParts = line.split("\\(");
                        requestParts[1] = requestParts[1].replace(')', Character.MIN_VALUE).trim();
                        Config.REQUEST.METHODS_NAME methodsName = Config.REQUEST.METHODS_NAME.valueOf(requestParts[0]);

                        Request request = null;
                        String[] arguments = requestParts[1].split(", ");
                        switch (methodsName) {
                            case createTRecord:
                                request = new Request(-1, arguments[0], Config.REQUEST.METHODS_NAME.createTRecord, arguments[1], arguments[2], arguments[3], arguments[4], arguments[5], arguments[6]);
                                break;
                            case createSRecord:
                                request = new Request(-1, arguments[0], Config.REQUEST.METHODS_NAME.createSRecord, arguments[1], arguments[2], arguments[3], arguments[4]);
                                break;
                            case editRecord:
                                request = new Request(-1, arguments[0], Config.REQUEST.METHODS_NAME.editRecord, arguments[1], arguments[2], arguments[3]);
                                break;
                            case transferRecord:
                                request = new Request(-1, arguments[0], Config.REQUEST.METHODS_NAME.transferRecord, arguments[1], arguments[2]);
                                break;
                            default:
                                // Do nothing
                                break;
                        }
//                        System.out.println(request.getFullInvocation());
                        System.out.println(replicaManagerID + " restores request " + request.getFullInvocation());
                        executeRequest(request);
                    }
                    fileReader.close();
                    System.out.println(replicaManagerID + " finish restoring Database");
                }
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
    }
}
