// Scenario: Strong updates vs weak updates.
// If a local variable is strongly overwritten with the exact same type on all branches, it should remain monomorphic.
// Expected: Devirtualized to FileLogger's method.

abstract class Logger {
  abstract void log();
}

class FileLogger extends Logger {
  void log() {
  }
}

public class Test {
  public static void main(String[] args) {
    Logger logger = new FileLogger();

    if (args.length > 2) {
      logger = new FileLogger(); // same type again
    }

    // Expected: Devirtualized (Points-to set has 2 alloc sites, but BOTH are
    // exactly `FileLogger`)
    logger.log();
  }
}