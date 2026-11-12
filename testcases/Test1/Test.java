// Scenario: Basic Monomorphic Call. A straightforward object allocation and method invocation.

class A {
  public void foo() {
    System.out.println("A.foo");
  }
}

public class Test {
  public static void main(String[] args) {
    long start = System.nanoTime();

    A obj = new A();
    for (int i = 0; i < 10000000; i++) {
      obj.foo(); // Expected: Devirtualized to static call
    }
    long end = System.nanoTime();
    System.out.println("Execution time" + (end - start) / 1_000_000 + " ms");
  }
}
