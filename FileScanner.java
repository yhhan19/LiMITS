import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.StringTokenizer;
import java.util.Vector;

public class FileScanner {
    
    public void listFilesForFolder(String folderName) throws Exception {
        File folder = new File("./data/");
        Vector<BigDecimal> x = new Vector<BigDecimal>(), y = new Vector<BigDecimal>(), z = new Vector<BigDecimal>();
        Vector<PointKD> points = new Vector<PointKD>();
        Vector<Long> space = new Vector<Long>(), time = new Vector<Long>(), totalSpace = new Vector<Long>(), totalTime = new Vector<Long>();
        EvaluatorKD ekd = new EvaluatorKD();
        Vector<BigDecimal> eps = new Vector<BigDecimal>();
        for (int i = 0; i < Evaluator.totalMethods; i ++) {
            totalSpace.add((long) 0);
            totalTime.add((long) 0);
        }
        int total = 0;
        int inputSpace = 0, inputCount = 0, processSpace = 0, processCount = 0;
        for (final File fileEntry : folder.listFiles()) {
            if (inputCount < 0) {
                inputCount ++;
                continue;
            }
            if (fileEntry.isDirectory()) continue;
            String fileName = folderName + fileEntry.getName();
            BufferedReader br = new BufferedReader(new FileReader(fileName)); 
            String s;
            BigDecimal ymin = new BigDecimal("180"), ymax = new BigDecimal("0"), zmin = new BigDecimal("180"), zmax = new BigDecimal("0");
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
                ymin = ymin.min(y.lastElement()); ymax = ymax.max(y.lastElement());
                zmin = zmin.min(z.lastElement()); zmax = zmax.max(z.lastElement());
            }
            BigDecimal ry = ymax.subtract(ymin), rz = zmax.subtract(zmax);
            double cosy = Math.cos(Math.toRadians(ymax.doubleValue()));
            double meters = 50;
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
                ekd.evaluate(input, eps, space, time);
                for (int i = 0; i < space.size(); i ++) {
                    totalSpace.set(i,  totalSpace.get(i).longValue() + space.get(i).longValue());
                    totalTime.set(i, totalTime.get(i).longValue() + time.get(i).longValue());
                }
                space.clear();
                time.clear();
                for (int i = 0; i < EvaluatorKD.totalMethods; i ++) {
                    NumberFormat formatter = new DecimalFormat("0.000000");
                    String spatial = formatter.format(totalSpace.get(i) / (double) processSpace);
                    String temporal = formatter.format(totalTime.get(i) / (double) processCount);
                    System.out.println(spatial  + " " + temporal);
                }
            }
        }
        System.out.println(total);
    }

    public static void testSimData() {
        SeriesKD s = new SeriesKD(5000, 5, (int) 1e8, 2);
        Vector<BigDecimal> eps = new Vector<BigDecimal>();
        eps.add(new BigDecimal((int) 1e7));
        eps.add(new BigDecimal((int) 1e7));
        EvaluatorKD ekd = new EvaluatorKD();
        SeriesKD t = ekd.greedySimplify(s, eps);
        System.out.println(t.size() + " " + Arithmetic.format(s.distanceLoo(t)));
        t = ekd.greedy2Simplify(s, eps);
        System.out.println(t.size() + " " + Arithmetic.format(s.distanceLoo(t)));
        t = ekd.refinedCombineSimplify(s, eps, new BigDecimal("0.5"));
        System.out.println(t.size() + " " + Arithmetic.format(s.distanceLoo(t)));
        t = ekd.refinedCombineSimplify(s, eps, 10);
        System.out.println(t.size() + " " + Arithmetic.format(s.distanceLoo(t)));
    }

    public static void testRealData() throws Exception {
        String folderName = "./data/";
        FileScanner fs = new FileScanner();
        fs.listFilesForFolder(folderName);
    }

    public static void main(String[] args) throws Exception {
        //testSimData();
        testRealData();
    }
}
