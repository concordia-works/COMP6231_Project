import HelloApp.*;

import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import org.omg.CORBA.*;

public class HelloClientLVL {

	static Hello helloImpl;
	static String managerID;
	private static Scanner entrace = new Scanner(System.in);
	static ArrayList<String> teacher = new ArrayList<String>();
	static ArrayList<String> specialization = new ArrayList<String>();
	static ArrayList<String> student = new ArrayList<String>();
	static ArrayList<String> courses = new ArrayList<String>();
	static ArrayList<String> edit = new ArrayList<String>();
	static ArrayList<String> transfer = new ArrayList<String>();
	static Date date = new Date();
	static SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
	static String today = formatter.format(date).toString();

	public static void main(String args[]) throws Exception {
		boolean session = true;
		do {
			String serverkey;
			serverkey = verifyManagerID();
			connectToServer(args, serverkey);
			System.out.println("Do you want to make an other operation? \nType 1 for YES, 2 for NO ");
			String userChoice = entrace.nextLine();
			if (userChoice.equals("2")) {
				session = false;
			}
		} while (session);

		System.out.println("###################PROGRAM CLOSED###################");

	}

	public static void connectToServer(String args[], String serverkey) throws Exception {
		try {
			// create and initialize the ORB
			ORB orb = ORB.init(args, null);
			// get the root naming context
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			// Use NamingContextExt instead of NamingContext. This is
			// part of the Interoperable naming Service.
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			// resolve the Object Reference in Naming
			String name = serverkey;
			helloImpl = HelloHelper.narrow(ncRef.resolve_str(name));
			System.out.println("Obtained a handle on server object: " + helloImpl);
			// helloImpl.shutdown();
		} catch (Exception e) {
			System.out.println("ERROR : " + e);
			e.printStackTrace(System.out);
		}

		chooseOperation(helloImpl);
	}

	public static void chooseOperation(Hello helloImpl) throws Exception {
		boolean x = false;
		String choice;
		String result;
		do {
			System.out.println("Which Operation would you like to carry out:" + "\n"
					+ "PRESS 1 TO INSERT A TEACHER RECORD" + "\n" + "PRESS 2 TO INSERT A STUDENT RECORD" + "\n"
					+ "PRESS 3 TO GET THE NUMBER OF RECORDS" + "\n" + "PRESS 4 TO EDIT A RECORD" 
					+ "\n" + "PRESS 5 TO SEND A RECORD TO ANOTHER SERVER");
			choice = entrace.nextLine();
			switch (choice) {
			case "1":
				String[] spec = insertTData();
				System.out.println(teacher.get(4));
				result = helloImpl.createTRecord(managerID, teacher.get(0), teacher.get(1), teacher.get(2),
						teacher.get(3), spec, teacher.get(4));
				System.out.println(result);
				teacher.clear();
				specialization.clear();
				x = true;
				break;
			case "2":
				String[] cr = insertSData();
				result = helloImpl.createSRecord(managerID, student.get(0), student.get(1), cr, student.get(2), today);
				System.out.println(result);
				student.clear();
				courses.clear();
				x = true;
				break;
			case "3":
				result = helloImpl.getRecordCounts(managerID);
				System.out.println(result);
				x = true;
				break;
			case "4":
				editData();
				result = helloImpl.editRecord(managerID, edit.get(0), edit.get(1), edit.get(2));
				System.out.println(result);
				edit.clear();
				x = true;
				break;
			case "5":
				transferData();
				result = helloImpl.transferRecord(managerID, transfer.get(0), transfer.get(1));
				System.out.println(result);
				transfer.clear();
				x = true;
				break;
			default:
				System.out.println("Enter a right choice");
				break;
			}
		} while (x == false);
	}

	public static String verifyManagerID() {

		String str1 = "error";
		String str2;
		boolean x = false;

		do {
			System.out.println("Please type your ManagerID: ");
			managerID = entrace.nextLine();
			if (managerID.length() == 7) {
				str1 = managerID.substring(0, 3);
				str1 = str1.toUpperCase();
				str2 = managerID.substring(3, 7);
				if (str1.equals("LVL")) {
					try {
						Integer.valueOf(str2);
						x = true;
					} catch (NumberFormatException e) {
						System.out.println("Invalid ManagerID.");
					}
				}
			} else {
				System.out.println("Invalid ManagerID.");
			}
		} while (x == false);
		return str1;
	}

	public static String[] insertTData() {
		boolean x = false;
		String choice;
		String location;

		System.out.println("Insert the name of the teacher: ");
		teacher.add(entrace.nextLine());
		System.out.println("Insert the last name of the teacher: ");
		teacher.add(entrace.nextLine());
		System.out.println("Insert the adress of the teacher: ");
		teacher.add(entrace.nextLine());
		System.out.println("Insert the phone of the teacher: ");
		teacher.add(entrace.nextLine());
		do {
			System.out.println("Insert the specialization(s) of the teacher: ");
			specialization.add(entrace.nextLine());
			do {
				System.out.println("The teacher has more specizalitions? \nType 1 for YES or type 2 for NO: ");
				choice = entrace.nextLine();
				if (choice.equals("2")) {
					x = true;
					break;
				}
			} while (!choice.equals("1"));
		} while (x == false);
		x = false;
		do {
			System.out.println("Insert the location of the teacher (LVL or MTL or DDO): ");
			location = entrace.nextLine().toUpperCase();
			if ((location.equals("LVL")) || (location.equals("MTL")) || (location.equals("DDO"))) {
				teacher.add(location);
				x = true;
			} else {
				System.out.println("The the location of the teacher needs to be: MTL or LVL or DDO");
			}
		} while (x == false);

		return fixArrayT();

	}

	public static String[] insertSData() {
		boolean x = false;
		String choice;
		String status;

		System.out.println("Insert the name of the student: ");
		student.add(entrace.nextLine());
		System.out.println("Insert the last name of the student: ");
		student.add(entrace.nextLine());
		do {
			System.out.println("Insert the courses(s) of the student: ");
			specialization.add(entrace.nextLine());
			do {
				System.out.println("The student has more courses? \nType 1 for YES or type 2 for NO: ");
				choice = entrace.nextLine();
				if (choice.equals("2")) {
					x = true;
					break;
				}
			} while (!choice.equals("1"));
		} while (x == false);
		x = false;
		do {
			System.out.println("Insert the status of the student (ACTIVE OR INACTIVE): ");
			status = entrace.nextLine().toUpperCase();
			if ((status.equals("ACTIVE")) || (status.equals("INACTIVE"))) {
				student.add(status);
				x = true;
			} else {
				System.out.println("The the status of the student needs to be: ACTIVE or INACTIVE");
			}
		} while (x == false);

		return fixArrayS();

	}

	public static String[] fixArrayS() {
		int count = 0;
		for (int x = 0; x < courses.size(); x++) {
			if (courses.get(x) != null) {
				count++;
			}
		}
		String[] cr = new String[count];
		for (int i = 0; i < cr.length; i++) {
			cr[i] = courses.get(i);
		}
		return cr;
	}

	public static String[] fixArrayT() {
		int count = 0;
		for (int x = 0; x < specialization.size(); x++) {
			if (specialization.get(x) != null) {
				count++;
			}
		}
		String[] spec = new String[count];
		for (int i = 0; i < spec.length; i++) {
			spec[i] = specialization.get(i);
		}
		return spec;
	}

	public static void editData() {
		System.out.println("Insert the recordID of the record that you want to modify: ");
		edit.add(entrace.nextLine().toUpperCase());
		System.out.println("Insert the field name of the record that you want to modify: ");
		edit.add(entrace.nextLine());
		System.out.println("Insert the new value of the record that you want to modify: ");
		edit.add(entrace.nextLine());

	}

	public static void transferData() {
		System.out.println("Insert the recordID of the record that you want to transfer: ");
		transfer.add(entrace.nextLine().toUpperCase());
		System.out.println("Insert the name of the server that you want to transfer: ");
		transfer.add(entrace.nextLine().toUpperCase());
	}
}
