---
name: html-regex-sanitization
description: Safe HTML tag/event/comment removal with non-greedy regex, Pattern.quote for tag names, and Matcher.appendReplacement for targeted substitution.
source: auto-skill
extracted_at: '2026-06-27T05:40:26.391Z'
---

# HTML Regex Sanitization Patterns

## Problem

HTML string manipulation with regex is fragile. Three common pitfalls cause bugs:

1. **Greedy `.*` escapes tag boundaries** — matches across multiple tags
2. **`String.replace` on capture groups replaces globally** — affects text outside target tags
3. **Unescaped tag names** — regex special chars in tag names break patterns

## Patterns

### 1. Non-greedy comments (avoid swallowing content)

```java
// WRONG — greedy .* swallows content between <!-- A --> and <!-- B -->
html.replaceAll("<!--.*-->", "");
// Input:  "<!-- a --><div>keep</div><!-- b -->"
// Result: "" (everything removed!)

// CORRECT — non-greedy .*?
html.replaceAll("<!--.*?-->", "");
// Result: "<div>keep</div>"
```

### 2. Tag-bound event matching (`[^>]*` instead of `.*`)

```java
// WRONG — .* escapes the tag and matches into following content
String regex = "<\\w+.*(on\\w+)";
// Input:  "<div onclick='x'>text</div><span onmouseover='y'>hover</span>"
// Match:  spans from "<div" all the way to "onmouseover" — destroys everything in between

// CORRECT — [^>]* stays within the tag
String regex = "<\\w+[^>]*\\b(on\\w+)";
// Match:  "<div onclick='x'" and "<span onmouseover='y'" — targeted
```

Key insight: `[^>]*` matches any char except `>`, keeping the match inside one tag.

### 3. Targeted replacement with `Matcher.appendReplacement`

```java
// WRONG — String.replace operates on the ENTIRE string, all occurrences
while (mat.find()) {
    MatchResult mr = mat.toMatchResult();
    html = html.replace(mr.group(1), "_gdx_");  // replaces ALL "onclick" in entire HTML
}

// CORRECT — Matcher.appendReplacement only replaces within the current match
StringBuilder sb = new StringBuilder();
while (mat.find()) {
    mat.appendReplacement(sb, mat.group().replace(mat.group(1), "_gdx_"));
}
mat.appendTail(sb);
return sb.toString();
```

### 4. Safe tag names with `Pattern.quote`

```java
// WRONG — tagName can contain regex special chars
String regex = "<" + tagName + "[^>]*>";  // tagName="div.x" → regex="<div.x[^>]*>"
                                          // "." matches any char!

// CORRECT
String q = Pattern.quote(tagName);
String regex = "<" + q + "[^>]*>";
```

### 5. Tag removal (paired + self-closing)

```java
String q = Pattern.quote(tagName);

// Paired: <tag>...</tag>
Pattern p1 = Pattern.compile("<" + q + "[^>]*>[^<]*</" + q + "[^>]*>", Pattern.CASE_INSENSITIVE);

// Self-closing: <tag .../>
Pattern p2 = Pattern.compile("<" + q + ".*/>", Pattern.CASE_INSENSITIVE);

// Apply both
```

## Summary

| Pitfall | Fix |
|---------|-----|
| `.*` greedy across tags | `[^>]*` stay-in-tag, `.*?` non-greedy |
| `String.replace` global side-effect | `Matcher.appendReplacement` scoped |
| Regex injection via tag name | `Pattern.quote(tagName)` |
