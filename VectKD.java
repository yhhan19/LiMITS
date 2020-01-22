import java.math.BigDecimal;
import java.util.Vector;

public class VectKD {
    
    private PointKD from, to;

    public VectKD(PointKD from, PointKD to) {
        this.from = from;
        this.to = to;
    }

    public VectKD(PointKD to) {
        this.from = new PointKD(to.dim());
        this.to = to;
    }

    public void display() {
        from.display();
        to.display();
    }

    public int dim() {
        return from.dim();
    }

    public BigDecimal get(int i) {
        BigDecimal x = to.get(i).subtract(from.get(i));
        return x;
    }

    public PointKD getFrom() {
        return from;
    }

    public PointKD getTo() {
        return to;
    }

    public VectKD scalar(BigDecimal r) {
        Vector<BigDecimal> data = new Vector<BigDecimal>();
        for (int i = 0; i < from.dim(); i ++) {
            BigDecimal x = get(i).multiply(r);
            data.add(x);
        }
        PointKD to_ = from.add(new VectKD(new PointKD(data)));
        return new VectKD(from, to_);
    }

    public BigDecimal normoo() {
        BigDecimal d = null;
        for (int i = 0; i < from.dim(); i ++) {
            BigDecimal dx = this.get(i).abs();
            if (d == null || d.compareTo(dx) == -1) 
                d = dx;
        }
        return d;
    }

    public BigDecimal normnoo() {
        BigDecimal d = null;
        for (int i = 0; i < from.dim(); i ++) {
            BigDecimal dx = this.get(i).abs();
            if (d == null || d.compareTo(dx) == 1) 
                d = dx;
        }
        return d;
    }

    public BigDecimal norm1() {
        BigDecimal d = BigDecimal.ZERO;
        for (int i = 0; i < from.dim(); i ++) 
            d = d.add(this.get(i).abs());
        return d;
    }
}
