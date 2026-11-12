// Testcase 7:
// Handling Inheritance with more objects.

class Test
{
	Test f1;
	Test f2;
	public static Test global;
	public static void main(String[] args) {
		Test o1 = new Test();
		Test o2 = new Test();
		Test o3 = new Test();
		int a;
		a = args.length;
		Test o4 = o1.foo(o2, o3 , a);
		o4.f2 = new Test();
	}
	
	Test foo(Test p1, Test p2, int val ) {
		Test a1 = new Test();
		if(val == 0) {
			a1.f1 = new Test();
		} else if (val == 1 ) {
			a1.f1 = new Child();
			global = a1.f1;
		} else {
			a1.f1 = new Child2();
		}
		Test o5 = new Test();
		a1.f1.f2= new Test();
		o5.f1 = a1.f1;
		p2.f1 = new Test();
		return o5.f1;
	}
}

class Child extends Test {
	Test foo(Test p1, Test p2, int val ) {
		return new Test();
	}
}

class Child2 extends Test {
	Test foo(Test p1, Test p2, int val ) {
		return new Test();
	}
}