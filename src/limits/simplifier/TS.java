package limits.simplifier;

import java.math.BigDecimal;
import java.util.Vector;

import limits.geometry.*;
import limits.util.*;

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
        double[] res = new double[Arithmetic.RES_LEN];
        long time = System.currentTimeMillis();
        SeriesKD t = simplifyKD(s, eps);
        res[1] = (double) (System.currentTimeMillis() - time);
        res[0] = t.restore();
        res[2] = (double) t.size();
        res[3] = (double) t.getBitSize();
        res[4] = sphere ? s.sphereL2(t) / Math.sqrt(2) : s.distanceLoo(t).doubleValue();
        return res;
    }
}
