package FrontEnd;

import Servers.FIFO;
import Utils.Request;
import Utils.Config;
import Utils.Response;
import org.omg.CORBA.ORB;

import java.net.SocketException;

public class FrontEndImpl extends FEPOA {
    private ORB orb;
    private FIFO fifo = new FIFO();
    private int leaderPort;

    public FrontEndImpl() {
        super();
    }

    public void setORB(ORB orb_val) {
        this.orb = orb_val;
    }

    @Override
    public String createTRecord(String managerID, String firstName, String lastName, String address, String phone, String specialization, String location) {
        Request request = new Request(fifo.generateRequestNumber(managerID), managerID, Config.REQUEST.METHODS_NAME.createTRecord, firstName, lastName, address, phone, specialization, location);
        return sendAndGetResponse(request);
    }

    @Override
    public String createSRecord(String managerID, String firstName, String lastName, String coursesRegistered, String status) {
        Request request = new Request(fifo.generateRequestNumber(managerID), managerID, Config.REQUEST.METHODS_NAME.createSRecord, firstName, lastName, coursesRegistered, status);
        return sendAndGetResponse(request);
    }

    @Override
    public String getRecordCounts(String managerID) {
        Request request = new Request(fifo.generateRequestNumber(managerID), managerID, Config.REQUEST.METHODS_NAME.getRecordsCount);
        return sendAndGetResponse(request);
    }

    @Override
    public String editRecord(String managerID, String recordID, String fieldName, String newValue) {
        Request request = new Request(fifo.generateRequestNumber(managerID), managerID, Config.REQUEST.METHODS_NAME.editRecord, recordID, fieldName, newValue);
        return sendAndGetResponse(request);
    }

    @Override
    public String transferRecord(String managerID, String recordID, String remoteCenterServerName) {
        Request request = new Request(fifo.generateRequestNumber(managerID), managerID, Config.REQUEST.METHODS_NAME.transferRecord, recordID, remoteCenterServerName);
        return sendAndGetResponse(request);
    }

    @Override
    public String printRecord(String managerID, String recordID) {
        Request request = new Request(fifo.generateRequestNumber(managerID), managerID, Config.REQUEST.METHODS_NAME.printRecord);
        return sendAndGetResponse(request);
    }

    @Override
    public String printAllRecords(String managerID) {
        Request request = new Request(fifo.generateRequestNumber(managerID), managerID, Config.REQUEST.METHODS_NAME.printAllRecords);
        return sendAndGetResponse(request);
    }

    private String sendAndGetResponse(Request request) {
        try {
            Unicast unicast = new Unicast(leaderPort);
            unicast.send(request);
            Response response = unicast.receive();
            return response.getContent();
        } catch (SocketException e) {
            e.printStackTrace(System.out);
        }
        return "";
    }
}
