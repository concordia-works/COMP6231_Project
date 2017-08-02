package ServersImpl;
//Provides the implementation of type of arraylist in which records are to be inserted

public class Record {
    String first_Name;
    String last_Name;
    String courses_Registered;
    String status;
    String date;
    String address;
    String phone;
    String specilization;
    String location;
    String record_id;

    Record(String id, String f_name, String l_name, String courses, String status, String dat)        //overloaded constructor for Student record
    {
        this.record_id = id;
        this.first_Name = f_name;
        this.last_Name = l_name;
        this.courses_Registered = courses;
        this.status = status;
        this.date = dat;
    }

    Record(String id, String f_name, String l_name, String addr, String number, String spec, String loc) //overloaded constructor for Teacher record
    {
        this.record_id = id;
        this.first_Name = f_name;
        this.last_Name = l_name;
        this.address = addr;
        this.phone = number;
        this.location = loc;
        this.specilization = spec;
    }

    String getFirst_Name() {
        return this.first_Name;
    }

    public String toString()                                        //default toString method modified
    {
        if (record_id.substring(0, 2).equals("SR")) {
            return record_id + "|" + first_Name + "|" + last_Name + "|" + status + "|" + courses_Registered + "|" + date;
        } else
            return record_id + "|" + first_Name + "|" + last_Name + "|" + address + "|" + phone + "|" + specilization + "|" + location;
    }
}
