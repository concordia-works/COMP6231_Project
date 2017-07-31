package Servers;


import Utils.Config;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Created by kamal on 7/28/2017.
 */
import Utils.Config;
import Servers.ReplicaManager;
import Utils.Configuration;

public class HeartBeat extends Thread {
    private Config.ARCHITECTURE.REPLICAS server_id;
    private int heartbeat_port;
    private int frequency = 5;
    private boolean status=false;

    public HeartBeat(Config.ARCHITECTURE.REPLICAS id, int port_no)                         //Constructor overloaded
    {
        this.server_id = id;
        System.out.println(server_id);
        this.heartbeat_port = id.getValue() *port_no;
        System.out.println(heartbeat_port);
        status=true;
    }

    public int getHeartBeat_Port(Config.ARCHITECTURE.REPLICAS s_id) {
        return s_id.getValue() *Config.UDP.PORT_HEART_BEAT;
    }

    public Config.ARCHITECTURE.REPLICAS getServer_id() {
        return server_id;
    }

    public void listner() {
        try {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    DatagramSocket socket = null;
                    try {
                        socket = new DatagramSocket(getHeartBeat_Port(server_id));
                        byte[] buffer = new byte[1000];
                        HashMap<String, Instant> msg_recieved = new HashMap();
                        for (Config.ARCHITECTURE.REPLICAS replicaID : Config.ARCHITECTURE.REPLICAS.values())
                        {
                            msg_recieved.put(replicaID.toString(),Instant.now());
                        }
                        while (true) {
                            DatagramPacket request = new DatagramPacket(buffer, buffer.length);

                            socket.receive(request);
                            System.out.println("msg" + new String(request.getData()));
                            String data_recieved = new String(request.getData());

                            msg_recieved.put(data_recieved.substring(10,data_recieved.length()) , Instant.now());

                       /*     if (data_recieved.contains("I am Alive")) {
                                msg_recieved.put(data_recieved.substring(10,data_recieved.length()) , Instant.now());
                            }*/

                            Iterator<Map.Entry<String, Instant>> itr = msg_recieved.entrySet().iterator();
                            while (itr.hasNext()) {
                                Map.Entry<String, Instant> entry = itr.next();
                                String key = entry.getKey();
                                // System.out.println(key +"]]");
                                Instant time = entry.getValue();
                                Instant current_time = Instant.now();
                                int duration = (int) Duration.between(time, current_time).getSeconds();
                                System.out.println("duration:" + duration);
                                synchronized (msg_recieved) {
                                    if (duration > frequency) {
                                        System.out.println("Key : " + entry.getKey() + " Removed.");
                                        System.out.println("server failed" + key);
                                        itr.remove();
                                        //call election system
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep((frequency - 1) * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        //finally {
        //if (socket != null) {
        //     socket.close();
        // }
    }


    public void sender() throws Exception {
        Config.ARCHITECTURE.REPLICAS threadServerID = this.server_id;
        System.out.println("this .server id"+this.server_id);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        for (Config.ARCHITECTURE.REPLICAS replicaID : Config.ARCHITECTURE.REPLICAS.values()) {
                            if (replicaID != threadServerID) {
                                send_message(replicaID);
                                System.out.println("send msg"+replicaID);
                            }
                        }
                    } catch (Exception e) {
                    }
                    try {
                        Thread.sleep(frequency * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    public void send_message(Config.ARCHITECTURE.REPLICAS s_id) throws Exception {
        InetAddress host = InetAddress.getLocalHost();
        byte[] message = ("I am Alive" + this.server_id).getBytes();
        DatagramSocket ds = new DatagramSocket();
        DatagramPacket data_packet = new DatagramPacket(message, message.length, host, getHeartBeat_Port(s_id));//first packet is sent to first serve(port no1)
        System.out.println("msg sent to port" + getHeartBeat_Port(s_id));
        ds.send(data_packet);
    }

    public void run() {
        try {
            listner();
            sender();
        } catch (Exception e) {
        }
    }
}


