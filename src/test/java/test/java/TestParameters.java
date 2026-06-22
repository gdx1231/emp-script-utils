package test.java;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.utils.Utils;
import com.gdxsoft.easyweb.utils.msnet.MListStr;

public class TestParameters extends TestBase {

	public static void main(String[] a) {
		TestParameters t = new TestParameters();
		try {
			t.testChineseParameters();
			t.testMixedParameters();
			t.testOriginalBug();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 测试纯中文参数名
	 */
	@Test
	public void testChineseParameters() throws Exception {
		String sql = "SELECT * FROM users WHERE name = @姓名 AND age > @年龄";
		MListStr params = Utils.getParameters(sql, "@");

		System.out.println("=== testChineseParameters ===");
		for (int i = 0; i < params.size(); i++) {
			System.out.println(params.get(i));
		}

		assertEquals(2, params.size(), "应提取2个中文参数");
		assertContains(params, "姓名", "应包含参数 '姓名'");
		assertContains(params, "年龄", "应包含参数 '年龄'");
	}

	/**
	 * 测试中文参数名带后缀（如 .hash）
	 */
	@Test
	public void testChineseWithSuffix() throws Exception {
		String sql = "{CALL PR_TEST(@姓名, @姓名.hash, @年龄.int)}";
		MListStr params = Utils.getParameters(sql, "@");

		System.out.println("=== testChineseWithSuffix ===");
		for (int i = 0; i < params.size(); i++) {
			System.out.println(params.get(i));
		}

		assertEquals(3, params.size(), "应提取3个参数");
		assertContains(params, "姓名", "应包含参数 '姓名'");
		assertContains(params, "姓名.hash", "应包含参数 '姓名.hash'");
		assertContains(params, "年龄.int", "应包含参数 '年龄.int'");
	}

	/**
	 * 测试混合中英文参数
	 */
	@Test
	public void testMixedParameters() throws Exception {
		String sql = "{CALL PR_EWA_HOR_STR (@CO_UNID, @招生简章, @招生简章.hash, @G_SUP_ID, @G_ADM_ID)}";
		MListStr params = Utils.getParameters(sql, "@");

		System.out.println("=== testMixedParameters ===");
		for (int i = 0; i < params.size(); i++) {
			System.out.println(params.get(i));
		}

		assertEquals(5, params.size(), "应提取5个参数");
		assertContains(params, "CO_UNID", "应包含 'CO_UNID'");
		assertContains(params, "招生简章", "应包含 '招生简章'");
		assertContains(params, "招生简章.hash", "应包含 '招生简章.hash'");
		assertContains(params, "G_SUP_ID", "应包含 'G_SUP_ID'");
		assertContains(params, "G_ADM_ID", "应包含 'G_ADM_ID'");
	}

	/**
	 * 测试原始 bug 场景
	 */
	@Test
	public void testOriginalBug() throws Exception {
		String sql = "{CALL PR_EWA_HOR_STR ('dc701883-826c-48b4-af9b-e5382983291f', @CO_UNID, @招生简章, @招生简章.hash, @G_SUP_ID, @G_ADM_ID)}";
		MListStr params = Utils.getParameters(sql, "@");

		System.out.println("=== testOriginalBug ===");
		for (int i = 0; i < params.size(); i++) {
			System.out.println(params.get(i));
		}

		assertEquals(5, params.size(), "应提取5个参数（修复后）");
		// 修复前只有4个：CO_UNID, 招生简章.hash, G_SUP_ID, G_ADM_ID
		// 修复后应有5个：CO_UNID, 招生简章, 招生简章.hash, G_SUP_ID, G_ADM_ID
		assertContains(params, "招生简章", "修复后应包含 '招生简章'");
	}

	/**
	 * 测试英文参数名（确保修复不影响原有功能）
	 */
	@Test
	public void testEnglishParameters() throws Exception {
		String sql = "SELECT * FROM users WHERE id = @user_id AND name = @user_name";
		MListStr params = Utils.getParameters(sql, "@");

		System.out.println("=== testEnglishParameters ===");
		for (int i = 0; i < params.size(); i++) {
			System.out.println(params.get(i));
		}

		assertEquals(2, params.size());
		assertContains(params, "user_id");
		assertContains(params, "user_name");
	}

	/**
	 * 测试参数名冲突场景（@code 和 @code_key）
	 */
	@Test
	public void testParameterConflict() throws Exception {
		String sql = "SELECT @code, @code_key, @code_value FROM t";
		MListStr params = Utils.getParameters(sql, "@");

		System.out.println("=== testParameterConflict ===");
		for (int i = 0; i < params.size(); i++) {
			System.out.println(params.get(i));
		}

		assertEquals(3, params.size());
		assertContains(params, "code");
		assertContains(params, "code_key");
		assertContains(params, "code_value");
	}

	/**
	 * 测试特殊字符分隔
	 */
	@Test
	public void testSpecialCharSeparators() throws Exception {
		String sql = "WHERE @name = 'test' AND @age > 18 OR @status = 1";
		MListStr params = Utils.getParameters(sql, "@");

		System.out.println("=== testSpecialCharSeparators ===");
		for (int i = 0; i < params.size(); i++) {
			System.out.println(params.get(i));
		}

		assertEquals(3, params.size());
		assertContains(params, "name");
		assertContains(params, "age");
		assertContains(params, "status");
	}

	/**
	 * 测试中文参数后跟中文标点
	 */
	@Test
	public void testChinesePunctuation() throws Exception {
		String sql = "SELECT @姓名，@年龄；WHERE @状态 = 1";
		MListStr params = Utils.getParameters(sql, "@");

		System.out.println("=== testChinesePunctuation ===");
		for (int i = 0; i < params.size(); i++) {
			System.out.println(params.get(i));
		}

		assertEquals(3, params.size());
		assertContains(params, "姓名");
		assertContains(params, "年龄");
		assertContains(params, "状态");
	}

	/**
	 * 测试 @@ 转义场景
	 */
	@Test
	public void testDoubleAtEscape() throws Exception {
		String sql = "SELECT @@IDENTITY, @user_name FROM t";
		MListStr params = Utils.getParameters(sql, "@");

		System.out.println("=== testDoubleAtEscape ===");
		for (int i = 0; i < params.size(); i++) {
			System.out.println(params.get(i));
		}

		// @@ 应该被转义，不提取
		assertEquals(1, params.size());
		assertContains(params, "user_name");
	}

	// Helper method
	private void assertContains(MListStr list, String value, String message) {
		for (int i = 0; i < list.size(); i++) {
			if (value.equals(list.get(i))) {
				return;
			}
		}
		fail(message + " (list: " + list.join(", ") + ")");
	}

	private void assertContains(MListStr list, String value) {
		assertContains(list, value, "应包含 '" + value + "'");
	}

}
