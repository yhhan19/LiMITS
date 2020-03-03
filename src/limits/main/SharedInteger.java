package limits.main;

import limits.util.*;

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
        if (cur % 100 == 0) 
            System.out.println(Arithmetic.count[1] + " "  +  Arithmetic.count[2] + " " + Arithmetic.count[3] + " " + cur);
        return ret;
    }
}
