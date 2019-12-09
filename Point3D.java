import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public class Point3D {

    BigDecimal x, y, z;
    static Point3D ORIGIN = new Point3D("0", "0", "0");

    public Point3D(BigInteger x, BigInteger y, BigInteger z) {
        this.x = new BigDecimal(x);
        this.y = new BigDecimal(y);
        this.z = new BigDecimal(z);
    }

    public Point3D(BigDecimal x, BigDecimal y, BigDecimal z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Point3D(String x, String y, String z) {
        this.x = new BigDecimal(x);
        this.y = new BigDecimal(y);
        this.z = new BigDecimal(z);
    }

    public void display(MathContext mc) {
        BigDecimal x = this.x.round(mc);
        BigDecimal y = this.y.round(mc);
        BigDecimal z = this.z.round(mc);
        System.out.print("(" + x + " " + y + " " + z + ") ");
    }

    public void display() {
        String sx = Arithmetic.format(x);
        String sy = Arithmetic.format(y);
        String sz = Arithmetic.format(z);
        System.out.print("(" + sx + " " + sy + " " + sz + ") ");
    }

    public Point projectZ() {
        return new Point(x, y);
    }

    public Point projectY() {
        return new Point(x, z);
    }

    public BigDecimal getX() {
        return x;
    }

    public BigDecimal getY() {
        return y;
    }

    public BigDecimal getZ() {
        return z;
    }
}
