# UPath 路径与配置管理使用指南

## 概述

`UPath` 是 emp-script-utils 的核心配置和路径管理类，位于 `com.gdxsoft.easyweb.utils` 包中。它负责：

- 加载和管理配置文件（ewa_conf.xml）
- 提供系统路径访问
- 管理全局变量和自定义参数
- 支持配置热加载
- 提供域名验证、调试 IP 管理等功能

---

## 配置文件加载机制

### 配置文件搜索顺序

UPath 按以下顺序加载配置文件：

1. **用户自定义配置文件** - 通过 `UPath.CONF_NAME` 指定绝对路径
2. **ewa_conf.xml** - 从 classpath 加载
3. **ewa_conf_console.xml** - 从 classpath 加载（控制台应用）

### 配置加载流程

```
UPath.initPath()
    ├── initPathReal()        // 初始化物理路径
    ├── loadEwaConf()         // 加载配置文件
    │   ├── 检查 CONF_NAME
    │   ├── 查找 ewa_conf.xml
    │   └── 查找 ewa_conf_console.xml
    └── initPathXml()         // 初始化 XML 配置
        ├── initPaths()           // 路径配置
        ├── initSmtpParas()       // SMTP 配置
        ├── initDebugIps()        // 调试 IP
        ├── initParas()           // 自定义参数
        ├── initRequestValueGlobal() // 全局变量
        ├── initValidDomains()    // 合法域名
        ├── initCfgCacheMethod()  // 缓存模式
        └── initRequestValuesType() // 变量类型
```

---

## 核心 API

### 配置管理方法

| 方法 | 说明 | 返回值 |
|------|------|--------|
| `initPath()` | 初始化配置路径（自动检查变化） | void |
| `reloadConf()` | 重新加载配置 | void |
| `getCfgXmlDoc()` | 获取配置文件 Document 对象 | Document |
| `getPropTime()` | 获取配置文件最后修改时间 | long |

### 路径相关方法

| 方法 | 说明 | 返回值 |
|------|------|--------|
| `getRealPath()` | 获取项目 Class 所在目录 | String |
| `getRealContextPath()` | 获取 WebRoot 物理目录 | String |
| `getConfigPath()` | 获取系统配置目录 | String |
| `getScriptPath()` | 获取系统描述目录 | String |
| `getProjectPath()` | 获取项目路径（config_path/projects/） | String |
| `getGroupPath()` | 获取组件生产/导入目录 | String |
| `getCachedPath()` | 获取 Cache 文件目录 | String |
| `getPATH_UPLOAD()` | 获取上传文件物理路径 | String |
| `getPATH_UPLOAD_URL()` | 获取上传文件 URL 路径 | String |
| `getPATH_IMG_CACHE()` | 获取图片临时文件路径 | String |
| `getPATH_IMG_CACHE_URL()` | 获取图片临时文件 URL | String |
| `getEmpScriptV2Path()` | 获取 EmpScriptV2 路径 | String |

### 转换工具路径

| 方法 | 说明 | 返回值 |
|------|------|--------|
| `getCVT_IMAGEMAGICK_HOME()` | 获取 ImageMagick 可执行目录 | String |
| `getCVT_OPENOFFICE_HOME()` | 获取 OpenOffice 目录 | String |
| `getCVT_SWFTOOL_HOME()` | 获取 SWFTool 目录 | String |

### 全局变量和参数

| 方法 | 说明 | 返回值 |
|------|------|--------|
| `getRV_GLOBALS()` | 获取 RequestValue 全局变量 | Map<String, String> |
| `getRvTypes()` | 获取 RequestValue 类型定义 | Map<String, String> |
| `getInitPara(String name)` | 获取自定义初始化参数 | String |

### 调试和验证

| 方法 | 说明 | 返回值 |
|------|------|--------|
| `getDebugIps()` | 获取可调试 IP 地址列表 | MTableStr |
| `isDebugSql()` | 是否输出 SQL 执行日志 | boolean |
| `isWebCall()` | 是否 Web 环境加载 | boolean |
| `checkIsValidDomain(String domain)` | 检查域名是否合法 | boolean |
| `getVALID_DOMAINS()` | 获取合法域名列表 | MTableStr |

### 其他配置

| 方法 | 说明 | 返回值 |
|------|------|--------|
| `getWFXML()` | 获取工作流配置 XML | String |
| `getDATABASEXML()` | 获取数据库连接池配置 XML | String |
| `getCfgCacheMethod()` | 获取配置缓存模式（memory/sqlcached） | String |

---

## 使用示例

### 1. 基本路径访问

```java
import com.gdxsoft.easyweb.utils.UPath;

// 获取项目路径
String realPath = UPath.getRealPath();
System.out.println("项目路径：" + realPath);

// 获取配置目录
String configPath = UPath.getConfigPath();
System.out.println("配置目录：" + configPath);

// 获取项目路径
String projectPath = UPath.getProjectPath();
System.out.println("项目路径：" + projectPath);

// 获取上传路径
String uploadPath = UPath.getPATH_UPLOAD();
String uploadUrl = UPath.getPATH_UPLOAD_URL();
```

### 2. 配置重新加载

```java
// 手动重新加载配置
UPath.reloadConf();

// 获取配置文件最后修改时间
long propTime = UPath.getPropTime();
System.out.println("配置修改时间：" + propTime);

// 修改检查间隔（默认 60 秒）
UPath.CHK_DURATION = 30000; // 30 秒
```

### 3. 全局变量使用

```java
// 获取全局变量
Map<String, String> globals = UPath.getRV_GLOBALS();

// 访问全局变量
String ewaPath = globals.get("RV_EWA_STYLE_PATH");
String appName = globals.get("APP_NAME");

// 获取 EmpScriptV2 路径
String empPath = UPath.getEmpScriptV2Path();
```

### 4. 自定义参数

```java
// 获取自定义参数（大小写不敏感）
String maxUpload = UPath.getInitPara("MAX_UPLOAD_SIZE");
String pageSize = UPath.getInitPara("PAGE_SIZE");
String allowExt = UPath.getInitPara("ALLOWED_EXTENSIONS");
```

### 5. 域名验证

```java
// 检查域名是否合法
boolean isValid = UPath.checkIsValidDomain("gdxsoft.com");
boolean isSubValid = UPath.checkIsValidDomain("test.gdxsoft.com");

// 获取所有合法域名
MTableStr validDomains = UPath.getVALID_DOMAINS();
```

### 6. 调试功能

```java
// 获取调试 IP 列表
MTableStr debugIps = UPath.getDebugIps();

// 检查是否输出 SQL 日志
boolean debugSql = UPath.isDebugSql();

// 检查是否 Web 环境
boolean isWeb = UPath.isWebCall();
```

### 7. 转换工具路径

```java
// 获取 ImageMagick 路径
String magickPath = UPath.getCVT_IMAGEMAGICK_HOME();

// 获取 OpenOffice 路径
String officePath = UPath.getCVT_OPENOFFICE_HOME();

// 获取 SWFTool 路径
String swftoolPath = UPath.getCVT_SWFTOOL_HOME();
```

### 8. 获取配置 XML

```java
// 获取配置文件 Document 对象
Document doc = UPath.getCfgXmlDoc();

// 获取工作流配置
String workflowXml = UPath.getWFXML();

// 获取数据库配置
String databaseXml = UPath.getDATABASEXML();
```

---

## 常量定义

```java
// EWA 静态文件路径变量名
public static final String RV_EWA_STYLE_PATH = "rv_ewa_style_path";

// 默认 EWA 静态文件路径
public static final String DEF_EWA_STYLE_PATH = "/EmpScriptV2";

// 配置文件名称
public static String CONF_NAME = null;

// 临时目录
public static String PATH_TEMP = "_EWA_TMP_";

// 项目缓存目录
public static String PATH_PRJ_CACHE = PATH_TEMP + "/PRJ/";

// 图片缓存目录
public static String PATH_IMG_CACHE = PATH_TEMP + "/IMG/";

// 配置检查间隔（毫秒）
public static long CHK_DURATION = 60 * 1000;
```

---

## 配置项对应关系

### XML 路径配置与 Java 方法

| XML 配置 name 值 | Java 方法 | 说明 |
|-----------------|----------|------|
| `config_path` | `getConfigPath()` | 系统配置目录 |
| `script_path` | `getScriptPath()` | 系统描述目录 |
| `group_path` | `getGroupPath()` | 组件生产/导入目录 |
| `cached_path` | `getCachedPath()` | Cache 文件目录 |
| `cvt_ImageMagick_Home` | `getCVT_IMAGEMAGICK_HOME()` | ImageMagick 目录 |
| `cvt_office_home` | `getCVT_OPENOFFICE_HOME()` | OpenOffice 目录 |
| `cvt_swftool_Home` | `getCVT_SWFTOOL_HOME()` | SWFTool 目录 |
| `img_tmp_path` | `getPATH_UPLOAD()` / `getPATH_IMG_CACHE()` | 上传物理路径 |
| `img_tmp_path_url` | `getPATH_UPLOAD_URL()` / `getPATH_IMG_CACHE_URL()` | 上传 URL 路径 |

### XML 全局变量与 Java 方法

| XML 配置 | Java 方法 | 说明 |
|----------|----------|------|
| `<rv name="rv_ewa_style_path" ...>` | `getRV_GLOBALS().get("RV_EWA_STYLE_PATH")` | EWA 静态路径 |
| `<rv name="APP_NAME" ...>` | `getRV_GLOBALS().get("APP_NAME")` | 应用名称 |
| 任意 `<rv>` | `getRV_GLOBALS()` | 所有全局变量 |

### XML 自定义参数与 Java 方法

| XML 配置 | Java 方法 | 说明 |
|----------|----------|------|
| `<para Name="MAX_UPLOAD_SIZE" ...>` | `getInitPara("MAX_UPLOAD_SIZE")` | 最大上传大小 |
| `<para Name="PAGE_SIZE" ...>` | `getInitPara("PAGE_SIZE")` | 分页大小 |
| 任意 `<para>` | `getInitPara(name)` | 自定义参数 |

---

## 配置热加载机制

### 工作原理

1. **自动检查**: `initPath()` 方法每 60 秒（CHK_DURATION）检查一次配置文件变化
2. **时间戳比对**: 通过文件最后修改时间（PROP_TIME）判断是否变化
3. **JAR 包优化**: 如果配置在 JAR 包内，不进行检查（不会变化）
4. **自动重载**: 检测到变化后自动调用 `reloadConf()`

### 热加载流程

```
initPath()
    ├── 检查距离上次检查是否超过 CHK_DURATION
    ├── 检查是否在 JAR 包内（跳过检查）
    ├── isConfFileChanged() 检查文件修改时间
    │   ├── PROP_TIME == -1231: 初始化时间戳
    │   ├── lastModified == PROP_TIME: 未变化
    │   └── lastModified != PROP_TIME: 已变化
    └── reloadConf() 重新加载
```

### 手动控制

```java
// 立即重新加载配置
UPath.reloadConf();

// 修改检查间隔
UPath.CHK_DURATION = 10000; // 10 秒

// 获取配置修改时间
long propTime = UPath.getPropTime();
```

---

## 完整使用示例

### 示例 1：应用启动初始化

```java
import com.gdxsoft.easyweb.utils.UPath;
import java.util.Map;

public class AppInit {
    public static void init() {
        // 初始化配置
        UPath.initPath();
        
        // 验证配置加载
        if (UPath.getCfgXmlDoc() == null) {
            System.err.println("配置文件加载失败！");
            return;
        }
        
        // 输出路径信息
        System.out.println("项目路径：" + UPath.getRealPath());
        System.out.println("配置目录：" + UPath.getConfigPath());
        System.out.println("上传路径：" + UPath.getPATH_UPLOAD());
        
        // 输出全局变量
        Map<String, String> globals = UPath.getRV_GLOBALS();
        System.out.println("应用名称：" + globals.get("APP_NAME"));
    }
}
```

### 示例 2：文件上传路径获取

```java
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.UFile;

public class FileUploadService {
    
    public String getUploadPath() {
        // 获取上传物理路径
        String uploadPath = UPath.getPATH_UPLOAD();
        if (uploadPath == null) {
            uploadPath = UPath.getRealContextPath() + "/uploads/";
        }
        return uploadPath;
    }
    
    public String getUploadUrl() {
        // 获取上传 URL 路径
        String uploadUrl = UPath.getPATH_UPLOAD_URL();
        if (uploadUrl == null) {
            uploadUrl = "/uploads/";
        }
        return uploadUrl;
    }
    
    public String getTempImagePath() {
        // 获取临时图片路径
        return UPath.getPATH_IMG_CACHE();
    }
}
```

### 示例 3：域名验证中间件

```java
import com.gdxsoft.easyweb.utils.UPath;

public class DomainValidator {
    
    /**
     * 验证请求域名是否合法
     */
    public boolean validate(String requestDomain) {
        if (requestDomain == null || requestDomain.trim().isEmpty()) {
            return false;
        }
        
        // 使用 UPath 验证域名
        return UPath.checkIsValidDomain(requestDomain);
    }
    
    /**
     * 获取所有合法域名列表
     */
    public String[] getValidDomains() {
        MTableStr domains = UPath.getVALID_DOMAINS();
        return domains.getTable().keySet().toArray(new String[0]);
    }
}
```

### 示例 4：调试模式检查

```java
import com.gdxsoft.easyweb.utils.UPath;

public class DebugHelper {
    
    /**
     * 检查当前 IP 是否允许调试
     */
    public boolean isDebugAllowed(String clientIp) {
        MTableStr debugIps = UPath.getDebugIps();
        return debugIps.containsKey(clientIp);
    }
    
    /**
     * 是否输出 SQL 日志
     */
    public boolean shouldLogSql() {
        return UPath.isDebugSql();
    }
    
    /**
     * 输出调试信息（仅调试 IP 可见）
     */
    public void debug(String clientIp, String message) {
        if (isDebugAllowed(clientIp)) {
            System.out.println("[DEBUG] " + message);
        }
    }
}
```

### 示例 5：配置监控

```java
import com.gdxsoft.easyweb.utils.UPath;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ConfigMonitor {
    
    private static long lastPropTime = 0;
    
    public static void startMonitoring() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        
        scheduler.scheduleAtFixedRate(() -> {
            long currentPropTime = UPath.getPropTime();
            
            if (currentPropTime != lastPropTime && lastPropTime != 0) {
                System.out.println("配置文件已更新！");
                System.out.println("更新时间：" + new Date(currentPropTime));
                
                // 可以在这里触发其他更新逻辑
            }
            
            lastPropTime = currentPropTime;
        }, 0, 30, TimeUnit.SECONDS);
    }
}
```

---

## 环境变量配置

### 指定配置文件

```java
// 方法 1：直接设置 CONF_NAME
UPath.CONF_NAME = "/path/to/ewa_conf_prod.xml";
UPath.reloadConf();

// 方法 2：通过系统属性
System.setProperty("ewa.conf.name", "ewa_conf_prod.xml");
UPath.CONF_NAME = System.getProperty("ewa.conf.name");
UPath.reloadConf();
```

### 多环境配置

```java
public class EnvironmentConfig {
    
    public static void init(String env) {
        String confFile;
        
        switch (env.toLowerCase()) {
            case "dev":
                confFile = "/config/ewa_conf_dev.xml";
                break;
            case "test":
                confFile = "/config/ewa_conf_test.xml";
                break;
            case "prod":
                confFile = "/config/ewa_conf_prod.xml";
                break;
            default:
                confFile = "ewa_conf.xml";
        }
        
        UPath.CONF_NAME = confFile;
        UPath.reloadConf();
    }
}
```

---

## 注意事项

### 1. 初始化时机

- UPath 采用懒加载机制，首次调用任何 getter 方法时自动初始化
- 建议在应用启动时显式调用 `UPath.initPath()`

### 2. 路径分隔符

- 配置的路径值会自动添加 `/` 或 `\` 后缀
- 代码中无需手动添加路径分隔符

### 3. 空值处理

- 如果配置未定义，部分方法返回 `null`
- 使用前应进行空值检查

### 4. 线程安全

- 配置加载使用 `synchronized` 保证线程安全
- 全局变量使用 `ConcurrentHashMap`

### 5. JAR 包内配置

- 如果配置文件在 JAR 包内，不会进行热加载检查
- 生产环境建议将配置文件放在 JAR 包外

### 6. 上传路径配置

- `img_tmp_path` 和 `img_tmp_path_url` 需要配合使用
- 需要在 Web 服务器（Tomcat/Apache/Nginx）中配置虚拟路径

---

## 故障排查

### 配置文件未找到

**现象**: 日志显示 `Not found ewa_conf.xml or ewa_conf_console.xml`

**解决方案**:
```java
// 1. 检查配置文件位置
System.out.println(UPath.getRealPath());

// 2. 使用绝对路径
UPath.CONF_NAME = "/absolute/path/to/ewa_conf.xml";
UPath.reloadConf();

// 3. 检查文件权限
File f = new File("/path/to/ewa_conf.xml");
System.out.println("Exists: " + f.exists());
System.out.println("Readable: " + f.canRead());
```

### 配置不生效

**现象**: 修改配置后未生效

**解决方案**:
```java
// 1. 等待自动重新加载（60 秒）
// 2. 或手动重新加载
UPath.reloadConf();

// 3. 检查配置检查间隔
System.out.println("Check duration: " + UPath.CHK_DURATION);

// 4. 检查配置修改时间
System.out.println("Prop time: " + UPath.getPropTime());
```

### 路径为 null

**现象**: 获取路径返回 null

**解决方案**:
```java
// 1. 确保配置已初始化
UPath.initPath();

// 2. 检查配置文件中是否定义
Document doc = UPath.getCfgXmlDoc();
NodeList paths = doc.getElementsByTagName("path");
```

---

## 相关类

| 类名 | 包路径 | 说明 |
|------|--------|------|
| `UXml` | `com.gdxsoft.easyweb.utils` | XML 工具类 |
| `UFile` | `com.gdxsoft.easyweb.utils` | 文件操作工具 |
| `MTableStr` | `com.gdxsoft.easyweb.utils.msnet` | 字符串表格类 |
| `SmtpCfgs` | `com.gdxsoft.easyweb.utils.Mail` | SMTP 配置管理 |
| `ConfImageMagick` | `com.gdxsoft.easyweb.conf` | ImageMagick 配置 |
