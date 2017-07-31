package Servers;

import Utils.Config;

class Driver
{
    public static void main(String [] s)
    {
        HeartBeat h=new HeartBeat(Config.ARCHITECTURE.REPLICAS.KAMAL, Config.UDP.PORT_HEART_BEAT);
        h.start();

    }
}