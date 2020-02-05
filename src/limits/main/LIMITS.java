package limits.main;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.Vector;

import limits.data.*;
import limits.simplifier.*;

public class LIMITS {

    public static final String 
        PROJECT_NAME = "L-Inifinity Multidimensional Interpolation Trajectory Simplification (LIMITS)", 
        IO_FOLDER_NAME = "C:/Academics/vldb-2020/experiments-java", 
        DATA_FOLDER_NAME = IO_FOLDER_NAME + "/data", 
        LOG_FOLDER_NAME = IO_FOLDER_NAME + "/log/long", 
        RES_FOLDER_NAME = IO_FOLDER_NAME + "/log/short", 
        REPORT_FOLDER_NAME = IO_FOLDER_NAME + "/report";

    public static final Datasets 
        DATASETS = new Datasets(DATA_FOLDER_NAME, new String[] {
            "BEIJINGx6x,x4/0/1/3x-777x12", 
            "MOPSIx0x x2/0/1/3x-1.0x12", 
            "BEEx0x,x0/1/2x,x3", 
            "FLIGHTx1x,x0/3/4/1x,x12", 
            "TDRIVEx0x,x1/3/2x,x12", 
            "TAXIx0x;x1/2/3x;x12", 
            "STORK.CSVx1x,x20/2/4/3/17x,x12", 
            "GOOSE.CSVx1x,x19/2/4/3/16x,x12", 
            "FISH.CSVx1x,x1/2/3/4/5x,x12"
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

    public static void execute(int mask, String batch, String param) throws Exception {
        String[] batch_ = batch.split("x");
        double minError = Double.parseDouble(batch_[0]), maxError = Double.parseDouble(batch_[1]);
        int t0 = Integer.parseInt(batch_[2]), t1 = Integer.parseInt(batch_[3]);
        Task[][] tasks = new Task[t0][t1];
        for (int i = 0; i < t0; i ++) {
            double e = minError + ((maxError - minError) / (t0 - 1)) * i;
            CountDownLatch count = new CountDownLatch(t1);
            SharedInteger pointer = new SharedInteger(i % 2);
            for (int j = 0; j < t1; j ++) {
                tasks[i][j] = new Task(mask, j + "x" + param + "_" + e, count, pointer);
                es.execute(tasks[i][j]);
            }
        }
        Log report = new Log(REPORT_FOLDER_NAME, tasks[0][0].superName() + "_" + batch + ".txt");
        Result results[] = new Result[t0];
        for (int i = 0; i < t0; i ++) {
            results[i] = new Result(TS.getNames(tasks[i][0].getTS()), tasks[i][0].getResults().getError());
            tasks[i][0].getCount().await();
            for (int j = 0; j < t1; j ++) {
                results[i].add(tasks[i][j].getResults());
            }
            String res = results[i].toString(results[i].getSize());
            Log log = new Log(RES_FOLDER_NAME, tasks[i][0].taskName() + ".res");
            log.add(res);
            log.close();
            report.add(res);
            System.out.println("\nbatch done: " + tasks[i][0].taskName());
        }
        report.close();
        Results rep = new Results(REPORT_FOLDER_NAME, tasks[0][0].superName() + "_" + batch + ".txt");
        rep.toCommands(param.split("_")[0], minError + "," + maxError);
    }

    public static void executes(int mask, String batch, String[] params) throws Exception {
        for (int i = 0; i < params.length; i ++) {
            execute(mask, batch, params[i]);
        }
    }

    public static void shutdown() {
        es.shutdown();
    }

    public static void main(String[] args) throws Exception {
        executes(EFFICIENT_ALGORITHMS, "0.1x5x50x10", new String[] {
            "FISH.CSV_0x0x4_DEFAULT" 
            //  "BEIJING_10x0x3_SPHERE" 
            //, "BEIJING_10x0x3_EUCLIDEAN" 
            //, "BEIJING_10x0x4_EUCLIDEAN" 
            //  "MOPSI_1000x100x3_SPHERE" 
            //, "MOPSI_1000x100x3_EUCLIDEAN" 
            //, "MOPSI_1000x100x4_EUCLIDEAN" 
            //, "SIM_100x100x3_20x2xUNIFORM" 
            //, "SIM_10x100x4_20x2xUNIFORM" 
            //, "BEE_10x0x3_DEFAULT" 
        });
        shutdown();
    }
}