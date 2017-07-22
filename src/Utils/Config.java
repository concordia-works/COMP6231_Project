package Utils;

public class Config {
    public static class ARCHITECTURE {
        public enum REPLICAS {MINH, KAMAL, KEN_RO}
    }

    public static class REQUEST {
        public enum METHODS_NAME {createTRecord, createSRecord, getRecordsCount,
                                  editRecord, transferRecord, printRecord, printAllRecords}
    }

    public static class RESPONSE {
        public static final String RESPONSE_CONTENT = "Request %s is %s%s";
        public static final String CREATE_T_RECORD = "\nRecord %s is created";
        public static final String CREATE_S_RECORD = "\nStudent record %s is created";
        public static final String EDIT_RECORD = "\nRecord %s is edited";
        public static final String TRANSFER_RECORD = "\nRecord %s is transferred";
    }

    public static class CORBA {
        public static final String ORB_PARAMETERS = "-ORBInitialPort 1050 -ORBInitialHost localhost";
        public static final String ROOT_POA = "RootPOA";
        public static final String NAME_SERVICE = "NameService";
    }

    public static class UDP {
        public static final int PORT_FRONT_END_TO_LEADER = 123;
        public static final int PORT_LEADER_TO_BACKUPS = 234;
        public static final int PORT_HEART_BEAT = 345;
    }
}
