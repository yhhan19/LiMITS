package limits.data;

import java.util.Vector;

import limits.io.*;
import limits.util.*;

public class Datasets {

    private Dataset[] data;
    private final String[] params;

    public Datasets(String[] params) {
        this.params = params;
        data = new Dataset[params.length];
    }

    private String getWord(int i, int j) {
        Vector<String> param_ = Arithmetic.getWords(params[i], "x");
        return param_.get(j);
    }

    private int getIndex(String folderName) {
        for (int i = 0; i < params.length; i ++) {
            if (folderName.equals(getWord(i, 0))) 
                return i;
        }
        return -1;
    }

    public Dataset getDataset(String folderName) {
        int i = getIndex(folderName);
        if (i == -1) return null;
        if (data[i] == null) {
            synchronized (this) {
                if (data[i] == null) {
                    data[i] = new Dataset(params[i]);
                }
            }
        }
        return data[i];
    }
}
