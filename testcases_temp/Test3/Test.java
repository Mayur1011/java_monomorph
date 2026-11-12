// Testcase 3:
// Handling storing to parameter's field.

class Test
{
	Test f1;
	public static void main(String[] args) {
		Test o1 = new Test();
		Test o2 = new Test();
		Test o3 = new Test();
		o1.foo(o2, o3);
	}
	void foo (Test p1, Test p2) {
		Test o4 = new Test();
		Test o5 = new Test();
		p1.f1 = o4;
		p2.f1 = o5;
	}
}
