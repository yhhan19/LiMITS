package limits.polytope;

import java.math.BigDecimal;
import java.util.Vector;

import limits.geometry.*;

public class Polytube extends Polytope {

    private Edge start, end;
    private Vect endChord, endChord_, endRange;
    private BigDecimal ratio;
    private boolean relative;

    public Polytube(Series s, BigDecimal eps) {
        super(s, eps);
        start = getVertex(-1).getHead();
        end = getVertex(size() / 2 - 1).getHead();
    }

    public Edge getStart() {
        return start;
    }

    public Edge getEnd() {
        return end;
    }
    
    private void addChord(Vector<Chord> windows, Chord ch) {
        if (ch.getFromVertex().getPoint() != null && ch.getToVertex().getPoint() != null) 
            if (ch.intermediate()) windows.add(ch);
    }

    private void endChords(String type, Point p0, Point p1, Vertex B, Vect t, Vect v, Funnel f, Vertex t0) {
        endRange = new Vect(p0 != null ? p0 : B.getPoint(), p1 != null ? p1 : B.getPoint());
        if (ratio == null) {
            endChord = t;
        }
        else {
            Vect er = relative ? endRange : v;
            BigDecimal y = er.interpolationYratio(ratio).getY();
            if (type.equals("LEFT")) {
                Vertex endVertex = new Vertex(-1, new Point(p0.getX(), y));
                endChord = Vertex.getVect(f.search(endVertex), endVertex);
                if (! endVertex.getPoint().on(endRange)) 
                    endChord_ = Vertex.getVect(t0, new Vertex(-1, p0));
            }
            else {
                Vertex endVertex = new Vertex(-1, new Point(p1.getX(), y));
                endChord = Vertex.getVect(f.search(endVertex), endVertex);
                if (! endVertex.getPoint().on(endRange)) 
                    endChord_ = Vertex.getVect(new Vertex(-1, p1), t0);
            }
        }
    }

    private Vector<Chord> biDepthFirstSearch(Edge e, Funnel fn0, Funnel fn1) {
        Vector<Chord> windows = new Vector<Chord>();
        Vertex A = e.getFrom(), B = e.getNext().getTo(), C = e.getTo();
        Vertex t00 = fn0.getApex(), t11 = fn1.getApex(), t10 = fn0.getRightHead(), t01 = fn1.getLeftHead();
        Vect t0 = Vertex.getVect(t11, t01), t1 = Vertex.getVect(t00, t10);
        Funnel fn0_ = new Funnel(), fn1_ = new Funnel();
        if (fn0.split(0, B, fn0_).equals("LEFT")) {
            Funnel temp = fn0; fn0 = fn0_; fn0_ = temp;
        }
        if (fn1.split(1, B, fn1_).equals("LEFT")) {
            Funnel temp = fn1; fn1 = fn1_; fn1_ = temp;
        }
        if (A.leftConvex(0) && B.rightConvex(1)) {
            Edge AB = e.getPrev().getTwin();
            if (! isOutside(AB)) {
                windows.addAll(biDepthFirstSearch(AB, fn0, fn1));
            }
            else {
                Vect v0 = AB.getVect();
                Point p0 = v0.segmentLineIntersect(t0), p1 = v0.segmentLineIntersect(t1);
                addChord(windows, new Chord(t01, new Vertex(-1, p0), null, AB));
                addChord(windows, new Chord(new Vertex(-1, p1), t10, AB, null));
                if (AB == getEnd().getTwin()) 
                    endChords("LEFT", p0, p1, B, t0, v0, fn0, t01);
            }
        }
        if (B.leftConvex(0) && C.rightConvex(1)) {
            Edge BC = e.getNext().getTwin();
            if (! isOutside(BC)) {
                windows.addAll(biDepthFirstSearch(BC, fn0_, fn1_));
            }
            else {
                Vect v1 = BC.getVect();
                Point p0 = v1.segmentLineIntersect(t0), p1 = v1.segmentLineIntersect(t1);
                addChord(windows, new Chord(new Vertex(-1, p1), t10, BC, null));
                addChord(windows, new Chord(t01, new Vertex(-1, p0), null, BC));
                if (BC == getEnd().getTwin()) 
                    endChords("RIGHT", p0, p1, B, t1, v1, fn0_, t10);
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
        Vector<Chord> windows = new Vector<Chord>();
        Vertex A = e.getFrom(), B = e.getNext().getTo(), C = e.getTo();
        Vertex t00 = fn.getApex(), t10 = fn.getRightHead(), t01 = fn.getLeftHead();
        Vect t0 = Vertex.getVect(t00, t01), t1 = Vertex.getVect(t00, t10);
        Funnel fn_ = new Funnel();
        if (fn.split(0, B, fn_).equals("LEFT")) {
            Funnel temp = fn; fn = fn_; fn_ = temp;
        }
        if (A.leftConvex(0) && B.rightConvex(0)) {
            Edge AB = e.getPrev().getTwin();
            if (! isOutside(AB)) {
                windows.addAll(simDepthFirstSearch(AB, fn));
            }
            else {
                Vect v0 = AB.getVect();
                Point p0 = v0.segmentLineIntersect(t0), p1 = v0.segmentLineIntersect(t1);
                addChord(windows, new Chord(t01, new Vertex(-1, p0), null, AB));
                addChord(windows, new Chord(new Vertex(-1, p1), t10, AB, null));
                if (AB == getEnd().getTwin()) 
                    endChords("LEFT", p0, p1, B, t0, v0, fn, t01);
            }
        }
        if (B.leftConvex(0) && C.rightConvex(0)) {
            Edge BC = e.getNext().getTwin();
            if (! isOutside(BC)) {
                windows.addAll(simDepthFirstSearch(BC, fn_));
            }
            else {
                Vect v1 = BC.getVect();
                Point p0 = v1.segmentLineIntersect(t0), p1 = v1.segmentLineIntersect(t1);
                addChord(windows, new Chord(new Vertex(-1, p1), t10, BC, null));
                addChord(windows, new Chord(t01, new Vertex(-1, p0), null, BC));
                if (BC == getEnd().getTwin()) 
                    endChords("RIGHT", p0, p1, B, t1, v1, fn_, t10);
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
                if (chord == null || windows.get(j).reach().compareTo(chord.reach()) == 1) 
                    chord = windows.get(j);
            }
            trace.add(chord);
            Vector<Edge> edges = intersectEdges(chord);
            for (int j = 0; j < edges.size(); j ++) 
                edges.get(j).disconnect();
            Vertex v0 = chord.getFromVertex(), v1 = chord.getToVertex();
            if (chord.getFromEdge() != null) {
                Vertex v0_ = chord.getFromEdge().splitAt(v0);
                v0 = v0_ != null ? v0_ : addVertex(v0);
            }
            else {
                Vertex v1_ = chord.getToEdge().splitAt(v1);
                v1 = v1_ != null ? v1_ : addVertex(v1);
            }
            iterator = v0.connect(v1, chord.getFirstFacet());
            iterator.getFacet().triangulate();
            windows = getWindows(iterator);
        }
        return trace;
    }

    private Vector<Point> getPoints(Vector<Chord> trace) {
        Vector<Point> points = new Vector<Point>();
        Vect v0 = getStart().getVect(), v1 = null;
        for (int i = 0; i < trace.size(); i ++) {
            v1 = trace.get(i).getVect();
            points.add(v0.lineIntersect(v1));
            v0 = v1;
        }
        if (endChord_ == null) {
            Point p = v0.lineIntersect(endChord);
            if (p != null) points.add(p);
            points.add(endChord.lineIntersect(getEnd().getVect()));
        }
        else {
            Point p = v0.lineIntersect(endChord_);
            if (p != null) points.add(p);
            p = endChord_.lineIntersect(endChord);
            if (p != null) points.add(p);
            points.add(endChord.lineIntersect(getEnd().getVect()));
        }
        return points;
    }

    public Vector<Point> linkPath() {
        ratio = null;
        Edge start = getStart();
        triangulate();
        return getPoints(getTrace(getWindows(start)));
    }

    public Vector<Point> linkPath(Range y, BigDecimal rs, BigDecimal re, boolean relative) {
        ratio = re;
        this.relative = relative;
        BigDecimal y0 = y.interpolation(rs);
        Vertex v = new Vertex(-1, getStart().interpolationY(y0));
        Vertex start = getStart().splitAt(v);
        if (start == null) {
            start = addVertex(v);
            start.setSide(3);
        }
        triangulate();
        endChord = endChord_ = null;
        Vector<Point> points = getPoints(getTrace(getWindows(start)));
        y.setX(endRange.getFrom().getY());
        y.setY(endRange.getTo().getY());
        return points;
    }
}
