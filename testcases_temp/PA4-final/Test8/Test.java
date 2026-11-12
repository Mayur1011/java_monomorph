// Scenario: Multi-level inheritance and dynamic dispatch optimization
class Animal {
  void speak() {
  }
}

class Mammal extends Animal {
}

class Dog extends Mammal {
  void speak() {
  }
}

public class Test {
  public static void main(String[] args) {
    Animal a = new Dog();
    Mammal m = new Dog();
    Dog d = new Dog();

    // Expected: All 3 variables point to Dog allocation site.
    // All calls transformed into static Dog.speak.
    a.speak();
    m.speak();
    d.speak();
  }
}