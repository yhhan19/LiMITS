import java.math.BigDecimal;
import java.util.Vector;
import java.time.Clock;
import javafx.util.Pair;

public class Main {

    private Clock clock;
    private long startTime;

    public Main() {
        clock = Clock.systemDefaultZone();
        startTime = clock.millis();
    }

    private void printTime() {
        long curTime = clock.millis();
        System.out.println("time: " + (curTime - startTime) + "ms");
        startTime = curTime;
    }

    public void test_0() {
        Polytope p = new Polytope(new Series(100000, 50), new BigDecimal("100"));
        System.out.println("vertices: " + p.size());
        printTime();
        p.triangulate();
        p.checkTriangulation();
        printTime();
        Edge start = p.getVertex(-1).getHead();
        Vector<Chord> windows = p.getWindows(start);
        int res = 0;
        while (windows.size() > 0) {
            res ++;
            Chord chord = null;
            for (int j = 0; j < windows.size(); j ++) {
                Chord ch = windows.get(j);
                if (chord == null || ch.reach().compareTo(chord.reach()) == 1) 
                    chord = ch;
            }
            Vector<Edge> edges = p.intersectEdges(chord);
            for (int j = 0; j < edges.size(); j ++) {
                Edge e = edges.get(j);
                e.disconnect();
            }
            Facet f = chord.getFirstFacet();
            Edge e0 = chord.getFromEdge(), e1= chord.getToEdge();
            Vertex v0 = chord.getFromVertex(), v1 = chord.getToVertex();
            if (e0 != null && e1 == null) {
                Vertex v0_ = e0.splitAt(v0);
                if (v0_ == null)
                    p.addVertex(v0);
                else 
                    v0 = v0_;
                start = v0.connect(v1, f);
                start.getFacet().triangulate();
            }
            else if (e0 == null && e1 != null) {
                Vertex v1_ = e1.splitAt(v1);
                if (v1_ == null) 
                    p.addVertex(v1);
                else 
                    v1 = v1_;
                start = v0.connect(v1, f);
                start.getFacet().triangulate();
            }
            else throw new NullPointerException("chord error");
            windows = p.getWindows(start);
        }
        System.out.println(res);
        printTime();
        p.checkTriangulation();
    }

    public static void main(String args[]) {
        Main runtime = new Main();
        runtime.test_0();
    }
}