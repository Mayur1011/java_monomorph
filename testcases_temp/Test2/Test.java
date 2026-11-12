// // Testcase 2:
// // Handling return and arguments

// class Test {
//   Test f1;

//   public static void main(String[] args) {
//     Test o1 = new Test();
//     Test o4 = o1.bar(o1.f1); // monomorphic call
//     o4.f1 = new Test();
//   }

//   Test bar(Test p1) {
//     Test o5 = new Test();
//     o5.foo();
//     o5.foo();
//     return o5;
//   }

//   Test foo() {
//     return new Test();
//   }

// }

// Scenario: Basic Monomorphic Call. A straightforward object allocation and method invocation.

class A {
  public void foo() {
    System.out.println("A.foo");
  }
}

public class Test {
  public static void main(String[] args) {
    A obj = new A();
    obj.foo(); // Expected: Devirtualized to static call
  }
}
