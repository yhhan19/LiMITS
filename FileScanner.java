import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
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
        int inputSpace = 0;
        int k = 0;
        int count = 0, countLen = 0;
        for (final File fileEntry : folder.listFiles()) {
            if (k < 0) {
                k ++;
                continue;
            }
            System.out.println(k);
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
            if (input.size() > (int) 1e4) {
                count ++;
                countLen += input.size();
                inputSpace += input.size();
                eva.evaluate3D6(input, new BigDecimal("1e-4"), space, time);
                for (int i = 0; i < space.size(); i ++) {
                    totalSpace.set(i, new Long(totalSpace.get(i).intValue() + space.get(i).intValue()));
                    totalTime.set(i, new Long(totalTime.get(i).intValue() + time.get(i).intValue()));
                }
                space.clear();
                time.clear();
                System.out.println(inputSpace);
                for (int i = 0; i < Evaluator.totalMethods; i ++) 
                    System.out.println(totalSpace.get(i) + " " + totalTime.get(i));
            }
            k ++;
        }
        System.out.println(count + " " + countLen);
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
        testSimData();
        testRealData();
    }
}
