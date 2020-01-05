import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.StringTokenizer;
import java.util.Vector;

public class FileScanner {
    
    public double[][] listGeoLifeFiles(TS[] ts, String folderName, double meters) throws Exception {
        File folder = new File("./data/");
        double[][] ret = new double[ts.length][2];
        Vector<BigDecimal> x = new Vector<BigDecimal>(), y = new Vector<BigDecimal>(), z = new Vector<BigDecimal>(), eps = new Vector<BigDecimal>();
        Vector<PointKD> points = new Vector<PointKD>();
        double[] space = new double[ts.length], time = new double[ts.length];
        int inputSpace = 0, inputCount = 0, processSpace = 0, processCount = 0;
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) continue;
            String fileName = folderName + fileEntry.getName(), s = null;
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            BigDecimal min = new BigDecimal("360");
            for (int i = 0; (s = br.readLine()) != null; i ++) {
                if (i < 6) continue;
                StringTokenizer st = new StringTokenizer(s, ",");
                String lat = null, lon = null, stamp = null;
                for (int j = 0; st.hasMoreTokens(); j ++) {
                    switch (j) {
                        case 0: lat = st.nextToken(); break;
                        case 1: lon = st.nextToken(); break;
                        case 4: stamp = st.nextToken(); break;
                        default: st.nextToken(); break;
                    }
                }
                x.add(new BigDecimal(stamp));
                y.add(new BigDecimal(lat));
                z.add(new BigDecimal(lon));
                min = min.min(y.lastElement());
            }
            BigDecimal e0 = new BigDecimal(meters / Arithmetic.C);
            BigDecimal e1 = new BigDecimal(meters / Arithmetic.C / Arithmetic.cos(min));
            eps.clear();
            eps.add(e0); eps.add(e1);
            Vector<BigDecimal> data = new Vector<BigDecimal>();
            for (int i = 0; i < x.size(); i ++) {
                if (i > 0 && Arithmetic.sgn(x.get(i).subtract(x.get(i - 1))) <= 0) 
                    continue;
                BigDecimal dx = x.get(i).subtract(x.get(0)).add(Arithmetic.epsRand());
                BigDecimal dy = y.get(i).add(Arithmetic.epsRand());
                BigDecimal dz = z.get(i).add(Arithmetic.epsRand());
                data.clear();
                data.add(dx); data.add(dy); data.add(dz);
                points.add(new PointKD(data));
            }
            x.clear(); y.clear(); z.clear();
            SeriesKD input = new SeriesKD(points);
            points.clear();
            inputCount ++;
            inputSpace += input.size();
            if (input.size() <= (int) 1e9) {
                processCount ++;
                processSpace += input.size();
                System.out.println("case: " + processCount + "/" + inputCount + " " + processSpace + "/" + inputSpace);
                for (int i = 0; i < ts.length; i ++) {
                    double[] res = ts[i].evaluateKD(input, eps, true);
                    ret[i][1] += res[0];
                    ret[i][0] += res[1];
                    NumberFormat f = Arithmetic.fd;
                    System.out.println(f.format(ret[i][0] / (double) processSpace) + " " + f.format(ret[i][1] / (double) processCount) + " " + f.format(res[2]));
                }
            }
        }
        for (int i = 0; i < ts.length; i ++) {
            ret[i][0] /= (double) processSpace;
            ret[i][1] /= (double) processCount;
        }
        return ret;
    }
}
