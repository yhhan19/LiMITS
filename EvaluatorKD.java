import java.math.BigDecimal;
import java.time.Clock;
import java.util.Collections;
import java.util.Vector;

public class EvaluatorKD {

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

    public SeriesKD greedySimplify(SeriesKD s, BigDecimal eps) {
        Vector<PointKD> points = new Vector<PointKD>();
        Vect[] low = new Vect[s.dim()], high = new Vect[s.dim()];
        Vect pos = new Vect(BigDecimal.ZERO, eps), neg = new Vect(BigDecimal.ZERO, eps.negate());
        for (int i = 0, j = 1; j < s.size(); i = j - 1) {
            points.add(s.get(i));
            for (int k = 1; k < s.dim(); k ++) {
                Point p = s.get(i).project(k);
                low[k] = new Vect(p, p.add(neg));
                high[k] = new Vect(p, p.add(pos));
            }
            for (boolean bet = true; j < s.size(); j ++) {
                for (int k = 1; k < s.dim(); k ++) {
                    Point p = s.get(i).project(k), q = s.get(j).project(k); 
                    Vect v = new Vect(p, q);
                    bet = bet && v.between(low[k], high[k]);
                    Vect low_ = new Vect(p, q.add(neg));
                    Vect high_ = new Vect(p, q.add(pos));
                    if (low[k].cross(low_).signum() > 0) low[k] = low_;
                    if (high[k].cross(high_).signum() < 0) high[k] = high_;
                }
                if (! bet) break;
            }
        }
        points.add(s.get(s.size() - 1));
        return new SeriesKD(points);
    }

    public SeriesKD greedy2Simplify(SeriesKD s, BigDecimal eps) {
        Vector<PointKD> points = new Vector<PointKD>();
        Vect[] low = new Vect[s.dim()], high = new Vect[s.dim()];
        Point[] p = new Point[s.dim()], q = new Point[s.dim()];
        Vect pos = new Vect(BigDecimal.ZERO, eps), neg = new Vect(BigDecimal.ZERO, eps.negate());
        PointKD point = s.get(0);
        for (int i = 0, j = 1; j < s.size(); i = j - 1) {
            points.add(point);
            for (int k = 1; k < s.dim(); k ++) {
                p[k] = point.project(k);
                low[k] = new Vect(p[k], p[k].add(neg));
                high[k] = new Vect(p[k], p[k].add(pos));
            }
            point = null;
            while (j < s.size()) {
                boolean bet = true;
                for (int k = 1; k < s.dim(); k ++) {
                    q[k] = s.get(j).project(k);
                    Vect v = new Vect(p[k], q[k]);
                    bet = bet && (v.between(low[k], high[k]));
                }
                if (j != i + 1) {
                    if (bet) {
                        point = s.get(j - 1);
                    }
                    else {
                        Vector<BigDecimal> data = new Vector<BigDecimal>();
                        data.add(s.get(j - 1).get(0));
                        for (int k = 1; k < s.dim(); k ++) {
                            Point pl = low[k].interpolationX(s.get(j - 1).get(0));
                            Point ph = high[k].interpolationX(s.get(j - 1).get(0));
                            Point pm = (new Vect(pl, ph)).interpolationYratio(new BigDecimal("0.5"));
                            data.add(pm.getY().add(BigDecimal.ZERO, Arithmetic.MC));
                        }
                        point = new PointKD(data);
                    }
                }
                boolean flag = true;
                for (int k = 1; k < s.dim(); k ++) {
                    Vect low_ = new Vect(p[k], q[k].add(neg));
                    Vect high_ = new Vect(p[k], q[k].add(pos));
                    if (low[k].cross(low_).signum() > 0) low[k] = low_;
                    if (high[k].cross(high_).signum() < 0) high[k] = high_;
                    flag = flag && (low[k].cross(high[k]).signum() >= 0);
                }
                if (! flag) break;
                j ++;
            }
        }
        boolean bet = true;
        for (int k = 1; k < s.dim(); k ++) {
            q[k] = s.get(s.size() - 1).project(k);
            Vect v = new Vect(p[k], q[k]);
            bet = bet && (v.between(low[k], high[k]));
        }
        if (true) {
            if (bet) {
                points.add(s.get(s.size() - 1));
            }
            else {
                Vector<BigDecimal> data = new Vector<BigDecimal>();
                data.add(s.get(s.size() - 1).get(0));
                for (int k = 1; k < s.dim(); k ++) {
                    Point pl = low[k].interpolationX(s.get(s.size() - 1).get(0));
                    Point ph = high[k].interpolationX(s.get(s.size() - 1).get(0));
                    Point pm = (new Vect(pl, ph)).interpolationYratio(new BigDecimal("0.5"));
                    data.add(pm.getY().add(BigDecimal.ZERO, Arithmetic.MC));
                }
                points.add(new PointKD(data));
            }
        }
        return new SeriesKD(points);
    }

    public SeriesKD combineSimplify(SeriesKD s, BigDecimal eps) {
        SeriesKD t = null;
        for (int i = 1; i < s.dim(); i ++) {
            Series s_ = s.project(i);
            Series t_ = simplify(s_, eps);
            t = new SeriesKD(t, t_);
        }
        if (t.size() > s.size()) return s;
        return t;
    }

    public SeriesKD refinedCombineSimplify(SeriesKD s, BigDecimal eps) {
        SeriesKD t = null;
        for (int dim = 1; dim < s.dim(); dim ++) {
            if (dim == 1) {
                Series s_ = s.project(dim);
                Series t_ = simplify(s_, eps);
                t = new SeriesKD(t, t_);
            }
            else {
                Series s_ = s.project(dim);
                Vector<Point> points = new Vector<Point>();
                Point first = null;
                BigDecimal last = null;
                for (int i = 1, j = 1, delta; i < t.size(); i ++) {
                    Vector<Point> buffer = new Vector<Point>();
                    BigDecimal x = t.get(i).get(0);
                    if (first != null) buffer.add(first);
                    for (delta = -1, j = j - 1; j < s_.size() && delta < 0; j ++) {
                        delta = Arithmetic.sgn(s_.get(j).getX().subtract(x));
                        Point p = delta <= 0 ? s_.get(j) : Point.interpolationX(s_.get(j - 1), s_.get(j), x);
                        first = delta <= 0 ? null : p;
                        buffer.add(p);
                    }
                    Series s__ = new Series(buffer), t__ = null;
                    if (last == null) {
                        t__ = simplify(s__, eps);
                        for (int k = 0; k < t__.size(); k ++) 
                            points.add(t__.get(k));
                    }
                    else {
                        t__ = simplify(s__, eps, last);
                        for (int k = 1; k < t__.size(); k ++) 
                            points.add(t__.get(k));
                    }
                    last = t__.lastElement().getY();
                }
                Series t_ = new Series(points);
                t = new SeriesKD(t, t_);
            }
        }
        if (t.size() > s.size()) return s;
        return t;
    }

    public SeriesKD refinedCombineSimplify(SeriesKD s, BigDecimal eps, BigDecimal r) {
        SeriesKD t = null;
        for (int dim = 1; dim < s.dim(); dim ++) {
            if (dim == 1) {
                Series s_ = s.project(dim);
                Series t_ = simplify(s_, eps);
                t = new SeriesKD(t, t_);
            }
            else {
                Series s_ = s.project(dim);
                Vector<Point> points = new Vector<Point>();
                Point first = null;
                Range last = new Range(s_.get(0).getY().add(eps), s_.get(0).getY().subtract(eps));
                points.add(new Point(s_.get(0).getX(), last.interpolation(r)));
                for (int i = 1, j = 1, delta; i < t.size(); i ++) {
                    Vector<Point> buffer = new Vector<Point>();
                    BigDecimal x = t.get(i).get(0);
                    if (first != null) buffer.add(first);
                    for (delta = -1, j = j - 1; j < s_.size() && delta < 0; j ++) {
                        delta = Arithmetic.sgn(s_.get(j).getX().subtract(x));
                        Point p = delta <= 0 ? s_.get(j) : Point.interpolationX(s_.get(j - 1), s_.get(j), x);
                        first = delta <= 0 ? null : p;
                        buffer.add(p);
                    }
                    Series s__ = new Series(buffer);
                    Series t__ = simplify(s__, eps, last, r);
                    for (int k = 1; k < t__.size(); k ++) 
                        points.add(t__.get(k));
                }
                Series t_ = new Series(points);
                t = new SeriesKD(t, t_);
            }
        }
        if (t.size() > s.size()) return s;
        return t;
    }

    private BigDecimal sample(int R, int u) {
        BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(R), Arithmetic.MC);
        if (u == 0) return step.multiply(new BigDecimal(1e-6));
        if (u == R) return step.multiply(BigDecimal.ONE.subtract(new BigDecimal(1e-6)));
        return step.multiply(new BigDecimal(u));
    }

    public SeriesKD refinedCombineSimplify(SeriesKD s, BigDecimal eps, int R) {
        SeriesKD t = null;
        for (int dim = 1; dim < s.dim(); dim ++) {
            if (dim == 1) {
                Series s_ = s.project(dim);
                Series t_ = simplify(s_, eps);
                t = new SeriesKD(null, t_);
            }
            else {
                Series s_ = s.project(dim);
                Point first = null;
                Vector<Point> points = new Vector<Point>();
                int weight[][][] = new int[t.size()][R + 1][R + 1];
                for (int i = 1, j = 1, delta; i < t.size(); i ++) {
                    Vector<Point> buffer = new Vector<Point>();
                    BigDecimal x = t.get(i).get(0);
                    if (first != null) buffer.add(first);
                    for (delta = -1, j = j - 1; j < s_.size() && delta < 0; j ++) {
                        delta = Arithmetic.sgn(s_.get(j).getX().subtract(x));
                        Point p = delta <= 0 ? s_.get(j) : Point.interpolationX(s_.get(j - 1), s_.get(j), x);
                        first = delta <= 0 ? null : p;
                        buffer.add(p);
                    }
                    Series s__ = new Series(buffer);
                    for (int u = 0; u <= R; u ++) {
                        BigDecimal ru = sample(R, u);
                        Range last = new Range(s__.get(0).getY().add(eps), s__.get(0).getY().subtract(eps));
                        Series t__ = simplify(s__, eps, last, ru);
                        for (int v = 0; v <= R; v ++) {
                            BigDecimal rv = sample(R, v);
                            BigDecimal y = (new Range(s__.lastElement().getY().add(eps), s__.lastElement().getY().subtract(eps))).interpolation(rv);
                            weight[i][u][v] = last.contains(y) ? t__.size() : t__.size() + 1;
                        }
                    }
                }
                int opt[][] = new int[t.size()][R + 1], pre[][] = new int[t.size()][R + 1];
                int min = -1, argmin = -1;
                for (int i = 0; i < t.size(); i ++) {
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
                        if (i == t.size() - 1 && (min == -1 || opt[i][u] < min)) {
                            min = opt[i][u];
                            argmin = u;
                        }
                    }
                }
                int arg[] = new int[t.size()];
                for (int i = t.size() - 1, j = argmin; i >= 0; j = pre[i][j], i --) {
                    arg[i] = j;
                }
                for (int i = 1, j = 1, delta; i < t.size(); i ++) {
                    Vector<Point> buffer = new Vector<Point>();
                    BigDecimal x = t.get(i).get(0);
                    if (first != null) buffer.add(first);
                    for (delta = -1, j = j - 1; j < s_.size() && delta < 0; j ++) {
                        delta = Arithmetic.sgn(s_.get(j).getX().subtract(x));
                        Point p = delta <= 0 ? s_.get(j) : Point.interpolationX(s_.get(j - 1), s_.get(j), x);
                        first = delta <= 0 ? null : p;
                        buffer.add(p);
                    }
                    Series s__ = new Series(buffer);
                    int u = arg[i - 1], v = arg[i];
                    BigDecimal ru = sample(R, u), rv = sample(R, v);
                    Range last = new Range(s__.get(0).getY().add(eps), s__.get(0).getY().subtract(eps));
                    Series t__ = simplify(s__, eps, last, ru, rv);
                    if (i == 1) {
                        for (int k = 0; k < t__.size(); k ++) 
                            points.add(t__.get(k));
                    }
                    else {
                        for (int k = 1; k < t__.size(); k ++) 
                            points.add(t__.get(k));
                    }
                }
                Series t_ = new Series(points);
                System.out.println(t_.size() + " " + min);
                t = new SeriesKD(t, t_);
            }
        }
        return t;
    }

    /*
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
    */

    public static BigDecimal reportPerformance(Series3D s, Series3D t) {
        displayTime();
        setTime();
        if (t == null) return null;
        System.out.println("output: " + t.size() + " ratio: " + String.format("%.2f%s", (double) t.size() / s.size() * 100, "%"));
        BigDecimal error = t.distanceL2(s);
        System.out.println("error: " + Arithmetic.format(error));
        return error;
    }

    public static Point recordPerformance(Series3D s, Series3D t, Vector<Long> space, Vector<Long> time) {
        if (t == null) return null;
        time.add(EvaluatorKD.getSetTime());
        space.add((long) t.size());
        System.out.print("- ");
        return t.distance(s);
    }
}
