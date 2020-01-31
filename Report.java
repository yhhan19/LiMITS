import java.text.NumberFormat;
import java.util.Vector;

public class Report {

    private static final NumberFormat f = Arithmetic.DOUBLE_FORMAT_;

    private Result[] results;
    private String fileName;

    private TS search(String name) {
        for (int i = 0; i < LIMITS.ALGORITHMS.length; i ++) 
            if (LIMITS.ALGORITHMS[i].name().equals(name)) 
                return LIMITS.ALGORITHMS[i];
        return null;
    }

    public Report(String folderName, String fileName) {
        this.fileName = fileName;
        Vector<Result> temp = new Vector<Result>();
        Vector<String> lines = Reader.getLines(folderName + "/" + fileName, 0);
        for (int i = lines.size() - 1, j = lines.size(); i >= 0; i --) {
            Vector<String> words = Reader.getWords(lines.get(i), " ");
            if (words.get(0).equals("error:")) {
                double eps = Double.parseDouble(words.get(1));
                int cases = Integer.parseInt(words.get(3)), size = Integer.parseInt(words.get(6));
                TS[] ts = new TS[j - i - 1];
                double[][] res = new double[j - i - 1][3];
                for (int k = i + 1; k < j; k ++) {
                    Vector<String> words_ = Reader.getWords(lines.get(k), " ");
                    ts[k - i - 1] = search(words_.get(0));
                    res[k - i - 1][0] = Double.parseDouble(words_.get(1));
                    res[k - i - 1][1] = Double.parseDouble(words_.get(2));
                    res[k - i - 1][2] = Double.parseDouble(words_.get(3));
                }
                temp.add(new Result(ts, res, eps, size, cases));
                j = i;
            }
        }
        results = new Result[temp.size()];
        for (int i = temp.size() - 1, j = 0; i >= 0; i --, j ++) 
            results[j] = temp.get(i);
    }

    public String toString(int x, int y) {
        String series = "{";
        for (int i = 0; i < results.length; i ++) {
            series += "{" + f.format(results[i].getError()) + ", " + f.format(results[i].getData(x, y) / results[i].getData(0, y)) + "}";
            series += (i == results.length - 1) ? "}" : ", ";
        }
        return series;
    }

    public String toString(int y) {
        String series = "data = {";
        for (int i = 0; i < results[0].getRaws(); i ++) {
            series += toString(i, y);
            series += (i == results[0].getRaws() - 1) ? "};\n" : ", ";
        }
        return series;
    }

    public void toCommands() {
        Log commands = new Log(LIMITS.FINAL_FOLDER_NAME, fileName);
        commands.write(toString(0) + toString(1));
        commands.close();
    }
}
