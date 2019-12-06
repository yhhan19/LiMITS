import java.util.Vector;

public class Edge {

    private Vertex from, to;
    private Edge twin, prev, next;
    private Facet left;

    public Edge(Vertex from, Vertex to) {
        this.from = from;
        this.to = to;
    }

    public void display() {
        System.out.print("<" + from.getId() + " " + to.getId() + "> ");
    }

    public void setFrom(Vertex v) {
        from = v;
    }
    
    public void setTo(Vertex v) {
        to = v;
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

    static void twinEdge(Edge e0, Edge e1) {
        e0.setTwin(e1);
        e1.setTwin(e0);
    }

    static void linkEdge(Edge e0, Edge e1) {
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
        int sd1 = from.getSide();
        int sd2 = to.getSide();
        if (sd1 == sd2) return sd1;
        return -1;
    }

    public void disconnect() {
        Edge e1 = getTwin();
        Facet f0 = getFacet(), f1 = e1.getFacet();
        if (f0.getLoop() == this) 
            f0.setLoop(getNext());
        Vector<Edge> edges = f1.getEdges();
        for (int i = 0; i < edges.size(); i ++) {
            Edge e = edges.get(i);
            e.setFacet(f0);
        }
        e1.getPrev().setNext(getNext());
        getNext().setPrev(e1.getPrev());
        e1.getNext().setPrev(getPrev());
        getPrev().setNext(e1.getNext());
    }

    public Vertex splitAt(Vertex v) {
        if (Arithmetic.sgn(from.getPoint().distLoo(v.getPoint())) == 0) 
            return from;
        if ( Arithmetic.sgn(to.getPoint().distLoo(v.getPoint())) == 0) 
            return to;
        Edge e1 = this;
        Edge e0 = twin;
        Edge e1_ = new Edge(v, e1.to);
        Edge e0_ = new Edge(v, e0.to);
        e1.to = v;
        e0.to = v;
        v.setHead(e0_);
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
}