package Utils;

/**
 * Created by quocminhvu on 2017-05-26.
 */

public class Configuration {
    public class CORBA {
        public static final String ROOT_POA = "RootPOA";
        public static final String NAME_SERVICE = "NameService";
    }

    // Records
    public static final String STUDENT_RECORD_FORMAT = "SR%05d";
    public static final String TEACHER_RECORD_FORMAT = "TR%05d";

    // Servers
    public enum Server_ID { QM_MTL, QM_LVL, QM_DDO }
    public static final String MTL_SERVER_FORMAT = "MTL%04d";
    public static final String LVL_SERVER_FORMAT = "LVL%04d";
    public static final String DDO_SERVER_FORMAT = "DDO%04d";
    public static final String MONTREAL_HOSTNAME = "localhost";
    public static final String LAVAL_HOSTNAME = "localhost";
    public static final String DOLLARD_DES_ORMEAUX_HOSTNAME = "localhost";
    public static final int MONTREAL_UDP_PORT = 2341;
    public static final int LAVAL_UDP_PORT = 2342;
    public static final int DOLLARD_DES_ORMEAUX_UDP_PORT = 2343;
    public static final int MONTREAL_RMI_PORT = 2344;
    public static final int LAVAL_RMI_PORT = 2345;
    public static final int DOLLARD_DES_ORMEAUX_RMI_PORT = 2346;
    public static final String FUNC_GET_RECORDS_NUMBER = "getRecordsNumber";
    public static final String FUNC_TRANSFER_TEACHER_RECORD = "transferTRecord";
    public static final String FUNC_TRANSFER_STUDENT_RECORD = "transferSRecord";
    public static final String FUNC_GET_RECORD_ID = "getRecordID";
    public static final String DELIMITER = "\\|";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String PRINT_TEACHER_RECORD = "%s: Name(%s %s) Address(%s) Phone(%s) Specialization(%s) Location(%s)";
    public static final String PRINT_STUDENT_RECORD = "%s: Name(%s %s) CoursesRegistered(%s) Status(%s) StatusDate(%s)";
    public static final String NO_RECORD_PRINT = "There is no record to print";

    // Logging
    public static final String LOG_SERVER_FILENAME = "server_%s.log";
    public static final String LOG_MANAGER_FILENAME = "manager_%s.log";
    public static final String LOG_MODIFIED_RECORD_SUCCESS = "%s modify %s: FieldName(%s) Value(%s)";
    public static final String LOG_MODIFIED_RECORD_FAILED = "%s cannot modify %s: FieldName(%s) Value(%s)";
    public static final String LOG_CREATE_TEACHER_RECORD = "%s add %s: Name(%s %s) Add(%s) Phone(%s) Spec(%s) Loc(%s)";
    public static final String LOG_CREATE_STUDENT_RECORD = "%s add %s: Name(%s %s) Courses(%s) Status(%s)";
    public static final String LOG_TRANSFER_TEACHER_RECORD = "%s transfer %s: Name(%s %s) Add(%s) Phone(%s) Spec(%s) Loc(%s)";
    public static final String LOG_TRANSFER_STUDENT_RECORD = "%s transfer %s: Name(%s %s) Courses(%s) Status(%s) Date(%s)";
    public static final String LOG_TRANSFER_RECORD_SUCCESS = "%s transfer %s to %s";
    public static final String LOG_TRANSFER_RECORD_FAIL = "%s failed to transfer %s to %s";
    public static final String LOG_RECORDS_COUNT = "%s get count %s";
    public static final String LOG_PRINT_RECORD = "%s print %s";
    public static final String LOG_UDP_SERVER_START = "UDP Server started at port %s";
    public static final String LOG_UDP_SERVER_STOP = "UDP Server at port %s stopped";
    public static final String LOG_CONNECT_RMI_SUCCESS = "Connect to the %s server at port %s successfully";

    // Menu
    public static final String MAIN_MENU = System.lineSeparator() +
                                            "0. Quit" + System.lineSeparator() +
                                            "1. Single Thread" + System.lineSeparator() +
                                            "2. Multi Threads" + System.lineSeparator() +
                                            "Your choice: ";

    public static final String SINGLE_THREAD_MENU = System.lineSeparator() +
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
                                                    "0. Back" + System.lineSeparator() +
                                                    "1. Multi-threads create new records concurrently" + System.lineSeparator() +
                                                    "2. Multi-threads edit, print and transfer the same record concurrently" + System.lineSeparator() +
                                                    "Your choice: ";

    public static int getUDPPortByServerID(Server_ID server_id) {
        switch (server_id) {
            case QM_MTL:
                return MONTREAL_UDP_PORT;
            case QM_LVL:
                return LAVAL_UDP_PORT;
            case QM_DDO:
                return DOLLARD_DES_ORMEAUX_UDP_PORT;
            default:
                return 0;
        }
    }

    public static int getRMIPortByServerID(Server_ID server_id) {
        switch (server_id) {
            case QM_MTL:
                return MONTREAL_RMI_PORT;
            case QM_LVL:
                return LAVAL_RMI_PORT;
            case QM_DDO:
                return DOLLARD_DES_ORMEAUX_RMI_PORT;
            default:
                return 0;
        }
    }

    public static String getHostnameByServerID(Server_ID server_id) {
        switch (server_id) {
            case QM_MTL:
                return MONTREAL_HOSTNAME;
            case QM_LVL:
                return LAVAL_HOSTNAME;
            case QM_DDO:
                return DOLLARD_DES_ORMEAUX_HOSTNAME;
            default:
                return "Wrong Server ID";
        }
    }
}
