class TC07{
    public static void main(String[] args){
        A o;
        B z;
        int res;
        o = new A();
        z = new B();
        res = o.foo(z);
        System.out.println(res);
    }
}
    
class A {
    A f1;
    public int foo(B z) {
        B o1;
        B o2;
        int a;
        int b;
        boolean c;
        B o3;
        B o4;
        b = 10;
        f1 = new B();
        o1 = new B();
        o1.f1 = new B();
        o2 = o1.f1;
        o3 = (B)o2;
        a = B.foobar(o3);
        o4 = (B)o1;
        a = B.foobar(o4);
        c = z.bar(o1);
        return b;
    }
}

class B extends A{
    B f1;
    int f2;

    public boolean bar(B p1) {
        B t1;
        int t2;
        B t3;
        f1 = new B();
        t1 = new B();
        t3 = (B)t1;
        t2 = B.foobar(t3);
        System.out.println(t2);
        return true;
    }
    public int foobar() {
        B t3;
        int t4;
        t3 = new B();
        t3.f2 = 22;
        t4 = t3.f2;
        System.out.println(t4);
        return 1;
    }

    public static int foobar(B z){
        B t3;
        int t4;
        int t5;
        t5 = z.f2;
        t3 = new B();
        t3.f2 = 22;
        t4 = t3.f2;
        System.out.println( t4 );
        return 1;
    }
}
