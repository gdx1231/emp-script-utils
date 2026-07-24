---
name: maven-vuln-fix
description: Query and fix Maven dependency vulnerabilities reported by GitHub Dependabot using OSV API
source: auto-skill
extracted_at: '2026-06-22T02:45:26.806Z'
---

# Fix Maven Dependency Vulnerabilities from Dependabot Alerts

When GitHub reports Dependabot vulnerabilities on a Maven project, the security page often requires authentication to view details. Use the OSV (Open Source Vulnerabilities) API to identify and fix them.

## Procedure

### 1. List current dependencies

```bash
mvn dependency:tree 2>&1 | grep -v '^\[INFO\] ---' | grep -v 'BUILD'
```

### 2. Query OSV API for each dependency

For each dependency in `pom.xml`, query the OSV API:

```bash
curl -s -X POST https://api.osv.dev/v1/query \
  -H "Content-Type: application/json" \
  -d '{"package":{"name":"<groupId>:<artifactId>","ecosystem":"Maven"},"version":"<version>"}' \
  | python3 -m json.tool
```

- An empty `{}` response means no known vulnerabilities.
- A response with `"vulns"` array contains CVE details, affected versions, and the `"fixed"` version.

### 3. Batch-query all dependencies

```bash
for pkg in "groupId:artifactId:version" ...; do
  name=$(echo $pkg | cut -d: -f1,2)
  ver=$(echo $pkg | cut -d: -f3)
  result=$(curl -s -X POST https://api.osv.dev/v1/query \
    -H "Content-Type: application/json" \
    -d "{\"package\":{\"name\":\"$name\",\"ecosystem\":\"Maven\"},\"version\":\"$ver\"}")
  if [ "$result" != "{}" ]; then
    echo "=== VULNERABLE: $name:$ver ==="
    echo "$result" | python3 -m json.tool | head -40
  fi
done
```

### 4. Update pom.xml to fixed versions

Edit `pom.xml` to bump each vulnerable dependency to its `"fixed"` version from the OSV response.

### 5. Compile and test

```bash
mvn clean compile
mvn test
```

## Common pitfall: alimaven HTTP mirror 403

If `~/.m2/settings.xml` uses `http://maven.aliyun.com/nexus/content/groups/public` (HTTP, not HTTPS), new artifacts may return 403 Forbidden. Fix by changing the mirror URL to HTTPS:

```xml
<url>https://maven.aliyun.com/nexus/content/groups/public</url>
```

Or temporarily use a custom settings file:

```bash
mvn clean compile -s /path/to/https-settings.xml
```

## Key OSV API fields

| Field | Meaning |
|-------|---------|
| `aliases` | CVE identifiers (e.g., `CVE-2025-7962`) |
| `summary` | Short description |
| `database_specific.severity` | `MODERATE`, `HIGH`, `CRITICAL` |
| `affected[].ranges[].events` | `introduced` and `fixed` version pairs |
