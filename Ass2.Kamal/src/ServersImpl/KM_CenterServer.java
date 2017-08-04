package ServersImpl;

import Ass2CORBA.DCMSPOA;
import org.omg.CORBA.ORB;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.logging.Logger;

public class KM_CenterServer extends DCMSPOA {
    private ORB orb;
    int rmi_port = 0;                                                        //rmi port of server
    int udp_port = 0;                                                        //udp port of server
    String server_name = "", username = "";
    DatagramSocket ds;                                                    //socket
    DatagramPacket data_packet_request_1, data_packet_request_2;            //packets to send request
    DatagramPacket data_packet_reply_1, data_packet_reply_2;                //packets to recieve request

    Boolean success_flag = false;                                            //flag indicating success or failure of a function
    String record_id = "";
    int student_count = 0;                                            //student and teacher count intialized to zero
    int teacher_count = 0;
    int recordIDGenerator = 0;

    String count_result[] = {"0", "0", "0"};                                //String array to store count of all three servers

    byte[] byte_array_request = new byte[1000];                            //byte array to send request
    byte[] byte_array_reply = new byte[1000];                            //byte array to recieve reply
    private final java.lang.Object lockID = new java.lang.Object();
    private final java.lang.Object lockCount = new java.lang.Object();

    Logger logger = Logger.getLogger(Logger.class.getName());            //instance of Logger class

    HashMap<Character, ArrayList<Record>> map = new HashMap<Character, ArrayList<Record>>();    //hashmap of type character as key and arraylist as value

    public KM_CenterServer() throws RemoteException {
        super();
    }                            //default constructor

    public void setOrb(ORB orb_value) {
        orb = orb_value;
    }

    public KM_CenterServer(String x) throws RemoteException                          //Constructor overloaded
    {
        server_name = x;

        if (x.equals("KM_MTL")) {
            rmi_port = 2964;
            udp_port = 5434;
            recordIDGenerator = 0;
        } else if (x.equals("KM_LVL")) {
            rmi_port = 2965;
            udp_port = 5439;
            recordIDGenerator = 1;
        } else if (x.equals("KM_DDO")) {
            rmi_port = 2966;
            udp_port = 5436;
            recordIDGenerator = 2;
        }
    }

    public int get_rmi_port() {
        return rmi_port;
    }

    public int get_udp_port() {
        return udp_port;
    }

    private int total_no_of_records()                            //returns the total number of records on server
    {
        synchronized (lockCount) {
            return (student_count + teacher_count);
        }
    }

    private void insert(Record record, char first_letter)                //inserts the new recordinto arraylist and that arraylist into hashmap
    {
        if (map.containsKey(first_letter)) {
            ArrayList<Record> old_arraylist = new ArrayList<Record>();
            old_arraylist = map.get(first_letter);

            ArrayList<Record> new_arraylist = new ArrayList<Record>();
            synchronized (new_arraylist) {
                new_arraylist.addAll(0, old_arraylist);
                new_arraylist.add(record);
                map.remove(first_letter);
                map.put(first_letter, new_arraylist);
            }
        } else {
            ArrayList<Record> new_arraylist = new ArrayList<Record>();
            synchronized (new_arraylist) {
                new_arraylist.add(record);
                map.put(first_letter, new_arraylist);
            }
        }

    }

    private String[] send_and_recieve_packets(int port1, int port2)        //invoked server sends the count request to other two servers and recieve their reply
    {
        String result[] = {"", ""};
        try {

            InetAddress host = InetAddress.getLocalHost();
            byte_array_request = "count".getBytes();
            ds = new DatagramSocket();
            data_packet_request_1 = new DatagramPacket(byte_array_request, byte_array_request.length, host, port1);//first packet is sent to first serve(port no1)
            this.ds.send(data_packet_request_1);


            data_packet_reply_1 = new DatagramPacket(byte_array_reply, byte_array_reply.length);
            this.ds.receive(data_packet_reply_1);

            ds = new DatagramSocket();
            data_packet_request_2 = new DatagramPacket(byte_array_request, byte_array_request.length, host, port2);
            ds.send(data_packet_request_2);

            data_packet_reply_2 = new DatagramPacket(byte_array_reply, byte_array_reply.length);
            this.ds.receive(data_packet_reply_2);

            result[0] = new String(data_packet_reply_1.getData()).trim();
            result[1] = new String(data_packet_reply_2.getData()).trim();
        } catch (Exception e) {

        }

        return result;
    }

    public String createTRecord(String manager_id, String f_name, String l_name, String addr, String number, String spec, String loc) {                                                                                    //creates the teacher record
        String result = "";
        synchronized (lockID) {
            teacher_count++;

            String formatted = String.format("%05d", recordIDGenerator);
            recordIDGenerator += 3;
            record_id = "TR" + formatted;


            Record r = new Record(record_id, f_name, l_name, addr, number, spec, loc);

            char first = l_name.charAt(0);
            insert(r, first);
            result = record_id;
        }         //calls insert method to insert the new record	success_flag=true;
//        logger.info("Teacher record created by " + manager_id);
        return result;
    }

    public String createSRecord(String manager_id, String f_name, String l_name, String courses, String status) {                                                                            //creates student record
        String result = "";
        synchronized (lockID) {
            ++student_count;

            //System.out.println("student count"+student_count);

            String formatted = String.format("%05d", recordIDGenerator);
            recordIDGenerator += 3;
            record_id = "SR" + formatted;
            String dat = new Date(System.currentTimeMillis()).toString();
            Record r = new Record(record_id, f_name, l_name, courses, status, dat);

            char first = l_name.charAt(0);

            insert(r, first);
            result = record_id;
        }
        success_flag = true;
//        logger.info("Student record created by " + manager_id);
        return result;
    }

    public String printAllRecords(String manager_id)                                            //displays all the records on a server
    {
        StringBuffer str = new StringBuffer();
        String result;
        for (Map.Entry<Character, ArrayList<Record>> entry : map.entrySet()) {
            for (Record x : entry.getValue()) {
                str.append((x));
                str.append("\n");
            }
        }
        result = str.toString();
        if (result.compareTo("") == 0)
            return "There is no record to print";
//        logger.info("Display function performed by " + manager_id);
        return result;
    }

    public String printRecord(String managerID, String recordID)                                            //displays all the records on a server
    {
        boolean isFound = false;
//        System.out.println("inside print record");
        StringBuffer str = new StringBuffer();
        String result = "";
        for (Map.Entry<Character, ArrayList<Record>> entry : map.entrySet()) {
            synchronized (lockID) {
                for (Record x : entry.getValue()) {
                    if (recordID.equals(x.record_id)) {
//                        System.out.println("inside if");
                        isFound = true;
                        str.append("[" + entry.getKey() + "]");
                        str.append((x));
                        str.append("\n");
                    }
                }
            }
        }
        result = str.toString();
//        logger.info("Display function performed by " + managerID);
        if (!isFound)
            return "There is no record to print";
        return result;
    }

    public synchronized String getRecordCounts(String manager_id) {               //returns the total number of records on all the servers
        String result_value = "";
        try {
            int record_count = this.total_no_of_records();
            String[] result = {""};
            switch (server_name) {
                case "KM_MTL": {
                    count_result[0] = "MTL: " + Integer.toString(record_count);
                    result = send_and_recieve_packets(5436, 5439); //calls send_and_recieve_packets method to recieve count from other two servers
                    count_result[1] = " LVL: " + result[0];
                    count_result[2] = " DDO: " + result[1];
                    break;
                }

                case "KM_LVL":
                    count_result[0] = "LVL: " + Integer.toString(record_count);
                    result = send_and_recieve_packets(5434, 5436);
                    count_result[1] = " MTL: " + result[0];
                    count_result[2] = " DDO: " + result[1];
                    break;

                case "KM_DDO":
                    count_result[0] = "DDO: " + Integer.toString(record_count);
                    result = send_and_recieve_packets(5434, 5439);
                    count_result[1] = " MTL: " + result[0];
                    count_result[2] = " LVL: " + result[1];
                    break;
            }
            result_value = count_result[0] + " " + count_result[1] + " " + count_result[2];
//            logger.info("gerRecordCount function performed by user " + manager_id + "  " + count_result[0] + count_result[1] + count_result[2]);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return result_value;
    }

    public boolean editRecord(String manager_id, String id, String field_name, String value)            //edit the record with given record id
    {
        success_flag = false;
        for (Map.Entry<Character, ArrayList<Record>> entry : map.entrySet()) {
            synchronized (lockID) {
                for (Record x : entry.getValue()) {
                    if (id.equals(x.record_id)) {
                        String sub = (x.record_id).substring(0, 2);
                        if (sub.equals("TR")) {                                                //edit the record of teacher
                            if (field_name.equals("address")) {
                                x.address = value;
                                success_flag = true;
//                                logger.info("Address is edited for record id:" + record_id + " by " + manager_id);

                            } else if (field_name.equals("phone")) {
                                x.phone = value;
                                success_flag = true;
//                                logger.info("Phone no is edited for record id:" + record_id + "by " + manager_id);
                            } else if (field_name.equals("location")) {
                                if (value.equals("MTL") || value.equals("LVL") || value.equals("DDO")) {
                                    x.location = value;
                                    success_flag = true;
//                                    logger.info("Location is edited for record id:" + record_id + " by " + manager_id);
                                } else {
//                                    logger.info("Operation failed:invalid location");
                                    success_flag = false;
                                }
                            }
                        } else                                                //edit the record of student
                        {
                            if (field_name.equals("status")) {

                                if (value.equals("Active") || value.equals("DeActive")) {
                                    x.status = value;
                                    x.date = new Date(System.currentTimeMillis()).toString();
                                    success_flag = true;
//                                    logger.info("Status is edited for record id:" + record_id + " by " + manager_id);

                                } else {
                                    success_flag = false;
//                                    logger.info("Status edit failed by " + manager_id);
                                }
                            } else if (field_name.equals("coursesRegistered")) {
                                x.courses_Registered = value;
                                success_flag = true;
//                                logger.info("Courses_Registered is edited for record id:" + record_id + " by " + manager_id);

                            } else {
                                success_flag = false;
                            }
                        }
                    }
                }
            }

        }
        return success_flag;
    }


    public void startUDPServer() {                       //starts the udp server on given port
        try {
            ds = new DatagramSocket(udp_port);
            while (true) {
                byte[] a = new byte[2000];
                DatagramPacket data_packet = new DatagramPacket(a, a.length);
                ds.receive(data_packet);                                            //data is recieved in data packet
                DatagramSocket socket = ds;

                try {
                    String data = new String(data_packet.getData());
                    if (data.trim().equals("count")) {                        //to recieve message count, to send record count
                        int count = total_no_of_records();
                        byte[] b = (count + "").getBytes();
                        DatagramPacket data_packet_respone = new DatagramPacket(b, b.length, data_packet.getAddress(), data_packet.getPort());
                        socket.send(data_packet_respone);
                    } else {//to recieve transfered record
//                        System.out.println(data);
                        String fields[] = data.split("\\|");
                        String record_id = fields[0];
                        String f_name = fields[1];
                        String l_name = fields[2];

                        if (record_id.contains("SR")) {                        //if record is student record
                            String courses = fields[4];
                            String status = fields[3];
                            String date = fields[5];

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    transferSRecord(record_id, f_name, l_name, courses, status);
                                }
                            }).start();
                            //createSRecord() is called
                        } else {
//                            System.out.println("inside else");//if record id teacher record
                            String addr = fields[3];
                            String phone = fields[4];
                            String spec = fields[5];
                            String loc = fields[6];
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    transferTRecord(record_id, f_name, l_name, addr, phone, spec, loc);
                                }
                            }).start();      //createTRecord() is called
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public synchronized boolean transferRecord(String manager_id, String record_id, String servername) {
        int port = 0;
        Record record_to_be_transfered;
        if (servername.equals("MTL"))
            port = 5434;
        else if (servername.equals("LVL"))
            port = 5439;
        else if (servername.equals("DDO"))
            port = 5436;


        for (ArrayList<Record> recordlist : this.map.values()) {
            Iterator<Record> iterator = recordlist.iterator();
            while (iterator.hasNext()) {
                Record record = iterator.next();
                if (record_id.equals(record.record_id)) {    //record with particular recrd id is searched
                    synchronized (record) {
                        try {
                            record_to_be_transfered = record;

                            String y = record_to_be_transfered.toString();            //record converted into string
                            byte array[] = y.getBytes();                                //String is converted into byte array
                            InetAddress host = InetAddress.getLocalHost();
                            ds = new DatagramSocket();
                            DatagramPacket data_packet_request = new DatagramPacket(array, array.length, host, port);//first packet is sent to first serve(port no1)
                            this.ds.send(data_packet_request);                        //Data is sent to remote server

                            iterator.remove();                                        //record is removed from hashmap

                            synchronized (lockCount) {
                                if (record_id.contains("SR"))                        //record count is decremented
                                    student_count--;
                                else
                                    teacher_count--;
                            }

                        } catch (Exception e) {
                        }
                    }


                }


            }

        }
        return success_flag;
    }

    private String transferTRecord(String record_id, String f_name, String l_name, String addr, String number, String spec, String loc) {        //creates the teacher record
        synchronized (lockID) {
            ++teacher_count;

            Record r = new Record(record_id, f_name, l_name, addr, number, spec, loc);

            char first = l_name.charAt(0);
            insert(r, first);
            success_flag = true;
        }         //calls insert method to insert the new record	success_flag=true;
//        logger.info("Teacher record created by " + manager_id);
        return record_id;
    }

    private String transferSRecord(String record_id, String f_name, String l_name, String courses, String status) {  //creates student record
        synchronized (lockID) {
            ++student_count;

            //System.out.println("student count"+student_count);

            String dat = new Date(System.currentTimeMillis()).toString();
            Record r = new Record(record_id, f_name, l_name, courses, status, dat);
            char first = l_name.charAt(0);

            insert(r, first);
            success_flag = true;
        }
//        logger.info("Student record created by " + manager_id);
        return record_id;
    }

}







