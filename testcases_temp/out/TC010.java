class TC010 {
    public static void main(String[] args) {
        A o;
        int res;
        o = new A();
        res = o.foo();
        System.out.println(res);
    }
}

class A {
    A f1;

    public int foo() {
        B o1;
        A o2;
        B o3;
        B o4;
        int b;
        B o5;
        b = 300;
        o1 = new B();
        o1.f1 = new B();
        o2 = o1;
        o2.f1 = new B();
        o3 = o1.f1;
        o5 = (B) o3;
        o4 = B.bar(o5);
        return b;
    }
}

class B extends A {
    B f1;

    public B bar() {
        B t1;
        B t2;
        B t3;
        int t4;
        t1 = new B();
        f1 = new B();
        t2 = new B();
        t2.f1 = f1;
        t3 = t2.f1;
        t4 = t3.foobar();
        return t1;
    }

    public int foobar() {
        int t5;
        t5 = 10;
        System.out.println(t5);
        return t5;
    }

    public static B bar(B z) {
        B t1;
        B t2;
        B t3;
        int t4;
        B t5;
        t5 = z.f1;
        t1 = new B();
        t5 = new B();
        z.f1 = t5;
        t2 = new B();
        t2.f1 = t5;
        t3 = t2.f1;
        t4 = t3.foobar();
        t5 = z.f1;
        return t1;
    }
}
