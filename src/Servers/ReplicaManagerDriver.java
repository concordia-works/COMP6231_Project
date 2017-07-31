package Servers;

import Utils.Config;

public class ReplicaManagerDriver {
    public static void main(String args[]) {
        ReplicaManager kenroReplica = new ReplicaManager(Config.ARCHITECTURE.REPLICAS.KEN_RO);
        Thread t1 = new Thread(kenroReplica);
        t1.start();

        ReplicaManager kamalReplica = new ReplicaManager(Config.ARCHITECTURE.REPLICAS.KAMAL);
        Thread t2 = new Thread(kamalReplica);
        t2.start();

        ReplicaManager minhReplica = new ReplicaManager(Config.ARCHITECTURE.REPLICAS.MINH);
        Thread t3 = new Thread(minhReplica);
        t3.start();

        // Start the election
    }
}
