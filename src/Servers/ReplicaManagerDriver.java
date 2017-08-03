package Servers;

import Utils.Config;

public class ReplicaManagerDriver {
    public static void main(String args[]) {
        for (Config.ARCHITECTURE.REPLICAS replicaID : Config.ARCHITECTURE.REPLICAS.values()) {
            ReplicaManager replicaManager = new ReplicaManager(replicaID);
            Thread thread = new Thread(replicaManager);
            thread.start();
        }
    }
}
