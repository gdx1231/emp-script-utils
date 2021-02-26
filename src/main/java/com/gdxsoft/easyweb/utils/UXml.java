package com.gdxsoft.easyweb.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class UXml {
	/**
	 * 过滤非法的字符 0x00 - 0x08 ,0x0b - 0x0c ,0x0e - 0x1f
	 * 
	 * @param xmlStr
	 * @return 过滤后的xml
	 */
	public static String filterInvalidXMLcharacter(String xmlStr) {
		if (xmlStr == null || xmlStr.length() == 0) {
			return xmlStr;
		}
		StringBuilder sb = new StringBuilder();
		char[] chs = xmlStr.toCharArray();
		for (char ch : chs) {
			if ((ch >= 0x00 && ch <= 0x08) || (ch >= 0x0b && ch <= 0x0c) || (ch >= 0x0e && ch <= 0x1f)) {
				// eat...
			} else {
				sb.append(ch);
			}
		}
		return sb.toString();
	}

	/**
	 * 从Xml中查找某个节点
	 * 
	 * @param fromNode
	 * @param findTag      要查找的TagName
	 * @param findAttr     检查的属性名称
	 * @param checkValue   检测值
	 * @param isIgnoreCase 是否大小写匹配
	 * @return 节点
	 */
	public static Element findNode(Element fromNode, String findTag, String findAttr, String checkValue,
			boolean isIgnoreCase) {
		NodeList nl = fromNode.getElementsByTagName(findTag);

		for (int i = 0; i < nl.getLength(); i++) {
			Element ele = (Element) nl.item(i);
			String v = ele.getAttribute(findAttr);
			if (isIgnoreCase) {
				if (v.equalsIgnoreCase(checkValue)) {
					return ele;
				}
			} else {
				if (v.equals(checkValue)) {
					return ele;
				}
			}
		}
		return null;
	}

	/**
	 * 生成可Xml节点值
	 * 
	 * @param s1
	 * @return 节点值
	 */
	public static String createXmlValue(String s1) {
		if (s1 == null)
			return s1;
		s1 = s1.replace("\r", "&#xD");
		s1 = s1.replace("\n", "&#xA");
		s1 = s1.replace("&", "&amp;");
		s1 = s1.replace("<", "&lt;");
		s1 = s1.replace(">", "&gt;");
		s1 = s1.replace("\"", "&quot;");
		return s1;
	}

	/**
	 * 读取文件，返回 Document
	 * 
	 * @param xmlPath        xml文件路径
	 * @param isAbsolutePath 是否为绝对路径
	 * @return document 对象
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Document retDocument(String xmlPath, boolean isAbsolutePath)
			throws ParserConfigurationException, SAXException, IOException {
		String p1 = xmlPath;
		if (!isAbsolutePath) {
			p1 = UPath.getScriptPath() + "/" + xmlPath;
		}
		return retDocument(p1);
	}

	/**
	 * 读取文件，返回 Document
	 * 
	 * @param xmlPath xml文件路径
	 * @return document 对象
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public static Document retDocument(String xmlPath) throws ParserConfigurationException, SAXException, IOException {
		File f = new File(xmlPath);
		Document doc = null;
		if (!f.exists()) {
			File fencode = new File(xmlPath + ".bin");
			if (!fencode.exists()) {
				throw new IOException("File not found!(" + f.getAbsolutePath());
			}
			byte[] encoderContent = null;
			UAes des = null;
			try {
				des = UAes.getInstance();
			} catch (Exception e1) {
				throw new IOException("穿件解码出错!(" + e1.getMessage());
			}
			try {
				encoderContent = UFile.readFileBytes(fencode.getAbsolutePath());
			} catch (Exception e) {
				throw new IOException("读取文件出错!(" + fencode.getAbsolutePath());
			}

			String xml;
			try {
				xml = des.decrypt(encoderContent).trim();
			} catch (Exception e) {
				throw new IOException("解码出错!(" + fencode.getAbsolutePath());
			}

			xml = UXml.filterInvalidXMLcharacter(xml);
			doc = asDocument(xml);
		} else {
			URI u = f.toURI();
			String uri = u.toASCIIString();
			DocumentBuilderFactory factory = getDocumentBuilder();
			DocumentBuilder builder = factory.newDocumentBuilder();
			doc = builder.parse(uri);
		}
		return doc;
	}

	/**
	 * save xml document to file
	 * 
	 * @param document    Document对象
	 * @param xmlFileName 文件名
	 * @return 是否保存成功
	 */
	public static boolean saveDocument(Document document, String xmlFileName) {
		String xml = UXml.asXmlAll(document);
		try {
			UFile.createNewTextFile(xmlFileName, xml);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * 将 Document对象保存为文件
	 * 
	 * @param document Document对象
	 * @return 是否保存成功
	 */
	public static boolean saveDocument(Document document) {
		String xmlFileName = document.getDocumentURI().replace("file:///", "");
		return saveDocument(document, xmlFileName);
	}

	/**
	 * 返回属性值
	 * 
	 * @param node    当前节点
	 * @param attName 属性值
	 * @return 属性值
	 */
	public static String retNodeValue(Node node, String attName) {
		if (node == null) {
			return "";
		}
		if (node.getAttributes().getNamedItem(attName) == null) {
			return "";
		}
		return node.getAttributes().getNamedItem(attName).getNodeValue();
	}

	/**
	 * 返回节点text
	 * 
	 * @param node 当前节点
	 * @return TextContent
	 */
	public static String retNodeText(Node node) {
		if (node == null) {
			return null;
		}
		return node.getTextContent();
	}

	/**
	 * 返回下级节点集合
	 * 
	 * @param document document对象
	 * @param tagName  下级节点表达式，例如 aa/bb/cc，返回 aa>bb>cc
	 * @return 集合
	 */
	public static NodeList retNodeList(Document document, String tagName) {
		return retNodeListByPath(document, tagName);
	}

	/**
	 * 返回下级节点集合
	 * 
	 * @param node    node 对象
	 * @param tagName 下级节点表达式，例如 aa/bb/cc，返回 aa>bb>cc
	 * @return 集合
	 */
	public static NodeList retNodeList(Node node, String tagName) {
		return retNodeListByPath(node, tagName);
	}

	/**
	 * 返回下级节点集合
	 * 
	 * @param element element 对象
	 * @param tagName 下级节点表达式，例如 aa/bb/cc，返回 aa>bb>cc
	 * @return 集合
	 */
	public static NodeList retNodeList(Element element, String tagName) {
		return retNodeListByPath(element, tagName);
	}

	/**
	 * 返回下级节点
	 * 
	 * @param node    node对象
	 * @param tagName 下级节点表达式，例如 aa/bb/cc，返回 aa>bb>cc
	 * @return 下级节点
	 */
	public static Node retNode(Node node, String tagName) {
		return retNode((Element) node, tagName);
	}

	/**
	 * 返回下级节点
	 * 
	 * @param document
	 * @param tagName  下级节点表达式，例如 aa/bb/cc，返回 aa>bb>cc
	 * @return 下级节点
	 */
	public static Node retNode(Document document, String tagName) {
		if (document == null)
			return null;
		return retNode(document.getDocumentElement(), tagName);
	}

	/**
	 * 返回下级节点
	 * 
	 * @param element
	 * @param tagName 下级节点表达式，例如 aa/bb/cc，返回 aa>bb>cc
	 * @return 下级节点
	 */
	public static Node retNode(Element element, String tagName) {
		NodeList nl = retNodeList(element, tagName);
		if (nl == null) {
			return null;
		}
		return nl.item(0);
	}

	/**
	 * 返回节点列表通过路径
	 * 
	 * @param element element 元素
	 * @param tagPath 下级节点表达式，例如 aa/bb/cc，返回 aa>bb>cc
	 * @return 列表
	 */
	public static NodeList retNodeListByPath(Element element, String tagPath) {
		if (tagPath == null || element == null)
			return null;
		String[] s1 = tagPath.split("/");
		Element ele = element;
		for (int i = 0; i < s1.length - 1; i++) {
			Node node = retNode(ele, s1[i]);
			if (node == null)
				return null;
			ele = (Element) node;
		}
		return ele.getElementsByTagName(s1[s1.length - 1]);
	}

	/**
	 * 返回节点列表通过路径
	 * 
	 * @param document document对象
	 * @param tagPath  下级节点表达式，例如 aa/bb/cc，返回 aa>bb>cc
	 * @return 列表
	 */
	public static NodeList retNodeListByPath(Document document, String tagPath) {
		if (document == null)
			return null;
		String[] a = tagPath.split("/");
		if (a.length == 1) {
			if (document.getFirstChild().getNodeName().equals(a[0])) {
				return document.getChildNodes();
			} else {
				return null;
			}
		}
		Element ele = document.getDocumentElement();

		String s2 = "";
		for (int i = 1; i < a.length; i++) {
			if (i == 1) {
				s2 = a[1];
			} else {
				s2 = s2 + "/" + a[i];
			}
		}
		return retNodeListByPath(ele, s2);
	}

	/**
	 * 返回节点列表通过路径
	 * 
	 * @param node    父节点
	 * @param tagPath 下级节点表达式，例如 aa/bb/cc，返回 aa>bb>cc
	 * @return 节点列表
	 */
	public static NodeList retNodeListByPath(Node node, String tagPath) {
		return retNodeListByPath((Element) node, tagPath);
	}

	/**
	 * 增加节点
	 * 
	 * @param document
	 * @param newChilid
	 * @param tagPathParent
	 * @return 是否成功
	 */
	public static boolean addNode(Document document, Element newChilid, String tagPathParent) {
		Node parentNode = retNodeListByPath(document, tagPathParent).item(0);
		if (parentNode == null)
			return false;
		parentNode.appendChild(newChilid);
		return true;
	}

	/**
	 * 删除节点
	 * 
	 * @param document
	 * @param tagPath
	 * @param nodeAttribute
	 * @param attValue
	 * @return 是否成功
	 */
	public static boolean removeNode(Document document, String tagPath, String nodeAttribute, String attValue) {
		NodeList nodes = retNodeListByPath(document, tagPath);
		if (nodes == null)
			return false;
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			String v1 = retNodeValue(node, nodeAttribute);
			if (v1.equals(attValue)) {
				node.getParentNode().removeChild(node);
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取配置文件的节点
	 * 
	 * @param xmlName  文件路径
	 * @param itemName 节点名称
	 * @param tagPath  路径
	 * @return 节点
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static Node queryNode(String xmlName, String itemName, String tagPath)
			throws ParserConfigurationException, SAXException, IOException {
		Document doc = retDocument(xmlName);
		return queryNode(doc, "Name", itemName, tagPath);
	}

	/**
	 * 获取配置文件的节点
	 * 
	 * @param document xml文档
	 * @param itemName 节点名称
	 * @param tagPath  路径
	 * @return 节点
	 */
	public static Node queryNode(Document document, String itemName, String tagPath) {

		return queryNode(document, "Name", itemName, tagPath);
	}

	/**
	 * 获取配置文件的节点
	 * 
	 * @param document      xml文档
	 * @param attributeName 属性名
	 * @param itemName      节点名称
	 * @param tagPath       路径
	 * @return 节点
	 */
	public static Node queryNode(Document document, String attributeName, String itemName, String tagPath) {
		NodeList nl = retNodeListByPath(document, tagPath);
		if (nl == null)
			return null;
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = (Node) nl.item(i);
			String name = retNodeValue(node, attributeName);
			if (name == null)
				continue;
			if (name.toUpperCase().trim().equals(itemName.toUpperCase().trim())) {
				return node;
			}
		}
		return null;
	}

	/**
	 * 返回Xml字符，不包含 &lt;?xml version="1.0" encoding="UTF-8"?&gt;
	 * 
	 * @param node 节点
	 * @return Xml字符串
	 */
	public static String asXml(Node node) {
		String xml = asXmlAll(node);
		if (xml != null) {
			int m0 = xml.indexOf("<?xml");
			int m1 = xml.indexOf("?>", m0);
			if (m1 > m0 && m0 >= 0) {
				xml = xml.substring(m1 + 2);
			}
		}
		return xml;
	}

	/**
	 * 美化Xml输出
	 * 
	 * @param node 节点
	 * @return Xml字符串
	 */
	public static String asXmlPretty(Node node) {
		TransformerFactory tf = TransformerFactory.newInstance();
		tf.setAttribute("indent-number", new Integer(2));

		// (2)
		Transformer t;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		String s1 = "";
		try {
			t = tf.newTransformer();
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.transform(new DOMSource(node), new StreamResult(new OutputStreamWriter(out, "utf-8")));
			s1 = out.toString("utf-8");
			return s1;
		} catch (TransformerConfigurationException e) {
			return e.getMessage();
		} catch (UnsupportedEncodingException e) {
			return e.getMessage();
		} catch (TransformerException e) {
			return e.getMessage();
		}

	}

	/**
	 * 返回 Xml字符串 ，包含 <?xml version="1.0" encoding="UTF-8"?>
	 * 
	 * @param node
	 * @return Xml字符串
	 */
	public static String asXmlAll(Node node) {
		TransformerFactory l_transformFactory = TransformerFactory.newInstance();
		Transformer l_transformer = null;
		ByteArrayOutputStream l_byteOutStream = new ByteArrayOutputStream();
		String s1 = "";
		try {
			l_transformer = l_transformFactory.newTransformer();
			l_transformer.transform(new DOMSource(node), new StreamResult(l_byteOutStream));
			s1 = l_byteOutStream.toString("utf-8");
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return null;
		} finally {
			try {
				l_byteOutStream.close();
			} catch (IOException e) {
			}
		}
		return s1;
	}

	/**
	 * 将xml字符串转换为 node
	 * 
	 * @param xmlSource
	 * @return node
	 */
	public static Node asNode(String xmlSource) {
		Document doc = asDocument(xmlSource);
		if (doc == null)
			return null;
		return doc.getFirstChild();
	}

	/**
	 * 获取安全的 DocumentBuilderFactory，避免XXE攻击
	 * 
	 * @return DocumentBuilderFactory
	 */
	public static DocumentBuilderFactory getDocumentBuilder() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// 避免 XXE 注入攻击 郭磊 2018-07-05
		factory.setExpandEntityReferences(false);

		return factory;
	}

	/**
	 * 从xml字符串中返回XmlDocument对象
	 * 
	 * @param xmlSource xml字符串
	 * @return Document
	 */
	public static Document asDocument(String xmlSource) {
		DocumentBuilderFactory factory = getDocumentBuilder();

		DocumentBuilder builder;
		StringReader sr = new StringReader(xmlSource);
		InputSource is = new InputSource(sr);
		is.setEncoding("utf-8");
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(is);
			return doc;
		} catch (ParserConfigurationException e) {
			System.out.println(e.getMessage());
			return null;
		} catch (SAXException e) {
			System.out.println(e.getMessage());
			return null;
		} catch (IOException e) {
			System.out.println(e.getMessage());
			return null;
		} finally {
			sr.close();
		}
	}

	/**
	 * 创建并返回空白的xml文档
	 * 
	 * @return Document
	 */
	public static Document createBlankDocument() {
		DocumentBuilderFactory factory = getDocumentBuilder();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			return doc;
		} catch (ParserConfigurationException e) {
			System.err.println(e.toString());
			return null;
		}
	}

	/**
	 * 生成空xml文档，并保存为文件
	 * 
	 * @param path xml路径
	 * @return xml文档
	 */
	public static Document createSavedDocument(String path, String rootTagName) {
		String s1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
		s1 += "<" + rootTagName + "></" + rootTagName + ">";
		try {
			UFile.createNewTextFile(path, s1);
			Document d = retDocument(path);
			return d;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			return null;
		}
	}

	/**
	 * 生成空文件
	 * 
	 * @param dtd   dtd文件
	 * @param qName qName
	 * @return Document
	 */
	public static Document createBlankDocument(String dtd, String qName) {
		DocumentBuilderFactory factory = getDocumentBuilder();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			DOMImplementation impl = builder.getDOMImplementation();
			DocumentType type = impl.createDocumentType(qName, "SYSTEM", dtd);
			Document doc = impl.createDocument(qName, qName, type);
			return doc;
		} catch (ParserConfigurationException e) {
			System.err.println(e.toString());
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * 在Xml文档中增加新的节点
	 * 
	 * @param sourceDocument 源文档
	 * @param nodeXmlString  node的Xml字符
	 * @param tagPath        增加的路径
	 * @return 新文档
	 */
	public static Document appendNode(Document sourceDocument, String nodeXmlString, String tagPath) {

		String xml = asXml(sourceDocument.getDocumentElement());
		String[] a1 = tagPath.split("/");

		int m = 0;
		for (int i = 0; i < a1.length; i++) {
			if (m < 0) {
				return null;
			}
			m = xml.indexOf("<" + a1[i], m);
			int m1 = xml.indexOf("</" + a1[i] + ">", m);
			if (m1 < 0) {
				m1 = xml.indexOf("/>", m);
				if (m1 > 0) {
					xml = xml.substring(0, m1) + "></" + a1[i] + ">" + xml.substring(m1 + 2);
				} else {
					return null;
				}
				m = xml.indexOf("</" + a1[i] + ">", m);
			} else {
				m = m1;
			}
		}
		// 不要用字符串，可能会内存溢出
		StringWriter sw = new StringWriter();
		sw.write(xml.substring(0, m));
		sw.write(nodeXmlString);
		sw.write(xml.substring(m));

		Node n1 = asNode(sw.toString());
		try {
			sw.close();
		} catch (Exception e) {
			System.err.println(e.toString());
		}
		sw = null;
		if (n1 == null)
			return null;
		Document newDoc = n1.getOwnerDocument();
		return newDoc;
	}

}
