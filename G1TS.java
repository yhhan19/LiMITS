import java.math.BigDecimal;
import java.util.Vector;

public class G1TS extends TS {

    public SeriesKD simplifyKD(SeriesKD s, Vector<BigDecimal> eps) {
        Vector<PointKD> points = new Vector<PointKD>();
        Vect[] low = new Vect[s.dim()], high = new Vect[s.dim()];
        for (int i = 0, j = 1; j < s.size(); i = j - 1) {
            points.add(s.get(i));
            for (int k = 1; k < s.dim(); k ++) {
                Point p = s.get(i).project(k);
                low[k] = new Vect(p, p.add(new Vect(BigDecimal.ZERO, eps.get(k - 1).negate())));
                high[k] = new Vect(p, p.add(new Vect(BigDecimal.ZERO, eps.get(k - 1))));
            }
            for (boolean bet = true; j < s.size(); j ++) {
                for (int k = 1; k < s.dim(); k ++) {
                    Point p = s.get(i).project(k), q = s.get(j).project(k); 
                    Vect v = new Vect(p, q);
                    bet = bet && v.between(low[k], high[k]);
                    Vect low_ = new Vect(p, q.add(new Vect(BigDecimal.ZERO, eps.get(k - 1).negate())));
                    Vect high_ = new Vect(p, q.add(new Vect(BigDecimal.ZERO, eps.get(k - 1))));
                    if (low[k].cross(low_).signum() > 0) low[k] = low_;
                    if (high[k].cross(high_).signum() < 0) high[k] = high_;
                }
                if (! bet) break;
            }
        }
        points.add(s.get(s.size() - 1));
        return new SeriesKD(points);
    }
}
