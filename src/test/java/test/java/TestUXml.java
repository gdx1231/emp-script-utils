package test.java;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

public class TestUXml extends TestBase {

	public static void main(String[] a) {
		UPath.getRealPath();
		TestUXml t = new TestUXml();
		try {
			t.testXml2JsonFromString();
			t.testXml2JsonFromStringPretty();
			t.testXml2JsonFromFile();
			t.testXml2JsonFromFilePretty();
			t.testXml2JsonFromResourceEwaDefine();
			t.testXml2JsonFromResourceM();
			t.testXml2JsonFromResourceAll();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testXml2JsonFromString() {
		super.printCaption("测试XML字符串转JSON");

		String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<root>" +
				"  <person id=\"1\">" +
				"    <name>张三</name>" +
				"    <age>25</age>" +
				"    <city>北京</city>" +
				"  </person>" +
				"  <person id=\"2\">" +
				"    <name>李四</name>" +
				"    <age>30</age>" +
				"    <city>上海</city>" +
				"  </person>" +
				"</root>";

		String json = UXml.xml2Json(xmlString);
		System.out.println("JSON输出:");
		System.out.println(json);

		assertNotNull(json, "JSON should not be null");
		assertTrue(json.contains("\"name\""), "JSON should contain 'name' field");
		assertTrue(json.contains("\"张三\""), "JSON should contain '张三' value");
		assertTrue(json.contains("\"age\""), "JSON should contain 'age' field");
		assertTrue(json.contains("25"), "JSON should contain age value 25");
	}

	@Test
	public void testXml2JsonFromStringPretty() {
		super.printCaption("测试XML字符串转JSON（格式化输出）");

		String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<root>" +
				"  <person id=\"1\">" +
				"    <name>张三</name>" +
				"    <age>25</age>" +
				"  </person>" +
				"</root>";

		String json = UXml.xml2Json(xmlString, 2);
		System.out.println("格式化JSON输出:");
		System.out.println(json);

		assertNotNull(json, "JSON should not be null");
		assertTrue(json.contains("\n"), "Pretty JSON should contain newlines");
	}

	@Test
	public void testXml2JsonFromStringNull() {
		super.printCaption("测试XML字符串为null或空");

		assertNull(UXml.xml2Json((String) null), "Null input should return null");
		assertNull(UXml.xml2Json(""), "Empty string should return null");
		assertNull(UXml.xml2Json("   "), "Whitespace string should return null");
	}

	@Test
	public void testXml2JsonFromFile() throws IOException {
		super.printCaption("测试XML文件转JSON");

		String testXmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<config>\n" +
				"  <database>\n" +
				"    <host>localhost</host>\n" +
				"    <port>3306</port>\n" +
				"    <name>testdb</name>\n" +
				"  </database>\n" +
				"  <settings>\n" +
				"    <timeout>30</timeout>\n" +
				"    <retry>3</retry>\n" +
				"  </settings>\n" +
				"</config>";

		String tempDir = System.getProperty("java.io.tmpdir");
		String tempXmlPath = tempDir + File.separator + "test_xml2json.xml";
		File xmlFile = new File(tempXmlPath);

		try {
			UFile.createNewTextFile(tempXmlPath, testXmlContent);

			String json = UXml.xml2Json(xmlFile);
			System.out.println("从文件转换的JSON:");
			System.out.println(json);

			assertNotNull(json, "JSON should not be null");
			assertTrue(json.contains("\"host\""), "JSON should contain 'host' field");
			assertTrue(json.contains("\"localhost\""), "JSON should contain 'localhost' value");
			assertTrue(json.contains("\"port\""), "JSON should contain 'port' field");
			assertTrue(json.contains("3306"), "JSON should contain port value 3306");
		} finally {
			if (xmlFile.exists()) {
				xmlFile.delete();
			}
		}
	}

	@Test
	public void testXml2JsonFromFilePretty() throws IOException {
		super.printCaption("测试XML文件转JSON（格式化输出）");

		String testXmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<config>\n" +
				"  <app>\n" +
				"    <name>MyApp</name>\n" +
				"    <version>1.0.0</version>\n" +
				"  </app>\n" +
				"</config>";

		String tempDir = System.getProperty("java.io.tmpdir");
		String tempXmlPath = tempDir + File.separator + "test_xml2json_pretty.xml";
		File xmlFile = new File(tempXmlPath);

		try {
			UFile.createNewTextFile(tempXmlPath, testXmlContent);

			String json = UXml.xml2Json(xmlFile, 4);
			System.out.println("从文件转换的格式化JSON:");
			System.out.println(json);

			assertNotNull(json, "JSON should not be null");
			assertTrue(json.contains("\"name\""), "JSON should contain 'name' field");
			assertTrue(json.contains("\"MyApp\""), "JSON should contain 'MyApp' value");
		} finally {
			if (xmlFile.exists()) {
				xmlFile.delete();
			}
		}
	}

	@Test
	public void testXml2JsonFromPath() throws IOException {
		super.printCaption("测试XML路径转JSON");

		String testXmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<data>\n" +
				"  <item id=\"1\">Item1</item>\n" +
				"  <item id=\"2\">Item2</item>\n" +
				"</data>";

		String tempDir = System.getProperty("java.io.tmpdir");
		String tempXmlPath = tempDir + File.separator + "test_xml2json_path.xml";
		File xmlFile = new File(tempXmlPath);

		try {
			UFile.createNewTextFile(tempXmlPath, testXmlContent);

			String json = UXml.xml2Json(tempXmlPath, true);
			System.out.println("从路径转换的JSON:");
			System.out.println(json);

			assertNotNull(json, "JSON should not be null");
			assertTrue(json.contains("\"item\""), "JSON should contain 'item' field");

			String jsonPretty = UXml.xml2Json(tempXmlPath, true, 2);
			System.out.println("从路径转换的格式化JSON:");
			System.out.println(jsonPretty);
			assertNotNull(jsonPretty, "Pretty JSON should not be null");
		} finally {
			if (xmlFile.exists()) {
				xmlFile.delete();
			}
		}
	}

	@Test
	public void testXml2JsonWithAttributes() {
		super.printCaption("测试带属性的XML转JSON");

		String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<root>" +
				"  <user id=\"100\" status=\"active\">" +
				"    <name>王五</name>" +
				"    <email>wangwu@example.com</email>" +
				"  </user>" +
				"</root>";

		String json = UXml.xml2Json(xmlString, 2);
		System.out.println("带属性的JSON输出:");
		System.out.println(json);

		assertNotNull(json, "JSON should not be null");
		assertTrue(json.contains("\"id\""), "JSON should contain 'id' attribute");
		assertTrue(json.contains("100"), "JSON should contain id value 100");
		assertTrue(json.contains("\"status\""), "JSON should contain 'status' attribute");
		assertTrue(json.contains("\"active\""), "JSON should contain status value 'active'");
	}

	@Test
	public void testXml2JsonSimpleElement() {
		super.printCaption("测试简单XML元素转JSON");

		String xmlString = "<message>Hello World</message>";

		String json = UXml.xml2Json(xmlString);
		System.out.println("简单元素JSON输出:");
		System.out.println(json);

		assertNotNull(json, "JSON should not be null");
		assertTrue(json.contains("\"message\""), "JSON should contain 'message' field");
		assertTrue(json.contains("\"Hello World\""), "JSON should contain 'Hello World' value");
	}

	@Test
	public void testXml2JsonFileNull() {
		super.printCaption("测试File为null");

		assertThrows(IOException.class, () -> {
			UXml.xml2Json((File) null);
		}, "Null file should throw IOException");
	}

	@Test
	public void testXml2JsonFileNotFound() {
		super.printCaption("测试文件不存在");

		File nonExistent = new File("/tmp/nonexistent_xml2json_test.xml");
		assertThrows(IOException.class, () -> {
			UXml.xml2Json(nonExistent);
		}, "Non-existent file should throw IOException");
	}

	@Test
	public void testXml2JsonPathNull() {
		super.printCaption("测试路径为null");

		assertThrows(IllegalArgumentException.class, () -> {
			UXml.xml2Json(null, false);
		}, "Null path should throw IllegalArgumentException");

		assertThrows(IllegalArgumentException.class, () -> {
			UXml.xml2Json("", true);
		}, "Empty path should throw IllegalArgumentException");
	}

	@Test
	public void testXml2JsonPathTraversal() {
		super.printCaption("测试路径遍历防护");

		assertThrows(IllegalArgumentException.class, () -> {
			UXml.xml2Json("../../etc/passwd", false);
		}, "Path traversal should throw IllegalArgumentException");
	}

	@Test
	public void testXml2JsonFromResourceEwaDefine() throws IOException {
		super.printCaption("测试resources/xmls/EwaDefine.xml转JSON");

		String resourcePath = UPath.getRealPath().replace("classes/", "test-classes/") + "resources/xmls/EwaDefine.xml";
		File xmlFile = new File(resourcePath);

		if (!xmlFile.exists()) {
			System.out.println("资源文件不存在，跳过测试: " + resourcePath);
			return;
		}

		String json = UXml.xml2Json(xmlFile, 2);
		System.out.println("EwaDefine.xml 转换的JSON（部分）:");
		if (json != null && json.length() > 500) {
			System.out.println(json.substring(0, 500) + "...");
		} else {
			System.out.println(json);
		}

		assertNotNull(json, "JSON should not be null");
		assertTrue(json.contains("\"EwaDefine\""), "JSON should contain 'EwaDefine' root element");
		assertTrue(json.contains("\"Steps\""), "JSON should contain 'Steps' element");
		assertTrue(json.contains("\"Frame\""), "JSON should contain 'Frame' element");
	}

	@Test
	public void testXml2JsonFromResourceM() throws IOException {
		super.printCaption("测试resources/xmls/m.xml转JSON");

		String resourcePath = UPath.getRealPath().replace("classes/", "test-classes/") + "resources/xmls/m.xml";
		File xmlFile = new File(resourcePath);

		if (!xmlFile.exists()) {
			System.out.println("资源文件不存在，跳过测试: " + resourcePath);
			return;
		}

		String json = UXml.xml2Json(xmlFile, 2);
		System.out.println("m.xml 转换的JSON（部分）:");
		if (json != null && json.length() > 500) {
			System.out.println(json.substring(0, 500) + "...");
		} else {
			System.out.println(json);
		}

		assertNotNull(json, "JSON should not be null");
		assertTrue(json.contains("\"EasyWebTemplates\""), "JSON should contain 'EasyWebTemplates' root element");
		assertTrue(json.contains("\"EasyWebTemplate\""), "JSON should contain 'EasyWebTemplate' element");
	}

	@Test
	public void testXml2JsonFromResourceAll() throws IOException {
		super.printCaption("测试resources/xmls目录下所有XML文件转JSON");

		String xmlsDir = UPath.getRealPath().replace("classes/", "test-classes/") + "resources/xmls/";
		File dir = new File(xmlsDir);

		if (!dir.exists() || !dir.isDirectory()) {
			System.out.println("目录不存在，跳过测试: " + xmlsDir);
			return;
		}

		File[] xmlFiles = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".xml"));
		if (xmlFiles == null || xmlFiles.length == 0) {
			System.out.println("未找到XML文件");
			return;
		}

		System.out.println("找到 " + xmlFiles.length + " 个XML文件:");
		for (File xmlFile : xmlFiles) {
			System.out.println("\n--- 处理文件: " + xmlFile.getName() + " ---");
			try {
				String json = UXml.xml2Json(xmlFile);
				assertNotNull(json, "JSON for " + xmlFile.getName() + " should not be null");
				System.out.println("转换成功，JSON长度: " + json.length() + " 字符");
			} catch (Exception e) {
				System.err.println("转换失败: " + e.getMessage());
			}
		}

		System.out.println("\n所有XML文件测试完成！");
	}
}
