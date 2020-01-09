import java.math.BigDecimal;
import java.math.MathContext;

public class Vect {

    private Point from, to;

    public Vect(Point from, Point to) {
        this.from = new Point(from);
        this.to = new Point(to);
    }

    public Vect(Edge e) {
        this.from = e.getFrom().getPoint();
        this.to = e.getTo().getPoint();
    }

    public Vect(Chord ch) {
        this.from = ch.getFromVertex().getPoint();
        this.to = ch.getToVertex().getPoint();
    }

    public Vect(Vertex v0, Vertex v1) {
        this.from = v0.getPoint();
        this.to = v1.getPoint();
    }

    public Vect(Point to) {
        this.from = Point.ORIGIN;
        this.to = new Point(to);
    }

    public Vect(BigDecimal x, BigDecimal y) {
        this.from = Point.ORIGIN;
        this.to = new Point(x, y);
    }

    public Vect(String x, String y) {
        this.from = Point.ORIGIN;
        this.to = new Point(x, y);
    }

    public BigDecimal mid() {
        return from.getY().add(to.getY()).divide(new BigDecimal(2));
    }

    public void display() {
        from.display();
        to.display();
        System.out.println();
    }

    public BigDecimal getX() {
        BigDecimal x = to.getX().subtract(from.getX());
        return x;
    }

    public BigDecimal getY() {
        BigDecimal y = to.getY().subtract(from.getY());
        return y;
    }

    public Point getFrom() {
        return from;
    }

    public Point getTo() {
        return to;
    }

    public Vect add(Vect v) {
        return new Vect(from, to.add(v));
    }

    public Vect scalar(BigDecimal r) {
        BigDecimal x = getX().multiply(r);
        BigDecimal y = getY().multiply(r);
        Point to_ = from.add(new Vect(x, y));
        return new Vect(from, to_);
    }

    public BigDecimal dot(Vect v) {
        BigDecimal a = this.getX().multiply(v.getX());
        BigDecimal b = this.getY().multiply(v.getY());
        BigDecimal c = a.add(b);
        return c;
    }

    public BigDecimal modulus(MathContext mc) {
        BigDecimal d = this.dot(this);
        d = Arithmetic.sqrt(d, mc);
        return d;
    }

    public BigDecimal cross(Vect v) {
        BigDecimal a = this.getX().multiply(v.getY());
        BigDecimal b = this.getY().multiply(v.getX());
        BigDecimal c = a.subtract(b);
        return c;
    }

    public int divide(Point p0, Point p1) {
        Vect v0 = new Vect(from, p0);
        Vect v1 = new Vect(from, p1);
        BigDecimal c0 = cross(v0);
        BigDecimal c1 = cross(v1);
        int d0 = Arithmetic.sgn(c0);
        int d1 = Arithmetic.sgn(c1);
        if (d0 == 0 && d1 == 0) return 0;
        if (d0 == 0 || d1 == 0) return 1;
        return - d0 * d1; 
    }

    public boolean isSegmentIntersect(Vect v) {
        int d0 = divide(v.from, v.to);
        int d1 = v.divide(from, to);
        if (d0 != 0) {
            return d0 == 1 && d1 == 1;
        }
        else {
            if (d1 != 0) {
                this.display();
                v.display();
                throw new NullPointerException("intersect error");
            }
            else 
                return v.from.on(this) || v.to.on(this);
        }
    }

    public Point lineIntersect(Vect v) {
        BigDecimal cross = this.cross(v);
        if (Arithmetic.sgn(cross) == 0) return null;
        Vect v0 = new Vect(from), v1 = new Vect(to);
        Vect v2 = new Vect(v.from), v3 = new Vect(v.to);
        BigDecimal c10 = v1.cross(v0);
        BigDecimal c32 = v3.cross(v2);
        BigDecimal x = (c10.multiply(v.getX()).subtract(c32.multiply(this.getX())));
        x = x.divide(cross, Arithmetic.MC);
        BigDecimal y = (c10.multiply(v.getY()).subtract(c32.multiply(this.getY())));
        y = y.divide(cross, Arithmetic.MC);
        Point p = new Point(x, y);
        return p;
    }

    public Point segmentLineIntersect(Vect v) {
        Point p = lineIntersect(v);
        if (p == null) {
            BigDecimal cross = cross(new Vect(from, v.to));
            if (Arithmetic.sgn(cross) == 0) {
                if (to.distL22(v.from).compareTo(to.distL22(v.to)) == -1)
                    return v.from;
                else 
                    return v.to;
            }
            return null;
        }
        if (! p.on(this)) {
            return null;
        }
        if (! p.outside(v.from, v.to)) {
            return null;
        }
        return p;
    }

     public boolean between(Vect v0, Vect v1) {
        return Arithmetic.sgn(cross(v0)) <= 0 && Arithmetic.sgn(cross(v1)) >= 0;
    }

    public Point interpolationX(BigDecimal x) {
        return Point.interpolationX(from, to, x);
    }

    public Point interpolationY(BigDecimal y) {
        return Point.interpolationY(from, to, y);
    }

    public Point interpolationXratio(BigDecimal r) {
        BigDecimal x = from.getX().multiply(r).add(to.getX().multiply(BigDecimal.ONE.subtract(r)));
        return Point.interpolationX(from, to, x);
    }

    public Point interpolationYratio(BigDecimal r) {
        BigDecimal y = from.getY().multiply(r).add(to.getY().multiply(BigDecimal.ONE.subtract(r)));
        return Point.interpolationY(from, to, y);
    }

    public Vect bisector(Vect that) {
        Vect v0 = this.scalar(that.modulus(Arithmetic.MC));
        Vect v1 = that.scalar(this.modulus(Arithmetic.MC));
        return v0.add(v1);
    }
}
