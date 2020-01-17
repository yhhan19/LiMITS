public class SharedInteger {
    
    private volatile int cur;

    public SharedInteger() {
        cur = 0;
    }

    public synchronized int next() {
        return cur ++;
    }
}
