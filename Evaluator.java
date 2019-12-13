import java.math.BigDecimal;
import java.time.Clock;
import java.util.Collections;
import java.util.Vector;

public class Evaluator {

    public static Clock clock = Clock.systemDefaultZone();
    public static long firstTime = clock.millis();
    public static int totalMethods = 6;
    
    public static void setTime() {
        long curTime = clock.millis();
        firstTime = curTime;
    }

    public static long getSetTime() {
        long curTime = clock.millis();
        long res = curTime - firstTime;
        firstTime = curTime;
        return res;
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

    public Series simplify(Series s, BigDecimal eps) {
        Polytope p = new Polytope(s, eps);
        Vector<Point> points = p.linkPath();
        Series t = new Series(points);
        return t;
    }

    public Series simplify(Series s, BigDecimal eps, BigDecimal y) {
        Polytope p = new Polytope(s, eps);
        Vector<Point> points = p.linkPath(y);
        Series t = new Series(points);
        return t;
    }

    public Series simplify(Series s, BigDecimal eps, Range y) {
        Polytope p = new Polytope(s, eps);
        Vector<Point> points = p.linkPath(y);
        Series t = new Series(points);
        return t;
    }

    public Series simplify(Series s, BigDecimal eps, Range y, BigDecimal r) {
        BigDecimal y0 = y.interpolation(r);
        Polytope p = new Polytope(s, eps);
        Vector<Point> points = p.linkPath(y0, y, r, null);
        Series t = new Series(points);
        return t;
    }

    public Series simplify(Series s, BigDecimal eps, Range y, BigDecimal start, BigDecimal end) {
        BigDecimal y0 = y.interpolation(start);
        Polytope p = new Polytope(s, eps);
        Vector<Point> points = p.linkPath(y0, y, null, end);
        Series t = new Series(points);
        return t;
    }

    public Series greedySimplify(Series s, BigDecimal eps) {
        Vector<Point> points = new Vector<Point>();
        for (int i = 0, j = 1; j < s.size(); i = j - 1) {
            points.add(s.get(i));
            Vect low = new Vect(s.get(i), s.get(i).add(new Vect("0", "-1")));
            Vect high = new Vect(s.get(i), s.get(i).add(new Vect("0", "1")));
            while (j < s.size() && (new Vect(s.get(i), s.get(j))).between(low, high)) {
                Vect low_ = new Vect(s.get(i), s.get(j).add(new Vect(BigDecimal.ZERO, eps.negate())));
                Vect high_ = new Vect(s.get(i), s.get(j).add(new Vect(BigDecimal.ZERO, eps)));
                if (low.cross(low_).signum() > 0) low = low_;
                if (high.cross(high_).signum() < 0) high = high_;
                j ++;
            }
        }
        points.add(s.get(s.size() - 1));
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
                if ((new Vect(s.get(i), s.get(j))).between(high, low)) 
                    visible[i][j - i + backward] = true;
                else 
                    visible[i][j - i + backward] = false;
            }
        }
        int opt[] = new int[s.size()], pre[] = new int[s.size()];
        for (int i = 0; i < s.size(); i ++) {
            if (i == 0) {
                opt[i] = 1; pre[i] = -1;
            }
            else {
                opt[i] = s.size() + 1;
                for (int j = i - 1; j >= 0 && j > i - backward; j --) 
                    if (visible[i][j - i + backward] && opt[j] + 1 < opt[i]) {
                        opt[i] = opt[j] + 1; pre[i] = j;
                    }
            }
        }
        Vector<Point> points = new Vector<Point>();
        points.setSize(opt[s.size() - 1]);
        for (int i = s.size() - 1, j = opt[s.size() - 1] - 1; i != -1; i = pre[i], j --) 
            points.set(j, s.get(i));
        return new Series(points);
    }

    public Series3D combineSimplify3D(Series3D s, BigDecimal eps) {
        Series s0 = s.projectZ();
        Series t0 = simplify(s0, eps);
        Series s1 = s.projectY();
        Series t1 = simplify(s1, eps);
        Series3D t = new Series3D(t0, t1);
        //System.out.println("interpolation: " + t0.size());
        return t;
    }

    public Series3D combineSimplify3D(Series3D s, BigDecimal eps, BigDecimal r0, BigDecimal r1) {
        Series s0 = s.projectZ();
        Series t0 = simplify(s0, eps, r0);
        Series s1 = s.projectY();
        Series t1 = simplify(s1, eps, r1);
        Series3D t = new Series3D(t0, t1);
        //System.out.println("interpolation: " + t0.size());
        return t;
    }

    public Series3D refinedCombineSimplify3D(Series3D s, BigDecimal eps) {
        Series s0 = s.projectZ();
        Series t0 = simplify(s0, eps);
        Series s1 = s.projectY();
        Vector<Point> points = new Vector<Point>();
        Point first = null;
        BigDecimal last = null;
        for (int i = 1, j = 1, delta; i < t0.size(); i ++) {
            Vector<Point> buffer = new Vector<Point>();
            BigDecimal x = t0.get(i).getX();
            if (first != null) buffer.add(first);
            for (delta = -1, j = j - 1; j < s1.size() && delta < 0; j ++) {
                delta = Arithmetic.sgn(s1.get(j).getX().subtract(x));
                Point p = delta <= 0 ? s1.get(j) : Point.interpolationX(s1.get(j - 1), s1.get(j), x);
                first = delta <= 0 ? null : p;
                buffer.add(p);
            }
            Series s1_ = new Series(buffer), t1_ = null;
            //s1_.display();
            if (last == null) {
                t1_ = simplify(s1_, eps);
                for (int k = 0; k < t1_.size(); k ++) 
                    points.add(t1_.get(k));
            }
            else {
                t1_ = simplify(s1_, eps, last);
                for (int k = 1; k < t1_.size(); k ++) 
                    points.add(t1_.get(k));
            }
            last = t1_.lastElement().getY();
        }
        Series t1 = new Series(points);
        Series3D t = new Series3D(t0, t1);
        //System.out.println("interpolation: " + t0.size());
        return t;
    }

    public Series3D re2finedCombineSimplify3D(Series3D s, BigDecimal eps, BigDecimal r) {
        Series s0 = s.projectZ();
        Series t0 = simplify(s0, eps);
        Series s1 = s.projectY();
        Vector<Point> points = new Vector<Point>();
        Point first = null;
        Range last = new Range(s1.get(0).getY().add(eps), s1.get(0).getY().subtract(eps));
        points.add(new Point(s1.get(0).getX(), last.interpolation(r)));
        for (int i = 1, j = 1, delta; i < t0.size(); i ++) {
            Vector<Point> buffer = new Vector<Point>();
            BigDecimal x = t0.get(i).getX();
            if (first != null) buffer.add(first);
            for (delta = -1, j = j - 1; j < s1.size() && delta < 0; j ++) {
                delta = Arithmetic.sgn(s1.get(j).getX().subtract(x));
                Point p = delta <= 0 ? s1.get(j) : Point.interpolationX(s1.get(j - 1), s1.get(j), x);
                first = delta <= 0 ? null : p;
                buffer.add(p);
            }
            Series s1_ = new Series(buffer);
            Series t1_ = simplify(s1_, eps, last, r);
            for (int k = 1; k < t1_.size(); k ++) 
                points.add(t1_.get(k));
        }
        Series t1 = new Series(points);
        Series3D t = new Series3D(t0, t1);
        //System.out.println("interpolation: " + t0.size());
        return t;
    }

    private BigDecimal sample(int R, int u) {
        BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(R), Arithmetic.MC);
        if (u == 0) return step.multiply(new BigDecimal(1e-6));
        if (u == R) return step.multiply(BigDecimal.ONE.subtract(new BigDecimal(1e-6)));
        return step.multiply(new BigDecimal(u));
    }

    public Series3D re3finedCombineSimplify3D(Series3D s, BigDecimal eps, int R) {
        Series s0 = s.projectZ();
        Series t0 = simplify(s0, eps);
        Series s1 = s.projectY();
        Point first = null;
        int weight[][][] = new int[t0.size()][R + 1][R + 1], bin[][][] = new int[t0.size()][R + 1][R + 1];
        for (int i = 1, j = 1, delta; i < t0.size(); i ++) {
            Vector<Point> buffer = new Vector<Point>();
            BigDecimal x = t0.get(i).getX();
            if (first != null) buffer.add(first);
            for (delta = -1, j = j - 1; j < s1.size() && delta < 0; j ++) {
                delta = Arithmetic.sgn(s1.get(j).getX().subtract(x));
                Point p = delta <= 0 ? s1.get(j) : Point.interpolationX(s1.get(j - 1), s1.get(j), x);
                first = delta <= 0 ? null : p;
                buffer.add(p);
            }
            Series s1_ = new Series(buffer);
            for (int u = 0; u <= R; u ++) {
                BigDecimal ru = sample(R, u);
                Range last = new Range(s1_.get(0).getY().add(eps), s1_.get(0).getY().subtract(eps));
                Series t1_ = simplify(s1_, eps, last, ru);
                for (int v = 0; v <= R; v ++) {
                    BigDecimal rv = sample(R, v);
                    BigDecimal y = (new Range(s1_.lastElement().getY().add(eps), s1_.lastElement().getY().subtract(eps))).interpolation(rv);
                    weight[i][u][v] = last.contains(y) ? t1_.size() : t1_.size() + 1;
                    bin[i][u][v] = last.contains(y) ? 0 : 1;
                }
            }
        }
        int opt[][] = new int[t0.size()][R + 1], pre[][] = new int[t0.size()][R + 1];
        int min = s.size(), argmin = -1;
        for (int i = 0; i < t0.size(); i ++) {
            for (int u = 0; u <= R; u ++) {
                if (i == 0) {
                    opt[i][u] = 1;
                    pre[i][u] = -1;
                    continue;
                }
                opt[i][u] = -1;
                for (int v = 0; v <= R; v ++) {
                    int temp = opt[i - 1][v] + weight[i][v][u] - 1;
                    if (opt[i][u] == -1 || temp < opt[i][u]) {
                        opt[i][u] = temp;
                        pre[i][u] = v;
                    }
                }
                if (i == t0.size() - 1 && opt[i][u] < min) {
                    min = opt[i][u];
                    argmin = u;
                }
            }
        }
        if (min != s.size()) {
            int arg[] = new int[t0.size()];
            for (int i = t0.size() - 1, j = argmin; i >= 0; j = pre[i][j], i --) {
                arg[i] = j;
            }
        }
        //System.out.println("output: " + min + " ratio: " + String.format("%.2f%s", (double) min / s.size() * 100, "%"));
        return new Series3D(min);
    }

    public Series3D greedySimplify3D(Series3D s, BigDecimal eps) {
        Vector<Point3D> points = new Vector<Point3D>();
        for (int i = 0, j = 1; j < s.size(); i = j - 1) {
            points.add(s.get(i));
            Point p0 = s.get(i).projectZ(), p1 = s.get(i).projectY();
            Vect l0 = new Vect(p0, p0.add(new Vect("0", "-1")));
            Vect h0 = new Vect(p0, p0.add(new Vect("0", "1")));
            Vect l1 = new Vect(p1, p1.add(new Vect("0", "-1")));
            Vect h1 = new Vect(p1, p1.add(new Vect("0", "1")));
            while (j < s.size()) {
                Point q0 = s.get(j).projectZ(), q1 = s.get(j).projectY();
                Vect c0 = new Vect(p0, q0), c1 = new Vect(p1, q1);
                if (! c0.between(l0, h0) || ! c1.between(l1, h1)) break;
                Vect l0_ = new Vect(p0, q0.add(new Vect(BigDecimal.ZERO, eps.negate())));
                Vect h0_ = new Vect(p0, q0.add(new Vect(BigDecimal.ZERO, eps)));
                Vect l1_ = new Vect(p1, q1.add(new Vect(BigDecimal.ZERO, eps.negate())));
                Vect h1_ = new Vect(p1, q1.add(new Vect(BigDecimal.ZERO, eps)));
                if (l0.cross(l0_).signum() > 0) l0 = l0_;
                if (h0.cross(h0_).signum() < 0) h0 = h0_;
                if (l1.cross(l1_).signum() > 0) l1 = l1_;
                if (h1.cross(h1_).signum() < 0) h1 = h1_;
                j ++;
            }
        }
        points.add(s.get(s.size() - 1));
        return new Series3D(points);
    }

    public Series3D greedy2Simplify3D(Series3D s, BigDecimal eps) {
        Vector<Point3D> points = new Vector<Point3D>();
        for (int i = 0, j = 1; j < s.size(); i = j - 1) {
            points.add(s.get(i));
            Point p0 = s.get(i).projectZ(), p1 = s.get(i).projectY();
            Vect l0 = new Vect(p0, p0.add(new Vect("0", "-1")));
            Vect h0 = new Vect(p0, p0.add(new Vect("0", "1")));
            Vect l1 = new Vect(p1, p1.add(new Vect("0", "-1")));
            Vect h1 = new Vect(p1, p1.add(new Vect("0", "1")));
            while (j < s.size()) {
                Point q0 = s.get(j).projectZ(), q1 = s.get(j).projectY();
                Vect c0 = new Vect(p0, q0), c1 = new Vect(p1, q1);
                if (! c0.between(l0, h0) || ! c1.between(l1, h1)) break;
                Vect l0_ = new Vect(p0, q0.add(new Vect(BigDecimal.ZERO, eps.negate())));
                Vect h0_ = new Vect(p0, q0.add(new Vect(BigDecimal.ZERO, eps)));
                Vect l1_ = new Vect(p1, q1.add(new Vect(BigDecimal.ZERO, eps.negate())));
                Vect h1_ = new Vect(p1, q1.add(new Vect(BigDecimal.ZERO, eps)));
                if (l0.cross(l0_).signum() > 0) l0 = l0_;
                if (h0.cross(h0_).signum() < 0) h0 = h0_;
                if (l1.cross(l1_).signum() > 0) l1 = l1_;
                if (h1.cross(h1_).signum() < 0) h1 = h1_;
                j ++;
            }
        }
        points.add(s.get(s.size() - 1));
        return new Series3D(points);
    }

    public Series3D dpSimplify3D(Series3D s, BigDecimal eps, int times) {
        int backward = times / s.size();
        boolean visible[][] = new boolean[s.size()][backward];
        for (int i = 0; i < s.size(); i ++) {
            Point p0 = s.get(i).projectZ(), p1 = s.get(i).projectY();
            Vect l0 = new Vect(p0, p0.add(new Vect("0", "-1")));
            Vect h0 = new Vect(p0, p0.add(new Vect("0", "1")));
            Vect l1 = new Vect(p1, p1.add(new Vect("0", "-1")));
            Vect h1 = new Vect(p1, p1.add(new Vect("0", "1")));
            for (int j = i - 1; j >= 0 && j > i - backward; j --) {
                Point q0 = s.get(j).projectZ(), q1 = s.get(j).projectY();
                Vect c0 = new Vect(p0, q0), c1 = new Vect(p1, q1);
                Vect l0_ = new Vect(p0, q0.add(new Vect(BigDecimal.ZERO, eps.negate())));
                Vect h0_ = new Vect(p0, q0.add(new Vect(BigDecimal.ZERO, eps)));
                Vect l1_ = new Vect(p1, q1.add(new Vect(BigDecimal.ZERO, eps.negate())));
                Vect h1_ = new Vect(p1, q1.add(new Vect(BigDecimal.ZERO, eps)));
                if (l0.cross(l0_).signum() < 0) l0 = l0_;
                if (h0.cross(h0_).signum() > 0) h0 = h0_;
                if (l1.cross(l1_).signum() < 0) l1 = l1_;
                if (h1.cross(h1_).signum() > 0) h1 = h1_;
                if (c0.between(h0, l0) && c1.between(h1, l1)) 
                    visible[i][j - i + backward] = true;
                else 
                    visible[i][j - i + backward] = false;
            }
        }
        int opt[] = new int[s.size()], pre[] = new int[s.size()];
        for (int i = 0; i < s.size(); i ++) {
            if (i == 0) {
                opt[i] = 1; pre[i] = -1;
            }
            else {
                opt[i] = s.size() + 1;
                for (int j = i - 1; j >= 0 && j > i - backward; j --) 
                    if (visible[i][j - i + backward] && opt[j] + 1 < opt[i]) {
                        opt[i] = opt[j] + 1; pre[i] = j;
                    }
            }
        }
        Vector<Point3D> points = new Vector<Point3D>();
        points.setSize(opt[s.size() - 1]);
        for (int i = s.size() - 1, j = opt[s.size() - 1] - 1; i != -1; i = pre[i], j --) 
            points.set(j, s.get(i));
        return new Series3D(points);
    }

    private Vector<Point3D> RDP(Series3D s, int left, int right, BigDecimal eps) {
        Vector<Point3D> points = new Vector<Point3D>();
        Point3D p = s.get(left), q = s.get(right);
        if (right - left == 0) {
            points.add(p);
        }
        if (right - left == 1) {
            points.add(p);
            points.add(q);
        }
        if (right - left > 1) {
            Vect v0 = new Vect(p.projectZ(), q.projectZ());
            Vect v1 = new Vect(p.projectY(), q.projectY());
            BigDecimal max = null;
            int maxarg = -1;
            for (int i = left; i <= right; i ++) {
                Point3D r = s.get(i);
                Point r0 = r.projectZ(), r1 = r.projectY();
                Point r0_ = v0.interpolationX(r0.getX()), r1_ = v1.interpolationX(r1.getX());
                BigDecimal d0 = r0.getY().subtract(r0_.getY()), d1 = r1.getY().subtract(r1_.getY());
                BigDecimal dist = (new Point(d0, d1)).distLoo(Point.ORIGIN);
                if (max == null || max.compareTo(dist) == -1) {
                    max = dist;
                    maxarg = i;
                }
            }
            if (max.compareTo(eps) <= 0) {
                points.add(p);
                points.add(q);
            }
            else {
                Vector<Point3D> L = RDP(s, left, maxarg, eps);
                Vector<Point3D> R = RDP(s, maxarg, right, eps);
                for (int i = 0; i < L.size() - 1; i ++) 
                    points.add(L.get(i));
                for (int i = 0; i < R.size(); i ++) 
                    points.add(R.get(i));
            }
        }
        return points;
    }

    public Series3D RDPSimplify3D(Series3D s, BigDecimal eps) {
        Vector<Point3D> points = RDP(s, 0, s.size() - 1, eps);
        return new Series3D(points);
    }

    public static BigDecimal reportPerformance(Series s, Series t) {
        displayTime();
        setTime();
        if (t == null) return null;
        System.out.println("output: " + t.size() + " ratio: " + String.format("%.2f%s", (double) t.size() / s.size() * 100, "%"));
        BigDecimal error = t.distance(s);
        System.out.print("head: ");
        t.get(0).subtract(s.get(0)).getTo().display();
        System.out.print("tail: ");
        t.get(t.size() - 1).subtract(s.get(s.size() - 1)).getTo().display();
        System.out.println("error: " + Arithmetic.format(error));
        return error;
    }

    public static Point reportPerformance(Series3D s, Series3D t) {
        displayTime();
        setTime();
        if (t == null) return null;
        System.out.println("output: " + t.size() + " ratio: " + String.format("%.2f%s", (double) t.size() / s.size() * 100, "%"));
        Point error = t.distance(s);
        /*
        System.out.print("head: ");
        t.get(0).projectZ().subtract(s.get(0).projectZ()).getTo().display();
        t.get(0).projectY().subtract(s.get(0).projectY()).getTo().display();
        System.out.print("tail: ");
        t.get(t.size() - 1).projectZ().subtract(s.get(s.size() - 1).projectZ()).getTo().display();
        t.get(t.size() - 1).projectY().subtract(s.get(s.size() - 1).projectY()).getTo().display();
        */
        System.out.print("error: " + Arithmetic.format(error.getX()));
        System.out.println(" " + Arithmetic.format(error.getY()));
        return error;
    }

    public static Point recordPerformance(Series3D s, Series3D t, Vector<Long> space, Vector<Long> time) {
        if (t == null) return null;
        time.add(Evaluator.getSetTime());
        space.add((long) t.size());
        System.out.println("-");
        return t.distance(s);
    }

    private void evaluate() {
        Series s = new Series((int) 1e5, (int) 1e9);
        BigDecimal eps = new BigDecimal((int) 1e9);
        Evaluator.displayTime();
        Evaluator.reportPerformance(s, simplify(s, eps));
        Evaluator.reportPerformance(s, simplify(s, eps, (new BigDecimal("0.5")).multiply(eps)));
        Evaluator.reportPerformance(s, greedySimplify(s, eps));
        Evaluator.reportPerformance(s, dpSimplify(s, eps, (int) 1e7));
        Evaluator.reportPerformance(s, dpSimplify(s, eps, (int) 1e8));
    }

    private void evaluate3D() {
        Series3D s = new Series3D((int) 1e5, (int) 1e9, 2);
        BigDecimal eps = new BigDecimal((int) 1e9);
        Evaluator.displayTime();
        Evaluator.reportPerformance(s, combineSimplify3D(s, eps));
        Evaluator.reportPerformance(s, combineSimplify3D(s, eps, 
        (new BigDecimal("0.5")).multiply(eps), (new BigDecimal("-0.5")).multiply(eps)));
        Evaluator.reportPerformance(s, greedySimplify3D(s, eps));
        Evaluator.reportPerformance(s, dpSimplify3D(s, eps, (int) 1e6));
        Evaluator.reportPerformance(s, dpSimplify3D(s, eps, (int) 1e7));
        Evaluator.reportPerformance(s, dpSimplify3D(s, eps, (int) 1e8));
    }

    public void evaluate3D2() {
        Series3D s = new Series3D((int) 1e5, (int) 1e9, 2);
        BigDecimal eps = new BigDecimal((int) 1e9);
        Evaluator.displayTime();
        Evaluator.reportPerformance(s, re2finedCombineSimplify3D(s, eps, new BigDecimal("0.5")));
        Evaluator.reportPerformance(s, refinedCombineSimplify3D(s, eps));
        Evaluator.reportPerformance(s, combineSimplify3D(s, eps));
        Evaluator.reportPerformance(s, greedySimplify3D(s, eps));
        Evaluator.reportPerformance(s, dpSimplify3D(s, eps, (int) 1e6));
        Evaluator.reportPerformance(s, dpSimplify3D(s, eps, (int) 1e7));
        Evaluator.reportPerformance(s, dpSimplify3D(s, eps, (int) 1e8));
    }

    public void evaluate3D3() {
        Series3D s = new Series3D((int) 1e4, (int) 1e9, 2);
        BigDecimal eps = new BigDecimal("2e8");
        BigDecimal eps_ = new BigDecimal("0.70710678118e9");
        Evaluator.displayTime();
        System.out.println("re3:");
        Evaluator.reportPerformance(s, re3finedCombineSimplify3D(s, eps, 5));
        Evaluator.reportPerformance(s, re3finedCombineSimplify3D(s, eps, 10));
        Evaluator.reportPerformance(s, re3finedCombineSimplify3D(s, eps, 15));
        Evaluator.reportPerformance(s, re3finedCombineSimplify3D(s, eps, 20));
        System.out.println("re12:");
        Evaluator.reportPerformance(s, re2finedCombineSimplify3D(s, eps, new BigDecimal("0.5")));
        Evaluator.reportPerformance(s, refinedCombineSimplify3D(s, eps));
        Evaluator.reportPerformance(s, combineSimplify3D(s, eps));
        System.out.println("base:");
        Evaluator.reportPerformance(s, RDPSimplify3D(s, eps));
        Evaluator.reportPerformance(s, greedySimplify3D(s, eps));
        Evaluator.reportPerformance(s, dpSimplify3D(s, eps, (int) 1e6));
        Evaluator.reportPerformance(s, dpSimplify3D(s, eps, (int) 1e7));
        Evaluator.reportPerformance(s, dpSimplify3D(s, eps, (int) 1e8));
    }

    public void evaluate3D4(Series3D s, BigDecimal eps, Vector<Long> space, Vector<Long> time) {
        Evaluator.getSetTime();
        Evaluator.recordPerformance(s, refinedCombineSimplify3D(s, eps), space, time);
        Evaluator.recordPerformance(s, re2finedCombineSimplify3D(s, eps, new BigDecimal("0.5")), space, time);
        Evaluator.recordPerformance(s, re3finedCombineSimplify3D(s, eps, 10), space, time);
        Evaluator.recordPerformance(s, RDPSimplify3D(s, eps), space, time);
        Evaluator.recordPerformance(s, greedySimplify3D(s, eps), space, time);
        Evaluator.recordPerformance(s, dpSimplify3D(s, eps, (int) 1e8), space, time);
    }
}
