// Testcase 5:
// Handling conditionals.

class Test {
	Test f1;
	Test f2;

	public static void main(String[] args) {
		Test o1 = new Test();
		Test o2 = new Test();
		Test o3 = new Test();
		boolean b;
		b = false;
		Test o4 = o1.foo(o2, o3, b);
		o4.f2 = new Test();
	}

	Test foo(Test p1, Test p2, Boolean b) {
		if (b) {
			p1.f1 = new Test();
			return p1.f1;
		} else {
			p1.f1 = new Test();
		}
		p1.f1.f2 = new Test();
		p2.f1 = new Test();
		return p2.f1;
	}
}
