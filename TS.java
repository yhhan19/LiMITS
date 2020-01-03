import java.math.BigDecimal;
import java.time.Clock;
import java.util.Vector;

public abstract class TS {

    public static Clock clock = Clock.systemDefaultZone();
    public static long firstTime = clock.millis();
    
    public static void setTime() {
        long curTime = clock.millis();
        firstTime = curTime;
    }

    public static long getTime() {
        long curTime = clock.millis();
        long res = curTime - firstTime;
        return res;
    }

    public static void displayTime() {
        long curTime = clock.millis();
        System.out.println("time: " + (curTime - firstTime) + "ms");
    }

    public abstract SeriesKD simplifyKD(SeriesKD s, Vector<BigDecimal> eps);

    public double[] evaluateKD(SeriesKD s, Vector<BigDecimal> eps, boolean real) {
        double[] res = new double[3];
        setTime();
        SeriesKD t = simplifyKD(s, eps);
        res[0] = (double) getTime();
        res[1] = (double) t.size();
        res[2] = real ? s.sphereL2(t) / Math.sqrt(2) : s.distanceLoo(t).doubleValue();
        return res;
    }
}
