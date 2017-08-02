package Servers;

import Utils.Config;
import com.sun.org.apache.regexp.internal.RE;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.*;

import static java.lang.Thread.sleep;

class Election implements Runnable {
    private Config.ARCHITECTURE.REPLICAS replicaManagerID = null;
    private DatagramSocket listeningSocket;
    private int timeout;

    public Election(Config.ARCHITECTURE.REPLICAS replicaManagerID) throws SocketException {
        this.replicaManagerID = replicaManagerID;
        int port = replicaManagerID.getCoefficient() * Config.UDP.PORT_ELECTION;
        this.listeningSocket = new DatagramSocket(port);
        this.timeout = Config.ELECTION.ANSWER_TIMEOUT;
    }

    public Config.ARCHITECTURE.REPLICAS startElection() {
        Config.ARCHITECTURE.REPLICAS maxReplicaID = this.replicaManagerID;
        boolean[] replicasStatus = new boolean[Config.ARCHITECTURE.REPLICAS.values().length];
        if (replicaManagerID != null) {
            replicasStatus[replicaManagerID.getCoefficient() - 1] = true;
            for (Config.ARCHITECTURE.REPLICAS replicaID : Config.ARCHITECTURE.REPLICAS.values()) {
                if (replicaID.getCoefficient() > this.replicaManagerID.getCoefficient()) {
                    Config.ARCHITECTURE.REPLICAS threadReplicaID = replicaID;
                    checkAlive(threadReplicaID, replicasStatus);
                }
            }
        } else {
            for (Config.ARCHITECTURE.REPLICAS replicaID : Config.ARCHITECTURE.REPLICAS.values()) {
                Config.ARCHITECTURE.REPLICAS threadReplicaID = replicaID;
                checkAlive(threadReplicaID, replicasStatus);
            }
        }

        try {
            // Wait for responses from all alive replicas
            sleep(Config.ELECTION.ELECTION_TIMEOUT);
            for (Config.ARCHITECTURE.REPLICAS replicaID : Config.ARCHITECTURE.REPLICAS.values()) {
                if (replicasStatus[replicaID.getCoefficient() - 1] == true)
                    maxReplicaID = replicaID;
            }
        } catch (InterruptedException e) {
            e.printStackTrace(System.out);
        }
        return maxReplicaID;
    }

    private void sendElectionMessage(DatagramSocket socket, Config.ARCHITECTURE.REPLICAS toReplicaID) {
        try {
            byte[] buffer = Config.ELECTION.MESSAGE.getBytes();
            DatagramPacket electionPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getLocalHost(), toReplicaID.getCoefficient() * Config.UDP.PORT_ELECTION);
            socket.send(electionPacket);
//            System.out.println(this.replicaManagerID.name() + " send election message to replica " + toReplicaID.name() + " from port " + socket.getLocalPort() + " to port " + toReplicaID.getCoefficient() * Config.UDP.PORT_ELECTION);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    private boolean listenToElectionAnswer(DatagramSocket socket) {
        if (socket != null) {
            byte[] buffer = new byte[1000];
            DatagramPacket answerPacket = new DatagramPacket(buffer, buffer.length);
            try {
                socket.setSoTimeout(timeout);
                socket.receive(answerPacket);
                String answerContent = new String(answerPacket.getData()).trim();
                if (answerContent.compareTo(Config.ELECTION.RESPONSE) == 0) {
//                    System.out.println(this.replicaManagerID.name() + " answers to election message at port " + socket.getLocalPort());
                    return true;
                } else {
//                    System.out.println("Replica NOT answers to election message " + answerContent);
                    return false;
                }
            } catch (SocketTimeoutException e) {
                System.err.println(this.replicaManagerID.name() + " don't get answer at port " + socket.getLocalPort());
//                e.printStackTrace(System.out);
                return false;
            } catch (Exception e) {
                System.err.println("Error: " + e);
//                e.printStackTrace(System.out);
                return false;
            } finally {
                socket.close();
            }
        } else
            return false;
    }

    private void listenToElectionMessage() {
        while (true) {
            try {
                byte[] receiveBuffer = new byte[1000];
                DatagramPacket electionPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
//                System.out.println(this.replicaManagerID.name() + " listen to election message at port " + listeningSocket.getLocalPort());
                listeningSocket.receive(electionPacket);
//                System.out.println(this.replicaManagerID.name() + " get the election message");
                String receiveContent = new String(electionPacket.getData()).trim();
                if (receiveContent.compareTo(Config.ELECTION.MESSAGE) == 0) {
                    byte[] sendBuffer = Config.ELECTION.RESPONSE.getBytes();
                    DatagramPacket sendingPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getLocalHost(), electionPacket.getPort());
                    listeningSocket.send(sendingPacket);
//                    System.out.println(this.replicaManagerID.name() + " response to the election message to port " + sendingPacket.getPort());
                }
            } catch (IOException e) {
                e.printStackTrace(System.out);
            }
        }
    }

    public void announceNewLeader(Config.ARCHITECTURE.REPLICAS newLeaderID) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            byte[] buffer = newLeaderID.name().getBytes();
            for (Config.ARCHITECTURE.REPLICAS replicaID : Config.ARCHITECTURE.REPLICAS.values()) {
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getLocalHost(), replicaID.getCoefficient() * Config.UDP.PORT_NEW_LEADER);
                socket.send(datagramPacket);
//                System.out.println(this.replicaManagerID.name() + " announce new leader " + newLeaderID.name() + " to " + replicaID.name());
            }
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getLocalHost(), Config.FRONT_END.COEFFICIENT * Config.UDP.PORT_NEW_LEADER);
            socket.send(datagramPacket);
            System.out.println(this.replicaManagerID.name() + " announces new leader is " + newLeaderID.name());
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    private void checkAlive(Config.ARCHITECTURE.REPLICAS threadReplicaID, boolean[] replicasStatus) {
        new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket();
                sendElectionMessage(socket, threadReplicaID);
                boolean isReplicaAlive = listenToElectionAnswer(socket);
                synchronized (replicasStatus) {
                    replicasStatus[threadReplicaID.getCoefficient() - 1] = isReplicaAlive;
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void run() {
        new Thread(() -> listenToElectionMessage()).start();

        try {
            sleep(20);
            System.out.println(this.replicaManagerID.name() + " starts new election");
            Config.ARCHITECTURE.REPLICAS newLeaderID = startElection();
            announceNewLeader(newLeaderID);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}