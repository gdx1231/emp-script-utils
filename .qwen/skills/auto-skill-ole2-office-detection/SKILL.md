---
name: ole2-office-detection
description: Distinguish OLE2 compound document types (doc/xls/ppt) by peeking sector 512 for application-specific magic bytes (BOF, FIB, CurrentUserAtom).
source: auto-skill
extracted_at: '2026-06-27T05:17:00.000Z'
---

# OLE2 Compound Document Type Detection

## Problem

All legacy Microsoft Office formats (doc, xls, ppt) and WPS share the same 8-byte OLE2 magic: `D0 CF 11 E0 A1 B1 1A E1`. Magic bytes alone cannot distinguish them. Additionally, `new String(bytes)` using the platform default charset corrupts non-ASCII bytes (e.g., on Windows GBK systems), causing false negatives.

## Solution

1. **Use ISO-8859-1** for byte→char mapping (preserves all byte values 0x00-0xFF)
2. After confirming OLE2 magic at offset 0, **peek at offset 512** (first sector) for the application-specific signature

## Implementation

```java
/**
 * Distinguish OLE2 compound document types by checking first sector (offset 512).
 */
private static String detectOle2Type(byte[] buf) {
    if (buf.length < 530) {
        return "doc"; // insufficient data, default to doc
    }
    // Read first 2 bytes of sector 1 as little-endian word
    int w1 = ((buf[512] & 0xFF) | ((buf[513] & 0xFF) << 8));

    // Excel BIFF8: BOF record 0x0809, sub-type 0x0005 (workbook globals)
    // Bytes: 09 08 len_lo len_hi 00 06 05 00
    if (w1 == 0x0809 && buf.length >= 518
            && buf[516] == 0x05 && buf[517] == 0x00) {
        return "xls";
    }

    // PowerPoint: CurrentUserAtom record type 0x000F
    // Bytes: 0F 00 E8 03 ...
    if (w1 == 0x000F) {
        return "ppt";
    }

    // Word FIB: magic word 0xA5EC
    // Bytes: EC A5 00 C1 ...
    if (w1 == 0xA5EC) {
        return "doc"; // also matches WPS (same binary format)
    }

    // Default OLE2 → doc (covers WPS, MSI, MSG, etc.)
    return "doc";
}
```

## OLE2 Structure Quick Reference

```
Offset  Size  Field
0       8     Magic: D0 CF 11 E0 A1 B1 1A E1
... (header fields: CLSID, version, sector size, etc.)
512     N     First sector data (application-specific)
```

### Sector 1 (offset 512) content by application

| Format | Bytes at offset 512 | Meaning |
|--------|---------------------|---------|
| xls    | `09 08` ... `05 00` | BOF record, sub-type=workbook (0x0005) |
| ppt    | `0F 00` ... | CurrentUserAtom record (0x000F) |
| doc    | `EC A5` ... | Word FIB magic (0xA5EC) |
| wps    | `EC A5` ... | Same as doc (WPS uses Word binary format) |

## Minimum Buffer Size

- OLE2 header: 512 bytes
- Sector 1 first 6-8 bytes: +8 bytes
- **Minimum**: 530 bytes for reliable detection

If buffer < 530 bytes, default to "doc" for OLE2 magic.

## What Cannot Be Distinguished

- **doc vs wps**: WPS Office (.wps) uses the Word Binary format — identical FIB magic at sector 512
- **New WPS (2019+)**: Uses ZIP container (`PK\x03\x04`) like docx — completely different detection path

## Related: ZIP-based Office Format Detection

New Office formats (docx, xlsx, pptx, odt) use ZIP containers. To distinguish from generic ZIP, read the first local file header entry name:

| Format | First Entry Name |
|--------|-----------------|
| odt    | `mimetype` |
| docx   | Contains `word/` |
| xlsx   | Contains `xl/` |
| pptx   | Contains `ppt/` |

Implementation: parse ZIP local file header at offset 26-27 (filename length, little-endian), then read filename at offset 30.
