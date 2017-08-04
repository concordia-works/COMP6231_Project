package Servers;

import Utils.Config;

import java.lang.management.ManagementFactory;

public class MinhRMDriver {
    public static void main(String args[]) {
        System.out.println(ManagementFactory.getRuntimeMXBean().getName());
        ReplicaManager replicaManager = new ReplicaManager(Config.ARCHITECTURE.REPLICAS.MINH);
        Thread thread = new Thread(replicaManager);
        thread.start();
    }
}