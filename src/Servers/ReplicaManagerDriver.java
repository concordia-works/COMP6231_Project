package Servers;

import Utils.Config;

public class ReplicaManagerDriver {
    public static void main(String args[]) {
        ReplicaManager minhReplica = new ReplicaManager(Config.ARCHITECTURE.REPLICAS.MINH);
        Thread t1 = new Thread(minhReplica);
        t1.start();
    }
}
