class TC08 {
    public static void main(String[] args) {
        A o;
        int res;
        o = new A();
        res = o.foo();
        System.out.println(res);
    }
}

class A {
    public int foo() {
        B o1;
        B o2;
        B o3;
        B o4;
        B o5;
        B o6;
        boolean a;
        int b;
        // B o6;
        B o7;
        b = 10;
        o1 = new B();
        o1.f1 = new B();
        o2 = o1.f1;
        o2.f1 = new B();
        o3 = o2.f1;
        o3.f1 = new B();
        o4 = o3.f1;
        o5 = o4;
        o6 = (B) o5;
        a = B.bar(o6, o1);
        o4.f1 = new B();
        o6 = o4.f1;
        a = o6.bar(o2);
        return b;
    }

}

class B extends A {
    B f1;
    int f2;

    public boolean bar(B p1) {
        B t1;
        int t2;
        B t3;
        t1 = new B();
        t1.f1 = p1;
        t3 = (B) t1;
        t2 = B.foobar(t3);
        System.out.println(t2);
        return true;
    }

    public int foobar() {
        int t4;
        t4 = 10;
        System.out.println(t4);
        return t4;
    }

    public static boolean bar(B z, B p1) {
        B t1;
        int t2;
        t1 = new B();
        t1.f1 = p1;
        t2 = t1.foobar();
        System.out.println(t2);
        return true;
    }

    public static int foobar(B z) {
        int t4;
        t4 = 10;
        System.out.println(t4);
        return t4;
    }
}
