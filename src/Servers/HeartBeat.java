package Servers;

import Utils.Config;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.sleep;

/**
 * Created by kamal on 7/28/2017.
 */

public class HeartBeat implements Runnable {
    private Config.ARCHITECTURE.REPLICAS replicaManagerID;
    private int heartbeatPort;
    private Map<Config.ARCHITECTURE.REPLICAS, Boolean> replicaManagerStatus;
    private final Object replicaManagerStatusLock = new Object();
    private Map<Config.ARCHITECTURE.REPLICAS, Long> mostRecentAliveTime;
    private final Object mostRecentAliveTimeLock = new Object();
    private Election election;
    private boolean[] replicasStatus;
    private Config.ARCHITECTURE.REPLICAS leaderID;

    // A HeartBeat object is created when a RM starts
    public HeartBeat(Config.ARCHITECTURE.REPLICAS replicaManagerID, int heartbeatPort) throws SocketException {
        this.replicaManagerID = replicaManagerID;
        this.heartbeatPort = heartbeatPort;
        // Start listening to election messages
        this.election = new Election(this.replicaManagerID);

        replicasStatus = new boolean[Config.ARCHITECTURE.REPLICAS.values().length];
        for (Config.ARCHITECTURE.REPLICAS replicaID : Config.ARCHITECTURE.REPLICAS.values())
            replicasStatus[replicaID.getCoefficient() - 1] = true;

        // Assume all RMs are alive when HeartBeat object is initiated
        this.replicaManagerStatus = Collections.synchronizedMap(new HashMap<Config.ARCHITECTURE.REPLICAS, Boolean>());
        this.replicaManagerStatus.put(Config.ARCHITECTURE.REPLICAS.KEN_RO, true);
        this.replicaManagerStatus.put(Config.ARCHITECTURE.REPLICAS.KAMAL, true);
        this.replicaManagerStatus.put(Config.ARCHITECTURE.REPLICAS.MINH, true);
        this.mostRecentAliveTime = Collections.synchronizedMap(new HashMap<Config.ARCHITECTURE.REPLICAS, Long>());
        this.mostRecentAliveTime.put(Config.ARCHITECTURE.REPLICAS.KEN_RO, System.nanoTime() / 1000000);
        this.mostRecentAliveTime.put(Config.ARCHITECTURE.REPLICAS.KAMAL, System.nanoTime() / 1000000);
        this.mostRecentAliveTime.put(Config.ARCHITECTURE.REPLICAS.MINH, System.nanoTime() / 1000000);
    }

    @Override
    public void run() {
        // Start listening to HeartBeat messages from other RMs
        new Thread(() -> listenToAliveMessage()).start();

        // Start sending Alive messages to other RMs
        new Thread(() -> broadcastAliveMessage()).start();

        // Start checking Alive status of RMs
        new Thread(() -> checkingAlive()).start();

        // Start a new election after the RM is online
        new Thread(() -> {
//            new Thread(() -> election.listenToElectionMessage()).start();

            try {
                sleep(Config.ELECTION.ELECTION_DELAY);
//                System.out.println(replicaManagerID.name() + " starts new election");
                election.startElection(replicasStatus);
                election.announceNewLeader();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
        }).start();
    }

    public void updateLeaderID(Config.ARCHITECTURE.REPLICAS leaderID) {
        this.leaderID = leaderID;
    }

    private void listenToAliveMessage() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(heartbeatPort);

            while (true) {
                byte[] buffer = new byte[1000];
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(datagramPacket);

                new Thread(() -> {
                    long currentTime = System.nanoTime() / 1000000;
                    String dataReceived = new String(datagramPacket.getData()).trim();
                    Config.ARCHITECTURE.REPLICAS replicaID = Config.ARCHITECTURE.REPLICAS.valueOf(dataReceived);
//                    System.out.println(replicaManagerID + ": knows " + replicaID + " is alive at " + currentTime);
                    synchronized (replicaManagerStatusLock) {
                        replicaManagerStatus.put(replicaID, true);
                    }
                    synchronized (mostRecentAliveTimeLock) {
                        mostRecentAliveTime.put(replicaID, currentTime);
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

    private void broadcastAliveMessage() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            byte[] buffer = replicaManagerID.name().getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
            datagramPacket.setAddress(InetAddress.getLocalHost());
            while (true) {
                for (Config.ARCHITECTURE.REPLICAS replicaID : Config.ARCHITECTURE.REPLICAS.values()) {
                    if (replicaID != replicaManagerID) {
                        datagramPacket.setPort(replicaID.getCoefficient() * Config.UDP.PORT_HEART_BEAT);
                        socket.send(datagramPacket);
//                        System.out.println("Send " + replicaManagerID.name() + " is alive to " + replicaID.name());
                    }
                }
                sleep(Config.HEARTBEAT.HEART_BEAT_DELAY);
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            if (socket != null)
                socket.close();
        }
    }

    private void checkingAlive() {
        try {
            while (true) {
                for (Config.ARCHITECTURE.REPLICAS replicaID : Config.ARCHITECTURE.REPLICAS.values()) {
                    if (isReplicaAlive(replicaID) && replicaID != replicaManagerID) {
                        long mostRecentTime;
                        synchronized (mostRecentAliveTimeLock) {
                            mostRecentTime = mostRecentAliveTime.get(replicaID);
                        }
                        long currentTime = System.nanoTime() / 1000000;
//                            System.out.println("currentTime - mostRecentTime = " + currentTime + " - " + mostRecentTime + " = " + (currentTime - mostRecentTime));

                        // The replica is failed
                        if (currentTime - mostRecentTime > Config.HEARTBEAT.HEART_BEAT_TIMEOUT) {
                            System.out.println(replicaManagerID.name() + ": knows " + replicaID.name() + " is CRASH, current leader is " + leaderID);
                            synchronized (replicaManagerStatusLock) {
                                replicaManagerStatus.put(replicaID, false);
                            }

                            // Restart the crashed replica
//                            new Thread(() -> {
//                                ReplicaManager replicaManager = new ReplicaManager(replicaID);
//                                new Thread(replicaManager).start();
//                            }).start();

                            // Start new election if leader is failed
                            if (replicaID.equals(leaderID)) {
                                new Thread(() -> {
                                    System.out.println(replicaManagerID + ": starts new election");
                                    election.startElection(replicasStatus);
                                    election.announceNewLeader();
                                }).start();
                            }
                        }
                    }
                }
                sleep(Config.HEARTBEAT.HEART_BEAT_TIMEOUT);
            }
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }

    private boolean isReplicaAlive(Config.ARCHITECTURE.REPLICAS replicaID) {
        synchronized (replicaManagerStatusLock) {
            return replicaManagerStatus.get(replicaID);
        }
    }
}