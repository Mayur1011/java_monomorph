package testcases_temp;

class TC06 {
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
        boolean a;
        int b;
        b = 10;
        o1 = new B();
        o2 = o1;
        a = o2.bar(o1); // PTA of o2 = {O18) // Monomorphic
        a = o1.bar(o2); // PTA of o1 = {O18) // Monomorphic
        return b;
    }
}

class B extends A {
    B f1;

    public boolean bar(B p1) {
        B t1;
        int t2;
        p1.f1 = new B();
        t1 = p1.f1;
        t2 = t1.foobar(); // PTA of t1 = {\bot)
        System.out.println(t2);
        return true;
    }

    public int foobar() {
        B t3;
        int t4;
        t3 = new B();
        t4 = t3.fb(); // PTA of t3 = {O) // Monomorphic
        System.out.println(t4);
        return 1;
    }

    public int fb() {
        return 2;
    }
}