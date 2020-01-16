import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.Vector;

public class LIMITS {

    public static final String 
        LIMITS_NAME = "L-Inifinity Multidimensional Interpolation Trajectory Simplification (LIMITS)",
        LOG_FOLDER_NAME = "log";

    public static final Dataset 
        BEIJING = new Dataset("beijing", 6), 
        OPORTO = new Dataset("oporto", 0);

    public static final TS[] 
        ALGORITHMS = new TS[] {
            //new RDP(), 
            new G1TS(), 
            //new DPTS(0), 
            new G2TS(), 
            //new M1TS(0.5), 
            new M2TS(10)
        };

    private static final ExecutorService es = Executors.newFixedThreadPool(10);

    public static Result[] execute(String param, String batch) throws Exception {
        Vector<String> batch_ = Reader.getWords(batch, "x");
        double maxError = Double.parseDouble(batch_.get(0)), e = 0;
        int t0 = Integer.parseInt(batch_.get(1)), t1 = Integer.parseInt(batch_.get(2));
        Task[][] tasks = new Task[t0][t1];
        CountDownLatch[] count = new CountDownLatch[t0];
        Pointer[] pointer = new Pointer[t0];
        for (int i = 0; i < t0; i ++) {
            e += maxError / t0;
            count[i] = new CountDownLatch(t1);
            pointer[i] = new Pointer();
            for (int j = 0; j < t1; j ++) {
                tasks[i][j] = new Task(ALGORITHMS, j + "x" + t1 + "x" + param + "_" + e, count[i], pointer[i]);
                es.execute(tasks[i][j]);
            }
        }
        Result results[] = new Result[tasks.length];
        for (int i = 0; i < tasks.length; i ++) {
            results[i] = new Result(ALGORITHMS);
            count[i].await();
            for (int j = 0; j < tasks[i].length; j ++) {
                results[i].add(tasks[i][j].getResults());
            }
            Log log = new Log(tasks[i][0].superName() + ".res");
            log.write(results[i].toString(results[i].getSize()));
            log.close();
            System.out.println("batch done: " + tasks[i][0].superName());
        }
        return results;
    }

    public static void shutdown() {
        es.shutdown();
    }

    public static void main(String[] args) throws Exception {
        //execute("OPORTO_0x100x4_EUCLIDEAN", "10x2x10");
        execute("BEIJING_0x0x3_SPHERE", "3x3x10");
        //execute("SIM_100x300x5_10x2xUNIFORM", "8x4x10");
        shutdown();
    }
}
