package HelloApp;


/**
* HelloApp/HelloOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from C:/Users/rodmm/IdeaProjects/COMP6231_Project/Ass2.Rodolfo_Kenlo/src/Hello.idl
* Monday, July 31, 2017 5:19:29 PM EDT
*/

public interface HelloOperations 
{
  String createTRecord (String managerID, String firstName, String lastName, String address, String phone, String[] specialization, String location);
  String createSRecord (String managerID, String firstName, String lastName, String[] courses, String status, String statusDate);
  String getRecordCounts (String managerID);
  boolean editRecord (String managerID, String recordID, String fieldName, String newValue);
  boolean transferRecord (String managerID, String recordID, String remoteCenterServerName);
  void shutdown ();
} // interface HelloOperations
