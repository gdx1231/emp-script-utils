---
name: binary-magic-detection
description: Detect file types from binary magic bytes using ISO-8859-1 String mapping with startsWith/charAt instead of byte array copies or hex conversion.
source: auto-skill
extracted_at: '2026-06-27T05:45:00.000Z'
---

# Binary Magic Number File Type Detection

## Overview

Detecting file types from raw bytes traditionally uses either byte array comparison or hex string matching. Both have unnecessary overhead. Using `new String(bytes, offset, len, StandardCharsets.ISO_8859_1)` maps each byte (0x00-0xFF) to a Java char (U+0000-U+00FF) without encoding corruption, enabling efficient `startsWith()` and `charAt()` checks.

## Why ISO-8859-1

ISO-8859-1 (Latin-1) maps bytes 0x00-0xFF one-to-one to Unicode code points U+0000 to U+00FF. This means:

```java
// These are always equivalent:
byte b = buf[i];
char c = new String(buf, 0, len, StandardCharsets.ISO_8859_1).charAt(i);
// c == (b & 0xFF) — guaranteed
```

- `charAt(0)` → `buf[0] & 0xFF` without bounds checking overhead
- `startsWith("PK")` → checks `buf[0] == 'P' && buf[1] == 'K'`
- Non-printable bytes (0x00-0x1F, 0x80-0x9F, 0xFF) map correctly

**Never use the platform default charset** (`new String(bytes)` without charset) for binary magic detection — on non-UTF-8 systems (Windows GBK), byte sequences get corrupted.

## Pattern

### Before: byte copies + hex conversion (slow, wasteful)

```java
// Copies 120 bytes unnecessarily
byte[] bytes = new byte[120];
System.arraycopy(buf, 0, bytes, 0, bytes.length);
String s = new String(bytes).toUpperCase(); // platform charset!

if (s.indexOf("PNG") >= 0) return "png";     // matches ANYWHERE — false positives

// Another copy for hex matching
byte[] bytesDoc = new byte[8];
System.arraycopy(buf, 0, bytesDoc, 0, 8);
String hex = Utils.bytes2hex(bytesDoc);
if (hex.equals("D0CF11E0A1B11AE1")) return "doc";
```

Problems:
- 2× `System.arraycopy` allocations
- Platform charset (corrupted on non-UTF-8 systems)
- `indexOf` matches anywhere in string (false positives)
- Hex conversion for what could be direct char comparison

### After: ISO-8859-1 String + charAt/startsWith (zero-copy, precise)

```java
String s = new String(buf, 0, 120, StandardCharsets.ISO_8859_1);
char c0 = s.charAt(0);

// ZIP: PK\x03\x04
if (c0 == 'P' && s.charAt(1) == 'K') return "zip";

// PNG: \x89 P N G
if (c0 == 0x89 && s.charAt(1) == 'P' && s.charAt(2) == 'N' && s.charAt(3) == 'G')
    return "png";

// JPEG: \xFF \xD8 \xFF, then JFIF/Exif at offset 6
if (c0 == 0xFF && s.charAt(1) == 0xD8 && s.charAt(2) == 0xFF
        && s.length() > 10
        && (s.substring(6, 10).equals("JFIF") || s.substring(6, 10).equals("Exif")))
    return "jpg";

// OLE2 compound: D0 CF 11 E0 A1 B1 1A E1
if (c0 == 0xD0 && s.charAt(1) == 0xCF && s.charAt(2) == 0x11 && s.charAt(3) == 0xE0
        && s.charAt(4) == 0xA1 && s.charAt(5) == 0xB1 && s.charAt(6) == 0x1A && s.charAt(7) == 0xE1)
    return "doc"; // also: xls, ppt, msi...

// TIFF: "II" (little-endian) or "MM" (big-endian)
if ((c0 == 'I' && s.charAt(1) == 'I') || (c0 == 'M' && s.charAt(1) == 'M'))
    return "tif";

// RAR: R a r ! \x1A \x07 \x00
if (s.startsWith("Rar!\u001A\u0007")) return "rar";

// GIF: G I F 8 (7a or 9a)
if (s.startsWith("GIF8")) return "gif";

// SWF: CWS/FWS + version byte ≤ 20
if ((s.startsWith("CWS") || s.startsWith("FWS")) && s.length() > 3 && s.charAt(3) <= 20)
    return "swf";
```

## When to use charAt vs startsWith vs substring

| Approach | Use when | Example |
|----------|----------|---------|
| `startsWith("...")` | Fixed prefix, all ASCII printable bytes | `"PK"`, `"GIF8"`, `"{\\rtf"` |
| `charAt(N)` | Fixed position, may contain non-printable bytes (0x00-0x1F, 0x80-0xFF) | `c0 == 0x89`, `s.charAt(1) == 0xD8` |
| `substring(a, b).equals("...")` | Fixed range, printable bytes | `s.substring(6, 10).equals("JFIF")` |

## What NOT to do

1. **Don't use `String.indexOf` for magic bytes** — matches anywhere in buffer, causing false positives for file formats with magic bytes at fixed positions
2. **Don't use platform charset** — always `StandardCharsets.ISO_8859_1` for binary data
3. **Don't convert to hex for comparison** — `charAt` directly checks the value without intermediate `bytes2hex` + string allocation
4. **Don't `System.arraycopy` bytes before creating String** — `new String(buf, offset, len, charset)` avoids the copy

## Charset note for JDK 8 compatibility

If the target is JDK 8, use `java.nio.charset.StandardCharsets.ISO_8859_1`. If `StandardCharsets` is unavailable (pre-JDK 7), use `Charset.forName("ISO-8859-1")` cached as a constant.

## Advanced: Distinguishing ZIP Container Formats

New Office formats (docx, xlsx, pptx, odt) use ZIP containers (`PK\x03\x04`). The first entry's filename distinguishes them from generic ZIP:

```java
if (c0 == 'P' && s.charAt(1) == 'K' && s.charAt(2) == 0x03 && s.charAt(3) == 0x04) {
    String firstEntry = readZipFirstEntryName(buf);
    if (firstEntry != null) {
        if (firstEntry.equals("mimetype")) return "odt";
        if (firstEntry.contains("word/"))    return "docx";
        if (firstEntry.contains("xl/"))      return "xlsx";
        if (firstEntry.contains("ppt/"))     return "pptx";
    }
    return "zip";
}
```

### readZipFirstEntryName implementation

ZIP local file header at offset 0:
```
Offset Size  Field
0      4     Signature PK\x03\x04
26     2     Filename length (little-endian)
28     2     Extra field length (little-endian)
30     N     Filename
```

```java
private static String readZipFirstEntryName(byte[] buf) {
    if (buf.length < 30) return null;
    int nameLen = ((buf[27] & 0xFF) << 8) | (buf[26] & 0xFF);
    int nameStart = 30;
    int nameEnd = nameStart + nameLen;
    if (nameLen <= 0 || nameLen > 256 || buf.length < nameEnd) return null;
    return new String(buf, nameStart, nameLen, StandardCharsets.UTF_8);
}
```

## Advanced: Distinguishing OLE2 Container Formats

All legacy Office formats share OLE2 magic (`D0 CF 11 E0 A1 B1 1A E1`). Peek at **offset 512** (first sector) for application-specific signatures:

| Byte at 512-513 (LE) | Application |
|----------------------|-------------|
| `0x0809` + sub-type `0x0005` at 516 | xls (Excel BIFF8 BOF) |
| `0x000F` | ppt (PowerPoint CurrentUserAtom) |
| `0xA5EC` | doc/wps (Word FIB) |

Minimum buffer: **530 bytes** (512 OLE2 header + 8 sector peek).

```java
private static String detectOle2Type(byte[] buf) {
    if (buf.length < 530) return "doc";
    int w1 = ((buf[512] & 0xFF) | ((buf[513] & 0xFF) << 8));
    if (w1 == 0x0809 && buf.length >= 518
            && buf[516] == 0x05 && buf[517] == 0x00) return "xls";
    if (w1 == 0x000F) return "ppt";
    if (w1 == 0xA5EC) return "doc"; // also wps
    return "doc"; // default OLE2
}
```

### Limitations

- **doc vs wps**: WPS Office uses the Word Binary format — identical FIB magic. Cannot distinguish without deeper content inspection.
- **New WPS (2019+)**: Uses ZIP container like docx — must distinguish via ZIP entry name (falls to docx).
