import java.math.BigInteger;
import java.math.BigDecimal;
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
        BigDecimal x = this.x.round(Arithmetic.DMC);
        BigDecimal y = this.y.round(Arithmetic.DMC);
        System.out.print("(" + x + " " + y + ") ");
    }

    public BigDecimal getX() {
        return x;
    }

    public BigDecimal getY() {
        return y;
    }

    public void add(Vect v) {
        x = x.add(v.getX());
        y = y.add(v.getY());
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
}
