package Servers;

import Utils.Config;

import java.lang.management.ManagementFactory;

public class KenroRMDriver {
    public static void main(String args[]) {
        System.out.println(ManagementFactory.getRuntimeMXBean().getName());
        ReplicaManager replicaManager = new ReplicaManager(Config.ARCHITECTURE.REPLICAS.KEN_RO);
        Thread thread = new Thread(replicaManager);
        thread.start();
    }
}