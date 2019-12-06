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

    public boolean isIntersect(Vect v) {
        int d0 = divide(v.from, v.to);
        int d1 = v.divide(from, to);
        if (d0 != 0) {
            return d0 == 1 && d1 == 1;
        }
        else {
            if (d1 != 0) 
                throw new NullPointerException("intersect error");
            else 
                return v.from.on(this) || v.to.on(this);
        }
    }

    public Point lineIntersect(Vect v) {
        BigDecimal cross = this.cross(v);
        if (Arithmetic.sgn(cross) == 0) return null;
        Vect v0 = new Vect(from), v1 = new Vect(to);
        Vect v2 = new Vect(v.from), v3 = new Vect(v.to);
        BigDecimal cross10 = v1.cross(v0);
        BigDecimal cross32 = v3.cross(v2);
        BigDecimal x = (cross10.multiply(v.getX()).subtract(cross32.multiply(this.getX())));
        x = x.divide(cross, Arithmetic.MC);
        BigDecimal y = (cross10.multiply(v.getY()).subtract(cross32.multiply(this.getY())));
        y = y.divide(cross, Arithmetic.MC);
        Point p = new Point(x, y);
        return p;
    }

    public Point segmentIntersect(Vect v) {
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
}