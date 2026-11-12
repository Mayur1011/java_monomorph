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
    public int foo () { 
        B o1; 
        B o2; 
        boolean a; 
        int b; 
        B o3; 
        B o4; 
        b = 10; 
        o1 = new B(); 
        o2 = o1; 
        o3 = (B) o2; 
        a = B.bar( o3 , o1); 
        o4 =(B) o1; 
        a = B.bar(o4 , o2); 
        return b; 
    } 
} 
class B extends A { 
    B f1 ; 
    public boolean bar ( B p1 ) { 
        B t1; 
        int t2; 
        p1.f1 = new B(); 
        t1 = p1.f1; 
        t2 = t1.foobar(); 
        System.out.println(t2); 
        return true; 
    } 
    public int foobar ( ) { 
        B t3; 
        int t4; 
        B t5; 
        t3 = new B(); 
        t5 = (B)t3; 
        t4 = B.fb(t5); 
        System.out.println(t4); 
        return 1; 
    } 
    public int fb() { 
        return 2; 
    } 
    public static boolean bar (B caller_obj, B p1) { 
        B t1; 
        int t2; 
        p1.f1 = new B(); 
        t1 = p1.f1; 
        t2 = t1.foobar(); 
        System.out.println(t2); 
        return true; 
    } 
    public static int fb (B caller_obj) { 
        return 2; 
    } 
} 
     