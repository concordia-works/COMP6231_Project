package Servers;

import Utils.Config;

import java.lang.management.ManagementFactory;

public class KamalRMDriver {
    public static void main(String args[]) {
        System.out.println(ManagementFactory.getRuntimeMXBean().getName());
        ReplicaManager replicaManager = new ReplicaManager(Config.ARCHITECTURE.REPLICAS.KAMAL);
        Thread thread = new Thread(replicaManager);
        thread.start();
    }
}