import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

public class Facet {

    private Edge loop;
    private boolean visited;

    public void display() {
        System.out.print("[ ");
        Vector<Edge> edges = getEdges();
        for (int i = 0; i < edges.size(); i ++) {
            Edge e = edges.get(i);
            System.out.print(e.getFrom().getId() + " ");
        }
        System.out.println("]");
    }

    public void setLoop(Edge e) {
        loop = e;
    }

    public Edge getLoop() {
        return loop;
    }

    public void setVisited() {
        visited = true;
    }

    public void cleanVisited() {
        visited = false;
    }

    public boolean isVisited() {
        return visited;
    }

    public Edge getStartEdge() {
        Edge result = null, e = loop;
        do {
            Point p = e.getFrom().getPoint();
            if (result == null 
            || result.getFrom().getPoint().cartesianCompareTo(p) == -1)
                result = e;
            e = e.getNext();
        } while (e != loop);
        return result;
    }

    public Edge getEndEdge() {
        Edge result = null, e = loop;
        do {
            Point p = e.getFrom().getPoint();
            if (result == null 
            || result.getFrom().getPoint().cartesianCompareTo(p) == 1) 
                result = e;
            e = e.getNext();
        } while (e != loop);
        return result;
    }

    public void triangulate() {
        Edge start = getStartEdge();
        Edge end = getEndEdge();
        Vector<Map.Entry<String, Vertex>> queue = new Vector<Map.Entry<String, Vertex>>();
        queue.add(new AbstractMap.SimpleEntry<String, Vertex>("3", start.getFrom()));
        Edge upper = start;
        Edge lower = start.getPrev();
        while (upper.getTo() != end.getFrom() && lower.getFrom() != end.getFrom()) {
            if (upper.getTo().getPoint().cartesianCompareTo(lower.getFrom().getPoint()) == 1) {
                queue.add(new AbstractMap.SimpleEntry<String, Vertex>("0", upper.getTo()));
                upper = upper.getNext();
            }
            else {
                queue.add(new AbstractMap.SimpleEntry<String, Vertex>("1", lower.getFrom()));
                lower = lower.getPrev();
            }
        }
        while (upper.getTo() != end.getFrom()) {
            queue.add(new AbstractMap.SimpleEntry<String, Vertex>("0", upper.getTo()));
            upper = upper.getNext();
        }
        while (lower.getFrom() != end.getFrom()) {
            queue.add(new AbstractMap.SimpleEntry<String, Vertex>("1", lower.getFrom()));
            lower = lower.getPrev();
        }
        queue.add(new AbstractMap.SimpleEntry<String, Vertex>("3", end.getFrom()));
        Stack<Map.Entry<String, Vertex>> stack = new Stack<Map.Entry<String, Vertex>>();
        stack.push(queue.get(0));
        stack.push(queue.get(1));
        Map.Entry<String, Vertex> A = null, B = null, C = null;
        for (int i = 2; i < queue.size() - 1; i ++) {
            A = queue.get(i);
            if (! A.getKey().equals(stack.peek().getKey())) {
                for (int j = 1; j < stack.size(); j ++) {
                    B = stack.get(j);
                    A.getValue().connect(B.getValue(), this);
                }
                while (! stack.empty()) 
                    B = stack.pop();
                stack.push(queue.get(i - 1));
                stack.push(A);
            }
            else {
                B = stack.pop();
                while (! stack.empty()) {
                    Vect v0 = new Vect(stack.peek().getValue().getPoint(), B.getValue().getPoint());
                    Vect v1 = new Vect(B.getValue().getPoint(), A.getValue().getPoint());
                    BigDecimal c = v0.cross(v1);
                    if (A.getKey().equals("0") && Arithmetic.sgn(c) > 0 || A.getKey().equals("1") && Arithmetic.sgn(c) < 0) {
                        C = stack.pop();
                        A.getValue().connect(C.getValue(), this);
                        B = C;
                    }
                    else 
                        break;
                }
                stack.push(B);
                stack.push(A);
            }
        }
        A = queue.get(queue.size() - 1);
        stack.pop();
        for (int j = 1; j < stack.size(); j ++) {
            B = stack.get(j);
            A.getValue().connect(B.getValue(), this);
        }
    }

    public Vector<Edge> getEdges() {
        Vector<Edge> result = new Vector<Edge>();
        Edge e = loop;
        do {
            result.add(e);
            e = e.getNext();
        } while (e != loop);
        return result;
    }

    public BigDecimal area() {
        BigDecimal result = BigDecimal.ZERO;
        Vertex origin = loop.getFrom();
        Vector<Edge> edges = getEdges();
        for (int i = 0; i < edges.size(); i ++) {
            Edge e = edges.get(i);
            Vect v0 = new Vect(origin, e.getFrom());
            Vect v1 = new Vect(origin, e.getTo());
            BigDecimal c = v0.cross(v1).divide(new BigDecimal(2));
            result = result.add(c);
        }
        return result;
    }
}
