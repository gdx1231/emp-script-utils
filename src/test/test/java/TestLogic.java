package test.java;

import org.junit.Test;

import com.gdxsoft.easyweb.utils.ULogic;

public class TestLogic extends TestBase {

	public static void main(String[] a) {
		TestLogic t = new TestLogic();
		try {
			t.testLogic();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testLogic() throws Exception {
		long t0 = System.currentTimeMillis();
		super.printCaption("testLogic");

		this.test(" 1 = 1 ");
		this.test(" 1 == 1 ");
		this.test(" 31 < 1 ");
		this.test(" 'aaa' = 'aaa' ");
		this.test(" 'aaa' < 'b' ");

		/*
		 * for (int i = 0; i < 100000; i++) { String exp = (Math.random() * 10000) + ">"
		 * + (Math.random() * 10000); test(exp); }
		 */
		super.printCaption("testLogic " + (System.currentTimeMillis() - t0));
	}

	private void test(String exp) {
		boolean isOk = ULogic.runLogic(exp);
		System.out.println(isOk + " " + exp);
	}

}
