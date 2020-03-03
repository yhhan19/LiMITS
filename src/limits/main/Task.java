package limits.main;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.Vector;

import limits.data.*;
import limits.simplifier.*;

public class Task extends Thread {

    private final TS[] ts;
    private final Log log;
    private final Result results;
    private final Dataset data;
    private final int cases, size, rawDim, dim;
    private final String param, superName, taskName, folderName, type;
    private final double eps;
    private final CountDownLatch count;
    private final SharedInteger pointer;

    public Task(int algorithms, String param, CountDownLatch count, SharedInteger pointer) {
        this.ts = LIMITS.select(algorithms);
        this.param = param;
        this.count = count;
        this.pointer = pointer;
        log = new Log(LIMITS.LOG_FOLDER_NAME, param + "_" + algorithms + ".log");
        String[] p = param.split("_"), name = p[0].split("x"), scale = p[1].split("x");
        superName = name[1] + "_" + p[1] + "_" + p[2] + "_" + algorithms;
        taskName = name[1] + "_" + p[1] + "_" + p[2] + "_" + p[3] + "_" + algorithms;
        folderName = name[1];
        data = LIMITS.DATASETS.getDataset(folderName);
        cases = Integer.parseInt(scale[0]);
        size = Integer.parseInt(scale[1]);
        rawDim = Integer.parseInt(scale[2]);
        type = p[2];
        eps = Double.parseDouble(p[3]);
        results = new Result(TS.getNames(ts), eps);
        dim = type.equals("EUCLIDEAN") ? 4 : rawDim;
    }

    public String taskName() {
        return taskName;
    }

    public String superName() {
        return superName;
    }

    public CountDownLatch getCount() {
        return count;
    }

    public TS[] getTS() {
        return ts;
    }

    public void test() {
        for (int i = pointer.next(); i < data.size() && (cases <= 0 || i < cases); i = pointer.next()) {
            limits.geometry.SeriesKD input = new limits.geometry.SeriesKD(data.getWords(i), data.invalid(), data.getGenRange(), rawDim, size, type);
            if (type.equals("EUCLIDEAN") && input.rawDim() < rawDim) continue;
            log.add("case: " + i + " size: " + input.size() + "\n");
            Vector<BigDecimal> error = input.getError(type, eps);
            double[][] output = new double[ts.length][];
            for (int j = 0; j < ts.length; j ++) {
                output[j] = ts[j].evaluateKD(input, error, type.equals("SPHERE"));
                log.add(Result.toString(output[j], 1, input.rawDim()));
            }
            results.add(new Result(TS.getNames(ts), output, eps, input.size(), 1));
        }
        log.write(results.toString(results.getSize(), dim));
    }

    public Result getResults() {
        return results;
    }

    public int getDim() {
        return dim;
    }

    public void run() {
        test();
        log.close();
        count.countDown();
    }
}
