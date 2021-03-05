package test.java;

import java.util.List;

import org.junit.Test;

import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UPath;

public class TestUFile extends TestBase {

	public static void main(String[] a) {
		TestUFile t = new TestUFile();
		try {
			t.testLogic();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testLogic() throws Exception {
		super.printCaption("读取zip文件信息");
		
		// windows format zip path
		String zip1 = UPath.getRealPath() + "/resources/win-format.zip";
		String txt1 = UFile.readZipText(zip1, "win-format/tools/node");
		System.out.println(txt1);

		byte[] buf = UFile.readZipBytes(zip1, "win-format/tools/node");
		System.out.println(new String(buf));

		List<String> al = UFile.getZipList(zip1);
		System.out.println(al);
	}

}
