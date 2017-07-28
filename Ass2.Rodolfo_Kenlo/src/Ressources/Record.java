package Ressources;

public class Record {
	String firstName;
	String lastName;
	RECORD_TYPE recordType;

	public enum RECORD_TYPE {
		STUDENT, TEACHER
	};

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public RECORD_TYPE getRecordType() {
		return recordType;
	}

}
