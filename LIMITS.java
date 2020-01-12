import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Vector;

public class LIMITS extends Thread {

    public static String LIMITS = "L-Inifinity Multidimensional Interpolation Trajectory Simplification (LIMITS)";

    private TS[] ts;
    private String name, folderName, scale, type;
    private double eps;
    private Log log;

    public LIMITS(String name, TS[] ts, String folderName, String scale, String type, double eps) {
        this.name = name;
        this.ts = ts;
        this.folderName = folderName;
        this.scale = scale;
        this.type = type;
        this.eps = eps;
        this.log = new Log((folderName == null ? "simulate" : folderName) + "_" + scale + "_" + type + "_" + eps + ".log");
    }

    public String toString(double[] result) {
        String s = "";
        NumberFormat f = Arithmetic.fd;
        for (int i = 0; i < result.length; i ++) 
            s = s + f.format(result[i]) + " ";
        return s + "\n";
    }

    public String toString(double[][] results, int cases, int size, int dim) {
        String s = "cases: " + cases + " total size: " + size + " dim: " + dim + "\n";
        NumberFormat f = Arithmetic.fd;
        for (int i = 0; i < results.length; i ++) 
            s = s + ts[i].name() + " " + toString(results[i]);
        return s;
    }

    public void run() {
        double[][] results = new double[ts.length][2];
        Vector<String> scale_ = Reader.getWords(scale, "x");
        int cases_ = Integer.parseInt(scale_.get(0)), size_ = Integer.parseInt(scale_.get(1)), dim = Integer.parseInt(scale_.get(2));
        int cases = 0, size = 0;
        if (folderName == null) {
            for (int i = 0; i < cases_; i ++) {
                SeriesKD s = new SeriesKD(size_, dim, type);
                log.write("case: " + i + " size: " + s.size() + "\n", false);
                size += s.size();
                cases ++;
                Vector<BigDecimal> e = new Vector<BigDecimal>();
                for (int j = 0; j < dim - 1; j ++) {
                    e.add(new BigDecimal(eps));
                }
                for (int j = 0; j < ts.length; j ++) {
                    double[] res = ts[j].evaluateKD(s, e, false);
                    results[j][0] += res[1];
                    results[j][1] += res[0];
                    log.write(toString(new double[]{res[1], res[0], res[2]}), false);
                }
            }
        }
        else {
            Vector<String> list = Reader.getFiles(folderName);
            for (int i = 0; i < list.size() && (cases_ <= 0 || cases < cases_); i ++) {
                SeriesKD s = null;
                if (folderName.equals("oporto")) {
                    Vector<String> input = Reader.getLines(list.get(i), 0, size_);
                    s = new SeriesKD(input, new int[]{2, 0, 1, 3}, " ", type);
                }
                else if (folderName.equals("beijing")) {
                    Vector<String> input = Reader.getLines(list.get(i), 6, size_);
                    s = new SeriesKD(input, new int[]{4, 0, 1}, ",", type);
                }
                if (s.rawDim() == dim) {
                    log.write("case: " + i + " size: " + s.size() + "\n", false);
                    size += s.size();
                    cases ++;
                    Vector<BigDecimal> e = new Vector<BigDecimal>();
                    if (type.equals("SPHERE")) {
                        e.add(new BigDecimal(eps / Arithmetic.C));
                        e.add(new BigDecimal(eps / Arithmetic.C / Arithmetic.cos(s.min(1))));
                        if (s.dim() == 4) e.add(new BigDecimal(eps / Arithmetic.F2M));
                    }
                    else if (type.equals("EUCLIDEAN")) {
                        for (int j = 0; j < 3; j ++) 
                            e.add(new BigDecimal(eps));
                    }
                    for (int j = 0; j < ts.length; j ++) {
                        double[] res = ts[j].evaluateKD(s, e, dim == 3 && type.equals("SPHERE"));
                        results[j][0] += res[1];
                        results[j][1] += res[0];
                        log.write(toString(new double[]{results[j][0] / size, results[j][1] / cases, res[2]}), false);
                    }
                }
            }
        }
        for (int i = 0; i < ts.length; i ++) {
            results[i][0] /= size;
            results[i][1] /= cases;
        }
        log.write(toString(results, cases, size, dim), false);
    }

    public static TS[] ts() {
        return new TS[]{new RDP(), new G1TS(), new DPTS(0), new G2TS(), new M1TS(0.5), new M2TS(10)};
    }

    public static void main(String[] args) {
        for (int i = 10; i <= 100; i += 10) {
            (new LIMITS(i + " ", ts(), "beijing", "100x2000x3", "SPHERE", i)).start();
        }
        //(new LIMITS("1", ts(), "oporto", "100x2000x4", "EUCLIDEAN", 10)).start();
        //(new LIMITS("2", ts(), null, "100x2000x5", "1e4_2_UNIFORM", 1e4)).start();
    }
}
