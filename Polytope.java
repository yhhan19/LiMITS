import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Vector;
import java.util.Iterator;
import javafx.util.Pair;
import java.lang.Integer;

public class Polytope {

    private Vector<Vertex> vertices;
    private Facet external;
    private Edge start, end;

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

    private Polytope(int N, BigDecimal radius) {
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
}
