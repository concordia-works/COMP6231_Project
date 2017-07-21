package Servers;

import java.net.InetAddress;
import java.util.*;

public class FIFO {
    private Map<String, Integer> sequenceNumberGenerator;
    private final Object sequenceNumberLock = new Object();

    private Map<String, TreeMap<Integer, Request>> holdbackRequest;
    private final Object holdbackRequestLock = new Object();

    private Map<String, TreeMap<Integer, Response>> holdbackResponse;
    private final Object holdbackResponseLock = new Object();

    public FIFO() {
        sequenceNumberGenerator = new HashMap<>();
        holdbackRequest = new HashMap<>();
        holdbackResponse = new HashMap<>();
    }

    public int nextSequenceNumber(String managerID) {
        synchronized (sequenceNumberLock) {
            int currentSequenceNumber = (sequenceNumberGenerator.get(managerID));
            sequenceNumberGenerator.put(managerID, ++currentSequenceNumber);
            return currentSequenceNumber;
        }
    }

    public void holdRequest(String managerID, Request request) {
        synchronized (holdbackRequestLock) {
            TreeMap<Integer, Request> holdbackQueue = holdbackRequest.get(managerID);
            holdbackQueue.put(request.getSequenceNumber(), request);
        }
    }

    public int getExpectedRequestNumber(String managerID) {
        synchronized (holdbackRequestLock) {
            TreeMap<Integer, Request> holdbackQueue = holdbackRequest.get(managerID);
            return holdbackQueue.firstKey();
        }
    }

    public Request getNextRequest(String managerID) {
        synchronized (holdbackRequestLock) {
            TreeMap<Integer, Request> holdbackQueue = holdbackRequest.get(managerID);
            return holdbackQueue.get(holdbackQueue.firstKey());
        }
    }

    public void holdRResponse(String managerID, Response response) {
        synchronized (holdbackResponseLock) {
            TreeMap<Integer, Response> holdbackQueue = holdbackResponse.get(managerID);
            holdbackQueue.put(response.getSequenceNumber(), response);
        }
    }

    public int getExpectedResponseNumber(String managerID) {
        synchronized (holdbackResponseLock) {
            TreeMap<Integer, Response> holdbackQueue = holdbackResponse.get(managerID);
            return holdbackQueue.firstKey();
        }
    }

    public Response getNextResponse(String managerID) {
        synchronized (holdbackResponseLock) {
            TreeMap<Integer, Response> holdbackQueue = holdbackResponse.get(managerID);
            return holdbackQueue.get(holdbackQueue.firstKey());
        }
    }

    public void uniCast(InetAddress address, int port) {}

    public void multiCast(InetAddress addresses[], int ports[]) {}
}
