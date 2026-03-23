# UFormat 格式化使用指南

## 概述

`UFormat` 是 emp-script-utils 提供的通用格式化工具类，位于 `com.gdxsoft.easyweb.utils` 包中。它支持多种数据类型的格式化，包括日期、数字、货币、百分比、中文大写金额等。

## 核心方法

### formatValue - 通用格式化方法

**方法签名**:
```java
public static String formatValue(String toFormat, Object oriValue, String lang) throws Exception
```

**参数说明**:
| 参数 | 说明 |
|------|------|
| toFormat | 格式化类型（大小写不敏感） |
| oriValue | 原始值 |
| lang | 语言：`enus`（美式英语）或 `zhcn`（简体中文） |

**支持的格式化类型**:

| 类型 | 说明 | 示例 |
|------|------|------|
| `date` | 日期格式 | 2024-03-23 |
| `dateTime` | 日期时间格式 | 2024-03-23 14:30:45 |
| `time` | 时间格式 | 14:30:45 |
| `dateShortTime` | 日期 + 短时间（无秒） | 2024-03-23 14:30 |
| `shortTime` | 短时间（无秒） | 14:30 |
| `shortDate` | 短日期 | 03-23（zhcn）/ 03-23（enus） |
| `shortDateTime` | 短日期时间 | 03-23 14:30 |
| `age` | 年龄计算 | 14 |
| `int` | 整数格式 | 12345 |
| `money` | 货币格式（2 位小数） | 1,234.56 |
| `fixed2` | 保留 2 位小数（无逗号） | 1234.56 |
| `leastMoney` | 清除小数末尾的 0（有逗号） | 1,234.5 |
| `leastDecimal` | 清除小数末尾的 0（无逗号） | 1234.5 |
| `percent` | 百分比格式 | 12.34% |
| `week` | 星期 | 六 / Sat |
| `ChineseMoney` | 中文大写金额 | 壹仟贰佰叁拾肆元伍角陆分 |
| `bin2base64` | 二进制转 Base64 | - |
| `bin2hex` | 二进制转 Hex | - |

---

## 日期格式化

### formatDate 方法

**方法签名**:
```java
public static String formatDate(String toFormat, Object oriValue, String lang) throws Exception
```

### 支持的日期格式类型

| 格式类型 | zhcn 示例 | enus 示例 | 说明 |
|----------|----------|----------|------|
| `date` | 2024-03-23 | 03/23/2024 | 日期 |
| `dateTime` | 2024-03-23 14:30:45 | 03/23/2024 14:30:45 | 日期时间 |
| `time` | 14:30:45 | 14:30:45 | 时间 |
| `dateShortTime` | 2024-03-23 14:30 | 03/23/2024 14:30 | 日期 + 短时间 |
| `shortTime` | 14:30 | 14:30 | 短时间（无秒） |
| `shortDate` | 03-23 | 03-23 | 短日期 |
| `shortDateTime` | 03-23 14:30 | 03-23 14:30 | 短日期时间 |
| `week` | 六 | Sat | 星期 |

### 特殊日期格式

#### 英式/美式日期
| 格式 | 说明 | 示例 |
|------|------|------|
| `DD_MM_YYYY` | 英式日期 | 23/03/2024 |
| `MM_DD_YYYY` | 美式日期 | 03/23/2024 |

#### 中文日期格式
| 格式 | 说明 | 示例 |
|------|------|------|
| `date_zh` | 中文日期 | 2024 年 03 月 23 日 |
| `date_zh1` | 中文日期（无前导零） | 2024 年 3 月 23 日 |
| `date_zh2` | 中文汉字日期 | 二〇二四年三月二十三日 |
| `dateshorttime_zh` | 中文日期时间 | 2024 年 03 月 23 日 14 点 30 分 |
| `dateshorttime_zh1` | 中文日期时间（无前导零） | 2024 年 3 月 23 日 14 点 30 分 |
| `datetime_zh` | 中文完整日期时间 | 2024 年 03 月 23 日 14 点 30 分 45 秒 |
| `datetime_zh1` | 中文完整日期时间（无前导零） | 2024 年 3 月 23 日 14 点 30 分 45 秒 |
| `time_zh` | 中文时间 | 14 点 30 分 45 秒 |
| `time_zh1` | 中文时间（无前导零） | 14 点 30 分 45 秒 |
| `shorttime_zh` | 中文短时间 | 14 点 30 分 |
| `shorttime_zh1` | 中文短时间（无前导零） | 14 点 30 分 |

### Java 使用示例

```java
import com.gdxsoft.easyweb.utils.UFormat;
import com.gdxsoft.easyweb.utils.Utils;
import java.util.Date;

// 格式化当前日期
Date now = new Date();
String dateStr = UFormat.formatDate("date", now, "zhcn");
System.out.println(dateStr);  // 输出：2024-03-23

// 格式化字符串日期
String dateInput = "2024-12-31 22:59:59";
String formatted = UFormat.formatDate("dateTime", dateInput, "zhcn");
System.out.println(formatted);

// 美式日期
String usDate = UFormat.formatDate("MM_DD_YYYY", now, "enus");
System.out.println(usDate);  // 输出：03/23/2024

// 英式日期
String ukDate = UFormat.formatDate("DD_MM_YYYY", now, "enus");
System.out.println(ukDate);  // 输出：23/03/2024

// 中文日期格式
String zhDate = UFormat.formatDate("date_zh", now, "zhcn");
System.out.println(zhDate);  // 输出：2024 年 03 月 23 日

String zhDate1 = UFormat.formatDate("date_zh1", now, "zhcn");
System.out.println(zhDate1);  // 输出：2024 年 3 月 23 日

String zhDate2 = UFormat.formatDate("date_zh2", now, "zhcn");
System.out.println(zhDate2);  // 输出：二〇二四年三月二十三日

// 星期格式
String week = UFormat.formatDate("week", now, "zhcn");
System.out.println(week);  // 输出：六

String weekEn = UFormat.formatDate("week", now, "enus");
System.out.println(weekEn);  // 输出：Sat
```

---

## 数字格式化

### formatInt - 整数格式

**方法签名**:
```java
public static String formatInt(Object oriValue)
```

**说明**: 去除小数部分，返回整数

**示例**:
```java
UFormat.formatInt(123.456);      // 输出："123"
UFormat.formatInt("456.789");    // 输出："456"
UFormat.formatInt(789);          // 输出："789"
```

---

### formatMoney - 货币格式

**方法签名**:
```java
public static String formatMoney(Object oriValue)
```

**说明**: 格式化为带千位分隔符、保留 2 位小数的货币格式

**示例**:
```java
UFormat.formatMoney(132312.4133);    // 输出："132,312.41"
UFormat.formatMoney(132312.4153);    // 输出："132,312.42" (四舍五入)
UFormat.formatMoney("132,312.41");   // 输出："132,312.41"
UFormat.formatMoney(1000);           // 输出："1,000.00"
```

---

### formatPercent - 百分比格式

**方法签名**:
```java
public static String formatPercent(Object oriValue) throws Exception
```

**说明**: 将小数转换为百分比格式，保留 2 位小数

**示例**:
```java
UFormat.formatPercent(0.1234);       // 输出："12.34%"
UFormat.formatPercent(0.5);          // 输出："50.00%"
UFormat.formatPercent(1.0);          // 输出："100.00%"
UFormat.formatPercent("0.075");      // 输出："7.50%"
```

---

### formatNumberClearZero - 清除小数末尾的 0（有逗号）

**方法签名**:
```java
public static String formatNumberClearZero(Object oriValue) throws Exception
```

**说明**: 格式化为带千位分隔符的数字，清除小数末尾的 0，最多保留 4 位小数

**示例**:
```java
UFormat.formatNumberClearZero(1234.5000);    // 输出："1,234.5"
UFormat.formatNumberClearZero(1234.5678);    // 输出："1,234.5678"
UFormat.formatNumberClearZero(1234.0000);    // 输出："1,234"
UFormat.formatNumberClearZero(1234.56789);   // 输出："1,234.5679" (四舍五入)
```

---

### formatDecimalClearZero - 清除小数末尾的 0（无逗号）

**方法签名**:
```java
public static String formatDecimalClearZero(Object oriValue) throws Exception
```

**说明**: 清除小数末尾的 0，最多保留 4 位小数，无千位分隔符

**示例**:
```java
UFormat.formatDecimalClearZero(12.4100);       // 输出："12.41"
UFormat.formatDecimalClearZero("12.5100000");  // 输出："12.51"
UFormat.formatDecimalClearZero(12.0000);       // 输出："12"
UFormat.formatDecimalClearZero(12.56789);      // 输出："12.5679" (四舍五入)
```

---

### fixed2 - 保留 2 位小数（无逗号）

**说明**: 通过 `formatValue` 方法调用，保留 2 位小数，无千位分隔符

**示例**:
```java
UFormat.formatValue("fixed2", 1234.5, "zhcn");    // 输出："1234.50"
UFormat.formatValue("fixed2", 1234.567, "zhcn");  // 输出："1234.57"
```

---

### leastMoney - 清除小数后的 0（有逗号）

**说明**: 通过 `formatValue` 方法调用，清除小数末尾的 0，有千位分隔符

**示例**:
```java
UFormat.formatValue("leastMoney", 1234.5000, "zhcn");  // 输出："1,234.5"
UFormat.formatValue("leastMoney", 1234.00, "zhcn");    // 输出："1,234"
```

---

### leastDecimal - 清除小数后的 0（无逗号）

**说明**: 通过 `formatValue` 方法调用，清除小数末尾的 0，无千位分隔符

**示例**:
```java
UFormat.formatValue("leastDecimal", 1234.5000, "zhcn");  // 输出："1234.5"
UFormat.formatValue("leastDecimal", 1234.00, "zhcn");    // 输出："1234"
```

---

## 特殊格式化

### formatAge - 年龄计算

**方法签名**:
```java
public static String formatAge(Object dbo)
```

**说明**: 根据出生日期计算年龄（当前年 - 出生年）

**示例**:
```java
UFormat.formatAge("2010-01-01");           // 输出："16" (2026 年)
UFormat.formatAge("1990-05-15");           // 输出："35" (2026 年)

Date dbo = Utils.getDate("2010-01-01");
UFormat.formatAge(dbo);                     // 输出："16"
```

---

### formatChineseMoney - 中文大写金额

**方法签名**:
```java
public static String formatChineseMoney(double n)
```

**说明**: 将数字转换为中文大写金额（汉字大写）

**示例**:
```java
UFormat.formatChineseMoney(1234.56);   
// 输出："壹仟贰佰叁拾肆元伍角陆分"

UFormat.formatChineseMoney(10000);     
// 输出："壹万元整"

UFormat.formatChineseMoney(0.50);      
// 输出："伍角"

UFormat.formatChineseMoney(1000000);   
// 输出："壹佰万元整"

UFormat.formatChineseMoney(-1234.56);  
// 输出："负壹仟贰佰叁拾肆元伍角陆分"
```

**通过 formatValue 调用**:
```java
UFormat.formatValue("ChineseMoney", 1234.56, "zhcn");
// 输出："壹仟贰佰叁拾肆元伍角陆分"
```

---

### formatWeek - 星期格式化

**方法签名**:
```java
public static String formatWeek(Object oriValue, String lang) throws Exception
```

**参数说明**:
| 参数 | 说明 |
|------|------|
| oriValue | 日期对象或日期字符串 |
| lang | `enus`（英文）或 `zhcn`（中文） |

**示例**:
```java
Date now = new Date();

// 中文星期
UFormat.formatWeek(now, "zhcn");    // 输出："六"（根据实际日期）
UFormat.formatWeek("2024-03-23", "zhcn");  // 输出："六"

// 英文星期
UFormat.formatWeek(now, "enus");    // 输出："Sat"
UFormat.formatWeek("2024-03-23", "enus");  // 输出："Sat"
```

**星期对照表**:
| 星期 | zhcn | enus |
|------|------|------|
| 星期日 | 日 | Sun |
| 星期一 | 一 | Mon |
| 星期二 | 二 | Tue |
| 星期三 | 三 | Wed |
| 星期四 | 四 | Thu |
| 星期五 | 五 | Fri |
| 星期六 | 六 | Sat |

---

### bin2Base64 - 二进制转 Base64

**方法签名**:
```java
public static String bin2Base64(Object oriValue) throws Exception
```

**说明**: 将 byte 数组转换为 Base64 字符串

**示例**:
```java
byte[] data = "Hello World".getBytes();
String base64 = UFormat.bin2Base64(data);
// 输出："SGVsbG8gV29ybGQ="

// 通过 formatValue 调用
UFormat.formatValue("bin2base64", data, "zhcn");
```

---

### bin2Hex - 二进制转 Hex

**方法签名**:
```java
public static String bin2Hex(Object oriValue) throws Exception
```

**说明**: 将 byte 数组转换为 Hex 字符串

**示例**:
```java
byte[] data = "Hello".getBytes();
String hex = UFormat.bin2Hex(data);
// 输出："48656C6C6F"

// 通过 formatValue 调用
UFormat.formatValue("bin2hex", data, "zhcn");
```

---

## objectToString - 对象转字符串

**方法签名**:
```java
public static String objectToString(Object oriValue)
```

**说明**: 将对象转换为字符串，数组类型会用逗号分隔拼接

**示例**:
```java
// 普通对象
UFormat.objectToString("Hello");        // 输出："Hello"
UFormat.objectToString(123);            // 输出："123"

// 整数数组
int[] arr = {1, 2, 3, 4, 5};
UFormat.objectToString(arr);            // 输出："1, 2, 3, 4, 5"

// 字符串数组
String[] strs = {"a", "b", "c"};
UFormat.objectToString(strs);           // 输出："a, b, c"

// byte 数组（转 Base64）
byte[] bytes = "test".getBytes();
UFormat.objectToString(bytes);          // 输出 Base64 字符串
```

---

## calcNumberScale - 比例计算

**方法签名**:
```java
public static Object calcNumberScale(Object ori, BigDecimal numberScale)
```

**说明**: 计算数字除以比例后的数值

**示例**:
```java
import java.math.BigDecimal;

// 转换为"万"单位
UFormat.calcNumberScale(10000, new BigDecimal(10000));    // 输出：1.0

// 转换为"百万"单位
UFormat.calcNumberScale(1500000, new BigDecimal(1000000)); // 输出：1.5

// 比例为 1 时返回原值
UFormat.calcNumberScale(100, new BigDecimal(1));          // 输出：100
```

---

## 完整使用示例

### 示例 1：综合格式化

```java
import com.gdxsoft.easyweb.utils.UFormat;
import com.gdxsoft.easyweb.utils.Utils;
import java.math.BigDecimal;
import java.util.Date;

public class FormatDemo {
    public static void main(String[] args) throws Exception {
        Date now = new Date();
        
        // 日期格式化
        System.out.println("日期：" + UFormat.formatDate("date", now, "zhcn"));
        System.out.println("日期时间：" + UFormat.formatDate("dateTime", now, "zhcn"));
        System.out.println("星期：" + UFormat.formatDate("week", now, "zhcn"));
        
        // 数字格式化
        double amount = 1234567.89;
        System.out.println("货币：" + UFormat.formatMoney(amount));
        System.out.println("整数：" + UFormat.formatInt(amount));
        System.out.println("百分比：" + UFormat.formatPercent(0.85));
        
        // 特殊格式化
        System.out.println("年龄：" + UFormat.formatAge("1990-01-01"));
        System.out.println("中文金额：" + UFormat.formatChineseMoney(amount));
        
        // 通用格式化
        System.out.println("固定 2 位小数：" + UFormat.formatValue("fixed2", amount, "zhcn"));
        System.out.println("清除零：" + UFormat.formatValue("leastDecimal", amount, "zhcn"));
    }
}
```

### 示例 2：中文日期格式化

```java
Date now = new Date();

// 标准中文日期
System.out.println(UFormat.formatDate("date_zh", now, "zhcn"));
// 输出：2024 年 03 月 23 日

// 简洁中文日期
System.out.println(UFormat.formatDate("date_zh1", now, "zhcn"));
// 输出：2024 年 3 月 23 日

// 汉字中文日期
System.out.println(UFormat.formatDate("date_zh2", now, "zhcn"));
// 输出：二〇二四年三月二十三日

// 中文日期时间
System.out.println(UFormat.formatDate("dateshorttime_zh", now, "zhcn"));
// 输出：2024 年 03 月 23 日 14 点 30 分
```

### 示例 3：货币相关格式化

```java
double salary = 12345.6789;

// 标准货币格式
System.out.println(UFormat.formatMoney(salary));
// 输出："12,345.68"

// 保留 2 位小数（无逗号）
System.out.println(UFormat.formatValue("fixed2", salary, "zhcn"));
// 输出："12345.68"

// 清除末尾零（有逗号）
System.out.println(UFormat.formatValue("leastMoney", 12345.5000, "zhcn"));
// 输出："12,345.5"

// 清除末尾零（无逗号）
System.out.println(UFormat.formatValue("leastDecimal", 12345.5000, "zhcn"));
// 输出："12345.5"

// 中文大写金额
System.out.println(UFormat.formatChineseMoney(salary));
// 输出："壹万贰仟叁佰肆拾伍元陆角捌分"
```

---

## 常量定义

```java
// 日期格式常量
public static final String DATE_FROMAT_UK = "dd/MM/yyyy";      // 英式
public static final String DATE_FROMAT_US = "MM/dd/yyyy";      // 美式
public static String DATE_FROMAT_ZHCN = "yyyy-MM-dd";          // 中文
public static String DATE_FROMAT_ENUS = DATE_FROMAT_US;        // 默认美式
```

---

## 注意事项

1. **空值处理**: 所有格式化方法对 `null` 输入返回 `null`
2. **异常处理**: 部分方法可能抛出 `Exception`，需要适当处理
3. **语言参数**: `lang` 参数支持 `enus`（美式英语）和 `zhcn`（简体中文）
4. **四舍五入**: 货币和百分比格式化采用四舍五入
5. **小数精度**: `leastMoney` 和 `leastDecimal` 最多保留 4 位小数
6. **中文金额**: 支持负数，会自动添加"负"字前缀
7. **日期解析**: 支持 ISO 8601 格式（含 `T` 和 `Z`）

---

## 相关类

| 类名 | 包路径 | 说明 |
|------|--------|------|
| `Utils` | `com.gdxsoft.easyweb.utils` | 通用工具类（日期解析等） |
| `UConvert` | `com.gdxsoft.easyweb.utils` | 类型转换工具 |
| `UFormat` | `com.gdxsoft.easyweb.utils` | 格式化工具 |
