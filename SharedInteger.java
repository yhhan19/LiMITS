public class SharedInteger {
    
    private volatile int cur;

    public SharedInteger() {
        cur = 0;
    }

    public synchronized int next() {
        int ret = cur ++;
        if (cur % 10 == 0) 
            System.out.print("*");
        if (cur % 400 == 0) 
            System.out.println(" " + cur);
        return ret;
    }
}
