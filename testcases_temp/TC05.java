package testcases_temp;

class TC05 {
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
        a = true;
        b = 10;
        o1 = new B();
        if (a) {
            o2 = new B();
        } else {
            o2 = new B();
        }
        a = o2.bar(o1); // PTA of O2 = {O21, O23} But only one bar method is possible --> Monomorphic
                        // call
        return b;
    }
}

class B {
    int f1;
    B f2;

    public boolean bar(B p1) {
        B t1;
        int t2;
        t1 = new B();
        t2 = t1.foobar(); // PTA of t1 = {O35) // Monomorphic
        System.out.println(t2);
        return true;
    }

    public int foobar() {
        return 1;
    }
}