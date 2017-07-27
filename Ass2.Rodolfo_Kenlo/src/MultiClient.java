import HelloApp.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.*;

public class MultiClient {
    // helloImpl is an instance of the Hello class
    static Hello helloImpl;
    public static void main(String[] args) throws Exception {
        for (Integer threadNumber = 1; threadNumber <= 4; threadNumber++) {
            String threadID = threadNumber.toString();
            String managerID = "MTL000" + threadID;
            MyThreads thread = new MyThreads(managerID);
            thread.start(args);
        }
    }

}
