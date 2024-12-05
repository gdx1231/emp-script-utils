package test.java;

import java.util.List;

import org.junit.jupiter.api.Test;
import com.gdxsoft.easyweb.utils.UPinYin;

public class TestPinYin extends TestBase {

	public static void main(String[] a) {
		TestPinYin t = new TestPinYin();
		try {
			t.testLogic();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testLogic() throws Exception {

		this.test("长沙市第一中学12班级");
		 
		this.test("了如指掌 和 乐器 乐不思蜀");
		this.test("麗人");
	}

	private void test(String exp) throws Exception {
		List<String> lst = UPinYin.convertWithoutTone(exp);
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < lst.size(); i++) {
			sb.append(lst.get(i)).append(" ");
		}
		System.out.println(exp + ":" +sb.toString());
	}

}
