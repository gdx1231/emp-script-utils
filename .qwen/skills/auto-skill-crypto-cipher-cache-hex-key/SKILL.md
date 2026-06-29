---
name: crypto-cipher-cache-hex-key
description: When caching crypto Cipher instances by key+IV bytes, use hex or MD5 encoding for HashMap keys instead of new String(bytes) which corrupts binary data via charset conversion.
source: auto-skill
extracted_at: '2026-06-27T06:40:35.789Z'
---

# Crypto Cipher Cache Binary Key Safety

## Problem

When caching `Cipher` or `OpCipher` instances in a `HashMap` for reuse, the cache key must uniquely identify the combination of key bytes + IV bytes + encrypt/decrypt mode. Using `new String(byte[])` to convert binary crypto material to a String key is unsafe:

```java
// DANGEROUS: binary key/IV → platform-default charset String
String cipherKey = new String(iv) + "," + new String(key) + ",true";
if (mapCiphers.containsKey(cipherKey)) { ... }
```

## Why `new String(byte[])` fails

1. **Charset corruption**: Binary bytes outside the platform charset range produce `?` replacement characters or exception-throwing malformed sequences
2. **Key collisions**: Different `iv`/`key` byte arrays can produce the same `String` after charset lossy conversion
3. **Thread/platform dependency**: The same bytes produce different `String` values depending on `file.encoding`

## Solution: Use deterministic hash or hex encoding

```java
// SAFE: MD5 of byte arrays as cache key
String cipherKey = Utils.md5(iv) + "," + Utils.md5(key) + ",true";
if (mapCiphers.containsKey(cipherKey)) { ... }
```

Alternative (no external dependency):
```java
// SAFE: Hex encoding
String cipherKey = bytesToHex(iv) + "," + bytesToHex(key) + ",true";
```

## When to apply

- Any `HashMap`/`ConcurrentHashMap` key derived from `byte[]` that contains cryptographic material (keys, IVs, nonces, salts)
- JCE `Cipher` caching, BouncyCastle cipher caching, MAC instance caching
- Session token caches that use raw bytes as identifiers

## Where this was fixed

- `UAes.encryptBytes()` — `new String(iv) + new String(key) + ",true"` → `Utils.md5(iv) + Utils.md5(key) + ",true"`
- `UAes.decryptBytes()` — same pattern for decrypt cache key
