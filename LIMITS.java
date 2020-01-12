import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Vector;

public class LIMITS extends Thread {

    public static String LIMITS = "L-Inifinity Multidimensional Interpolation Trajectory Simplification (LIMITS)";

    public static Vector<Vector<String>> 
        beijing = Reader.getDataset("beijing", 6), 
        oporto = Reader.getDataset("oporto", 0);

    private TS[] ts;
    private Log log;
    private String name, folderName, scale, type;
    private double eps;

    public LIMITS(TS[] ts, String folderName, String scale, String type, double eps) {
        this.name = (folderName == null ? "simulate" : folderName) + "_" + scale + "_" + type + "_" + eps;
        this.log = new Log(name + ".log");
        this.ts = ts;
        this.folderName = folderName;
        this.scale = scale;
        this.type = type;
        this.eps = eps;
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

    private SeriesKD getSeriesKD(String folderName, int i, int size, int dim, String type) {
        SeriesKD s = null;
        if (folderName.equals("oporto")) {
            if (i == oporto.size()) return null;
            Vector<String> input = oporto.get(i);
            s = new SeriesKD(input, new int[]{2, 0, 1, 3}, " ", size, type);
        }
        else if (folderName.equals("beijing")) {
            if (i == beijing.size()) return null;
            Vector<String> input = beijing.get(i);
            s = new SeriesKD(input, new int[]{4, 0, 1}, ",", size, type);
        }
        else return null;
        if (s.rawDim() != dim) return null;
        return s;
    }

    private Vector<BigDecimal> getEps(String type, double eps, int dim, double cos) {
        Vector<BigDecimal> e = new Vector<BigDecimal>();
        if (type.equals("SPHERE")) {
            e.add(new BigDecimal(eps / Arithmetic.C));
            e.add(new BigDecimal(eps / Arithmetic.C / cos));
            if (dim == 4) e.add(new BigDecimal(eps / Arithmetic.F2M));
        }
        else if (type.equals("EUCLIDEAN")) {
            for (int i = 0; i < dim; i ++) 
                e.add(new BigDecimal(eps));
        }
        return e;
    }

    public void run() {
        System.out.println(name + " start");
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
                Vector<BigDecimal> e = getEps("EUCLIDEAN", eps, dim - 1, 1);
                for (int j = 0; j < ts.length; j ++) {
                    double[] res = ts[j].evaluateKD(s, e, false);
                    results[j][0] += res[1];
                    results[j][1] += res[0];
                    log.write(toString(new double[]{res[1], res[0], res[2]}));
                }
            }
        }
        else {
            for (int i = 0; cases_ <= 0 || cases < cases_; i ++) {
                SeriesKD s = getSeriesKD(folderName, i, size_, dim, type);
                if (s == null) continue;
                log.write("case: " + i + " size: " + s.size() + "\n");
                size += s.size();
                cases ++;
                Vector<BigDecimal> e = getEps(type, eps, dim, Arithmetic.cos(s.min(1)));
                for (int j = 0; j < ts.length; j ++) {
                    double[] res = ts[j].evaluateKD(s, e, dim == 3 && type.equals("SPHERE"));
                    results[j][0] += res[1];
                    results[j][1] += res[0];
                    log.write(toString(new double[]{results[j][0] / size, results[j][1] / cases, res[2]}));
                }
            }
        }
        for (int i = 0; i < ts.length; i ++) {
            results[i][0] /= size;
            results[i][1] /= cases;
        }
        log.write(toString(results, cases, size, dim));
        System.out.println(name + " done");
    }

    public static TS[] ts() {
        return new TS[] {
            new RDP(), 
            new G1TS(), 
            //new DPTS(0), 
            new G2TS(), 
            new M1TS(0.5), 
            new M2TS(10)
        };
    }

    public static void test(String folderName, String scale, String type, String eps) {
        System.out.println("loaded");
        Vector<String> eps_ = Reader.getWords(eps, "x");
        double min = Double.parseDouble(eps_.get(0)), max = Double.parseDouble(eps_.get(1)), steps = Integer.parseInt(eps_.get(2));
        double e = 0;
        for (int i = 0; i < steps; i ++) {
            e += (max - min) / steps;
            (new LIMITS(ts(), folderName, scale, type, e)).start();
        }
    }

    public static void main(String[] args) {
        test("beijing", "10x2000x3", "SPHERE", "0x100x10");
        test("oporto", "10x2000x4", "EUCLIDEAN", "0x100x10");
        test(null, "10x2000x5", "10_2_UNIFORM", "0x100x10");
    }
}
