import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.Vector;

public class Task extends Thread {

    private TS[] ts;
    private Log log;
    private Result results;
    private Dataset data;
    private int cases, size, dim;
    private String param, superName, folderName, type;
    private double eps;
    private CountDownLatch count;
    private Pointer pointer;

    public Task(TS[] ts, String param, CountDownLatch count, Pointer pointer) {
        this.ts = ts;
        this.param = param;
        this.count = count;
        this.pointer = pointer;
        log = new Log(param + ".log");
        results = new Result(ts);
        Vector<String> p = Reader.getWords(param, "_"), name = Reader.getWords(p.get(0), "x"),  scale = Reader.getWords(p.get(1), "x");
        superName = name.get(2) + "_" + p.get(1) + "_" + p.get(2) + "_" + p.get(3);
        folderName = name.get(2).equals("SIM") ? null : name.get(2);
        data = folderName == null ? null : LIMITS.DATASETS.getDataset(folderName);
        cases = Integer.parseInt(scale.get(0));
        size = Integer.parseInt(scale.get(1));
        dim = Integer.parseInt(scale.get(2));
        type = p.get(2);
        eps = Double.parseDouble(p.get(3));
    }

    public String superName() {
        return superName;
    }

    public CountDownLatch getCount() {
        return count;
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
            for (int i = pointer.next(); i < cases; i = pointer.next()) {
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
                SeriesKD s = new SeriesKD(data, i, size, type);
                if (s.rawDim() != dim) continue;
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
        System.out.println("task start: " + param);
        test();
        log.close();
        count.countDown();
        System.out.println("task done: " + param);
    }
}
