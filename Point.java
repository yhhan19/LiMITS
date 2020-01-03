import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public class Point {

    private BigDecimal x, y;
    static Point ORIGIN = new Point("0", "0");

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

    public Point toL2(Point p, BigDecimal r) {
        BigDecimal dx = x.subtract(p.x).divide(r, Arithmetic.MC), dy = y.subtract(p.y).divide(r, Arithmetic.MC);
        BigDecimal sx = Arithmetic.sqrt(BigDecimal.ONE.subtract(dy.multiply(dy).divide(Arithmetic.TWO, Arithmetic.MC)), Arithmetic.MC);
        BigDecimal sy = Arithmetic.sqrt(BigDecimal.ONE.subtract(dx.multiply(dx).divide(Arithmetic.TWO, Arithmetic.MC)), Arithmetic.MC);
        return new Point(dx.multiply(sx).multiply(r).add(p.x), dy.multiply(sy).multiply(r).add(p.y));
    }

    public double sphereLoo(Point p) {
        BigDecimal dx = x.subtract(p.x).abs(), dy = y.subtract(p.y).abs();
        BigDecimal min = x.min(p.x);
        double cos = Math.cos(Math.toRadians(min.doubleValue()));
        double dx_ = dx.doubleValue() * (cos * 111320);
        double dy_ = dy.doubleValue() * 111320;
        return dx_ > dy_ ? dx_ : dy_;
    }

    public double sphereL2(Point p) {
        BigDecimal dy = y.subtract(p.y).abs();
        double sx1 = Math.sin(Math.toRadians(x.doubleValue()));
        double sx2 = Math.sin(Math.toRadians(p.x.doubleValue()));
        double cy = Math.cos(Math.toRadians(dy.doubleValue()));
        double cx1 = Math.cos(Math.toRadians(x.doubleValue()));
        double cx2 = Math.cos(Math.toRadians(p.x.doubleValue()));
        return Math.acos(cx1 * cx2 * cy + sx1 * sx2) * (111320 * 360) / (Math.PI * 2);
    }
}
