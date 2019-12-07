import java.math.BigDecimal;
import javafx.util.Pair;

public class Chord {

    Pair<Vertex, Edge> from, to;

    private void init() {
        Vertex v0 = getFromVertex();
        Vertex v1 = getToVertex();
        Edge e0 = getFromEdge();
        Edge e1 = getToEdge();
        if (v0.getSide() == -1) v0.setSide(e0.getSide());
        if (v1.getSide() == -1) v1.setSide(e1.getSide());
    }

    public Chord(Vertex from, Vertex to, Edge efrom, Edge eto) {
        this.from = new Pair<Vertex, Edge>(from, efrom);
        this.to = new Pair<Vertex, Edge>(to, eto);
        init();
    }

    public Chord(Vertex from, Vertex to) {
        this.from = new Pair<Vertex, Edge>(from, from.getHead());
        this.to = new Pair<Vertex, Edge>(to, to.getHead());
        init();
    }

    public Chord(Edge e) {
        this.from = new Pair<Vertex, Edge>(e.getFrom(), e.getPrev());
        this.to = new Pair<Vertex, Edge>(e.getTo(), e.getNext());
        init();
    }

    public void display() {
        getFromVertex().display();
        getToVertex().display();
        if (getFromEdge() != null) getFromEdge().display();
        if (getToEdge() != null) getToEdge().display();
        System.out.println(getFromVertex().getSide() + " " + getToVertex().getSide());
    }

    public boolean intermediate() {
        return (getFromVertex().getSide() == 1 && getToVertex().getSide() == 2)
            || (getFromVertex().getSide() == 2 && getToVertex().getSide() == 1);
    }

    public boolean valid() {
        BigDecimal d = getFromVertex().getPoint().distLoo(getToVertex().getPoint());
        return Arithmetic.sgn(d) != 0;
    }

    public Pair<Vertex, Edge> getFrom() {
        return from;
    }

    public Pair<Vertex, Edge> getTo() {
        return to;
    }

    public Vertex getFromVertex() {
        return from.getKey();
    }

    public Vertex getToVertex() {
        return to.getKey();
    }

    public Edge getFromEdge() {
        return from.getValue();
    }

    public Edge getToEdge() {
        return to.getValue();
    }

    public Facet getFirstFacet() {
        if (from.getValue() != null) 
            return from.getValue().getTwin().getFacet();
        else 
            return to.getValue().getTwin().getFacet(); 
    }

    public Pair<Vertex, Edge> getToSplit() {
        if (from.getValue() != null && to.getValue() == null) return from;
        if (from.getValue() == null && to.getValue() != null) return to;
        throw new NullPointerException("chord error");
    }

    public Vertex getToConnect() {
        if (from.getValue() != null && to.getValue() == null) return to.getKey();
        if (from.getValue() == null && to.getValue() != null) return from.getKey();
        throw new NullPointerException("chord error");
    }

    public BigDecimal reach() {
        BigDecimal fx = from.getKey().getPoint().getX();
        BigDecimal tx = to.getKey().getPoint().getX();
        if (fx.compareTo(tx) == 1) 
            return fx;
        else 
            return tx; 
    }
}
