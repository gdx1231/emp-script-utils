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
	private static Map<String, Boolean> CACHE = new ConcurrentHashMap<String, Boolean>(); // 缓存
	private static Logger LOGGER = LoggerFactory.getLogger(ULogic.class);

	static {
		Statement st = null;
		Connection conn = null;
		try {
			conn = createConn();
			// This property, when set TRUE, enables support for some elements of Oracle
			// syntax. The DUAL table is supported
			// , together with ROWNUM, NEXTVAL and CURRVAL syntax and semantics.
			st = conn.createStatement();
			st.execute("SET DATABASE SQL SYNTAX ORA TRUE ");

			LOGGER.info("initLogic org.hsqldb.jdbc.JDBCDriver ");
		} catch (Exception e) {
			String ERR_MSG = e.getMessage();
			LOGGER.error(ERR_MSG);
		} finally {
			if (conn != null) {
				try {
					conn.close();
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
	 * 
	 */
	public static boolean runLogic(String exp) {
		if (exp == null) {
			return false;
		}
		String exp1 = exp.trim();

		if (exp1.length() == 0) {
			return false;
		}

		String md5 = Utils.md5(exp1);
		if (CACHE.containsKey(md5)) {
			return CACHE.get(md5);
		}

		boolean rst = execExpFromJdbc(exp, md5);
		return rst;
	}

	/**
	 * Execute the logic expression for hsqldb
	 * 
	 * @param exp the logic expression
	 * @return
	 */
	private static boolean execExpFromJdbc(String exp, String md5) {
		boolean rst = false;
		Statement st = null;
		ResultSet rs = null;
		Connection conn = null;
		String testSql = "select 1 from dual where " + exp;

		long t0 = System.currentTimeMillis();
		try {
			conn = createConn();
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
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					LOGGER.error(e.getMessage());
				}
			}
		}
		addToCahche(md5, rst);
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
	private static void addToCahche(String md5, boolean rst) {
		if (CACHE.size() > 10000) {
			LOGGER.info("CLEAR over 10000");
			CACHE.clear();
		}
		CACHE.put(md5, rst);
	}
}
