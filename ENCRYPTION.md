# 加密/解密使用指南

## 概述

emp-script-utils 提供了完整的加密/解密工具类，位于 `com.gdxsoft.easyweb.utils` 包中，包括：

- **对称加密**: `UAes` (AES), `UDes` (DES)
- **非对称加密**: `URsa` (RSA)
- **密码哈希**: `UArgon2` (Argon2)
- **消息摘要**: `UDigest` (MD5, SHA, HMAC, SM3 等)
- **数字签名**: `USign` (签名工具)

所有加密功能基于 **BouncyCastle** 库实现。

---

## 目录

1. [AES 加密/解密](#1-aes-加密解密)
2. [DES 加密/解密](#2-des-加密解密)
3. [RSA 加密/解密](#3-rsa-加密解密)
4. [Argon2 密码哈希](#4-argon2-密码哈希)
5. [消息摘要](#5-消息摘要)
6. [数字签名](#6-数字签名)
7. [Utils 快捷方法](#7-utils-快捷方法)

---

## 1. AES 加密/解密

### 1.1 概述

`UAes` 提供 AES 对称加密，支持多种模式和密钥长度。

**支持的模式**:
| 模式 | 常量 | 说明 |
|------|------|------|
| GCM | `AES_128_GCM`, `AES_192_GCM`, `AES_256_GCM` | Galois Counter Mode (推荐) |
| CCM | `AES_128_CCM`, `AES_192_CCM`, `AES_256_CCM` | Counter with CBC-MAC |
| CBC | `AES_128_CBC`, `AES_192_CBC`, `AES_256_CBC` | Cipher Block Chaining |
| CFB | `AES_128_CFB`, `AES_192_CFB`, `AES_256_CFB` | Cipher Feedback |
| CTR | `AES_128_CTR`, `AES_192_CTR`, `AES_256_CTR` | Counter Mode |
| OFB | `AES_128_OFB`, `AES_192_OFB`, `AES_256_OFB` | Output Feedback |
| ECB | `AES_128_ECB`, `AES_192_ECB`, `AES_256_ECB` | Electronic Codebook |

**支持的填充方式**:
- `PKCS7Padding` (推荐)
- `PKCS5Padding`
- `NoPadding`

### 1.2 初始化密钥

```java
import com.gdxsoft.easyweb.utils.UAes;

// 方式 1: 初始化默认密钥 (128 位)
UAes.initDefaultKey("my-secret-key-123", "my-iv-value-123");

// 方式 2: 初始化默认密钥并指定算法 (256 位)
UAes.initDefaultKey(UAes.AES_256_GCM, "my-secret-key-256", "my-iv-value-256");

// 方式 3: 初始化默认密钥并指定 MAC 大小 (GCM/CCM)
UAes.initDefaultKey(UAes.AES_256_GCM, "key", "iv", 128);

// 方式 4: 初始化默认密钥并指定 AAD (GCM/CCM)
UAes.initDefaultKey(UAes.AES_256_GCM, "key", "iv", 128, "additional-data");
```

### 1.3 加密/解密示例

#### 使用默认密钥

```java
// 获取实例
UAes aes = UAes.getInstance();
aes.setPaddingMethod(UAes.PKCS7Padding);

String plainText = "Hello World 你好世界";

// 加密
String encrypted = aes.encrypt(plainText);
System.out.println("加密后：" + encrypted);

// 解密
String decrypted = aes.decrypt(encrypted);
System.out.println("解密后：" + decrypted);
```

#### 使用自定义密钥

```java
// 创建 AES 实例（128 位 CBC 模式）
UAes aes = new UAes("my-secret-key", "my-iv-value");
aes.setPaddingMethod(UAes.PKCS7Padding);

String plainText = "Secret Message";

// 加密
String encrypted = aes.encrypt(plainText);

// 解密
String decrypted = aes.decrypt(encrypted);
```

#### 使用不同模式

```java
// AES-256-GCM 模式
UAes aesGcm = new UAes("key-256-bit", "iv-16-bytes", UAes.AES_256_GCM);
aesGcm.setMacSizeBits(128);  // 设置 MAC 大小

// AES-256-CBC 模式
UAes aesCbc = new UAes("key-256-bit", "iv-16-bytes", UAes.AES_256_CBC);

// AES-256-CCM 模式
UAes aesCcm = new UAes("key-256-bit", "iv-12-bytes", UAes.AES_256_CCM);
aesCcm.setMacSizeBits(128);
```

### 1.4 二进制加密

```java
UAes aes = new UAes("key", "iv");

byte[] plainData = "Binary data".getBytes();

// 加密二进制
byte[] encryptedData = aes.encryptBytes(plainData);

// 解密二进制
byte[] decryptedData = aes.decryptBytes(encryptedData);
String result = new String(decryptedData, "UTF-8");
```

### 1.5 静态快捷方法

```java
// 初始化默认密钥
UAes.initDefaultKey("my-key", "my-iv");

// 静态加密
String encrypted = UAes.defaultEncrypt("Hello World");

// 静态解密
String decrypted = UAes.defaultDecrypt(encrypted);
```

### 1.6 自动 IV 模式

```java
UAes aes = new UAes("key", "");  // IV 为空时自动生成
aes.setPaddingMethod(UAes.PKCS7Padding);

String encrypted = aes.encrypt("Secret");
String decrypted = aes.decrypt(encrypted);
```

---

## 2. DES 加密/解密

### 2.1 概述

`UDes` 提供 DES 对称加密，使用 64 位密钥（实际 56 位）和 64 位 IV。

**算法**: `DES/CBC/PKCS5Padding`

### 2.2 初始化密钥

```java
import com.gdxsoft.easyweb.utils.UDes;

// 初始化默认密钥（至少 8 字节）
UDes.initDefaultKey("8bytekey", "8byteiv");
```

### 2.3 加密/解密示例

#### 使用默认密钥

```java
// 获取实例
UDes des = UDes.getInstance();

String plainText = "Hello World";

// 加密
String encrypted = des.encrypt(plainText);
System.out.println("加密后：" + encrypted);

// 解密
String decrypted = des.decrypt(encrypted);
System.out.println("解密后：" + decrypted);
```

#### 使用自定义密钥

```java
// 创建 DES 实例（密钥和 IV 必须至少 8 字节）
UDes des = new UDes("8bytekey", "8byteiv");

String plainText = "Secret Message";

// 加密
String encrypted = des.encrypt(plainText, "UTF-8");

// 解密
String decrypted = des.decrypt(encrypted, "UTF-8");
```

### 2.4 二进制加密

```java
UDes des = new UDes("8bytekey", "8byteiv");

byte[] plainData = "Binary data".getBytes();

// 加密
byte[] encryptedData = des.encryptBytes(plainData);

// 解密
byte[] decryptedData = des.decryptBytes(encryptedData);
```

---

## 3. RSA 加密/解密

### 3.1 概述

`URsa` 提供 RSA 非对称加密，支持：
- 密钥生成
- 公钥加密/私钥解密
- 私钥加密/公钥解密
- 数字签名/验证

**支持的签名算法**:
- `SHA256withRSA` (默认)
- `SHA1withRSA`
- `MD5withRSA` (不推荐)

### 3.2 生成密钥对

```java
import com.gdxsoft.easyweb.utils.URsa;

URsa rsa = new URsa();

// 生成 2048 位密钥对
rsa.generateRsaKeys(2048);

// 获取公钥和私钥
RSAPublicKey publicKey = rsa.getPublicKey();
RSAPrivateKey privateKey = rsa.getPrivateKey();

// 输出 PEM 格式
String publicKeyPem = rsa.publicKeyToPem();
String privateKeyPem = rsa.privateKeyToPem();

System.out.println(publicKeyPem);
System.out.println(privateKeyPem);
```

### 3.3 保存密钥到文件

```java
import com.gdxsoft.easyweb.utils.UFile;

// 保存 PEM 格式密钥
UFile.createNewTextFile("/path/to/public_key.pem", publicKeyPem);
UFile.createNewTextFile("/path/to/private_key.pem", privateKeyPem);
```

### 3.4 从文件加载密钥

#### 加载 PEM 格式密钥

```java
URsa rsa = new URsa();

// 加载公钥
RSAPublicKey publicKey = rsa.initPublicPemKey("/path/to/public_key.pem");

// 加载私钥
RSAPrivateKey privateKey = rsa.initPrivatePemKey("/path/to/private_key.pem");
```

#### 加载 DER 格式密钥

```java
// 加载公钥
RSAPublicKey publicKey = rsa.initPublicDerKey("/path/to/public_key.der");

// 加载私钥
RSAPrivateKey privateKey = rsa.initPrivateDerKey("/path/to/private_key.der");
```

### 3.5 加密/解密

#### 公钥加密，私钥解密

```java
URsa rsa = new URsa();
rsa.initPublicPemKey("/path/to/public_key.pem");

String plainText = "Secret Message";
byte[] plainData = plainText.getBytes("UTF-8");

// 公钥加密
byte[] encryptedData = rsa.encryptPublic(plainData);
String encryptedBase64 = UConvert.ToBase64String(encryptedData);

// 私钥解密
rsa.initPrivatePemKey("/path/to/private_key.pem");
byte[] decryptedData = rsa.decryptPrivate(encryptedData);
String decryptedText = new String(decryptedData, "UTF-8");
```

#### 私钥加密，公钥解密

```java
URsa rsa = new URsa();
rsa.initPrivatePemKey("/path/to/private_key.pem");

String plainText = "Signed Message";
byte[] plainData = plainText.getBytes("UTF-8");

// 私钥加密
byte[] encryptedData = rsa.encryptPrivate(plainData);

// 公钥解密
rsa.initPublicPemKey("/path/to/public_key.pem");
byte[] decryptedData = rsa.decryptPublic(encryptedData);
```

### 3.6 数字签名

#### 签名

```java
URsa rsa = new URsa();
rsa.initPrivatePemKey("/path/to/private_key.pem");

// 设置签名算法
rsa.setSignAlgorithm(URsa.SIGNATURE_SHA256withRSA);
rsa.setDigestAlgorithm(URsa.DIGEST_SHA256);

String data = "Message to sign";
byte[] dataBytes = data.getBytes("UTF-8");

// 签名（返回二进制）
byte[] signature = rsa.sign(dataBytes);

// 签名（返回 Base64）
String signatureBase64 = rsa.signBase64(dataBytes);
```

#### 验证签名

```java
URsa rsa = new URsa();
rsa.initPublicPemKey("/path/to/public_key.pem");

String data = "Message to sign";
byte[] dataBytes = data.getBytes("UTF-8");
String signatureBase64 = "Base64EncodedSignature";

// 验证签名（使用 Base64 签名）
boolean isValid = rsa.verifyBase64(dataBytes, signatureBase64);
System.out.println("签名验证：" + (isValid ? "有效" : "无效"));

// 验证签名（使用二进制签名）
byte[] signature = UConvert.FromBase64String(signatureBase64);
boolean isValid2 = rsa.verify(dataBytes, signature);
```

### 3.7 使用 Java 或 BC 实现

```java
URsa rsa = new URsa();

// 使用 BouncyCastle（默认）
rsa.setUsingBc(true);

// 使用 Java Security
rsa.setUsingBc(false);
```

---

## 4. Argon2 密码哈希

### 4.1 概述

`UArgon2` 提供 Argon2 密码哈希函数，是 2015 年密码哈希竞赛的获胜者。

**支持的类型**:
- `Argon2d` - 数据依赖型（适合加密货币）
- `Argon2i` - 数据无关型（适合密码哈希）
- `Argon2id` - 混合型（推荐，默认）

### 4.2 快速使用

```java
import com.gdxsoft.easyweb.utils.UArgon2;

String password = "MySecurePassword123";

// 哈希密码
String hashedPassword = UArgon2.hashPwd(password);
System.out.println("哈希后：" + hashedPassword);

// 验证密码
boolean isValid = UArgon2.verifyPwd(password, hashedPassword);
System.out.println("验证结果：" + isValid);
```

### 4.3 自定义参数

```java
UArgon2 argon2 = new UArgon2();

// 设置参数
argon2.setArgon2Type(Argon2Parameters.ARGON2_id);  // 类型
argon2.setVersion(Argon2Parameters.ARGON2_VERSION_13);  // 版本
argon2.setParallelity(4);  // 并行度
argon2.setMemory(65536);   // 内存（KB）
argon2.setIterations(3);   // 迭代次数
argon2.setSaltLength(16);  // 盐长度

// 哈希密码
String password = "MyPassword";
String hashed = argon2.hashPassword(password);

// 验证密码
boolean isValid = argon2.verifyPassword(password, hashed);
```

### 4.4 哈希格式

Argon2 哈希字符串格式：
```
$argon2id$v=19$m=32,t=3,p=1<base64-salt>$<base64-hash>
```

**参数说明**:
- `argon2id` - 算法类型
- `v=19` - 版本号
- `m=32` - 内存大小（KB）
- `t=3` - 迭代次数
- `p=1` - 并行度

---

## 5. 消息摘要

### 5.1 概述

`UDigest` 提供多种消息摘要算法。

**支持的算法**:

| 类别 | 算法 | 说明 |
|------|------|------|
| MD | MD2, MD4, MD5 | 消息摘要 |
| SHA | SHA-1, SHA-224, SHA-256, SHA-384, SHA-512, SHA-3 | 安全哈希 |
| 国密 | SM3 | 中国国密标准 |
| RIPEMD | RIPEMD128, RIPEMD160, RIPEMD256, RIPEMD320 | RIPEMD 系列 |
| GOST | GOST3411, GOST3411_2012_256, GOST3411_2012_512 | 俄罗斯标准 |
| 其他 | Tiger, BLAKE2b, BLAKE2s | 其他算法 |
| HMAC | HmacMD5, HmacSHA1, HmacSHA256 | 带密钥哈希 |

### 5.2 基本摘要

#### Hex 格式

```java
import com.gdxsoft.easyweb.utils.UDigest;

String data = "Hello World";

// MD5
String md5 = UDigest.digestHex(data, "MD5");
System.out.println("MD5: " + md5);

// SHA256
String sha256 = UDigest.digestHex(data, "SHA256");
System.out.println("SHA256: " + sha256);

// SHA512
String sha512 = UDigest.digestHex(data, "SHA512");

// SM3（国密）
String sm3 = UDigest.digestHex(data, "SM3");
```

#### Base64 格式

```java
// MD5 Base64
String md5Base64 = UDigest.digestBase64(data, "MD5");

// SHA256 Base64
String sha256Base64 = UDigest.digestBase64(data, "SHA256");
```

#### 二进制格式

```java
byte[] dataBytes = data.getBytes("UTF-8");

// MD5 二进制
byte[] md5Bytes = UDigest.digest(dataBytes, "MD5");

// SHA256 二进制
byte[] sha256Bytes = UDigest.digest(dataBytes, "SHA256");
```

### 5.3 HMAC 摘要

```java
String data = "Message to sign";
String secret = "SecretKey";

// HmacSHA256 Hex
String hmacSha256Hex = UDigest.digestHex(data, "HmacSHA256", secret);
System.out.println("HmacSHA256 Hex: " + hmacSha256Hex);

// HmacSHA256 Base64
String hmacSha256Base64 = UDigest.digestBase64(data, "HmacSHA256", secret);
System.out.println("HmacSHA256 Base64: " + hmacSha256Base64);

// HmacSHA256 二进制
byte[] hmacSha256Bytes = UDigest.digestHmacSHA256Bytes(data, secret);
```

### 5.4 Utils 快捷方法

```java
import com.gdxsoft.easyweb.utils.Utils;

String data = "Hello World";

// MD5
String md5 = Utils.md5(data);

// SHA1
String sha1 = Utils.sha1(data);

// 字节转 Hex
byte[] bytes = data.getBytes();
String hex = Utils.bytes2hex(bytes);

// Hex 转字节
byte[] fromHex = Utils.hex2bytes(hex);
```

---

## 6. 数字签名

### 6.1 概述

`USign` 提供数字签名工具，支持对 Map、JSONObject、XML Document 进行签名。

### 6.2 Map 签名

```java
import com.gdxsoft.easyweb.utils.USign;
import java.util.HashMap;
import java.util.Map;

Map<String, String> params = new HashMap<>();
params.put("a", "1");
params.put("b", "2");
params.put("c", "3");

String keyName = "sign_key";
String keyValue = "my_secret_key";

// MD5 签名
String md5Sign = USign.signMd5(params, keyName, keyValue, true);

// SHA1 签名
String sha1Sign = USign.signSha1(params, keyName, keyValue, true);
```

### 6.3 JSONObject 签名

```java
import org.json.JSONObject;

JSONObject json = new JSONObject();
json.put("name", "John");
json.put("age", 30);
json.put("city", "New York");

String keyName = "app_key";
String keyValue = "secret123";

// MD5 签名
String md5Sign = USign.signMd5(json, keyName, keyValue);

// SHA1 签名
String sha1Sign = USign.signSha1(json, keyName, keyValue);
```

### 6.4 XML Document 签名

```java
import org.w3c.dom.Document;
import com.gdxsoft.easyweb.utils.UXml;

String xml = "<request><name>John</name><age>30</age></request>";
Document doc = UXml.asDocument(xml);

String keyName = "sign_key";
String keyValue = "secret_key";

// MD5 签名
String md5Sign = USign.signMd5(doc, keyName, keyValue, true);

// SHA1 签名
String sha1Sign = USign.signSha1(doc, keyName, keyValue, true);

// 添加签名节点
USign.addXmlNode(doc, "sign", md5Sign);
```

### 6.5 字符串拼接

```java
Map<String, String> params = new HashMap<>();
params.put("c", "3");
params.put("a", "1");
params.put("b", "2");

// 按 Key 排序拼接：a=1&b=2&c=3
String sorted = USign.concatSortedStr(params, true);
```

---

## 7. Utils 快捷方法

### 7.1 摘要方法

```java
import com.gdxsoft.easyweb.utils.Utils;

String data = "Hello World";

// MD5 摘要（Hex）
String md5 = Utils.md5(data);

// SHA1 摘要（Hex）
String sha1 = Utils.sha1(data);

// 字节数组 MD5
byte[] bytes = data.getBytes();
String md5Bytes = Utils.md5(bytes);
```

### 7.2 编码转换

```java
// 字节转 Hex 字符串
byte[] bytes = {0x48, 0x65, 0x6C, 0x6C, 0x6F};
String hex = Utils.bytes2hex(bytes);  // "48656C6C6F"

// Hex 字符串转字节
byte[] fromHex = Utils.hex2bytes("48656C6C6F");
```

---

## 完整示例

### 示例 1: AES 加密配置文件

```java
import com.gdxsoft.easyweb.utils.UAes;
import com.gdxsoft.easyweb.utils.UFile;

public class ConfigEncryptor {
    
    public static void main(String[] args) throws Exception {
        // 读取配置文件
        String configContent = UFile.readFileText("/path/to/config.json");
        
        // 加密
        UAes.initDefaultKey("my-secret-key-12345", "my-iv-value-1234");
        UAes aes = UAes.getInstance();
        
        String encrypted = aes.encrypt(configContent);
        
        // 保存加密文件
        UFile.createNewTextFile("/path/to/config.json.enc", encrypted);
        
        System.out.println("配置已加密保存");
    }
}
```

### 示例 2: RSA 签名验证

```java
import com.gdxsoft.easyweb.utils.URsa;
import com.gdxsoft.easyweb.utils.UConvert;

public class RsaSignDemo {
    
    public static void main(String[] args) throws Exception {
        URsa rsa = new URsa();
        
        // 生成密钥对
        rsa.generateRsaKeys(2048);
        
        // 保存密钥
        UFile.createNewTextFile("public.pem", rsa.publicKeyToPem());
        UFile.createNewTextFile("private.pem", rsa.privateKeyToPem());
        
        // 签名
        String data = "Important message";
        byte[] dataBytes = data.getBytes("UTF-8");
        
        String signature = rsa.signBase64(dataBytes);
        System.out.println("签名：" + signature);
        
        // 验证
        rsa.initPublicPemKey("public.pem");
        boolean isValid = rsa.verifyBase64(dataBytes, signature);
        System.out.println("验证结果：" + isValid);
    }
}
```

### 示例 3: 用户密码存储

```java
import com.gdxsoft.easyweb.utils.UArgon2;
import com.gdxsoft.easyweb.utils.UFile;
import java.util.HashMap;
import java.util.Map;

public class UserPasswordManager {
    
    private Map<String, String> userPasswords = new HashMap<>();
    
    // 注册用户
    public void registerUser(String username, String password) {
        String hashedPassword = UArgon2.hashPwd(password);
        userPasswords.put(username, hashedPassword);
        System.out.println("用户 " + username + " 注册成功");
    }
    
    // 验证密码
    public boolean login(String username, String password) {
        String hashedPassword = userPasswords.get(username);
        if (hashedPassword == null) {
            return false;
        }
        return UArgon2.verifyPwd(password, hashedPassword);
    }
    
    public static void main(String[] args) {
        UserPasswordManager manager = new UserPasswordManager();
        
        // 注册用户
        manager.registerUser("admin", "SecurePassword123");
        manager.registerUser("user1", "MyPassword456");
        
        // 登录验证
        System.out.println("admin 登录：" + manager.login("admin", "SecurePassword123"));
        System.out.println("admin 登录（错误密码）：" + manager.login("admin", "WrongPassword"));
    }
}
```

### 示例 4: API 请求签名

```java
import com.gdxsoft.easyweb.utils.USign;
import com.gdxsoft.easyweb.utils.UDigest;
import org.json.JSONObject;

public class ApiRequestSigner {
    
    private static final String APP_KEY = "my_app_key";
    private static final String SECRET = "my_secret_key";
    
    public static JSONObject createSignedRequest() {
        // 创建请求参数
        JSONObject params = new JSONObject();
        params.put("timestamp", System.currentTimeMillis());
        params.put("user_id", "12345");
        params.put("action", "transfer");
        params.put("amount", "100.00");
        
        // 生成签名
        String signature = USign.signSha1(params, "app_key", SECRET);
        params.put("sign", signature);
        
        return params;
    }
    
    public static void main(String[] args) {
        JSONObject request = createSignedRequest();
        System.out.println("签名请求：" + request.toString());
    }
}
```

### 示例 5: 敏感数据加密存储

```java
import com.gdxsoft.easyweb.utils.UAes;
import com.gdxsoft.easyweb.utils.UDigest;
import com.gdxsoft.easyweb.utils.UFile;

public class SensitiveDataEncryptor {
    
    private static final String ENCRYPTION_KEY = "my-256-bit-key-for-aes-encryption";
    private static final String ENCRYPTION_IV = "my-16-byte-iv-here";
    
    // 加密敏感数据
    public static String encryptSensitiveData(String data) throws Exception {
        UAes.initDefaultKey(UAes.AES_256_GCM, ENCRYPTION_KEY, ENCRYPTION_IV);
        UAes aes = UAes.getInstance();
        return aes.encrypt(data);
    }
    
    // 解密敏感数据
    public static String decryptSensitiveData(String encryptedData) throws Exception {
        UAes.initDefaultKey(UAes.AES_256_GCM, ENCRYPTION_KEY, ENCRYPTION_IV);
        UAes aes = UAes.getInstance();
        return aes.decrypt(encryptedData);
    }
    
    // 计算数据指纹
    public static String getDataFingerprint(String data) {
        return UDigest.digestHex(data, "SHA256");
    }
    
    public static void main(String[] args) throws Exception {
        String sensitiveData = "身份证号：110101199001011234";
        
        // 加密
        String encrypted = encryptSensitiveData(sensitiveData);
        System.out.println("加密后：" + encrypted);
        
        // 解密
        String decrypted = decryptSensitiveData(encrypted);
        System.out.println("解密后：" + decrypted);
        
        // 指纹
        String fingerprint = getDataFingerprint(sensitiveData);
        System.out.println("数据指纹：" + fingerprint);
    }
}
```

---

## 注意事项

### 1. 密钥安全

- 不要硬编码密钥在代码中
- 使用密钥管理系统或环境变量
- 定期更换密钥

### 2. AES 密钥长度

- Java 默认限制 AES 密钥为 128 位
- 如需 192/256 位，需安装 JCE Unlimited Strength Policy Files

### 3. IV 使用

- IV（初始化向量）应该随机生成
- 不要重复使用相同的 IV 和密钥组合
- GCM/CCM 模式下 IV 长度建议为 12 字节

### 4. RSA 密钥长度

- 推荐使用 2048 位或更长的密钥
- 1024 位已被认为不够安全

### 5. 密码哈希

- 永远不要明文存储密码
- 使用 Argon2 或 bcrypt 等慢哈希算法
- 每个密码使用不同的盐

### 6. 签名验证

- 始终验证接收到的签名
- 使用 HTTPS 传输签名数据
- 防止重放攻击（添加时间戳）

---

## 相关类

| 类名 | 包路径 | 说明 |
|------|--------|------|
| `UAes` | `com.gdxsoft.easyweb.utils` | AES 加密/解密 |
| `UDes` | `com.gdxsoft.easyweb.utils` | DES 加密/解密 |
| `URsa` | `com.gdxsoft.easyweb.utils` | RSA 加密/签名 |
| `UArgon2` | `com.gdxsoft.easyweb.utils` | Argon2 密码哈希 |
| `UDigest` | `com.gdxsoft.easyweb.utils` | 消息摘要 |
| `USign` | `com.gdxsoft.easyweb.utils` | 数字签名工具 |
| `Utils` | `com.gdxsoft.easyweb.utils` | 通用工具（MD5/SHA1 快捷方法） |
| `UConvert` | `com.gdxsoft.easyweb.utils` | Base64 转换 |
| `UFile` | `com.gdxsoft.easyweb.utils` | 文件操作 |
