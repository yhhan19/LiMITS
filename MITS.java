import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Vector;

public class MITS {

    public void testSimData(TS[] ts, SeriesKD s, double eps) {
        Vector<BigDecimal> e = new Vector<BigDecimal>();
        for (int i = 0; i < s.dim(); i ++) 
            e.add(new BigDecimal(eps));
        NumberFormat formatter = new DecimalFormat("0.00000E0");
        double[] res = new double[3];
        for (int i = 0; i < ts.length; i ++) {
            res = ts[i].evaluateKD(s, e, false);
            System.out.println(formatter.format(res[1]) + " " + formatter.format(res[0]) + " " + formatter.format(res[2]));
        }
    }

    public void testRealData(TS[] ts, String folderName, double eps) throws Exception {
        FileScanner fs = new FileScanner();
        fs.listFilesForFolder(ts, folderName, eps);
    }

    public static void main(String[] args) throws Exception {
        MITS runtime = new MITS();
        TS[] ts = new TS[] {new RDP(), new G1TS(), new DPTS(0), new G2TS(), new MI1TS(new BigDecimal("0.5")), new MI2TS(10)};
        runtime.testSimData(ts, new SeriesKD(2000, 5, (int) 1e8, 2), 1e8);
        runtime.testRealData(ts, "./data/", 50);
    }
}
