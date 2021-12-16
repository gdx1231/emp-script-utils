package test.java;

import org.junit.jupiter.api.Test;
import com.gdxsoft.easyweb.utils.UAes;
import com.gdxsoft.easyweb.utils.UDes;
import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MListStr;

public class TestParameters extends TestBase {

	public static void main(String[] a) {
		TestParameters t = new TestParameters();
		try {
			t.testEncyrpt();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testEncyrpt() throws Exception {
		String s1 = " {CALL PR_EWA_HOR_STR (@a, @CO_UNID, @姓名, @姓名.hash)}";
		MListStr al = Utils.getParameters(s1, "@");

		al.getList().forEach(o -> {
			System.out.println(o);
		});

	}

}
