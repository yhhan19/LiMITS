import java.util.Random;
import java.math.BigInteger;
import java.lang.String;
import java.util.Vector;
import java.math.MathContext;

public class Series {

    private Vector<Point> data;

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
        simplify();
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
        for (int i = 0; i < data.size(); i ++) {
            Point p = data.get(i);
            p.display(mc);
        }
        System.out.println();
    }

    public void display() {
        for (int i = 0; i < data.size(); i ++) {
            Point p = data.get(i);
            p.display();
        }
        System.out.println();
    }

    public int size() {
        return data.size();
    }

    public Point get(int i) {
        return data.get(i);
    }
}
