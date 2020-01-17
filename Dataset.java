import java.util.Vector;

public class Dataset {

    private final Vector<Vector<String>> data;
    private final int[] perm;
    private final String token;
    private final String invalid;

    private int[] getPerm(String s) {
        Vector<String> s_ = Reader.getWords(s, "|");
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
        perm = getPerm(param_.get(3));
        invalid = param_.size() >= 4 ? param_.get(4) : null;
        System.out.println("loading: " + folderName);
        Vector<String> fileNames = Reader.getFiles(folderName);
        data = new Vector<Vector<String>>();
        for (int i = 0; i < fileNames.size(); i ++) {
            Vector<String> lines = Reader.getLines(fileNames.get(i), startLine);
            data.add(lines);
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
