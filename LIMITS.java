import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.Vector;

public class LIMITS {

    public static String NAME = "L-Inifinity Multidimensional Interpolation Trajectory Simplification (LIMITS)";

    public static Vector<Vector<String>> 
        BEIJING = Reader.getDataset("beijing", 6), 
        OPORTO = Reader.getDataset("oporto", 0);
    
    public static ExecutorService es = Executors.newFixedThreadPool(10);

    public static Task[][] tasks;

    public static TS[] ts() {
        return new TS[] {
            new RDP(), 
            new G1TS(), 
            new DPTS(0), 
            new G2TS(), 
            new M1TS(0.5), 
            new M2TS(10)
        };
    }

    public static Result[] start(String param, String div) throws Exception {
        Vector<String> div_ = Reader.getWords(div, "x");
        double maxError = Double.parseDouble(div_.get(0)), e = 0;
        int t0 = Integer.parseInt(div_.get(1)), t1 = Integer.parseInt(div_.get(2));
        Task[][] tasks = new Task[t0][t1];
        CountDownLatch[] count = new CountDownLatch[t0];
        for (int i = 0; i < t0; i ++) {
            e += maxError / t0;
            count[i] = new CountDownLatch(t1);
            for (int j = 0; j < t1; j ++) {
                tasks[i][j] = new Task(ts(), j + "x" + t1 + "x" + param + "_" + e, count[i]);
                es.execute(tasks[i][j]);
            }
        }
        Result results[] = new Result[tasks.length];
        for (int i = 0; i < tasks.length; i ++) {
            results[i] = new Result(ts());
            count[i].await();
            for (int j = 0; j < tasks[i].length; j ++) {
                results[i].add(tasks[i][j].getResults());
            }
            Log log = new Log(tasks[i][0].superName() + ".res");
            log.write(results[i].toString(results[i].getSize()));
            System.out.println("batch done: " + tasks[i][0].superName());
        }
        return results;
    }

    public static void shutdown() {
        es.shutdown();
    }

    public static void main(String[] args) throws Exception {
        start("OPORTO_5x0x4_EUCLIDEAN", "100x10x10");
        start("BEIJING_0x2000x3_SPHERE", "100x10x10");
        start("SIM_10x2000x5_10x2xUNIFORM", "100x10x10");
        shutdown();
    }
}
