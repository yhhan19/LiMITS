import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Vector;

public class Polytope {

    private Vector<Vertex> vertices;
    private Facet external;
    private Edge start, end;
    private Vect lastChord;

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
        this.external = external;
        this.start = getVertex(-1).getHead();
        this.end = getVertex(vertices.size() / 2 - 1).getHead();
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
            points.add(q.add(v));
        }
        for (int i = s.size() - 1; i >= 0; i --) {
            Point p = s.get(i);
            Point q = new Point(p);
            Vect v = new Vect(BigDecimal.ZERO, eps);
            points.add(q.add(v));
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

    public Edge getStart() {
        return start;
    }

    public Edge getEnd() {
        return end;
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
        System.out.print("difference: " + result);
        System.out.print(" min: " + min.add(BigDecimal.ZERO, Arithmetic.DMC));
        System.out.print(" max: " + max.add(BigDecimal.ZERO, Arithmetic.DMC));
        System.out.println(" facets: " + (queue.size() - 1));
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
                if (! isOutside(e.getTwin())) {
                    Facet f0 = e.getTwin().getFacet();
                    if (! f0.isVisited()) {
                        f0.setVisited();
                        Vect v1 = new Vect(e);
                        if (! v0.isSegmentIntersect(v1)) {
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

    private Vector<Chord> biDepthFirstSearch(Edge e, Funnel fn0, Funnel fn1) {
        if (e.getNext().getNext().getNext() != e) 
            throw new NullPointerException("triangle error");
        Vector<Chord> windows = new Vector<Chord>();
        Vertex A = e.getFrom(), B = e.getNext().getTo(), C = e.getTo();
        Vertex t00 = fn0.getApex(), t11 = fn1.getApex(), t10 = fn0.getRightHead(), t01 = fn1.getLeftHead();
        Vect t0 = new Vect(t11, t01), t1 = new Vect(t00, t10);
        Funnel fn0_ = new Funnel(), fn1_ = new Funnel();
        if (fn0.split(0, B, fn0_).equals("Left")) {
            Funnel temp = fn0; fn0 = fn0_; fn0_ = temp;
        }
        if (fn1.split(1, B, fn1_).equals("Left")) {
            Funnel temp = fn1; fn1 = fn1_; fn1_ = temp;
        }
        if (A.leftConvex(0) && B.rightConvex(1)) {
            Edge AB = e.getPrev().getTwin();
            if (! isOutside(AB)) {
                windows.addAll(biDepthFirstSearch(AB, fn0, fn1));
            }
            else {
                if (AB == getEnd().getTwin()) lastChord = t0;
                Vect v0 = new Vect(AB);
                Point p0 = v0.segmentLineIntersect(t0);
                Point p1 = v0.segmentLineIntersect(t1);
                Chord ch = new Chord(t01, new Vertex(-1, p0), null, AB);
                if (ch.intermediate()) windows.add(ch);
                if (p1 != null) {
                    ch = new Chord(new Vertex(-1, p1), t10, AB, null);
                    if (ch.intermediate()) windows.add(ch);
                }
            }
        }
        if (B.leftConvex(0) && C.rightConvex(1)) {
            Edge BC = e.getNext().getTwin();
            if (! isOutside(BC)) {
                windows.addAll(biDepthFirstSearch(BC, fn0_, fn1_));
            }
            else {
                if (BC == getEnd().getTwin()) lastChord = t1;
                Vect v1 = new Vect(BC);
                Point p0 = v1.segmentLineIntersect(t0);
                Point p1 = v1.segmentLineIntersect(t1);
                Chord ch = new Chord(new Vertex(-1, p1), t10, BC, null);
                if (ch.intermediate()) windows.add(ch);
                if (p0 != null) {
                    ch = new Chord(t01, new Vertex(-1, p0), null, BC);
                    if (ch.intermediate()) windows.add(ch);
                }
            }
        }
        return windows;
    }

    private Vector<Chord> getWindows(Edge e) {
        Funnel fn0 = new Funnel(e.getFrom(), null, e.getTo());
        Funnel fn1 = new Funnel(e.getTo(), e.getFrom(), null);
        e.getFrom().shortestLink(0, null);
        e.getTo().shortestLink(1, null);
        e.getFrom().shortestLink(1, e.getTo());
        e.getTo().shortestLink(0, e.getFrom());
        return biDepthFirstSearch(e, fn0, fn1);
    }

    private Vector<Chord> simDepthFirstSearch(Edge e, Funnel fn) {
        if (e.getNext().getNext().getNext() != e) 
            throw new NullPointerException("triangle error");
        Vector<Chord> windows = new Vector<Chord>();
        Vertex A = e.getFrom(), B = e.getNext().getTo(), C = e.getTo();
        Vertex t00 = fn.getApex(), t10 = fn.getRightHead(), t01 = fn.getLeftHead();
        Vect t0 = new Vect(t00, t01), t1 = new Vect(t00, t10);
        Funnel fn_ = new Funnel();
        if (fn.split(0, B, fn_).equals("Left")) {
            Funnel temp = fn; fn = fn_; fn_ = temp;
        }
        if (A.leftConvex(0) && B.rightConvex(0)) {
            Edge AB = e.getPrev().getTwin();
            if (! isOutside(AB)) {
                windows.addAll(simDepthFirstSearch(AB, fn));
            }
            else {
                if (AB == getEnd().getTwin()) lastChord = t0;
                Vect v0 = new Vect(AB);
                Point p0 = v0.segmentLineIntersect(t0);
                Point p1 = v0.segmentLineIntersect(t1);
                Chord ch = new Chord(t01, new Vertex(-1, p0), null, AB);
                if (ch.intermediate()) windows.add(ch);
                if (p1 != null) {
                    ch = new Chord(new Vertex(-1, p1), t10, AB, null);
                    if (ch.intermediate()) windows.add(ch);
                }
            }
        }
        if (B.leftConvex(0) && C.rightConvex(0)) {
            Edge BC = e.getNext().getTwin();
            if (! isOutside(BC)) {
                windows.addAll(simDepthFirstSearch(BC, fn_));
            }
            else {
                if (BC == getEnd().getTwin()) lastChord = t1;
                Vect v1 = new Vect(BC);
                Point p0 = v1.segmentLineIntersect(t0);
                Point p1 = v1.segmentLineIntersect(t1);
                Chord ch = new Chord(new Vertex(-1, p1), t10, BC, null);
                if (ch.intermediate()) windows.add(ch);
                if (p0 != null) {
                    ch = new Chord(t01, new Vertex(-1, p0), null, BC);
                    if (ch.intermediate()) windows.add(ch);
                }
            }
        }
        return windows;
    }

    private Vector<Chord> getWindows(Vertex v) {
        Edge e = v.getHead();
        Funnel fn = new Funnel(v, null, e.getTo());
        v.shortestLink(0, null);
        e.getTo().shortestLink(0, v);
        return simDepthFirstSearch(e, fn);
    }

    private Vector<Chord> getTrace(Vector<Chord> windows) {
        Vector<Chord> trace = new Vector<Chord>();
        Edge iterator = null;
        while (windows.size() > 0) {
            Chord chord = null;
            for (int j = 0; j < windows.size(); j ++) {
                Chord ch = windows.get(j);
                if (chord == null || ch.reach().compareTo(chord.reach()) == 1) 
                    chord = ch;
            }
            trace.add(chord);
            Vector<Edge> edges = intersectEdges(chord);
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
                    addVertex(v0);
                else 
                    v0 = v0_;
                iterator = v0.connect(v1, f);
                iterator.getFacet().triangulate();
            }
            else if (e0 == null && e1 != null) {
                Vertex v1_ = e1.splitAt(v1);
                if (v1_ == null) 
                    addVertex(v1);
                else 
                    v1 = v1_;
                iterator = v0.connect(v1, f);
                iterator.getFacet().triangulate();
            }
            else throw new NullPointerException("chord structure error");
            windows = getWindows(iterator);
        }
        return trace;
    }

    private Vector<Point> getPoints(Vector<Chord> trace) {
        if (lastChord == null)
            throw new NullPointerException("last chord error");
        Vector<Point> points = new Vector<Point>();
        Vect v0 = new Vect(getStart()), v1 = null;
        for (int i = 0; i < trace.size(); i ++) {
            v1 = new Vect(trace.get(i));
            points.add(v0.lineIntersect(v1));
            v0 = v1;
        }
        points.add(v0.lineIntersect(lastChord));
        points.add(lastChord.lineIntersect(new Vect(getEnd())));
        return points;
    }

    public Vector<Point> linkPath() {
        Edge start = getStart();
        lastChord = null;
        triangulate();
        return getPoints(getTrace(getWindows(start)));
    }

    public Vector<Point> linkPath(BigDecimal y) {
        Point r = Point.interpolationY(getStart().getFrom().getPoint(), getStart().getTo().getPoint(), y);
        Vertex start_ = new Vertex(-1, r), start = getStart().splitAt(start_);
        if (start == null) {
            addVertex(start_);
            start = start_;
        }
        start.setSide(1);
        start.setHead(start.match(getVertex(0)));
        lastChord = null;
        triangulate();
        return getPoints(getTrace(getWindows(start)));
    }
}
