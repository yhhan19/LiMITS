package limits.polytope;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.Map;

import limits.geometry.*;

public class Chord {

    Map.Entry<Vertex, Edge> from, to;

    private void init() {
        Vertex v0 = getFromVertex();
        Vertex v1 = getToVertex();
        Edge e0 = getFromEdge();
        Edge e1 = getToEdge();
        if (v0.getSide() == -1) v0.setSide(e0.getSide());
        if (v1.getSide() == -1) v1.setSide(e1.getSide());
    }

    public Chord(Vertex from, Vertex to, Edge efrom, Edge eto) {
        this.from = new AbstractMap.SimpleEntry<Vertex, Edge>(from, efrom);
        this.to = new AbstractMap.SimpleEntry<Vertex, Edge>(to, eto);
        init();
    }

    public Chord(Vertex from, Vertex to) {
        this.from = new AbstractMap.SimpleEntry<Vertex, Edge>(from, from.getHead());
        this.to = new AbstractMap.SimpleEntry<Vertex, Edge>(to, to.getHead());
        init();
    }

    public Chord(Edge e) {
        this.from = new AbstractMap.SimpleEntry<Vertex, Edge>(e.getFrom(), e.getPrev());
        this.to = new AbstractMap.SimpleEntry<Vertex, Edge>(e.getTo(), e.getNext());
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
        return (getFromVertex().getSide() ^ getToVertex().getSide()) == 3;
    }

    public Map.Entry<Vertex, Edge> getFrom() {
        return from;
    }

    public Map.Entry<Vertex, Edge> getTo() {
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

    public BigDecimal reach() {
        BigDecimal fx = from.getKey().getPoint().getX();
        BigDecimal tx = to.getKey().getPoint().getX();
        return fx.compareTo(tx) == 1 ? fx : tx;
    }

    public BigDecimal range() {
        BigDecimal fx = from.getKey().getPoint().getX();
        BigDecimal tx = to.getKey().getPoint().getX();
        return fx.subtract(tx).abs();
    }

    public Vect getVect() {
        return new Vect(getFromVertex().getPoint(), getToVertex().getPoint());
    }
}
