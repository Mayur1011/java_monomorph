package testcases_temp;

class TC04 {
    public static void main(String[] args) {
        A o;
        int res;
        o = new A();
        res = o.foo();
        System.out.println(res);
    }
}

class A {
    int f1;

    public int foo() {
        A o1;
        A o2;
        B o3;
        B o4;
        int a;
        o1 = new A();
        a = o1.bar(o1); // PTA of o1 = {O20) // Monomorphic
        o2 = new A();
        a = o2.bar(o1); // PTA of o2 = {O22) // Monomorphic
        o3 = new B();
        o4 = new B();
        a = o3.bar(o4); // PTA of o3 = {O23) // Monomorphic
        a = o4.bar(o3); // PTA of o4 = {O24) // Monomorphic
        a = o3.bar(o3); // PTA of o3 = {O23) // Monomorphic
        return a;
    }

    public int bar(A p1) {
        B t1;
        int t2;
        p1.f1 = 1;
        t2 = p1.f1;
        System.out.println(t2);
        return t2;
    }
}

class B extends A {
    int f1;

    public int bar(B p2) {
        B t1;
        int t2;
        p2.f1 = 1;
        t2 = p2.f1;
        return t2;
    }
}