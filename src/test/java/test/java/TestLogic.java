package test.java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import com.gdxsoft.easyweb.utils.ULogic;

public class TestLogic extends TestBase {

	@Test
	public void testBasicLogic() {
		printCaption("Basic logic expressions");
		assertTrue(ULogic.runLogic("1 = 1"));
		assertTrue(ULogic.runLogic(" 1 = 1 "));
		assertTrue(ULogic.runLogic("1=1"));
		assertFalse(ULogic.runLogic("1 = 2"));
		assertTrue(ULogic.runLogic("31 < 100"));
		assertFalse(ULogic.runLogic("31 < 1"));
	}

	@Test
	public void testStringLogic() {
		printCaption("String comparisons");
		assertTrue(ULogic.runLogic("'a' = 'a'"));
		assertTrue(ULogic.runLogic("'aaa' < 'b'"));
		assertFalse(ULogic.runLogic("'zzz' = 'aaa'"));
	}

	@Test
	public void testNullAndEmpty() {
		printCaption("Null/empty input");
		assertFalse(ULogic.runLogic(null));
		assertFalse(ULogic.runLogic(""));
		assertFalse(ULogic.runLogic("   "));
	}

	@Test
	public void testCache() {
		printCaption("Caching — same expression returns same result");
		String exp = "100 > 50";
		assertTrue(ULogic.runLogic(exp));
		assertTrue(ULogic.runLogic(exp)); // cached
	}

	@Test
	public void testSqlInjectionBlocked() {
		printCaption("SQL injection blocked by whitelist");
		// Semicolons, DROP, CREATE etc. should be rejected
		assertFalse(ULogic.runLogic("1=1; DROP TABLE users"));
		assertFalse(ULogic.runLogic("1=1 -- comment"));
		assertFalse(ULogic.runLogic("1=1 /* inject */"));
	}

	@Test
	public void testUnicodeStringLogic() {
		printCaption("Unicode string literals (Chinese, Japanese)");
		// Chinese characters in string comparisons
		assertTrue(ULogic.runLogic("'管理员' = '管理员'"));
		assertFalse(ULogic.runLogic("'管理员' = '普通用户'"));
		assertTrue(ULogic.runLogic("'管理员' <> ''"));

		// Mixed ASCII/Unicode
		assertTrue(ULogic.runLogic("'管理员' <> 'admin'"));
		assertTrue(ULogic.runLogic("'ユーザー' = 'ユーザー'"));

		// Unicode in empty string comparisons
		assertFalse(ULogic.runLogic("'管理员' = ''"));
		assertTrue(ULogic.runLogic("'' = ''"));
	}

	@Test
	public void testAndOr() {
		printCaption("AND/OR expressions");
		assertTrue(ULogic.runLogic("1=1 AND 2=2"));
		assertFalse(ULogic.runLogic("1=1 AND 2=3"));
		assertTrue(ULogic.runLogic("1=1 OR 1=2"));
		assertFalse(ULogic.runLogic("1=2 OR 2=3"));
	}

	@Test
	public void testPerformance() {
		printCaption("Performance — first call vs cached");

		// Warmup (static init)
		ULogic.runLogic("1=1");

		// First call: creates HSQLDB connection + query
		long t0 = System.nanoTime();
		ULogic.runLogic("100 > 50 AND 200 < 300");
		long firstCall = System.nanoTime() - t0;

		// Cached call: HashMap lookup only
		t0 = System.nanoTime();
		ULogic.runLogic("100 > 50 AND 200 < 300");
		long cachedCall = System.nanoTime() - t0;

		// 10 unique expressions: each creates new connection
		t0 = System.nanoTime();
		for (int i = 0; i < 10; i++) {
			ULogic.runLogic(i + " = " + i);
		}
		long tenUnique = System.nanoTime() - t0;

		System.out.printf("First call (new conn):  %,d ns%n", firstCall);
		System.out.printf("Cached call (HashMap): %,d ns%n", cachedCall);
		System.out.printf("10 unique calls:       %,d ns (%,d avg)%n", tenUnique, tenUnique / 10);

		assertTrue(cachedCall < firstCall, "Cached call should be faster than first call");
	}

	@Test
	public void testMultiThreaded() throws Exception {
		printCaption("Multi-threaded — 10 threads × 100 calls");

		final int threads = 10;
		final int callsPerThread = 100;
		final String[] exps = new String[callsPerThread];
		for (int i = 0; i < callsPerThread; i++) {
			exps[i] = i + " = " + i; // always true
		}

		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch doneLatch = new CountDownLatch(threads);
		AtomicLong cacheHits = new AtomicLong();
		AtomicLong totalTime = new AtomicLong();

		for (int t = 0; t < threads; t++) {
			new Thread(() -> {
				try {
					startLatch.await();
					for (String exp : exps) {
						long t0 = System.nanoTime();
						ULogic.runLogic(exp);
						long elapsed = System.nanoTime() - t0;
						totalTime.addAndGet(elapsed);
						// Second call hits cache
						if (ULogic.runLogic(exp)) {
							cacheHits.incrementAndGet();
						}
					}
				} catch (InterruptedException ignored) {
				} finally {
					doneLatch.countDown();
				}
			}).start();
		}

		long t0 = System.nanoTime();
		startLatch.countDown();
		doneLatch.await();
		long wallTime = System.nanoTime() - t0;

		long totalCalls = threads * callsPerThread;
		System.out.printf("Threads: %d, calls/thread: %d%n", threads, callsPerThread);
		System.out.printf("Total calls:   %d%n", totalCalls);
		System.out.printf("Wall time:     %,d ns (%,d ns/call)%n",
				wallTime, wallTime / totalCalls);
		System.out.printf("Sum CPU time:  %,d ns (%,d ns/call)%n",
				totalTime.get(), totalTime.get() / totalCalls);

		assertEquals(totalCalls, cacheHits.get(), "All second calls should hit cache");
	}

	@Test
	public void testMemoryFootprint() {
		printCaption("Memory footprint estimation");

		Runtime rt = Runtime.getRuntime();
		System.gc();
		long before = rt.totalMemory() - rt.freeMemory();

		// Trigger connection init
		ULogic.runLogic("1=1");

		// Fill cache with 5000 entries
		for (int i = 0; i < 5000; i++) {
			ULogic.runLogic(i + " = " + i);
		}

		System.gc();
		long after = rt.totalMemory() - rt.freeMemory();
		long used = after - before;

		System.out.printf("Before:        %,d bytes%n", before);
		System.out.printf("After:         %,d bytes%n", after);
		System.out.printf("ULogic usage: ~%,d bytes (HSQLDB + 5000 cache entries)%n", used);

		// Rough sanity: HSQLDB + 5000 entries should be under 10MB
		assertTrue(used < 20_000_000,
				"Memory usage should be under 20MB, actual: " + used);
	}
}
