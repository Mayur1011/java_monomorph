// Scenario: Context Sensitivity (k-level).
// Context-insensitive analysis merges both TaskA and TaskB at the parameter, resulting in a polymorphic points-to set.
// But Context-sensitive analysis keeps them isolated based on the call stack.

abstract class Task {
  abstract void process();
}

class TaskA extends Task {
  void process() {
  }
}

class TaskB extends Task {
  void process() {
  }
}

public class Test {
  static Task wrapTask(Task t) {
    return t;
  }

  public static void main(String[] args) {
    Task t1 = wrapTask(new TaskA());
    // Expected: Devirtualized (Context sensitivity tracks t1 precisely as TaskA)
    t1.process();

    Task t2 = wrapTask(new TaskB());
    // Expected: Devirtualized (Context sensitivity tracks t2 precisely as TaskB)
    t2.process();
  }
}