import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class Arithmetic {

    public static final int precision = 64;
    public static final BigDecimal TWO = new BigDecimal("2");
    public static final MathContext MC = new MathContext(precision, RoundingMode.HALF_EVEN);
    public static final MathContext DMC = new MathContext(16, RoundingMode.HALF_EVEN);
 
    public static BigDecimal sqrt(BigDecimal x, MathContext mc) {
        BigDecimal g = x.divide(TWO, mc);
        boolean done = false;
        final int maxIterations = mc.getPrecision() + 1;
        for (int i = 0; ! done && i < maxIterations; i ++) {
            if (g.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
            BigDecimal r = x.divide(g, mc);
            r = r.add(g);
            r = r.divide(TWO, mc);
            done = r.equals(g);
            g = r;
        }
        return g;
    }

    public static int sgn(BigDecimal x) {
        BigDecimal eps = new BigDecimal("1E-" + (precision / 2));
        if (x.abs().compareTo(eps) <= 0)
            return 0;
        return x.signum();
    }

    public static int sgn(BigDecimal x, MathContext mc) {
        BigDecimal eps = new BigDecimal("1E-" + mc.getPrecision());
        if (x.abs().compareTo(eps) <= 0)
            return 0;
        return x.signum();
    }
}
