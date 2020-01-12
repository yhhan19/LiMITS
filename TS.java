import java.math.BigDecimal;
import java.util.Vector;

public abstract class TS {

    private long firstTime;

    public void setTime() {
        long curTime = System.currentTimeMillis();
        firstTime = curTime;
    }

    public long getTime() {
        long curTime = System.currentTimeMillis();
        long res = curTime - firstTime;
        return res;
    }

    public abstract String name();

    public abstract SeriesKD simplifyKD(SeriesKD s, Vector<BigDecimal> eps);

    public synchronized double[] evaluateKD(SeriesKD s, Vector<BigDecimal> eps, boolean sphere) {
        double[] res = new double[3];
        setTime();
        SeriesKD t = simplifyKD(s, eps);
        res[0] = (double) getTime();
        res[1] = (double) t.size();
        res[2] = sphere ? s.sphereL2(t) / Math.sqrt(2) : s.distanceLoo(t).doubleValue();
        return res;
    }
}
