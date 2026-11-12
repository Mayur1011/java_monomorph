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
    public int foo ( ) { 
        A o1; 
        A o2; 
        B o3; 
        B o4; 
        int a; 
        A o5; 
        A o6; 
        B o7; 
        B o8; 
        B o9; 
        o1 = new A(); 
        o5 = (A)o1; 
        a = A.bar(o5, o1); 
        o2 = new A(); 
        o6 = (A)o2; 
        a = A.bar(o6, o1); 
        o3 = new B(); 
        o4 = new B(); 
        o7 = (B)o3; 
        a = B.bar(o7, o4); 
        o8 = (B)o4; 
        a = B.bar(o8, o3); 
        o9 = (B)o3; 
        a = B.bar(o9, o3); 
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
    public static int bar (A z, A p1) { 
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
    public static int bar(B z, B p2) { 
        B t1 ; 
        int t2; 
        p2.f1 = 1; 
        t2 = p2.f1; 
        return t2; 
    } 
}