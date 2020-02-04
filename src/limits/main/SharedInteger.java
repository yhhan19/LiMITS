package limits.main;

public class SharedInteger {
    
    private volatile int cur;
    private char ch;

    public SharedInteger(int type) {
        cur = 0;
        if (type == 0) ch = '#';
        else ch = '*';
    }

    public synchronized int next() {
        int ret = cur ++;
        if (cur % 10 == 0) 
            System.out.print(ch);
        if (cur % 400 == 0) 
            System.out.println(" " + cur);
        return ret;
    }
}
