// Scenario: Field Sensitivity. Objects are stored in fields and later retrieved.
// The analysis must track heap objects accurately to determine the exact type.
// Expected: Call is devirtualized as the field points to a specific allocation.

class Helper {
  void doWork() {
  }
}

class Container {
  Helper h;
}

public class Test {
  public static void main(String[] args) {
    Container c = new Container();
    c.h = new Helper();

    Helper temp = c.h;
    temp.doWork();// Expected: Devirtualized (heap tracking identifies 'temp' as perfectly
                  // monomorphic Helper)

  }
}