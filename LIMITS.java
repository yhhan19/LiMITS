import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Vector;

public class LIMITS {

    private TS[] ts;

    public LIMITS(TS[] ts) {
        this.ts = ts;
    }

    public String name() {
        return "L-Inifinity Multidimensional Interpolation Trajectory Simplification (LIMITS)";
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
        return s + "\n";
    }

    public double[][] test(String folderName, String scale, String type, double eps) throws Exception {
        Log log = new Log((folderName == null ? "simulate" : folderName) + "_" + scale + "_" + type + "_" + eps + ".log");
        double[][] results = new double[ts.length][2];
        Vector<String> scale_ = Reader.getWords(scale, "x");
        int cases_ = Integer.parseInt(scale_.get(0)), size_ = Integer.parseInt(scale_.get(1)), dim = Integer.parseInt(scale_.get(2));
        int cases = 0, size = 0;
        if (folderName == null) {
            for (int i = 0; i < cases_; i ++) {
                SeriesKD s = new SeriesKD(size_, dim, type);
                log.write("case: " + i + " size: " + s.size() + "\n");
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
                    log.write(toString(new double[]{res[1], res[0], res[2]}));
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
                if (folderName.equals("beijing")) {
                    Vector<String> input = Reader.getLines(list.get(i), 6, size_);
                    s = new SeriesKD(input, new int[]{4, 0, 1}, ",", type);
                }
                if (s.rawDim() == dim) {
                    log.write("case: " + i + " size: " + s.size() + "\n");
                    size += s.size();
                    cases ++;
                    Vector<BigDecimal> e = new Vector<BigDecimal>();
                    if (type.equals("SPHERE")) {
                        e.add(new BigDecimal(eps / Arithmetic.C));
                        e.add(new BigDecimal(eps / Arithmetic.C / Arithmetic.cos(s.min(1))));
                        if (s.dim() == 4) e.add(new BigDecimal(eps / Arithmetic.F2M));
                    }
                    if (type.equals("EUCLIDEAN")) {
                        for (int j = 0; j < 3; j ++) 
                            e.add(new BigDecimal(eps));
                    }
                    for (int j = 0; j < ts.length; j ++) {
                        double[] res = ts[j].evaluateKD(s, e, dim == 3 && type.equals("SPHERE"));
                        results[j][0] += res[1];
                        results[j][1] += res[0];
                        log.write(toString(new double[]{results[j][0] / size, results[j][1] / cases, res[2]}));
                    }
                }
            }
        }
        for (int i = 0; i < ts.length; i ++) {
            results[i][0] /= size;
            results[i][1] /= cases;
        }
        log.write(toString(results, cases, size, dim));
        return results;
    }

    public static void main(String[] args) throws Exception {
        LIMITS run = new LIMITS(new TS[]{new RDP(), new G1TS(), new DPTS(0), new G2TS(), new M1TS(0.5), new M2TS(10)});
        System.out.println(run.name());
        run.test("beijing", "10x2000x3", "SPHERE", 100);
        run.test("oporto", "10x2000x4", "EUCLIDEAN", 10);
        run.test(null, "5x2000x5", "1e4_2_UNIFORM", 1e4);
    }
}
