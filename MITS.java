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

    public double[][] testSimData(int cases, int size, int dim, double bound, int mono, String type, double eps) {
        Vector<BigDecimal> e = new Vector<BigDecimal>();
        for (int i = 0; i < dim; i ++) 
            e.add(new BigDecimal(eps));
        double[][] ret = new double[ts.length][2];
        for (int i = 0; i < cases; i ++) {
            System.out.println("case: " + i);
            SeriesKD s = new SeriesKD(size, dim, bound, mono, type);
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

    public double[][] testRealData(String folderName, int rawDim, String type, double eps) throws Exception {
        FileScanner fs = new FileScanner();
        Vector<String> list = fs.getFiles(folderName), input = null;
        Vector<BigDecimal> e = new Vector<BigDecimal>();
        double[][] ret = new double[ts.length][2];
        int cases = 0, size = 0;
        for (int i = 0; i < list.size(); i ++) {
            System.out.println("case: " + i);
            SeriesKD s = null;
            if (folderName.equals("./porto")) {
                input = fs.getLines(list.get(i), 0);
                s = new SeriesKD(input, new int[]{2, 0, 1, 3}, " ", type);
            }
            if (folderName.equals("./beijing")) {
                input = fs.getLines(list.get(i), 6);
                s = new SeriesKD(input, new int[]{4, 0, 1}, ",", type);
            }
            if (s.rawDim() != rawDim) continue;
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
                double[] res = ts[j].evaluateKD(s, e, rawDim == 3 && type.equals("sphere"));
                ret[j][0] += res[1];
                ret[j][1] += res[0];
                NumberFormat f = Arithmetic.fd;
                System.out.println(f.format(ret[j][0] / (double) size) + " " + f.format(ret[j][1] / (double) cases) + " " + f.format(res[2]));
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
            //new DPTS(0), 
            new G2TS(), 
            //new MI1TS("0.5"), 
            new RDP(), new G1TS(), 
            new MI2TS(10)
        };
        MITS run = new MITS(ts);
        //run.display(run.testSimData(5, 10000, 5, 1e4, 2, "uniform", 1e4));
        run.display(run.testRealData("./porto", 3, "sphere", 10));
    }
}
