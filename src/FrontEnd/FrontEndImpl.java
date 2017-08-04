package FrontEnd;

import Servers.FIFO;
import Utils.Request;
import Utils.Config;
import Utils.Response;
import Utils.Unicast;
import org.omg.CORBA.ORB;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class FrontEndImpl extends FEPOA {
    private ORB orb;
    private FIFO fifo = new FIFO();
    private int leaderPort;

    public FrontEndImpl() {
        super();
        new Thread(() -> listenNewLeader()).start();
    }

    public void setORB(ORB orb_val) {
        this.orb = orb_val;
    }

    @Override
    public String createTRecord(String managerID, String firstName, String lastName, String address, String phone, String specialization, String location) {
        Request request = new Request(fifo.generateRequestNumber(managerID), managerID, Config.REQUEST.METHODS_NAME.createTRecord, firstName, lastName, address, phone, specialization, location);
//        System.out.println(request.getFullInvocation());
        return sendAndGetResponse(request);
    }

    @Override
    public String createSRecord(String managerID, String firstName, String lastName, String coursesRegistered, String status) {
        Request request = new Request(fifo.generateRequestNumber(managerID), managerID, Config.REQUEST.METHODS_NAME.createSRecord, firstName, lastName, coursesRegistered, status);
//        System.out.println(request.getFullInvocation());
        return sendAndGetResponse(request);
    }

    @Override
    public String getRecordCounts(String managerID) {
        Request request = new Request(fifo.generateRequestNumber(managerID), managerID, Config.REQUEST.METHODS_NAME.getRecordsCount);
//        System.out.println(request.getFullInvocation());
        return sendAndGetResponse(request);
    }

    @Override
    public String editRecord(String managerID, String recordID, String fieldName, String newValue) {
        Request request = new Request(fifo.generateRequestNumber(managerID), managerID, Config.REQUEST.METHODS_NAME.editRecord, recordID, fieldName, newValue);
//        System.out.println(request.getFullInvocation());
        return sendAndGetResponse(request);
    }

    @Override
    public String transferRecord(String managerID, String recordID, String remoteCenterServerName) {
        Request request = new Request(fifo.generateRequestNumber(managerID), managerID, Config.REQUEST.METHODS_NAME.transferRecord, recordID, remoteCenterServerName);
//        System.out.println(request.getFullInvocation());
        return sendAndGetResponse(request);
    }

    @Override
    public String printRecord(String managerID, String recordID) {
        Request request = new Request(fifo.generateRequestNumber(managerID), managerID, Config.REQUEST.METHODS_NAME.printRecord, recordID);
//        System.out.println(request.getFullInvocation());
        return sendAndGetResponse(request);
    }

    @Override
    public String printAllRecords(String managerID) {
        Request request = new Request(fifo.generateRequestNumber(managerID), managerID, Config.REQUEST.METHODS_NAME.printAllRecords);
//        System.out.println(request.getFullInvocation());
        return sendAndGetResponse(request);
    }

    private String sendAndGetResponse(Request request) {
        Unicast unicast = null;
        try {
            unicast = new Unicast(leaderPort);
            unicast.send(request);
            System.out.println("Forward " + request.getSequenceNumber() + " " + request.getFullInvocation() + " to port " + leaderPort);
            Response response = unicast.receive();
            System.out.println("Receive answer for " + request.getSequenceNumber() + " " + request.getFullInvocation() + " is " + response.getContent());
            return response.getContent();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        } finally {
            if (unicast != null)
                unicast.closeSocket();
        }
        return "";
    }

    private void listenNewLeader() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(Config.FRONT_END.COEFFICIENT * Config.UDP.PORT_NEW_LEADER);
            while (true) {
                byte[] buffer = new byte[1000];
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
//                System.out.println("FrontEnd listens to new leader");
                socket.receive(datagramPacket);
                String[] datagramContent = new String(datagramPacket.getData()).trim().split(",");
                Config.ARCHITECTURE.REPLICAS newLeaderID = Config.ARCHITECTURE.REPLICAS.valueOf(datagramContent[1]);
                this.leaderPort = newLeaderID.getCoefficient() * Config.UDP.PORT_FRONT_END_TO_LEADER;
                System.out.println("FrontEnd updates the new leader is " + newLeaderID.name());
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        } finally {
            if (socket != null)
                socket.close();
        }
    }
}
