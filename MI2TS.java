import java.math.BigDecimal;
import java.util.Vector;

public class MI2TS extends TS {

    private int R;

    public MI2TS(int R) {
        this.R = R;
    }

    private Series simplify(Series s, BigDecimal eps) {
        Polytope p = new Polytope(s, eps);
        Vector<Point> points = p.linkPath();
        Series t = new Series(points);
        return t;
    }

    private Series simplify(Series s, BigDecimal eps, Range y, BigDecimal start, BigDecimal end, boolean relative) {
        Polytope p = new Polytope(s, eps);
        Vector<Point> points = p.linkPath(y, start, end, relative);
        Series t = new Series(points);
        return t;
    }
    
    private BigDecimal sample(int R, int u) {
        BigDecimal step = BigDecimal.ONE.divide(new BigDecimal(R), Arithmetic.MC);
        if (u == 0) return step.multiply(new BigDecimal("1e-6"));
        if (u == R) return step.multiply((new BigDecimal(u)).subtract(new BigDecimal("1e-6")));
        return step.multiply(new BigDecimal(u));
    }

    public SeriesKD simplifyKD(SeriesKD s, Vector<BigDecimal> eps) {
        SeriesKD t = null;
        for (int dim = 1; dim < s.dim(); dim ++) {
            Series s_ = s.project(dim), t_ = null;
            if (dim == 1) {
                t_ = simplify(s_, eps.get(dim - 1));
            }
            else {
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
                        Range last = new Range(s__.get(0).getY().add(eps.get(dim - 1)), s__.get(0).getY().subtract(eps.get(dim - 1)));
                        Series t__ = simplify(s__, eps.get(dim - 1), last, ru, ru, true);
                        for (int v = 0; v <= R; v ++) {
                            BigDecimal rv = sample(R, v);
                            BigDecimal y = (new Range(s__.lastElement().getY().add(eps.get(dim - 1)), s__.lastElement().getY().subtract(eps.get(dim - 1)))).interpolation(rv);
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
                for (int i = t.size() - 1, j = argmin; i >= 0; j = pre[i][j], i --) 
                    arg[i] = j;
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
                    BigDecimal ru = sample(R, arg[i - 1]), rv = sample(R, arg[i]);
                    Range last = new Range(s__.get(0).getY().add(eps.get(dim - 1)), s__.get(0).getY().subtract(eps.get(dim - 1)));
                    Series t__ = simplify(s__, eps.get(dim - 1), last, ru, rv, false);
                    if (i == 1) points.add(t__.get(0));
                    for (int k = 1; k < t__.size(); k ++) 
                        points.add(t__.get(k));
                }
                t_ = new Series(points);
            }
            t = new SeriesKD(t, t_);
            //System.out.println(t.size());
            if (t.size() > s.size()) return s;
        }
        return t;
    }
}
