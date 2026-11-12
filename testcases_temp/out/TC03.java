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
        A o3;
        A o4;
        a = true;
        b = 10;
        c = 20;
        a = b <= c;
        o1 = new A();
        while (a) {
            o1 = new A();
            o3 = (A) o1;
            b = A.bar(o3, b);
            a = b <= c;
        }
        o4 = (A) o1;
        b = A.bar(o4, b);
        return b;
    }

    public int bar(int p1) {
        B t1;
        int t2;
        p1 = p1 + 1;
        System.out.println(p1);
        return p1;
    }

    public static int bar(A z, int p1) {
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