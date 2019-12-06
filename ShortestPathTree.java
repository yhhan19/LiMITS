import java.math.BigDecimal;

public class ShortestPathTree {
    
    private Vertex vertex;
    private ShortestPathTree parent;
    private int convex, depth;
    private BigDecimal cross;

    public ShortestPathTree(Vertex vertex, ShortestPathTree parent) {
        this.vertex = vertex;
        this.parent = parent;
        if (parent == null) {
            this.depth = 0;
            this.convex = 3;
            this.cross = BigDecimal.ZERO;
        }
        else {
            this.depth = parent.getDepth() + 1;
            ShortestPathTree grandparent = parent.getParent();
            if (grandparent == null) {
                this.convex = 3;
                this.cross = BigDecimal.ZERO;
            }
            else {
                Vect v0 = new Vect(grandparent.getVertex(), parent.getVertex());
                Vect v1 = new Vect(parent.getVertex(), vertex);
                this.cross = v0.cross(v1);
                switch (Arithmetic.sgn(this.cross)) {
                    case -1:
                        this.convex = parent.getConvex() & 1;
                        break;
                    case 0:
                        this.convex = parent.getConvex() & 3;
                        break;
                    case 1:
                        this.convex = parent.getConvex() & 2;
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public void display(int index) {
        System.out.print("( ");
        ShortestPathTree t = this;
        while (t != null) {
            System.out.print(t.vertex.getId() + " ");
            t = t.vertex.getShortestPathTree(index).getParent();
        }
        System.out.println(")");
        System.out.print("( ");
        t = this;
        while (t != null) {
            System.out.print(t.cross + " ");
            t = t.vertex.getShortestPathTree(index).getParent();
        }
        System.out.println(")");
        System.out.print("[ ");
        t = this;
        while (t != null) {
            t.vertex.display();
            t = t.vertex.getShortestPathTree(index).getParent();
        }
        System.out.println("]");
    }

    public Vertex getVertex() {
        return vertex;
    }

    public ShortestPathTree getParent() {
        return parent;
    }

    public int getConvex() {
        return convex;
    }

    public int getDepth() {
        return depth;
    }
    
    public boolean leftConvex() {
        return (convex & 2) > 0;
    }

    public boolean rightConvex() {
        return (convex & 1) > 0;
    }
}
