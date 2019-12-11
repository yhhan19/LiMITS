import java.math.BigDecimal;
import java.time.Clock;
import java.util.Collections;
import java.util.Vector;

public class Evaluator {

    private Clock clock;
    private long firstTime;
    
    public Evaluator() {
        clock = Clock.systemDefaultZone();
        firstTime = clock.millis();
    }

    private void displayTime() {
        long curTime = clock.millis();
        System.out.println("time: " + (curTime - firstTime) + "ms");
        firstTime = curTime;
    }

    public Series simplify(Series s, BigDecimal eps) {
        Polytope p = new Polytope(s, eps);
        Vector<Point> points = p.linkPath();
        Series t = new Series(points);
        //System.out.println("output: " + t.size());
        return t;
    }

    public Series simplify(Series s, BigDecimal eps, BigDecimal y) {
        Polytope p = new Polytope(s, eps);
        Vector<Point> points = p.linkPath(y);
        Series t = new Series(points);
        //System.out.println("output: " + t.size());
        return t;
    }

    public Series simplify(Series s, BigDecimal eps, Range y) {
        Polytope p = new Polytope(s, eps);
        Vector<Point> points = p.linkPath(y);
        Series t = new Series(points);
        //System.out.println("output: " + t.size());
        return t;
    }

    public Series simplify(Series s, BigDecimal eps, Range y, BigDecimal r) {
        BigDecimal y0 = y.interpolation(r);
        Polytope p = new Polytope(s, eps);
        Vector<Point> points = p.linkPath(y0, y, r);
        Series t = new Series(points);
        //System.out.println("output: " + t.size());
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
        System.out.println("output: " + points.size());
        return new Series(points);
    }

    public Series3D combineSimplify3D(Series3D s, BigDecimal eps) {
        Series s0 = s.projectZ();
        Series t0 = simplify(s0, eps);
        Series s1 = s.projectY();
        Series t1 = simplify(s1, eps);
        Series3D t = new Series3D(t0, t1);
        System.out.println("output: " + t.size() + " " + t0.size() + " " + t1.size());
        return t;
    }

    public Series3D combineSimplify3D(Series3D s, BigDecimal eps, BigDecimal r0, BigDecimal r1) {
        Series s0 = s.projectZ();
        Series t0 = simplify(s0, eps, r0);
        Series s1 = s.projectY();
        Series t1 = simplify(s1, eps, r1);
        Series3D t = new Series3D(t0, t1);
        System.out.println("output: " + t.size() + " " + t0.size() + " " + t1.size());
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
        System.out.println("output: " + t.size() + " " + t0.size() + " " + t1.size());
        return t;
    }

    public Series3D reRefinedCombineSimplify3D(Series3D s, BigDecimal eps, BigDecimal r) {
        Series s0 = s.projectZ();
        Series t0 = simplify(s0, eps);
        Series s1 = s.projectY();
        Vector<Point> points = new Vector<Point>();
        Point first = null;
        Range last =null;
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
            if (last == null) {
                last = new Range(s1.get(0).getY().add(eps), s1.get(0).getY().subtract(eps));
                points.add(new Point(s1.get(0).getX(), last.interpolation(r)));
            }
            Series t1_ = simplify(s1_, eps, last, r);
            for (int k = 1; k < t1_.size(); k ++) 
                points.add(t1_.get(k));
        }
        Series t1 = new Series(points);
        Series3D t = new Series3D(t0, t1);
        System.out.println("output: " + t.size() + " " + t0.size() + " " + t1.size());
        return t;
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
        System.out.println("output: " + points.size());
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
        System.out.println("output: " + points.size());
        return new Series3D(points);
    }

    public static BigDecimal getError(Series s, Series t) {
        BigDecimal error = t.distance(s);
        System.out.print("head: ");
        t.get(0).subtract(s.get(0)).getTo().display();
        System.out.print("tail: ");
        t.get(t.size() - 1).subtract(s.get(s.size() - 1)).getTo().display();
        System.out.println("error: " + Arithmetic.format(error));
        return error;
    }

    public static Point getError(Series3D s, Series3D t) {
        Point error = t.distance(s);
        System.out.print("head: ");
        t.get(0).projectZ().subtract(s.get(0).projectZ()).getTo().display();
        t.get(0).projectY().subtract(s.get(0).projectY()).getTo().display();
        System.out.print("tail: ");
        t.get(t.size() - 1).projectZ().subtract(s.get(s.size() - 1).projectZ()).getTo().display();
        t.get(t.size() - 1).projectY().subtract(s.get(s.size() - 1).projectY()).getTo().display();
        System.out.print("error: " + Arithmetic.format(error.getX()));
        System.out.println(" " + Arithmetic.format(error.getY()));
        return error;
    }

    private void evaluate() {
        Series s = new Series((int) 1e5, (int) 1e9);
        BigDecimal eps = new BigDecimal((int) 1e9);
        displayTime();
        Series t0 = simplify(s, eps);
        Evaluator.getError(s, t0);
        displayTime();
        Series t1 = simplify(s, eps, (new BigDecimal("0.5")).multiply(eps));
        Evaluator.getError(s, t1);
        displayTime();
        Series t2 = greedySimplify(s, eps);
        Evaluator.getError(s, t2);
        displayTime();
        Series t30 = dpSimplify(s, eps, (int) 1e7);
        Evaluator.getError(s, t30);
        displayTime();
        Series t31 = dpSimplify(s, eps, (int) 1e8);
        Evaluator.getError(s, t31);
        displayTime();
    }

    private void evaluate3D() {
        Series3D s = new Series3D((int) 1e5, (int) 1e9, 2);
        BigDecimal eps = new BigDecimal((int) 1e9);
        displayTime();
        Series3D t0 = combineSimplify3D(s, eps);
        Evaluator.getError(s, t0);
        displayTime();
        Series3D t1 = combineSimplify3D(s, eps, (new BigDecimal("0.5")).multiply(eps), (new BigDecimal("-0.5")).multiply(eps));
        Evaluator.getError(s, t1);
        displayTime();
        Series3D t2 = greedySimplify3D(s, eps);
        Evaluator.getError(s, t2);
        displayTime();
        Series3D t30 = dpSimplify3D(s, eps, (int) 1e7);
        Evaluator.getError(s, t30);
        displayTime();
        Series3D t31 = dpSimplify3D(s, eps, (int) 1e8);
        Evaluator.getError(s, t31);
        displayTime();
    }

    public void evaluate3D2() {
        Series3D s = new Series3D((int) 1e4, (int) 1e9, 2);
        BigDecimal eps = new BigDecimal((int) 1e9);
        displayTime();
        Series3D t4 = reRefinedCombineSimplify3D(s, eps, new BigDecimal("0.5"));
        Evaluator.getError(s, t4);
        displayTime();
        Series3D t0 = refinedCombineSimplify3D(s, eps);
        Evaluator.getError(s, t0);
        displayTime();
        Series3D t1 = combineSimplify3D(s, eps);
        Evaluator.getError(s, t1);
        displayTime();
        Series3D t2 = greedySimplify3D(s, eps);
        Evaluator.getError(s, t2);
        displayTime();
        Series3D t30 = dpSimplify3D(s, eps, (int) 1e7);
        Evaluator.getError(s, t30);
        displayTime();
        Series3D t31 = dpSimplify3D(s, eps, (int) 1e8);
        Evaluator.getError(s, t31);
        displayTime();
    }

    public static void main(String args[]) {
        Evaluator runtime = new Evaluator();
        //runtime.evaluate();
        //runtime.evaluate3D();
        runtime.evaluate3D2();
    }
}
