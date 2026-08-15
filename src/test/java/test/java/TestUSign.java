package test.java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.utils.USign;
import com.gdxsoft.easyweb.utils.Utils;

public class TestUSign extends TestBase {

	@Test
	public void testConcatSortedStrMap() {
		printCaption("concatSortedStr — Map sorted by key");
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("c", "3");
		map.put("a", "1");
		map.put("b", "2");

		String result = USign.concatSortedStr(map, false);
		assertEquals("a=1&b=2&c=3", result, "Should be sorted alphabetically");
	}

	@Test
	public void testConcatSortedStrMapSkipBlank() {
		printCaption("concatSortedStr — Map skip blank values");
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("a", "1");
		map.put("b", "");
		map.put("c", null);

		String result = USign.concatSortedStr(map, true);
		assertEquals("a=1", result, "Blank/null should be skipped");
	}

	@Test
	public void testConcatSortedStrJson() {
		printCaption("concatSortedStr — JSONObject sorted");
		JSONObject json = new JSONObject();
		json.put("z", "last");
		json.put("a", "first");
		json.put("m", "middle");

		String result = USign.concatSortedStr(json, false);
		assertTrue(result.startsWith("a=first&m=middle&z=last"),
				"JSON should be sorted by key, got: " + result);
	}

	@Test
	public void testConcatSortedStrJsonWithSeparator() {
		printCaption("concatSortedStr — JSONObject has & separator");
		JSONObject json = new JSONObject();
		json.put("a", "1");
		json.put("b", "2");
		json.put("c", "3");

		String result = USign.concatSortedStr(json, false);
		assertEquals("a=1&b=2&c=3", result);
	}

	@Test
	public void testSignMd5Map() {
		printCaption("signMd5 — Map");
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("c", "3");
		map.put("a", "1");

		String sign = USign.signMd5(map, "key", "secret", false);
		assertNotNull(sign);
		assertEquals(32, sign.length(), "MD5 hex should be 32 chars");

		// Verify: sign = md5("a=1&c=3&key=secret")
		String expected = Utils.md5("a=1&c=3&key=secret");
		assertEquals(expected, sign);
	}

	@Test
	public void testSignMd5Json() {
		printCaption("signMd5 — JSONObject");
		JSONObject json = new JSONObject();
		json.put("b", "2");
		json.put("a", "1");

		String sign = USign.signMd5(json, "key", "secret");
		assertNotNull(sign);
		assertEquals(32, sign.length());

		String expected = Utils.md5("a=1&b=2&key=secret");
		assertEquals(expected, sign, "JSON sign MD5 should match manual calculation");
	}

	@Test
	public void testSignSha1Json() {
		printCaption("signSha1 — JSONObject");
		JSONObject json = new JSONObject();
		json.put("x", "val");

		String sign = USign.signSha1(json, "k", "v");
		assertNotNull(sign);
		assertEquals(40, sign.length(), "SHA1 hex should be 40 chars");
	}

	@Test
	public void testFixNumberWithZero() {
		printCaption("fixNumberWithZero");
		assertEquals("00000123", USign.fixNumberWithZero(123, 8));
		assertEquals("0001", USign.fixNumberWithZero(1, 4));
		assertEquals("42", USign.fixNumberWithZero(42, 2));
	}
}
