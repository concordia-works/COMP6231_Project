package Client;

import Servers.Record;
import Servers.StudentRecord;
import Servers.TeacherRecord;
import Utils.Config;
import org.omg.CORBA.DCMS;
import org.omg.CORBA.DCMSHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by quocminhvu on 2017-05-19.
 */

public class ManagerClient {
    private Logger LOGGER;
    private String managerID;
    private static Scanner sc;
    private static ORB orb;
    private static org.omg.CORBA.Object namingContextObj;
    private static NamingContextExt namingContextRef;

    protected ManagerClient(String managerID) throws IOException {
        this.managerID = managerID;
    }

    private static void prepareORB(String args[]) throws Exception {
        // Initiate client ORB
        orb = ORB.init(args, null);

        // Get object reference to the Naming Service
        namingContextObj = orb.resolve_initial_references(Config.CORBA.NAME_SERVICE);

        // Narrow the NamingContext object reference to the proper type to be usable (like any CORBA object)
        namingContextRef = NamingContextExtHelper.narrow(namingContextObj);
    }

    public static void main(String args[]) {
        try {
            sc = new Scanner(System.in);
            prepareORB(args);

            int input;
            do {
                System.out.print(Config.MAIN_MENU);
                input = Integer.parseInt(sc.nextLine());

                switch (input) {
                    case 1:
                        singleThreadUI();
                        break;
                    case 2:
                        multiThreadUI();
                        break;
                    default:
                        break;
                }
            } while (input != 0);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    private static void singleThreadUI() throws Exception {
        System.out.print("Enter Manager ID: ");
        String managerID = sc.nextLine().toUpperCase();
        Config.Server_ID serverID = Config.Server_ID.valueOf(managerID.substring(0, 3));
        ManagerClient client = new ManagerClient(managerID);
        client.initiateLogger();

        // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
        DCMS dcmsServer = DCMSHelper.narrow(namingContextRef.resolve_str(serverID.name()));
        System.out.println(managerID + " connect to server " + serverID.name() + " successfully");

        int input;
        do {
            System.out.print(Config.SINGLE_THREAD_MENU);
            input = Integer.parseInt(sc.nextLine());
            switch (input) {
                case 1:
                    client.createStudentRecord(dcmsServer);
                    break;
                case 2:
                    client.createTeacherRecord(dcmsServer);
                    break;
                case 3:
                    client.editRecord(dcmsServer);
                    break;
                case 4:
                    client.getRecordCount(dcmsServer);
                    break;
                case 5:
                    client.transferRecord(dcmsServer);
                    break;
                case 6:
                    client.printRecord(dcmsServer);
                    break;
                case 7:
                    System.out.println(dcmsServer.printAllRecords());
                    break;
                case 8:
                    System.out.print("Enter Manager ID: ");
                    managerID = sc.nextLine().toUpperCase();
                    serverID = Config.Server_ID.valueOf(managerID.substring(0, 3));
                    client = new ManagerClient(managerID);
                    client.initiateLogger();

                    // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                    dcmsServer = DCMSHelper.narrow(namingContextRef.resolve_str(serverID.name()));
                    System.out.println(managerID + " connect to server " + serverID.name() + " successfully");
                    break;
                default:
                    break;
            }
        } while (input != 0);


    }

    private static void multiThreadUI() throws Exception {
        int input;
        do {
            System.out.print(Config.MULTI_THREAD_MENU);
            input = Integer.parseInt(sc.nextLine());

            switch (input) {
                case 1:
                    multiThread01();
                    break;
                case 2:
                    System.out.print("Enter a Student ID: ");
                    String studentID = sc.nextLine().toUpperCase();
                    System.out.print("Enter a Teacher ID: ");
                    String teacherID = sc.nextLine().toUpperCase();
                    multiThread02(studentID, teacherID);
                    break;
                default:
                    break;
            }
        } while (input != 0);
    }

    private static void multiThread01() {
        /**
         * Multi-threads create new records concurrently
         */
        // 1
        new Thread(() -> {
            try {
                String managerID = "MTL2111";
                Config.Server_ID serverID = Config.Server_ID.valueOf(managerID.substring(0, 3).toUpperCase());
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                DCMS dcmsServer = DCMSHelper.narrow(namingContextRef.resolve_str(serverID.name()));
                client.writeLog(managerID + " connect to server " + serverID.name() + " successfully");

                String recordID = dcmsServer.createSRecord(managerID, "Quoc Minh", "Vu", "distributed system", "Active");
                client.writeLog(recordID + " is created");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CannotProceed cannotProceed) {
                cannotProceed.printStackTrace();
            } catch (InvalidName invalidName) {
                invalidName.printStackTrace();
            } catch (NotFound notFound) {
                notFound.printStackTrace();
            }
        }).start();
        // 2
        new Thread(() -> {
            try {
                String managerID = "MTL2112";
                Config.Server_ID serverID = Config.Server_ID.valueOf(managerID.substring(0, 3).toUpperCase());
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                DCMS dcmsServer = DCMSHelper.narrow(namingContextRef.resolve_str(serverID.name()));
                client.writeLog(managerID + " connect to server " + serverID.name() + " successfully");

                String recordID = dcmsServer.createSRecord(managerID, "Duc Son", "Vo", "advanced programming", "Active");
                client.writeLog(recordID + " is created");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CannotProceed cannotProceed) {
                cannotProceed.printStackTrace();
            } catch (InvalidName invalidName) {
                invalidName.printStackTrace();
            } catch (NotFound notFound) {
                notFound.printStackTrace();
            }
        }).start();
        // 3
        new Thread(() -> {
            try {
                String managerID = "MTL2113";
                Config.Server_ID serverID = Config.Server_ID.valueOf(managerID.substring(0, 3).toUpperCase());
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                DCMS dcmsServer = DCMSHelper.narrow(namingContextRef.resolve_str(serverID.name()));
                client.writeLog(managerID + " connect to server " + serverID.name() + " successfully");

                String recordID = dcmsServer.createSRecord(managerID, "Duy Tung", "Viet", "human-computer interaction", "Active");
                client.writeLog(recordID + " is created");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CannotProceed cannotProceed) {
                cannotProceed.printStackTrace();
            } catch (InvalidName invalidName) {
                invalidName.printStackTrace();
            } catch (NotFound notFound) {
                notFound.printStackTrace();
            }
        }).start();
        // 4
        new Thread(() -> {
            try {
                String managerID = "MTL2114";
                Config.Server_ID serverID = Config.Server_ID.valueOf(managerID.substring(0, 3).toUpperCase());
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                DCMS dcmsServer = DCMSHelper.narrow(namingContextRef.resolve_str(serverID.name()));
                client.writeLog(managerID + " connect to server " + serverID.name() + " successfully");

                String recordID = dcmsServer.createSRecord(managerID, "Phi Son", "Van Mai", "social and information network", "Inactive");
                client.writeLog(recordID + " is created");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CannotProceed cannotProceed) {
                cannotProceed.printStackTrace();
            } catch (InvalidName invalidName) {
                invalidName.printStackTrace();
            } catch (NotFound notFound) {
                notFound.printStackTrace();
            }
        }).start();
        // 5
        new Thread(() -> {
            try {
                String managerID = "MTL2115";
                Config.Server_ID serverID = Config.Server_ID.valueOf(managerID.substring(0, 3).toUpperCase());
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                DCMS dcmsServer = DCMSHelper.narrow(namingContextRef.resolve_str(serverID.name()));
                client.writeLog(managerID + " connect to server " + serverID.name() + " successfully");

                String recordID = dcmsServer.createSRecord(managerID, "Duc Minh", "Vuong", "advanced programming practice", "Inactive");
                client.writeLog(recordID + " is created");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CannotProceed cannotProceed) {
                cannotProceed.printStackTrace();
            } catch (InvalidName invalidName) {
                invalidName.printStackTrace();
            } catch (NotFound notFound) {
                notFound.printStackTrace();
            }
        }).start();
        // 6
        new Thread(() -> {
            try {
                String managerID = "MTL2116";
                Config.Server_ID serverID = Config.Server_ID.valueOf(managerID.substring(0, 3).toUpperCase());
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                DCMS dcmsServer = DCMSHelper.narrow(namingContextRef.resolve_str(serverID.name()));
                client.writeLog(managerID + " connect to server " + serverID.name() + " successfully");

                String recordID = dcmsServer.createTRecord(managerID, "Trieu Nguyen", "Van", "sherbrooke", "123 784 5678", "information technology", "MTL");
                client.writeLog(recordID + " is created");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CannotProceed cannotProceed) {
                cannotProceed.printStackTrace();
            } catch (InvalidName invalidName) {
                invalidName.printStackTrace();
            } catch (NotFound notFound) {
                notFound.printStackTrace();
            }
        }).start();
        // 7
        new Thread(() -> {
            try {
                String managerID = "MTL2117";
                Config.Server_ID serverID = Config.Server_ID.valueOf(managerID.substring(0, 3).toUpperCase());
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                DCMS dcmsServer = DCMSHelper.narrow(namingContextRef.resolve_str(serverID.name()));
                client.writeLog(managerID + " connect to server " + serverID.name() + " successfully");

                String recordID = dcmsServer.createTRecord(managerID, "Nguyen Gia", "Vo", "sir george williams", "432 964 1930", "architect", "MTL");
                client.writeLog(recordID + " is created");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CannotProceed cannotProceed) {
                cannotProceed.printStackTrace();
            } catch (InvalidName invalidName) {
                invalidName.printStackTrace();
            } catch (NotFound notFound) {
                notFound.printStackTrace();
            }
        }).start();
        // 8
        new Thread(() -> {
            try {
                String managerID = "MTL2118";
                Config.Server_ID serverID = Config.Server_ID.valueOf(managerID.substring(0, 3).toUpperCase());
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                DCMS dcmsServer = DCMSHelper.narrow(namingContextRef.resolve_str(serverID.name()));
                client.writeLog(managerID + " connect to server " + serverID.name() + " successfully");

                String recordID = dcmsServer.createTRecord(managerID, "Mai Huong", "Van", "cavendish", "849 282 4567", "drawing", "MTL");
                client.writeLog(recordID + " is created");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CannotProceed cannotProceed) {
                cannotProceed.printStackTrace();
            } catch (InvalidName invalidName) {
                invalidName.printStackTrace();
            } catch (NotFound notFound) {
                notFound.printStackTrace();
            }
        }).start();
        // 9
        new Thread(() -> {
            try {
                String managerID = "MTL2119";
                Config.Server_ID serverID = Config.Server_ID.valueOf(managerID.substring(0, 3).toUpperCase());
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                DCMS dcmsServer = DCMSHelper.narrow(namingContextRef.resolve_str(serverID.name()));
                client.writeLog(managerID + " connect to server " + serverID.name() + " successfully");

                String recordID = dcmsServer.createTRecord(managerID, "Nguyen", "Vova", "mont royal", "514 234 9705", "electric", "MTL");
                client.writeLog(recordID + " is created");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CannotProceed cannotProceed) {
                cannotProceed.printStackTrace();
            } catch (InvalidName invalidName) {
                invalidName.printStackTrace();
            } catch (NotFound notFound) {
                notFound.printStackTrace();
            }
        }).start();
        // 10
        new Thread(() -> {
            try {
                String managerID = "MTL2110";
                Config.Server_ID serverID = Config.Server_ID.valueOf(managerID.substring(0, 3).toUpperCase());
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                DCMS dcmsServer = DCMSHelper.narrow(namingContextRef.resolve_str(serverID.name()));
                client.writeLog(managerID + " connect to server " + serverID.name() + " successfully");

                String recordID = dcmsServer.createTRecord(managerID, "Nguyen Binh", "Vuon", "metcalfe", "125 765 3378", "chemistry", "MTL");
                client.writeLog(recordID + " is created");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CannotProceed cannotProceed) {
                cannotProceed.printStackTrace();
            } catch (InvalidName invalidName) {
                invalidName.printStackTrace();
            } catch (NotFound notFound) {
                notFound.printStackTrace();
            }
        }).start();
    }

    private static void multiThread02(String studentRecordID, String teacherRecordID) {
        /**
         * Multi-threads edit, print and transfer the same record concurrently
         */
        // 1
        new Thread(() -> {
            try {
                String managerID = "MTL3111";
                String recordID = studentRecordID;
                Config.Server_ID serverID = Config.Server_ID.valueOf(managerID.substring(0, 3).toUpperCase());
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                DCMS dcmsServer = DCMSHelper.narrow(namingContextRef.resolve_str(serverID.name()));
//                client.writeLog(managerID + " connect to server " + serverID.name() + " successfully");

                boolean isSuccess = dcmsServer.editRecord(managerID, recordID, "coursesRegistered", "edited courses registered");
                if (isSuccess)
                    client.writeLog(recordID + " coursesRegistered is edited");
                else
                    client.writeLog(recordID + " coursesRegistered is failed to edited");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CannotProceed cannotProceed) {
                cannotProceed.printStackTrace();
            } catch (InvalidName invalidName) {
                invalidName.printStackTrace();
            } catch (NotFound notFound) {
                notFound.printStackTrace();
            }
        }).start();
        // 2
        new Thread(() -> {
            try {
                String managerID = "MTL3113";
                String recordID = studentRecordID;
                Config.Server_ID serverID = Config.Server_ID.valueOf(managerID.substring(0, 3).toUpperCase());
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                DCMS dcmsServer = DCMSHelper.narrow(namingContextRef.resolve_str(serverID.name()));
//                client.writeLog(managerID + " connect to server " + serverID.name() + " successfully");

                boolean isSuccess = dcmsServer.transferRecord(managerID, recordID, "LVL");
                if (isSuccess)
                    client.writeLog(recordID + " transferred to LVL");
                else
                    client.writeLog(recordID + " failed to transfer to LVL ");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CannotProceed cannotProceed) {
                cannotProceed.printStackTrace();
            } catch (InvalidName invalidName) {
                invalidName.printStackTrace();
            } catch (NotFound notFound) {
                notFound.printStackTrace();
            }
        }).start();
        // 3
        new Thread(() -> {
            try {
                String managerID = "MTL3112";
                String recordID = studentRecordID;
                Config.Server_ID serverID = Config.Server_ID.valueOf(managerID.substring(0, 3).toUpperCase());
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                DCMS dcmsServer = DCMSHelper.narrow(namingContextRef.resolve_str(serverID.name()));
//                client.writeLog(managerID + " connect to server " + serverID.name() + " successfully");

                boolean isSuccess = dcmsServer.editRecord(managerID, recordID, "status", "edited status");
                if (isSuccess)
                    client.writeLog(recordID + " status is edited");
                else
                    client.writeLog(recordID + " status is failed to edited");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CannotProceed cannotProceed) {
                cannotProceed.printStackTrace();
            } catch (InvalidName invalidName) {
                invalidName.printStackTrace();
            } catch (NotFound notFound) {
                notFound.printStackTrace();
            }
        }).start();
        // 4
        new Thread(() -> {
            try {
                String managerID = "MTL3114";
                String recordID = teacherRecordID;
                Config.Server_ID serverID = Config.Server_ID.valueOf(managerID.substring(0, 3).toUpperCase());
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                DCMS dcmsServer = DCMSHelper.narrow(namingContextRef.resolve_str(serverID.name()));
//                client.writeLog(managerID + " connect to server " + serverID.name() + " successfully");

                boolean isSuccess = dcmsServer.editRecord(managerID, recordID, "address", "edited address");
                if (isSuccess)
                    client.writeLog(recordID + " address is edited");
                else
                    client.writeLog(recordID + " address is failed to edited");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CannotProceed cannotProceed) {
                cannotProceed.printStackTrace();
            } catch (InvalidName invalidName) {
                invalidName.printStackTrace();
            } catch (NotFound notFound) {
                notFound.printStackTrace();
            }
        }).start();
        // 5
        new Thread(() -> {
            try {
                String managerID = "MTL3117";
                String recordID = teacherRecordID;
                Config.Server_ID serverID = Config.Server_ID.valueOf(managerID.substring(0, 3).toUpperCase());
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                DCMS dcmsServer = DCMSHelper.narrow(namingContextRef.resolve_str(serverID.name()));
//                client.writeLog(managerID + " connect to server " + serverID.name() + " successfully");

                boolean isSuccess = dcmsServer.transferRecord(managerID, recordID, "DDO");
                if (isSuccess)
                    client.writeLog(recordID + " transferred to DDO");
                else
                    client.writeLog(recordID + " failed to transfer to DDO");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CannotProceed cannotProceed) {
                cannotProceed.printStackTrace();
            } catch (InvalidName invalidName) {
                invalidName.printStackTrace();
            } catch (NotFound notFound) {
                notFound.printStackTrace();
            }
        }).start();
        // 6
        new Thread(() -> {
            try {
                String managerID = "MTL3116";
                String recordID = teacherRecordID;
                Config.Server_ID serverID = Config.Server_ID.valueOf(managerID.substring(0, 3).toUpperCase());
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                DCMS dcmsServer = DCMSHelper.narrow(namingContextRef.resolve_str(serverID.name()));
//                client.writeLog(managerID + " connect to server " + serverID.name() + " successfully");

                boolean isSuccess = dcmsServer.editRecord(managerID, recordID, "location", "edited location");
                if (isSuccess)
                    client.writeLog(recordID + " location is edited");
                else
                    client.writeLog(recordID + " location is failed to edited");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CannotProceed cannotProceed) {
                cannotProceed.printStackTrace();
            } catch (InvalidName invalidName) {
                invalidName.printStackTrace();
            } catch (NotFound notFound) {
                notFound.printStackTrace();
            }
        }).start();
        // 7
        new Thread(() -> {
            try {
                String managerID = "MTL3115";
                String recordID = teacherRecordID;
                Config.Server_ID serverID = Config.Server_ID.valueOf(managerID.substring(0, 3).toUpperCase());
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                DCMS dcmsServer = DCMSHelper.narrow(namingContextRef.resolve_str(serverID.name()));
//                client.writeLog(managerID + " connect to server " + serverID.name() + " successfully");

                boolean isSuccess = dcmsServer.editRecord(managerID, recordID, "phone", "edited phone");
                if (isSuccess)
                    client.writeLog(recordID + " phone is edited");
                else
                    client.writeLog(recordID + " phone is failed to edited");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CannotProceed cannotProceed) {
                cannotProceed.printStackTrace();
            } catch (InvalidName invalidName) {
                invalidName.printStackTrace();
            } catch (NotFound notFound) {
                notFound.printStackTrace();
            }
        }).start();
    }

    private void createStudentRecord(DCMS dcmsServer) {
        System.out.print("Enter FirstName: ");
        String firstName = sc.nextLine();
        System.out.print("Enter LastName: ");
        String lastName = sc.nextLine();
        System.out.print("Enter Courses registered: ");
        String coursesRegistered = sc.nextLine();
        System.out.print("Enter Status: ");
        String status = sc.nextLine();
        String recordID = dcmsServer.createSRecord(managerID, firstName, lastName, coursesRegistered, status);
        LOGGER.info(recordID + " created");
        System.out.println(recordID + " created");
    }

    private void createTeacherRecord(DCMS dcmsServer) {
        System.out.print("Enter FirstName: ");
        String firstName = sc.nextLine();
        System.out.print("Enter LastName: ");
        String lastName = sc.nextLine();
        System.out.print("Enter Address: ");
        String address = sc.nextLine();
        System.out.print("Enter Phone: ");
        String phone = sc.nextLine();
        System.out.print("Enter Specialization: ");
        String specialization = sc.nextLine();
        System.out.print("Enter Location: ");
        String location = sc.nextLine();
        String recordID = dcmsServer.createTRecord(managerID, firstName, lastName, address, phone, specialization, location);
        LOGGER.info(recordID + " created");
        System.out.println(recordID + " created");
    }

    private void editRecord(DCMS dcmsServer) {
        int input;
        System.out.print("Enter Record ID: ");
        String recordID = sc.nextLine().toUpperCase();

        String fieldName;
        String recordType = dcmsServer.getRecordType(recordID);
        if (recordType.compareTo(Record.Record_Type.STUDENT.name()) == 0) {
            int i = 1;
            System.out.println(System.lineSeparator());
            for (StudentRecord.Mutable_Fields fieldType : StudentRecord.Mutable_Fields.values()) {
                if (i != 3)
                    System.out.println(i + ". " + fieldType.name());
                i++;
            }
            System.out.print("Your choice: ");
            input = Integer.parseInt(sc.nextLine());
            fieldName = StudentRecord.Mutable_Fields.values()[--input].name();
        } else if (recordType.compareTo(Record.Record_Type.TEACHER.name()) == 0) {
            int i = 1;
            System.out.println(System.lineSeparator());
            for (TeacherRecord.Mutable_Fields fieldType : TeacherRecord.Mutable_Fields.values()) {
                System.out.println(i + ". " + fieldType.name());
                i++;
            }
            System.out.print("Your choice: ");
            input = Integer.parseInt(sc.nextLine());
            fieldName = TeacherRecord.Mutable_Fields.values()[--input].name();
        } else {
            System.out.println(recordID + " not found");
            LOGGER.info(recordID + " not found to edit");
            return;
        }

        System.out.print("Enter Value: ");
        String value = sc.nextLine();

        boolean isSuccess = dcmsServer.editRecord(managerID, recordID, fieldName, value);
        if (isSuccess) {
            LOGGER.info(recordID + " edited: " + fieldName + " = " +value);
            System.out.println(recordID + " edited: " + fieldName + " = " +value);
        }
        else {
            LOGGER.info(recordID + " edit failed");
            System.out.println(recordID + " edit failed");
        }
    }

    private void transferRecord(DCMS dcmsServer) {
        System.out.print("Enter Record ID: ");
        String recordID = sc.nextLine().toUpperCase();
        System.out.print("Enter Server Name: ");
        String serverName = sc.nextLine().toUpperCase();
        boolean isSuccess = dcmsServer.transferRecord(managerID, recordID, serverName);
        if (isSuccess) {
            LOGGER.info(recordID + " transfered to " + serverName);
            System.out.println(recordID + " transfered to " + serverName);
        }
        else {
            LOGGER.info(recordID + " failed to transfer to " + serverName);
            System.out.println(recordID + " failed to transfer to " + serverName);
        }
    }

    private void printRecord(DCMS dcmsServer) {
        System.out.print("Enter Record ID: ");
        String recordID = sc.nextLine().toUpperCase();
        String result = dcmsServer.printRecord(managerID, recordID);
        if (result.compareTo("") != 0) {
            System.out.println(result);
            LOGGER.info(String.format(recordID + " is printed"));
        }
        else {
            System.out.println(recordID + " not found");
            LOGGER.info(recordID + " not found to print");
        }
    }

    private void getRecordCount(DCMS dcmsServer) {
        String result = dcmsServer.getRecordCounts(managerID);
        System.out.println(result);
        LOGGER.info(String.format(Config.LOG_RECORDS_COUNT, managerID, result));
    }

    private void initiateLogger() throws IOException {
        LOGGER = Logger.getLogger(managerID);
        LOGGER.setUseParentHandlers(false);
        FileHandler fileHandler = new FileHandler(String.format(Config.LOG_MANAGER_FILENAME, managerID));
        LOGGER.addHandler(fileHandler);
        SimpleFormatter formatter = new SimpleFormatter();
        fileHandler.setFormatter(formatter);
    }

    public void writeLog(String content) {
        LOGGER.info(content);
        System.out.println(content);
    }
}
