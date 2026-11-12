// Scenario: Loop causing path explosion but stable points-to set.
// A loop heavily creating objects, passing them through methods.
class Data {
  void process() {
  }
}

public class Test {
  static void handle(Data d) {
    // Expected: Devirtualized since d always points to Data
    d.process();
  }

  public static void main(String[] args) {
    for (int i = 0; i < 100; i++) {
      Data d = new Data();
      for (int j = 0; j < 50; j++) {
        handle(d);
      }
    }
  }
}