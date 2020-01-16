public class Pointer {
    
    private int cur;

    public Pointer() {
        cur = 0;
    }

    public synchronized int next() {
        int ret = cur;
        cur ++;
        return ret;
    }
}
