import java.text.NumberFormat;

public class Result {

    private static final NumberFormat f = Arithmetic.DOUBLE_FORMAT;

    public static final int LEN = 3;

    private TS[] ts;
    private double[][] results;
    private int cases, size;
    private double eps;

    public Result(TS[] ts, double eps) {
        this.ts = ts;
        this.results = new double[ts.length][LEN];
        cases = size = 0;
        this.eps = eps;
    }

    public Result(TS[] ts, double[][] results, double eps, int size, int cases) {
        this.ts = ts;
        this.results = new double[ts.length][LEN];
        for (int i = 0; i < ts.length; i ++) 
            for (int j = 0; j < LEN; j ++) 
                this.results[i][j] = results[i][j];
        this.size = size;
        this.cases = cases;
        this.eps = eps;
    }

    public static String toString(double[] result, int dom) {
        String s = "";
        for (int i = 0; i < LEN - 1; i ++) 
            s = s + f.format(result[i] / dom) + " ";
        return s + f.format(result[LEN - 1]) + "\n";
    }

    public String toString(int dom) {
        String s = "error: " + eps + " cases: " + cases + " total size: " + size + "\n";
        for (int i = 0; i < ts.length; i ++) 
            s = s + ts[i].name() + " " + toString(results[i], dom);
        return s;
    }

    public void add(int i, double[] result) {
        for (int j = 0; j < LEN - 1; j ++) 
            results[i][j] += result[j];
        if (results[i][LEN - 1] < result[LEN - 1]) 
            results[i][LEN - 1] = result[LEN - 1];
    }

    public void add(Result that) {
        for (int i = 0; i < ts.length; i ++) {
            for (int j = 0; j < LEN - 1; j ++) 
                results[i][j] += that.results[i][j];
            if (results[i][LEN - 1] < that.results[i][LEN - 1]) 
                results[i][LEN - 1] = that.results[i][LEN - 1];
        }
        size += that.size;
        cases += that.cases;
    }

    public int getCases() {
        return cases;
    }

    public int getSize() {
        return size;
    }

    public double getError() {
        return eps;
    }

    public int getRaws() {
        return ts.length;
    }

    public double getData(int i, int j) {
        return results[i][j];
    }
}
