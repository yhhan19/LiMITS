import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Random;
import java.util.Vector;

public class Series3D {

    private Vector<Point3D> data;

    public Series3D(Vector<Point3D> data) {
        this.data = new Vector<Point3D>();
        for (int i = 0; i < data.size(); i ++) 
            this.data.add(data.get(i));
    }

    public Series3D(int N, int bound, int mono) {
        data = new Vector<Point3D>();
        Random rand = new Random();
        BigInteger x = new BigInteger("0");
        BigInteger y = new BigInteger("0");
        BigInteger z = new BigInteger("0");
        for (int i = 0; i < N; i ++) {
            Point3D p = new Point3D(x, y, z);
            data.add(p);
            String str = String.valueOf(rand.nextInt(bound));
            BigInteger delta = new BigInteger(str);
            x = x.add(BigInteger.ONE);
            if (rand.nextInt(mono) == 0)
                y = y.add(delta);
            else 
                y = y.subtract(delta);
            str = String.valueOf(rand.nextInt(bound));
            delta = new BigInteger(str);
            str = String.valueOf(rand.nextInt(bound));
            if (rand.nextInt(mono) == 0)
                z = z.add(delta);
            else 
                z = z.subtract(delta);
        }
        System.out.println("input: " + data.size());
        //simplify();
    }

    public Series3D(Series s0, Series s1) {
        data = new Vector<Point3D>();
        int i = 0, j = 0;
        while (i < s0.size() && j < s1.size()) {
            int delta = Arithmetic.sgn(s0.get(i).getX().subtract(s1.get(j).getX()));
            switch (delta) {
                case 0: 
                    data.add(new Point3D(s0.get(i).getX(), s0.get(i).getY(), s1.get(j).getY()));
                    i ++; j ++;
                    break;
                case -1: 
                    if (j != 0) {
                        Point p = s1.get(j - 1), q = s1.get(j);
                        Point r = Point.interpolationX(p, q, s0.get(i).getX());
                        data.add(new Point3D(s0.get(i).getX(), s0.get(i).getY(), r.getY()));
                    }
                    i ++; 
                    break;
                case 1: 
                    if (i != 0) {
                        Point p = s0.get(i - 1), q = s0.get(i);
                        Point r = Point.interpolationX(p, q, s1.get(j).getX());
                        data.add(new Point3D(s1.get(j).getX(), r.getY(), s1.get(j).getY()));
                    }
                    j ++;
                    break;
                default: 
            }
        }
        while (i < s0.size()) {
            Point p = s1.get(s1.size() - 2), q = s1.get(s1.size() - 1);
            Point r = Point.interpolationX(p, q, s0.get(i).getX());
            data.add(new Point3D(s0.get(i).getX(), s0.get(i).getY(), r.getY()));
            i ++;
        }
        while (j < s1.size()) {
            Point p = s0.get(s0.size() - 2), q = s0.get(s1.size() - 1);
            Point r = Point.interpolationX(p, q, s1.get(j).getX());
            data.add(new Point3D(s1.get(j).getX(), r.getY(), s1.get(j).getY()));
            j ++;
        }
    }

    public int size() {
        return data.size();
    }

    public Point3D get(int i) {
        return data.get(i);
    }

    public Series projectZ() {
        Vector<Point> points = new Vector<Point>();
        for (int i = 0; i < size(); i ++) 
            points.add(get(i).projectZ());
        return new Series(points);
    }

    public Series projectY() {
        Vector<Point> points = new Vector<Point>();
        for (int i = 0; i < size(); i ++) 
            points.add(get(i).projectY());
        return new Series(points);
    }

    public Point distance(Series3D that) {
        BigDecimal e0 = BigDecimal.ZERO, e1 = BigDecimal.ZERO;
        for (int i = 0, j = 0; i < this.size() && j < that.size(); ) {
            if (this.get(i).getX().compareTo(that.get(j).getX()) == -1) {
                if (j != 0) {
                    Point p0 = that.get(j - 1).projectZ(), q0 = that.get(j).projectZ();
                    Point r0 = Point.interpolationX(p0, q0, this.get(i).getX());
                    BigDecimal d0 = r0.getY().subtract(this.get(i).getY()).abs();
                    if (d0.compareTo(e0) == 1) e0 = d0;
                    Point p1 = that.get(j - 1).projectY(), q1 = that.get(j).projectY();
                    Point r1 = Point.interpolationX(p1, q1, this.get(i).getX());
                    BigDecimal d1 = r1.getY().subtract(this.get(i).getZ()).abs();
                    if (d1.compareTo(e1) == 1) e1 = d1;
                }
                i ++;
            }
            else {
                if (i != 0) {
                    Point p0 = this.get(i - 1).projectZ(), q0 = this.get(i).projectZ();
                    Point r0 = Point.interpolationX(p0, q0, that.get(j).getX());
                    BigDecimal d0 = r0.getY().subtract(that.get(j).getY()).abs();
                    if (d0.compareTo(e0) == 1) e0 = d0;
                    Point p1 = this.get(i - 1).projectY(), q1 = this.get(i).projectY();
                    Point r1 = Point.interpolationX(p1, q1, that.get(j).getX());
                    BigDecimal d1 = r1.getY().subtract(that.get(j).getZ()).abs();
                    if (d1.compareTo(e1) == 1) e1 = d1;
                }
                j ++;
            }
        }
        return new Point(e0, e1);
    }
}