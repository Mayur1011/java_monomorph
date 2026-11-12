// Scenario: Partial Devirtualization (Mixed monomorphic and polymorphic loops).
// Some of the virtual calls can be monomorphic and optimized to static calls, while others remain polymorphic.

class Animal {
  void eat() {
  }
}

class Cat extends Animal {
  void eat() {
  }
}

class Dog extends Animal {
  void eat() {
  }
}

public class Test {
  public static void main(String[] args) {
    Animal safeCat = new Cat();
    Animal safeDog = new Dog();
    Animal mixed = null;

    for (int i = 0; i < 50; i++) {
      // Expected: safeCat and safeDog never change types are monomorphic and should
      // be optimized to static.
      safeCat.eat();
      safeDog.eat();

      if (i % 2 == 0) {
        mixed = new Cat();
      } else {
        mixed = new Dog();
      }

      // Expected: 'mixed' points to both Cat and Dog.
      mixed.eat();
    }
  }
}