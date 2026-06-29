---
name: jndi-dns-query
description: Reliable JNDI DNS queries with timeout configuration, try-finally resource cleanup, DKIM key parsing, and Hashtable→Map evolution for JDK 17+.
source: auto-skill
extracted_at: '2026-06-27T05:17:00.000Z'
---

# JNDI DNS Query with Timeout and Resource Management

## Overview

Java JNDI DNS (`com.sun.jndi.dns.DnsContextFactory`) has no default timeout — an unreachable DNS server blocks indefinitely. Proper configuration and resource cleanup are essential.

## Configuration with Timeout

```java
public static Map<String, String> createDefaultEnv() {
    Map<String, String> env = new HashMap<>();
    env.put(Context.INITIAL_CONTEXT_FACTORY,
            "com.sun.jndi.dns.DnsContextFactory");

    // CRITICAL: without these, unreachable DNS blocks indefinitely
    env.put("com.sun.jndi.dns.timeout.initial", "5000");  // 5s per attempt
    env.put("com.sun.jndi.dns.timeout.retries", "2");     // retry twice = 15s total

    return env;
}
```

| Property | Default | Recommended | Effect |
|----------|---------|-------------|--------|
| `com.sun.jndi.dns.timeout.initial` | infinite | `"5000"` | Per-attempt timeout (ms) |
| `com.sun.jndi.dns.timeout.retries` | 4 | `"2"` | Number of retries before failure |

With initial=5000 + retries=2: worst case = 5s × (1 + 2) = 15s.

## Query with Resource Cleanup

Both `DirContext` and `NamingEnumeration` must be closed. JDK 17 allows try-with-resources for `DirContext`:

```java
public static List<String> nslookup(Map<String, String> env,
        String domain, String queryType) {
    String qt = queryType.toLowerCase().trim();
    List<String> values = new ArrayList<>();

    try {
        // InitialDirContext requires Hashtable even in JDK 17
        DirContext dnsContext = new InitialDirContext(new Hashtable<>(env));
        try {
            Attributes attribs = dnsContext.getAttributes(
                    domain, new String[] { qt });
            Attribute records = attribs.get(qt);

            if (records == null) {
                return null; // no such record type
            }

            NamingEnumeration<?> vals = records.getAll();
            try {
                while (vals.hasMore()) {
                    values.add(vals.next().toString());
                }
            } finally {
                vals.close(); // close enumeration
            }
        } finally {
            dnsContext.close(); // close context
        }
    } catch (NamingException ne) {
        // DNS error — log and return whatever we have
    }
    return values;
}
```

## API Evolution: Hashtable → Map

**Before** (JDK 8 style — over-synchronized):
```java
public static Hashtable<String, String> createDefaultEnv() {
    Hashtable<String, String> env = new Hashtable<>();
    ...
    return env;
}
public static List<String> nslookup(Hashtable<String, String> env, ...) { ... }
```

**After** (JDK 17 style — interface over concrete type):
```java
public static Map<String, String> createDefaultEnv() {
    Map<String, String> env = new HashMap<>(); // no synchronization overhead
    ...
    return env;
}
public static List<String> nslookup(Map<String, String> env, ...) { ... }
```

Only convert to `Hashtable` at the API boundary (`InitialDirContext` constructor requires it):
```java
DirContext dnsContext = new InitialDirContext(new Hashtable<>(env));
```

## DKIM Public Key Parsing

DKIM public keys are stored in TXT records like:
```
v=DKIM1; k=rsa; p=MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC...
```

Parse the `p=` tag:
```java
public static String queryDkimPublickey(String domain, String selector) {
    String recordname = selector + "._domainkey." + domain;
    List<String> records = nslookup(recordname, "txt");

    if (records == null || records.isEmpty()) {
        return null; // no DKIM record
    }

    for (String tag : records.get(0).split(";")) {
        tag = tag.trim();
        if (tag.startsWith("p=")) {
            return tag.substring(2); // base64-encoded DER public key
        }
    }
    return null;
}
```

## Testing Tips

### Verify timeout behavior

Use TEST-NET-1 (`192.0.2.1` — RFC 5737, non-routable):
```java
long t0 = System.currentTimeMillis();
List<String> records = nslookup("example.com", "a", "192.0.2.1");
long elapsed = System.currentTimeMillis() - t0;
assertTrue(elapsed < 20000, "Timeout should complete within 20s");
```

### Verify env configuration
```java
Map<String, String> env = createDefaultEnv();
assertEquals("com.sun.jndi.dns.DnsContextFactory",
        env.get("java.naming.factory.initial"));
assertEquals("5000", env.get("com.sun.jndi.dns.timeout.initial"));
assertEquals("2", env.get("com.sun.jndi.dns.timeout.retries"));
```

## Common Pitfalls

| Issue | Symptom | Fix |
|-------|---------|-----|
| No timeout config | Thread hangs indefinitely | Set `dns.timeout.initial` + `dns.timeout.retries` |
| `DirContext` not closed | File descriptor leak under heavy load | try-finally / try-with-resources |
| `NamingEnumeration` not closed | Native resource leak | `vals.close()` in finally block |
| `records.get(0)` without size check | `IndexOutOfBoundsException` for empty result | `if (records == null \|\| records.isEmpty())` |
| `Hashtable` in public API | Unnecessary synchronization overhead | Return `Map`, convert to `Hashtable` only at `InitialDirContext` call |
