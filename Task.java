import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.Vector;

public class Task extends Thread {

    private TS[] ts;
    private Log log;
    private Result results;
    private Dataset data;
    private int cases, size, dim, part, total;
    private String param, folderName, type, superName;
    private double eps;
    private CountDownLatch count;
    private Pointer pointer;

    public Task(TS[] ts, String param, CountDownLatch count, Pointer pointer) {
        this.ts = ts;
        this.param = param;
        log = new Log(param + ".log");
        results = new Result(ts);
        Vector<String> p = Reader.getWords(param, "_");
        Vector<String> name = Reader.getWords(p.get(0), "x");
        if (name.get(2).equals("SIM")) {
            part = Integer.parseInt(name.get(0));
            total = Integer.parseInt(name.get(1));
            folderName = null;
            data = null;
            superName = "SIM";
        }
        else {
            part = Integer.parseInt(name.get(0));
            total = Integer.parseInt(name.get(1));
            folderName = name.get(2);
            if (folderName.equals("BEIJING")) 
                data = LIMITS.BEIJING;
            if (folderName.equals("OPORTO")) 
                data = LIMITS.OPORTO;
            superName = folderName;
        }
        Vector<String> scale = Reader.getWords(p.get(1), "x");
        cases = Integer.parseInt(scale.get(0));
        size = Integer.parseInt(scale.get(1));
        dim = Integer.parseInt(scale.get(2));
        type = p.get(2);
        eps = Double.parseDouble(p.get(3));
        superName += "_" + p.get(1) + "_" + p.get(2) + "_" + p.get(3);
        this.count = count;
        this.pointer = pointer;
    }

    public String superName() {
        return superName;
    }

    private SeriesKD getSeriesKD(int i) {
        SeriesKD s = null;
        Vector<String> input = data.get(i);
        if (folderName.equals("OPORTO")) 
            s = new SeriesKD(input, new int[]{2, 0, 1, 3}, " ", size, type);
        else if (folderName.equals("BEIJING")) 
            s = new SeriesKD(input, new int[]{4, 0, 1}, ",", size, type);
        if (s.rawDim() != dim) return null;
        return s;
    }

    private Vector<BigDecimal> getError(double cos) {
        Vector<BigDecimal> e = new Vector<BigDecimal>();
        if (type.equals("SPHERE")) {
            e.add(new BigDecimal(eps / Arithmetic.METERS_PER_LON));
            e.add(new BigDecimal(eps / Arithmetic.METERS_PER_LON / cos));
            if (dim == 4) e.add(new BigDecimal(eps / Arithmetic.F2M));
        }
        else if (type.equals("EUCLIDEAN")) {
            for (int i = 0; i < dim - 1; i ++) 
                e.add(new BigDecimal(eps));
        }
        else {
            for (int i = 0; i < dim - 1; i ++) 
                e.add(new BigDecimal(eps));
        }
        return e;
    }

    public void test() {
        if (folderName == null) {
            for (int i = 0; i < cases; i ++) {
                SeriesKD s = new SeriesKD(size, dim, type);
                log.write("case: " + i + " size: " + s.size() + "\n");
                Vector<BigDecimal> e = getError(1);
                double[][] res = new double[ts.length][];
                for (int j = 0; j < ts.length; j ++) {
                    res[j] = ts[j].evaluateKD(s, e, false);
                    log.write(Result.toString(res[j], 1));
                }
                results.add(new Result(ts, res, s.size(), 1));
            }
        }
        else {
            for (int i = pointer.next(); i < data.size() && (cases <= 0 || i < cases); i = pointer.next()) {
                SeriesKD s = getSeriesKD(i);
                if (s == null) continue;
                log.write("case: " + i + " size: " + s.size() + "\n");
                Vector<BigDecimal> e = getError(Arithmetic.cos(s.min(1)));
                double[][] res = new double[ts.length][];
                for (int j = 0; j < ts.length; j ++) {
                    res[j] = ts[j].evaluateKD(s, e, dim == 3 && type.equals("SPHERE"));
                    log.write(Result.toString(res[j], 1));
                }
                results.add(new Result(ts, res, s.size(), 1));
            }
        }
        log.write(results.toString(results.getSize()));
    }

    public Result getResults() {
        return results;
    }

    public void run() {
        System.out.println("start: " + param);
        test();
        log.close();
        count.countDown();
        System.out.println("done: " + param);
    }
}
