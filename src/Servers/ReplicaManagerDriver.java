package Servers;

import Utils.Config;

import java.lang.management.ManagementFactory;

public class ReplicaManagerDriver {
    public static void main(String args[]) {
        System.out.println(ManagementFactory.getRuntimeMXBean().getName());
        for (Config.ARCHITECTURE.REPLICAS replicaID : Config.ARCHITECTURE.REPLICAS.values()) {
            ReplicaManager replicaManager = new ReplicaManager(replicaID);
            Thread thread = new Thread(replicaManager);
            thread.start();
        }
    }
}