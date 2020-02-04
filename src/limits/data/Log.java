package limits.data;

import limits.io.*;

public class Log extends Writer {

    private int len;

    public Log(String folderName, String fileName) {
        super(folderName, fileName);
        len = 0;
    }

    public void add(String s) {
        len += s.length();
        super.write(s);
    }
}
