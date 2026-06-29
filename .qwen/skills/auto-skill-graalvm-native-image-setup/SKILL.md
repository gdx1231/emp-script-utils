---
name: graalvm-native-image-setup
description: Install and configure GraalVM JDK 21 with native-image on macOS via Homebrew for Maven/Java projects.
source: auto-skill
extracted_at: '2026-06-27T07:15:09.205Z'
---

# GraalVM Native-Image Setup (macOS)

## Prerequisites

- macOS with Homebrew installed
- Existing JDK (any version) for bootstrapping

## Installation Steps

### 1. Install GraalVM JDK 21 via Homebrew

```bash
brew install --cask graalvm/tap/graalvm-jdk21
```

This requires sudo password. Installs to:
```
/Library/Java/JavaVirtualMachines/graalvm-jdk-21/Contents/Home
```

### 2. Verify Installation

```bash
/Library/Java/JavaVirtualMachines/graalvm-jdk-21/Contents/Home/bin/java -version
# Expected: Oracle GraalVM 21.0.x
```

### 3. Check native-image Availability

**CRITICAL**: In Oracle GraalVM 21+, `native-image` is **bundled directly** — no `gu install native-image` needed. The `gu` (GraalVM Updater) tool may not even exist in `bin/`.

```bash
ls /Library/Java/JavaVirtualMachines/graalvm-jdk-21/Contents/Home/bin/native-image
# Should exist directly

/Library/Java/JavaVirtualMachines/graalvm-jdk-21/Contents/Home/bin/native-image --version
# Expected: native-image 21.0.x
```

### 4. Configure JAVA_HOME

For `zsh` (default on modern macOS), update `~/.zshrc`:

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/graalvm-jdk-21/Contents/Home
```

Or for temporary use in current shell:
```bash
export JAVA_HOME="/Library/Java/JavaVirtualMachines/graalvm-jdk-21/Contents/Home"
```

Apply changes: `source ~/.zshrc` or open a new terminal.

## Compilation Command

For a Maven project (`emp-script-utils`) using `target17/` as build directory:

```bash
export JAVA_HOME="/Library/Java/JavaVirtualMachines/graalvm-jdk-21/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

# Build classpath from compiled classes + all dependency jars
CP="target17/classes"
for jar in target17/lib/*.jar; do CP="$CP:$jar"; done

native-image \
  --no-fallback \
  -cp "$CP" \
  -o target17/ucurl \
  com.gdxsoft.easyweb.utils.UCurl
```

### Important:
- Use `-o <path>` for output, NOT trailing positional argument — native-image treats trailing args as class names
- `-H:Name=<name>` is experimental and produces a warning; prefer `-o`
- `-H:Class=<class>` is also valid but `-o` + positional class name at end is cleaner

### Expected output:
- `32-33MB` Mach-O arm64 executable
- Peak RSS: ~1.9GB during build (needs >2GB free RAM minimum)
- Build time: ~1 minute on Apple Silicon
- Only links macOS system libraries (Foundation, CoreServices, libSystem, libz) — **zero JVM dependency**

## Common Errors & Fixes

| Error | Cause | Fix |
|-------|-------|-----|
| `Main entry point class 'target17/ucurl' neither found` | Trailing path treated as class name | Use `-o target17/ucurl` then class name as positional arg |
| `NoClassDefFoundError: org/slf4j/LoggerFactory` | Dependencies not downloaded | Run `mvn package -DskipTests` first to populate `target17/lib/` |
| `java.io.IOException` uncaught in `System.out.write(buf)` | `System.out.write(byte[])` throws IOException | Wrap in try-catch |
| `-H:Name=...` experimental warning | `-H:Name` needs `-H:+UnlockExperimentalVMOptions` | Use `-o <name>` instead |

## Creating a Main Entry Point for Library Projects

When the project is a library (JAR) with no existing `main()`, create a thin CLI wrapper:

1. Place in `src/main/java/...` (main source tree, not test)
2. Parse a subset of familiar CLI flags (e.g. curl syntax: `-X`, `-d`, `-H`, `-x`, `-o`)
3. Delegate actual work to existing project classes (e.g. `UNet`)
4. For binary file output, use `downloadData()` / `postMsgAndDownload()` instead of `doGet()` / `doPost()` — the text-returning methods may not populate `getLastBuf()` correctly
5. **Auto-prepend scheme**: If the URL lacks `://` (e.g. `ip.gezz.cn`), prepend `https://` — otherwise `java.net.URI` throws "URI with undefined scheme"
6. **Quiet by default**: Only output response body to stdout. Use `--log`/`-v` flag to enable verbose request/response header logging. This keeps CLI output clean for scripts and piping.

Minimal curl-style wrapper example for `UNet`:

```java
// Supports: java UCurl [-X METHOD] [-d DATA] [-H "K: V"] [-x PROXY] [-o FILE] [--log] <url>
// Auto-prepends https:// if no scheme
public static void main(String[] args) { /* parse flags, call UNet */ }
```

## Key Gotchas

- **No `gu` tool**: Oracle GraalVM 21 removed `gu`. `native-image`, `native-image-configure`, and `native-image-inspect` are pre-installed in `bin/`.
- **Library vs Application**: native-image requires a main entry point. For library projects, consider creating a thin wrapper main class or compile as a shared library (`--shared`).
- **Reflection-heavy dependencies**: BouncyCastle, Jakarta Mail, HSQLDB, etc. need `reflect-config.json` generated via the tracing agent or `native-image-configure`.
- **1GB server constraint**: When deploying native images to memory-constrained servers, use `-H:MaxHeapSize` and `-R:MaxHeapSize` flags.
