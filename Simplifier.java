import java.math.BigDecimal;
import java.util.Vector;
import java.time.Clock;
import javafx.util.Pair;

public class Simplifier {

    private Clock clock;
    private long startTime;
    private Vect lastChord;
    
    public Simplifier() {
        clock = Clock.systemDefaultZone();
        startTime = clock.millis();
    }

    private void printTime() {
        long curTime = clock.millis();
        System.out.println("time: " + (curTime - startTime) + "ms");
        startTime = curTime;
    }

    private Vector<Edge> intersectEdges(Polytope p, Chord chord) {
        Vector<Edge> result = new Vector<Edge>();
        Vector<Facet> queue = new Vector<Facet>();
        Vector<Facet> margin = new Vector<Facet>();
        Vect v0 = new Vect(chord.getFromVertex(), chord.getToVertex());
        Facet start = chord.getFirstFacet();
        start.setVisited();
        queue.add(start);
        for (int i = 0; i < queue.size(); i ++) {
            Facet f = queue.get(i);
            Edge e = f.getLoop();
            do {
                if (! p.isOutside(e.getTwin())) {
                    Facet f0 = e.getTwin().getFacet();
                    if (! f0.isVisited()) {
                        f0.setVisited();
                        Vect v1 = new Vect(e);
                        if (! v0.isIntersect(v1)) {
                            margin.add(f0);
                        }
                        else {
                            result.add(e);
                            queue.add(f0);
                        }
                    }
                }
                e = e.getNext();
            } while (e != f.getLoop());
        }
        for (int i = 0; i < queue.size(); i ++) 
            queue.get(i).cleanVisited();
        for (int i = 0; i < margin.size(); i ++) 
            margin.get(i).cleanVisited();
        return result;
    }

    private void addChord(Vector<Chord> windows, Chord chord) {
        if (chord.intermediate()) {
            windows.add(chord);
        }
    }

    private void biDepthFirstSearch(Polytope p, Edge e, Funnel fn0, Funnel fn1, Vector<Chord> windows) {
        Vertex A = e.getFrom(), B = e.getNext().getTo(), C = e.getTo();
        if (e.getNext().getNext().getNext() != e) 
            throw new NullPointerException("triangle error");
        Vertex t00 = fn0.getApex(), t11 = fn1.getApex();
        Vertex t10 = fn0.getRightHead(), t01 = fn1.getLeftHead();
        Vect t0 = new Vect(t11, t01);
        Vect t1 = new Vect(t00, t10);
        Funnel fn0_ = new Funnel(), fn1_ = new Funnel();
        if (fn0.split(0, B, fn0_)) {
            Funnel temp = fn0; fn0 = fn0_; fn0_ = temp;
        }
        if (fn1.split(1, B, fn1_)) {
            Funnel temp = fn1; fn1 = fn1_; fn1_ = temp;
        }
        if (B.leftConvex(0) && C.rightConvex(1)) {
            Edge BC = e.getNext().getTwin();
            if (! p.isOutside(BC)) {
                biDepthFirstSearch(p, BC, fn0_, fn1_, windows);
            }
            else {
                if (BC == p.getEnd().getTwin()) lastChord = t1;
                Vect v1 = new Vect(BC);
                Point p0 = v1.segmentIntersect(t0);
                Point p1 = v1.segmentIntersect(t1);
                Chord chord = null;
                addChord(windows, new Chord(new Vertex(-1, p1), t10, BC, null));
                if (p0 != null) 
                    addChord(windows, new Chord(t01, new Vertex(-1, p0), null, BC));
            }
        }
        if (A.leftConvex(0) && B.rightConvex(1)) {
            Edge AB = e.getPrev().getTwin();
            if (! p.isOutside(AB)) {
                biDepthFirstSearch(p, AB, fn0, fn1, windows);
            }
            else {
                if (AB == p.getEnd().getTwin()) lastChord = t0;
                Vect v0 = new Vect(AB);
                Point p0 = v0.segmentIntersect(t0);
                Point p1 = v0.segmentIntersect(t1);
                Chord chord = null;
                addChord(windows, new Chord(t01, new Vertex(-1, p0), null, AB));
                if (p1 != null) 
                    addChord(windows, new Chord(new Vertex(-1, p1), t10, AB, null));
            }
        }
    }

    private Vector<Chord> getWindows(Polytope p, Edge e) {
        Vector<Chord> windows = new Vector<Chord>();
        Funnel fn0 = new Funnel(e.getFrom(), null, e.getTo());
        Funnel fn1 = new Funnel(e.getTo(), e.getFrom(), null);
        e.getFrom().shortestLink(0, null);
        e.getTo().shortestLink(1, null);
        e.getFrom().shortestLink(1, e.getTo());
        e.getTo().shortestLink(0, e.getFrom());
        biDepthFirstSearch(p, e, fn0, fn1, windows);
        return windows;
    }

    private Vector<Chord> getTrace(Polytope p) {
        Vector<Chord> trace = new Vector<Chord>();
        p.triangulate();
        p.checkTriangulation();
        Edge start = p.getStart();
        Vector<Chord> windows = getWindows(p, start);
        while (windows.size() > 0) {
            Chord chord = null;
            for (int j = 0; j < windows.size(); j ++) {
                Chord ch = windows.get(j);
                if (chord == null || ch.reach().compareTo(chord.reach()) == 1) 
                    chord = ch;
            }
            trace.add(chord);
            Vector<Edge> edges = intersectEdges(p, chord);
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
            windows = getWindows(p, start);
        }
        p.checkTriangulation();
        return trace;
    }

    private Vector<Point> minLinkPath(Polytope p) {
        Vect vstart = new Vect(p.getStart());
        Vect vend = new Vect(p.getEnd());
        Vector<Chord> trace = getTrace(p);
        Vector<Point> points = new Vector<Point>();
        for (int i = 0; i < trace.size(); i ++) {
            Vect v0 = null, v1 = new Vect(trace.get(i));
            if (i == 0) 
                v0 = vstart;
            else 
                v0 = new Vect(trace.get(i - 1));
            points.add(v0.lineIntersect(v1));
            if (i == trace.size() - 1) {
                points.add(v1.lineIntersect(lastChord));
                points.add(lastChord.lineIntersect(vend));
            }
        }
        return points;
    }

    public Series simplify(Series s, BigDecimal eps) {
        Polytope p = new Polytope(s, eps);
        Vector<Point> points = minLinkPath(p);
        Series t = new Series(points);
        System.out.println("output: " + t.size());
        return t;
    }

    public static BigDecimal getError(Series s, Series t, BigDecimal eps) {
        BigDecimal error = t.distance(s, eps);
        System.out.print("head: ");
        t.get(0).subtract(s.get(0)).getTo().display();
        System.out.print("tail: ");
        t.get(t.size() - 1).subtract(s.get(s.size() - 1)).getTo().display();
        System.out.println("error: " + error.add(BigDecimal.ZERO, Arithmetic.DMC));
        return error;
    }

    public static void main(String args[]) {
        Simplifier runtime = new Simplifier();
        Series s = new Series(100000, 2000);
        BigDecimal eps = new BigDecimal("50");
        Series t = runtime.simplify(s, eps);
        runtime.printTime();
        BigDecimal error = Simplifier.getError(s, t, eps);
    }
}
