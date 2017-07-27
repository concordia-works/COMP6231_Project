package Ressources;

import java.util.ArrayList;

public class TeacherRecord extends Record{
	private String recordID;
	private String adress;
	private String phone;
	private ArrayList<String> specalization = new ArrayList<String>();
	private String location;
	
		

	public TeacherRecord(String firstname, String lastname, String adress, String phone,
			ArrayList<String> specalization, String location) {
	

		this.firstName = firstname;
		this.lastName = lastname;
		this.adress = adress;
		this.phone = phone;
		this.specalization = specalization;
		this.location = location;
		this.recordType = Record.RECORD_TYPE.TEACHER;

	}
	
	
	
	public String getRecordID() {
		return recordID;
	}

	public void setRecordID(String strID) {
		this.recordID = strID;
	}

	public String getAdress() {
		return adress;
	}
	public void setAdress(String adress) {
		this.adress = adress;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public ArrayList<String> getSpecalization() {
		return specalization;
	}
	public void setSpecalization(ArrayList<String> specalization) {
		this.specalization = specalization;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	
	

}

