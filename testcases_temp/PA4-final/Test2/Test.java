// Scenario: Basic Polymorphic Call. The variable can point to two different allocation sites
// (A and B) depending on control flow.
// Expected: The call is polymorphic and cannot be safely devirtualized.

class Base {
  public void invoke() {
  }
}

class A extends Base {
  public void invoke() {
  }
}

class B extends Base {
  public void invoke() {
  }
}

public class Test {
  public static void main(String[] args) {
    Base obj;
    if (args.length > 0) {
      obj = new A();
    } else {
      obj = new B();
    }
    // Expected: Remains a virtual call (Polymorphic)
    obj.invoke();
  }
}