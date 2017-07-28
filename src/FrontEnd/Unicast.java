package FrontEnd;

import Utils.Config;
import Utils.Request;
import Utils.Response;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Unicast {
    private DatagramSocket socket;
    int serverPort;

    public Unicast(int serverPort) throws SocketException {
        this.serverPort = serverPort;
        socket = new DatagramSocket();
    }

    /**
     * Basic: UN-RELIABLE UNICAST
     */
    public boolean send(Request request) {
        try {
            byte[] buffer = request.serialize();
            DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getLocalHost(), serverPort);
            socket.send(requestPacket);
        } catch (IOException e) {
            e.printStackTrace(System.out);
            return false;
        }
        return true;
    }

    public Response receive() {
        try {
            byte[] buffer = new byte[3000];
            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(responsePacket);
            return Config.deserializeResponse(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null)
                socket.close();
        }
        return null;
    }

    /**
     * Advanced: RELIABLE UNICAST
     *
     * For reliable unicast, the server needs 2 threads
     * One for sending messages, one for receiving acknowledgements
     * Sending threads
     * - After sending a message, put it into an array
     * - After each period of time (timeout), every request in the array would be resent
     * - After receiving the acknowledgement, remove the message from the array
     *
     * Receiving threads
     * - Every time receiving a message, send back an acknowledgement immediately
     * - Use checksums to ensure messages' integrity
     * - Check for duplicated messages
     */
}
