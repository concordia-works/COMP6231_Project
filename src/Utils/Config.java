package Utils;

public class Config {
    public static class ARCHITECTURE {
        public enum REPLICAS {MINH, KAMAL, KEN_RO}
    }

    public static class REQUEST {
        public enum METHODS_NAME {createTRecord, createSRecord, getRecordsCount,
                                  editRecord, transferRecord,printRecord, printAllRecords}
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
