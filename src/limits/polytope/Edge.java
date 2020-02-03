package limits.polytope;

import java.math.BigDecimal;
import java.util.Vector;

import limits.geometry.*;

public class Edge {

    private Vertex from, to;
    private Edge twin, prev, next;
    private Facet left;

    public Edge(Vertex from, Vertex to) {
        this.from = from;
        this.to = to;
    }

    public void display() {
        System.out.print("< ");
        from.display();
        to.display();
        System.out.println(">");
    }

    public void setTwin(Edge e) {
        twin = e;
    }

    public void setNext(Edge e) {
        next = e;
    }

    public void setPrev(Edge e) {
        prev = e;
    }

    public void setFacet(Facet f) {
        left = f;
    }

    public Vertex getFrom() {
        return from;
    }

    public Vertex getTo() {
        return to;
    }

    public Edge getNext() {
        return next;
    }

    public Edge getPrev() {
        return prev;
    }

    public Edge getTwin() {
        return twin;
    }

    public Facet getFacet() {
        return left;
    }

    public static void twinEdge(Edge e0, Edge e1) {
        e0.setTwin(e1);
        e1.setTwin(e0);
    }

    public static void linkEdge(Edge e0, Edge e1) {
        e0.setNext(e1);
        e1.setPrev(e0);
    }

    public Edge getVertexNext() {
        return this.getPrev().getTwin();
    }

    public Edge getVertexPrev() {
        return this.getTwin().getNext();
    }

    public int getSide() {
        return from.getSide() & to.getSide(); 
    }

    public void disconnect() {
        Edge e1 = getTwin();
        Facet f0 = getFacet(), f1 = e1.getFacet();
        if (f0.getLoop() == this) 
            f0.setLoop(getNext());
        Vector<Edge> edges = f1.getEdges();
        for (int i = 0; i < edges.size(); i ++) 
            edges.get(i).setFacet(f0);
        e1.getPrev().setNext(getNext());
        getNext().setPrev(e1.getPrev());
        e1.getNext().setPrev(getPrev());
        getPrev().setNext(e1.getNext());
    }

    public Vertex splitAt(Vertex v) {
        if (from.getPoint().on(v.getPoint())) return from;
        if (to.getPoint().on(v.getPoint())) return to;
        Edge e1 = this;
        Edge e0 = twin;
        Edge e1_ = new Edge(v, e1.to);
        Edge e0_ = new Edge(v, e0.to);
        e1.to = v;
        e0.to = v;
        v.setHead(e1_);
        twinEdge(e1, e0_);
        twinEdge(e0, e1_);
        linkEdge(e1_, e1.next);
        linkEdge(e1, e1_);
        linkEdge(e0_, e0.next);
        linkEdge(e0, e0_);
        e1_.left = e1.left;
        e0_.left = e0.left;
        return null;
    }

    public Point interpolationX(BigDecimal x) {
        return Point.interpolationX(from.getPoint(), to.getPoint(), x); 
    }

    public Point interpolationY(BigDecimal y) {
        return Point.interpolationY(from.getPoint(), to.getPoint(), y); 
    }

    public Vect getVect() {
        return new Vect(getFrom().getPoint(), getTo().getPoint());
    }
}
