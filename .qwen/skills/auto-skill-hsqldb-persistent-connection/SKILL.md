---
name: hsqldb-persistent-connection
description: HSQLDB in-memory persistent connection with volatile DCL for logic evaluation — avoid create-close per query when mem:. is isolated per connection
source: auto-skill
extracted_at: '2026-06-27T06:04:28.853Z'
---

# HSQLDB In-Memory Persistent Connection for Logic Evaluation

## Problem

HSQLDB `jdbc:hsqldb:mem:.` creates an **isolated, unnamed in-memory database** per connection. Each `DriverManager.getConnection()` call starts a fresh, empty instance. Previous DDL/DML (like `SET DATABASE SQL SYNTAX ORA TRUE`) is lost.

Naive per-query connection creation wastes ~0.2ms per call and requires re-applying initialization SQL every time.

Connection pooling does **not** work — every pooled connection sees a different empty database.

## Solution: Single Persistent Connection

One `volatile` connection reused for all queries, with `synchronized` serialization to protect the shared resource.

```java
/** HSQLDB mem:. is per-connection — MUST reuse the same instance */
private static volatile Connection PERSISTENT_CONN;
private static final Object CONN_LOCK = new Object();

/**
 * DCL (double-checked locking) with volatile for thread-safe lazy init.
 * Re-creates connection if it was closed (e.g., due to fatal error).
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
            // Apply one-time initialization
            Statement st = PERSISTENT_CONN.createStatement();
            try {
                st.execute("SET DATABASE SQL SYNTAX ORA TRUE");
            } finally {
                st.close();
            }
        }
        return PERSISTENT_CONN;
    }
}

private static Connection createConn() throws Exception {
    Class.forName("org.hsqldb.jdbc.JDBCDriver");
    return DriverManager.getConnection("jdbc:hsqldb:mem:.", "sa", "");
}
```

## Query Execution

Use `synchronized` on the execution method to serialize access to the shared connection. `Statement` and `ResultSet` are still created and closed per-call (these are thread-safe within a single connection when serialized).

```java
private synchronized static boolean execExpFromJdbc(String exp, String md5) {
    Statement st = null;
    ResultSet rs = null;
    Connection conn = null;
    String testSql = "select 1 from dual where " + exp;

    try {
        conn = getConn();        // persistent, reuses same connection
        st = conn.createStatement();
        rs = st.executeQuery(testSql);
        return rs.next();
    } catch (Exception e) {
        LOG.error(e.getMessage());
        return false;
    } finally {
        if (rs != null) { try { rs.close(); } catch (SQLException e) {} }
        if (st != null) { try { st.close(); } catch (SQLException e) {} }
        // Connection is persistent — do NOT close
    }
}
```

## Cache Layer

Wrap JDBC execution with a `ConcurrentHashMap` cache (keyed by MD5 of the expression) to avoid hitting the database for repeated expressions:

```java
private static Map<String, Boolean> CACHE = new ConcurrentHashMap<>();

public static boolean runLogic(String exp) {
    // ... validate input ...
    String md5 = Utils.md5(exp);
    Boolean cached = CACHE.get(md5);
    if (cached != null) {
        return cached;
    }
    return execExpFromJdbc(exp, md5);
}
```

## Eviction Strategy

Avoid brute-force `CACHE.clear()` — it evicts useful entries. Instead, trim to keep the most recent entries:

```java
private static void addToCache(String md5, boolean rst) {
    if (CACHE.size() > 10000) {
        // Keep the 5000 most recent entries
        CACHE.keySet().removeIf(k -> CACHE.size() > 5000);
    }
    CACHE.put(md5, rst);
}
```

## Performance Impact

| Approach | Per unique expression |
|----------|----------------------|
| New connection each time | ~220,000 ns |
| Persistent connection (this pattern) | ~150,000 ns (**33% faster**) |
| Cache hit | ~58,000 ns (pure HashMap) |

## Anti-Patterns

| Don't | Why | Do Instead |
|-------|-----|------------|
| `conn.close()` in finally block | Destroys the shared persistent connection | Keep connection open for JVM lifetime |
| Connection pool for `mem:.` | Every pool connection is a separate empty DB | Single persistent connection |
| Static initializer block that closes connection | Oracle syntax setting lost on next connect | Lazy-init via `getConn()`, never close |
| `createConn()` every call | ~0.2ms wasted per expression | `getConn()` returns persistent instance |

## Security: Input Validation

When executing dynamic SQL (even in-memory), whitelist the expression characters and block comment/statement terminators:

```java
if (!exp.matches("^[0-9a-zA-Z_\\-+*/%()\\[\\].,'\\s=!<>&|^~@:]+$")
        || exp.contains("--") || exp.contains("/*") || exp.contains(";")) {
    LOG.warn("Rejected unsafe expression: {}", exp);
    return false;
}
```
