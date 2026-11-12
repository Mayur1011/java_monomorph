class M {
    int x, y;

    public M() {
    }
}

public class Test {
    public static void main(String[] args) {
        Object o = new Object(); // O1 Target - java.lang.Object
        Test m = new Test(); // O2 Target - java.lang.Object
        M x = new M(); // O3 Target - Test : void <init> ()
        return;
    }
}