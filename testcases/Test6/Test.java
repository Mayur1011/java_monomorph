// Scenario: Loops with Object Re-allocation
// Demonstrates that even within a loop, if the same type is allocated,
// the variable points to an object of a consistent type, enabling devirtualization.
class Worker {
  void work() {
  }
}

public class Test {
  public static void main(String[] args) {
    Worker w = null;
    for (int i = 0; i < 10; i++) {
      w = new Worker();
      // Expected: w always points to object of type Worker.
      // Virtual call should be transformed to static call.
      w.work();
    }
  }
}