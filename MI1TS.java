import java.math.BigDecimal;
import java.util.Vector;

public class MI1TS extends TS {

    private BigDecimal r;

    public MI1TS(BigDecimal r) {
        this.r = r;
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
    
    public SeriesKD simplifyKD(SeriesKD s, Vector<BigDecimal> eps) {
        SeriesKD t = null;
        for (int dim = 1; dim < s.dim(); dim ++) {
            if (dim == 1) {
                Series s_ = s.project(dim);
                Series t_ = simplify(s_, eps.get(dim - 1));
                t = new SeriesKD(t, t_);
            }
            else {
                Series s_ = s.project(dim);
                Vector<Point> points = new Vector<Point>();
                Point first = null;
                Range last = new Range(s_.get(0).getY().add(eps.get(dim - 1)), s_.get(0).getY().subtract(eps.get(dim - 1)));
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
                    Series t__ = simplify(s__, eps.get(dim - 1), last, r, r, true);
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
}
