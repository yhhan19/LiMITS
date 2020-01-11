import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Vector;

public class LIMITS {

    private TS[] ts;

    public LIMITS(TS[] ts) {
        this.ts = ts;
    }

    public String name() {
        return "L-Inifinity Multidimensional Interpolation Trajectory Simplification";
    }

    public void display(double[] result) {
        NumberFormat f = Arithmetic.fd;
        for (int i = 0; i < result.length; i ++) 
            System.out.print(f.format(result[i]) + " ");
        System.out.println();
    }

    public void display(double[][] results) {
        NumberFormat f = Arithmetic.fd;
        System.out.println();
        for (int i = 0; i < results.length; i ++) {
            System.out.print(ts[i].name() + " ");
            display(results[i]);
        }
        System.out.println();
    }

    public double[][] testSimuData(String scale, int dim, String type, double eps) {
        double[][] results = new double[ts.length][2];
        Vector<String> scale_ = FileScanner.getWords(scale, "x");
        int cases = Integer.parseInt(scale_.get(0)), size = Integer.parseInt(scale_.get(1));
        Vector<BigDecimal> e = new Vector<BigDecimal>();
        for (int j = 0; j < dim - 1; j ++) {
            e.add(new BigDecimal(eps));
        }
        for (int i = 0; i < cases; i ++) {
            System.out.println("case: " + i);
            SeriesKD s = new SeriesKD(size, dim, type);
            for (int j = 0; j < ts.length; j ++) {
                double[] res = ts[j].evaluateKD(s, e, false);
                results[j][0] += res[1];
                results[j][1] += res[0];
                display(new double[]{res[1], res[0], res[2]});
            }
        }
        for (int i = 0; i < ts.length; i ++) {
            results[i][0] /= cases * size;
            results[i][1] /= cases;
        }
        return results;
    }

    public double[][] testRealData(String folderName, int dim, String type, double eps) throws Exception {
        double[][] results = new double[ts.length][2];
        Vector<String> list = FileScanner.getFiles(folderName), input = null;
        int cases = 0, size = 0;
        for (int i = 0; i < list.size(); i ++) {
            SeriesKD s = null;
            if (folderName.equals("oporto")) {
                input = FileScanner.getLines(list.get(i), 0);
                s = new SeriesKD(input, new int[]{2, 0, 1, 3}, " ", type);
            }
            if (folderName.equals("beijing")) {
                input = FileScanner.getLines(list.get(i), 6);
                s = new SeriesKD(input, new int[]{4, 0, 1}, ",", type);
            }
            if (s.rawDim() == dim) {
                System.out.println("case: " + i);
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
                size += s.size();
                cases ++;
                for (int j = 0; j < ts.length; j ++) {
                    double[] res = ts[j].evaluateKD(s, e, dim == 3 && type.equals("SPHERE"));
                    results[j][0] += res[1];
                    results[j][1] += res[0];
                    display(new double[]{results[j][0] / size, results[j][1] / cases, res[2]});
                }
            }
        }
        System.out.println("size: " + size + " cases: " + cases);
        for (int i = 0; i < ts.length; i ++) {
            results[i][0] /= size;
            results[i][1] /= cases;
        }
        return results;
    }

    public static void main(String[] args) throws Exception {
        LIMITS run = new LIMITS(new TS[]{new RDP(), new G1TS(), new DPTS(0), new G2TS(), new M1TS(0.5), new M2TS(10)});
        System.out.println(run.name());
        run.display(run.testRealData("beijing", 3, "SPHERE", 100));
        run.display(run.testRealData("oporto", 4, "EUCLIDEAN", 10));
        run.display(run.testSimuData("5x2000", 5, "1e4:2:GAUSSIAN", 1e4));
    }
}
