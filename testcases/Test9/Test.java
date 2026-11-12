// Scenario: Method Chaining.
// Multiple virtual calls on the same monomorphic object reference.
class A {
  A select() {
    return this;
  }

  A from() {
    return this;
  }

  A where() {
    return this;
  }

  A orderBy() {
    return this;
  }

  A limit() {
    return this;
  }

  void execute() {
  }
}

public class Test {
  public static void main(String[] args) {
    A a = new A();

    // Expected: All these consecutive virtual calls should be optimized to static
    // calls
    // since `builder` is purely monomorphic.
    a.select()
        .from()
        .where()
        .orderBy()
        .limit()
        .execute();

    A b = new A();
    b.select().from().execute();
  }
}