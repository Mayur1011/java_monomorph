package testcases_temp;

class TC03 {
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
        A o1;
        A o2;
        boolean a;
        int b;
        int c;
        a = true;
        b = 10;
        c = 20;
        a = b <= c;
        o1 = new A(); // PTA of o1 = {O22)
        while (a) {
            o1 = new A(); // PTA of o1 = {O24};
            b = o1.bar(b); // PTA of o1 = {O22, O24} --> Monomorphic call
            a = b <= c;
        }
        b = o1.bar(b); // PTA of o1 = {O24};
        return b;
    }

    public int bar(int p1) {
        B t1;
        int t2;
        p1 = p1 + 1;
        System.out.println(p1);
        return p1;
    }
}

class B extends A {
    int f1;
    B f2;

    public int bar(int p2) {
        B t1;
        int t2;
        p2 = p2 + 1;
        return p2;
    }
}