package test.java;


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
		String zipSource = UPath.getRealPath() + "resources/docx1";
		String outFile = System.getProperty("java.io.tmpdir") + java.io.File.separator + "test_zip_output.docx";

		try {
			UFile.zipPaths(zipSource, outFile);

			java.io.File f = new java.io.File(outFile);
			org.junit.jupiter.api.Assertions.assertTrue(f.exists(), "Output zip file should exist");
			org.junit.jupiter.api.Assertions.assertTrue(f.length() > 0, "Output zip file should not be empty");
			System.out.println("ZIP output: " + outFile + " (" + f.length() + " bytes)");
		} finally {
			new java.io.File(outFile).delete();
		}
	}

}
