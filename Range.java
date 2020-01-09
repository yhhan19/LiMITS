import java.math.BigDecimal;
import java.math.BigInteger;

public class Range {

    private BigDecimal x, y;
    
    public Range() {
        x = BigDecimal.ZERO;
        y = BigDecimal.ZERO;
    }

    public Range(int x, int y) {
        this.x = new BigDecimal(x);
        this.y = new BigDecimal(y);
    }

    public Range(BigInteger x, BigInteger y) {
        this.x = new BigDecimal(x);
        this.y = new BigDecimal(y);
    }

    public Range(String x, String y) {
        this.x = new BigDecimal(x);
        this.y = new BigDecimal(y);
    }

    public Range(BigDecimal x, BigDecimal y) {
        this.x = x;
        this.y = y;
    }

    public Range(Range that) {
        this.x = that.x;
        this.y = that.y;
    }

    public void display() {
        System.out.println("[" + Arithmetic.format(x) + " " + Arithmetic.format(y) + "]");
    }

    public BigDecimal getX() {
        return x;
    }

    public BigDecimal getY() {
        return y;
    }

    public void setX(BigDecimal x) {
        this.x = x;
    }

    public void setY(BigDecimal y) {
        this.y = y;
    }

    public BigDecimal range() {
        return x.subtract(y);
    }

    public BigDecimal mid() {
        return (x.add(y)).divide(new BigDecimal(2));
    }

    public BigDecimal interpolation(BigDecimal r) {
        return x.multiply(r).add(y.multiply(BigDecimal.ONE.subtract(r)));
    }

    public Range intersection(Range that) {
        BigDecimal x = this.x.min(that.x);
        BigDecimal y = this.y.max(that.y);
        int delta = Arithmetic.sgn(x.subtract(y));
        if (delta == 0)
            return new Range(x, x);
        if (delta > 0)
            return new Range(x, y);
        return null;
    }

    public boolean contains(BigDecimal z) {
        int dx = Arithmetic.sgn(x.subtract(z));
        int dy = Arithmetic.sgn(z.subtract(y));
        return dx >= 0 && dy >= 0;
    }
}
