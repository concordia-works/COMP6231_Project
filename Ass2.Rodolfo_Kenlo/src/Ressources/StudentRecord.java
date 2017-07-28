package Ressources;

import java.util.ArrayList;

public class StudentRecord extends Record {
	private String recordID;
	private ArrayList<String> courseRegistered = new ArrayList<String>();
	private String status;
	private String statusDate;

	
	public StudentRecord(String firstName, String lastName, ArrayList<String> courseRegistered,
			String status, String statusDate) {

		this.firstName = firstName;
		this.lastName = lastName;
		this.courseRegistered = courseRegistered;
		this.status = status;
		this.statusDate = statusDate;
		this.recordType = Record.RECORD_TYPE.STUDENT;
		
	}
		
	public String getRecordID() {
			return recordID;
	}

	public void setRecordID(String recordID) {
			this.recordID = recordID;
	}

	public ArrayList<String> getCourseRegistered() {
		return courseRegistered;
	}
	public void setCourseRegistered(ArrayList<String> courseRegistered) {
		this.courseRegistered = courseRegistered;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStatusDate() {
		return statusDate;
	}
	public void setStatusDate(String statusDate) {
		this.statusDate = statusDate;
	}
	
	
	

}
