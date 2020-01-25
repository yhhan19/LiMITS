import java.math.BigDecimal;
import java.util.Vector;

public class DPTS extends TS {

    private final int times;

    public DPTS(int times) {
        this.times = times;
    }

    public String name() {
        return "S-OPT";
    }

    public SeriesKD simplifyKD(SeriesKD s, Vector<BigDecimal> eps) {
        if (s.size() <= 2) return s;
        int backward = times > 0 ? times / s.size() : s.size();
        boolean visible[][] = new boolean[s.size()][backward];
        Vect[] low = new Vect[s.dim()], high = new Vect[s.dim()];
        Point[] p = new Point[s.dim()], q = new Point[s.dim()];
        for (int i = 0; i < s.size(); i ++) {
            for (int k = 1; k < s.dim(); k ++) {
                p[k] = s.get(i).project(k);
                low[k] = new Vect(p[k], p[k].add(new Vect(BigDecimal.ZERO, eps.get(k - 1).negate())));
                high[k] = new Vect(p[k], p[k].add(new Vect(BigDecimal.ZERO, eps.get(k - 1))));
            }
            for (int j = i - 1; j >= 0 && j > i - backward; j --) {
                boolean flag = true;
                for (int k = 1; k < s.dim(); k ++) {
                    q[k] = s.get(j).project(k);
                    Vect c = new Vect(p[k], q[k]);
                    Vect low_ = new Vect(p[k], q[k].add(new Vect(BigDecimal.ZERO, eps.get(k - 1).negate())));
                    Vect high_ = new Vect(p[k], q[k].add(new Vect(BigDecimal.ZERO, eps.get(k - 1))));
                    if (low[k].cross(low_).signum() < 0) low[k] = low_;
                    if (high[k].cross(high_).signum() > 0) high[k] = high_;
                    flag = flag && c.between(high[k], low[k]);
                }
                visible[i][j - i + backward] = flag;
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
        Vector<PointKD> points = new Vector<PointKD>();
        points.setSize(opt[s.size() - 1]);
        for (int i = s.size() - 1, j = opt[s.size() - 1] - 1; i != -1; i = pre[i], j --) 
            points.set(j, s.get(i));
        return new SeriesKD(points);
    }
}
