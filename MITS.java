import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Vector;

public class MITS {

    private TS[] ts;

    public MITS(TS[] ts) {
        this.ts = ts;
    }

    private void display_(double[] ret) {
        NumberFormat f = Arithmetic.fd;
        for (int i = 0; i < ret.length; i ++) 
            System.out.print(f.format(ret[i]) + " ");
        System.out.println();
    }

    private void display(double[][] ret) {
        NumberFormat f = Arithmetic.fd;
        System.out.println();
        for (int i = 0; i < ret.length; i ++) {
            System.out.print(ts[i].name() + " ");
            display_(ret[i]);
        }
        System.out.println();
    }

    public double[][] testSimData(String scale, int dim, String type, double eps) {
        Vector<String> scale_ = FileScanner.getWords(scale, "x");
        int cases = Integer.parseInt(scale_.get(0));
        int size = Integer.parseInt(scale_.get(1));
        Vector<BigDecimal> e = new Vector<BigDecimal>();
        for (int i = 0; i < dim; i ++) 
            e.add(new BigDecimal(eps));
        double[][] ret = new double[ts.length][2];
        for (int i = 0; i < cases; i ++) {
            System.out.println("case: " + i);
            SeriesKD s = new SeriesKD(size, dim, type);
            for (int j = 0; j < ts.length; j ++) {
                double[] res = ts[j].evaluateKD(s, e, false);
                ret[j][0] += res[1];
                ret[j][1] += res[0];
                display_(new double[]{res[1], res[0], res[2]});
            }
        }
        for (int i = 0; i < ts.length; i ++) {
            ret[i][0] /= cases * size;
            ret[i][1] /= cases;
        }
        return ret;
    }

    public double[][] testRealData(String folderName, int dim, String type, double eps) throws Exception {
        Vector<String> list = FileScanner.getFiles(folderName), input = null;
        Vector<BigDecimal> e = new Vector<BigDecimal>();
        double[][] ret = new double[ts.length][2];
        int cases = 0, size = 0;
        for (int i = 0; i < list.size(); i ++) {
            SeriesKD s = null;
            if (folderName.equals("./porto")) {
                input = FileScanner.getLines(list.get(i), 0);
                s = new SeriesKD(input, new int[]{2, 0, 1, 3}, " ", type);
            }
            if (folderName.equals("./beijing")) {
                input = FileScanner.getLines(list.get(i), 6);
                s = new SeriesKD(input, new int[]{4, 0, 1}, ",", type);
            }
            if (s.rawDim() == dim) {
                System.out.println("case: " + i);
                e.clear();
                if (type.equals("sphere")) {
                    e.add(new BigDecimal(eps / Arithmetic.C));
                    e.add(new BigDecimal(eps / Arithmetic.C / Arithmetic.cos(s.min(1))));
                    if (s.dim() == 4) e.add(new BigDecimal(eps / Arithmetic.F2M));
                }
                if (type.equals("euclidean")) {
                    e.add(new BigDecimal(eps));
                    e.add(new BigDecimal(eps));
                    e.add(new BigDecimal(eps));
                }
                size += s.size();
                cases ++;
                for (int j = 0; j < ts.length; j ++) {
                    double[] res = ts[j].evaluateKD(s, e, dim == 3 && type.equals("sphere"));
                    ret[j][0] += res[1];
                    ret[j][1] += res[0];
                    display_(new double[]{ret[j][0] / size, ret[j][1] / cases, res[2]});
                }
            }
        }
        for (int i = 0; i < ts.length; i ++) {
            ret[i][0] /= size;
            ret[i][1] /= cases;
        }
        return ret;
    }

    public static void main(String[] args) throws Exception {
        TS[] ts = new TS[] {
            new RDP(), 
            new G1TS(), 
            new DPTS(0), 
            new G2TS(), 
            new MI1TS("0.5"), 
            new MI2TS(10)
        };
        MITS run = new MITS(ts);
        run.display(run.testSimData("5x2000", 5, "1e4:2:gaussian", 1e4));
        run.display(run.testRealData("./porto", 4, "euclidean", 10));
    }
}
