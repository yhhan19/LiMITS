import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.Vector;

public class LIMITS {

    public static final String 
        PROJECT_NAME = "L-Inifinity Multidimensional Interpolation Trajectory Simplification (LIMITS)", 
        DATA_FOLDER_NAME = "../data", 
        LOG_FOLDER_NAME = "../log", 
        REPORT_FOLDER_NAME = "../report";

    public static final Datasets 
        DATASETS = new Datasets(new String[] {
            "BEIJINGx6x,x4|0|1", 
            "OPORTOx0x x2|0|1|3x-1.0"
        });

    public static final TS[] 
        ALGORITHMS = new TS[] {
            new RDP(), 
            new G1TS(), 
            new DPTS(0), 
            new G2TS(), 
            new M1TS(0.5), 
            new M2TS(10)
        };
    
    public static final int 
        ALL_ALGORITHMS = 0B111111, 
        STRONG_ALGORITHMS = 0B000111, 
        WEAK_ALGORITHMS = 0B111000, 
        EFFICIENT_ALGORITHMS = 0B111011, 
        EFFECTIVE_ALGORITHMS = 0B101100;

    private static final ExecutorService es = Executors.newFixedThreadPool(10);

    public static TS[] select(int mask) {
        int len = 0;
        for (int i = 0; i < ALGORITHMS.length; i ++) 
            if ((mask & (1 << i)) > 0) 
                len ++;
        TS[] ts = new TS[len];
        for (int i = 0, j = 0; i < ALGORITHMS.length; i ++) 
            if ((mask & (1 << i)) > 0) 
                ts[j ++] = ALGORITHMS[i];
        return ts;
    }

    public static Result[] execute(String param, String batch, int mask) throws Exception {
        Vector<String> batch_ = Reader.getWords(batch, "x");
        double maxError = Double.parseDouble(batch_.get(0));
        int t0 = Integer.parseInt(batch_.get(1)), t1 = Integer.parseInt(batch_.get(2));
        Task[][] tasks = new Task[t0][t1];
        for (int i = 0; i < t0; i ++) {
            double e = (maxError / t0) * (i + 1);
            CountDownLatch count = new CountDownLatch(t1);
            SharedInteger pointer = new SharedInteger();
            for (int j = 0; j < t1; j ++) {
                tasks[i][j] = new Task(mask, j + "x" + param + "_" + e, count, pointer);
                es.execute(tasks[i][j]);
            }
        }
        Log report = new Log(REPORT_FOLDER_NAME, tasks[0][0].superName() + ".txt");
        Result results[] = new Result[t0];
        for (int i = 0; i < t0; i ++) {
            results[i] = new Result(tasks[i][0].getTS());
            tasks[i][0].getCount().await();
            for (int j = 0; j < t1; j ++) {
                results[i].add(tasks[i][j].getResults());
            }
            String res = results[i].toString(results[i].getSize());
            Log log = new Log(tasks[i][0].taskName() + ".res");
            log.write(res);
            log.close();
            report.write("error: " + (maxError / t0) * (i + 1) + " " + res);
            System.out.println("batch done: " + tasks[i][0].taskName());
        }
        report.close();
        return results;
    }

    public static void shutdown() {
        es.shutdown();
    }

    public static void main(String[] args) throws Exception {
        int mask = EFFECTIVE_ALGORITHMS & WEAK_ALGORITHMS;
        String data = "OPORTO_1000x50x4_EUCLIDEAN", batch = "10x5x5";
        // OPORTO_1000x50x4_EUCLIDEAN, BEIJING_100x50x3_SPHERE, SIM_100x200x5_10x2xUNIFORM
        execute(data, batch, mask);
        shutdown();
    }
}
