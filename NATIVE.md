# GraalVM Native Image 编译指南

## 概述

emp-script-utils 支持通过 GraalVM Native Image 将 `UCurl` 入口类编译为原生可执行文件，无需 JVM 即可运行，启动速度显著提升，适合容器化部署和 CLI 工具分发。

**编译产物**: `target17/ucurl` — Mach-O arm64 / ELF x86-64 原生可执行文件 (~33MB)

## 环境要求

| 组件 | 版本 | 说明 |
|------|------|------|
| GraalVM JDK | 21+ | Oracle GraalVM / GraalVM Community Edition |
| native-image | 内置 | Oracle GraalVM 21 已内置，无需 `gu install` |
| macOS / Linux | — | 编译目标与编译主机同架构 |
| Xcode CLI (macOS) | — | 提供 C 编译器 (cc) |

### 安装 GraalVM (macOS)

```bash
brew install --cask graalvm/tap/graalvm-jdk21
```

安装后配置 `JAVA_HOME`:

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/graalvm-jdk-21/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH
```

验证:

```bash
$ java -version
java version "21.0.11" ... Oracle GraalVM ...

$ native-image --version
native-image 21.0.11 ...
```

## 快速开始

### 1. 编译项目

```bash
cd emp-script-utils
mvn clean package -DskipTests
```

### 2. 构建原生镜像

```bash
native-image \
  --no-fallback \
  -cp target17/classes:target17/lib/* \
  -o target17/ucurl \
  com.gdxsoft.easyweb.utils.UCurl
```

参数说明:

| 参数 | 说明 |
|------|------|
| `--no-fallback` | 禁用 JVM 回退，确保生成纯原生镜像 |
| `-cp` | classpath，包含编译输出和所有依赖 JAR |
| `-o` | 输出文件路径 |
| 最后一个参数 | 入口 main 类全限定名 |

构建时间约 60 秒 (Apple M-series)，峰值内存 ~2GB。

### 3. 运行

```bash
./target17/ucurl https://www.baidu.com
```

## UCurl 用法

```
Usage: ucurl [options] <url>

Options:
  -X METHOD        HTTP method (GET, POST, PUT, DELETE), default GET
  -d DATA          Request body (implies POST when method is GET)
  -H "Key: Value"   Add request header
  -x PROXY_URL     Set proxy (http://host:port or socks://host:port)
  -o FILE          Write response body to file
```

### 示例

```bash
# GET 请求
./target17/ucurl https://httpbin.org/get

# POST 表单
./target17/ucurl -d "name=foo&age=30" https://httpbin.org/post

# 自定义 Header
./target17/ucurl -H "Authorization: Bearer token123" https://api.example.com/data

# 下载文件
./target17/ucurl -o /tmp/page.html https://www.baidu.com

# 通过 SOCKS5 代理
./target17/ucurl -x socks://127.0.0.1:1086 https://example.com

# 通过 HTTP 代理
./target17/ucurl -x http://127.0.0.1:1087 https://example.com
```

## 构建输出解读

```
========================================================================================================================
GraalVM Native Image: Generating 'ucurl' (executable)...
========================================================================================================================
[1/8] Initializing...                     (3.3s)
[2/8] Performing analysis...             (12.9s)   6,820 reachable types
[3/8] Building universe...                (2.3s)
[4/8] Parsing methods...                  (7.1s)
[5/8] Inlining methods...                 (0.9s)
[6/8] Compiling methods...               (46.3s)  19,889 compilation units
[7/8] Laying out methods...               (2.3s)
[8/8] Creating image...                   (2.8s)  32.96MB total
------------------------------------------------------------------------------------------------------------------------
Produced artifacts:
 /path/to/target17/ucurl (executable)
========================================================================================================================
```

- **reachable types**: 静态分析可达的类型数，反映镜像中实际包含的类
- **compilation units**: 编译为原生代码的方法数
- **Peak RSS**: 构建过程中的峰值内存

## 依赖反射配置

项目依赖中以下库使用了反射、JNI 或动态类加载，GraalVM 自动检测了大部分：

| 依赖 | 反射使用 | native-image 处理 |
|------|----------|-------------------|
| BouncyCastle (bcprov) | JCE Provider 注册 | 需 `--initialize-at-build-time` 或反射配置 |
| Jakarta Mail | MIME 类型检测 | Angus 提供内置 Feature 自动注册 |
| SLF4J | LoggerFactory 绑定 | 编译时绑定 slf4j-jdk14 |
| HSQLDB | JDBC Driver | 需额外配置（UCurl 入口不使用） |

当前 `UCurl` 入口仅使用 `java.net.http.HttpClient`（JDK 内置，零反射问题），因此无需额外配置文件即可编译成功。

### 更复杂入口的反射配置

如果需要编译使用 BouncyCastle 加密、HSQLDB 数据库等的入口类，需要生成反射配置:

```bash
# 1. 运行 agent 收集反射信息
java -agentlib:native-image-agent=config-output-dir=target17/native-config \
  -cp target17/classes:target17/lib/* \
  com.gdxsoft.easyweb.utils.YourMainClass

# 2. 使用生成的配置构建
native-image \
  --no-fallback \
  -cp target17/classes:target17/lib/* \
  -H:ConfigurationFileDirectories=target17/native-config \
  -o target17/your-app \
  com.gdxsoft.easyweb.utils.YourMainClass
```

## 性能对比

| 指标 | JVM (GraalVM 21) | Native Image |
|------|------------------|--------------|
| 启动时间 | ~0.8s (含 JVM 启动) | ~0.005s |
| 内存占用 (RSS) | ~80MB+ | ~10-20MB |
| 镜像大小 | JAR 300KB + JRE ~200MB | 33MB (独立可执行文件) |
| HTTPS 请求性能 | 相同 (均使用 JDK HttpClient) | 相同 |

## 优化选项

```bash
native-image \
  --no-fallback \
  -march=native \                # 针对当前 CPU 优化指令集
  -O2 \                          # 优化级别 (默认)
  --strict-image-heap \          # 更严格的堆检查
  -H:+BuildReport \              # 生成构建报告
  -cp target17/classes:target17/lib/* \
  -o target17/ucurl \
  com.gdxsoft.easyweb.utils.UCurl
```

### PGO (Profile-Guided Optimization)

```bash
# 1. 生成 instrumented 版本
native-image --pgo-instrument -cp ... -o target17/ucurl-pgo com.gdxsoft.easyweb.utils.UCurl

# 2. 运行几次典型请求收集 profile
./target17/ucurl-pgo https://httpbin.org/get
./target17/ucurl-pgo -d "test" https://httpbin.org/post

# 3. 基于 profile 构建优化版本
native-image --pgo=default.iprof -cp ... -o target17/ucurl com.gdxsoft.easyweb.utils.UCurl
```

## 交叉编译

native-image 不支持交叉编译。若需 Linux x86-64 版本，需在 Linux 主机上执行相同步骤。

CI/CD 中可在构建矩阵中分别执行:

```yaml
# GitHub Actions 示例
strategy:
  matrix:
    os: [ubuntu-latest, macos-latest]
steps:
  - uses: graalvm/setup-graalvm@v1
    with:
      java-version: '21'
  - run: mvn package -DskipTests
  - run: native-image --no-fallback -cp target17/classes:target17/lib/* -o target17/ucurl com.gdxsoft.easyweb.utils.UCurl
```

## 常见问题

### 编译时 OOM

native-image 构建过程需要较大内存（峰值可达 2-4GB）。若内存不足:

```bash
# 限制并行度
native-image -J-Xmx4g --parallelism=4 ...
```

### ClassNotFoundException at runtime

静态分析未覆盖的类会缺失。通过 agent 收集配置后重新构建。

### UnsupportedFeatureException

某些 JDK 特性（如 JMX、JFR recording）在 native-image 中不可用，需回退到纯 JDK 实现。
