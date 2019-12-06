import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Vector;
import java.util.Iterator;
import javafx.util.Pair;
import java.lang.Integer;

public class Polytope {

    private Vector<Vertex> vertices;
    private Facet external;

    private void structuralize() {
        for (int i = 0; i < vertices.size(); i ++) {
            Vertex v = vertices.get(i);
            if (i == 0 || v.getPoint().getX().compareTo(vertices.get(i - 1).getPoint().getX()) == 1)
                v.setSide(1);
            else 
                v.setSide(2);
        }
        Facet internal = new Facet();
        Facet external = new Facet();
        Edge last0 = null, last1 = null;
        for (int i = 0; i < vertices.size(); i ++) {
            Vertex from = vertices.get(i), to;
            if (i == vertices.size() - 1)
                to = vertices.get(0);
            else 
                to = vertices.get(i + 1);
            Edge e0 = new Edge(from, to);
            Edge e1 = new Edge(to, from);
            e0.setFacet(internal);
            e1.setFacet(external);
            Edge.twinEdge(e0, e1);
            if (i == 0) {
                internal.setLoop(e0);
                external.setLoop(e1);
            }
            else {
                Edge.linkEdge(last0, e0);
                Edge.linkEdge(e1, last1);
            }
            from.setHead(e0);
            last0 = e0;
            last1 = e1;
        }
        Edge.linkEdge(last0, internal.getLoop());
        Edge.linkEdge(external.getLoop(), last1);
        for (int i = 0; i < vertices.size(); i ++) {
            Vertex from = vertices.get(i);
            Edge e = from.getHead();
            from.setOut(e);
            from.setIn(e.getPrev());
        }
        this.external = external;
    }

    private void points2Vertices(Vector<Point> points) {
        vertices = new Vector<Vertex>();
        for (int i = 0; i < points.size(); i ++) {
            Point p = points.get(i);
            Vertex v = new Vertex(i, p);
            vertices.add(v);
        }
    }

    public Polytope(Vector<Point> points) {
        points2Vertices(points);
        structuralize();
    }

    public Polytope(Series s, BigDecimal eps) {
        Vector<Point> points = new Vector<Point>();
        for (int i = 0; i < s.size(); i ++) {
            Point p = s.get(i);
            Point q = new Point(p);
            Vect v = new Vect(BigDecimal.ZERO, eps.negate());
            q.add(v);
            points.add(q);
        }
        for (int i = s.size() - 1; i >= 0; i --) {
            Point p = s.get(i);
            Point q = new Point(p);
            Vect v = new Vect(BigDecimal.ZERO, eps);
            q.add(v);
            points.add(q);
        }
        points2Vertices(points);
        structuralize();
    }

    public Polytope(int N, BigDecimal radius) {
        Vector<Point> points = new Vector<Point>();
        points.add(new Point("-1", "-1"));
        BigDecimal n = new BigDecimal(Integer.toString(N));
        BigDecimal delta = radius.divide(n, Arithmetic.MC);
        BigDecimal x = radius, y = BigDecimal.ZERO;
        points.add(new Point(x, y));
        for (int i = 0; i < N; i ++) {
            x = x.subtract(delta);
            BigDecimal y2 = radius.multiply(radius).subtract(x.multiply(x));
            y = Arithmetic.sqrt(y2, Arithmetic.MC);
            Point p = new Point(x, y);
            points.add(p);
        }
        points2Vertices(points);
        structuralize();
    }

    public void display(MathContext mc) {
        System.out.println(size());
        external.display();
        external.getLoop().getTwin().getFacet().display();
    }

    public int size() {
        return vertices.size();
    }

    public void addVertex(Vertex v) {
        vertices.add(v);
        v.setId(vertices.size() - 1);
    }

    public Vertex getVertex(int i) {
        if (i < 0) 
            return this.vertices.get(i + size());
        else 
            return this.vertices.get(i);
    }

    public boolean isBoundary(Edge e) {
        return e.getFacet() == external || e.getTwin().getFacet() == external;
    }

    public boolean isOutside(Edge e) {
        return e.getFacet() == external;
    }

    public Vector<Facet> getFacets() {
        Vector<Facet> queue = new Vector<Facet>();
        external.setVisited();
        queue.add(external);
        for (int i = 0; i < queue.size(); i ++) {
            Facet f = queue.get(i);
            Edge e = f.getLoop();
            do {
                Facet f0 = e.getTwin().getFacet();
                if (! f0.isVisited()) {
                    f0.setVisited();
                    queue.add(f0);
                }
                e = e.getNext();
            } while (e != f.getLoop());
        }
        for (int i = 0; i < queue.size(); i ++) 
            queue.get(i).cleanVisited();
        return queue;
    }

    public void checkTriangulation() {
        Vector<Facet> queue = getFacets();
        BigDecimal result = BigDecimal.ZERO, min = null, max = null;
        for (int i = 0; i < queue.size(); i ++) {
            Facet f = queue.get(i);
            BigDecimal area = f.area();
            if (f == external) 
                result = result.add(area);
            else {
                result = result.add(area);
                if (min == null || area.compareTo(min) == -1) 
                    min = area;
                if (max == null || area.compareTo(max) == 1) 
                    max = area;
            }
        }
        System.out.println("difference: " + result);
        System.out.println("min: " + min);
        System.out.println("max: " + max);
        System.out.println("triangles: " + (queue.size() - 1));
    }

    public void triangulate() {
        Vector<Facet> facets = getFacets();
        for (int i = 0; i < facets.size(); i ++) {
            Facet internal = facets.get(i);
            if (internal != external) {
                internal.triangulate();
            }
        }
    }

    public Vector<Edge> intersectEdges(Chord chord) {
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
                Facet f0 = e.getTwin().getFacet();
                if (f0 != external && ! f0.isVisited()) {
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
                e = e.getNext();
            } while (e != f.getLoop());
        }
        for (int i = 0; i < queue.size(); i ++) 
            queue.get(i).cleanVisited();
        for (int i = 0; i < margin.size(); i ++) 
            margin.get(i).cleanVisited();
        return result;
    }
    
    private void appendChord(Vector<Chord> windows, Chord chord) {
        if (chord.valid()) {
            windows.add(chord);
        }
    }

    private void biDepthFirstSearch(Edge e, Funnel fn0, Funnel fn1, Vector<Chord> windows) {
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
            if (! isOutside(BC)) {
                biDepthFirstSearch(BC, fn0_, fn1_, windows);
            }
            else {
                Vect v1 = new Vect(BC);
                Point p0 = v1.segmentIntersect(t0);
                Point p1 = v1.segmentIntersect(t1);
                Chord chord = null;
                if (p0 != null) 
                    appendChord(windows, new Chord(t01, new Vertex(-1, p0), null, BC));
                appendChord(windows, new Chord(new Vertex(-1, p1), t10, BC, null));
            }
        }
        if (A.leftConvex(0) && B.rightConvex(1)) {
            Edge AB = e.getPrev().getTwin();
            if (! isOutside(AB)) {
                biDepthFirstSearch(AB, fn0, fn1, windows);
            }
            else {
                Vect v0 = new Vect(AB);
                Point p0 = v0.segmentIntersect(t0);
                Point p1 = v0.segmentIntersect(t1);
                Chord chord = null;
                appendChord(windows, new Chord(t01, new Vertex(-1, p0), null, AB));
                if (p1 != null) 
                    appendChord(windows, new Chord(new Vertex(-1, p1), t10, AB, null));
            }
        }
    }

    public Vector<Chord> getWindows(Edge e) {
        Vector<Chord> windows = new Vector<Chord>();
        Funnel fn0 = new Funnel(e.getFrom(), null, e.getTo());
        Funnel fn1 = new Funnel(e.getTo(), e.getFrom(), null);
        e.getFrom().shortestLink(0, null);
        e.getTo().shortestLink(1, null);
        e.getFrom().shortestLink(1, e.getTo());
        e.getTo().shortestLink(0, e.getFrom());
        biDepthFirstSearch(e, fn0, fn1, windows);
        return windows;
    }
}
