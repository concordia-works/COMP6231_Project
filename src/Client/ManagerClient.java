package Client;

import FrontEnd.FE;
import FrontEnd.FEHelper;
import Servers.Record;
import Servers.StudentRecord;
import Servers.TeacherRecord;
import Utils.Config;
import Utils.Configuration;
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
                System.out.print(Config.UI.MAIN_MENU);
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
        ManagerClient client = new ManagerClient(managerID);
        client.initiateLogger();

        // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
        FE frontEnd = FEHelper.narrow(namingContextRef.resolve_str(Config.CORBA.FRONT_END_NAME));
        System.out.println(managerID + " connect to Front-End successfully");

        int input;
        do {
            System.out.print(Config.UI.SINGLE_THREAD_MENU);
            input = Integer.parseInt(sc.nextLine());
            switch (input) {
                case 1:
                    client.createStudentRecord(frontEnd);
                    break;
                case 2:
                    client.createTeacherRecord(frontEnd);
                    break;
                case 3:
                    client.editRecord(frontEnd);
                    break;
                case 4:
                    client.getRecordCount(frontEnd);
                    break;
                case 5:
                    client.transferRecord(frontEnd);
                    break;
                case 6:
                    client.printRecord(frontEnd);
                    break;
                case 7:
                    System.out.println(frontEnd.printAllRecords(managerID));
                    break;
                case 8:
                    System.out.print("Enter Manager ID: ");
                    managerID = sc.nextLine().toUpperCase();
                    client = new ManagerClient(managerID);
                    client.initiateLogger();

                    // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                    frontEnd = FEHelper.narrow(namingContextRef.resolve_str(Config.CORBA.FRONT_END_NAME));
                    System.out.println(managerID + " connect to Front-End successfully");
                    break;
                default:
                    // Do nothing
                    break;
            }
        } while (input != 0);


    }

    private static void multiThreadUI() throws Exception {
        int input;
        do {
            System.out.print(Config.UI.MULTI_THREAD_MENU);
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
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                FE frontEnd = FEHelper.narrow(namingContextRef.resolve_str(Config.CORBA.FRONT_END_NAME));
                client.writeLog(managerID + " connect to Front-End successfully");

                String recordID = frontEnd.createSRecord(managerID, "Quoc Minh", "Vu", "distributed system", "Active");
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
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                FE frontEnd = FEHelper.narrow(namingContextRef.resolve_str(Config.CORBA.FRONT_END_NAME));
                client.writeLog(managerID + " connect to Front-End successfully");

                String recordID = frontEnd.createSRecord(managerID, "Duc Son", "Vo", "advanced programming", "Active");
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
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                FE frontEnd = FEHelper.narrow(namingContextRef.resolve_str(Config.CORBA.FRONT_END_NAME));
                client.writeLog(managerID + " connect to Front-End successfully");

                String recordID = frontEnd.createSRecord(managerID, "Duy Tung", "Viet", "human-computer interaction", "Active");
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
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                FE frontEnd = FEHelper.narrow(namingContextRef.resolve_str(Config.CORBA.FRONT_END_NAME));
                client.writeLog(managerID + " connect to Front-End successfully");

                String recordID = frontEnd.createSRecord(managerID, "Phi Son", "Van Mai", "social and information network", "Inactive");
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
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                FE frontEnd = FEHelper.narrow(namingContextRef.resolve_str(Config.CORBA.FRONT_END_NAME));
                client.writeLog(managerID + " connect to Front-End successfully");

                String recordID = frontEnd.createSRecord(managerID, "Duc Minh", "Vuong", "advanced programming practice", "Inactive");
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
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                FE frontEnd = FEHelper.narrow(namingContextRef.resolve_str(Config.CORBA.FRONT_END_NAME));
                client.writeLog(managerID + " connect to Front-End successfully");

                String recordID = frontEnd.createTRecord(managerID, "Trieu Nguyen", "Van", "sherbrooke", "123 784 5678", "information technology", "QM_MTL");
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
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                FE frontEnd = FEHelper.narrow(namingContextRef.resolve_str(Config.CORBA.FRONT_END_NAME));
                client.writeLog(managerID + " connect to Front-End successfully");

                String recordID = frontEnd.createTRecord(managerID, "Nguyen Gia", "Vo", "sir george williams", "432 964 1930", "architect", "QM_MTL");
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
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                FE frontEnd = FEHelper.narrow(namingContextRef.resolve_str(Config.CORBA.FRONT_END_NAME));
                client.writeLog(managerID + " connect to Front-End successfully");

                String recordID = frontEnd.createTRecord(managerID, "Mai Huong", "Van", "cavendish", "849 282 4567", "drawing", "QM_MTL");
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
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                FE frontEnd = FEHelper.narrow(namingContextRef.resolve_str(Config.CORBA.FRONT_END_NAME));
                client.writeLog(managerID + " connect to Front-End successfully");

                String recordID = frontEnd.createTRecord(managerID, "Nguyen", "Vova", "mont royal", "514 234 9705", "electric", "QM_MTL");
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
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                FE frontEnd = FEHelper.narrow(namingContextRef.resolve_str(Config.CORBA.FRONT_END_NAME));
                client.writeLog(managerID + " connect to Front-End successfully");

                String recordID = frontEnd.createTRecord(managerID, "Nguyen Binh", "Vuon", "metcalfe", "125 765 3378", "chemistry", "QM_MTL");
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
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                FE frontEnd = FEHelper.narrow(namingContextRef.resolve_str(Config.CORBA.FRONT_END_NAME));

                String result = frontEnd.editRecord(managerID, recordID, "coursesRegistered", "edited courses registered");
                client.writeLog(result);
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
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                FE frontEnd = FEHelper.narrow(namingContextRef.resolve_str(Config.CORBA.FRONT_END_NAME));

                String result = frontEnd.transferRecord(managerID, recordID, "QM_LVL");
                client.writeLog(result);
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
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                FE frontEnd = FEHelper.narrow(namingContextRef.resolve_str(Config.CORBA.FRONT_END_NAME));

                String result = frontEnd.editRecord(managerID, recordID, "status", "edited status");
                client.writeLog(result);
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
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                FE frontEnd = FEHelper.narrow(namingContextRef.resolve_str(Config.CORBA.FRONT_END_NAME));

                String result = frontEnd.editRecord(managerID, recordID, "address", "edited address");
                client.writeLog(result);
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
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                FE frontEnd = FEHelper.narrow(namingContextRef.resolve_str(Config.CORBA.FRONT_END_NAME));

                String result = frontEnd.transferRecord(managerID, recordID, "QM_DDO");
                client.writeLog(result);
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
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                FE frontEnd = FEHelper.narrow(namingContextRef.resolve_str(Config.CORBA.FRONT_END_NAME));

                String result = frontEnd.editRecord(managerID, recordID, "location", "edited location");
                client.writeLog(result);
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
                ManagerClient client = new ManagerClient(managerID);
                client.initiateLogger();

                // Pass the NameComponent to the NamingService to get the object, then narrow it to proper type
                FE frontEnd = FEHelper.narrow(namingContextRef.resolve_str(Config.CORBA.FRONT_END_NAME));

                String result = frontEnd.editRecord(managerID, recordID, "phone", "edited phone");
                client.writeLog(result);
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

    private void createStudentRecord(FE frontEnd) {
        System.out.print("Enter FirstName: ");
        String firstName = sc.nextLine();
        System.out.print("Enter LastName: ");
        String lastName = sc.nextLine();
        System.out.print("Enter Courses registered: ");
        String coursesRegistered = sc.nextLine();
        System.out.print("Enter Status: ");
        String status = sc.nextLine();
        String recordID = frontEnd.createSRecord(managerID, firstName, lastName, coursesRegistered, status);
        LOGGER.info(recordID + " created");
        System.out.println(recordID + " created");
    }

    private void createTeacherRecord(FE frontEnd) {
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
        String recordID = frontEnd.createTRecord(managerID, firstName, lastName, address, phone, specialization, location);
        LOGGER.info(recordID + " created");
        System.out.println(recordID + " created");
    }

    private void editRecord(FE frontEnd) {
        int input;
        System.out.print("Enter ServersImpl.Record ID: ");
        String recordID = sc.nextLine().toUpperCase();

        String fieldName;
        if (recordID.substring(0, 2).compareTo("SR") == 0) {
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
        } else if (recordID.substring(0, 2).compareTo("TR") == 0) {
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

        String result = frontEnd.editRecord(managerID, recordID, fieldName, value);
        LOGGER.info(result);
        System.out.println(result);
    }

    private void transferRecord(FE frontEnd) {
        System.out.print("Enter ServersImpl.Record ID: ");
        String recordID = sc.nextLine().toUpperCase();
        System.out.print("Enter Server Name: ");
        String serverName = sc.nextLine().toUpperCase();
        String result = frontEnd.transferRecord(managerID, recordID, serverName);
        LOGGER.info(result);
        System.out.println(result);
    }

    private void printRecord(FE frontEnd) {
        System.out.print("Enter ServersImpl.Record ID: ");
        String recordID = sc.nextLine().toUpperCase();
        String result = frontEnd.printRecord(managerID, recordID);
        if (result.compareTo("") != 0) {
            System.out.println(result);
            LOGGER.info(String.format(recordID + " is printed"));
        }
        else {
            System.out.println(recordID + " not found");
            LOGGER.info(recordID + " not found to print");
        }
    }

    private void getRecordCount(FE frontEnd) {
        String result = frontEnd.getRecordCounts(managerID);
        System.out.println(result);
        LOGGER.info(String.format(Config.LOGGING.GET_RECORDS_COUNT, managerID, result));
    }

    private void initiateLogger() throws IOException {
        LOGGER = Logger.getLogger(managerID);
        LOGGER.setUseParentHandlers(false);
        FileHandler fileHandler = new FileHandler(String.format(Config.LOGGING.MANAGER_FILE_PATH, managerID));
        LOGGER.addHandler(fileHandler);
        SimpleFormatter formatter = new SimpleFormatter();
        fileHandler.setFormatter(formatter);
    }

    public void writeLog(String content) {
        LOGGER.info(content);
        System.out.println(content);
    }
}