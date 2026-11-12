package testcases_temp;

class TC09 {
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
        boolean a;
        int b;
        b = 30;
        o1 = new B();
        o2 = o1.bar(); // PTA of o1 = {O21) // Monomorphic
        o2.f1 = new B();
        o3 = new B();
        o3.f1 = new B();
        o4 = o3.f1;
        o5 = o4.bar(); // PTA of o4 = {O21) // Monomorphic
        return b;
    }

}

class B extends A {
    B f1;

    public B bar() {
        B t1;
        int t2;
        t1 = new B();
        t2 = 20;
        System.out.println(t2);
        return t1;
    }
}