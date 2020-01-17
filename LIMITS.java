import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.Vector;

public class LIMITS {

    public static final String 
        LIMITS_NAME = "L-Inifinity Multidimensional Interpolation Trajectory Simplification (LIMITS)", 
        DATA_FOLDER_NAME = "../data", 
        LOG_FOLDER_NAME = "../log";

    public static final Datasets 
        DATASETS = new Datasets(new String[] {
            "BEIJINGx6x,x4|0|1", 
            "OPORTOx0x x2|0|1|3x-1.0"
        });

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

    public static Task[][] execute(String param, String batch) throws Exception {
        Vector<String> batch_ = Reader.getWords(batch, "x");
        double maxError = Double.parseDouble(batch_.get(0));
        int t0 = Integer.parseInt(batch_.get(1)), t1 = Integer.parseInt(batch_.get(2));
        Task[][] tasks = new Task[t0][t1];
        for (int i = 0; i < t0; i ++) {
            double e = (maxError / t0) * (i + 1);
            CountDownLatch count = new CountDownLatch(t1);
            Pointer pointer = new Pointer();
            for (int j = 0; j < t1; j ++) {
                tasks[i][j] = new Task(ALGORITHMS, j + "x" + t1 + "x" + param + "_" + e, count, pointer);
                es.execute(tasks[i][j]);
            }
        }
        return tasks;
    }

    public static Result[] summarize(Task[][] tasks) throws Exception {
        Result results[] = new Result[tasks.length];
        for (int i = 0; i < tasks.length; i ++) {
            results[i] = new Result(ALGORITHMS);
            tasks[i][0].getCount().await();
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
        summarize(execute("OPORTO_1000x100x4_EUCLIDEAN", "10x5x5"));
        //summarize(execute("BEIJING_100x10x3_SPHERE", "10x5x5"));
        //summarize(execute("SIM_100x300x5_10x2xUNIFORM", "8x4x10"));
        shutdown();
    }
}
