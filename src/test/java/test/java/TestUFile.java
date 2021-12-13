package test.java;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UPath;

public class TestUFile extends TestBase {

	public static void main(String[] a) {
		UPath.getRealPath();
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

		URL url = TestUFile.class.getResource("/resources/test.der");
		System.out.println(url);

		// windows format zip path
		String zip1 = UPath.getRealPath() + "/resources/win-format.zip";
		String txt1 = UFile.readZipText(zip1, "win-format/tools/node");
		System.out.println(txt1);

		byte[] buf = UFile.readZipBytes(zip1, "win-format/tools/node");
		System.out.println(new String(buf));

		List<String> al = UFile.getZipList(zip1);
		System.out.println(al);

		File f = new File(zip1);

		System.out.println("md5: " + UFile.md5(f));
		System.out.println("sha1: " + UFile.sha1(f));
		System.out.println("sha256: " + UFile.sha256(f));
		System.out.println("sha512: " + UFile.digestFile(f, "SHA-512"));
		
		// big file 500m
		File large = new File("D:/BaiduNetdiskDownload/WPS_Office_3.9.3(6359).dmg");
		if(large.exists()) {
			System.out.println("md5: " + UFile.md5(large));
			System.out.println("sha1: " + UFile.sha1(large));
			System.out.println("sha256: " + UFile.sha256(large));
			System.out.println("sha512: " + UFile.digestFile(large, "SHA-512"));
		}
	}

}
