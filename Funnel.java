import java.util.Vector;
import java.math.BigDecimal;

public class Funnel {
    
    private Vertex apex;
    private Vector<Vertex> left, right;

    public Funnel() {
        this.apex = null;
        this.left = null;
        this.right = null;
    }

    public Funnel(Vertex apex) {
        this.apex = apex;
        left = new Vector<Vertex>();
        right = new Vector<Vertex>();
    }

    public Funnel(Vertex apex, Vertex left, Vertex right) {
        this.apex = apex;
        this.left = new Vector<Vertex>();
        this.right = new Vector<Vertex>();
        if (left != null) 
            this.left.add(left);
        if (right != null)
            this.right.add(right);
    }

    public void copy(Funnel that) {
        this.apex = that.apex;
        this.left = that.left;
        this.right= that.right;
    }

    public void display() {
        System.out.print("[ ");
        for (int i = left.size() - 1; i >= 0; i --) 
            System.out.print(left.get(i).getId() + " ");
        System.out.print("[ " + apex.getId() + " ] ");
        for (int i = 0; i < right.size(); i ++) 
            System.out.print(right.get(i).getId() + " ");
        System.out.println("]");
    }

    public Vertex getApex() {
        return apex;
    }

    public Vertex getLeft(int i) {
        if (i == -1) 
            return apex;
        else 
            return left.get(i);
    }

    public Vertex getRight(int i) {
        if (i == -1)
            return apex;
        else 
            return right.get(i);
    }

    public Vertex getLeftHead() {
        if (left.isEmpty()) 
            return apex;
        else 
            return left.get(0);
    }

    public Vertex getRightHead() {
        if (right.isEmpty())
            return apex;
        else 
            return right.get(0);
    }

    public boolean isLeftEmpty() {
        return left.isEmpty();
    }

    public boolean isRightEmpty() {
        return right.isEmpty();
    }
    
    public Vertex getLeftest() {
        if (left.isEmpty()) 
            return apex;
        else
            return left.lastElement();
    }

    public Vertex getRightest() {
        if (right.isEmpty()) 
            return apex;
        else 
            return right.lastElement();
    }

    public int size() {
        return left.size() + right.size();
    }

    public boolean split(int index, Vertex v, Funnel that) {
        if (left.isEmpty()) {
            v.shortestLink(index, apex);
            that.copy(new Funnel(apex, null, v));
            left.add(v);
            return true;
        }
        if (right.isEmpty()) {
            v.shortestLink(index, apex);
            right.add(v);
            that.copy(new Funnel(apex, v, null));
            return false;
        }
        int i = 0, cni = -1;
        for (i = left.size() - 1; i >= 0; i --) {
            Vect v0 = new Vect(getLeft(i - 1), getLeft(i));
            Vect v1 = new Vect(getLeft(i), v);
            BigDecimal cross = v0.cross(v1);
            if (cross.signum() >= 0) break;
        }
        if (i >= 0) {
            v.shortestLink(index, getLeft(i));
            that.copy(new Funnel(getLeft(i), null, v));
            for (int k = i + 1; k < left.size(); k ++)
                that.left.add(getLeft(k));
            for (int k = left.size() - 1; k > i; k --)
                left.remove(k);
            left.add(v);
            return true;
        }
        int j = 0, cnj = -1;
        for (j = right.size() - 1; j >= 0; j --) {
            Vect v0 = new Vect(getRight(j - 1), getRight(j));
            Vect v1 = new Vect(getRight(j), v);
            BigDecimal cross = v0.cross(v1);
            if (cross.signum() <= 0) break;
        }
        if (j >= 0) {
            v.shortestLink(index, getRight(j));
            that.copy(new Funnel(getRight(j), v, null));
            for (int k = j + 1; k < right.size(); k ++) 
                that.right.add(getRight(k));
            for (int k = right.size() - 1; k > j; k --)
                right.remove(k);
            right.add(v);
            return false;
        }
        else {
            v.shortestLink(index, apex);
            that.copy(new Funnel(getRight(j), v, null));
            for (int k = 0; k < right.size(); k ++) 
                that.right.add(getRight(k));
            for (int k = right.size() - 1; k >= 0; k --)
                right.remove(k);
            right.add(v);
            return false;
        }
    }
}