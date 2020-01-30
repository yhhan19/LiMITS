import java.math.BigDecimal;
import java.util.Vector;

public class Dataset {

    private final Vector<Vector<String>> data;
    private final int[] perm;
    private final String token;
    private final String invalid;
    private int range;

    private int[] getPerm(String s) {
        Vector<String> s_ = Reader.getWords(s, "/");
        int[] perm = new int[s_.size()];
        for (int i = 0; i < s_.size(); i ++) {
            perm[i] = Integer.parseInt(s_.get(i));
        }
        return perm;
    }

    public Dataset(String param) {
        Vector<String> param_ = Reader.getWords(param, "x");
        String folderName = LIMITS.DATA_FOLDER_NAME + "/" + param_.get(0);
        int startLine = Integer.parseInt(param_.get(1));
        token = param_.get(2);
        int[] perm_ = getPerm(param_.get(3));
        invalid = param_.get(4);
        range = Integer.parseInt(param_.get(5));
        System.out.println("loading: " + folderName);
        if (param_.get(0).indexOf('.') != -1) {
            int key = perm_[0];
            perm = new int[perm_.length - 1];
            for (int i = 1; i < perm_.length; i ++) 
                perm[i - 1] = perm_[i];
            data = new Vector<Vector<String>>();
            Vector<String> lines = Reader.getLines(folderName, startLine);
            String lastKey = null;
            for (int i = 0, j = 0; i < lines.size(); i ++) {
                String curKey = Reader.getWords(lines.get(i), token).get(key);
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
        Vector<String> input = Reader.getWords(data.get(i).get(j), token), output = new Vector<String>();
        for (int k = 0; k < perm.length; k ++) {
            output.add(input.get(perm[k]));
        }
        return output;
    }

    public BigDecimal getGenRange() {
        return new BigDecimal("1e-" + range);
    }

    public int size() {
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
