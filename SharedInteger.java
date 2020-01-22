public class SharedInteger {
    
    private volatile int cur;

    public SharedInteger() {
        cur = 0;
    }

    public synchronized int next() {
        int ret = cur ++;
        if (cur % 100 == 0) 
            System.out.println(cur + " trajectories processed");
        return ret;
    }
}
