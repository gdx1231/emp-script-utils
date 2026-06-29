---
name: argon2-xml-config
description: Tune Argon2 memory/iterations via ewa_conf.xml with a singleton ConfArgon2 config class, 1GB server-safe defaults (1MB/3-iterations), backward-compatible hash verification, and PROP_TIME-based hot-reload detection.
source: auto-skill
extracted_at: '2026-06-27T05:32:43.018Z'
---

# Argon2 Configuration via ewa_conf.xml with ConfArgon2

## Problem

Argon2 password hashing default parameters (e.g., 32KB memory) are too weak for production,
but the right defaults depend on the deployment server's available memory.
1GB servers constrain concurrent hash operations.
Parameters must be tunable without code changes, and existing persisted hashes must remain verifiable.

## Architecture

Follow the existing `ConfImageMagick` pattern: a dedicated singleton config class in `com.gdxsoft.easyweb.conf`.

```
ewa_conf.xml  ──>  UPath.initPathXml()  ──>  ConfArgon2.initConfig()
                                                   │
                                          UArgon2() constructor reads
                                          ConfArgon2.getInstance().getMemoryKB()
                                          ConfArgon2.getInstance().getIterations()
```

## Implementation

### 1. ConfArgon2 singleton class

```java
package com.gdxsoft.easyweb.conf;

public class ConfArgon2 {
    private static ConfArgon2 INST = null;
    private static long PROP_TIME = 0;

    private int memoryKB = 1024;   // 1MB default
    private int iterations = 3;

    public static ConfArgon2 getInstance() {
        if (INST != null && UPath.getPropTime() == PROP_TIME) {
            return INST; // cached; ewa_conf hasn't changed
        }
        initConfig();
        return INST;
    }

    synchronized static void initConfig() {
        INST = new ConfArgon2();

        if (UPath.getCfgXmlDoc() == null) {
            PROP_TIME = UPath.getPropTime(); // MUST set even on early return
            return;
        }

        NodeList nl = UPath.getCfgXmlDoc().getElementsByTagName("argon2");
        if (nl.getLength() == 0) {
            PROP_TIME = UPath.getPropTime();
            return;
        }

        Element ele = (Element) nl.item(0);
        String mem = ele.hasAttribute("memory") ? ele.getAttribute("memory")
                : ele.hasAttribute("Memory") ? ele.getAttribute("Memory") : null;
        String iter = ele.hasAttribute("iterations") ? ele.getAttribute("iterations")
                : ele.hasAttribute("Iterations") ? ele.getAttribute("Iterations") : null;

        if (mem != null) {
            try { INST.memoryKB = Integer.parseInt(mem.trim()); }
            catch (NumberFormatException e) { LOG.warn("Invalid memory: {}", mem); }
        }
        if (iter != null) {
            try { INST.iterations = Integer.parseInt(iter.trim()); }
            catch (NumberFormatException e) { LOG.warn("Invalid iterations: {}", iter); }
        }

        PROP_TIME = UPath.getPropTime();
        LOG.info("Argon2 config: memory={}KB, iterations={}", INST.memoryKB, INST.iterations);
    }

    public int getMemoryKB() { return memoryKB; }
    public int getIterations() { return iterations; }
}
```

### 2. PROP_TIME early-return pitfall

**MUST** set `PROP_TIME = UPath.getPropTime()` on EVERY return path, including early returns.
Otherwise, if UPath later loads an ewa_conf after ConfArgon2 initialized,
`UPath.getPropTime()` changes but `ConfArgon2.PROP_TIME` stays at 0,
breaking the cache check (`PROP_TIME == UPath.getPropTime()` → false),
causing `getInstance()` to call `initConfig()` repeatedly, breaking singleton semantics.

```java
// WRONG — PROP_TIME not set on early return
if (UPath.getCfgXmlDoc() == null) {
    return; // PROP_TIME stays 0, breaks singleton when UPath later loads config
}

// CORRECT
if (UPath.getCfgXmlDoc() == null) {
    PROP_TIME = UPath.getPropTime();
    return;
}
```

### 3. UArgon2 constructor reads from ConfArgon2

```java
import com.gdxsoft.easyweb.conf.ConfArgon2;

public UArgon2() {
    ConfArgon2 cfg = ConfArgon2.getInstance();
    argon2Type = DEFAULT_ARGON2_TYPE;
    version = DEFAULT_VERSION;
    parallelity = DEFAULT_PARALLELISM;
    iterations = cfg.getIterations();
    memory = cfg.getMemoryKB();
    saltLength = DEFAULT_SALT_LENGTH;
}
```

No package-private static fields on UArgon2 — all config centralized in ConfArgon2.

### 4. XML config element in ewa_conf.xml

```xml
<argon2 memory="1024" iterations="3" />
```

- `memory`: KB of RAM per hash invocation
- `iterations`: number of passes
- Not present → uses code defaults (1MB/3)
- Attribute names accept both `memory`/`Memory` and `iterations`/`Iterations` (case-tolerant)

### 5. Backward compatibility

Verification reads parameters from the hash string, not from defaults:
```
$argon2id$v=19$m=1024,t=3,p=1$salt$hash
                  ^^^^^^ ^^^
```
So existing hashes with old `m=32` verify correctly regardless of new defaults.

### 6. Memory sizing for 1G servers

| Concurrent hashes | 1MB/hash | 32MB/hash | 64MB/hash |
|-------------------|----------|-----------|-----------|
| 1                 | 1MB      | 32MB      | 64MB      |
| 5                 | 5MB      | 160MB     | 320MB     |
| 10                | 10MB     | 320MB     | 640MB ⚠️  |

Rule: `total_memory = memoryKB * peak_concurrency`. For 1G server with 300-500MB
usable, keep total under 150MB.

Recommended configs:
```xml
<!-- 1G server (safe) -->
<argon2 memory="1024" iterations="3" />     <!-- 5ms, 10-concurrency=10MB -->

<!-- 4G+ server (stronger) -->
<argon2 memory="65536" iterations="2" />    <!-- 300ms, 10-concurrency=640MB -->
```

## Testing

### ConfArgon2 singleton test
```java
@Test
public void testDefaultValues() {
    ConfArgon2 cfg = ConfArgon2.getInstance();
    assertEquals(1024, cfg.getMemoryKB());
    assertEquals(3, cfg.getIterations());
}

@Test
public void testSingleton() {
    ConfArgon2 a = ConfArgon2.getInstance();
    ConfArgon2 b = ConfArgon2.getInstance();
    assertSame(a, b);
}
```

### Hash format verification
```java
@Test
public void testDefaultMemory1024() {
    String hashed = UArgon2.hashPwd("test");
    assertTrue(hashed.contains("m=1024,") || hashed.contains("m=1024$"));
}
```
