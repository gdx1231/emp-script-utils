# UFile 文件操作使用指南

## 概述

`UFile` 是 emp-script-utils 提供的文件操作工具类，位于 `com.gdxsoft.easyweb.utils` 包中。它提供了丰富的文件操作方法，包括：

- 文件读取（文本、二进制、Base64、GZIP）
- 文件写入（文本、二进制）
- ZIP 压缩/解压
- 文件哈希（MD5、SHA1、SHA256 等）
- 文件属性修改
- 文件类型识别
- 路径操作等

---

## 核心 API

### 文件读取方法

| 方法 | 说明 | 返回值 |
|------|------|--------|
| `readFileText(String filePath)` | 读取文件文本内容（UTF8） | String |
| `readFileBytes(String path)` | 读取文件二进制内容 | byte[] |
| `readFileBase64(String path)` | 读取文件并转为 Base64 | String |
| `readFileGzipBase64(String path)` | 读取文件并压缩为 GZIP Base64 | String |
| `readZipBytes(String zipFilePath, String innerFileName)` | 读取 ZIP 文件内的文件二进制 | byte[] |
| `readZipText(String zipFilePath, String innerFileName)` | 读取 ZIP 文件内的文件文本 | String |
| `getZipList(String zipFilePath)` | 获取 ZIP 文件内的文件列表 | List<String> |

### 文件写入方法

| 方法 | 说明 | 返回值 |
|------|------|--------|
| `createNewTextFile(String fileName, String content)` | 创建文本文件（UTF8） | void |
| `createBinaryFile(String path, byte[] bytes, boolean isOverWrite)` | 创建二进制文件 | void |
| `createMd5File(byte[] bytes, String ext, String path, boolean isOverWrite)` | 创建 MD5 哈希命名的文件 | String |
| `createHashTextFile(String content, String ext, String path, boolean isOverWrite)` | 创建哈希命名的文本文件 | String |
| `createUnGZipHashFile(String base64String, String ext, String path, boolean isOverWrite)` | 创建 GZIP 解压文件 | String |

### 文件压缩方法

| 方法 | 说明 | 返回值 |
|------|------|--------|
| `zipFile(String filePath)` | 压缩单个文件为 ZIP | String |
| `zipPath(String path)` | 压缩目录（不含子目录）为 ZIP | String |
| `zipPaths(String pathRoot, String zipFileName)` | 压缩目录（含子目录）为 ZIP | void |
| `zipFiles(File[] files, String zipFileName)` | 压缩多个文件为 ZIP | void |
| `zipFiles(String[] files, String zipFileName)` | 压缩多个文件为 ZIP | void |
| `unZipFile(String zipFilePath)` | 解压 ZIP 文件 | List<String> |
| `unZipFile(String zipFilePath, String targetPath)` | 解压 ZIP 到指定目录 | List<String> |

### 文件哈希方法

| 方法 | 说明 | 返回值 |
|------|------|--------|
| `md5(File file)` | 获取文件 MD5 哈希值 | String |
| `sha1(File file)` | 获取文件 SHA1 哈希值 | String |
| `sha256(File file)` | 获取文件 SHA256 哈希值 | String |
| `digestFile(File file, String digestName)` | 获取文件指定算法哈希值 | String |
| `createMd5(File file)` | 获取文件 MD5 哈希值 | String |

### 文件操作方法

| 方法 | 说明 | 返回值 |
|------|------|--------|
| `delete(String name)` | 删除文件 | boolean |
| `copyFile(String fileFrom, String fileTo)` | 复制文件 | void |
| `renameFile(String sourcePathAndName, String newName)` | 重命名文件 | void |
| `changeFileExt(String name, String newExt)` | 更改文件扩展名 | String |
| `changeCreationTime(String filePath, Date creationTime)` | 修改文件创建时间 | void |
| `changeModificationTime(String filePath, Date modificationTime)` | 修改文件修改时间 | void |
| `changeCreationAndModifiedTime(String filePath, Date creationTime, Date modificationTime)` | 修改文件创建和修改时间 | void |

### 文件信息方法

| 方法 | 说明 | 返回值 |
|------|------|--------|
| `getFileExt(String name)` | 获取文件扩展名 | String |
| `getFileNoExt(String name)` | 获取不含扩展名的文件名 | String |
| `getExtFromFileBytes(byte[] buf)` | 从文件字节识别扩展名 | String |
| `getFiles(String rootPath, String[] filter)` | 获取目录下的文件 | File[] |

### 路径操作方法

| 方法 | 说明 | 返回值 |
|------|------|--------|
| `buildPaths(String path)` | 创建目录（递归） | boolean |
| `createSplitDirPath(String name, int len)` | 创建分层目录路径 | String |

---

## 使用示例

### 1. 文件读取

#### 读取文本文件

```java
import com.gdxsoft.easyweb.utils.UFile;
import java.io.IOException;

// 读取文件内容（UTF8）
String content = UFile.readFileText("/path/to/file.txt");
System.out.println(content);

// 从 classpath 读取资源
String resourceContent = UFile.readFileText("config/app.properties");
```

#### 读取二进制文件

```java
// 读取文件二进制内容
byte[] bytes = UFile.readFileBytes("/path/to/image.png");

// 从 classpath 读取资源
byte[] resourceBytes = UFile.readFileBytes("images/logo.png");
```

#### 读取为 Base64

```java
// 读取文件并转为 Base64
String base64 = UFile.readFileBase64("/path/to/file.png");
System.out.println(base64);

// 读取文件并压缩为 GZIP Base64
String gzipBase64 = UFile.readFileGzipBase64("/path/to/largefile.txt");
```

#### 读取 ZIP 文件内容

```java
import java.util.List;

String zipPath = "/path/to/archive.zip";

// 获取 ZIP 文件列表
List<String> fileList = UFile.getZipList(zipPath);
for (String fileName : fileList) {
    System.out.println(fileName);
}

// 读取 ZIP 内的文件文本
String text = UFile.readZipText(zipPath, "config/settings.xml");

// 读取 ZIP 内的文件二进制
byte[] bytes = UFile.readZipBytes(zipPath, "images/logo.png");
```

---

### 2. 文件写入

#### 创建文本文件

```java
import java.io.IOException;

// 创建文本文件（UTF8）
UFile.createNewTextFile("/path/to/output.txt", "Hello World!");

// 自动创建目录
UFile.createNewTextFile("/path/to/nested/dir/output.txt", "Content");
```

#### 创建二进制文件

```java
byte[] data = "Binary content".getBytes();

// 创建二进制文件（不覆盖）
UFile.createBinaryFile("/path/to/file.bin", data, false);

// 创建二进制文件（覆盖）
UFile.createBinaryFile("/path/to/file.bin", data, true);
```

#### 创建 MD5 哈希文件

```java
byte[] content = "File content".getBytes();

// 以内容 MD5 作为文件名
String fileName = UFile.createMd5File(content, "txt", "/path/to/save", false);
System.out.println("Saved as: " + fileName);  // 输出：d41d8cd98f00b204e9800998ecf8427e.txt

// 指定 MD5 值
String md5 = "custom_md5_hash";
String fileName2 = UFile.createMd5File(content, md5, "txt", "/path/to/save", true);
```

#### 创建哈希文本文件

```java
String content = "Text content";

// 以内容 hashCode 作为文件名
String fileName = UFile.createHashTextFile(content, "txt", "/path/to/save", false);
System.out.println("Saved as: " + fileName);  // 输出：t_xxxxxxx.txt
```

#### 创建 GZIP 解压文件

```java
String gzipBase64 = "H4sIAAAAAAAA...";  // GZIP 压缩的 Base64 字符串

// 解压并保存
String fileName = UFile.createUnGZipHashFile(gzipBase64, "txt", "/path/to/save", false);
```

---

### 3. ZIP 压缩/解压

#### 压缩单个文件

```java
// 压缩文件
String zipFile = UFile.zipFile("/path/to/document.pdf");
System.out.println("Created: " + zipFile);  // 输出：/path/to/document.pdf.zip
```

#### 压缩目录

```java
// 压缩目录（不含子目录）
String zipFile = UFile.zipPath("/path/to/folder");

// 压缩目录（含子目录）
UFile.zipPaths("/path/to/root", "/path/to/output.zip");
```

#### 压缩多个文件

```java
// 文件数组
File[] files = new File[] {
    new File("/path/to/file1.txt"),
    new File("/path/to/file2.txt")
};
UFile.zipFiles(files, "/path/to/output.zip");

// 文件路径数组
String[] filePaths = {"/path/to/file1.txt", "/path/to/file2.txt"};
UFile.zipFiles(filePaths, "/path/to/output.zip");
```

#### 解压文件

```java
import java.util.List;

String zipPath = "/path/to/archive.zip";

// 解压到默认目录（zip 文件名_xxx.unzip）
List<String> extractedFiles = UFile.unZipFile(zipPath);

// 解压到指定目录
List<String> files = UFile.unZipFile(zipPath, "/path/to/target");
for (String file : files) {
    System.out.println("Extracted: " + file);
}
```

---

### 4. 文件哈希

#### 计算文件哈希值

```java
import java.io.File;

File file = new File("/path/to/file.dat");

// MD5
String md5 = UFile.md5(file);
System.out.println("MD5: " + md5);

// SHA1
String sha1 = UFile.sha1(file);
System.out.println("SHA1: " + sha1);

// SHA256
String sha256 = UFile.sha256(file);
System.out.println("SHA256: " + sha256);

// SHA512
String sha512 = UFile.digestFile(file, "SHA-512");
System.out.println("SHA512: " + sha512);

// 其他算法（MD2, MD5, SHA-1, SHA-224, SHA-256, SHA-384, SHA-512）
String sha384 = UFile.digestFile(file, "SHA-384");
```

#### 大文件哈希（流式处理）

```java
// UFile 使用流式处理，支持大文件
File largeFile = new File("/path/to/largefile.iso");  // 500MB+
String md5 = UFile.md5(largeFile);  // 不会内存溢出
System.out.println("Large file MD5: " + md5);
```

---

### 5. 文件操作

#### 删除文件

```java
boolean deleted = UFile.delete("/path/to/file.txt");
System.out.println("Deleted: " + deleted);
```

#### 复制文件

```java
UFile.copyFile("/path/to/source.txt", "/path/to/destination.txt");
```

#### 重命名文件

```java
// 注意：此方法使用 UPath.getScriptPath() 作为基础路径
UFile.renameFile("/subdir/oldname.txt", "newname.txt");
```

#### 更改文件扩展名

```java
String newName = UFile.changeFileExt("/path/to/file.txt", "bak");
System.out.println(newName);  // 输出：/path/to/file.bak
```

#### 修改文件时间

```java
import java.util.Date;

Date now = new Date();
Date yesterday = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);

// 修改创建时间
UFile.changeCreationTime("/path/to/file.txt", yesterday);

// 修改修改时间
UFile.changeModificationTime("/path/to/file.txt", now);

// 同时修改创建和修改时间
UFile.changeCreationAndModifiedTime("/path/to/file.txt", yesterday, now);
```

---

### 6. 文件信息

#### 获取文件扩展名

```java
String ext1 = UFile.getFileExt("document.pdf");      // 输出："pdf"
String ext2 = UFile.getFileExt("archive.tar.gz");    // 输出："gz"
String ext3 = UFile.getFileExt("noext");             // 输出：""
String ext4 = UFile.getFileExt("file.");             // 输出：""
```

#### 获取不含扩展名的文件名

```java
String name1 = UFile.getFileNoExt("document.pdf");   // 输出："document"
String name2 = UFile.getFileNoExt("/path/to/file.txt");  // 输出："file"
```

#### 从字节识别文件类型

```java
byte[] fileHeader = ...;  // 读取文件前 120 字节
String ext = UFile.getExtFromFileBytes(fileHeader);

// 支持的格式：
// swf, jpg, pdf, rar, zip, png, gif, bmp, rtf, doc, xls, ppt, tif
```

#### 获取目录下的文件

```java
// 获取目录下所有 .txt 和 .pdf 文件
String[] filter = {".txt", ".pdf"};
File[] files = UFile.getFiles("/path/to/dir", filter);

for (File file : files) {
    System.out.println(file.getName());
}
```

---

### 7. 路径操作

#### 创建目录

```java
// 递归创建目录
boolean created = UFile.buildPaths("/path/to/nested/directory");
System.out.println("Created: " + created);
```

#### 创建分层目录路径

```java
// 根据名称创建分层目录（用于分散文件）
String name = "abcdef123456";
String splitPath = UFile.createSplitDirPath(name, 2);
System.out.println(splitPath);  // 输出："ab/cd/ef/12/34/56/"

// 用于文件存储
String fileName = "large_file.dat";
String savePath = "/data/files/" + UFile.createSplitDirPath(fileName, 4);
UFile.buildPaths(savePath);
```

---

## 完整使用示例

### 示例 1：文件上传处理

```java
import com.gdxsoft.easyweb.utils.UFile;
import java.io.IOException;

public class FileUploadHandler {
    
    /**
     * 处理上传的文件
     */
    public String handleUpload(byte[] fileContent, String originalFileName) throws IOException {
        // 获取文件扩展名
        String ext = UFile.getFileExt(originalFileName);
        
        // 从文件头验证真实类型
        String realExt = UFile.getExtFromFileBytes(fileContent);
        
        // 如果不匹配，可能是伪造的扩展名
        if (!ext.equalsIgnoreCase(realExt)) {
            throw new SecurityException("File extension mismatch!");
        }
        
        // 创建 MD5 哈希文件名
        String savedFileName = UFile.createMd5File(fileContent, ext, "/data/uploads", false);
        
        return savedFileName;
    }
    
    /**
     * 获取上传文件
     */
    public byte[] getFile(String md5FileName) throws IOException {
        return UFile.readFileBytes("/data/uploads/" + md5FileName);
    }
}
```

### 示例 2：ZIP 备份工具

```java
import com.gdxsoft.easyweb.utils.UFile;
import java.io.IOException;
import java.util.List;

public class BackupTool {
    
    /**
     * 备份目录
     */
    public String backupDirectory(String sourceDir, String backupDir) throws IOException {
        // 生成备份文件名（带时间戳）
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        String zipFileName = backupDir + "/backup_" + timestamp + ".zip";
        
        // 压缩目录（含子目录）
        UFile.zipPaths(sourceDir, zipFileName);
        
        // 计算备份文件哈希
        String md5 = UFile.md5(new java.io.File(zipFileName));
        
        // 保存哈希值
        UFile.createNewTextFile(zipFileName + ".md5", md5);
        
        return zipFileName;
    }
    
    /**
     * 恢复备份
     */
    public List<String> restoreBackup(String zipFile, String targetDir) throws IOException {
        // 验证哈希
        String expectedMd5 = UFile.readFileText(zipFile + ".md5");
        String actualMd5 = UFile.md5(new java.io.File(zipFile));
        
        if (!expectedMd5.equals(actualMd5)) {
            throw new SecurityException("Backup file integrity check failed!");
        }
        
        // 解压备份
        return UFile.unZipFile(zipFile, targetDir);
    }
}
```

### 示例 3：文件缓存系统

```java
import com.gdxsoft.easyweb.utils.UFile;

public class FileCache {
    
    private String cacheDir;
    
    public FileCache(String cacheDir) {
        this.cacheDir = cacheDir;
        UFile.buildPaths(cacheDir);
    }
    
    /**
     * 获取缓存内容
     */
    public String get(String key) throws IOException {
        String fileName = getCacheFileName(key);
        File f = new File(cacheDir + "/" + fileName);
        
        if (!f.exists()) {
            return null;
        }
        
        // 检查缓存是否过期（24 小时）
        long age = System.currentTimeMillis() - f.lastModified();
        if (age > 24 * 60 * 60 * 1000) {
            UFile.delete(cacheDir + "/" + fileName);
            return null;
        }
        
        return UFile.readFileText(cacheDir + "/" + fileName);
    }
    
    /**
     * 设置缓存
     */
    public void set(String key, String content) throws IOException {
        String fileName = getCacheFileName(key);
        UFile.createNewTextFile(cacheDir + "/" + fileName, content);
    }
    
    private String getCacheFileName(String key) {
        // 使用 MD5 作为文件名
        try {
            return UFile.createMd5File(key.getBytes(), "cache", cacheDir, false);
        } catch (Exception e) {
            return "cache_" + Math.abs(key.hashCode());
        }
    }
}
```

### 示例 4：日志文件轮转

```java
import com.gdxsoft.easyweb.utils.UFile;
import java.io.File;
import java.io.IOException;
import java.util.Date;

public class LogRotator {
    
    private String logDir;
    private int maxBackups;
    
    public LogRotator(String logDir, int maxBackups) {
        this.logDir = logDir;
        this.maxBackups = maxBackups;
        UFile.buildPaths(logDir);
    }
    
    /**
     * 轮转日志文件
     */
    public void rotate(String logFileName) throws IOException {
        File logFile = new File(logDir + "/" + logFileName);
        if (!logFile.exists()) {
            return;
        }
        
        // 生成备份文件名
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String backupName = logFileName + "." + timestamp + ".gz";
        
        // 读取日志内容并压缩
        String gzipBase64 = UFile.readFileGzipBase64(logDir + "/" + logFileName);
        
        // 保存压缩文件
        UFile.createUnGZipHashFile(gzipBase64, "gz", logDir, false);
        
        // 清空原日志文件
        UFile.createNewTextFile(logDir + "/" + logFileName, "");
        
        // 删除旧备份
        cleanupOldBackups(logFileName);
    }
    
    private void cleanupOldBackups(String logFileName) {
        File[] backups = new File(logDir).listFiles(
            (dir, name) -> name.startsWith(logFileName + ".")
        );
        
        if (backups == null || backups.length <= maxBackups) {
            return;
        }
        
        // 按修改时间排序，删除最旧的
        java.util.Arrays.sort(backups, (a, b) -> 
            Long.compare(a.lastModified(), b.lastModified())
        );
        
        for (int i = 0; i < backups.length - maxBackups; i++) {
            UFile.delete(backups[i].getAbsolutePath());
        }
    }
}
```

### 示例 5：文件完整性验证

```java
import com.gdxsoft.easyweb.utils.UFile;
import java.io.File;
import java.io.IOException;

public class FileIntegrityChecker {
    
    /**
     * 计算并保存文件哈希
     */
    public void saveHash(String filePath, String hashFile) throws IOException {
        File file = new File(filePath);
        
        // 计算多种哈希值
        String md5 = UFile.md5(file);
        String sha1 = UFile.sha1(file);
        String sha256 = UFile.sha256(file);
        
        // 保存哈希值
        StringBuilder sb = new StringBuilder();
        sb.append("MD5: ").append(md5).append("\n");
        sb.append("SHA1: ").append(sha1).append("\n");
        sb.append("SHA256: ").append(sha256).append("\n");
        
        UFile.createNewTextFile(hashFile, sb.toString());
    }
    
    /**
     * 验证文件完整性
     */
    public boolean verify(String filePath, String hashFile) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("File not found: " + filePath);
            return false;
        }
        
        // 读取保存的哈希值
        String hashContent = UFile.readFileText(hashFile);
        
        // 解析哈希值
        String expectedMd5 = extractHash(hashContent, "MD5");
        String expectedSha256 = extractHash(hashContent, "SHA256");
        
        // 计算当前哈希值
        String actualMd5 = UFile.md5(file);
        String actualSha256 = UFile.sha256(file);
        
        // 验证
        boolean md5Match = expectedMd5.equals(actualMd5);
        boolean sha256Match = expectedSha256.equals(actualSha256);
        
        System.out.println("MD5: " + (md5Match ? "OK" : "FAILED"));
        System.out.println("SHA256: " + (sha256Match ? "OK" : "FAILED"));
        
        return md5Match && sha256Match;
    }
    
    private String extractHash(String content, String algorithm) {
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.startsWith(algorithm + ":")) {
                return line.split(":")[1].trim();
            }
        }
        return null;
    }
}
```

---

## 支持的文件格式识别

`getExtFromFileBytes` 方法支持以下文件格式识别：

| 格式 | 文件头特征 | 扩展名 |
|------|-----------|--------|
| SWF | CWS | swf |
| JPG | JFIF/XIF/FFD8FF | jpg |
| PDF | PDF | pdf |
| RAR | RAR | rar |
| ZIP | PK | zip |
| PNG | PNG | png |
| GIF | GIF | gif |
| BMP | BM | bmp |
| RTF | {\rtf1 | rtf |
| DOC/XLS/PPT | D0CF11E0A1B11AE1 | doc/xls/ppt |
| TIF | 49492A00 | tif |
| BIN | 其他 | bin |

---

## 支持的哈希算法

`digestFile` 方法支持以下哈希算法：

- MD2
- MD5
- SHA-1
- SHA-224
- SHA-256
- SHA-384
- SHA-512

---

## 注意事项

### 1. 文件编码

- 文本文件读写使用 UTF-8 编码
- 确保文件内容确实是 UTF-8 编码

### 2. 路径分隔符

- 支持 Windows（\）和 Unix（/）路径分隔符
- 自动处理路径中的双斜杠

### 3. 资源文件读取

- `readFileBytes` 和 `readFileText` 支持从 classpath 读取资源
- 如果文件不存在，尝试从 classpath 查找

### 4. 大文件处理

- 哈希计算使用流式处理，不会内存溢出
- 支持 500MB+ 大文件

### 5. ZIP 文件路径

- ZIP 内文件路径使用 Unix 风格（/）
- Windows 路径会自动转换

### 6. 目录创建

- `buildPaths` 方法递归创建目录
- 如果目录已存在，返回 true

### 7. 文件覆盖

- 创建文件时通过 `isOverWrite` 参数控制是否覆盖
- 默认建议设置为 false 避免意外覆盖

### 8. 异常处理

- 大部分方法抛出 `IOException`
- 部分方法抛出 `Exception`
- 需要适当捕获和处理异常

---

## 故障排查

### 文件未找到

**现象**: `IOException: The file ... not exists in resource and file`

**解决方案**:
```java
// 1. 检查文件是否存在
File f = new File(filePath);
System.out.println("Exists: " + f.exists());

// 2. 检查 classpath 资源
URL url = UFile.class.getClassLoader().getResource(filePath);
System.out.println("Resource: " + url);

// 3. 使用绝对路径
String absolutePath = new File(filePath).getAbsolutePath();
```

### 无法创建目录

**现象**: `Can't create the directory (...)`

**解决方案**:
```java
// 1. 检查权限
File dir = new File(path);
System.out.println("Can write: " + dir.canWrite());

// 2. 手动创建
boolean created = UFile.buildPaths(path);
System.out.println("Created: " + created);
```

### ZIP 解压失败

**现象**: 解压后文件丢失或损坏

**解决方案**:
```java
// 1. 检查 ZIP 文件完整性
List<String> list = UFile.getZipList(zipPath);
System.out.println("Files in ZIP: " + list);

// 2. 检查目标目录权限
File target = new File(targetPath);
System.out.println("Can write: " + target.canWrite());

// 3. 使用绝对路径
String absoluteZipPath = new File(zipPath).getAbsolutePath();
```

---

## 相关类

| 类名 | 包路径 | 说明 |
|------|--------|------|
| `UPath` | `com.gdxsoft.easyweb.utils` | 路径和配置管理 |
| `UConvert` | `com.gdxsoft.easyweb.utils` | Base64 转换 |
| `Utils` | `com.gdxsoft.easyweb.utils` | 通用工具（字节转 Hex 等） |
| `UFileFilter` | `com.gdxsoft.easyweb.utils` | 文件过滤器 |
| `MStr` | `com.gdxsoft.easyweb.utils.msnet` | 可变字符串 |
