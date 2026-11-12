// Testcase 6:
// Handling Inheritance

class Test
{
	Test f1;
	Test f2;
	public static void main(String[] args) {
		Test o1 = new Test();
		foo(o1);
	}
	static void foo(Test p1) {
		if(p1 instanceof Child) {
			p1.f1 = new Child();
		} else {
			p1.f1 = new Test();
		}
		p1.f1.f2 = new Test();
		p1.f1.bar();
	}
	void bar() {
		this.f1 = new Test();
	}
}
class Child extends Test {
	void bar() {
		this.f1 = new Child();
	}
}
