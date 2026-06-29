---
name: zip-slip-prevention
description: Prevent Zip Slip (path traversal) attacks in ZIP extraction by validating canonical paths stay within the target directory.
source: auto-skill
extracted_at: '2026-06-27T05:17:00.000Z'
---

# Zip Slip Prevention

## Problem

When extracting ZIP files, entry names can contain `../` sequences that escape the target directory:

```java
// VULNERABLE — attacker-controlled entry name escapes target
String filePath = targetPath + File.separator + entry.getName();
FileOutputStream fos = new FileOutputStream(filePath);
// entry.getName() = "../../.ssh/authorized_keys" → writes outside targetPath
```

This is CWE-22 (Path Traversal), commonly known as "Zip Slip".

## Solution

Before writing any extracted file, resolve its canonical path and verify it starts with the target directory's canonical path:

```java
public static List<String> unZipFile(String zipFilePath, String targetPath) throws IOException {
    File target = new File(targetPath);
    if (!target.exists()) { target.mkdirs(); }

    String canonicalTarget = target.getCanonicalPath();

    ZipFile zipFile = new ZipFile(zipFilePath);
    Enumeration<?> entries = zipFile.entries();
    List<String> fileList = new ArrayList<>();

    while (entries.hasMoreElements()) {
        ZipEntry entry = (ZipEntry) entries.nextElement();
        if (entry.isDirectory()) { continue; }

        File destFile = new File(targetPath, entry.getName());
        String canonicalDest = destFile.getCanonicalPath();

        // Validate: destination must be within target directory
        if (!canonicalDest.startsWith(canonicalTarget + File.separator)
                && !canonicalDest.equals(canonicalTarget)) {
            throw new IOException(
                "Zip entry escapes target directory: " + entry.getName());
        }

        // Safe to write
        destFile.getParentFile().mkdirs();
        // ... copy entry content to destFile ...
        fileList.add(destFile.getAbsolutePath());
    }
    return fileList;
}
```

## Why `.startsWith(canonical + File.separator)`

```java
// WRONG — "/tmp/app_evil" starts with "/tmp/app" without separator check
canonicalDest.startsWith(canonicalTarget)

// CORRECT — requires separator or exact match
canonicalDest.startsWith(canonicalTarget + File.separator)
    || canonicalDest.equals(canonicalTarget)
```

Without the separator suffix, `/tmp/app_evil/file` passes validation for target `/tmp/app`.

## Why `getCanonicalPath()` not `getAbsolutePath()`

| Method | `/tmp/../etc/passwd` resolves to |
|--------|-------------------------------|
| `getAbsolutePath()` | `/tmp/../etc/passwd` (relative components preserved) |
| `getCanonicalPath()` | `/etc/passwd` (resolved, normalized) |

`getAbsolutePath` does NOT resolve `..` — using it for Zip Slip validation is equivalent to no validation at all.

## Common Issues

| Issue | Fix |
|-------|-----|
| Using `entry.getName()` directly in path construction | Always use `new File(targetPath, entry.getName())` then validate |
| Using `getAbsolutePath()` for validation | Must use `getCanonicalPath()` |
| No separator check in `startsWith` | Check `+ File.separator` or exact equality |
| Catching and ignoring IOException during extraction | Re-throw or at minimum log the offending entry |
| Same path for directories and files | Skip directory entries (`entry.isDirectory()`) |

## Test: Create a Malicious ZIP for Verification

```java
@Test
public void testZipSlipBlocked() throws Exception {
    // Create a ZIP with path traversal entry
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ZipOutputStream zos = new ZipOutputStream(baos);
    zos.putNextEntry(new ZipEntry("../../escape.txt"));
    zos.write("malicious".getBytes());
    zos.closeEntry();
    zos.close();

    Files.write(Path.of("/tmp/malicious.zip"), baos.toByteArray());

    // Should throw or skip the entry (depending on implementation)
    assertThrows(IOException.class, () -> {
        UFile.unZipFile("/tmp/malicious.zip", "/tmp/safe_target");
    });
}
```
