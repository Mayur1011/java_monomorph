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
        B o3; 
        a = true; 
        b = 10; 
        o1 = new B(); 
        if (a) { 
            o2 = new B(); 
        } else { 
            o2 = new B(); 
        } 
        o3 = (B)o2; 
        a = B.bar(o3 , o1); 
        return b; 
    } 
    } 
class B { 
    int f1 ; 
    B f2 ; 
    public boolean bar ( B p1 ) { 
        B t1; 
        int t2; 
        B t3; 
        t1 = new B(); 
        t3 = (B)t1; 
        t2 = B.foobar(t3); 
        System.out.println(t2); 
        return true; 
    }
    public int foobar() {
        return 1;
    }
    public static boolean bar(B z , B p1) { 
        B t1; 
        int t2; 
        int f1; 
        B f2; 
        t1 = new B(); 
        t2 = t1.foobar(); 
        System.out.println(t2); 
        return true; 
    } 
    public static int foobar(B z) { 
        return 1; 
    } 
} 
     