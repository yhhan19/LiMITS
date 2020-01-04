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

    public double[][] testSimData(int M, int N, int K, double eps) {
        Vector<BigDecimal> e = new Vector<BigDecimal>();
        for (int i = 0; i < K; i ++) 
            e.add(new BigDecimal(eps));
        double[][] ret = new double[ts.length][2];
        for (int i = 0; i < M; i ++) {
            System.out.println("case: " + i);
            SeriesKD s = new SeriesKD(N, K, (int) 1e8, 2);
            for (int j = 0; j < ts.length; j ++) {
                double[] res = ts[j].evaluateKD(s, e, false);
                ret[j][0] += res[1];
                ret[j][1] += res[0];
                NumberFormat f = Arithmetic.fd;
                System.out.println(f.format(res[1]) + " " + f.format(res[0]) + " " + f.format(res[2]));
            }
        }
        for (int i = 0; i < ts.length; i ++) {
            ret[i][0] /= M * N;
            ret[i][1] /= M;
        }
        return ret;
    }

    public double[][] testGeoData(String folderName, double eps) throws Exception {
        FileScanner fs = new FileScanner();
        double[][] ret = fs.listGeoLifeFiles(ts, folderName, eps);
        return ret;
    }

    public static void main(String[] args) throws Exception {
        TS[] ts = new TS[] {
            new RDP(), new G1TS(), new DPTS(0), 
            new G2TS(), new MI1TS("0.5"), new MI2TS(10)
        };
        MITS run = new MITS(ts);
        run.display(run.testSimData(10, 2000, 5, 1e8));
        run.display(run.testGeoData("./data/", 100));
    }
}
