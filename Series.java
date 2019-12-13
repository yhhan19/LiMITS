import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Random;
import java.util.Vector;

public class Series {

    private Vector<Point> data;

    public Series(int N) {
        this.data = new Vector<Point>();
        for (int i = 0; i < N; i ++) 
            this.data.add(new Point(String.valueOf(i), "0"));
    }

    public Series(Vector<Point> data) {
        this.data = new Vector<Point>();
        for (int i = 0; i < data.size(); i ++) 
            this.data.add(data.get(i));
        //simplify();
    }

    public Series(int N, int bound) {
        data = new Vector<Point>();
        Random rand = new Random();
        BigInteger x = new BigInteger("0");
        BigInteger y = new BigInteger("0");
        for (int i = 0; i < N; i ++) {
            Point p = new Point(x, y);
            data.add(p);
            String str = String.valueOf(rand.nextInt(bound));
            BigInteger delta = new BigInteger(str);
            x = x.add(BigInteger.ONE);
            if (rand.nextInt(2) == 0)
                y = y.add(delta);
            else 
                y = y.subtract(delta);
        }
        System.out.println("input: " + data.size());
        //simplify();
    }

    public void simplify() {
        Vector<Point> result = new Vector<Point>();
        result.add(data.get(0));
        result.add(data.get(1));
        for (int i = 2; i < data.size(); i ++) {
            Point p0 = result.get(result.size() - 1);
            Point p1 = result.get(result.size() - 2);
            Point p2 = data.get(i);
            if (! p0.on(p1, p2)) 
                result.add(p2);
            else 
                result.set(result.size() - 1, p2);
        }
        System.out.println("simplified: " + result.size());
        data = result;
    }

    public void display(MathContext mc) {
        for (int i = 0; i < data.size(); i ++) 
            data.get(i).display(mc);
        System.out.println();
    }

    public void display() {
        for (int i = 0; i < data.size(); i ++) 
            data.get(i).display();
        System.out.println();
    }

    public int size() {
        return data.size();
    }

    public Point get(int i) {
        return data.get(i);
    }

    public Point lastElement() {
        return data.lastElement();
    }

    public BigDecimal distance(Series that) {
        BigDecimal error = BigDecimal.ZERO;
        for (int i = 0, j = 0; i < this.size() && j < that.size(); ) {
            if (this.get(i).getX().compareTo(that.get(j).getX()) == -1) {
                if (j != 0) {
                    Point p = Point.interpolationX(that.get(j - 1), that.get(j), this.get(i).getX());
                    BigDecimal delta = p.getY().subtract(this.get(i).getY()).abs();
                    if (delta.compareTo(error) == 1) 
                        error = delta;
                }
                i ++;
            }
            else {
                if (i != 0) {
                    Point p = Point.interpolationX(this.get(i - 1), this.get(i), that.get(j).getX());
                    BigDecimal delta = p.getY().subtract(that.get(j).getY()).abs();
                    if (delta.compareTo(error) == 1) 
                        error = delta;
                }
                j ++;
            }
        }
        return error;
    }
}
