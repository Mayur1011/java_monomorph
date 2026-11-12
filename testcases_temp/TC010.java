package testcases_temp;

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
        b = 300;
        o1 = new B(); // PTA of o1 = {O20)
        o1.f1 = new B(); // PTA of o1.f1 = {O21)
        o2 = o1; // PTA of o1 = {O21)
        o2.f1 = new B(); // PTA of o2.f1 = {O23}
        o3 = o1.f1; // PTA of o3 = {O23}
        o4 = o3.bar(); // PTA of o3 = {O23) // Monomorphic
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
        t4 = t3.foobar(); // PTA of t3 = {\bot)
        return t1;
    }

    public int foobar() {
        int t5;
        t5 = 10;
        System.out.println(t5);
        return t5;
    }
}
