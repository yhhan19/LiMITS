import java.util.Vector;

public class Vertex {

    private Point p;
    private Edge head;
    private int id, side;
    private ShortestPathTree[] sp;

    public Vertex(int id, Point p) {
        this.id = id;
        this.p = p;
        this.sp = new ShortestPathTree[2];
        this.side = -1;
    }

    public void displayShortestPath(int i) {
        this.sp[i].display(i);
    }

    public void display() {
        System.out.print(id + " ");
        p.display();
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setHead(Edge head) {
        this.head = head;
    }
    
    public void setShortestPathTree(int i, ShortestPathTree sp) {
        this.sp[i] = sp;
    }

    public void setSide(int side) {
        this.side = side;
    }

    public int getId() {
        return id;
    }

    public Point getPoint() {
        return p;
    }

    public Edge getHead() {
        return head;
    }

    public ShortestPathTree getShortestPathTree(int i) {
        return sp[i];
    }

    public int getSide() {
        return side;
    }

    public Edge connect(Vertex that, Facet f0) {
        Edge e0 = new Edge(this, that);
        Edge e1 = new Edge(that, this);
        Edge.twinEdge(e0, e1);
        Edge ei = this.getHead();
        do {
            if (ei.getFacet() == f0) break;
            ei = ei.getVertexNext();
        } while (ei != this.getHead());
        Edge ej = that.getHead();
        do {
            if (ej.getFacet() == f0) break;
            ej = ej.getVertexNext();
        } while (ej != that.getHead());
        Edge.linkEdge(ej.getPrev(), e1);
        Edge.linkEdge(ei.getPrev(), e0);
        Edge.linkEdge(e0, ej);
        Edge.linkEdge(e1, ei);
        Facet f1 = new Facet();
        if (ei.getNext().getTo() == that) {
            e0.setFacet(f0);
            e1.setFacet(f1);
            f0.setLoop(e0);
            f1.setLoop(e1);
            for (Edge ek = ei; ek.getFrom() != that; ek = ek.getNext()) 
                ek.setFacet(f1);
        }
        else {
            e0.setFacet(f1);
            e1.setFacet(f0);
            f0.setLoop(e1);
            f1.setLoop(e0);
            for (Edge ek = ej; ek.getFrom() != this; ek = ek.getNext()) 
                ek.setFacet(f1);
        }
        return e0;
    }

    public void shortestLink(int i, Vertex parent) {
        ShortestPathTree sp = null;
        if (parent == null)
            sp = new ShortestPathTree(this, null);
        else 
            sp = new ShortestPathTree(this, parent.getShortestPathTree(i));
        setShortestPathTree(i, sp);
    }

    public boolean leftConvex(int i) {
        return sp[i].leftConvex();
    }

    public boolean rightConvex(int i) {
        return sp[i].rightConvex();
    }

    public Vector<Edge> getEdges() {
        Vector<Edge> result = new Vector<Edge>();
        Edge e = head;
        do {
            result.add(e);
            e = e.getVertexNext();
        } while (e != head);
        return result;
    }

    public Edge match(Vertex that) {
        Vector<Edge> edges = getEdges();
        for (int i = 0; i < edges.size(); i ++) {
            Edge e = edges.get(i);
            if (e.getTo() == that) 
                return e;
        }
        return null;
    }

    public Edge match(Facet f) {
        Vector<Edge> edges = getEdges();
        for (int i = 0; i < edges.size(); i ++) {
            Edge e = edges.get(i);
            if (e.getFacet() == f)
                return e;
        }
        return null;
    }
}
