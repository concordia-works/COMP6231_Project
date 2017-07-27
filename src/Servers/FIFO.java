package Servers;

import java.net.InetAddress;
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

    /**
     * RELIABLE UNICAST
     *
     * For reliable unicast, the server needs 2 threads
     * One for sending messages, one for receiving acknowledgements
     * Sending threads
     * - After sending a message, put it into an array
     * - Each message has a timeout, messages will be resend as long as no acknowledgement
     * - After receiving the acknowledgement, remove the message from the array
     *
     * Receiving threads
     * - Every time receiving a message, send back an acknowledgement immediately
     * - Use checksums to ensure messages' integrity
     * - Check for duplicated messages
     */

    // Send a message to another process reliably
    public void uniCast(InetAddress address, int port, byte[] data) {}

    // Send a message to a group of processes reliably
    public void multiCast(InetAddress[] addresses, int[] ports, byte[] data) {}
}
