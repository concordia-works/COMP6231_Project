package Servers;

import Utils.Config;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.*;

import static java.lang.Thread.sleep;

class Election {
    private Config.ARCHITECTURE.REPLICAS replicaManagerID;
    private DatagramSocket listeningSocket;
    private int timeout;

    public Election(Config.ARCHITECTURE.REPLICAS replicaManagerID) throws SocketException {
        this.replicaManagerID = replicaManagerID;
        this.listeningSocket = new DatagramSocket(replicaManagerID.getValue() * Config.UDP.PORT_ELECTION);
        this.timeout = Config.ELECTION.ANSWER_TIMEOUT;

        new Thread(new Runnable() {
            @Override
            public void run() {
                listenToElectionMessage();
            }
        }).start();
    }

    public Config.ARCHITECTURE.REPLICAS startElection() {
        Config.ARCHITECTURE.REPLICAS maxReplicaID = this.replicaManagerID;
        boolean[] replicasStatus = new boolean[Config.ARCHITECTURE.REPLICAS.values().length];
        replicasStatus[replicaManagerID.getValue() - 1] = true;
        for (Config.ARCHITECTURE.REPLICAS replicaID : Config.ARCHITECTURE.REPLICAS.values()) {
            if (replicaID.getValue() > this.replicaManagerID.getValue()) {
                Config.ARCHITECTURE.REPLICAS threadReplicaID = replicaID;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DatagramSocket socket = new DatagramSocket();
                            sendElectionMessage(socket, threadReplicaID);
                            boolean isReplicaAlive = listenToElectionAnswer(socket);
                            synchronized (replicasStatus) {
                                replicasStatus[threadReplicaID.getValue() - 1] = isReplicaAlive;
                            }
                        } catch (SocketException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }

        try {
            // Wait for responses from all alive replicas
            sleep(4000);
            for (Config.ARCHITECTURE.REPLICAS replicaID : Config.ARCHITECTURE.REPLICAS.values()) {
                if (replicasStatus[replicaID.getValue() - 1] == true)
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
            DatagramPacket electionPacket = new DatagramPacket(buffer, buffer.length, InetAddress.getLocalHost(), toReplicaID.getValue() * Config.UDP.PORT_ELECTION);
            socket.send(electionPacket);
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
                if (answerContent.compareTo(Config.ELECTION.RESPONSE) == 0)
                    return true;
                else
                    return false;
            } catch (InterruptedIOException e) {
                e.printStackTrace(System.out);
                return false;
            } catch (Exception e) {
                e.printStackTrace(System.out);
                return false;
            } finally {
                socket.close();
            }
        }
        else
            return false;
    }

    private void listenToElectionMessage() {
        while (true) {
            try {
                byte[] receiveBuffer = new byte[1000];
                DatagramPacket electionPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                listeningSocket.receive(electionPacket);

                DatagramSocket threadSocket = listeningSocket;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String receiveContent = new String(electionPacket.getData()).trim();
                        if (receiveContent.compareTo(Config.ELECTION.MESSAGE) == 0) {
                            try {
                                byte[] sendBuffer = Config.ELECTION.RESPONSE.getBytes();
                                DatagramPacket sendingPacket = new DatagramPacket(sendBuffer, sendBuffer.length, InetAddress.getLocalHost(), electionPacket.getPort());
                                threadSocket.send(sendingPacket);
                            } catch (IOException e) {
                                e.printStackTrace(System.out);
                            }
                        }
                    }
                }).start();
            } catch (IOException e) {
                e.printStackTrace(System.out);
            }
        }
    }
}