package Servers;

/**
 * Created by quocminhvu on 2017-05-19.
 */

public class StudentRecord extends Record {
    public enum Mutable_Fields {coursesRegistered, status, statusDate}
    private String coursesRegistered;
    private String status;
    private String statusDate;

    public StudentRecord(String recordID, String firstName, String lastName, String coursesRegistered, String status, String statusDate) {
        super(recordID, firstName, lastName, Record_Type.STUDENT);
        this.coursesRegistered = coursesRegistered;
        this.status = status;
        this.statusDate = statusDate;
    }

    public String getCoursesRegistered() {
        return this.coursesRegistered;
    }

    public void setCoursesRegistered(String coursesRegistered) {
        this.coursesRegistered = coursesRegistered;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusDate() {
        return this.statusDate;
    }

    public void setStatusDate(String statusDate) {
        this.statusDate = statusDate;
    }
}
