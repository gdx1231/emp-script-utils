# UConvert 类型转换使用指南

## 概述

`UConvert` 是 emp-script-utils 提供的类型转换工具类，位于 `com.gdxsoft.easyweb.utils` 包中。它模仿 .NET Framework 的 `System.Convert` 类，提供多种数据类型之间的转换方法。

**主要功能**:
- Base64 编码/解码
- 布尔类型转换
- 数值类型转换（byte, short, int, long, float, double）
- 字符类型转换
- 字符串转换

---

## Base64 编码/解码

### ToBase64String - 编码为 Base64

**方法签名**:
```java
// 完整数组编码
public static String ToBase64String(byte[] inArray)

// 部分数组编码
public static String ToBase64String(byte[] inArray, int offset, int length)
```

**参数说明**:
| 参数 | 说明 |
|------|------|
| inArray | 要编码的字节数组 |
| offset | 起始偏移量 |
| length | 编码的字节数 |

**返回值**: Base64 编码的字符串

**示例**:
```java
import com.gdxsoft.easyweb.utils.UConvert;

// 完整数组编码
byte[] data = "Hello World".getBytes("UTF-8");
String base64 = UConvert.ToBase64String(data);
System.out.println(base64);  // 输出："SGVsbG8gV29ybGQ="

// 部分数组编码
byte[] data2 = new byte[100];
System.arraycopy("Partial".getBytes(), 0, data2, 0, 7);
String partialBase64 = UConvert.ToBase64String(data2, 0, 7);
System.out.println(partialBase64);  // 输出："UGFydGlhbA=="
```

---

### FromBase64String - 从 Base64 解码

**方法签名**:
```java
public static byte[] FromBase64String(String s) throws IOException
```

**参数说明**:
| 参数 | 说明 |
|------|------|
| s | Base64 编码的字符串 |

**返回值**: 解码后的字节数组

**特性**:
- 自动移除换行符（`\n`, `\r`）
- 修复非法 Base64 字符问题

**示例**:
```java
import com.gdxsoft.easyweb.utils.UConvert;
import java.io.IOException;

// 基本解码
String base64 = "SGVsbG8gV29ybGQ=";
byte[] data = UConvert.FromBase64String(base64);
String text = new String(data, "UTF-8");
System.out.println(text);  // 输出："Hello World"

// 带换行符的 Base64（自动处理）
String base64WithNewlines = "SGVsbG8g\nV29ybGQ=\r\n";
byte[] data2 = UConvert.FromBase64String(base64WithNewlines);
String text2 = new String(data2, "UTF-8");
System.out.println(text2);  // 输出："Hello World"
```

---

## 布尔类型转换 (ToBoolean)

### 方法列表

```java
ToBoolean(boolean value)   // boolean → boolean
ToBoolean(byte value)      // byte → boolean
ToBoolean(char value)      // char → boolean
ToBoolean(double value)    // double → boolean
ToBoolean(short value)     // short → boolean
ToBoolean(int value)       // int → boolean
ToBoolean(long value)      // long → boolean
ToBoolean(float value)     // float → boolean
ToBoolean(String value)    // String → boolean
```

### 转换规则

| 输入类型 | 输入值 | 输出 |
|----------|--------|------|
| boolean | true/false | true/false |
| byte | 0 | false |
| byte | 非 0 | true |
| char | '0' | false |
| char | 非'0' | true |
| double | 0.0 | false |
| double | 非 0.0 | true |
| short/int/long | 0 | false |
| short/int/long | 非 0 | true |
| float | 0.0f | false |
| float | 非 0.0f | true |
| String | null | false |
| String | "true" | true |
| String | 其他 | false |

### 示例

```java
import com.gdxsoft.easyweb.utils.UConvert;

// 数值转布尔
boolean b1 = UConvert.ToBoolean(1);      // true
boolean b2 = UConvert.ToBoolean(0);      // false
boolean b3 = UConvert.ToBoolean(-1);     // true
boolean b4 = UConvert.ToBoolean(3.14);   // true
boolean b5 = UConvert.ToBoolean(0.0);    // false

// 字符串转布尔
boolean b6 = UConvert.ToBoolean("true");   // true
boolean b7 = UConvert.ToBoolean("false");  // false
boolean b8 = UConvert.ToBoolean(null);     // false
boolean b9 = UConvert.ToBoolean("1");      // false (Java Boolean.getBoolean)

// 字符转布尔
boolean b10 = UConvert.ToBoolean('a');   // true
boolean b11 = UConvert.ToBoolean('0');   // false
```

---

## 字节类型转换 (ToByte)

### 方法列表

```java
ToByte(boolean value)    // boolean → byte
ToByte(byte value)       // byte → byte
ToByte(char value)       // char → byte
ToByte(double value)     // double → byte
ToByte(short value)      // short → byte
ToByte(int value)        // int → byte
ToByte(long value)       // long → byte
ToByte(float value)      // float → byte
ToByte(String value)     // String → byte
ToByte(String value, int fromBase)  // 指定进制
```

### 转换规则

| 输入类型 | 示例 | 输出 |
|----------|------|------|
| boolean | true | 1 |
| boolean | false | 0 |
| byte | 65 | 65 |
| char | 'A' | 65 |
| double | 65.9 | 65 (截断) |
| short/int/long | 65 | 65 |
| float | 65.5f | 65 (截断) |
| String | "65" | 65 |
| String | "1010101", 2 | 85 (二进制) |
| String | "41", 16 | 65 (十六进制) |

### 示例

```java
import com.gdxsoft.easyweb.utils.UConvert;

// 布尔转字节
byte b1 = UConvert.ToByte(true);    // 1
byte b2 = UConvert.ToByte(false);   // 0

// 数值转字节
byte b3 = UConvert.ToByte(65);      // 65
byte b4 = UConvert.ToByte(256);     // 0 (溢出)
byte b5 = UConvert.ToByte(3.14);    // 3

// 字符转字节
byte b6 = UConvert.ToByte('A');     // 65

// 字符串转字节
byte b7 = UConvert.ToByte("128");   // -128 (溢出)
byte b8 = UConvert.ToByte("1010", 2);  // 10 (二进制)
byte b9 = UConvert.ToByte("FF", 16);   // -1 (十六进制)
```

---

## 字符类型转换 (ToChar)

### 方法列表

```java
ToChar(boolean value)    // boolean → char
ToChar(byte value)       // byte → char
ToChar(char value)       // char → char
ToChar(double value)     // double → char
ToChar(short value)      // short → char
ToChar(int value)        // int → char
ToChar(long value)       // long → char
ToChar(Object value)     // Object → char
```

### 转换规则

| 输入类型 | 示例 | 输出 |
|----------|------|------|
| boolean | true | 't' |
| boolean | false | 'f' |
| byte | 65 | 'A' |
| char | 'A' | 'A' |
| double | 65.0 | 'A' (取首字符) |
| short/int/long | 65 | 'A' (取首字符) |
| Object | "ABC" | 'A' |

### 示例

```java
import com.gdxsoft.easyweb.utils.UConvert;

// 布尔转字符
char c1 = UConvert.ToChar(true);    // 't'
char c2 = UConvert.ToChar(false);   // 'f'

// 数值转字符
char c3 = UConvert.ToChar(65);      // 'A'
char c4 = UConvert.ToChar(97);      // 'a'
char c5 = UConvert.ToChar(48);      // '0'

// 字符串转字符（取首字符）
char c6 = UConvert.ToChar("ABC");   // 'A'
char c7 = UConvert.ToChar(123);     // '1'
```

---

## 短整数类型转换 (ToInt16)

### 方法列表

```java
ToInt16(boolean value)   // boolean → short
ToInt16(byte value)      // byte → short
ToInt16(char value)      // char → short
ToInt16(double value)    // double → short
ToInt16(short value)     // short → short
ToInt16(int value)       // int → short
ToInt16(long value)      // long → short
ToInt16(float value)     // float → short
ToInt16(String value)    // String → short
ToInt16(String value, int fromBase)  // 指定进制
```

### 转换规则

| 输入类型 | 示例 | 输出 |
|----------|------|------|
| boolean | true | 1 |
| boolean | false | 0 |
| byte | 65 | 65 |
| char | 'A' | 65 |
| double | 65.9 | 66 (四舍五入) |
| short | 100 | 100 |
| int | 100 | 100 |
| int | 100000 | 抛出 Overflow_Int16 |
| long | 100 | 100 |
| float | 65.5f | 66 (四舍五入) |
| String | "100" | 100 |
| String | "1100100", 2 | 100 (二进制) |

### 示例

```java
import com.gdxsoft.easyweb.utils.UConvert;

// 布尔转短整数
short s1 = UConvert.ToInt16(true);    // 1
short s2 = UConvert.ToInt16(false);   // 0

// 数值转短整数
short s3 = UConvert.ToInt16(100);     // 100
short s4 = UConvert.ToInt16(65.6);    // 66 (四舍五入)
short s5 = UConvert.ToInt16('A');     // 65

// 字符串转短整数
short s6 = UConvert.ToInt16("32767");  // 32767 (最大值)
short s7 = UConvert.ToInt16("100", 2); // 4 (二进制)
short s8 = UConvert.ToInt16("FF", 16); // 255 (十六进制)

// 溢出处理
try {
    short s9 = UConvert.ToInt16(100000);  // 抛出 Overflow_Int16
} catch (Exception e) {
    System.out.println(e.getMessage());
}
```

---

## 整数类型转换 (ToInt32)

### 方法列表

```java
ToInt32(boolean value)   // boolean → int
ToInt32(byte value)      // byte → int
ToInt32(char value)      // char → int
ToInt32(double value)    // double → int
ToInt32(short value)     // short → int
ToInt32(int value)       // int → int
ToInt32(long value)      // long → int
ToInt32(float value)     // float → int
ToInt32(String value)    // String → int
ToInt32(String value, int fromBase)  // 指定进制
```

### 转换规则

| 输入类型 | 示例 | 输出 |
|----------|------|------|
| boolean | true | 1 |
| boolean | false | 0 |
| byte | 65 | 65 |
| char | 'A' | 65 |
| double | 65.4 | 65 (四舍五入) |
| double | 65.6 | 66 (四舍五入) |
| double | 2147483648.0 | 抛出 Overflow_int32 |
| short/int | 100 | 100 |
| long | 100 | 100 |
| float | 65.5f | 66 (四舍五入) |
| String | "100" | 100 |
| String | "100.9" | 100 (取整) |
| String | "1100100", 2 | 100 (二进制) |

### 示例

```java
import com.gdxsoft.easyweb.utils.UConvert;

// 布尔转整数
int i1 = UConvert.ToInt32(true);    // 1
int i2 = UConvert.ToInt32(false);   // 0

// 数值转整数
int i3 = UConvert.ToInt32(100);     // 100
int i4 = UConvert.ToInt32(65.4);    // 65 (四舍五入)
int i5 = UConvert.ToInt32(65.6);    // 66 (四舍五入)
int i6 = UConvert.ToInt32('A');     // 65

// 字符串转整数
int i7 = UConvert.ToInt32("100");       // 100
int i8 = UConvert.ToInt32("100.9");     // 100 (取整部分)
int i9 = UConvert.ToInt32("1100100", 2); // 100 (二进制)
int i10 = UConvert.ToInt32("64", 16);   // 100 (十六进制)

// 溢出处理
try {
    int i11 = UConvert.ToInt32(3000000000.0);  // 抛出 Overflow_int32
} catch (Exception e) {
    System.out.println(e.getMessage());
}
```

---

## 长整数类型转换 (ToInt64)

### 方法列表

```java
ToInt64(boolean value)   // boolean → long
ToInt64(byte value)      // byte → long
ToInt64(char value)      // char → long
ToInt64(double value)    // double → long
ToInt64(short value)     // short → long
ToInt64(int value)       // int → long
ToInt64(long value)      // long → long
ToInt64(float value)     // float → long
ToInt64(String value)    // String → long
ToInt64(String value, int fromBase)  // 指定进制
```

### 转换规则

| 输入类型 | 示例 | 输出 |
|----------|------|------|
| boolean | true | 1 |
| boolean | false | 0 |
| byte | 65 | 65 |
| char | 'A' | 65 |
| double | 65.9 | 66 (四舍五入) |
| short/int/long | 100 | 100 |
| float | 65.5f | 66 (四舍五入) |
| String | "100" | 100 |
| String | "1100100", 2 | 100 (二进制) |

### 示例

```java
import com.gdxsoft.easyweb.utils.UConvert;

// 布尔转长整数
long l1 = UConvert.ToInt64(true);    // 1
long l2 = UConvert.ToInt64(false);   // 0

// 数值转长整数
long l3 = UConvert.ToInt64(100);     // 100
long l4 = UConvert.ToInt64(65.9);    // 66 (四舍五入)
long l5 = UConvert.ToInt64('A');     // 65

// 字符串转长整数
long l6 = UConvert.ToInt64("1000000");        // 1000000
long l7 = UConvert.ToInt64("1100100", 2);     // 100 (二进制)
long l8 = UConvert.ToInt64("64", 16);         // 100 (十六进制)

// 大数
long l9 = UConvert.ToInt64("9223372036854775807");  // Long.MAX_VALUE
```

---

## 浮点数类型转换

### ToDouble - 双精度浮点数

**方法列表**:
```java
ToDouble(boolean value)   // boolean → double
ToDouble(byte value)      // byte → double
ToDouble(char value)      // char → double
ToDouble(double value)    // double → double
ToDouble(short value)     // short → double
ToDouble(int value)       // int → double
ToDouble(long value)      // long → double
ToDouble(Object value)    // Object → double
ToDouble(String value)    // String → double
```

**示例**:
```java
import com.gdxsoft.easyweb.utils.UConvert;

// 布尔转双精度
double d1 = UConvert.ToDouble(true);    // 1.0
double d2 = UConvert.ToDouble(false);   // 0.0

// 数值转双精度
double d3 = UConvert.ToDouble(100);     // 100.0
double d4 = UConvert.ToDouble('A');     // 65.0

// 字符串转双精度
double d5 = UConvert.ToDouble("3.14159");  // 3.14159
double d6 = UConvert.ToDouble(null);       // 0.0
double d7 = UConvert.ToDouble("100");      // 100.0

// 对象转双精度
double d8 = UConvert.ToDouble((Object)"3.14");  // 3.14
double d9 = UConvert.ToDouble((Object)null);    // 0.0
```

---

### ToSingle - 单精度浮点数

**方法列表**:
```java
ToSingle(boolean value)   // boolean → float
ToSingle(byte value)      // byte → float
ToSingle(char value)      // char → float
ToSingle(double value)    // double → float
ToSingle(short value)     // short → float
ToSingle(int value)       // int → float
ToSingle(long value)      // long → float
ToSingle(float value)     // float → float
ToSingle(String value)    // String → float
```

**示例**:
```java
import com.gdxsoft.easyweb.utils.UConvert;

// 布尔转单精度
float f1 = UConvert.ToSingle(true);    // 1.0f
float f2 = UConvert.ToSingle(false);   // 0.0f

// 数值转单精度
float f3 = UConvert.ToSingle(100);     // 100.0f
float f4 = UConvert.ToSingle(3.14159); // 3.14159f

// 字符串转单精度
float f5 = UConvert.ToSingle("3.14");  // 3.14f
float f6 = UConvert.ToSingle(null);    // 0.0f
```

---

## 字符串类型转换 (ToString)

### 方法列表

```java
ToString(boolean value)   // boolean → String
ToString(byte value)      // byte → String
ToString(char value)      // char → String
ToString(double value)    // double → String
ToString(short value)     // short → String
ToString(int value)       // int → String
ToString(long value)      // long → String
ToString(Object value)    // Object → String
ToString(float value)     // float → String
ToString(String value)    // String → String
```

### 转换规则

| 输入类型 | 示例 | 输出 |
|----------|------|------|
| boolean | true | "true" |
| byte | 65 | "65" |
| char | 'A' | "A" |
| double | 65.0 | "65" (无小数部分) |
| double | 65.5 | "65.5" |
| short/int/long | 100 | "100" |
| float | 3.14f | "3.14" |
| Object | null | null |
| Object | "ABC" | "ABC" |
| String | null | null |
| String | "ABC" | "ABC" |

### 示例

```java
import com.gdxsoft.easyweb.utils.UConvert;

// 布尔转字符串
String s1 = UConvert.ToString(true);    // "true"
String s2 = UConvert.ToString(false);   // "false"

// 数值转字符串
String s3 = UConvert.ToString(100);     // "100"
String s4 = UConvert.ToString(65.0);    // "65" (无小数部分)
String s5 = UConvert.ToString(65.5);    // "65.5"
String s6 = UConvert.ToString(3.14159); // "3.14159"

// 字符转字符串
String s7 = UConvert.ToString('A');     // "A"

// 对象转字符串
String s8 = UConvert.ToString((Object)"ABC");  // "ABC"
String s9 = UConvert.toString((Object)null);   // null

// 字符串（直接返回）
String s10 = UConvert.ToString("ABC");  // "ABC"
String s11 = UConvert.toString((String)null);  // null
```

---

## 完整使用示例

### 示例 1: Base64 编码/解码

```java
import com.gdxsoft.easyweb.utils.UConvert;
import java.io.IOException;

public class Base64Demo {
    public static void main(String[] args) throws IOException {
        // 编码
        String original = "Hello World 你好世界";
        byte[] bytes = original.getBytes("UTF-8");
        String base64 = UConvert.ToBase64String(bytes);
        System.out.println("Base64: " + base64);
        
        // 解码
        byte[] decoded = UConvert.FromBase64String(base64);
        String result = new String(decoded, "UTF-8");
        System.out.println("解码后：" + result);
        
        // 文件内容 Base64
        byte[] fileContent = java.nio.file.Files.readAllBytes(
            java.nio.file.Paths.get("file.txt")
        );
        String fileBase64 = UConvert.ToBase64String(fileContent);
    }
}
```

### 示例 2: 类型转换工具类

```java
import com.gdxsoft.easyweb.utils.UConvert;

public class TypeConverter {
    
    // 安全转换整数
    public static int safeToInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return UConvert.ToInt32(value.toString());
        } catch (Exception e) {
            return 0;
        }
    }
    
    // 安全转换布尔
    public static boolean safeToBoolean(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return UConvert.ToBoolean(value.toString());
    }
    
    // 安全转换双精度
    public static double safeToDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return UConvert.ToDouble(value.toString());
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    public static void main(String[] args) {
        System.out.println(safeToInt("100"));        // 100
        System.out.println(safeToInt(null));         // 0
        System.out.println(safeToInt("abc"));        // 0
        
        System.out.println(safeToBoolean("true"));   // true
        System.out.println(safeToBoolean(1));        // true
        System.out.println(safeToBoolean(null));     // false
        
        System.out.println(safeToDouble("3.14"));    // 3.14
        System.out.println(safeToDouble(null));      // 0.0
    }
}
```

### 示例 3: 配置值转换

```java
import com.gdxsoft.easyweb.utils.UConvert;
import java.util.Properties;

public class ConfigParser {
    
    private Properties config;
    
    public ConfigParser(String configPath) throws Exception {
        config = new Properties();
        config.load(new java.io.FileInputStream(configPath));
    }
    
    public int getInt(String key, int defaultValue) {
        String value = config.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return UConvert.ToInt32(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    public double getDouble(String key, double defaultValue) {
        String value = config.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return UConvert.ToDouble(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = config.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return UConvert.ToBoolean(value);
    }
    
    public String getBase64(String key) throws Exception {
        String base64 = config.getProperty(key);
        if (base64 == null) {
            return null;
        }
        byte[] decoded = UConvert.FromBase64String(base64);
        return new String(decoded, "UTF-8");
    }
    
    public void setBase64(String key, String value) throws Exception {
        byte[] bytes = value.getBytes("UTF-8");
        String base64 = UConvert.ToBase64String(bytes);
        config.setProperty(key, base64);
    }
}
```

### 示例 4: 数据序列化

```java
import com.gdxsoft.easyweb.utils.UConvert;
import java.util.Base64;

public class DataSerializer {
    
    // 序列化对象为 Base64
    public static String serialize(Object obj) throws Exception {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
        oos.writeObject(obj);
        byte[] bytes = baos.toByteArray();
        return UConvert.ToBase64String(bytes);
    }
    
    // 反序列化 Base64 为对象
    public static Object deserialize(String base64) throws Exception {
        byte[] bytes = UConvert.FromBase64String(base64);
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(bytes);
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
        return ois.readObject();
    }
    
    public static void main(String[] args) throws Exception {
        // 序列化
        java.util.Date now = new java.util.Date();
        String serialized = serialize(now);
        System.out.println("序列化：" + serialized);
        
        // 反序列化
        java.util.Date restored = (java.util.Date) deserialize(serialized);
        System.out.println("反序列化：" + restored);
    }
}
```

### 示例 5: API 数据传输

```java
import com.gdxsoft.easyweb.utils.UConvert;
import org.json.JSONObject;

public class ApiDataTransfer {
    
    // 创建 API 请求数据
    public static String createRequestData(String action, int userId, double amount) {
        JSONObject json = new JSONObject();
        json.put("action", action);
        json.put("user_id", userId);
        json.put("amount", amount);
        json.put("timestamp", System.currentTimeMillis());
        
        // 将 JSON 转为 Base64 传输
        byte[] bytes = json.toString().getBytes();
        return UConvert.ToBase64String(bytes);
    }
    
    // 解析 API 请求数据
    public static JSONObject parseRequestData(String base64Data) throws Exception {
        byte[] bytes = UConvert.FromBase64String(base64Data);
        String jsonStr = new String(bytes, "UTF-8");
        return new JSONObject(jsonStr);
    }
    
    // 转换 API 参数
    public static int parseUserId(Object userIdObj) {
        if (userIdObj == null) {
            return 0;
        }
        return UConvert.ToInt32(userIdObj.toString());
    }
    
    public static double parseAmount(Object amountObj) {
        if (amountObj == null) {
            return 0.0;
        }
        return UConvert.ToDouble(amountObj.toString());
    }
    
    public static void main(String[] args) throws Exception {
        // 创建请求
        String requestData = createRequestData("transfer", 12345, 100.50);
        System.out.println("请求数据：" + requestData);
        
        // 解析请求
        JSONObject parsed = parseRequestData(requestData);
        System.out.println("解析后：" + parsed.toString());
        
        // 参数转换
        int userId = parseUserId(parsed.get("user_id"));
        double amount = parseAmount(parsed.get("amount"));
        System.out.println("User ID: " + userId);
        System.out.println("Amount: " + amount);
    }
}
```

---

## 注意事项

### 1. Base64 编码

- `FromBase64String` 自动移除换行符
- 使用 Java 8+ 的 `Base64` 类
- 修复了非法 Base64 字符问题

### 2. 数值转换

- 浮点数转整数采用四舍五入
- 溢出时抛出异常
- 字符串转数值时自动取整（去除小数部分）

### 3. 空值处理

- `null` 转数值返回 0 或 0.0
- `null` 转布尔返回 false
- `null` 转字符串返回 null

### 4. 字符串转换

- `ToString(double)` 会自动去除无意义的小数部分
- `ToString(Object)` 调用对象的 `toString()` 方法

### 5. 进制转换

- 支持 2-36 进制转换
- 输入字符串可以包含负号

---

## 相关类

| 类名 | 包路径 | 说明 |
|------|--------|------|
| `Utils` | `com.gdxsoft.easyweb.utils` | 通用工具（字节转 Hex 等） |
| `UFile` | `com.gdxsoft.easyweb.utils` | 文件操作（Base64 文件读写） |
| `UAes` | `com.gdxsoft.easyweb.utils` | AES 加密（使用 Base64） |
| `UDes` | `com.gdxsoft.easyweb.utils` | DES 加密（使用 Base64） |
| `URsa` | `com.gdxsoft.easyweb.utils` | RSA 加密（使用 Base64） |
