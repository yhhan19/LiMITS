import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.StringTokenizer;
import java.util.Vector;

public class FileScanner {
    
    public void listFilesForFolder(TS[] ts, String folderName, double meters) throws Exception {
        File folder = new File("./data/");
        Vector<BigDecimal> x = new Vector<BigDecimal>(), y = new Vector<BigDecimal>(), z = new Vector<BigDecimal>();
        Vector<PointKD> points = new Vector<PointKD>();
        Vector<BigDecimal> eps = new Vector<BigDecimal>();
        double[] space = new double[ts.length], time = new double[ts.length];
        int total = 0, inputSpace = 0, inputCount = 0, processSpace = 0, processCount = 0;
        for (final File fileEntry : folder.listFiles()) {
            if (inputCount < 0) {
                inputCount ++;
                continue;
            }
            if (fileEntry.isDirectory()) continue;
            String fileName = folderName + fileEntry.getName(), s = null;
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            BigDecimal ymax = new BigDecimal("0");
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
                ymax = ymax.max(y.lastElement());
            }
            double cosy = Math.cos(Math.toRadians(ymax.doubleValue()));
            BigDecimal e0 = new BigDecimal(meters / (cosy * 111320));
            BigDecimal e1 = new BigDecimal(meters / 111320);
            eps.clear();
            eps.add(e0); eps.add(e1);
            Vector<BigDecimal> data = new Vector<BigDecimal>();
            for (int i = 0; i < x.size(); i ++) {
                if (i > 0 && Arithmetic.sgn(x.get(i).subtract(x.get(i - 1))) <= 0) 
                    continue;
                BigDecimal dx = x.get(i).subtract(x.get(0)).add(Arithmetic.epsRand());
                BigDecimal dy = y.get(i).subtract(y.get(0)).add(Arithmetic.epsRand());
                BigDecimal dz = z.get(i).subtract(z.get(0)).add(Arithmetic.epsRand());
                data.clear();
                data.add(dx); data.add(dy); data.add(dz);
                points.add(new PointKD(data));
            }
            x.clear();
            y.clear();
            z.clear();
            SeriesKD input = new SeriesKD(points);
            points.clear();
            inputCount ++;
            inputSpace += input.size();
            System.out.println(inputCount + " " + inputSpace);
            if (input.size() <= (int) 1e9) {
                processCount ++;
                processSpace += input.size();
                System.out.println(processCount + " " + processSpace);
                double[] res = new double[3];
                for (int i = 0; i < ts.length; i ++) {
                    res = ts[i].evaluateKD(input, eps);
                    time[i] += res[0];
                    space[i] += res[1];
                    NumberFormat formatter = new DecimalFormat("0.00000E0");
                    String spatial = formatter.format(space[i] / (double) processSpace);
                    String temporal = formatter.format(time[i] / (double) processCount);
                    String error = formatter.format(res[2]);
                    System.out.println(spatial  + " " + temporal);
                }
            }
        }
        System.out.println(total);
    }
}
