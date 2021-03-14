package test.java;

import org.junit.Test;

import com.gdxsoft.easyweb.utils.UPath;

public class TestUpath {
	public static void main(String[] args) {
		TestUpath t = new TestUpath();
		t.testUpath();
	}

	@Test
	public void testUpath() {
		UPath.CHK_DURATION = -1;
		
		UPath.main(null);
		
		UPath.main(null);
		
		UPath.main(null);
	}
}
