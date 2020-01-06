import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Vector;

public class MITS {

    private TS[] ts;

    public MITS(TS[] ts) {
        this.ts = ts;
    }

    private void display(double[][] ret) {
        NumberFormat f = Arithmetic.fd;
        System.out.println();
        for (int i = 0; i < ret.length; i ++) {
            System.out.println(ts[i].name() + " " + f.format(ret[i][0]) + " " + f.format(ret[i][1]));
        }
        System.out.println();
    }

    public double[][] testSimData(int cases, int size, int dim, double b0, double b1, int mono, String t0, String t1, double eps) {
        Vector<BigDecimal> e = new Vector<BigDecimal>();
        for (int i = 0; i < dim; i ++) 
            e.add(new BigDecimal(eps));
        double[][] ret = new double[ts.length][2];
        for (int i = 0; i < cases; i ++) {
            System.out.println("case: " + i);
            SeriesKD s = new SeriesKD(size, dim, b0, b1, mono, t0, t1);
            for (int j = 0; j < ts.length; j ++) {
                double[] res = ts[j].evaluateKD(s, e, false);
                ret[j][0] += res[1];
                ret[j][1] += res[0];
                NumberFormat f = Arithmetic.fd;
                System.out.println(f.format(res[1]) + " " + f.format(res[0]) + " " + f.format(res[2]));
            }
        }
        for (int i = 0; i < ts.length; i ++) {
            ret[i][0] /= cases * size;
            ret[i][1] /= cases;
        }
        return ret;
    }

    public double[][] testGeoData(String folderName, double eps) throws Exception {
        FileScanner fs = new FileScanner();
        double[][] ret = fs.listGeoLifeFiles_(ts, folderName, eps);
        return ret;
    }

    public static void main(String[] args) throws Exception {
        TS[] ts = new TS[] {
            //new RDP(), new G1TS(),
            //new DPTS(0), 
            new G2TS(), //new MI1TS("0.5"),
            new MI2TS(10)
        };
        MITS run = new MITS(ts);
        //run.display(run.testSimData(10, 1000, 5, 1e4, 1, 2, "gaussian", "velocity", 1e4));
        run.display(run.testGeoData("./data/", 1));
    }
}
