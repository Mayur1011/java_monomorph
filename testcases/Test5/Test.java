// Scenario: Field Sensitivity with some monomorphic cases.
// Shows that a field can be monomorphic in one instance but polymorphic in another instance of the same class.
class A {
  void execute() {
  }
}

class B extends A {
  void execute() {
  }
}

class C extends A {
  void execute() {
  }
}

class M {
  A st;

  void setState(A s) {
    this.st = s;
  }

  A getState() {
    return this.st;
  }
}

public class Test {
  public static void main(String[] args) {
    M fM = new M();
    fM.setState(new B());

    M sM = new M();

    for (int i = 0; i < 100000; i++) {
      // Expected to be STATIC: fM.st only ever points to the single B allocation
      // site.
      // The method return is monomorphic.
      fM.getState().execute();

      if (i % 2 == 0) {
        sM.setState(new B());
      } else {
        sM.setState(new C());
      }

      // Expected to REMAIN VIRTUAL: sM.st is overwritten back and forth
      // with multiple types.
      // The points-to set for the field 'st' of the 'sM' allocation site
      // has size > 1.
      sM.getState().execute();
    }
  }
}