package test.java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.utils.UHtml;

public class TestUHtml extends TestBase {

	@Test
	public void testRemoveHtmlEvents() {
		printCaption("removeHtmlEvents");
		String html = "<div onclick=\"alert(1)\">click</div><span onmouseover=\"x()\">hover</span>";
		String result = UHtml.removeHtmlEvents(html);
		assertNotNull(result);
		assertFalse(result.contains("onclick"), "onclick should be removed");
		assertFalse(result.contains("onmouseover"), "onmouseover should be removed");
		assertTrue(result.contains("_gdx_"), "event name should be replaced with _gdx_");
	}

	@Test
	public void testRemoveHtmlEventsNoEvents() {
		printCaption("removeHtmlEvents — no events");
		String html = "<div>hello</div><p>world</p>";
		String result = UHtml.removeHtmlEvents(html);
		assertEquals(html, result);
	}

	@Test
	public void testRemoveHtmlEventsNullAndEmpty() {
		printCaption("removeHtmlEvents — null/empty");
		assertNull(UHtml.removeHtmlEvents(null));
		assertEquals("", UHtml.removeHtmlEvents(""));
	}

	@Test
	public void testRemoveHtmlAttributes() {
		printCaption("removeHtmlAttributes");
		String html = "<div class=\"a\" style=\"b\">text</div><p id=\"x\">para</p>";
		String result = UHtml.removeHtmlAttributes(html);
		assertNotNull(result);
		assertFalse(result.contains("class="), "class attribute should be removed");
		assertFalse(result.contains("style="), "style attribute should be removed");
		assertFalse(result.contains("id="), "id attribute should be removed");
		assertTrue(result.contains("<div"), "div tag should remain");
	}

	@Test
	public void testRemoveHtmlAttributesPreservesImg() {
		printCaption("removeHtmlAttributes — preserves img src");
		String html = "<img src=\"/img/logo.png\" class=\"logo\" alt=\"x\">";
		String result = UHtml.removeHtmlAttributes(html);
		assertTrue(result.contains("src="), "img src should be preserved");
	}

	@Test
	public void testRemoveHtmlComments() {
		printCaption("removeHtmlComments");
		String html = "<!-- header --><div>content</div><!-- footer -->";
		String result = UHtml.removeHtmlComments(html);
		assertNotNull(result);
		assertFalse(result.contains("<!--"), "comments should be removed");
		assertTrue(result.contains("<div>"), "content should remain");
	}

	@Test
	public void testRemoveHtmlCommtentsDeprecated() {
		printCaption("removeHtmlCommtents (deprecated)");
		String html = "<!-- old --><p>text</p>";
		String result1 = UHtml.removeHtmlComments(html);
		String result2 = UHtml.removeHtmlCommtents(html);
		assertEquals(result1, result2, "Deprecated method should delegate to new one");
	}

	@Test
	public void testRemoveHtmlTag() {
		printCaption("removeHtmlTag — script");
		String html = "<div>keep</div><script>alert(1)</script><p>text</p><script src=\"x.js\"/>";
		String result = UHtml.removeHtmlTag(html, "script");
		assertNotNull(result);
		assertFalse(result.contains("script"), "script tags should be removed");
		assertTrue(result.contains("<div>"), "other tags should remain");
		assertTrue(result.contains("<p>"), "other tags should remain");
	}

	@Test
	public void testRemoveHtmlTagRegexSafe() {
		printCaption("removeHtmlTag — regex-safe (special chars in tag name)");
		String html = "<div.x>content</div.x>";
		String result = UHtml.removeHtmlTag(html, "div.x");
		// With Pattern.quote, "div.x" is treated literally and matched correctly
		assertFalse(result.contains("div.x"), "Literal tag name with dot should be removed safely");
	}

	@Test
	public void testRemoveHtmlTagIframe() {
		printCaption("removeHtmlTag — iframe");
		String html = "<iframe src=\"x.html\"></iframe><p>text</p>";
		String result = UHtml.removeHtmlTag(html, "iframe");
		assertFalse(result.contains("iframe"), "iframe should be removed");
		assertTrue(result.contains("<p>"), "p should remain");
	}

	@Test
	public void testRemoveHtmlTagNullAndEmpty() {
		printCaption("removeHtmlTag — null/empty");
		assertNull(UHtml.removeHtmlTag(null, "script"));
		assertEquals("", UHtml.removeHtmlTag("", "script"));
	}

	@Test
	public void testRemoveHtmlAttributesNullAndEmpty() {
		printCaption("removeHtmlAttributes — null/empty");
		assertNull(UHtml.removeHtmlAttributes(null));
		assertEquals("", UHtml.removeHtmlAttributes(""));
	}
}
