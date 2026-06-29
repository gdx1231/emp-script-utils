---
name: xml-security-hardening
description: Secure Java XML parsing against XXE/BillionLaughs, cache DocumentBuilderFactory/TransformerFactory, fix XML escape order bugs, and replace string-based XML manipulation with DOM API.
source: auto-skill
extracted_at: '2026-06-27T04:53:30.380Z'
---

# XML Security Hardening for Java XML Parsing

## Overview

Standard Java XML parsing is vulnerable to XXE (XML External Entity) injection and entity expansion (Billion Laughs) attacks if `DocumentBuilderFactory` is used without proper security configuration. This skill covers: complete XXE protection, parser factory caching, `createXmlValue` escape order, and replacing fragile string-based XML manipulation with DOM API.

## DocumentBuilderFactory Security Configuration

### Incomplete (vulnerable)

```java
DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
factory.setExpandEntityReferences(false); // ALONE IS NOT ENOUGH
```

`setExpandEntityReferences(false)` only prevents entity content from being inlined into the document, but the parser may still **resolve** external entities — making network requests, reading local files, or performing DNS lookups.

### Complete protection

```java
private static final DocumentBuilderFactory DOC_FACTORY;

static {
    DOC_FACTORY = DocumentBuilderFactory.newInstance();
    // Prevent entity expansion entirely
    DOC_FACTORY.setExpandEntityReferences(false);
    // Block external entities — these are the core XXE defenses
    try { DOC_FACTORY.setFeature(
        "http://xml.org/sax/features/external-general-entities", false);
    } catch (ParserConfigurationException ignored) {}
    try { DOC_FACTORY.setFeature(
        "http://xml.org/sax/features/external-parameter-entities", false);
    } catch (ParserConfigurationException ignored) {}
    // Limit entity expansion depth (mitigates Billion Laughs)
    try { DOC_FACTORY.setFeature(
        "http://javax.xml.XMLConstants/feature/secure-processing", true);
    } catch (ParserConfigurationException ignored) {}
    // Prevent XInclude-based attacks
    DOC_FACTORY.setXIncludeAware(false);

    TRANS_FACTORY = TransformerFactory.newInstance();
}
```

**Why `try/catch`**: Not all XML parsers support all features. `setFeature` can throw `ParserConfigurationException` for unsupported features. Wrapping each in `try/catch` ensures the factory is still usable even if a particular security feature isn't available.

### What NOT to add

`disallow-doctype-decl` (`http://apache.org/xml/features/disallow-doctype-decl`) blocks ALL DOCTYPE declarations, including legitimate ones. This can break parsing of well-formed XML files that use internal DTD subsets. The combination of `external-general-entities=false` and `external-parameter-entities=false` already prevents XXE without blocking legitimate DOCTYPE usage.

### Why cache the factory

`DocumentBuilderFactory.newInstance()` performs SPI classpath scanning every call (~0.5-2ms). Cache it as a `static final` field:

```java
private static final DocumentBuilderFactory DOC_FACTORY;

// DocumentBuilder is NOT thread-safe — create fresh per parse
public static DocumentBuilderFactory getDocumentBuilder() {
    return DOC_FACTORY;
}

// Usage: create a new builder for each parse
DocumentBuilder builder = DOC_FACTORY.newDocumentBuilder();
Document doc = builder.parse(source);
```

Same pattern applies to `TransformerFactory`:
```java
private static final TransformerFactory TRANS_FACTORY;
```

## createXmlValue: Correct Escape Order

### Bug: escape order matters

The `&` character MUST be escaped first. Otherwise, previously-escaped sequences containing `&` (like `&#xD;`) will be double-escaped:

```java
// WRONG — & escaped last, corrupts \r/\n escapes
s1 = s1.replace("\r", "&#xD;");  // introduces &
s1 = s1.replace("\n", "&#xA;");  // introduces &
s1 = s1.replace("&", "&amp;");   // &#xD; → &amp;#xD; ← BUG!

// Result: "a\r\nb" → "a&amp;#xD;&amp;#xA;b" instead of "a&#xD;&#xA;b"
```

### Correct order

```java
public static String createXmlValue(String s1) {
    if (s1 == null) return null;
    // & MUST be first — all other escapes may introduce &
    s1 = s1.replace("&", "&amp;");
    s1 = s1.replace("<", "&lt;");
    s1 = s1.replace(">", "&gt;");
    s1 = s1.replace("\"", "&quot;");
    s1 = s1.replace("'", "&apos;");    // often overlooked
    s1 = s1.replace("\r", "&#xD;");    // note: trailing semicolon
    s1 = s1.replace("\n", "&#xA;");
    return s1;
}
```

## appendNode: Replace String Manipulation with DOM API

### Problem: string-based XML insertion

```java
// FRAGILE — O(n²), doesn't handle CDATA/namespaces/comments
String xml = asXml(document.getDocumentElement());
int m = xml.indexOf("<" + tagName, m);
// ... manual indexOf + substring + concatenation ...
StringWriter sw = new StringWriter();
sw.write(xml.substring(0, m));
sw.write(nodeXmlString);       // inserted as raw string
sw.write(xml.substring(m));
Node n1 = asNode(sw.toString()); // re-parse entire document
```

This approach is:
- **Fragile**: breaks on CDATA sections, namespaces, comments, self-closing tags
- **Slow**: serializes → searches → inserts → re-parses the entire document
- **Memory-intensive**: creates multiple copies of the full XML string

### Solution: DOM importNode

```java
public static Document appendNode(Document sourceDocument, String nodeXmlString, String tagPath) {
    // 1. Parse the fragment
    String wrapped = "<__root__>" + nodeXmlString + "</__root__>";
    Document tmpDoc = asDocument(wrapped);
    if (tmpDoc == null) return null;

    Node newNode = tmpDoc.getDocumentElement().getFirstChild();

    // 2. Find parent by path
    Node parent = retNode(sourceDocument, tagPath);
    if (parent == null) {
        // Fallback: if path matches document element, append to root
        if (sourceDocument.getDocumentElement() != null
                && sourceDocument.getDocumentElement().getNodeName().equals(tagPath)) {
            parent = sourceDocument.getDocumentElement();
        } else {
            return null;
        }
    }

    // 3. Import and append — O(1) node operations
    Node imported = sourceDocument.importNode(newNode, true);
    parent.appendChild(imported);
    return sourceDocument;
}
```

**Key points**:
- `<__root__>` wrapper handles both fragments (`<item/>`) and full documents
- `importNode(node, true)` performs a deep copy into the target document
- `appendChild` is O(1) — no string manipulation
- Documents from different `DocumentBuilder` instances are incompatible — always `importNode` before appending

## Testing XXE Protection

```java
@Test
public void testXxeRejected() {
    String xxeXml = "<?xml version=\"1.0\"?>"
            + "<!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>"
            + "<root>&xxe;</root>";
    Document doc = UXml.asDocument(xxeXml);
    if (doc != null) {
        String text = doc.getDocumentElement().getTextContent();
        assertFalse(text.contains("root:"),
                "External entity must NOT contain file contents");
    }
}

@Test
public void testBillionLaughsRejected() {
    String bomb = "<?xml version=\"1.0\"?>"
            + "<!DOCTYPE lolz ["
            + "<!ENTITY lol \"lol\">"
            + "<!ENTITY lol2 \"&lol;&lol;...\">"  // nested 10x
            + "]>"
            + "<root>&lol4;</root>";
    Document doc = UXml.asDocument(bomb);
    if (doc != null) {
        assertTrue(doc.getDocumentElement().getTextContent().length() < 100000,
                "Billion Laughs expansion must be limited");
    }
}
```

## queryNode: Case-Insensitive Comparison Performance

```java
// Before: creates 4 temporary String objects
name.toUpperCase().trim().equals(itemName.toUpperCase().trim())

// After: 0 temporary objects
name.trim().equalsIgnoreCase(itemName.trim())
```

`toUpperCase()` allocates a new String. `equalsIgnoreCase` compares character-by-character without allocation.

## Common Pitfalls

| Issue | Symptom | Fix |
|-------|---------|-----|
| Only `setExpandEntityReferences(false)` | External entities still resolved (network/DNS calls) | Add `external-general-entities=false` and `external-parameter-entities=false` |
| `disallow-doctype-decl` on all XML | Legitimate XML files with DOCTYPE fail to parse | Remove this feature; the other two are sufficient |
| `&` escaped last in `createXmlValue` | `&#xD;` becomes `&amp;#xD;` | Escape `&` first |
| `DocumentBuilder` shared across threads | Random parse failures | Create new `DocumentBuilder` per parse from cached factory |
| `importNode` on cross-document append | `WRONG_DOCUMENT_ERR` | Always call `targetDoc.importNode(sourceNode, true)` |
| No `TransformerFactory` cache | ~1ms overhead per serialization | Cache as `static final` |
