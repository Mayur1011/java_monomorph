class A {
    int f;
    static A global;

    A foo() {
        A a = new A(); // O6
        A c = new A(); // O7
        A b;
        if (a.f > 0) {
            b = a;
        } else {
            b = c;
        }
        global = b;
        b.f = 10;
        return b;

    }

}

public class Test {
    public static void main(String[] args) {
        A p = new A(); // O24
        p.foo();
        A q = p.foo();
        A r = q.foo();
    }
}
