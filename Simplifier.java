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
        Vector<Point> points = p.linkPath();
        Series t = new Series(points);
        System.out.println("output: " + t.size());
        return t;
    }

    public Series simplify(Series s, BigDecimal eps, BigDecimal r) {
        Polytope p = new Polytope(s, eps);
        Vector<Point> points = p.linkPath(r.multiply(eps));
        Series t = new Series(points);
        System.out.println("output: " + t.size());
        return t;
    }

    public Series greedySimplify(Series s, BigDecimal eps) {
        Vector<Point> points = new Vector<Point>();
        for (int i = 0, j = 1; j < s.size(); i = j - 1) {
            points.add(s.get(i));
            Vect low = new Vect(s.get(i), s.get(i).add(new Vect("0", "-1")));
            Vect high = new Vect(s.get(i), s.get(i).add(new Vect("0", "1")));
            while (j < s.size() && Arithmetic.sgn(low.cross(new Vect(s.get(i), s.get(j)))) >= 0 && Arithmetic.sgn(high.cross(new Vect(s.get(i), s.get(j)))) <= 0) {
                Vect low_ = new Vect(s.get(i), s.get(j).add(new Vect(BigDecimal.ZERO, eps.negate())));
                Vect high_ = new Vect(s.get(i), s.get(j).add(new Vect(BigDecimal.ZERO, eps)));
                if (low.cross(low_).signum() > 0) low = low_;
                if (high.cross(high_).signum() < 0) high = high_;
                j ++;
            }
        }
        points.add(s.get(s.size() - 1));
        System.out.println("output: " + points.size());
        return new Series(points);
    }

    public Series dpSimplify(Series s, BigDecimal eps, int times) {
        int backward = times / s.size();
        boolean visible[][] = new boolean[s.size()][backward];
        for (int i = 0; i < s.size(); i ++) {
            Vect low = new Vect(s.get(i), s.get(i).add(new Vect("0", "-1")));
            Vect high = new Vect(s.get(i), s.get(i).add(new Vect("0", "1")));
            for (int j = i - 1; j >= 0 && j > i - backward; j --) {
                Vect low_ = new Vect(s.get(i), s.get(j).add(new Vect(BigDecimal.ZERO, eps.negate())));
                Vect high_ = new Vect(s.get(i), s.get(j).add(new Vect(BigDecimal.ZERO, eps)));
                if (low.cross(low_).signum() < 0) low = low_;
                if (high.cross(high_).signum() > 0) high = high_;
                if (Arithmetic.sgn(low.cross(new Vect(s.get(i), s.get(j)))) <= 0 && Arithmetic.sgn(high.cross(new Vect(s.get(i), s.get(j)))) >= 0) 
                    visible[i][j - i + backward] = true;
                else 
                    visible[i][j - i + backward] = false;
            }
        }
        int opt[] = new int[s.size()], pre[] = new int[s.size()];
        for (int i = 0; i < s.size(); i ++) {
            if (i == 0) {
                opt[i] = 1;
                pre[i] = -1;
            }
            else {
                opt[i] = s.size() + 1;
                for (int j = i - 1; j >= 0 && j > i - backward; j --) {
                    if (visible[i][j - i + backward] && opt[j] + 1 < opt[i]) {
                        opt[i] = opt[j] + 1;
                        pre[i] = j;
                    }
                }
            }
        }
        Vector<Point> points = new Vector<Point>();
        points.setSize(opt[s.size() - 1]);
        for (int i = s.size() - 1, j = opt[s.size() - 1] - 1; i != -1; i = pre[i], j --) 
            points.set(j, s.get(i));
        System.out.println("output: " + points.size());
        return new Series(points);
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
        BigDecimal eps = new BigDecimal("100000000");
        runtime.displayTime();
        Series t0 = runtime.simplify(s, eps);
        BigDecimal err0 = Simplifier.getError(s, t0, eps);
        runtime.displayTime();
        Series t1 = runtime.simplify(s, eps, new BigDecimal("0.5"));
        BigDecimal err1 = Simplifier.getError(s, t1, eps);
        runtime.displayTime();
        Series t2 = runtime.greedySimplify(s, eps);
        BigDecimal err2 = Simplifier.getError(s, t2, eps);
        runtime.displayTime();
        Series t30 = runtime.dpSimplify(s, eps, (int) 1e6);
        BigDecimal err30 = Simplifier.getError(s, t30, eps);
        runtime.displayTime();
        Series t31 = runtime.dpSimplify(s, eps, (int) 1e7);
        BigDecimal err31 = Simplifier.getError(s, t31, eps);
        runtime.displayTime();
    }
}
