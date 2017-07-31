package Servers;

import Utils.Request;
import Utils.Response;
import Utils.Unicast;

import java.net.DatagramSocket;
import java.util.*;

public class FIFO {
    private Map<String, Integer> sequenceRequestNumber;
    private final Object sequenceRequestLock = new Object();

    private Map<String, TreeMap<Integer, Request>> holdbackRequest;
    private final Object holdbackRequestLock = new Object();

    private Map<String, Integer> sequenceResponseNumber;
    private final Object sequenceResponseLock = new Object();

    private Map<String, TreeMap<Integer, Response>> holdbackResponse;
    private final Object holdbackResponseLock = new Object();

    public FIFO() {
        sequenceRequestNumber = new HashMap<>();
        holdbackRequest = new HashMap<>();
        sequenceResponseNumber = new HashMap<>();
        holdbackResponse = new HashMap<>();
    }

    // Get the current expected request sequence number and increase it by 1
    public int generateRequestNumber(String managerID) {
        synchronized (sequenceRequestLock) {
            int currentSequenceNumber = (sequenceRequestNumber.getOrDefault(managerID, 0));
            sequenceRequestNumber.put(managerID, currentSequenceNumber + 1);
            return currentSequenceNumber;
        }
    }

    // Put a request into the queue
    public void holdRequest(String managerID, Request request) {
        synchronized (holdbackRequestLock) {
            TreeMap<Integer, Request> holdbackQueue = holdbackRequest.get(managerID);
            holdbackQueue.put(request.getSequenceNumber(), request);
        }
    }

    // Get the current expected request sequence number
    public int getExpectedRequestNumber(String managerID) {
        synchronized (sequenceRequestLock) {
            return sequenceRequestNumber.getOrDefault(managerID, 0);
        }
    }

    // Get the sequence number of the next request in the queue
    public int peekFirstRequestHoldNumber(String managerID) {
        synchronized (holdbackRequestLock) {
            TreeMap<Integer, Request> holdbackQueue = holdbackRequest.get(managerID);
            return holdbackQueue.firstKey();
        }
    }

    // Get and remove the first request in the queue
    public Request popNextRequest(String managerID) {
        synchronized (holdbackRequestLock) {
            TreeMap<Integer, Request> holdbackQueue = holdbackRequest.get(managerID);
            Request request = holdbackQueue.get(holdbackQueue.firstKey());
            holdbackQueue.remove(holdbackQueue.firstKey());
            return request;
        }
    }


    // Get the current expected response sequence number and increase it by 1
    public int generateResponseNumber(String managerID) {
        synchronized (sequenceResponseLock) {
            int currentSequenceNumber = (sequenceResponseNumber.getOrDefault(managerID, 0));
            sequenceResponseNumber.put(managerID, currentSequenceNumber + 1);
            return currentSequenceNumber;
        }
    }

    // Put a response into the queue
    public void holdResponse(String managerID, Response response) {
        synchronized (holdbackResponseLock) {
            TreeMap<Integer, Response> holdbackQueue = holdbackResponse.get(managerID);
            holdbackQueue.put(response.getSequenceNumber(), response);
        }
    }

    // Get the current expected response sequence number
    public int getExpectedResponseNumber(String managerID) {
        synchronized (sequenceResponseLock) {
            return sequenceResponseNumber.getOrDefault(managerID, 0);
        }
    }

    // Get the sequence number of the next response in the queue
    public int peekFirstResponseHoldNumber(String managerID) {
        synchronized (holdbackResponseLock) {
            TreeMap<Integer, Response> holdbackQueue = holdbackResponse.get(managerID);
            return holdbackQueue.firstKey();
        }
    }

    // Get and remove the first response in the queue
    public Response popNextResponse(String managerID) {
        synchronized (holdbackResponseLock) {
            TreeMap<Integer, Response> holdbackQueue = holdbackResponse.get(managerID);
            Response response = holdbackQueue.get(holdbackQueue.firstKey());
            holdbackQueue.remove(holdbackQueue.firstKey());
            return response;
        }
    }

    // Send a message to a group of processes reliably
    public void multiCast(ArrayList<Integer> ports, Request request) {
        for (int i = 0; i < ports.size(); i++)
            uniCast(ports.get(i), request);
    }

    private void uniCast(int port, Request request) {
        Unicast unicast = null;
        try {
            unicast = new Unicast(port);
            unicast.send(request);
        } catch (Exception e) {
            e.printStackTrace();
            if (unicast != null && unicast.isSocketOpen())
                unicast.closeSocket();
        }
    }
}
