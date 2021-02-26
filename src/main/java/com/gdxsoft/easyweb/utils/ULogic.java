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
 * 利用HSQLDB 内存数据库进行逻辑运算
 * 
 * @author admin
 *
 */
public class ULogic {
	private static Map<Integer, Boolean> CACHE = new ConcurrentHashMap<Integer, Boolean>(); // 缓存
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

			LOGGER.info("initLogic org.hsqldb.jdbcDriver ");

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
	 * 创建连接
	 * 
	 * @return
	 * @throws Exception
	 */
	private static Connection createConn() throws Exception {
		Class.forName("org.hsqldb.jdbcDriver");
		Connection conn = DriverManager.getConnection("jdbc:hsqldb:mem:.", "sa", "");
		return conn;
	}

	/**
	 * 执行表达式
	 * 
	 * @param exp
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

		int expCode = exp1.hashCode();
		if (CACHE.containsKey(expCode)) {
			return CACHE.get(expCode);
		}

		boolean rst = execExpFromJdbc(exp);
		return rst;
	}

	/**
	 * 从数据库返回表达式
	 * 
	 * @param exp
	 * @return
	 */
	private static boolean execExpFromJdbc(String exp) {
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
		addToCahche(exp.hashCode(), rst);
		long t1 = System.currentTimeMillis();
		LOGGER.debug(rst + " " + testSql + " " + (t1 - t0) + "ms");
		return rst;
	}

	/**
	 * 添加表达式到缓存中
	 * 
	 * @param code
	 * @param rst
	 */
	private synchronized static void addToCahche(int code, boolean rst) {
		CACHE.put(code, rst);
	}
}
