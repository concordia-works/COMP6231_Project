package Servers;

import Utils.Config;

/**
 * Created by kamal on 7/29/2017.
 */
public class Driver3
{
    public static void main(String [] s)
    {
        HeartBeat h=new HeartBeat(Config.ARCHITECTURE.REPLICAS.KEN_RO, Config.UDP.PORT_HEART_BEAT);
        h.start();

    }
}