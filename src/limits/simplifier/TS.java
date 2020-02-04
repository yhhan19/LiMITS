package limits.simplifier;

import java.math.BigDecimal;
import java.util.Vector;

import limits.geometry.*;

public abstract class TS {

    public abstract String name();

    public static String[] getNames(TS[] ts) {
        String[] names = new String[ts.length];
        for (int i = 0; i < ts.length; i ++) 
            names[i] = ts[i].name();
        return names;
    }

    public abstract SeriesKD simplifyKD(SeriesKD s, Vector<BigDecimal> eps);

    public double[] evaluateKD(SeriesKD s, Vector<BigDecimal> eps, boolean sphere) {
        double[] res = new double[3];
        long time = System.currentTimeMillis();
        SeriesKD t = simplifyKD(s, eps);
        res[1] = (double) (System.currentTimeMillis() - time);
        res[0] = (double) t.size();
        res[2] = sphere ? s.sphereL2(t) / Math.sqrt(2) : s.distanceLoo(t).doubleValue();
        return res;
    }
}
