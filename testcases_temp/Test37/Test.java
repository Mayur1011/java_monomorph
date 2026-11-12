class Node {
    Node f1, f2, g;

    Node() {

    }

    void foo(Node p, Node q) {
        p.f1 = new Node();
        Node ret = bar(p);
        System.out.println(ret);
    }

    Node bar(Node p) {
        p.f2 = new Node();
        System.out.println(p);
        p = new Node();
        p.f2 = new Node();
        return p;
    }
}

class Node_Child extends Node {
    static Node static_field;

    void foo(Node p, Node q) {
        static_field = bar(p);
    }
}

public class Test {
    public static void main(String[] args) {
        Node a = new Node();
        Node b = new Node();
        Node c = new Node();
        a.foo(b, c);
        Node_Child.static_field = new Node();
        a.f1 = new Node();
        a.f2 = new Node();
        a.f1.f1 = Node_Child.static_field;

    }
}
