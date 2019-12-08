import java.math.BigDecimal;
import java.time.Clock;
import java.util.Vector;

public class Simplifier {

    private Clock clock;
    private long startTime;
    
    public Simplifier() {
        clock = Clock.systemDefaultZone();
        startTime = clock.millis();
    }

    private void displayTime() {
        long curTime = clock.millis();
        System.out.println("time: " + (curTime - startTime) + "ms");
        startTime = curTime;
    }

    public Series simplify(Series s, BigDecimal eps) {
        Polytope p = new Polytope(s, eps);
        Vector<Point> points = p.minLinkPath();
        Series t = new Series(points);
        System.out.println("output: " + t.size());
        return t;
    }

    public Series simplify(Series s, BigDecimal eps, BigDecimal r) {
        Polytope p = new Polytope(s, eps);
        Vector<Point> points = p.minLinkPath(r.multiply(eps));
        Series t = new Series(points);
        System.out.println("output: " + t.size());
        return t;
    }

    public static BigDecimal getError(Series s, Series t, BigDecimal eps) {
        BigDecimal error = t.distance(s, eps);
        System.out.print("head: ");
        t.get(0).subtract(s.get(0)).getTo().display();
        System.out.print("tail: ");
        t.get(t.size() - 1).subtract(s.get(s.size() - 1)).getTo().display();
        System.out.println("error: " + error.add(BigDecimal.ZERO, Arithmetic.DMC));
        return error;
    }

    public static void main(String args[]) {
        Simplifier runtime = new Simplifier();
        Series s = new Series(100000, 100000000);
        BigDecimal eps = new BigDecimal("10000");
        runtime.displayTime();
        Series t0 = runtime.simplify(s, eps);
        BigDecimal err0 = Simplifier.getError(s, t0, eps);
        runtime.displayTime();
        Series t1 = runtime.simplify(s, eps, new BigDecimal("1"));
        BigDecimal err1 = Simplifier.getError(s, t1, eps);
        runtime.displayTime();
    }
}
