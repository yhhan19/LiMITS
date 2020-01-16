import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Vector;

public class Point {

    public static Point ORIGIN = new Point("0", "0");

    private BigDecimal x, y;

    public Point(int x, int y) {
        this.x = new BigDecimal(x);
        this.y = new BigDecimal(y);
    }

    public Point(BigInteger x, BigInteger y) {
        this.x = new BigDecimal(x);
        this.y = new BigDecimal(y);
    }

    public Point(String x, String y) {
        this.x = new BigDecimal(x);
        this.y = new BigDecimal(y);
    }

    public Point(BigDecimal x, BigDecimal y) {
        this.x = x;
        this.y = y;
    }

    public Point(Point p) {
        this.x = p.x;
        this.y = p.y;
    }

    public void display(MathContext mc) {
        BigDecimal x = this.x.round(mc);
        BigDecimal y = this.y.round(mc);
        System.out.print("(" + x + " " + y + ") ");
    }

    public void display() {
        String sx = Arithmetic.format(x);
        String sy = Arithmetic.format(y);
        System.out.print("(" + sx + " " + sy + ") ");
    }

    public BigDecimal getX() {
        return x;
    }

    public BigDecimal getY() {
        return y;
    }

    public Point add(Vect v) {
        BigDecimal x_ = x.add(v.getX());
        BigDecimal y_ = y.add(v.getY());
        return new Point(x_, y_);
    }

    public Vect subtract(Point p) {
        BigDecimal x_ = x.subtract(p.x);
        BigDecimal y_ = y.subtract(p.y);
        return new Vect(x_, y_);
    }

    public BigDecimal distL1(Point p) {
        BigDecimal dx = this.getX().subtract(p.getX());
        BigDecimal dy = this.getY().subtract(p.getY());
        dx = dx.abs();
        dy = dy.abs();
        BigDecimal d = dx.add(dy);
        return d;
    }

    public BigDecimal distL2(Point p, MathContext mc) {
        BigDecimal dx = this.getX().subtract(p.getX());
        BigDecimal dy = this.getY().subtract(p.getY());
        dx = dx.multiply(dx);
        dy = dy.multiply(dy);
        BigDecimal d = dx.add(dy);
        d = Arithmetic.sqrt(d, mc);
        return d;
    }

    public BigDecimal distL22(Point p) {
        BigDecimal dx = this.getX().subtract(p.getX());
        BigDecimal dy = this.getY().subtract(p.getY());
        dx = dx.multiply(dx);
        dy = dy.multiply(dy);
        BigDecimal d = dx.add(dy);
        return d;
    }

    public BigDecimal distLoo(Point p) {
        BigDecimal dx = this.getX().subtract(p.getX());
        BigDecimal dy = this.getY().subtract(p.getY());
        dx = dx.abs();
        dy = dy.abs();
        BigDecimal d = dx.max(dy);
        return d;
    }

    public int cartesianCompareTo(Point p) {
        int dx = this.getX().compareTo(p.getX());
        if (dx != 0) return dx;
        int dy = this.getY().compareTo(p.getY());
        return dy;
    }

    public boolean on(Point p, Point q) {
        Vect v0 = new Vect(this, p);
        Vect v1 = new Vect(this, q);
        BigDecimal c = v0.cross(v1);
        if (Arithmetic.sgn(c) == 0) {
            if (betweenX(p, q) && betweenY(p, q))
                return true;
        }
        return false;
    }

    public boolean on(Vect v) {
        return on(v.getFrom(), v.getTo());
    }

    public boolean outside(Point p, Point q) {
        int dx = Arithmetic.sgn(p.getX().subtract(q.getX()));
        if (dx != 0) {
            if (dx < 0) 
                return Arithmetic.sgn(x.subtract(q.getX())) >= 0;
            else 
                return Arithmetic.sgn(x.subtract(q.getX())) <= 0;
        }
        else {
            int dy = Arithmetic.sgn(p.getY().subtract(q.getY()));
            if (dy != 0) {
                if (dy < 0) 
                    return Arithmetic.sgn(y.subtract(q.getY())) >= 0;
                else 
                    return Arithmetic.sgn(y.subtract(q.getY())) <= 0;  
            }
            throw new NullPointerException("too close points");
        }
    }

    public boolean betweenX(Point p, Point q) {
        if (p.getX().compareTo(q.getX()) < 0) 
            return Arithmetic.sgn(x.subtract(p.getX())) >= 0
                && Arithmetic.sgn(x.subtract(q.getX())) <= 0;
        else 
            return Arithmetic.sgn(x.subtract(p.getX())) <= 0
                && Arithmetic.sgn(x.subtract(q.getX())) >= 0;
    }

    public boolean betweenY(Point p, Point q) {
        if (p.getY().compareTo(q.getY()) < 0) 
            return Arithmetic.sgn(y.subtract(p.getY())) >= 0
                && Arithmetic.sgn(y.subtract(q.getY())) <= 0;
        else 
            return Arithmetic.sgn(y.subtract(p.getY())) <= 0
                && Arithmetic.sgn(y.subtract(q.getY())) >= 0;
    }

    public static Point interpolationX(Point p, Point q, BigDecimal x) {
        Vect v = new Vect(p, q);
        if (Arithmetic.sgn(v.getX()) == 0) 
            return p;
        BigDecimal r = x.subtract(p.x).divide(v.getX(), Arithmetic.MC);
        return p.add(v.scalar(r));
    }

    public static Point interpolationY(Point p, Point q, BigDecimal y) {
        Vect v = new Vect(p, q);
        if (Arithmetic.sgn(v.getY()) == 0) 
            return p;
        BigDecimal r = y.subtract(p.y).divide(v.getY(), Arithmetic.MC);
        return p.add(v.scalar(r));
    }

    public boolean near(Point p, Point q, BigDecimal eps) {
        Point r = interpolationX(p, q, x);
        BigDecimal delta = r.y.subtract(y).abs();
        if (delta.compareTo(eps) <= 0) 
            return true;
        else 
            return false;
    }

    public double sphereLoo(Point p) {
        BigDecimal dx = x.subtract(p.x).abs(), dy = y.subtract(p.y).abs();
        double dx_ = dx.doubleValue() * Arithmetic.METERS_PER_LON;
        double dy_ = dy.doubleValue() * Arithmetic.METERS_PER_LON * Arithmetic.cos(x.min(p.x));
        return dx_ > dy_ ? dx_ : dy_;
    }

    public double sphereL2(Point p) {
        BigDecimal dx = x.subtract(p.x).abs(), dy = y.subtract(p.y).abs();
        double sx = Arithmetic.haversin(dx), sy = Arithmetic.haversin(dy);
        double cx1 = Arithmetic.cos(x), cx2 = Arithmetic.cos(p.x);
        return 2 * Arithmetic.EARTH_RADIUS * Math.asin(Math.sqrt(sx + cx1 * cx2 * sy)); // better
    }

    public double sphereL2_(Point p) {
        BigDecimal dy = y.subtract(p.y).abs();
        double sx1 = Arithmetic.sin(x), sx2 = Arithmetic.sin(p.x);
        double cy = Arithmetic.cos(dy), cx1 = Arithmetic.cos(x), cx2 = Arithmetic.cos(p.x);
        return Math.acos(cx1 * cx2 * cy + sx1 * sx2) * Arithmetic.EARTH_RADIUS; // double die here
    }

    public static Vector<BigDecimal> sphere2Euclidean(BigDecimal t, BigDecimal x, BigDecimal y, BigDecimal z) {
        double sx = Arithmetic.sin(x), cx = Arithmetic.cos(x);
        double sy = Arithmetic.sin(y), cy = Arithmetic.cos(y);
        Vector<BigDecimal> point = new Vector<BigDecimal>();
        point.add(t);
        point.add(new BigDecimal((Arithmetic.EARTH_RADIUS + z.doubleValue()) * cx * cy));
        point.add(new BigDecimal((Arithmetic.EARTH_RADIUS + z.doubleValue()) * cx * sy));
        point.add(new BigDecimal((Arithmetic.EARTH_RADIUS + z.doubleValue()) * sx));
        return point; 
    }
}
