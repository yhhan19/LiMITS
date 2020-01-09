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

    public PointKD(double[] data) {
        this.k = data.length;
        this.data = new Vector<BigDecimal>();
        for (int i = 0; i < data.length; i ++) 
            this.data.add(new BigDecimal(data[i]));
    }

    public PointKD(int[] data) {
        this.k = data.length;
        this.data = new Vector<BigDecimal>();
        for (int i = 0; i < data.length; i ++) 
            this.data.add(new BigDecimal(data[i]));
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

    public PointKD shake() {
        Vector<BigDecimal> data = new Vector<BigDecimal>();
        data.add(this.data.get(0));
        for (int i = 1; i < k; i ++) 
            data.add(this.data.get(i).add(Arithmetic.epsRand()));
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

    public double sphereLoo(PointKD p) {
        Point p_ = new Point(p.get(1), p.get(2));
        Point this_ = new Point(get(1), get(2));
        return this_.sphereLoo(p_);
    }

    public double sphereL2(PointKD p) {
        Point p_ = new Point(p.get(1), p.get(2));
        Point this_ = new Point(get(1), get(2));
        return this_.sphereL2(p_);
    }
}
