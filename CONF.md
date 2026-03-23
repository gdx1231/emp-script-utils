# 配置文件使用指南

## 概述

emp-script-utils 使用 XML 配置文件来管理各种系统参数。核心配置类位于 `com.gdxsoft.easyweb.conf` 包和相关工具类中。

## 配置文件加载机制

### 配置文件搜索顺序

系统按以下顺序加载配置文件：

1. **用户自定义配置文件** - 通过 `UPath.CONF_NAME` 指定
2. **ewa_conf.xml** - 从 classpath 加载
3. **ewa_conf_console.xml** - 从 classpath 加载（控制台应用）

### 配置加载类

**UPath** 是核心配置加载类，负责：
- 加载配置文件
- 初始化路径配置
- 初始化全局变量
- 初始化 SMTP 参数
- 初始化调试设置

```java
// 手动重新加载配置
UPath.reloadConf();

// 获取配置文件 Document 对象
Document doc = UPath.getCfgXmlDoc();

// 检查配置是否已改变
long propTime = UPath.getPropTime();
```

## 配置类说明

### 1. ConfImageMagick - ImageMagick 路径配置

用于配置 ImageMagick 可执行文件路径，支持图像处理功能。

#### XML 配置示例

```xml
<!-- 新方式 -->
<imageMagick path="c:/Program Files/ImageMagick/" />

<!-- 旧方式（兼容） -->
<path name="cvt_ImageMagick_Home" value="E:\Program Files\ImageMagick-7.0.8-Q16\" />
```

#### Java 使用示例

```java
import com.gdxsoft.easyweb.conf.ConfImageMagick;

// 获取实例
ConfImageMagick conf = ConfImageMagick.getInstance();

// 获取 ImageMagick 路径
String path = conf.getPath();

// 在 UImages 中使用
// UImages 内部会自动调用 ConfImageMagick.getInstance()
```

### 2. SmtpCfg - SMTP 服务器配置

表示单个 SMTP 服务器配置。

#### 属性说明

| 属性 | 类型 | 说明 |
|------|------|------|
| name | String | 配置名称（自动生成） |
| host | String | SMTP 服务器主机 |
| user | String | SMTP 用户名 |
| password | String | SMTP 密码 |
| port | int | SMTP 端口（默认 25） |
| ssl | boolean | 是否使用 SSL |
| startTls | boolean | 是否使用 STARTTLS |

### 3. SmtpCfgs - SMTP 配置管理

管理多个 SMTP 配置，支持根据发件人/收件人自动选择 SMTP 服务器。

#### XML 配置示例

```xml
<!-- SMTP 配置 -->
<smtp host="smtp.gmail.com" port="587" user="user@gmail.com" pwd="password" starttls="true">
    <!-- 发件人地址配置 -->
    <from email="@gdxsoft.com" />
    <from email="guolei@sina.com" />
    
    <!-- 收件人地址配置 -->
    <to email="@163.com" />
    <to email="support@gdxsoft.com" />
</smtp>

<smtp host="smtp.qq.com" port="465" user="user@qq.com" pwd="password" ssl="true">
    <from email="@qq.com" />
    <to email="@qq.com" />
</smtp>

<!-- DKIM 签名配置 -->
<dkim domain="gdxsoft.com" select="default" key="/path/to/private.key.der" />
```

#### Java 使用示例

```java
import com.gdxsoft.easyweb.utils.Mail.SmtpCfg;
import com.gdxsoft.easyweb.utils.Mail.SmtpCfgs;
import com.gdxsoft.easyweb.utils.Mail.DKIMCfg;
import javax.mail.Session;

// 获取默认 SMTP 配置
SmtpCfg defaultCfg = SmtpCfgs.getDefaultSmtpCfg();

// 根据发件人获取 SMTP 配置
List<SmtpCfg> fromCfgs = SmtpCfgs.getSmtpCfgByFromEmail("user@gdxsoft.com");
List<SmtpCfg> fromDomainCfgs = SmtpCfgs.getSmtpCfgByFromDomain("gdxsoft.com");

// 根据收件人获取 SMTP 配置
List<SmtpCfg> toCfgs = SmtpCfgs.getSmtpCfgByToEmail("recipient@163.com");
List<SmtpCfg> toDomainCfgs = SmtpCfgs.getSmtpCfgByToDomain("163.com");

// 创建邮件会话
Session session = SmtpCfgs.createMailSession(defaultCfg);

// 或者直接使用参数创建
Session session = SmtpCfgs.createMailSession(
    "smtp.example.com",  // host
    "user",              // user
    "password",          // password
    587,                 // port
    false,               // ssl
    true                 // startTls
);

// 获取 DKIM 配置
DKIMCfg dkimCfg = SmtpCfgs.getDkim("gdxsoft.com");
```

#### SMTP 选择优先级

当调用 `SmtpCfgs.getSmtpCfg()` 时，按以下优先级选择 SMTP 配置：

1. **发件人邮箱匹配** (优先级 0) - 精确匹配 from 邮箱地址
2. **发件人域名匹配** (优先级 1) - 匹配 from 邮箱域名
3. **收件人邮箱匹配** (优先级 2) - 匹配 to 邮箱地址
4. **收件人域名匹配** (优先级 3) - 匹配 to 邮箱域名
5. **默认配置** - 最后一个配置或标记为 default 的配置

### 4. DKIMCfg - DKIM 签名配置

用于配置邮件 DKIM 签名。

#### Java 使用示例

```java
import com.gdxsoft.easyweb.utils.Mail.DKIMCfg;

// 创建 DKIM 配置
DKIMCfg cfg = new DKIMCfg();
cfg.setDomain("gdxsoft.com");
cfg.setSelect("default");
cfg.setPrivateKeyPath("/path/to/private.key.der");
cfg.setDkim(true);

// 获取属性
String domain = cfg.getDomain();
String select = cfg.getSelect();
String keyPath = cfg.getPrivateKeyPath();
```

#### DKIM 密钥生成

```bash
# 将 PEM 格式转换为 DER 格式（JavaMail 需要）
openssl pkcs8 -topk8 -nocrypt -in private.key.pem -out private.key.der -outform der
```

## 完整配置文件示例

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config>
    <!-- 路径配置 -->
    <paths>
        <path name="config_path" value="/path/to/config/" />
        <path name="script_path" value="/path/to/script/" />
        <path name="group_path" value="/path/to/group/" />
        <path name="cached_path" value="/path/to/cache/" />
        
        <!-- 转换工具路径 -->
        <path name="cvt_ImageMagick_Home" value="C:/Program Files/ImageMagick/" />
        <path name="cvt_office_home" value="C:/Program Files/LibreOffice/" />
        
        <!-- 上传路径 -->
        <path name="img_tmp_path" value="/path/to/uploads/" />
        <path name="img_tmp_path_url" value="https://cdn.example.com/uploads/" />
    </paths>

    <!-- SMTP 配置 -->
    <smtp host="smtp.gmail.com" port="587" user="user@gmail.com" pwd="app-password" starttls="true" default="true">
        <from email="@gdxsoft.com" />
        <from email="noreply@example.com" />
        <to email="@gdxsoft.com" />
    </smtp>

    <smtp host="smtp.qq.com" port="465" user="user@qq.com" pwd="password" ssl="true">
        <from email="@qq.com" />
        <to email="@qq.com" />
    </smtp>

    <!-- DKIM 配置 -->
    <dkim domain="gdxsoft.com" select="default" key="/path/to/gdxsoft.key.der" />
    <dkim domain="example.com" select="mail" key="/path/to/example.key.der" />

    <!-- 全局变量 -->
    <requestValuesGlobal>
        <rv name="rv_ewa_style_path" value="/EmpScriptV2/" />
        <rv name="APP_NAME" value="My Application" />
    </requestValuesGlobal>

    <!-- RequestValue 类型定义 -->
    <requestValueType Name="USR_ID, G_ADM_ID" Type="int" />
    <requestValueType Name="TOKEN" Type="string" />

    <!-- 调试配置 -->
    <debug ips="127.0.0.1,192.168.1.100" sqlout="true" />

    <!-- 合法域名配置（防止跨域攻击） -->
    <validDomains Value="gdxsoft.com,example.com,*.gdxsoft.com" Host="https://www.gdxsoft.com" />

    <!-- 缓存配置 -->
    <cfgCacheMethod Value="memory" />
    <!-- 或 -->
    <!-- <cfgCacheMethod Value="sqlcached" /> -->

    <!-- 自定义参数 -->
    <para Name="MAX_UPLOAD_SIZE" Value="10485760" />
    <para Name="ALLOWED_EXTENSIONS" Value="jpg,png,gif,pdf" />
</config>
```

## Java 使用示例

### 获取配置值

```java
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.conf.ConfImageMagick;

// 获取路径配置
String configPath = UPath.getConfigPath();
String scriptPath = UPath.getScriptPath();
String uploadPath = UPath.getPATH_UPLOAD();
String uploadUrl = UPath.getPATH_UPLOAD_URL();

// 获取 ImageMagick 路径
ConfImageMagick imgConf = ConfImageMagick.getInstance();
String magickPath = imgConf.getPath();

// 获取全局变量
Map<String, String> globals = UPath.getRV_GLOBALS();
String ewaPath = globals.get("rv_ewa_style_path");

// 获取自定义参数
String maxUpload = UPath.getInitPara("MAX_UPLOAD_SIZE");

// 获取调试 IP 列表
MTableStr debugIps = UPath.getDebugIps();

// 检查是否为调试 SQL 模式
boolean debugSql = UPath.isDebugSql();

// 检查域名是否合法
boolean isValid = UPath.checkIsValidDomain("gdxsoft.com");
```

### 邮件发送示例

```java
import com.gdxsoft.easyweb.utils.UMail;
import com.gdxsoft.easyweb.utils.Mail.SendMail;
import com.gdxsoft.easyweb.utils.Mail.SmtpCfg;
import com.gdxsoft.easyweb.utils.Mail.SmtpCfgs;
import com.gdxsoft.easyweb.utils.Mail.DKIMCfg;

// 方式 1：使用高层封装
String result = UMail.sendHtmlMail(
    "from@gdxsoft.com",
    "发件人",
    "to@example.com",
    "收件人",
    "邮件主题",
    "<h1>邮件内容</h1>",
    null,  // 附件
    "utf-8"
);

// 方式 2：使用 SendMail 类
SendMail sendMail = new SendMail();
sendMail.setFrom("from@gdxsoft.com", "发件人")
        .addTo("to@example.com", "收件人")
        .setSubject("邮件主题")
        .setHtmlContent("<h1>邮件内容</h1>");

// 设置 DKIM
DKIMCfg dkimCfg = SmtpCfgs.getDkim("gdxsoft.com");
if (dkimCfg != null) {
    sendMail.setDkim(dkimCfg);
}

// 获取 SMTP 配置并发送
SmtpCfg smtpCfg = SmtpCfgs.getSmtpCfg(sendMail);
Session session = SmtpCfgs.createMailSession(smtpCfg);
sendMail.send(session);
```

## 配置热加载

配置文件支持热加载，系统会每 60 秒检查配置文件是否改变：

```java
// 检查配置是否改变
long propTime = UPath.getPropTime();

// 手动重新加载配置
UPath.reloadConf();

// 设置检查间隔（毫秒）
UPath.CHK_DURATION = 30000; // 30 秒
```

## 注意事项

1. **配置文件编码**: 必须使用 UTF-8 编码
2. **路径分隔符**: 路径值会自动添加 `/` 或 `\` 后缀
3. **DKIM 密钥格式**: 必须使用 DER 格式，而非 PEM
4. **SMTP SSL**: 端口 465 默认启用 SSL
5. **配置缓存**: 默认为 memory 模式，可配置为 sqlcached
6. **域名检查**: 使用 `validDomains` 配置防止跨域攻击

## 相关类

| 类名 | 包路径 | 说明 |
|------|--------|------|
| `ConfImageMagick` | `com.gdxsoft.easyweb.conf` | ImageMagick 配置 |
| `UPath` | `com.gdxsoft.easyweb.utils` | 核心配置加载类 |
| `SmtpCfg` | `com.gdxsoft.easyweb.utils.Mail` | SMTP 配置 |
| `SmtpCfgs` | `com.gdxsoft.easyweb.utils.Mail` | SMTP 配置管理 |
| `DKIMCfg` | `com.gdxsoft.easyweb.utils.Mail` | DKIM 配置 |
| `UXml` | `com.gdxsoft.easyweb.utils` | XML 工具类 |

---

# ewa_conf.xml 详细配置说明

## 配置文件结构

`ewa_conf.xml` 是 emp-script-utils 的核心配置文件，采用 XML 格式，包含以下主要配置节点：

```
<config>
├── <path>              # 路径配置
├── <imageMagick>       # ImageMagick 配置
├── <smtp>              # SMTP 邮件服务器配置
├── <dkim>              # DKIM 签名配置
├── <requestValuesGlobal>  # 全局变量配置
├── <requestValueType>     # 变量类型定义
├── <debug>             # 调试配置
├── <validDomains>      # 合法域名配置
├── <cfgCacheMethod>    # 缓存模式配置
├── <para>              # 自定义参数
├── <workflow>          # 工作流配置
└── <databases>         # 数据库连接池配置
</config>
```

## 配置节点详解

### 1. `<path>` - 路径配置

用于定义系统各种路径，支持多个 `<path>` 节点。

#### 属性说明

| 属性 | 必填 | 说明 | 示例值 |
|------|------|------|--------|
| name | 是 | 路径名称（大小写不敏感） | `config_path` |
| value | 是 | 路径值 | `/var/config/` |

#### 支持的路径名称

| name 值 | 说明 | 对应 Java 方法 |
|---------|------|---------------|
| `config_path` | 系统配置目录 | `UPath.getConfigPath()` |
| `script_path` | 系统描述目录 | `UPath.getScriptPath()` |
| `group_path` | 组件生产/导入目录 | `UPath.getGroupPath()` |
| `cached_path` | Cache 文件目录 | `UPath.getCachedPath()` |
| `cvt_ImageMagick_Home` | ImageMagick 可执行目录 | `UPath.getCVT_IMAGEMAGICK_HOME()` |
| `cvt_office_home` | OpenOffice 转换目录 | `UPath.getCVT_OPENOFFICE_HOME()` |
| `cvt_swftool_Home` | SWFTool 转换目录 | `UPath.getCVT_SWFTOOL_HOME()` |
| `img_tmp_path` | 图片缩略图保存物理路径 | `UPath.getPATH_UPLOAD()` |
| `img_tmp_path_url` | 图片缩略图 URL 路径 | `UPath.getPATH_UPLOAD_URL()` |

#### 配置示例

```xml
<path name="config_path" value="/opt/ewa/config/" />
<path name="script_path" value="/opt/ewa/scripts/" />
<path name="group_path" value="/opt/ewa/groups/" />
<path name="cached_path" value="/opt/ewa/cache/" />

<!-- 转换工具路径 -->
<path name="cvt_ImageMagick_Home" value="C:/Program Files/ImageMagick-7.0.8-Q16/" />
<path name="cvt_office_home" value="C:/Program Files/LibreOffice/" />
<path name="cvt_swftool_Home" value="C:/Program Files/SWFTool/" />

<!-- 上传路径配置 -->
<path name="img_tmp_path" value="/data/uploads/" />
<path name="img_tmp_path_url" value="https://cdn.example.com/uploads/" />
```

#### 注意事项

- 路径值会自动添加 `/` 或 `\` 后缀
- `img_tmp_path` 和 `img_tmp_path_url` 需要配合使用
- 路径支持绝对路径和相对路径

---

### 2. `<imageMagick>` - ImageMagick 专用配置

专门用于配置 ImageMagick 路径（新方式）。

#### 属性说明

| 属性 | 必填 | 说明 |
|------|------|------|
| path | 是 | ImageMagick 安装目录 |

#### 配置示例

```xml
<imageMagick path="C:/Program Files/ImageMagick/" />
```

#### Java 调用

```java
ConfImageMagick conf = ConfImageMagick.getInstance();
String path = conf.getPath();
```

---

### 3. `<smtp>` - SMTP 邮件服务器配置

配置邮件发送服务器，支持多个 SMTP 服务器和智能路由。

#### 属性说明

| 属性 | 必填 | 默认值 | 说明 |
|------|------|--------|------|
| host | 是 | - | SMTP 服务器主机名或 IP |
| ip | 否 | - | SMTP 服务器 IP（同 host，兼容用） |
| port | 否 | 25 | SMTP 端口 |
| user | 否 | - | SMTP 用户名 |
| pwd | 否 | - | SMTP 密码 |
| ssl | 否 | 自动 | 是否启用 SSL（端口 465 时默认为 true） |
| starttls | 否 | false | 是否启用 STARTTLS |
| default | 否 | false | 是否为默认 SMTP 服务器 |

#### 子节点

| 子节点 | 说明 | 属性 |
|--------|------|------|
| `<from>` | 发件人地址配置 | `email` - 邮箱地址或域名 |
| `<to>` | 收件人地址配置 | `email` - 邮箱地址或域名 |

#### 配置示例

```xml
<!-- Gmail 配置（使用 STARTTLS） -->
<smtp host="smtp.gmail.com" port="587" user="user@gmail.com" pwd="app-password" starttls="true" default="true">
    <from email="@gdxsoft.com" />
    <from email="noreply@example.com" />
    <to email="@gdxsoft.com" />
    <to email="@163.com" />
</smtp>

<!-- QQ 邮箱配置（使用 SSL） -->
<smtp host="smtp.qq.com" port="465" user="user@qq.com" pwd="authorization-code" ssl="true">
    <from email="@qq.com" />
    <to email="@qq.com" />
</smtp>

<!-- 企业邮箱配置 -->
<smtp host="mail.company.com" port="25" user="system@company.com" pwd="password">
    <from email="@company.com" />
    <from email="admin@company.com" />
</smtp>
```

#### 地址匹配规则

- **精确邮箱匹配**: `email="user@example.com"` - 仅匹配指定邮箱
- **域名匹配**: `email="@example.com"` - 匹配该域名下所有邮箱

#### SMTP 路由优先级

当发送邮件时，系统按以下优先级选择 SMTP 服务器：

```
优先级 0: 发件人邮箱精确匹配 (FROM_EMAIL_MAP)
    ↓
优先级 1: 发件人域名匹配 (FROM_DOMAIN_MAP)
    ↓
优先级 2: 收件人邮箱精确匹配 (TO_EMAIL_MAP)
    ↓
优先级 3: 收件人域名匹配 (TO_DOMAIN_MAP)
    ↓
默认：标记为 default="true" 或最后一个配置
```

---

### 4. `<dkim>` - DKIM 签名配置

配置邮件 DKIM 签名，增强邮件可信度。

#### 属性说明

| 属性 | 必填 | 说明 | 旧版本属性名 |
|------|------|------|-------------|
| domain | 是 | DKIM 签名域名 | `dkimdomain` |
| select | 是 | DKIM 选择器 | `dkimselect` |
| key | 是 | 私钥文件路径（DER 格式） | `dkimkey` |

#### 配置示例

```xml
<dkim domain="gdxsoft.com" select="default" key="/etc/dkim/gdxsoft.key.der" />
<dkim domain="example.com" select="mail" key="/etc/dkim/example.key.der" />
```

#### DKIM 密钥生成步骤

```bash
# 1. 生成 RSA 密钥对（使用 openssl）
openssl genrsa -out private.key.pem 2048

# 2. 提取公钥（用于 DNS TXT 记录）
openssl rsa -in private.key.pem -pubout -out public.key.pem

# 3. 将 PEM 转换为 DER 格式（JavaMail 需要）
openssl pkcs8 -topk8 -nocrypt -in private.key.pem -out private.key.der -outform der
```

#### DNS TXT 记录配置

在域名 DNS 中添加 TXT 记录：
- 主机名：`default._domainkey.gdxsoft.com`
- 记录值：`v=DKIM1; k=rsa; p=MIIBIjANBgkqh...`（公钥内容）

---

### 5. `<requestValuesGlobal>` - 全局变量配置

定义全局可用的请求值变量。

#### 子节点属性

| 属性 | 必填 | 说明 |
|------|------|------|
| name | 是 | 变量名（会自动转换为大写） |
| value | 是 | 变量值 |

#### 配置示例

```xml
<requestValuesGlobal>
    <rv name="rv_ewa_style_path" value="/EmpScriptV2/" />
    <rv name="APP_NAME" value="企业应用系统" />
    <rv name="APP_VERSION" value="2.0.1" />
    <rv name="COMPANY_NAME" value="某某公司" />
</requestValuesGlobal>
```

#### Java 调用

```java
Map<String, String> globals = UPath.getRV_GLOBALS();
String ewaPath = globals.get("RV_EWA_STYLE_PATH");  // 注意：键名会自动转大写
String appName = globals.get("APP_NAME");
```

#### 内置变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `RV_EWA_STYLE_PATH` | EWA 静态文件路径 | `/EmpScriptV2` |

---

### 6. `<requestValueType>` - 变量类型定义

定义 RequestValue 变量的数据类型。

#### 属性说明

| 属性 | 必填 | 默认值 | 说明 |
|------|------|--------|------|
| Name | 是 | - | 变量名列表，逗号分隔 |
| Type | 否 | int | 变量类型（int/string/long/double/boolean） |

#### 配置示例

```xml
<requestValueType Name="USR_ID, G_ADM_ID, GRP_ID" Type="int" />
<requestValueType Name="ENQ_ID, ENQ_JNY_ID" Type="long" />
<requestValueType Name="TOKEN, SESSION_ID" Type="string" />
<requestValueType Name="IS_ADMIN, HAS_PERMISSION" Type="boolean" />
```

---

### 7. `<debug>` - 调试配置

配置调试相关的参数。

#### 属性说明

| 属性 | 必填 | 默认值 | 说明 |
|------|------|--------|------|
| ips | 否 | - | 允许调试的 IP 地址列表，逗号分隔 |
| sqlout | 否 | false | 是否输出 SQL 执行日志 |

#### 配置示例

```xml
<debug ips="127.0.0.1,192.168.1.100,10.0.0.50" sqlout="true" />
```

#### Java 调用

```java
// 获取调试 IP 列表
MTableStr debugIps = UPath.getDebugIps();

// 检查是否为调试 SQL 模式
boolean debugSql = UPath.isDebugSql();
```

---

### 8. `<validDomains>` - 合法域名配置

配置合法的域名列表，用于防止跨域攻击。

#### 属性说明

| 属性 | 必填 | 说明 |
|------|------|------|
| Value | 是 | 合法域名列表，逗号分隔，支持 `*.domain.com` 格式 |
| Host | 否 | 组合使用的主机地址 |

#### 配置示例

```xml
<validDomains Value="gdxsoft.com,example.com,*.gdxsoft.com,*.example.com" Host="https://www.gdxsoft.com" />
```

#### Java 调用

```java
// 检查域名是否合法
boolean isValid = UPath.checkIsValidDomain("test.gdxsoft.com");

// 获取所有合法域名
MTableStr validDomains = UPath.getVALID_DOMAINS();
```

---

### 9. `<cfgCacheMethod>` - 配置缓存模式

配置系统配置的缓存方式。

#### 属性说明

| 属性 | 必填 | 默认值 | 可选值 | 说明 |
|------|------|--------|--------|------|
| Value | 是 | memory | memory/sqlcached | 缓存方式 |

#### 配置示例

```xml
<!-- 使用内存缓存（默认） -->
<cfgCacheMethod Value="memory" />

<!-- 使用 SQL 缓存（需要数据库支持） -->
<cfgCacheMethod Value="sqlcached" />
```

---

### 10. `<para>` - 自定义参数

定义用户自定义的初始化参数。

#### 属性说明

| 属性 | 必填 | 说明 |
|------|------|------|
| Name | 是 | 参数名（会自动转换为大写） |
| Value | 是 | 参数值 |

#### 配置示例

```xml
<para Name="MAX_UPLOAD_SIZE" Value="10485760" />
<para Name="ALLOWED_EXTENSIONS" Value="jpg,png,gif,pdf,doc,docx" />
<para Name="PAGE_SIZE" Value="20" />
<para Name="SESSION_TIMEOUT" Value="1800" />
<para Name="ENABLE_LOG" Value="true" />
```

#### Java 调用

```java
// 获取自定义参数（大小写不敏感）
String maxUpload = UPath.getInitPara("MAX_UPLOAD_SIZE");
String extensions = UPath.getInitPara("allowed_extensions");
```

---

### 11. `<workflow>` - 工作流配置

配置工作流相关参数。

#### 配置示例

```xml
<workflow>
    <engine class="com.gdxsoft.easyweb.workflow.DefaultEngine" />
    <configPath>/opt/ewa/workflow/config.xml</configPath>
</workflow>
```

#### Java 调用

```java
String workflowXml = UPath.getWFXML();
```

---

### 12. `<databases>` - 数据库连接池配置

配置数据库连接池。

#### 配置示例

```xml
<databases>
    <database name="main" type="mysql">
        <driver>com.mysql.cj.jdbc.Driver</driver>
        <url>jdbc:mysql://localhost:3306/mydb?useSSL=false&amp;serverTimezone=UTC</url>
        <user>root</user>
        <password>password</password>
        <maxConnections>50</maxConnections>
        <minConnections>5</minConnections>
    </database>
    <database name="backup" type="oracle">
        <driver>oracle.jdbc.OracleDriver</driver>
        <url>jdbc:oracle:thin:@localhost:1521:ORCL</url>
        <user>system</user>
        <password>password</password>
    </database>
</databases>
```

#### Java 调用

```java
String databaseXml = UPath.getDATABASEXML();
```

---

## 完整配置示例

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config>
    <!-- ==================== 路径配置 ==================== -->
    <path name="config_path" value="/opt/ewa/config/" />
    <path name="script_path" value="/opt/ewa/scripts/" />
    <path name="group_path" value="/opt/ewa/groups/" />
    <path name="cached_path" value="/opt/ewa/cache/" />
    
    <!-- 转换工具路径 -->
    <path name="cvt_ImageMagick_Home" value="/usr/local/ImageMagick/" />
    <path name="cvt_office_home" value="/opt/libreoffice/" />
    
    <!-- 上传路径 -->
    <path name="img_tmp_path" value="/data/uploads/" />
    <path name="img_tmp_path_url" value="https://cdn.example.com/uploads/" />

    <!-- ImageMagick 专用配置 -->
    <imageMagick path="/usr/local/ImageMagick/bin/" />

    <!-- ==================== SMTP 配置 ==================== -->
    <smtp host="smtp.gmail.com" port="587" user="noreply@example.com" pwd="app-password" starttls="true" default="true">
        <from email="@example.com" />
        <from email="@gdxsoft.com" />
        <to email="@example.com" />
    </smtp>

    <smtp host="smtp.qq.com" port="465" user="support@qq.com" pwd="auth-code" ssl="true">
        <from email="@qq.com" />
        <to email="@qq.com" />
    </smtp>

    <!-- ==================== DKIM 配置 ==================== -->
    <dkim domain="example.com" select="default" key="/etc/dkim/example.key.der" />
    <dkim domain="gdxsoft.com" select="mail" key="/etc/dkim/gdxsoft.key.der" />

    <!-- ==================== 全局变量 ==================== -->
    <requestValuesGlobal>
        <rv name="rv_ewa_style_path" value="/EmpScriptV2/" />
        <rv name="APP_NAME" value="企业应用平台" />
        <rv name="APP_VERSION" value="2.0.1" />
        <rv name="COPYRIGHT" value="© 2024 GDXSoft" />
    </requestValuesGlobal>

    <!-- ==================== 变量类型定义 ==================== -->
    <requestValueType Name="USR_ID, G_ADM_ID, GRP_ID" Type="int" />
    <requestValueType Name="ENQ_ID" Type="long" />
    <requestValueType Name="TOKEN" Type="string" />
    <requestValueType Name="IS_ADMIN" Type="boolean" />

    <!-- ==================== 调试配置 ==================== -->
    <debug ips="127.0.0.1,192.168.1.100" sqlout="true" />

    <!-- ==================== 合法域名 ==================== -->
    <validDomains Value="example.com,gdxsoft.com,*.example.com" Host="https://www.example.com" />

    <!-- ==================== 缓存模式 ==================== -->
    <cfgCacheMethod Value="memory" />

    <!-- ==================== 自定义参数 ==================== -->
    <para Name="MAX_UPLOAD_SIZE" Value="52428800" />
    <para Name="ALLOWED_EXTENSIONS" Value="jpg,png,gif,pdf,doc,docx,xls,xlsx" />
    <para Name="PAGE_SIZE" Value="25" />
    <para Name="SESSION_TIMEOUT" Value="3600" />
    <para Name="ENABLE_COMPRESSION" Value="true" />

    <!-- ==================== 工作流配置 ==================== -->
    <workflow>
        <engine class="com.gdxsoft.easyweb.workflow.DefaultEngine" />
        <configPath>/opt/ewa/workflow/config.xml</configPath>
    </workflow>

    <!-- ==================== 数据库配置 ==================== -->
    <databases>
        <database name="main" type="mysql">
            <driver>com.mysql.cj.jdbc.Driver</driver>
            <url>jdbc:mysql://localhost:3306/ewa_db?useSSL=false&amp;serverTimezone=Asia/Shanghai</url>
            <user>ewa_user</user>
            <password>secure_password</password>
            <maxConnections>100</maxConnections>
            <minConnections>10</minConnections>
        </database>
    </databases>
</config>
```

---

## 配置文件管理最佳实践

### 1. 环境分离

为不同环境创建不同的配置文件：

```
ewa_conf_dev.xml      # 开发环境
ewa_conf_test.xml     # 测试环境
ewa_conf_prod.xml     # 生产环境
```

通过 `UPath.CONF_NAME` 指定使用的配置文件：

```java
// 启动前设置
System.setProperty("ewa.conf.name", "ewa_conf_prod.xml");
UPath.CONF_NAME = "ewa_conf_prod.xml";
UPath.reloadConf();
```

### 2. 敏感信息保护

- 不要将密码等敏感信息直接写入配置文件
- 使用环境变量或加密方式存储敏感信息
- 限制配置文件的访问权限

```xml
<!-- 不推荐 -->
<smtp host="smtp.example.com" user="admin" pwd="plaintext_password" />

<!-- 推荐：使用环境变量 -->
<smtp host="smtp.example.com" user="${SMTP_USER}" pwd="${SMTP_PASSWORD}" />
```

### 3. 配置验证

在应用启动时验证关键配置：

```java
public boolean validateConfig() {
    Document doc = UPath.getCfgXmlDoc();
    if (doc == null) {
        LOG.error("配置文件加载失败");
        return false;
    }
    
    // 验证必要配置
    NodeList smtpNodes = doc.getElementsByTagName("smtp");
    if (smtpNodes.getLength() == 0) {
        LOG.warn("未配置 SMTP 服务器");
    }
    
    return true;
}
```

### 4. 配置热更新监控

利用配置热加载特性，实现配置动态更新：

```java
// 定期检查配置变化
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
scheduler.scheduleAtFixedRate(() -> {
    UPath.initPath();  // 自动检查并重新加载
}, 0, 60, TimeUnit.SECONDS);
```

---

## 故障排查

### 配置文件未找到

**现象**: 日志显示 `Not found ewa_conf.xml or ewa_conf_console.xml`

**解决方案**:
1. 检查配置文件是否在 classpath 中
2. 使用 `UPath.CONF_NAME` 指定绝对路径
3. 检查文件权限

### 配置不生效

**现象**: 修改配置后未生效

**解决方案**:
1. 等待 60 秒自动重新加载
2. 调用 `UPath.reloadConf()` 手动重新加载
3. 检查 XML 语法是否正确

### ImageMagick 路径错误

**现象**: 图像处理失败，提示找不到 ImageMagick

**解决方案**:
1. 确认 ImageMagick 已正确安装
2. 检查 `<imageMagick path="...">` 配置是否正确
3. 确保路径包含 bin 目录
