class Node {
    Node f1;
    Node f2;
    Node g;
    static Node s;

    public void foo(Node x, Node y, int i) {
        x.f2 = y;
    }
}

public class Test {
    public static void main(String[] args) {
        Node a = new Node(); // O14
        a.f1 = new Node(); // O15
        Node b = new Node(); // O16
        b.f1 = new Node(); // O17
        a.f2 = new Node(); // O18
        a.f2.f1 = new Node(); // O19
        a.foo(a, b, 1);
    }
}