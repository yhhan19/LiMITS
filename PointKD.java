import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Vector;

public class PointKD {

    private Vector<BigDecimal> data;
    private int k;

    public PointKD(int k) {
        this.k = k;
        this.data = new Vector<BigDecimal>();
        for (int i = 0; i < k; i ++) 
            this.data.add(BigDecimal.ZERO);
    }

    public PointKD(Vector<BigDecimal> data) {
        this.k = data.size();
        this.data = new Vector<BigDecimal>();
        for (int i = 0; i < k; i ++) 
            this.data.add(data.get(i));
    }

    public void display() {
        System.out.print("[ ");
        for (int i = 0; i < k; i ++) 
            System.out.print(Arithmetic.format(data.get(i)) + " ");
        System.out.println("]");
    }

    public Point project(int i) {
        return new Point(this.data.get(0), this.data.get(i));
    }

    public PointKD projectKD() {
        Vector<BigDecimal> data = new Vector<BigDecimal>();
        for (int i = 1; i < k; i ++) 
            data.add(this.data.get(i));
        return new PointKD(data);
    }

    public BigDecimal get(int i) {
        return this.data.get(i);
    }

    public int dim() {
        return k;
    }

    public PointKD add(VectKD v) {
        Vector<BigDecimal> data = new Vector<BigDecimal>();
        for (int i = 0; i < k; i ++) 
            data.add(this.get(i).add(v.get(i)));
        return new PointKD(data);
    }

    public static PointKD interpolation(PointKD p, PointKD q, BigDecimal x, int i) {
        if (p.dim() != q.dim()) 
            return null;
        VectKD v = new VectKD(p, q);
        if (Arithmetic.sgn(v.get(i)) == 0) 
            return null;
        BigDecimal r = x.subtract(p.get(i)).divide(v.get(i), Arithmetic.MC);
        return p.add(v.scalar(r));
    }

    public BigDecimal distanceLoo(PointKD p) {
        VectKD v = new VectKD(this, p);
        return v.normoo();
    }

    public BigDecimal distanceL1(PointKD p) {
        VectKD v = new VectKD(this, p);
        return v.norm1();
    }
}