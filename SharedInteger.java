public class SharedInteger {
    
    private volatile int cur;

    public SharedInteger() {
        cur = 0;
    }

    public synchronized int next() {
        int ret = cur;
        cur ++;
        return ret;
    }
}
