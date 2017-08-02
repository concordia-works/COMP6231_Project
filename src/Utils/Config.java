package Utils;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

public class Config {
    private Config() {}

    public static class ARCHITECTURE {
        public enum REPLICAS {
//            MINH(1), KAMAL(2), KEN_RO(3);
//            KEN_RO(1), MINH(2), KAMAL(3);
            KAMAL(1), KEN_RO(2), MINH(3);

            private final int coefficient;
            REPLICAS(int coefficient) { this.coefficient = coefficient; }
            public int getCoefficient() { return coefficient; }
        }

        public enum SERVER_ID {MTL, LVL, DDO}

        public enum KAMAL_SERVER_ID {KM_MTL, KM_LVL, KM_DDO}

        public enum KENRO_SERVER_ID {KR_MTL, KR_LVL, KR_DDO}
    }

    public static class REQUEST {
        public enum METHODS_NAME {createTRecord, createSRecord, getRecordsCount,
                                  editRecord, transferRecord, printRecord, printAllRecords}
    }

    public static class RESPONSE {
        public static final String RESPONSE_CONTENT = "Request %s is %s" + System.lineSeparator() + "%s";
        public static final String CREATE_T_RECORD = "Teacher record %s is created";
        public static final String CREATE_S_RECORD = "Student record %s is created";
        public static final String EDIT_RECORD = "Record %s is edited";
        public static final String TRANSFER_RECORD = "Record %s is transferred";
    }

    public static class CORBA {
        public static final String ORB_PARAMETERS = "-ORBInitialPort 1050 -ORBInitialHost localhost";
        public static final String ROOT_POA = "RootPOA";
        public static final String NAME_SERVICE = "NameService";
        public static final String FRONT_END_NAME = "FrontEnd";
    }

    public static class UDP {
        public static final int PORT_FRONT_END_TO_LEADER = 1231;
        public static final int PORT_LEADER_TO_BACKUPS = 1232;
        public static final int PORT_BACKUPS_TO_LEADER = 1233;
        public static final int PORT_HEART_BEAT = 1234;
        public static final int PORT_ELECTION = 1235;
        public static final int PORT_NEW_LEADER = 1236;

        public static final int KENRO_UDP_MTL = 6789;
        public static final int KENRO_UDP_LVL = 6788;
        public static final int KENRO_UDP_DDO = 6787;
    }

    public static class FRONT_END {
        public static final int COEFFICIENT = 10;
    }

    public static class ELECTION {
        public static final String MESSAGE = "Election message";
        public static final String RESPONSE = "Election response";
        public static final int ANSWER_TIMEOUT = 10;
        public static final int ELECTION_TIMEOUT = 20;
    }

    public static class UI {
        public static final String MAIN_MENU = System.lineSeparator() +
                "0. Quit" + System.lineSeparator() +
                "1. Single Thread" + System.lineSeparator() +
                "2. Multi Threads" + System.lineSeparator() +
                "Your choice: ";

        public static final String SINGLE_THREAD_MENU = System.lineSeparator() +
                "-----SINGLE THREAD MENU-----" + System.lineSeparator() +
                "0. Back" + System.lineSeparator() +
                "1. Create Student record" + System.lineSeparator() +
                "2. Create Teacher record" + System.lineSeparator() +
                "3. Edit record" + System.lineSeparator() +
                "4. Get records count" + System.lineSeparator() +
                "5. Transfer record" + System.lineSeparator() +
                "6. Print a record" + System.lineSeparator() +
                "7. Print all records" + System.lineSeparator() +
                "8. Login to another server" + System.lineSeparator() +
                "Your choice: ";

        public static final String MULTI_THREAD_MENU = System.lineSeparator() +
                "---------------------------MULTI THREAD MENU---------------------------" + System.lineSeparator() +
                "0. Back" + System.lineSeparator() +
                "1. Multi-threads create new records concurrently" + System.lineSeparator() +
                "2. Multi-threads edit, print and transfer the same record concurrently" + System.lineSeparator() +
                "Your choice: ";
    }

    public static class LOGGING {
        public static final String MANAGER_FILE_PATH = "manager_%s.log";
        public static final String GET_RECORDS_COUNT = "%s get count %s";
        public static final String SERVER_START = "RM %s: Server %s starts";
        public static final String UDP_START = "RM %s - Server %s: UDP starts at port %s";
    }

    public static Request deserializeRequest(byte[] data) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);
            return (Request) is.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Response deserializeResponse(byte[] data) {
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(data);
            ObjectInputStream is = new ObjectInputStream(in);
            return (Response) is.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
