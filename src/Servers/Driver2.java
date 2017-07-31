package Servers;

import Utils.Config;

class Driver2
{
    public static void main(String [] s)
    {
        HeartBeat h=new HeartBeat(Config.ARCHITECTURE.REPLICAS.MINH, Config.UDP.PORT_HEART_BEAT);
        h.start();
    }
}