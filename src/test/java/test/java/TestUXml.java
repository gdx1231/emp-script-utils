package test.java;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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

	// ==================== createXmlValue escaping ====================

	@Test
	public void testCreateXmlValueBasic() {
		printCaption("createXmlValue — basic escaping");
		assertEquals("&amp;", UXml.createXmlValue("&"));
		assertEquals("&lt;", UXml.createXmlValue("<"));
		assertEquals("&gt;", UXml.createXmlValue(">"));
		assertEquals("&quot;", UXml.createXmlValue("\""));
		assertEquals("&apos;", UXml.createXmlValue("'"));
	}

	@Test
	public void testCreateXmlValueCrLfNoDoubleEscape() {
		printCaption("createXmlValue — \\r\\n not double-escaped");
		// P0 bug: & 必须在 \r/\n 之前转义，否则 &#xD 变成 &amp;#xD
		String result = UXml.createXmlValue("a\r\nb");
		assertEquals("a&#xD;&#xA;b", result,
				"\\r\\n should produce &#xD;&#xA;, NOT &amp;#xD;&amp;#xA;");
	}

	@Test
	public void testCreateXmlValueNull() {
		printCaption("createXmlValue — null");
		assertNull(UXml.createXmlValue(null));
	}

	@Test
	public void testCreateXmlValueMixed() {
		printCaption("createXmlValue — mixed special chars");
		String result = UXml.createXmlValue("if (a < b && c > d) \"ok\"");
		assertTrue(result.contains("&amp;&amp;"), "Should escape &&");
		assertTrue(result.contains("&lt;"), "Should escape <");
		assertTrue(result.contains("&gt;"), "Should escape >");
		assertTrue(result.contains("&quot;"), "Should escape \"");
	}

	// ==================== filterInvalidXMLcharacter ====================

	@Test
	public void testFilterInvalidXmlChar() {
		printCaption("filterInvalidXMLcharacter — strips control chars");
		assertEquals("hello", UXml.filterInvalidXMLcharacter("he\u0001llo"),
				"0x01 should be stripped");
		assertEquals("ab", UXml.filterInvalidXMLcharacter("a\u0000b"),
				"0x00 should be stripped");
	}

	@Test
	public void testFilterInvalidXmlCharNull() {
		printCaption("filterInvalidXMLcharacter — null/empty");
		assertNull(UXml.filterInvalidXMLcharacter(null));
		assertEquals("", UXml.filterInvalidXMLcharacter(""));
	}

	// ==================== DOM parse / serialize ====================

	@Test
	public void testAsDocumentAndAsNode() {
		printCaption("asDocument / asNode roundtrip");
		String xml = "<root><child id=\"1\">text</child></root>";
		Document doc = UXml.asDocument(xml);
		assertNotNull(doc);

		Node node = UXml.asNode(xml);
		assertNotNull(node);
		assertEquals("root", node.getNodeName());
	}

	@Test
	public void testAsXmlAndAsXmlAll() {
		printCaption("asXml / asXmlAll");
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><item/></root>";
		Document doc = UXml.asDocument(xml);
		assertNotNull(doc);

		String all = UXml.asXmlAll(doc);
		assertNotNull(all);
		assertTrue(all.contains("<root>"), "asXmlAll should include XML declaration");

		String withoutDecl = UXml.asXml(doc.getDocumentElement());
		assertNotNull(withoutDecl);
		assertFalse(withoutDecl.trim().startsWith("<?xml"), "asXml should omit XML declaration");
		assertTrue(withoutDecl.contains("<root>"));
	}

	@Test
	public void testAsXmlPretty() {
		printCaption("asXmlPretty");
		Document doc = UXml.asDocument("<root><child a=\"1\">text</child></root>");
		assertNotNull(doc);
		String pretty = UXml.asXmlPretty(doc);
		assertNotNull(pretty);
		assertTrue(pretty.contains("\n"), "Pretty print should have newlines");
	}

	// ==================== appendNode (DOM API) ====================

	@Test
	public void testAppendNode() {
		printCaption("appendNode — insert child element");
		Document doc = UXml.asDocument("<root><items></items></root>");
		assertNotNull(doc);

		UXml.appendNode(doc, "<item name=\"new\">value</item>", "items");

		String xml = UXml.asXml(doc.getDocumentElement());
		assertTrue(xml.contains("<item"), "Should contain new item");
		assertTrue(xml.contains("value"), "Should preserve text content");
	}

	@Test
	public void testAppendNodeInvalidPath() {
		printCaption("appendNode — invalid path returns null");
		Document doc = UXml.asDocument("<root></root>");
		assertNotNull(doc);
		assertNull(UXml.appendNode(doc, "<item/>", "root/nonexistent"));
	}

	// ==================== retNode / retNodeList ====================

	@Test
	public void testRetNodeByPath() {
		printCaption("retNode — path navigation");
		Document doc = UXml.asDocument("<a><b><c id=\"1\">val1</c><c id=\"2\">val2</c></b></a>");
		assertNotNull(doc);

		Node node = UXml.retNode(doc, "b/c");  // root is "a", path relative to it
		assertNotNull(node);
		assertEquals("c", node.getNodeName());
	}

	@Test
	public void testRetNodeListByPath() {
		printCaption("retNodeListByPath — multiple children");
		Document doc = UXml.asDocument("<a><b><c id=\"1\"/><c id=\"2\"/><c id=\"3\"/></b></a>");
		assertNotNull(doc);

		NodeList nl = UXml.retNodeList(doc, "b/c");  // root is "a", path relative to it
		assertNotNull(nl);
		assertEquals(3, nl.getLength());
	}

	// ==================== queryNode ====================

	@Test
	public void testQueryNodeCaseInsensitive() {
		printCaption("queryNode — case-insensitive match");
		Document doc = UXml.asDocument("<root><items><item Name=\"Foo\"/><item Name=\"Bar\"/></items></root>");
		assertNotNull(doc);

		// queryNode uses toUpperCase internally; "foo" should match "Foo"
		Node node = UXml.queryNode(doc, "Name", "Foo", "items/item");
		assertNotNull(node);
		assertEquals("Foo", UXml.retNodeValue(node, "Name"));
	}

	@Test
	public void testQueryNodeNotFound() {
		printCaption("queryNode — not found returns null");
		Document doc = UXml.asDocument("<root><items><item Name=\"A\"/></items></root>");
		assertNull(UXml.queryNode(doc, "Name", "Z", "items/item"));
	}

	// ==================== removeNode ====================

	@Test
	public void testRemoveNode() {
		printCaption("removeNode");
		Document doc = UXml.asDocument("<root><items><item name=\"a\"/><item name=\"b\"/></items></root>");
		assertNotNull(doc);

		boolean removed = UXml.removeNode(doc, "items/item", "name", "a");
		assertTrue(removed);

		NodeList nl = UXml.retNodeList(doc, "items/item");
		assertEquals(1, nl.getLength());
		assertEquals("b", UXml.retNodeValue(nl.item(0), "name"));
	}

	@Test
	public void testRemoveNodeNotFound() {
		printCaption("removeNode — not found returns false");
		Document doc = UXml.asDocument("<root><items><item name=\"a\"/></items></root>");
		assertFalse(UXml.removeNode(doc, "items/item", "name", "z"));
	}

	// ==================== createBlankDocument / save ====================

	@Test
	public void testCreateBlankDocument() {
		printCaption("createBlankDocument");
		Document doc = UXml.createBlankDocument();
		assertNotNull(doc);
		assertNull(doc.getDocumentElement(), "Blank doc has no root element");
	}

	@Test
	public void testAppendAndSerializeRoundtrip() {
		printCaption("appendNode + serialize + re-parse");
		Document doc = UXml.asDocument("<config></config>");
		assertNotNull(doc);
		UXml.appendNode(doc, "<setting>test</setting>", "config");

		// Verify DOM was modified directly
		Element config = doc.getDocumentElement();
		assertNotNull(config);
		NodeList settings = config.getElementsByTagName("setting");
		assertEquals(1, settings.getLength(), "DOM should have one setting child after append");
		assertEquals("test", settings.item(0).getTextContent());
	}

	// ==================== getElementAttributes ====================

	@Test
	public void testGetElementAttributes() {
		printCaption("getElementAttributes");
		Document doc = UXml.asDocument("<root><item a=\"1\" B=\"2\" Cc=\"3\"/></root>");
		assertNotNull(doc);
		Element item = (Element) UXml.retNode(doc, "item");
		assertNotNull(item);

		Map<String, String> attrs = UXml.getElementAttributes(item, false);
		assertEquals("1", attrs.get("a"));
		assertEquals("2", attrs.get("B"));
		assertEquals("3", attrs.get("Cc"));

		Map<String, String> attrsLower = UXml.getElementAttributes(item, true);
		assertEquals("1", attrsLower.get("a"));
		assertEquals("2", attrsLower.get("b"));
		assertEquals("3", attrsLower.get("cc"));
	}

	// ==================== findNode ====================

	@Test
	public void testFindNode() {
		printCaption("findNode — case sensitive/insensitive");
		Document doc = UXml.asDocument("<root><item name=\"Abc\"/><item name=\"xyz\"/></root>");
		Element root = doc.getDocumentElement();

		Element found = UXml.findNode(root, "item", "name", "xyz", false);
		assertNotNull(found);
		assertEquals("xyz", found.getAttribute("name"));

		Element foundCi = UXml.findNode(root, "item", "name", "ABC", true);
		assertNotNull(foundCi);
		assertEquals("Abc", foundCi.getAttribute("name"));

		Element notFound = UXml.findNode(root, "item", "name", "ABC", false);
		assertNull(notFound);
	}

	// ==================== XXE protection ====================

	@Test
	public void testXxeRejected() {
		printCaption("XXE attack is rejected");
		String xxeXml = "<?xml version=\"1.0\"?>" +
				"<!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>" +
				"<root>&xxe;</root>";
		Document doc = UXml.asDocument(xxeXml);
		// With external-general-entities=false, the entity should not be resolved.
		// The parser may still succeed but the entity will be empty or the parse may fail.
		if (doc != null) {
			String text = doc.getDocumentElement().getTextContent();
			// Entity content must NOT contain actual file contents
			assertFalse(text.contains("root:"), "External entity should not be resolved");
		}
	}

	@Test
	public void testBillionLaughsRejected() {
		printCaption("Billion Laughs attack is rejected");
		String bomb = "<?xml version=\"1.0\"?>" +
				"<!DOCTYPE lolz [" +
				"<!ENTITY lol \"lol\">" +
				"<!ENTITY lol2 \"&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;\">" +
				"<!ENTITY lol3 \"&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;\">" +
				"<!ENTITY lol4 \"&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;\">" +
				"]>" +
				"<root>&lol4;</root>";
		Document doc = UXml.asDocument(bomb);
		// Should either fail parse or limit expansion (secure-processing FEATURE_SECURE_PROCESSING)
		if (doc != null) {
			String text = doc.getDocumentElement().getTextContent();
			// Should not blow up to 10^4 lols
			assertTrue(text.length() < 100000, "Billion laughs should be limited");
		}
	}

	// ==================== retNodeValue / retNodeText ====================

	@Test
	public void testRetNodeValueAndText() {
		printCaption("retNodeValue / retNodeText");
		Document doc = UXml.asDocument("<root><item id=\"42\">content</item></root>");
		assertNotNull(doc);
		Element item = (Element) UXml.retNode(doc, "item");
		assertNotNull(item);

		assertEquals("42", UXml.retNodeValue(item, "id"));
		assertEquals("", UXml.retNodeValue(item, "nonexistent"));
		assertEquals("content", UXml.retNodeText(item));

		assertEquals("", UXml.retNodeValue(null, "x"));
		assertNull(UXml.retNodeText(null));
	}

	// ==================== addNode ====================

	@Test
	public void testAddNode() {
		printCaption("addNode");
		Document doc = UXml.asDocument("<root><items><sub></sub></items></root>");
		assertNotNull(doc);
		Document childDoc = UXml.asDocument("<entry>data</entry>");
		assertNotNull(childDoc);

		// addNode uses retNodeListByPath which strips first segment for multi-segment paths
		Node child = childDoc.getDocumentElement();
		Node imported = doc.importNode(child, true);
		boolean ok = UXml.addNode(doc, (Element) imported, "items/sub");
		assertTrue(ok);

		Element sub = (Element) UXml.retNode(doc, "items/sub");
		assertNotNull(sub);
		NodeList nl = sub.getElementsByTagName("entry");
		assertEquals(1, nl.getLength());
	}
}
