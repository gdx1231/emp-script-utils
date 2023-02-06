package test.java;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UPath;

public class TestZipCompress extends TestBase {

	public static void main(String[] a) {
		UPath.getRealPath();
		TestZipCompress t = new TestZipCompress();
		try {
			t.testLogic();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testLogic() throws Exception {

		// windows format zip path
		String zip1 ="E:/Guolei/com.gdxsoft/emp-script-utils/src/test/resources/resources/docx1";
		
		UFile.zipPaths( zip1, "d:/test/test.docx");
	}

}
