package Servers;

import java.io.*;
import java.util.Scanner;

class SimpleElection{
    static int totalNumReplicaManager = 3;
    static int processPriority[] = new int[3];
    static int status[] = new int[3];
    static int leader;

    public static void main(String args[])throws IOException
    {
        System.out.println("Enter the number of process");
        System.out.println("Total Number of Processes is 3.");
        Scanner in = new Scanner(System.in);
//        totalNumReplicaManager = in.nextInt();



        for(int i=0;i<totalNumReplicaManager;i++)
        {
            System.out.println("For process "+(i+1)+":");
            System.out.println("Status:");
            status[i]=in.nextInt();
            // get real status

            System.out.println("Priority");
            processPriority[i] = in.nextInt();
            //get replicaManagerID


        }

        System.out.println("Which process will initiate election?");
        int initiator = in.nextInt();

        runElection(initiator);
        System.out.println("The Leading ReplicaManager is "+leader);
    }

    static void runElection(int initiator)
    {
        initiator = initiator-1;
        leader = initiator+1;
        for(int i=0;i<totalNumReplicaManager;i++)
        {
            if(processPriority[initiator]<processPriority[i])
            {
                System.out.println("Election message is sent from "+(initiator+1)+" to "+(i+1));
                if(status[i]==1)
                    runElection(i+1);
            }
        }
    }
}