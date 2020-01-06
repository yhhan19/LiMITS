import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Random;
import java.util.Vector;

public class SeriesKD {

    private Vector<PointKD> data;
    private int dim;

    public SeriesKD(Vector<PointKD> data) {
        this.dim = data.get(0).dim();
        this.data = new Vector<PointKD>();
        for (int i = 0; i < data.size(); i ++) 
            this.data.add(data.get(i));
    }

    public SeriesKD(int size, int dim, int bound, int mono) {
        this.dim = dim;
        data = new Vector<PointKD>();
        Random rand = new Random();
        Vector<BigDecimal> p = new Vector<BigDecimal>();
        for (int i = 0; i < dim; i ++) 
            p.add(new BigDecimal("0"));
        for (int i = 0; i < size; i ++) {
            data.add(new PointKD(p));
            p.set(0, p.get(0).add(BigDecimal.ONE));
            for (int j = 1; j < dim; j ++) {
                String str = String.valueOf(rand.nextInt(bound));
                BigDecimal delta = new BigDecimal(str);
                if (rand.nextInt(mono) == 0) 
                    p.set(j, p.get(j).add(delta));
                else 
                    p.set(j, p.get(j).subtract(delta));
            }
        }
    }

    public SeriesKD(int size, int dim, double b0, double b1, int mono, String t0, String t1) {
        this.dim = dim;
        data = new Vector<PointKD>();
        Random rand = new Random();
        double[] x = new double[dim], y = new double[dim], z = new double[dim];
        for (int i = 0; i < dim; i ++) {
            x[i] = y[i] = z[i] = 0;
        }
        for (int i = 0; i < size; i ++) {
            data.add(new PointKD(x));
            x[0] += 1;
            if (t1.equals("velocity")) {
                if (t0.equals("gaussian")) 
                    y = Arithmetic.gaussian(dim - 1);
                if (t0.equals("uniform")) 
                    y = Arithmetic.uniform(dim - 1);
                for (int j = 1; j < dim; j ++) {
                    if (rand.nextInt(mono) == 0)
                        y[j - 1] = - y[j - 1];
                    x[j] += y[j - 1] * b0;
                }
            }
            if (t1.equals("acceleration")) {
                if (t0.equals("gaussian")) 
                    z = Arithmetic.gaussian(dim - 1);
                if (t0.equals("uniform")) 
                    z = Arithmetic.uniform(dim - 1);
                for (int j = 1; j < dim; j ++) {
                    if (rand.nextInt(mono) == 0) 
                        z[j - 1] = - z[j - 1];
                    y[j - 1] += z[j - 1] * b1;
                    x[j] += y[j - 1] * b0;
                }
            }
        }
    }

    public SeriesKD(SeriesKD s0, Series s1) {
        if (s0 == null) {
            this.dim = 2;
            this.data = new Vector<PointKD>();
            Vector<BigDecimal> point = new Vector<BigDecimal>();
            point.add(BigDecimal.ZERO);
            point.add(BigDecimal.ZERO);
            for (int i = 0; i < s1.size(); i ++) {
                point.set(0, s1.get(i).getX());
                point.set(1, s1.get(i).getY());
                data.add(new PointKD(point));
            }
            return ;
        }
        this.dim = s0.dim() + 1;
        this.data = new Vector<PointKD>();
        Vector<BigDecimal> point = new Vector<BigDecimal>();
        for (int i = 0; i <= s0.dim(); i ++) 
            point.add(BigDecimal.ZERO);
        int i = 0, j = 0;
        while (i < s0.size() && j < s1.size()) {
            int delta = Arithmetic.sgn(s0.get(i).get(0).subtract(s1.get(j).getX()));
            switch (delta) {
                case 0: 
                    for (int k = 0; k < s0.dim(); k ++)
                        point.set(k, s0.get(i).get(k));
                    point.set(s0.dim(), s1.get(j).getY());
                    data.add(new PointKD(point));
                    i ++; j ++;
                    break;
                case -1:
                    if (j != 0) {
                        Point p = s1.get(j - 1), q = s1.get(j);
                        Point r = Point.interpolationX(p, q, s0.get(i).get(0));
                        for (int k = 0; k < s0.dim(); k ++) 
                            point.set(k, s0.get(i).get(k));
                        point.set(s0.dim(), r.getY());
                        data.add(new PointKD(point));
                    }
                    i ++;
                    break;
                case 1: 
                    if (i != 0) {
                        PointKD p = s0.get(i - 1), q = s0.get(i);
                        PointKD r = PointKD.interpolation(p, q, s1.get(j).getX(), 0);
                        for (int k = 0; k < s0.dim(); k ++) 
                            point.set(k, r.get(k));
                        point.set(s0.dim(), s1.get(j).getY());
                        data.add(new PointKD(point));
                    }
                    j ++;
                    break;
                default: 
            }
        }
        while (i < s0.size()) {
            Point p = s1.get(s1.size() - 2), q = s1.get(s1.size() - 1);
            Point r = Point.interpolationX(p, q, s0.get(i).get(0));
            for (int k = 0; k < s0.dim(); k ++) 
                point.set(k, s0.get(i).get(k));
            point.set(s0.dim(), r.getY());
            data.add(new PointKD(point));
            i ++;
        }
        while (j < s1.size()) {
            PointKD p = s0.get(s0.size() - 2), q = s0.get(s0.size() - 1);
            PointKD r = PointKD.interpolation(p, q, s1.get(j).getX(), 0);
            for (int k = 0; k < s0.dim(); k ++) 
                point.set(k, r.get(k));
            point.set(s0.dim(), s1.get(j).getY());
            data.add(new PointKD(point));
            j ++;
        }
    }

    public void display() {
        for (int i = 0; i < size(); i ++) 
            data.get(i).display();
        System.out.println();
    }

    public int dim() {
        return dim;
    }
    
    public int size() {
        return data.size();
    }

    public PointKD get(int i) {
        return data.get(i);
    }

    public PointKD lastElement() {
        return data.lastElement();
    }

    public Series project(int j) {
        Vector<Point> points = new Vector<Point>();
        for (int i = 0; i < size(); i ++) 
            points.add(get(i).project(j));
        return new Series(points);
    }

    public BigDecimal distanceLoo(SeriesKD that) {
        BigDecimal error = BigDecimal.ZERO;
        for (int i = 0, j = 0; i < this.size() && j < that.size(); ) {
            if (this.get(i).get(0).compareTo(that.get(j).get(0)) == -1) {
                if (j != 0) {
                    PointKD p = that.get(j - 1), q = that.get(j);
                    PointKD r = PointKD.interpolation(p, q, this.get(i).get(0), 0);
                    BigDecimal e = r.distanceLoo(this.get(i));
                    if (e.compareTo(error) == 1) error = e;
                }
                i ++;
            }
            else {
                if (i != 0) {
                    PointKD p = this.get(i - 1), q = this.get(i);
                    PointKD r = PointKD.interpolation(p, q, that.get(j).get(0), 0);
                    BigDecimal e = r.distanceLoo(that.get(j));
                    if (e.compareTo(error) == 1) error = e;
                }
                j ++;
            }
        }
        return error;
    }

    public double sphereLoo(SeriesKD that) {
        double error = 0;
        for (int i = 0, j = 0; i < this.size() && j < that.size(); ) {
            if (this.get(i).get(0).compareTo(that.get(j).get(0)) == -1) {
                if (j != 0) {
                    PointKD p = that.get(j - 1), q = that.get(j);
                    PointKD r = PointKD.interpolation(p, q, this.get(i).get(0), 0);
                    double e = r.sphereLoo(this.get(i));
                    if (e > error) error = e;
                }
                i ++;
            }
            else {
                if (i != 0) {
                    PointKD p = this.get(i - 1), q = this.get(i);
                    PointKD r = PointKD.interpolation(p, q, that.get(j).get(0), 0);
                    double e = r.sphereLoo(that.get(j));
                    if (e > error) error = e;
                }
                j ++;
            }
        }
        return error;
    }

    public double sphereL2(SeriesKD that) { // not accurate
        double error = 0;
        for (int i = 0, j = 0; i < this.size() && j < that.size(); ) {
            if (this.get(i).get(0).compareTo(that.get(j).get(0)) == -1) {
                if (j != 0) {
                    PointKD p = that.get(j - 1), q = that.get(j);
                    PointKD r = PointKD.interpolation(p, q, this.get(i).get(0), 0);
                    double e = r.sphereL2(this.get(i));
                    if (e > error) error = e;
                }
                i ++;
            }
            else {
                if (i != 0) {
                    PointKD p = this.get(i - 1), q = this.get(i);
                    PointKD r = PointKD.interpolation(p, q, that.get(j).get(0), 0);
                    double e = r.sphereL2(that.get(j));
                    if (e > error) error = e;
                }
                j ++;
            }
        }
        return error;
    }

    public double sphereL2(SeriesKD that, int T) { // sampling
        double error = 0;
        BigDecimal step = this.lastElement().get(0).subtract(this.get(0).get(0)).divide(new BigDecimal(T), Arithmetic.MC);
        BigDecimal t = step;
        for (int i = 0, j = 0; i < this.size() && j < that.size(); t = t.add(step)) {
            while (i < this.size() && this.get(i).get(0).compareTo(t) == -1) i ++;
            while (j < that.size() && that.get(j).get(0).compareTo(t) == -1) j ++;
            if (i < this.size() && j < that.size()) {
                PointKD p = PointKD.interpolation(this.get(i), this.get(i - 1), t, 0);
                PointKD q = PointKD.interpolation(that.get(j), that.get(j - 1), t, 0);
                double e = p.sphereL2(q);
                if (e > error) error = e;
            }
        }
        return error;
    }
}
