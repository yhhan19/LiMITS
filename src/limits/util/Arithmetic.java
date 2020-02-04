package limits.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Vector;

public class Arithmetic {
    
    public static final int 
        PRECISION = 64, 
        MAX_DIM = 8;
    
    public static final double 
        METERS_PER_LON = 111320, 
        EARTH_RADIUS = METERS_PER_LON * 360 / (Math.PI * 2), 
        FEET_TO_METER = 0.3048;
    
    public static final BigDecimal 
        MI2_ENDPOINT = new BigDecimal("1e-6");
    
    public static final MathContext 
        MATH_CONTEXT = new MathContext(PRECISION, RoundingMode.HALF_EVEN);
    
    public static final Random 
        RAND = new Random();

    public static BigDecimal generalize(BigDecimal range) {
        String str = String.valueOf(RAND.nextDouble());
        if (RAND.nextInt(2) == 0) str = "-" + str;
        BigDecimal d = new BigDecimal(str).multiply(range);
        return d;
    }

    public static BigDecimal sqrt(BigDecimal x, MathContext mc) {
        BigDecimal g = x.divide(new BigDecimal(2), mc);
        boolean done = false;
        for (int i = 0; ! done && i < mc.getPrecision() + 1; i ++) {
            if (g.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
            BigDecimal r = x.divide(g, mc);
            r = r.add(g);
            r = r.divide(new BigDecimal(2), mc);
            done = r.equals(g);
            g = r;
        }
        return g;
    }

    public static double sqrt(BigDecimal x) {
        double x_ = x.doubleValue();
        return Math.sqrt(x_);
    }

    public static double sin(BigDecimal x) {
        double x_ = Math.toRadians(x.doubleValue());
        return Math.sin(x_);
    }

    public static double cos(BigDecimal x) {
        double x_ = Math.toRadians(x.doubleValue());
        return Math.cos(x_);
    }

    public static double[] gaussian(int k) {
        double[] x = new double[k];
        double s;
        do {
            s = 0;
            for (int i = 0; i < k; i ++) { 
                x[i] = RAND.nextDouble();
                s += x[i] * x[i];
            }
        } while (s > 1);
        double y = Math.sqrt(- 2 * Math.log(s) / s);
        for (int i = 0; i < k; i ++) 
            x[i] *= y;
        return x;
    }

    public static double[] uniform(int k) {
        double[] x = new double[k];
        for (int i = 0; i < k; i ++) 
            x[i] = RAND.nextDouble();
        return x;
    }

    public static double haversin(BigDecimal x) {
        double x_ = Math.toRadians(x.doubleValue());
        return Math.sin(x_ / 2) * Math.sin(x_ / 2);
    }

    public static int sgn(BigDecimal x) {
        BigDecimal eps = new BigDecimal("1E-" + (PRECISION / 2));
        if (x.abs().compareTo(eps) <= 0)
            return 0;
        return x.signum();
    }

    public static BigDecimal getTime(String word) {
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = s.parse(word);
        }
        catch (Exception e) {

        }
        return new BigDecimal(date.getTime());
    }
}
