// Testcase 5:
// Handling conditionals.

class Test
{
	Test f1;
	public static void main(String[] args) {
		Test o1 = new Test();
		Test o2 = new Test();
		int i = 0;
		o1.foo(o2, i);
	}
	
	void foo(Test p1, int i) {
		Test o3 = new Test();
		if(i == 0) {
			return;
		}
		foo(new Test(), i++);
	}
}
