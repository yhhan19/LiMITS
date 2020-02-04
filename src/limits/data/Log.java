package limits.data;

import limits.io.*;

public class Log extends Writer {

    private String log;

    public Log(String folderName, String fileName) {
        super(folderName, fileName);
        log = "";
    }

    public void add(String s) {
        log += s;
        super.write(s);
    }
}
