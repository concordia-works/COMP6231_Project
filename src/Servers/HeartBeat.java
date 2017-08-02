package Servers;

//i am making hashmap status to store each servers status-true/false

import Utils.Config;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by kamal on 7/28/2017.
 */

public class HeartBeat extends Thread {
    private Config.ARCHITECTURE.REPLICAS server_id;
    private int heartbeat_port;
    private int frequency = 5;

    private static HashMap<String,Boolean> status=new HashMap<String,Boolean>();
    static
    {
        status.put("MINH",false);
        status.put("KEN_RO",false);
        status.put("KAMAL",false);
    }
    public HeartBeat(Config.ARCHITECTURE.REPLICAS id, int port_no)
    {
        this.server_id = id;
        System.out.println(server_id);
        this.heartbeat_port = id.getCoefficient() *port_no;
        synchronized(status) {
            status.replace(server_id.toString(), true);//status becomes true when Heartbeat starts
        }
    }
    public int getHeartBeat_Port(Config.ARCHITECTURE.REPLICAS s_id) {
        return s_id.getCoefficient() *Config.UDP.PORT_HEART_BEAT;
    }

    public Config.ARCHITECTURE.REPLICAS getServer_id() {
        return server_id;
    }

    public boolean get_status(String server_id)         //return status
    {
        boolean result=false;
        Iterator<Map.Entry<String, Boolean>> itr = status.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, Boolean> entry = itr.next();
            if(entry.getKey().equals(server_id))
                result=entry.getValue();
        }
        return result;
    }
    public void listner() {
        try {
            Config.ARCHITECTURE.REPLICAS threadServerID=this.server_id;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DatagramSocket socket = null;
                    try {
                        socket = new DatagramSocket(getHeartBeat_Port(server_id));
                        byte[] buffer = new byte[1000];
                        HashMap<String, Instant> msg_recieved = new HashMap();
                        synchronized (msg_recieved) {
                            for (Config.ARCHITECTURE.REPLICAS replicaID : Config.ARCHITECTURE.REPLICAS.values()) {
                                if (replicaID != threadServerID) {
                                    msg_recieved.put(replicaID.toString(), Instant.now());
                                }
                            }
                            while (true) {
                                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                                socket.receive(request);

                                String data_recieved =new String(new String(request.getData(), request.getOffset(),request.getLength()));

                                System.out.println("data recieved"+data_recieved);

                                msg_recieved.replace(data_recieved.substring(10,data_recieved.length()) , Instant.now());


                                Iterator<Map.Entry<String, Instant>> itr = msg_recieved.entrySet().iterator();
                                while (itr.hasNext()) {
                                    Map.Entry<String, Instant> entry = itr.next();
                                    String key = entry.getKey();
                                    System.out.println(key +entry.getValue());
                                    Instant time = entry.getValue();
                                    Instant current_time = Instant.now();
                                    int duration = (int) Duration.between(time, current_time).getSeconds();
                                    System.out.println(duration);

                                    if (duration > frequency+1) {
                                        System.out.println("Key : " + entry.getKey() + " Removed");
                                        System.out.println("server failed" + key);
                                        String failed_server=key;
                                        //set_status(failed_server);
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

        new Thread(new Runnable() {
            @Override
            public void run() {

                while (true) {
                    try {
                        for (Config.ARCHITECTURE.REPLICAS replicaID : Config.ARCHITECTURE.REPLICAS.values()) {
                            // if (replicaID != threadServerID&&get_status(replicaID.toString()))//actual code needs to be this
                            if(replicaID!=threadServerID){
                                {
                                    send_message(replicaID);
                                    System.out.print("msg sent to"+replicaID);
                                }
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