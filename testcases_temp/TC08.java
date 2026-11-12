package testcases_temp;

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
        b = 10;
        o1 = new B(); // o1 -> [o22]
        o1.f1 = new B(); // o1 -> [o22] -f1-> [o23]
        o2 = o1.f1; // o2 -> [o23]
        o2.f1 = new B(); // o2 -> [o23] -f1-> [o25]
        o3 = o2.f1; // o3 -> [o25]
        o3.f1 = new B(); // o3 -> [o25] -f1-> [o27]
        o4 = o3.f1; // o4 -> [o27]
        o5 = o4; // o5 -> [o27]
        a = o5.bar(o1); // o5 -> [o27] // Monomorphic
        o4.f1 = new B(); // o4 -> [\bot] -f1-> [\bot]
        o6 = o4.f1; // o6 -> [\bot]
        a = o6.bar(o2); // o6 -> [\bot] // Virtual
        return b;
    }

}

class B extends A {
    B f1;
    int f2;

    public boolean bar(B p1) {
        B t1;
        int t2;
        t1 = new B();
        t1.f1 = p1;
        t2 = t1.foobar(); // PTA of t1 = {O44) // Monomorphic
        System.out.println(t2);
        return true;
    }

    public int foobar() {
        int t4;
        t4 = 10;
        System.out.println(t4);
        return t4;
    }
}
