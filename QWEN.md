# emp-script-utils 项目上下文

## 项目概述

**emp-script-utils** (Easy Web Application Utils) 是一个 Java 工具类库，提供多种实用工具用于 Web 应用开发。

- **Group ID**: `com.gdxsoft.easyweb`
- **Artifact ID**: `emp-script-utils`
- **当前版本**: `1.1.10`
- **Java 版本**: 1.8
- **许可证**: MIT License
- **官方网站**: https://www.gdxsoft.com
- **GitHub**: https://github.com/gdx1231/emp-script-utils

## 主要功能模块

### 加密/解密工具
| 类名 | 功能描述 |
|------|----------|
| `UAes` | AES 加密/解密 (支持 GCM/CCM/CBC/ECB/CFB/OFB/CTR 模式，128/192/256 位) |
| `UDes` | DES 加密/解密工具 |
| `URsa` | RSA 非对称加密工具 |
| `UArgon2` | Argon2 密码哈希函数 |
| `UDigest` | 消息摘要工具 (SHA, MD5 等) |
| `USign` | 数字签名工具 |

### 邮件工具
| 类名 | 功能描述 |
|------|----------|
| `UMail` | 邮件发送高层封装 |
| `SendMail` | 邮件发送核心类 |
| `SmtpCfg` / `SmtpCfgs` | SMTP 配置管理 |
| `DKIM*` | DKIM 签名支持 (DKIMCfg, DKIMSigner, DKIMMessage 等) |
| `MailAuth` | 邮件认证 |
| `MailDecode` | 邮件解码 |

### 文件处理
| 类名 | 功能描述 |
|------|----------|
| `UFile` | 文件操作工具 |
| `UFileCheck` | 文件检查工具 |
| `UFileFilter` | 文件过滤器 |
| `UPath` | 路径处理工具 |

### 网络工具
| 类名 | 功能描述 |
|------|----------|
| `UNet` | 网络操作工具 |
| `UUrl` | URL 处理工具 |
| `UDns` | DNS 查询工具 |

### 数据格式化与转换
| 类名 | 功能描述 |
|------|----------|
| `UFormat` | 数据格式化工具 |
| `UConvert` | 数据类型转换 (含 Base64) |
| `UJSon` | JSON 处理工具 |
| `UXml` | XML 处理工具 |

### 图像处理
| 类名 | 功能描述 |
|------|----------|
| `UImages` | 图像处理工具 (基于 Thumbnailator) |
| `UQRCode` | 二维码生成/解析 (基于 ZXing) |

### 其他工具
| 类名 | 功能描述 |
|------|----------|
| `Utils` | 通用工具类 (字符串、日期、随机数等) |
| `UHtml` | HTML 处理工具 |
| `UPinYin` | 拼音转换工具 |
| `UCookies` | Cookie 处理工具 |
| `UCheckerIn` | 签到/检查工具 |
| `ULogic` | 逻辑运算工具 |
| `IPageSplit` | 分页接口 |
| `IUSymmetricEncyrpt` | 对称加密接口 |

### 自定义数据类型
- `UInt16` - 16 位无符号整数
- `UInt32` - 32 位无符号整数
- `UInt64` - 64 位无符号整数

### 自定义集合类 (msnet 包)
- `MStr` - 可变字符串
- `MList` / `MListStr` - 列表
- `MTable` / `MTableStr` - 表格数据结构

## 项目结构

```
emp-script-utils/
├── src/
│   ├── main/
│   │   ├── java/com/gdxsoft/easyweb/
│   │   │   ├── utils/          # 主要工具类
│   │   │   ├── utils/Mail/     # 邮件相关类
│   │   │   ├── utils/msnet/    # 自定义集合类
│   │   │   ├── utils/types/    # 自定义数据类型
│   │   │   └── conf/           # 配置类
│   │   └── resources/
│   └── test/
│       ├── java/test/java/     # 测试类 (Test*.java)
│       └── resources/
├── pom.xml
├── README.md
└── LICENSE
```

## 构建与运行

### 环境要求
- JDK 1.8+
- Maven 3.x+

### 构建命令
```bash
# 编译项目
mvn clean compile

# 打包 (生成 JAR)
mvn clean package

# 运行测试
mvn test

# 生成 Javadoc
mvn javadoc:javadoc

# 发布到 Maven 中央仓库 (需要 GPG 签名)
mvn clean deploy -P release
```

### 构建输出
- `target/emp-script-utils-<version>.jar` - 主 JAR 文件
- `target/emp-script-utils-<version>-javadoc.jar` - Javadoc JAR
- `target/emp-script-utils-<version>-sources.jar` - 源码 JAR (release 模式)
- `target/lib/` - 依赖库目录

## Maven 依赖

### 主要依赖
| 依赖 | 版本 | 用途 |
|------|------|------|
| commons-io | 2.20.0 | IO 工具 |
| commons-lang3 | 3.18.0 | 语言工具 |
| org.json | 20250517 | JSON 处理 |
| com.google.zxing/core | 3.5.3 | 二维码 |
| net.coobird/thumbnailator | 0.4.19 | 图像处理 |
| org.slf4j | 1.7.36 | 日志 |
| org.apache.commons/commons-exec | 1.3 | 进程执行 |
| com.sun.mail/jakarta.mail | 1.6.6 | 邮件 |
| org.apache.httpcomponents/httpclient | 4.5.13 | HTTP 客户端 |
| commons-codec | 1.15 | 编解码 |
| org.bouncycastle/bcprov-jdk18on | 1.78.1 | 加密 provider |
| org.hsqldb/hsqldb | 2.7.1 | 嵌入式数据库 |

### 测试依赖
- junit-jupiter-api 5.7.1
- slf4j-jdk14 1.7.32

### 提供的依赖 (provided)
- javax.servlet-api 4.0.1

## 使用示例

### Maven 依赖配置
```xml
<dependency>
  <groupId>com.gdxsoft.easyweb</groupId>
  <artifactId>emp-script-utils</artifactId>
  <version>1.1.10</version>
</dependency>
```

### AES 加密示例
```java
// 初始化默认密钥
UAes.initDefaultKey("my-secret-key-123", "my-iv-value-123");

// 加密
String encrypted = UAes.defaultEncrypt("Hello World");

// 解密
String decrypted = UAes.defaultDecrypt(encrypted);
```

### 发送邮件示例
```java
String result = UMail.sendHtmlMail(
    "from@example.com",           // 发件人
    "发件人姓名",                   // 发件人姓名
    "to@example.com",             // 收件人
    "收件人姓名",                   // 收件人姓名
    "邮件主题",                     // 主题
    "<h1>邮件内容</h1>",           // HTML 内容
    null,                         // 附件
    "utf-8"                       // 字符集
);
```

### 通用工具示例
```java
// 生成 UUID
String guid = Utils.getGuid();

// MD5 摘要
String md5 = Utils.md5("text");

// 日期格式化
String dateStr = Utils.getDateTimeString(new Date());

// 随机字符串
String random = Utils.randomStr(16);
```

## 开发约定

### 代码风格
- 使用 Java 8 语法
- 遵循 Java 命名规范
- 工具类采用静态方法为主 (如 `Utils`, `UFile`)
- 部分工具类支持实例化配置 (如 `UAes`, `URsa`)

### 测试实践
- 测试类位于 `src/test/java/test/java/`
- 使用 JUnit 5 (`junit-jupiter-api`)
- 测试类命名：`Test<功能>.java`
- 基础测试类：`TestBase` (提供辅助方法)

### 日志
- 使用 SLF4J 作为日志门面
- 测试时使用 `slf4j-jdk14` 实现

### Git 忽略文件
- 编译输出：`target/`, `*.class`, `build/`
- IDE 配置：`.idea/`, `.vscode/`, `.settings/`, `*.iml`
- 临时文件：`*.bak`, `*.log`, `*.properties`
- 系统文件：`.DS_Store`

## 注意事项

1. **AES 密钥长度**: Java 默认限制 AES 密钥为 128 位，如需 192/256 位需安装 JCE Unlimited Strength Policy Files
2. **BouncyCastle**: 加密功能依赖 BouncyCastle Provider，库中已自动注册
3. **邮件配置**: SMTP 配置通过 `SmtpCfgs` 管理，支持多配置和 DKIM 签名
4. **字符编码**: 默认使用 UTF-8 编码

## 相关资源

- [Maven Central](https://search.maven.org/search?q=com.gdxsoft.easyweb)
- [Sonatype OSSRH](https://issues.sonatype.org/browse/OSSRH-65277)
