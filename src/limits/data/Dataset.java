package limits.data;

import java.math.BigDecimal;
import java.util.Vector;

import limits.io.*;

public class Dataset {

    private final Vector<Vector<String>> data;
    private final int[] perm;
    private final String token;
    private final String invalid;
    private int range;

    private int[] getPerm(String s) {
        String[] s_ = s.split("/");
        int[] perm = new int[s_.length];
        for (int i = 0; i < s_.length; i ++) {
            perm[i] = Integer.parseInt(s_[i]);
        }
        return perm;
    }

    public Dataset() {
        data = null;
        perm = null;
        token = invalid = null;
        range = 0;
    }

    public Dataset(String folderName_, String param) {
        String[] param_ = param.split("x");
        String folderName = folderName_ + "/" + param_[0];
        int startLine = Integer.parseInt(param_[1]);
        token = param_[2];
        int[] perm_ = getPerm(param_[3]);
        invalid = param_[4];
        range = Integer.parseInt(param_[5]);
        System.out.println("loading: " + folderName);
        if (param_[0].indexOf('.') != -1) {
            int key = perm_[0];
            perm = new int[perm_.length - 1];
            for (int i = 1; i < perm_.length; i ++) 
                perm[i - 1] = perm_[i];
            data = new Vector<Vector<String>>();
            Vector<String> lines = Reader.getLines(folderName, startLine);
            String lastKey = null;
            for (int i = 0, j = 0; i < lines.size(); i ++) {
                String curKey = lines.get(i).split(token)[key];
                if (i > 0 && ! curKey.equals(lastKey)) {
                    data.add(new Vector<String>());
                    for (int k = j; k < i; k ++) 
                        data.lastElement().add(lines.get(k));
                }
                if (i == 0 || ! curKey.equals(lastKey)) {
                    lastKey = curKey;
                    j = i;
                }
            }
        }
        else {
            perm = perm_;
            data = new Vector<Vector<String>>();
            Vector<String> fileNames = Reader.getFiles(folderName);
            for (int i = 0; i < fileNames.size(); i ++) {
                Vector<String> lines = Reader.getLines(fileNames.get(i), startLine);
                data.add(lines);
            }
        }
        System.out.println("loaded: " + folderName);
    }

    public Vector<String> get(int i) {
        return data.get(i);
    }

    public Vector<String> get(int i, int j) {
        String[] input = data.get(i).get(j).split(token);
        Vector<String> output = new Vector<String>();
        for (int k = 0; k < perm.length; k ++) {
            output.add(input[perm[k]]);
        }
        return output;
    }

    public Vector<Vector<String>> getWords(int i) {
        if (data == null) return null;
        Vector<Vector<String>> output = new Vector<Vector<String>>();
        for (int j = 0; j < data.get(i).size(); j ++) {
            output.add(get(i, j));
        }
        return output;
    }

    public BigDecimal getGenRange() {
        return new BigDecimal("1e-" + range);
    }

    public int size() {
        if (data == null) return Integer.MAX_VALUE;
        return data.size();
    }

    public int[] getPerm() {
        return perm;
    }

    public String getToken() {
        return token;
    }

    public String invalid() {
        return invalid;
    }
}
