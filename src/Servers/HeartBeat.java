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
public class HeartBeat extends Thread
{
    int server_id;
    int heartbeat_port;
    int frequency=5;
int x=Config.ARCHITECTURE.REPLICAS.KEN_RO.getValue() * Config.UDP.PORT_HEART_BEAT;
    public HeartBeat(int id)                         //Constructor overloaded
    {
        server_id = id;
        if (server_id == 1)
            heartbeat_port = 5100;
        else if (server_id == 2)
            heartbeat_port = 5101;
        else if (server_id == 3)
            heartbeat_port = 5102;
    }
public int getHeartBeat_Port(int server_id)
{
    if (server_id == 1)
        heartbeat_port = 5100;
    else if (server_id == 2)
        heartbeat_port = 5101;
    else if (server_id == 3)
        heartbeat_port = 5102;
    return heartbeat_port;
}

    public void listner() {
        try {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    DatagramSocket socket = null;
                    try {
                        socket = new DatagramSocket(heartbeat_port);
                        byte[] buffer = new byte[1000];
                        HashMap<Integer,Instant>msg_recieved=new HashMap();
                        while (true) {
                            DatagramPacket request = new DatagramPacket(buffer, buffer.length);


                                    socket.receive(request);
                                    System.out.println("msg"+new String(request.getData()));
                                    String data_recieved =new String(request.getData());



                                    if (data_recieved.contains("I am Alive"))
                                    {
                                        msg_recieved.put(Integer.parseInt(data_recieved.substring(10,11)), Instant.now());

                                    }

                            Iterator<Map.Entry<Integer,Instant>> itr = msg_recieved.entrySet().iterator();
                            while(itr.hasNext()) {
                                Map.Entry<Integer, Instant> entry = itr.next();
                                int key = entry.getKey();
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
                                    }
                                }
                            }
                        }
                                                            }
                     catch (Exception e) {
                      e.printStackTrace();
                    }
                    try {
                        Thread.sleep((frequency-1)*1000);
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


    public void sender() throws Exception
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        if (server_id == 1) {
                            send_message(2);
                            send_message(3);
                        } else if (server_id == 2) {
                            send_message(1);
                            send_message(3);
                        } else  {
                            send_message(1);
                            send_message(2);
                        }
                    } catch (Exception e) {
                    }
                    try {
                        Thread.sleep(frequency*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            }).start();
        }



    public void send_message(int s_id) throws Exception
    {
        InetAddress host = InetAddress.getLocalHost();
        byte [] message = ("I am Alive"+this.server_id).getBytes();
        DatagramSocket ds = new DatagramSocket();
        DatagramPacket data_packet= new DatagramPacket(message,message.length, host, getHeartBeat_Port(s_id));//first packet is sent to first serve(port no1)
        System.out.println("msg sent to port"+getHeartBeat_Port(s_id));
        ds.send(data_packet);
    }

    public void run()
    {
        try {
            listner();
            sender();
        }
        catch(Exception e) {
        }
    }
}


