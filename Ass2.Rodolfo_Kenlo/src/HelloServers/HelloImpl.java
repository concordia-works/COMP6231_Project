package HelloServers;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.omg.CORBA.ORB;
import HelloApp.HelloPOA;
import Ressources.Record;
import Ressources.StudentRecord;
import Ressources.TeacherRecord;

import HelloApp.*;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;

import java.util.Properties;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class HelloImpl extends HelloPOA {
    private static HashMap<String, ArrayList<Record>> hmap = new HashMap<String, ArrayList<Record>>();
    private ORB orb;
    String serverName;
    Logger serverLog;
    static Record obj;
    private static int idGeneratingMTL = 0;
    private static int idGeneratingLVL = 1;
    private static int idGeneratingDDO = 2;

    public HelloImpl(String serverName) {
        super();
        this.serverName = serverName;
        serverLog = initializeLog();
    }

    public void setORB(ORB orb_val) {
        orb = orb_val;
    }

    public synchronized String createTRecord(String managerID, String firstName, String lastName, String address,
                                             String phone, String[] spec, String location) {

        ArrayList<String> specialization = new ArrayList<String>();
        String storageIndex;
        specialization = fixArrayT(spec);

        // Instantiate a teacher record object
        TeacherRecord teacherRecord = new TeacherRecord(firstName, lastName, address, phone, specialization, location);
        switch(serverName){
            case "MTL":
                teacherRecord.setRecordID(genTeacherID(idGeneratingMTL));
                idGeneratingMTL = idGeneratingMTL + 3;
                break;
            case "LVL":
                teacherRecord.setRecordID(genTeacherID(idGeneratingLVL));
                idGeneratingLVL = idGeneratingLVL + 3;
                break;
            case "DDO":
                teacherRecord.setRecordID(genTeacherID(idGeneratingDDO));
                idGeneratingDDO = idGeneratingDDO + 3;
            default:
                break;
        }
        // get teacher's last name first letter
        storageIndex = getFirstLetter(teacherRecord.getLastName());
        // insert the record object in the hashmap according to its first letter
        insertHashmap(storageIndex, teacherRecord);
        // for testing purpose
        readHashMap(storageIndex);

        // Write in the ServerLog
        String recordID = teacherRecord.getRecordID();
        String operation = " created the teacher record ";
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String msg = timestamp + " | " + managerID + operation + recordID + " on the " + serverName + " Server ";
        String id = recordID;
        serverLog.info(msg);
        clientLog(managerID, msg);
        System.out.println("Teacher Record " + recordID + " was properly created & logged.");

        return id;
    }

    public synchronized String createSRecord(String managerID, String firstName, String lastName, String[] cr, String status,
                                             String statusDate) {

        ArrayList<String> courses = new ArrayList<String>();
        String storageIndex;

        courses = fixArrayS(cr);

        // Instantiate a teacher record object
        StudentRecord studentRecord = new StudentRecord(firstName, lastName, courses, status, statusDate);
        switch(serverName){
            case "MTL":
                studentRecord.setRecordID(genStudentID(idGeneratingMTL));
                idGeneratingMTL = idGeneratingMTL + 3;
                break;
            case "LVL":
                studentRecord.setRecordID(genStudentID(idGeneratingLVL));
                idGeneratingLVL = idGeneratingLVL + 3;
                break;
            case "DDO":
                studentRecord.setRecordID(genStudentID(idGeneratingDDO));
                idGeneratingDDO = idGeneratingDDO + 3;
            default:
                break;
        }
        // get teacher's last name first letter
        storageIndex = getFirstLetter(studentRecord.getLastName());
        // insert the record object in the hashmap according to its first letter
        insertHashmap(storageIndex, studentRecord);

        // Write in the ServerLog
        String recordID = studentRecord.getRecordID();
        String operation = " created the student record ";
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String msg = timestamp + " | " + managerID + operation + recordID + " on the " + serverName + " Server ";
        String id = recordID;
        serverLog.info(msg);
        clientLog(managerID, msg);
        //Server Console
        System.out.println("Student Record " + recordID + " was properly created & logged.");

        // for testing purpose
        readHashMap(storageIndex);

        return id;
    }

    public String getRecordCounts(String managerID) {
        String countResult = "";
        String mtlCount = "";
        String ddoCount = "";
        String lvlCount = "";
        String operation = "count";
        String msg = "";
        // serverName = managerID.substring(0, 3).toUpperCase();
        switch (serverName) {
            case "MTL":
                // know my own count
                int mtlRecordCount = 0;
                mtlRecordCount = getMyHashMapCount();
                ddoCount = clientUDP(6787, operation);// ddo
                lvlCount = clientUDP(6788, operation);// lvl
                countResult = ("MTL: " + mtlRecordCount + " DDO: " + ddoCount + " LVL: " + lvlCount);
                // Write in the ServerLog
                Timestamp timestampMTL = new Timestamp(System.currentTimeMillis());
                msg = timestampMTL + " | " + managerID + " requested a Record Count from the " + serverName + " Server: " + countResult;
                serverLog.info(msg);
                clientLog(managerID, msg);
                //Server Console
                System.out.println(countResult);
                break;
            case "LVL":
                int lvlRecordCount = 0;
                lvlRecordCount = getMyHashMapCount();
                ddoCount = clientUDP(6787, operation);// ddo
                mtlCount = clientUDP(6789, operation);// mtl
                countResult = ("LVL: " + lvlRecordCount + " DDO: " + ddoCount + " MTL: " + mtlCount);
                // Write in the ServerLog
                Timestamp timestampLVL = new Timestamp(System.currentTimeMillis());
                msg = timestampLVL + " | " + managerID + " requested a Record Count from the " + serverName + " Server: " + countResult;
                serverLog.info(msg);
                clientLog(managerID, msg);
                //Server Console
                System.out.println(countResult);
                break;
            case "DDO":
                int ddoRecordCount = 0;
                ddoRecordCount = getMyHashMapCount();
                lvlCount = clientUDP(6788, operation);// lvl
                mtlCount = clientUDP(6789, operation);// mtl
                countResult = ("DDO: " + ddoRecordCount + " LVL: " + lvlCount + " MTL: " + mtlCount);
                // Write in the ServerLog
                Timestamp timestampDDO = new Timestamp(System.currentTimeMillis());
                msg = timestampDDO + " | " + managerID + " requested a Record Count from the " + serverName + " Server: " + countResult;
                serverLog.info(msg);
                clientLog(managerID, msg);
                //Server Console
                System.out.println(countResult);
                break;
        }
        return msg;
    }

    public synchronized boolean editRecord(String managerID, String recordID, String fieldName, String newValue) {
        String logMsg = "";
        boolean confirmationMsg = false;
        fieldName = fieldName.toUpperCase();
        for (String key : hmap.keySet()) {
            ArrayList<Record> tempAL = hmap.get(key);
            // iterating for each record in the array list
            for (int i = 0; i < tempAL.size(); i++) {
                Record tempRec = tempAL.get(i);
                if (tempRec instanceof StudentRecord) {
                    String sID = ((StudentRecord) tempRec).getRecordID();
                    if (sID.equals(recordID)) {
                        switch (fieldName) {
                            case "COURSE REGISTERED":
                                ArrayList<String> courses = new ArrayList<String>();
                                courses = ((StudentRecord) tempRec).getCourseRegistered();
                                String strCourseList = "";
                                for (int j = 0; j < courses.size(); j++) {
                                    strCourseList += courses.get(j).toString() + " ";
                                }
                                System.out.println("The registered courses were: " + strCourseList);
                                courses.add(newValue);
                                ((StudentRecord) tempRec).setCourseRegistered(courses);
                                String newCourseList = "";
                                for (int j = 0; j < courses.size(); j++) {
                                    newCourseList += courses.get(j).toString() + " ";
                                }
                                System.out.println("The registered courses are now: " + newCourseList);
                                // Write in the ServerLog
                                Timestamp timestampCourse = new Timestamp(System.currentTimeMillis());
                                logMsg = timestampCourse + " | " + managerID + " edited the Registered Courses of " + recordID + " from the " + serverName + " Server";
                                serverLog.info(logMsg);
                                clientLog(managerID, logMsg);
                                confirmationMsg = true;
                                break;
                            case "STATUS":
                                String strPreviousStat = ((StudentRecord) tempRec).getStatus();
                                System.out.println("The previous status was: " + strPreviousStat);
                                ((StudentRecord) tempRec).setStatus(newValue);
                                Date date = new Date();
                                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                                String today = formatter.format(date).toString();
                                ((StudentRecord) tempRec).setStatusDate(today);
                                String newStat = ((StudentRecord) tempRec).getStatus();
                                System.out.println("The status of Student: " + sID + " was successfully changed to: "
                                        + newStat + " on " + today);
                                // Write in the ServerLog
                                Timestamp timestampStatus = new Timestamp(System.currentTimeMillis());
                                logMsg = timestampStatus + " | " + managerID + " edited the Status of " + recordID + " from the " + serverName + " Server";
                                serverLog.info(logMsg);
                                clientLog(managerID, logMsg);
                                confirmationMsg = true;
                                break;
                            case "STATUS DATE":
                                String strPreviousDate = ((StudentRecord) tempRec).getStatusDate();
                                System.out.println("The previous status date was: " + strPreviousDate);
                                ((StudentRecord) tempRec).setStatusDate(newValue);
                                String strNewDate = ((StudentRecord) tempRec).getStatusDate();
                                System.out.println("The new status date is: " + strNewDate);
                                // Write in the ServerLog
                                Timestamp timestampStatusDate = new Timestamp(System.currentTimeMillis());
                                logMsg = timestampStatusDate + " | " + managerID + " edited the Status Date of " + recordID + " from the " + serverName + " Server";
                                serverLog.info(logMsg);
                                clientLog(managerID, logMsg);
                                confirmationMsg = true;
                                break;
                            default:
                                confirmationMsg = false;
                                break;
                        }
                    }
                }
                if (tempRec instanceof TeacherRecord) {
                    String tID = ((TeacherRecord) tempRec).getRecordID();
                    if (tID.equals(recordID)) {
                        switch (fieldName) {
                            case "ADDRESS":
                                String oldAddress = ((TeacherRecord) tempRec).getAdress();
                                System.out.println("The previous address was: " + oldAddress);
                                ((TeacherRecord) tempRec).setAdress(newValue);
                                String newAddress = ((TeacherRecord) tempRec).getAdress();
                                System.out.println("The new address is: " + newAddress);
                                // Write in the ServerLog
                                Timestamp timestampAddress = new Timestamp(System.currentTimeMillis());
                                logMsg = timestampAddress + " | " + managerID + " edited the Address of " + recordID + " from the " + serverName + " Server";
                                serverLog.info(logMsg);
                                clientLog(managerID, logMsg);
                                confirmationMsg = true;
                                break;
                            case "PHONE":
                                String oldPhone = ((TeacherRecord) tempRec).getPhone();
                                System.out.println("The previous phone number was: " + oldPhone);
                                ((TeacherRecord) tempRec).setPhone(newValue);
                                String newPhone = ((TeacherRecord) tempRec).getPhone();
                                System.out.println("The new phone number is: " + newPhone);
                                // Write in the ServerLog
                                Timestamp timestampPhone = new Timestamp(System.currentTimeMillis());
                                logMsg = timestampPhone + " | " + managerID + " edited the Phone Number of " + recordID + " from the " + serverName + " Server";
                                serverLog.info(logMsg);
                                clientLog(managerID, logMsg);
                                confirmationMsg = true;
                                break;
                            case "LOCATION":
                                String oldLocation = ((TeacherRecord) tempRec).getLocation();
                                System.out.println("The previous location was: " + oldLocation);
                                ((TeacherRecord) tempRec).setLocation(newValue);
                                String newLocation = ((TeacherRecord) tempRec).getLocation();
                                System.out.println("The new location is: " + newLocation);
                                // Write in the ServerLog
                                Timestamp timestampLocation = new Timestamp(System.currentTimeMillis());
                                logMsg = timestampLocation + " | " + managerID + " edited the Phone Number of " + recordID + " from the " + serverName + " Server";
                                serverLog.info(logMsg);
                                clientLog(managerID, logMsg);
                                confirmationMsg = true;
                                break;
                            default:
                                confirmationMsg = false;
                                break;
                        }
                    }

                }

            }

        }

        return confirmationMsg;
    }

    public synchronized boolean transferRecord(String managerID, String recordID, String remoteCenterServerName) {
        String logMsg;
        String storageIndex = "";
        boolean confirmationMsg = false; // DEFAULT
        String clientMsg;
        String operation = "transfer" + managerID;
        Timestamp timestamp;
        for (String key : hmap.keySet()) {
            ArrayList<Record> tempAL = hmap.get(key); //GET THE ARRAYLAST AT LETTER
            // iterating for each record in the array list
            for (int i = 0; i < tempAL.size(); i++) {
                Record tempRec = tempAL.get(i);
                if (tempRec instanceof StudentRecord) {
                    String sID = ((StudentRecord) tempRec).getRecordID();
                    if (sID.equals(recordID)) {
                        obj = ((StudentRecord) tempRec);
                        // get student last name first letter
                        storageIndex = getFirstLetter(obj.getLastName());
                        switch (remoteCenterServerName) {
                            case "MTL":
                                clientMsg = clientUDP(6789, operation);// mtl
                                if (!clientMsg.equals("notFilled")) {
                                    confirmationMsg = true;
                                }
                                tempAL.remove(tempRec);
                                // Write in the ServerLog
                                timestamp = new Timestamp(System.currentTimeMillis());
                                logMsg = timestamp + " | " + managerID + " transferred the Record " + recordID + " from the " + serverName + " Server"
                                        + " to the " + remoteCenterServerName + " Server";
                                serverLog.info(logMsg);
                                clientLog(managerID, logMsg);
                                //Changed because of the project architeture.
                                //confirmationMsg = logMsg;
                                break;
                            case "LVL":
                                clientMsg = clientUDP(6788, operation);// lvl
                                if (!clientMsg.equals("notFilled")) {
                                    confirmationMsg = true;
                                }
                                tempAL.remove(tempRec);
                                // Write in the ServerLog
                                timestamp = new Timestamp(System.currentTimeMillis());
                                logMsg = timestamp + " | " + managerID + " transferred the Record " + recordID + " from the " + serverName + " Server"
                                        + " to the " + remoteCenterServerName + " Server";
                                serverLog.info(logMsg);
                                clientLog(managerID, logMsg);
                                //Changed because of the project architeture.
                                //confirmationMsg = logMsg;
                                break;
                            case "DDO":
                                clientMsg = clientUDP(6787, operation);// ddo
                                if (!clientMsg.equals("notFilled")) {
                                    confirmationMsg = true;
                                }
                                tempAL.remove(tempRec);
                                // Write in the ServerLog
                                timestamp = new Timestamp(System.currentTimeMillis());
                                logMsg = timestamp + " | " + managerID + " transferred the Record " + recordID + " from the " + serverName + " Server"
                                        + " to the " + remoteCenterServerName + " Server";
                                serverLog.info(logMsg);
                                clientLog(managerID, logMsg);
                                //Changed because of the project architeture.
                                //confirmationMsg = logMsg;
                                break;
                        }

                    }
                } else if (tempRec instanceof TeacherRecord) {
                    String tID = ((TeacherRecord) tempRec).getRecordID();
                    if (tID.equals(recordID)) {
                        obj = ((TeacherRecord) tempRec);
                        // get teacher last name first letter
                        storageIndex = getFirstLetter(obj.getLastName());
                        switch (remoteCenterServerName) {
                            case "MTL":
                                clientMsg = clientUDP(6789, operation);// mtl
                                if (!clientMsg.equals("notFilled")) {
                                    confirmationMsg = true;
                                }
                                tempAL.remove(tempRec);
                                // Write in the ServerLog
                                timestamp = new Timestamp(System.currentTimeMillis());
                                logMsg = timestamp + " | " + managerID + " transferred the Record " + recordID + " from the " + serverName + " Server"
                                        + " to the " + remoteCenterServerName + " Server";
                                serverLog.info(logMsg);
                                clientLog(managerID, logMsg);
                                //Changed because of the project architeture.
                                //confirmationMsg = logMsg;
                                break;
                            case "LVL":
                                clientMsg = clientUDP(6788, operation);// lvl
                                if (!clientMsg.equals("notFilled")) {
                                    confirmationMsg = true;
                                }
                                tempAL.remove(tempRec);
                                // Write in the ServerLog
                                timestamp = new Timestamp(System.currentTimeMillis());
                                logMsg = timestamp + " | " + managerID + " transferred the Record " + recordID + " from the " + serverName + " Server"
                                        + " to the " + remoteCenterServerName + " Server";
                                serverLog.info(logMsg);
                                clientLog(managerID, logMsg);
                                //Changed because of the project architeture.
                                //confirmationMsg = logMsg;
                                break;
                            case "DDO":
                                clientMsg = clientUDP(6787, operation);// ddo
                                if (!clientMsg.equals("notFilled")) {
                                    confirmationMsg = true;
                                }
                                tempAL.remove(tempRec);
                                // Write in the ServerLog
                                timestamp = new Timestamp(System.currentTimeMillis());
                                logMsg = timestamp + " | " + managerID + " transferred the Record " + recordID + " from the " + serverName + " Server"
                                        + " to the " + remoteCenterServerName + " Server";
                                serverLog.info(logMsg);
                                clientLog(managerID, logMsg);
                                //Changed because of the project architeture.
                                //confirmationMsg = logMsg;
                                break;
                        }

                    }
                }
            }
        }
        if (storageIndex != "") {
            readHashMap(storageIndex);

        }
        return confirmationMsg;
    }

    private Logger initializeLog() {
        Logger logger = Logger.getLogger(serverName);
        logger.setLevel(Level.FINE);
        FileHandler fh;
        try {

            // This block configure the logger with handler and formatter
            fh = new FileHandler(serverName + ".log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);

            // the following statement is used to log any messages
            //logger.info("My first log");

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return logger;
    }

    public void readHashMap(String index) {
        int itemCount = hmap.get(index).size();
        System.out.println("Array List, in the HashMap at index " + index + " contains: " + itemCount + " items.");
        Record[] recArray = new Record[itemCount];
        String tmpmsg = "";

        String recordId = "";
        String fname = "";
        String lname = "";
        for (int i = 0; i < itemCount; i++) {
            recArray[i] = hmap.get(index).get(i);
            if (recArray[i].getRecordType() == Record.RECORD_TYPE.STUDENT) {
                StudentRecord studentRecord = (StudentRecord) recArray[i];
                recordId = studentRecord.getRecordID();
                fname = studentRecord.getFirstName();
                lname = studentRecord.getLastName();

            } else if (recArray[i].getRecordType() == Record.RECORD_TYPE.TEACHER) {
                TeacherRecord teacherRecord = (TeacherRecord) recArray[i];
                recordId = teacherRecord.getRecordID();
                fname = teacherRecord.getFirstName();
                lname = teacherRecord.getLastName();
            }
            tmpmsg += "\n" + recordId + " " + fname + " " + lname;
        }
        System.out.println("Records at index " + index + " are : " + tmpmsg);

    }

    public void insertHashmap(String index, Record recordObj) {
        if (hmap.get(index) == null) {
            hmap.put(index, new ArrayList<Record>());
            hmap.get(index).add(recordObj);
        } else {
            hmap.get(index).add(recordObj);
        }
    }

    public String getFirstLetter(String lastName) {

        return lastName.substring(0, 1).toUpperCase();
    }

    public static String genTeacherID(Integer intCount) {
        String strCount;
        String strID = "";
        strCount = intCount.toString();
        switch (strCount.length()) {
            case 1:
                strCount = "0000" + strCount;
                strID = "TR" + strCount;
                break;
            case 2:
                strCount = "000" + strCount;
                strID = "TR" + strCount;
                break;
            case 3:
                strCount = "00" + strCount;
                strID = "TR" + strCount;
                break;
            case 4:
                strCount = "0" + strCount;
                strID = "TR" + strCount;
                break;
            case 5:
                strID = "TR" + strCount;
                break;
            default:
                System.out.println("Error");
                break;
        }
        return strID;
        // System.out.println(id);
    }

    public static String genStudentID(Integer intCount) {
        String strCount;
        String strID = "";
        strCount = intCount.toString();
        switch (strCount.length()) {
            case 1:
                strCount = "0000" + strCount;
                strID = "SR" + strCount;
                break;
            case 2:
                strCount = "000" + strCount;
                strID = "SR" + strCount;
                break;
            case 3:
                strCount = "00" + strCount;
                strID = "SR" + strCount;
                break;
            case 4:
                strCount = "0" + strCount;
                strID = "SR" + strCount;
                break;
            case 5:
                strID = "SR" + strCount;
                break;
            default:
                System.out.println("Error");
                break;
        }
        return strID;
        // System.out.println(id);
    }

    public static ArrayList<String> fixArrayT(String[] spec) {
        ArrayList<String> specialization = new ArrayList<String>();
        int count = 0;
        for (int x = 0; x < specialization.size(); x++) {
            if (specialization.get(x) != null) {
                count++;
            }
        }
        for (int i = 0; i < spec.length; i++) {
            specialization.add(spec[i]);
        }
        return specialization;
    }

    public static ArrayList<String> fixArrayS(String[] cr) {
        ArrayList<String> courses = new ArrayList<String>();
        int count = 0;
        for (int x = 0; x < courses.size(); x++) {
            if (courses.get(x) != null) {
                count++;
            }
        }
        for (int i = 0; i < cr.length; i++) {
            courses.add(cr[i]);
        }
        return courses;
    }

    public static Integer getMyHashMapCount() {
        Integer alCountSum = 0;

        for (String key : hmap.keySet()) {
            ArrayList<Record> tempAL = hmap.get(key);
            Integer tempCount = tempAL.size();
            alCountSum += tempCount;
        }
        return alCountSum;
    }

    public String clientUDP(int port, String operation) {
        String returnMsg = "notFilled";
        DatagramSocket aSocket = null;
        byte[] m = null;
        try {
            aSocket = new DatagramSocket();
            if (operation.equals("count")) {
                m = operation.getBytes();
                InetAddress aHost = InetAddress.getByName("localhost");
                int serverport = port;
                DatagramPacket request = new DatagramPacket(m, m.length, aHost, serverport);
                aSocket.send(request);
            } else if (operation.substring(0, 8).equals("transfer")) {
                m = concatenate(obj, operation.substring(8, 15)).getBytes();
                InetAddress aHost = InetAddress.getByName("localhost");
                int serverport = port;
                DatagramPacket request = new DatagramPacket(m, m.length, aHost, serverport);
                aSocket.send(request);
            }
            // answer
            byte[] buffer = new byte[100];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            aSocket.receive(reply);
            System.out.println("clientUDP received: " + new String(reply.getData()));
            returnMsg = new String(reply.getData());

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }
        return returnMsg;
    }

    public void serverUDP(int port) throws Exception {
        DatagramSocket aSocket = null;
        String strHMC = null;
        String[] spec = null;
        byte[] hmc = null;
        try {
            aSocket = new DatagramSocket(port);

            while (true) {
                byte buffer[] = new byte[1000];
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);
                if (new String(request.getData()).trim().equals("count")) {
                    Integer hmCount = getMyHashMapCount();
                    strHMC = hmCount.toString();
                    hmc = strHMC.getBytes();
                } else {
                    String requestContent = new String(request.getData());
                    requestContent = requestContent.trim();
                    System.out.println(requestContent);
                    String[] contentComponents = requestContent.split("\\|", -1);
                    if (contentComponents[contentComponents.length - 1].equals("LVL")
                            || contentComponents[contentComponents.length - 1].equals("DDO")
                            || contentComponents[contentComponents.length - 1].equals("MTL")) {
                        if (contentComponents[5].contains(",")) {
                            spec = contentComponents[5].split("\\,", -1);
                        } else
                            spec = contentComponents[5].split("");
                        spec = fixArray(spec);
                        insertTransferedSRecord(contentComponents[0], contentComponents[1], contentComponents[2], spec,
                                contentComponents[4], contentComponents[5], contentComponents[6]);
                        strHMC = "Object Transfered";
                        hmc = strHMC.getBytes();
                    } else {
                        spec = contentComponents[3].split("\\,", -1);
                        spec = fixArray(spec);
                        insertTransferedTRecord(contentComponents[0], contentComponents[1], contentComponents[2],
                                contentComponents[3], contentComponents[4], spec, contentComponents[6], contentComponents[7]);
                        strHMC = "Object Transfered";
                        hmc = strHMC.getBytes();
                    }

                }
                // construct the answer in
                DatagramPacket reply = new DatagramPacket(hmc, strHMC.length(), request.getAddress(),
                        request.getPort());
                aSocket.send(reply);
            }
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }
    }

    public static String concatenate(Record obj, String managerID) {
        String inf = "";

        if (obj.getRecordType() == Record.RECORD_TYPE.STUDENT) {
            StudentRecord studentRecord = (StudentRecord) obj;
            inf = managerID + "|" + studentRecord.getFirstName() + "|" + studentRecord.getLastName() + "|"
                    + studentRecord.getCourseRegistered() + "|" + studentRecord.getStatus() + "|"
                    + studentRecord.getStatusDate() + "|" + studentRecord.getRecordID();

        } else if (obj.getRecordType() == Record.RECORD_TYPE.TEACHER) {
            TeacherRecord teacherRecord = (TeacherRecord) obj;
            inf = managerID + "|" + teacherRecord.getFirstName() + "|" + teacherRecord.getLastName() + "|"
                    + teacherRecord.getAdress() + "|" + teacherRecord.getPhone() + "|"
                    + teacherRecord.getSpecalization() + "|" + teacherRecord.getLocation() + "|" + teacherRecord.getRecordID();
        }
        return inf;
    }

    public static String[] fixArray(String[] spec) {
        // [ A ]
        spec[0] = spec[0].replace("[", "");
        spec[spec.length - 1] = spec[spec.length - 1].replace("]", "");
        return spec;
    }

    // implement shutdown() method
    public void shutdown() {
        orb.shutdown(false);
    }

    public synchronized void clientLog(String managerID, String logMessage) {
        String savestr = managerID.toUpperCase() + "Log.txt";
        File f = new File(savestr);

        PrintWriter out = null;
        if (f.exists() && !f.isDirectory()) {
            try {
                out = new PrintWriter(new FileOutputStream(new File(savestr), true));
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            out.append(logMessage + "\n");
            out.close();
        } else {
            try {
                out = new PrintWriter(savestr);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            out.println(logMessage);
            out.close();
        }

    }

    public void insertTransferedTRecord(String managerID, String firstName, String lastName, String address,
                                        String phone, String[] spec, String location, String recordID) {

        ArrayList<String> specialization = new ArrayList<String>();
        String storageIndex;
        specialization = fixArrayT(spec);

        // Instantiate a teacher record object
        TeacherRecord teacherRecord = new TeacherRecord(firstName, lastName, address, phone, specialization, location);
        teacherRecord.setRecordID(recordID);
        // get teacher's last name first letter
        storageIndex = getFirstLetter(teacherRecord.getLastName());
        // insert the record object in the hashmap according to its first letter
        insertHashmap(storageIndex, teacherRecord);
        // for testing purpose
        readHashMap(storageIndex);

        // Write in the ServerLog
        String operation = " created the teacher record ";
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String msg = timestamp + " | " + managerID + operation + recordID + " on the " + serverName + " Server ";
        //String id = recordID;
        serverLog.info(msg);
        clientLog(managerID, msg);
        System.out.println("Teacher Record " + recordID + " was properly created & logged.");

        //return id;

    }

    public void insertTransferedSRecord(String managerID, String firstName, String lastName, String[] cr, String status,
                                        String statusDate, String recordID) {

        ArrayList<String> courses = new ArrayList<String>();
        String storageIndex;

        courses = fixArrayS(cr);

        // Instantiate a teacher record object
        StudentRecord studentRecord = new StudentRecord(firstName, lastName, courses, status, statusDate);
        studentRecord.setRecordID(recordID);
        // get teacher's last name first letter
        storageIndex = getFirstLetter(studentRecord.getLastName());
        // insert the record object in the hashmap according to its first letter
        insertHashmap(storageIndex, studentRecord);

        // Write in the ServerLog
        String operation = " created the student record ";
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String msg = timestamp + " | " + managerID + operation + recordID + " on the " + serverName + " Server ";
        //String id = recordID;
        serverLog.info(msg);
        clientLog(managerID, msg);
        //Server Console
        System.out.println("Student Record " + recordID + " was properly created & logged.");

        // for testing purpose
        readHashMap(storageIndex);

        //return id;

    }
}
