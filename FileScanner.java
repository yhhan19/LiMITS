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
        Vector<Point3D> points = new Vector<Point3D>();
        Vector<Long> space = new Vector<Long>(), time = new Vector<Long>(), totalSpace = new Vector<Long>(), totalTime = new Vector<Long>();
        Evaluator eva = new Evaluator();
        for (int i = 0; i < Evaluator.totalMethods; i ++) {
            totalSpace.add((long) 0);
            totalTime.add((long) 0);
        }
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
            }
            for (int i = 0; i < x.size(); i ++) {
                if (i > 0 && Arithmetic.sgn(x.get(i).subtract(x.get(i - 1))) <= 0) 
                    continue;
                BigDecimal dx = x.get(i).subtract(x.get(0));
                BigDecimal dy = y.get(i).subtract(y.get(0));
                BigDecimal dz = z.get(i).subtract(z.get(0));
                Point3D p = new Point3D(dx.add(Arithmetic.epsRand()), dy.add(Arithmetic.epsRand()), dz.add(Arithmetic.epsRand()));
                points.add(p);
            }
            x.clear();
            y.clear();
            z.clear();
            Series3D input = new Series3D(points);
            points.clear();
            inputCount ++;
            inputSpace += input.size();
            System.out.println(inputCount + " " + inputSpace);
            if (input.size() <= (int) 1e9) {
                processCount ++;
                processSpace += input.size();
                System.out.println(processCount + " " + processSpace);
                eva.evaluate3D4(input, new BigDecimal("1e-4"), space, time);
                for (int i = 0; i < space.size(); i ++) {
                    totalSpace.set(i,  totalSpace.get(i).longValue() + space.get(i).longValue());
                    totalTime.set(i, totalTime.get(i).longValue() + time.get(i).longValue());
                }
                space.clear();
                time.clear();
                for (int i = 0; i < Evaluator.totalMethods; i ++) {
                    NumberFormat formatter = new DecimalFormat("0.000000");
                    String spatial = formatter.format(totalSpace.get(i) / (double) processSpace);
                    String temporal = formatter.format(totalTime.get(i) / (double) processCount);
                    System.out.println(spatial  + " " + temporal);
                }
            }
        }
    }

    public static void testSimData() {
        Evaluator eva = new Evaluator();
        eva.evaluate3D3();
    }

    public static void testRealData() throws Exception {
        String folderName = "./data/";
        FileScanner fs = new FileScanner();
        fs.listFilesForFolder(folderName);
    }

    public static void main(String[] args) throws Exception {
        //testSimData();
        //testRealData();
        Evaluator eva = new Evaluator();
        Series3D s_ = new Series3D(10000, (int) 1e9, 2);
        SeriesKD s = new SeriesKD(10000, 3, (int) 1e9, 2);
        BigDecimal eps = new BigDecimal((int) 1e9);
        EvaluatorKD ekd = new EvaluatorKD();
        System.out.println();

        Series3D t_ = eva.re3finedCombineSimplify3D(s_, eps, 10);
        System.out.println(s_.size() + " " + t_.size());
        t_.display();
        s_.distance(t_).display();
        System.out.println();
        
        t_ = eva.greedy2Simplify3D(s_, eps);
        System.out.println(s_.size() + " " + t_.size());
        t_.display();
        s_.distance(t_).display();
        System.out.println();
        System.out.println();

        SeriesKD t = ekd.greedySimplify(s, eps);
        System.out.println(s.size() + " " + t.size());
        System.out.println(Arithmetic.format(s.distanceLoo(t)));
        t = ekd.greedy2Simplify(s, eps);
        System.out.println(s.size() + " " + t.size());
        System.out.println(Arithmetic.format(s.distanceLoo(t)));
        t = ekd.refinedCombineSimplify(s, eps, new BigDecimal("0.5"));
        System.out.println(s.size() + " " + t.size());
        System.out.println(Arithmetic.format(s.distanceLoo(t)));
    }
}
