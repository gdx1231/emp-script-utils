package test.java;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import com.gdxsoft.easyweb.utils.UFile;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UXml;

import javax.xml.parsers.ParserConfigurationException;

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

		assert json != null : "JSON should not be null";
		assert json.contains("\"name\"") : "JSON should contain 'name' field";
		assert json.contains("\"张三\"") : "JSON should contain '张三' value";
		assert json.contains("\"age\"") : "JSON should contain 'age' field";
		assert json.contains("25") : "JSON should contain age value 25";

		System.out.println("测试通过！");
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

		assert json != null : "JSON should not be null";
		assert json.contains("\n") : "Pretty JSON should contain newlines";

		System.out.println("测试通过！");
	}

	@Test
	public void testXml2JsonFromStringNull() {
		super.printCaption("测试XML字符串为null或空");

		String json1 = UXml.xml2Json((String) null);
		assert json1 == null : "Null input should return null";

		String json2 = UXml.xml2Json("");
		assert json2 == null : "Empty string should return null";

		String json3 = UXml.xml2Json("   ");
		assert json3 == null : "Whitespace string should return null";

		System.out.println("空值测试通过！");
	}

	@Test
	public void testXml2JsonFromFile() throws IOException, ParserConfigurationException, SAXException {
		super.printCaption("测试XML文件转JSON");

		// 创建临时测试XML文件
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

		// 写入临时文件
		UFile.createNewTextFile(tempXmlPath, testXmlContent);

		// 测试从文件转换
		File xmlFile = new File(tempXmlPath);
		String json = UXml.xml2Json(xmlFile);
		System.out.println("从文件转换的JSON:");
		System.out.println(json);

		assert json != null : "JSON should not be null";
		assert json.contains("\"host\"") : "JSON should contain 'host' field";
		assert json.contains("\"localhost\"") : "JSON should contain 'localhost' value";
		assert json.contains("\"port\"") : "JSON should contain 'port' field";
		assert json.contains("3306") : "JSON should contain port value 3306";

		// 清理临时文件
		xmlFile.delete();

		System.out.println("测试通过！");
	}

	@Test
	public void testXml2JsonFromFilePretty() throws IOException, ParserConfigurationException, SAXException {
		super.printCaption("测试XML文件转JSON（格式化输出）");

		// 创建临时测试XML文件
		String testXmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<config>\n" +
				"  <app>\n" +
				"    <name>MyApp</name>\n" +
				"    <version>1.0.0</version>\n" +
				"  </app>\n" +
				"</config>";

		String tempDir = System.getProperty("java.io.tmpdir");
		String tempXmlPath = tempDir + File.separator + "test_xml2json_pretty.xml";

		// 写入临时文件
		UFile.createNewTextFile(tempXmlPath, testXmlContent);

		// 测试从文件转换（格式化）
		File xmlFile = new File(tempXmlPath);
		String json = UXml.xml2Json(xmlFile, 4);
		System.out.println("从文件转换的格式化JSON:");
		System.out.println(json);

		assert json != null : "JSON should not be null";
		assert json.contains("\"name\"") : "JSON should contain 'name' field";
		assert json.contains("\"MyApp\"") : "JSON should contain 'MyApp' value";

		// 清理临时文件
		xmlFile.delete();

		System.out.println("测试通过！");
	}

	@Test
	public void testXml2JsonFromPath() throws IOException, ParserConfigurationException, SAXException {
		super.printCaption("测试XML路径转JSON");

		// 创建临时测试XML文件
		String testXmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<data>\n" +
				"  <item id=\"1\">Item1</item>\n" +
				"  <item id=\"2\">Item2</item>\n" +
				"</data>";

		String tempDir = System.getProperty("java.io.tmpdir");
		String tempXmlPath = tempDir + File.separator + "test_xml2json_path.xml";

		// 写入临时文件
		UFile.createNewTextFile(tempXmlPath, testXmlContent);

		// 测试从路径转换（绝对路径）
		String json = UXml.xml2Json(tempXmlPath, true);
		System.out.println("从路径转换的JSON:");
		System.out.println(json);

		assert json != null : "JSON should not be null";
		assert json.contains("\"item\"") : "JSON should contain 'item' field";

		// 测试格式化版本
		String jsonPretty = UXml.xml2Json(tempXmlPath, true, 2);
		System.out.println("从路径转换的格式化JSON:");
		System.out.println(jsonPretty);

		// 清理临时文件
		new File(tempXmlPath).delete();

		System.out.println("测试通过！");
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

		assert json != null : "JSON should not be null";
		assert json.contains("\"id\"") : "JSON should contain 'id' attribute";
		assert json.contains("100") : "JSON should contain id value 100";
		assert json.contains("\"status\"") : "JSON should contain 'status' attribute";
		assert json.contains("\"active\"") : "JSON should contain status value 'active'";

		System.out.println("测试通过！");
	}

	@Test
	public void testXml2JsonSimpleElement() {
		super.printCaption("测试简单XML元素转JSON");

		String xmlString = "<message>Hello World</message>";

		String json = UXml.xml2Json(xmlString);
		System.out.println("简单元素JSON输出:");
		System.out.println(json);

		assert json != null : "JSON should not be null";
		assert json.contains("\"message\"") : "JSON should contain 'message' field";
		assert json.contains("\"Hello World\"") : "JSON should contain 'Hello World' value";

		System.out.println("测试通过！");
	}

	@Test
	public void testXml2JsonFromResourceEwaDefine() throws IOException, ParserConfigurationException, SAXException {
		super.printCaption("测试resources/xmls/EwaDefine.xml转JSON");

		// 获取资源文件路径 - 使用 test-classes 目录
		String resourcePath = UPath.getRealPath().replace("classes/", "test-classes/") + "resources/xmls/EwaDefine.xml";
		File xmlFile = new File(resourcePath);

		if (!xmlFile.exists()) {
			System.out.println("资源文件不存在，跳过测试: " + resourcePath);
			return;
		}

		String json = UXml.xml2Json(xmlFile, 2);
		System.out.println("EwaDefine.xml 转换的JSON（部分）:");
		// 只打印前500字符，避免输出过长
		if (json != null && json.length() > 500) {
			System.out.println(json.substring(0, 500) + "...");
		} else {
			System.out.println(json);
		}

		assert json != null : "JSON should not be null";
		assert json.contains("\"EwaDefine\"") : "JSON should contain 'EwaDefine' root element";
		assert json.contains("\"Steps\"") : "JSON should contain 'Steps' element";
		assert json.contains("\"Frame\"") : "JSON should contain 'Frame' element";

		System.out.println("测试通过！");
	}

	@Test
	public void testXml2JsonFromResourceM() throws IOException, ParserConfigurationException, SAXException {
		super.printCaption("测试resources/xmls/m.xml转JSON");

		// 获取资源文件路径 - 使用 test-classes 目录
		String resourcePath = UPath.getRealPath().replace("classes/", "test-classes/") + "resources/xmls/m.xml";
		File xmlFile = new File(resourcePath);

		if (!xmlFile.exists()) {
			System.out.println("资源文件不存在，跳过测试: " + resourcePath);
			return;
		}

		String json = UXml.xml2Json(xmlFile, 2);
		System.out.println("m.xml 转换的JSON（部分）:");
		// 只打印前500字符，避免输出过长
		if (json != null && json.length() > 500) {
			System.out.println(json.substring(0, 500) + "...");
		} else {
			System.out.println(json);
		}

		assert json != null : "JSON should not be null";
		assert json.contains("\"EasyWebTemplates\"") : "JSON should contain 'EasyWebTemplates' root element";
		assert json.contains("\"EasyWebTemplate\"") : "JSON should contain 'EasyWebTemplate' element";

		System.out.println("测试通过！");
	}

	@Test
	public void testXml2JsonFromResourceAll() throws IOException, ParserConfigurationException, SAXException {
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
				assert json != null : "JSON for " + xmlFile.getName() + " should not be null";
				System.out.println("转换成功，JSON长度: " + json.length() + " 字符");
			} catch (Exception e) {
				System.err.println("转换失败: " + e.getMessage());
			}
		}

		System.out.println("\n所有XML文件测试完成！");
	}
}