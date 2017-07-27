package Servers;

import Utils.Config;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Request implements Serializable {
    private int sequenceNumber;
    private Config.REQUEST.METHODS_NAME methodName;
    private String managerID;
    private String firstName;
    private String lastName;
    private String address;
    private String phone;
    private String specialization;
    private String location;
    private String coursesRegistered;
    private String status;
    private String recordID;
    private String fieldName;
    private String newValue;
    private String remoteCenterServerName;
    private String fullInvocation;

    // For createTRecord
    public Request(int sequenceNumber, String managerID, Config.REQUEST.METHODS_NAME methodName, String firstName, String lastName, String address, String phone, String specialization, String location) {
        this.sequenceNumber = sequenceNumber;
        this.managerID = managerID;
        this.methodName = methodName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.phone = phone;
        this.specialization = specialization;
        this.location = location;
        this.fullInvocation = String.format("%s(%s, %s, %s, %s, %s, %s, %s)", methodName, managerID, firstName, lastName, address, phone, specialization, location);
    }

    // For createSRecord
    public Request(int sequenceNumber, String managerID, Config.REQUEST.METHODS_NAME methodName, String firstName, String lastName, String coursesRegistered, String status) {
        this.sequenceNumber = sequenceNumber;
        this.managerID = managerID;
        this.methodName = methodName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.coursesRegistered = coursesRegistered;
        this.status = status;
        this.fullInvocation = String.format("%s(%s, %s, %s, %s, %s)", methodName, managerID, firstName, lastName, coursesRegistered, status);
    }

    // For getRecordsCount & printAllRecords
    public Request(int sequenceNumber, String managerID, Config.REQUEST.METHODS_NAME methodName) {
        this.sequenceNumber = sequenceNumber;
        this.managerID = managerID;
        this.methodName = methodName;
        this.fullInvocation = String.format("%s(%s)", methodName, managerID);
    }

    // For editRecord
    public Request(int sequenceNumber, String managerID, Config.REQUEST.METHODS_NAME methodName, String recordID, String fieldName, String newValue) {
        this.sequenceNumber = sequenceNumber;
        this.managerID = managerID;
        this.methodName = methodName;
        this.recordID = recordID;
        this.fieldName = fieldName;
        this.newValue = newValue;
        this.fullInvocation = String.format("%s(%s, %s, %s, %s)", methodName, managerID, recordID, fieldName, newValue);
    }

    // For transferRecord
    public Request(int sequenceNumber, String managerID, Config.REQUEST.METHODS_NAME methodName, String recordID, String remoteCenterServerName) {
        this.sequenceNumber = sequenceNumber;
        this.managerID = managerID;
        this.methodName = methodName;
        this.recordID = recordID;
        this.remoteCenterServerName = remoteCenterServerName;
        this.fullInvocation = String.format("%s(%s, %s, %s)", methodName, managerID, recordID, remoteCenterServerName);
    }

    // For printRecord
    public Request(int sequenceNumber, String managerID, Config.REQUEST.METHODS_NAME methodName, String recordID) {
        this.sequenceNumber = sequenceNumber;
        this.managerID = managerID;
        this.methodName = methodName;
        this.recordID = recordID;
        this.fullInvocation = String.format("%s(%s, %s)", methodName, managerID, recordID);
    }

    // Getters
    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public Config.REQUEST.METHODS_NAME getMethodName() {
        return methodName;
    }

    public String getManagerID() {
        return managerID;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getLocation() {
        return location;
    }

    public String getCoursesRegistered() {
        return coursesRegistered;
    }

    public String getStatus() {
        return status;
    }

    public String getRecordID() {
        return recordID;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getNewValue() {
        return newValue;
    }

    public String getRemoteCenterServerName() {
        return remoteCenterServerName;
    }

    public String getFullInvocation() {
        return fullInvocation;
    }

    public byte[] serialize() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(this);
        return out.toByteArray();
    }
}
