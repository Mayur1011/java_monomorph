// Testcase 1:
// Handling storing to globals

class Test {
	Test f1;
	static Test global;

	public static void main(String[] args) {
		Test o1 = new Test();
		Test o2 = new Test();
		Test o3 = new Test();
		o1.f1 = o2;
		o2.f1 = o3;
		global = o1;
	}
}
