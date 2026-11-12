class A {
    int f;
}

public class Test {
    public static void main(String[] args) {
        A a = new A(); // O0
        A c = new A(); // O1
        A b;
        if (a == c) {
            b = a;
        } else {
            b = c;
        }

        b.f = 10;
    }
}
