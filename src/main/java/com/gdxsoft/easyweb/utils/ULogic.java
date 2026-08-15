package com.gdxsoft.easyweb.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use HSQLDB memory database for logical operations
 */
public class ULogic {
	private static Map<String, Boolean> CACHE = new ConcurrentHashMap<>();
	private static Logger LOGGER = LoggerFactory.getLogger(ULogic.class);

	/** Persistent HSQLDB connection — in-memory DB is per-connection, must reuse */
	private static volatile Connection PERSISTENT_CONN;
	private static final Object CONN_LOCK = new Object();

	/**
	 * Get or create the persistent HSQLDB connection.
	 * DCL (double-checked locking) with volatile for thread-safe lazy init.
	 */
	private static Connection getConn() throws SQLException {
		Connection c = PERSISTENT_CONN;
		if (c != null && !c.isClosed()) {
			return c;
		}
		synchronized (CONN_LOCK) {
			if (PERSISTENT_CONN == null || PERSISTENT_CONN.isClosed()) {
				try {
					PERSISTENT_CONN = createConn();
				} catch (Exception e) {
					throw new SQLException("Failed to create HSQLDB connection", e);
				}
				Statement st = PERSISTENT_CONN.createStatement();
				try {
					st.execute("SET DATABASE SQL SYNTAX ORA TRUE");
				} finally {
					st.close();
				}
				LOGGER.info("ULogic persistent connection initialized");
			}
			return PERSISTENT_CONN;
		}
	}

	/**
	 * Create connection
	 * 
	 * @return
	 * @throws Exception
	 */
	private static Connection createConn() throws Exception {
		Class.forName("org.hsqldb.jdbc.JDBCDriver");
		Connection conn = DriverManager.getConnection("jdbc:hsqldb:mem:.", "sa", "");
		return conn;
	}
	/**
	 * Execute the logic expression
	 *
	 * @param exp the logic expression
	 * @return true/false
	 */
	public static boolean runLogic(String exp) {
		if (exp == null) {
			return false;
		}
		String exp1 = exp.trim();

		if (exp1.length() == 0) {
			return false;
		}

		// Whitelist: only allow SQL-compatible logical operators and values
		if (!exp1.matches("^[0-9a-zA-Z_\\p{L}\\-+*/%()\\[\\].,'\\s=!<>&|^~@:]+$")
				|| exp1.contains("--") || exp1.contains("/*") || exp1.contains(";")) {
			LOGGER.warn("Rejected unsafe logic expression: {}", exp1);
			return false;
		}

		String md5 = Utils.md5(exp1);
		if (CACHE.containsKey(md5)) {
			return CACHE.get(md5);
		}

		return execExpFromJdbc(exp1, md5);
	}

	/**
	 * Execute the logic expression for hsqldb
	 * 
	 * @param exp the logic expression
	 * @return
	 */
	private synchronized static boolean execExpFromJdbc(String exp, String md5) {
		boolean rst = false;
		Statement st = null;
		ResultSet rs = null;
		Connection conn = null;
		String testSql = "select 1 from dual where " + exp;

		long t0 = System.currentTimeMillis();
		try {
			conn = getConn();
			st = conn.createStatement();
			rs = st.executeQuery(testSql);
			rst = rs.next();
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			LOGGER.error(testSql);
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
					LOGGER.error(e.getMessage());
				}
			}
			if (st != null) {
				try {
					st.close();
				} catch (SQLException e) {
					LOGGER.error(e.getMessage());
				}
			}
			// Connection is persistent, do NOT close
		}
		addToCache(md5, rst);
		long t1 = System.currentTimeMillis();
		LOGGER.debug(rst + " " + testSql + " " + (t1 - t0) + "ms");
		return rst;
	}

	/**
	 * Cached the expression
	 * 
	 * @param code
	 * @param rst
	 */
	private static void addToCache(String md5, boolean rst) {
		if (CACHE.size() > 10000) {
			LOGGER.info("Trimming cache from {} entries", CACHE.size());
			// Keep the 5000 most recently added entries
			CACHE.keySet().removeIf(k -> CACHE.size() > 5000);
		}
		CACHE.put(md5, rst);
	}
}
