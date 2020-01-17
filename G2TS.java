import java.math.BigDecimal;
import java.util.Vector;

public class G2TS extends TS {

    public String name() {
        return "W-GRE";
    }

    public SeriesKD simplifyKD(SeriesKD s, Vector<BigDecimal> eps) {
        Vector<PointKD> points = new Vector<PointKD>();
        Vect[] low = new Vect[s.dim()], high = new Vect[s.dim()];
        Point[] p = new Point[s.dim()], q = new Point[s.dim()];
        PointKD point = s.get(0);
        for (int i = 0, j = 1; j < s.size(); i = j - 1) {
            points.add(point);
            for (int k = 1; k < s.dim(); k ++) {
                p[k] = point.project(k);
                low[k] = new Vect(p[k], p[k].add(new Vect(BigDecimal.ZERO, eps.get(k - 1).negate())));
                high[k] = new Vect(p[k], p[k].add(new Vect(BigDecimal.ZERO, eps.get(k - 1))));
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
                            data.add(pm.getY().add(BigDecimal.ZERO, Arithmetic.MATH_CONTEXT));
                        }
                        point = new PointKD(data);
                    }
                }
                boolean flag = true;
                for (int k = 1; k < s.dim(); k ++) {
                    Vect low_ = new Vect(p[k], q[k].add(new Vect(BigDecimal.ZERO, eps.get(k - 1).negate())));
                    Vect high_ = new Vect(p[k], q[k].add(new Vect(BigDecimal.ZERO, eps.get(k - 1))));
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
                    data.add(pm.getY().add(BigDecimal.ZERO, Arithmetic.MATH_CONTEXT));
                }
                points.add(new PointKD(data));
            }
        }
        return new SeriesKD(points);
    }
}
