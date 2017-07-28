package Utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class Config {
    private Config() {}

    public static class ARCHITECTURE {
        public enum REPLICAS {MINH, KAMAL, KEN_RO}
        public enum SERVER_ID {MTL, LVL, DDO}
    }

    public static class REQUEST {
        public enum METHODS_NAME {createTRecord, createSRecord, getRecordsCount,
                                  editRecord, transferRecord, printRecord, printAllRecords}
    }

    public static class RESPONSE {
        public static final String RESPONSE_CONTENT = "Request %s is %s" + System.lineSeparator() + "%s";
        public static final String CREATE_T_RECORD = "Record %s is created";
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
        public static final int PORT_FRONT_END_TO_LEADER = 123;
        public static final int PORT_LEADER_TO_FRONT_END = 456;
        public static final int PORT_LEADER_TO_BACKUPS = 234;
        public static final int PORT_BACKUPS_TO_LEADER = 567;
        public static final int PORT_HEART_BEAT = 345;
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
