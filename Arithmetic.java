import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;

public class Arithmetic {

    public static final int precision = 64, displayScale = 9;
    public static final double C = 111320, R = C * 360 / (Math.PI * 2), F2M = 0.3048;
    public static final MathContext MC = new MathContext(precision, RoundingMode.HALF_EVEN);
    public static final NumberFormat fd = new DecimalFormat("0.00000000E0");
    public static final Random rand = new Random();

    public static BigDecimal epsRand() {
        String str = String.valueOf(rand.nextDouble());
        BigDecimal d = new BigDecimal(str).divide(new BigDecimal("1e15"));
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
                x[i] = rand.nextDouble();
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
            x[i] = rand.nextDouble();
        return x;
    }

    public static double haversin(BigDecimal x) {
        double x_ = Math.toRadians(x.doubleValue());
        return Math.sin(x_ / 2) * Math.sin(x_ / 2);
    }

    public static int sgn(BigDecimal x) {
        BigDecimal eps = new BigDecimal("1E-" + (precision / 2));
        if (x.abs().compareTo(eps) <= 0)
            return 0;
        return x.signum();
    }

    public static String format(BigDecimal x) {
        NumberFormat formatter = new DecimalFormat("0.0E0");
        formatter.setRoundingMode(RoundingMode.HALF_EVEN);
        formatter.setMinimumFractionDigits(displayScale);
        return formatter.format(x);
    }
}
