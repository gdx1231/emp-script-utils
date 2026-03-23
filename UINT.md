# UInt 无符号整数类型使用指南

## 概述

Java 语言本身不支持无符号整数类型，emp-script-utils 提供了三个无符号整数类来弥补这一缺陷，位于 `com.gdxsoft.easyweb.utils.types` 包中：

- **UInt16** - 16 位无符号整数（0 ~ 65,535）
- **UInt32** - 32 位无符号整数（0 ~ 4,294,967,295）
- **UInt64** - 64 位无符号整数（0 ~ 18,446,744,073,709,551,615）

这些类继承自 `Number`，实现了 `Comparable` 接口，支持算术运算、类型转换和比较操作。

---

## 目录

1. [UInt16 - 16 位无符号整数](#1-uint16---16-位无符号整数)
2. [UInt32 - 32 位无符号整数](#2-uint32---32-位无符号整数)
3. [UInt64 - 64 位无符号整数](#3-uint64---64-位无符号整数)
4. [类型转换](#4-类型转换)
5. [算术运算](#5-算术运算)
6. [比较操作](#6-比较操作)
7. [进制转换](#7-进制转换)
8. [字节数组转换](#8-字节数组转换)

---

## 1. UInt16 - 16 位无符号整数

### 1.1 概述

**取值范围**: 0 ~ 65,535 (2^16 - 1)

**常量**:
```java
UInt16.MIN_VALUE  // 0
UInt16.MAX_VALUE  // 65535
```

### 1.2 创建 UInt16

```java
import com.gdxsoft.easyweb.utils.types.UInt16;
import java.math.BigInteger;

// 从 int 创建
UInt16 u1 = UInt16.valueOf(100);

// 从 short 创建
UInt16 u2 = UInt16.valueOf((short) 65535);

// 从 long 创建
UInt16 u3 = UInt16.valueOf(100L);

// 从 BigInteger 创建
BigInteger big = BigInteger.valueOf(50000);
UInt16 u4 = UInt16.valueOf(big);

// 从字符串创建
UInt16 u5 = UInt16.valueOf("65535");
UInt16 u6 = UInt16.valueOf("FFFF", 16);  // 十六进制

// 默认构造函数（值为 0）
UInt16 u7 = new UInt16();
```

### 1.3 基本操作

```java
UInt16 a = UInt16.valueOf(100);
UInt16 b = UInt16.valueOf(50);

// 加法
UInt16 sum = a.add(b);           // 150
UInt16 sum2 = a.add(10);         // 110

// 减法
UInt16 diff = a.subtract(b);     // 50
UInt16 diff2 = a.subtract(10);   // 90

// 乘法
UInt16 prod = a.multiply(b);     // 5000
UInt16 prod2 = a.multiply(10);   // 1000

// 除法
UInt16 quot = a.divide(b);       // 2
UInt16 quot2 = a.divide(10);     // 10

// 获取值
int intValue = a.intValue();         // 100
long longValue = a.longValue();      // 100
short shortValue = a.shortValue();   // 100
float floatValue = a.floatValue();   // 100.0f
double doubleValue = a.doubleValue(); // 100.0
```

### 1.4 溢出处理

```java
try {
    UInt16 overflow = UInt16.valueOf(70000);  // 抛出 ArithmeticException
} catch (ArithmeticException e) {
    System.out.println(e.getMessage());
    // 输出：the result (70000) greate than the UInt16.MAX_VALUE (65535)
}

try {
    UInt16 negative = UInt16.valueOf(-1);  // 抛出 ArithmeticException
} catch (ArithmeticException e) {
    System.out.println(e.getMessage());
    // 输出：the result (-1) is negative
}

// 运算溢出
UInt16 max = UInt16.MAX_VALUE;
try {
    UInt16 result = max.add(1);  // 抛出 ArithmeticException
} catch (ArithmeticException e) {
    System.out.println(e.getMessage());
}
```

---

## 2. UInt32 - 32 位无符号整数

### 2.1 概述

**取值范围**: 0 ~ 4,294,967,295 (2^32 - 1)

**常量**:
```java
UInt32.MIN_VALUE  // 0
UInt32.MAX_VALUE  // 4294967295
```

### 2.2 创建 UInt32

```java
import com.gdxsoft.easyweb.utils.types.UInt32;
import java.math.BigInteger;

// 从 int 创建（自动处理负数）
UInt32 u1 = UInt32.valueOf(100);
UInt32 u2 = UInt32.valueOf(-1);  // 实际值为 4294967295

// 从 long 创建
UInt32 u3 = UInt32.valueOf(4000000000L);

// 从 short 创建
UInt32 u4 = UInt32.valueOf((short) -1);  // 65535

// 从 BigInteger 创建
BigInteger big = BigInteger.valueOf("4000000000");
UInt32 u5 = UInt32.valueOf(big);

// 从字符串创建
UInt32 u6 = UInt32.valueOf("4294967295");
UInt32 u7 = UInt32.valueOf("FFFFFFFF", 16);  // 十六进制

// 默认构造函数（值为 0）
UInt32 u8 = new UInt32();
```

### 2.3 基本操作

```java
UInt32 a = UInt32.valueOf(100);
UInt32 b = UInt32.valueOf(50);

// 加法（支持 UInt16, UInt32, UInt64, int）
UInt32 sum = a.add(b);           // 100 + 50 = 150
UInt32 sum2 = a.add(10);         // 110
UInt32 sum3 = a.add(UInt16.valueOf(5));  // 105

// 减法
UInt32 diff = a.subtract(b);     // 50
UInt32 diff2 = a.subtract(10);   // 90

// 乘法
UInt32 prod = a.multiply(b);     // 5000
UInt32 prod2 = a.multiply(10);   // 1000

// 除法
UInt32 quot = a.divide(b);       // 2
UInt32 quot2 = a.divide(10);     // 10

// 获取值
int intValue = a.intValue();      // 100
long longValue = a.longValue();   // 100
float floatValue = a.floatValue(); // 100.0f
double doubleValue = a.doubleValue(); // 100.0
```

### 2.4 处理负数输入

```java
// Java 的 int 是有符号的，UInt32 会自动处理
UInt32 u1 = UInt32.valueOf(-1);
System.out.println(u1);  // 输出：4294967295

UInt32 u2 = UInt32.valueOf(-10000);
System.out.println(u2);  // 输出：4294957296

// 获取原始 int 值（有符号）
int rawInt = u2.intValue();
System.out.println(rawInt);  // 输出：-10000

// 获取无符号 long 值
long unsignedLong = u2.longValue();
System.out.println(unsignedLong);  // 输出：4294957296
```

---

## 3. UInt64 - 64 位无符号整数

### 3.1 概述

**取值范围**: 0 ~ 18,446,744,073,709,551,615 (2^64 - 1)

**常量**:
```java
UInt64.MIN_VALUE  // 0
UInt64.MAX_VALUE  // 18446744073709551615
```

### 3.2 创建 UInt64

```java
import com.gdxsoft.easyweb.utils.types.UInt64;
import java.math.BigInteger;

// 从 BigInteger 创建（推荐方式）
BigInteger big = new BigInteger("18446744073709551615");
UInt64 u1 = UInt64.valueOf(big);

// 从 long 创建（自动处理负数）
UInt64 u2 = UInt64.valueOf(-1L);  // 实际值为 2^64 - 1

// 从 int 创建
UInt64 u3 = UInt64.valueOf(100);

// 从 short 创建
UInt64 u4 = UInt64.valueOf((short) -1);  // 65535

// 从字符串创建
UInt64 u5 = UInt64.valueOf("18446744073709551615");
UInt64 u6 = UInt64.valueOf("FFFFFFFFFFFFFFFF", 16);  // 十六进制

// 默认构造函数（值为 0）
UInt64 u7 = new UInt64();
```

### 3.3 基本操作

```java
UInt64 a = UInt64.valueOf(100);
UInt64 b = UInt64.valueOf(50);

// 加法（支持 UInt16, UInt32, UInt64, int, long）
UInt64 sum = a.add(b);           // 150
UInt64 sum2 = a.add(10);         // 110
UInt64 sum3 = a.add(100L);       // 200
UInt64 sum4 = a.add(UInt32.valueOf(5));  // 105

// 减法
UInt64 diff = a.subtract(b);     // 50
UInt64 diff2 = a.subtract(10);   // 90
UInt64 diff3 = a.subtract(100L); // 0

// 乘法
UInt64 prod = a.multiply(b);     // 5000
UInt64 prod2 = a.multiply(10);   // 1000
UInt64 prod3 = a.multiply(100L); // 10000

// 除法
UInt64 quot = a.divide(b);       // 2
UInt64 quot2 = a.divide(10);     // 10
UInt64 quot3 = a.divide(100L);   // 1

// 获取值
int intValue = a.intValue();       // 100
long longValue = a.longValue();    // 100
float floatValue = a.floatValue(); // 100.0f
double doubleValue = a.doubleValue(); // 100.0

// 获取 BigInteger
BigInteger big = a.bigInteger();
```

### 3.4 大数运算

```java
// 创建大数
UInt64 max = UInt64.MAX_VALUE;
System.out.println(max);  // 18446744073709551615

// 大数加法
UInt64 a = UInt64.valueOf(new BigInteger("10000000000000000000"));
UInt64 b = UInt64.valueOf(new BigInteger("8000000000000000000"));
UInt64 sum = a.add(b);
System.out.println(sum);  // 18000000000000000000

// 大数乘法
UInt64 c = UInt64.valueOf(new BigInteger("10000000000"));
UInt64 d = UInt64.valueOf(new BigInteger("1000000000"));
UInt64 prod = c.multiply(d);
System.out.println(prod);  // 10000000000000000000
```

---

## 4. 类型转换

### 4.1 UInt 类型间转换

```java
// UInt16 → UInt32 / UInt64
UInt16 u16 = UInt16.valueOf(100);
UInt32 u32 = u16.toUInt32();
UInt64 u64 = u16.toUInt64();

// UInt32 → UInt16 / UInt64
UInt32 u32a = UInt32.valueOf(100);
UInt16 u16a = u32a.toUInt16();  // 可能溢出
UInt64 u64a = u32a.toUInt64();

// UInt64 → UInt16 / UInt32
UInt64 u64b = UInt64.valueOf(100);
UInt16 u16b = u64b.toUInt16();  // 可能溢出
UInt32 u32b = u64b.toUInt32();  // 可能溢出
```

### 4.2 转换为 Java 基本类型

```java
UInt16 u16 = UInt16.valueOf(65535);
short s = u16.shortValue();   // -1 (有符号)
int i = u16.intValue();       // 65535
long l = u16.longValue();     // 65535

UInt32 u32 = UInt32.valueOf(-1);  // 4294967295
int i32 = u32.intValue();     // -1 (有符号表示)
long l32 = u32.longValue();   // 4294967295 (无符号值)

UInt64 u64 = UInt64.valueOf(-1L);  // 2^64 - 1
long l64 = u64.longValue();   // -1 (有符号表示)
```

### 4.3 转换为 BigInteger

```java
UInt16 u16 = UInt16.MAX_VALUE;
BigInteger big16 = u16.bigInteger();  // 65535

UInt32 u32 = UInt32.MAX_VALUE;
BigInteger big32 = u32.bigInteger();  // 4294967295

UInt64 u64 = UInt64.MAX_VALUE;
BigInteger big64 = u64.bigInteger();  // 18446744073709551615
```

### 4.4 转换为字符串

```java
UInt16 u16 = UInt16.valueOf(65535);
String s1 = u16.toString();  // "65535"

UInt32 u32 = UInt32.valueOf(4294967295L);
String s2 = u32.toString();  // "4294967295"

UInt64 u64 = UInt64.valueOf(new BigInteger("18446744073709551615"));
String s3 = u64.toString();  // "18446744073709551615"
```

---

## 5. 算术运算

### 5.1 加法

```java
// UInt16 加法
UInt16 a16 = UInt16.valueOf(60000);
UInt16 b16 = UInt16.valueOf(5000);
try {
    UInt16 sum16 = a16.add(b16);  // 抛出 ArithmeticException
} catch (ArithmeticException e) {
    System.out.println("UInt16 溢出：" + e.getMessage());
}

// UInt32 加法
UInt32 a32 = UInt32.valueOf(4000000000L);
UInt32 b32 = UInt32.valueOf(200000000L);
try {
    UInt32 sum32 = a32.add(b32);  // 抛出 ArithmeticException
} catch (ArithmeticException e) {
    System.out.println("UInt32 溢出：" + e.getMessage());
}

// UInt64 加法（大数）
UInt64 a64 = UInt64.valueOf(new BigInteger("10000000000000000000"));
UInt64 b64 = UInt64.valueOf(new BigInteger("8000000000000000000"));
UInt64 sum64 = a64.add(b64);  // 18000000000000000000
```

### 5.2 减法

```java
UInt16 a16 = UInt16.valueOf(100);
UInt16 b16 = UInt16.valueOf(50);

UInt16 diff1 = a16.subtract(b16);  // 50
UInt16 diff2 = a16.subtract(10);   // 90

try {
    UInt16 diff3 = b16.subtract(a16);  // 抛出 ArithmeticException (负数)
} catch (ArithmeticException e) {
    System.out.println("负数结果：" + e.getMessage());
}
```

### 5.3 乘法

```java
UInt32 a32 = UInt32.valueOf(100000);
UInt32 b32 = UInt32.valueOf(100000);

try {
    UInt32 prod = a32.multiply(b32);  // 10000000000，可能溢出
} catch (ArithmeticException e) {
    System.out.println("UInt32 乘法溢出：" + e.getMessage());
}

// 使用 UInt64 避免溢出
UInt64 a64 = UInt64.valueOf(100000);
UInt64 b64 = UInt64.valueOf(100000);
UInt64 prod64 = a64.multiply(b64);  // 10000000000
```

### 5.4 除法

```java
UInt32 a32 = UInt32.valueOf(100);
UInt32 b32 = UInt32.valueOf(3);

UInt32 quot = a32.divide(b32);  // 33 (整数除法)
UInt32 remainder = a32.subtract(quot.multiply(b32));  // 1 (余数)

System.out.println("商：" + quot + ", 余数：" + remainder);
```

---

## 6. 比较操作

### 6.1 使用 compareTo

```java
UInt32 a = UInt32.valueOf(100);
UInt32 b = UInt32.valueOf(200);
UInt32 c = UInt32.valueOf(100);

int cmp1 = a.compareTo(b);  // -1 (a < b)
int cmp2 = a.compareTo(c);  // 0 (a == c)
int cmp3 = b.compareTo(a);  // 1 (b > a)

// 在排序中使用
List<UInt32> list = new ArrayList<>();
list.add(UInt32.valueOf(300));
list.add(UInt32.valueOf(100));
list.add(UInt32.valueOf(200));
Collections.sort(list);  // [100, 200, 300]
```

### 6.2 使用 compare 静态方法

```java
UInt16 a16 = UInt16.valueOf(100);
UInt16 b16 = UInt16.valueOf(200);

int cmp = UInt16.compare(a16, b16);  // -1

UInt64 a64 = UInt64.valueOf(100);
UInt64 b64 = UInt64.valueOf(200);

int cmp64 = UInt64.compare(a64, b64);  // -1
```

### 6.3 使用 equals

```java
UInt32 a = UInt32.valueOf(100);
UInt32 b = UInt32.valueOf(100);
UInt32 c = UInt32.valueOf(200);

System.out.println(a.equals(b));  // true
System.out.println(a.equals(c));  // false
System.out.println(a.equals(100)); // false (类型不同)
```

---

## 7. 进制转换

### 7.1 十进制字符串

```java
UInt32 u32 = UInt32.valueOf(255);
String decimal = u32.toString();  // "255"
```

### 7.2 十六进制字符串

```java
UInt32 u32 = UInt32.valueOf(255);
String hex = UInt32.toHexString(u32);  // "ff"

UInt16 u16 = UInt16.valueOf(65535);
String hex16 = UInt16.toHexString(u16);  // "ffff"
```

### 7.3 八进制字符串

```java
UInt32 u32 = UInt32.valueOf(64);
String octal = UInt32.toOctalString(u32);  // "100"
```

### 7.4 二进制字符串

```java
UInt16 u16 = UInt16.valueOf(255);
String binary = UInt16.toBinaryString(u16);  // "11111111"
```

### 7.5 自定义进制

```java
UInt32 u32 = UInt32.valueOf(255);

// 2 进制
String bin = UInt32.toString(u32, 2);  // "11111111"

// 8 进制
String oct = UInt32.toString(u32, 8);  // "377"

// 16 进制
String hex = UInt32.toString(u32, 16);  // "ff"

// 36 进制（最大支持）
String base36 = UInt32.toString(u32, 36);  // "73"
```

---

## 8. 字节数组转换

### 8.1 UInt16 转字节数组

```java
UInt16 u16 = UInt16.valueOf(65535);
byte[] bytes16 = u16.toBytes();  // [0, 0, -1, -1] (4 字节，实际使用 2 字节)

// 转换为 ByteBuffer 读取
ByteBuffer buffer = ByteBuffer.wrap(bytes16);
short value = buffer.getShort(2);  // -1 (有符号)
```

### 8.2 UInt32 转字节数组

```java
UInt32 u32 = UInt32.valueOf(4294967295L);  // 0xFFFFFFFF
byte[] bytes32 = u32.toBytes();  // [-1, -1, -1, -1]

// 转换为 ByteBuffer 读取
ByteBuffer buffer = ByteBuffer.wrap(bytes32);
int value = buffer.getInt();  // -1 (有符号表示)
```

### 8.3 UInt64 转字节数组

```java
UInt64 u64 = UInt64.valueOf(-1L);  // 2^64 - 1
byte[] bytes64 = u64.toBytes();  // BigInteger.toByteArray()

// 转换为 ByteBuffer 读取
ByteBuffer buffer = ByteBuffer.wrap(bytes64);
long value = buffer.getLong();  // -1 (有符号表示)
```

### 8.4 从字节数组创建

```java
// UInt16 从字节数组
byte[] bytes16 = {0, 0, 0, 100};  // 最后 2 字节为值
ByteBuffer buffer16 = ByteBuffer.wrap(bytes16);
short rawValue = buffer16.getShort(2);
UInt16 u16 = UInt16.valueOf(Short.toUnsignedInt(rawValue));

// UInt32 从字节数组
byte[] bytes32 = {0, 0, 0, 100};
ByteBuffer buffer32 = ByteBuffer.wrap(bytes32);
int rawValue32 = buffer32.getInt();
UInt32 u32 = UInt32.valueOf(rawValue32);
```

---

## 完整使用示例

### 示例 1: 网络协议解析

```java
import com.gdxsoft.easyweb.utils.types.UInt16;
import com.gdxsoft.easyweb.utils.types.UInt32;
import java.nio.ByteBuffer;

public class NetworkProtocolParser {
    
    // 解析 TCP 头部
    public static class TcpHeader {
        public UInt16 sourcePort;
        public UInt16 destPort;
        public UInt32 seqNumber;
        public UInt32 ackNumber;
        
        public static TcpHeader parse(byte[] data) {
            TcpHeader header = new TcpHeader();
            ByteBuffer buffer = ByteBuffer.wrap(data);
            
            // 源端口 (16 位无符号)
            short srcPortRaw = buffer.getShort();
            header.sourcePort = UInt16.valueOf(Short.toUnsignedInt(srcPortRaw));
            
            // 目标端口 (16 位无符号)
            short dstPortRaw = buffer.getShort();
            header.destPort = UInt16.valueOf(Short.toUnsignedInt(dstPortRaw));
            
            // 序列号 (32 位无符号)
            int seqRaw = buffer.getInt();
            header.seqNumber = UInt32.valueOf(seqRaw);
            
            // 确认号 (32 位无符号)
            int ackRaw = buffer.getInt();
            header.ackNumber = UInt32.valueOf(ackRaw);
            
            return header;
        }
        
        @Override
        public String toString() {
            return String.format("TCP: %d -> %d, Seq=%s, Ack=%s",
                sourcePort, destPort, seqNumber, ackNumber);
        }
    }
    
    public static void main(String[] args) {
        // 模拟 TCP 头部数据
        byte[] tcpData = new byte[16];
        ByteBuffer buffer = ByteBuffer.wrap(tcpData);
        buffer.putShort((short) 80);      // 源端口 80
        buffer.putShort((short) 443);     // 目标端口 443
        buffer.putInt(1000);              // 序列号
        buffer.putInt(2000);              // 确认号
        
        TcpHeader header = TcpHeader.parse(tcpData);
        System.out.println(header);
        // 输出：TCP: 80 -> 443, Seq=1000, Ack=2000
    }
}
```

### 示例 2: 文件大小计算

```java
import com.gdxsoft.easyweb.utils.types.UInt64;
import java.math.BigInteger;

public class FileSizeCalculator {
    
    // 计算总大小（可能超过 Long.MAX_VALUE）
    public static UInt64 calculateTotalSize(long[] fileSizes) {
        UInt64 total = UInt64.valueOf(0);
        for (long size : fileSizes) {
            total = total.add(size);
        }
        return total;
    }
    
    // 格式化文件大小
    public static String formatSize(UInt64 bytes) {
        BigInteger kb = bytes.bigInteger().divide(BigInteger.valueOf(1024));
        BigInteger mb = kb.divide(BigInteger.valueOf(1024));
        BigInteger gb = mb.divide(BigInteger.valueOf(1024));
        BigInteger tb = gb.divide(BigInteger.valueOf(1024));
        
        if (tb.compareTo(BigInteger.ZERO) > 0) {
            return tb.toString() + " TB";
        } else if (gb.compareTo(BigInteger.ZERO) > 0) {
            return gb.toString() + " GB";
        } else if (mb.compareTo(BigInteger.ZERO) > 0) {
            return mb.toString() + " MB";
        } else if (kb.compareTo(BigInteger.ZERO) > 0) {
            return kb.toString() + " KB";
        } else {
            return bytes.toString() + " bytes";
        }
    }
    
    public static void main(String[] args) {
        // 模拟大文件
        long[] sizes = {
            5_000_000_000L,  // 5GB
            3_000_000_000L,  // 3GB
            10_000_000_000L  // 10GB
        };
        
        UInt64 total = calculateTotalSize(sizes);
        System.out.println("总大小：" + formatSize(total));
        // 输出：总大小：16 GB
    }
}
```

### 示例 3: 时间戳处理

```java
import com.gdxsoft.easyweb.utils.types.UInt32;

public class TimestampHandler {
    
    // Unix 时间戳（32 位无符号，到 2106 年）
    public static UInt32 getCurrentTimestamp() {
        long seconds = System.currentTimeMillis() / 1000;
        return UInt32.valueOf(seconds);
    }
    
    // 检查时间戳是否有效
    public static boolean isValidTimestamp(UInt32 timestamp) {
        UInt32 maxTimestamp = UInt32.valueOf(4323456000L);  // 2106-02-07
        return timestamp.compareTo(maxTimestamp) <= 0;
    }
    
    // 计算时间差（秒）
    public static UInt32 timeDiff(UInt32 start, UInt32 end) {
        return end.subtract(start);
    }
    
    public static void main(String[] args) {
        UInt32 now = getCurrentTimestamp();
        System.out.println("当前时间戳：" + now);
        System.out.println("是否有效：" + isValidTimestamp(now));
        
        UInt32 start = UInt32.valueOf(1600000000);
        UInt32 diff = timeDiff(start, now);
        System.out.println("经过秒数：" + diff);
    }
}
```

### 示例 4: IP 地址处理

```java
import com.gdxsoft.easyweb.utils.types.UInt32;

public class IpAddressHandler {
    
    // IP 地址字符串转 UInt32
    public static UInt32 ipToUInt32(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid IP address");
        }
        
        long result = 0;
        for (int i = 0; i < 4; i++) {
            int part = Integer.parseInt(parts[i]);
            result = (result << 8) | (part & 0xFF);
        }
        
        return UInt32.valueOf(result);
    }
    
    // UInt32 转 IP 地址字符串
    public static String uint32ToIp(UInt32 value) {
        long ip = value.longValue();
        return String.format("%d.%d.%d.%d",
            (ip >> 24) & 0xFF,
            (ip >> 16) & 0xFF,
            (ip >> 8) & 0xFF,
            ip & 0xFF
        );
    }
    
    // 检查 IP 是否在范围内
    public static boolean isIpInRange(UInt32 ip, UInt32 start, UInt32 end) {
        return ip.compareTo(start) >= 0 && ip.compareTo(end) <= 0;
    }
    
    public static void main(String[] args) {
        String ipStr = "192.168.1.100";
        UInt32 ip = ipToUInt32(ipStr);
        System.out.println(ipStr + " -> " + ip);
        
        String back = uint32ToIp(ip);
        System.out.println(ip + " -> " + back);
        
        UInt32 rangeStart = ipToUInt32("192.168.1.0");
        UInt32 rangeEnd = ipToUInt32("192.168.1.255");
        System.out.println("在范围内：" + isIpInRange(ip, rangeStart, rangeEnd));
    }
}
```

### 示例 5: 计数器（避免溢出）

```java
import com.gdxsoft.easyweb.utils.types.UInt64;

public class SafeCounter {
    
    private UInt64 count = UInt64.valueOf(0);
    
    public void increment() {
        try {
            count = count.add(1);
        } catch (ArithmeticException e) {
            System.err.println("计数器溢出！");
            throw e;
        }
    }
    
    public void increment(long amount) {
        try {
            count = count.add(amount);
        } catch (ArithmeticException e) {
            System.err.println("计数器溢出！");
            throw e;
        }
    }
    
    public UInt64 getCount() {
        return count;
    }
    
    public void reset() {
        count = UInt64.valueOf(0);
    }
    
    public static void main(String[] args) {
        SafeCounter counter = new SafeCounter();
        
        // 大量计数
        counter.increment(10_000_000_000L);  // 100 亿
        counter.increment(5_000_000_000L);   // 50 亿
        
        System.out.println("计数：" + counter.getCount());
        // 输出：计数：15000000000
        
        System.out.println("是否超过 Integer.MAX_VALUE: " + 
            (counter.getCount().compareTo(UInt64.valueOf(Integer.MAX_VALUE)) > 0));
    }
}
```

---

## 注意事项

### 1. 溢出检测

- 所有运算都会检测溢出
- 溢出时抛出 `ArithmeticException`
- 不会自动回绕（wrap around）

### 2. 负数处理

- 输入负数时，根据类型自动转换
- `UInt32.valueOf(-1)` 返回 `4294967295`
- `intValue()` 返回有符号表示

### 3. 性能考虑

- `UInt64` 基于 `BigInteger`，性能较低
- 频繁运算建议使用 `UInt32` 或 `long`

### 4. 序列化

- 实现 `Serializable` 接口
- 可通过 `toBytes()` 转为字节数组

### 5. 比较操作

- 使用 `compareTo()` 或 `compare()` 进行比较
- 不要直接使用 `<`、`>` 运算符

---

## 相关类

| 类名 | 包路径 | 说明 |
|------|--------|------|
| `UInt16` | `com.gdxsoft.easyweb.utils.types` | 16 位无符号整数 |
| `UInt32` | `com.gdxsoft.easyweb.utils.types` | 32 位无符号整数 |
| `UInt64` | `com.gdxsoft.easyweb.utils.types` | 64 位无符号整数 |
| `BigInteger` | `java.math` | 大整数（UInt64 内部使用） |
| `ByteBuffer` | `java.nio` | 字节缓冲区 |
