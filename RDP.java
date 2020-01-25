import java.math.BigDecimal;
import java.util.Vector;

public class RDP extends TS {

    public String name() {
        return "S-RDP";
    }

    private Vector<PointKD> recursion(SeriesKD s, int left, int right, Vector<BigDecimal> eps) {
        Vector<PointKD> points = new Vector<PointKD>();
        PointKD p = s.get(left), q = s.get(right);
        if (right - left == 0) {
            points.add(p);
        }
        if (right - left == 1) {
            points.add(p);
            points.add(q);
        }
        if (right - left > 1) {
            Vect[] v = new Vect[s.dim()];
            for (int k = 1; k < s.dim(); k ++) 
                v[k] = new Vect(p.project(k), q.project(k));
            boolean flag = true;
            for (int i = left; i <= right; i ++) {
                PointKD r = s.get(i);
                for (int k = 1; k < s.dim(); k ++) {
                    Point r0 = r.project(k);
                    Point r0_ = v[k].interpolationX(r0.getX());
                    BigDecimal d0 = r0.getY().subtract(r0_.getY()).abs();
                    flag = flag && (d0.compareTo(eps.get(k - 1)) <= 0);
                }
            }
            if (flag) {
                points.add(p);
                points.add(q);
            }
            else {
                BigDecimal max = null;
                int maxarg = -1;
                for (int i = left; i <= right; i ++) {
                    PointKD r = s.get(i);
                    for (int k = 1; k < s.dim(); k ++) {
                        Point r0 = r.project(k);
                        Point r0_ = v[k].interpolationX(r0.getX());
                        BigDecimal d0 = r0.getY().subtract(r0_.getY()).abs();
                        if (max == null || d0.compareTo(max) > 0) {
                            max = d0;
                            maxarg = i;
                        }
                    }
                }
                Vector<PointKD> L = recursion(s, left, maxarg, eps);
                Vector<PointKD> R = recursion(s, maxarg, right, eps);
                for (int i = 0; i < L.size() - 1; i ++) 
                    points.add(L.get(i));
                for (int i = 0; i < R.size(); i ++) 
                    points.add(R.get(i));
            }
        }
        return points;
    }

    public SeriesKD simplifyKD(SeriesKD s, Vector<BigDecimal> eps) {
        if (s.size() <= 2) return s;
        Vector<PointKD> points = recursion(s, 0, s.size() - 1, eps);
        return new SeriesKD(points);
    }
}
