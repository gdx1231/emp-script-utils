# UPinYin 拼音转换使用指南

## 概述

`UPinYin` 是 emp-script-utils 提供的汉字拼音转换工具类，位于 `com.gdxsoft.easyweb.utils` 包中。它提供以下功能：

- 汉字转拼音（带声调）
- 拼音首字母提取
- 多音字识别
- 繁体转简体
- 姓氏优先处理
- 无声调拼音转换

**数据源**:
- `/pinyin/pinyin.txt` - 汉字拼音库（20,903 个汉字）
- `/pinyin/multi.txt` - 多音字库（含复姓，907 条）
- `/pinyin/traditional.txt` - 繁体 - 简体对照表
- `/pinyin/xing.txt` - 百家姓（409 个姓氏）

---

## 核心 API

### 方法列表

| 方法 | 说明 | 返回值 |
|------|------|--------|
| `convertToPinyinList(String str, boolean xingFirst)` | 转换为拼音列表 | List<String> |
| `convertToPinyinFirstAlpha(String str, boolean xingFirst)` | 转换为拼音首字母 | String |
| `convertToPinyin(char c, boolean xingFirst)` | 单个字符转拼音 | String |
| `convertWithoutTone(String str, boolean xingFirst)` | 转换为无声调拼音列表 | List<String> |
| `convertWithoutToneString(String str, boolean xingFirst)` | 转换为无声调拼音字符串 | String |
| `convertToSimpleChinese(char c)` | 繁体转简体 | char |
| `isChinese(char c)` | 判断是否为汉字 | boolean |

**参数说明**:
- `str` - 要转换的字符串
- `xingFirst` - 是否姓氏优先（识别复姓）
- `c` - 单个字符

---

## 使用示例

### 1. 转换为拼音列表

```java
import com.gdxsoft.easyweb.utils.UPinYin;
import java.util.List;

// 基本转换
List<String> pinyins = UPinYin.convertToPinyinList("张三", false);
System.out.println(pinyins);  // 输出：[zhāng, sān]

// 姓氏优先（识别百家姓）
List<String> pinyins2 = UPinYin.convertToPinyinList("张三", true);
System.out.println(pinyins2);  // 输出：[zhāng, sān]

// 多音字处理
List<String> pinyins3 = UPinYin.convertToPinyinList("长沙市", true);
System.out.println(pinyins3);  // 输出：[zhǎng, shā, shì]

// 混合内容
List<String> pinyins4 = UPinYin.convertToPinyinList("Hello 世界", false);
System.out.println(pinyins4);  // 输出：[H, e, l, l, o, , shì, jiè]
```

### 2. 拼音首字母

```java
import com.gdxsoft.easyweb.utils.UPinYin;

// 基本转换
String firstAlpha = UPinYin.convertToPinyinFirstAlpha("张三", false);
System.out.println(firstAlpha);  // 输出：zs

// 姓氏优先
String firstAlpha2 = UPinYin.convertToPinyinFirstAlpha("欧阳修", true);
System.out.println(firstAlpha2);  // 输出：oyx

// 多音字
String firstAlpha3 = UPinYin.convertToPinyinFirstAlpha("长沙市第一中学", true);
System.out.println(firstAlpha3);  // 输出：zssdyzx

// 混合内容
String firstAlpha4 = UPinYin.convertToPinyinFirstAlpha("ABC 中国", false);
System.out.println(firstAlpha4);  // 输出：ABCzg
```

### 3. 无声调拼音

```java
import com.gdxsoft.easyweb.utils.UPinYin;
import java.util.List;

// 转换为无声调拼音列表
List<String> noToneList = UPinYin.convertWithoutTone("张三", false);
System.out.println(noToneList);  // 输出：[zhang, san]

// 转换为无声调拼音字符串
String noToneStr = UPinYin.convertWithoutToneString("张三", false);
System.out.println(noToneStr);  // 输出：zhangsan

// 带声调的拼音
List<String> withToneList = UPinYin.convertToPinyinList("张三", false);
System.out.println(withToneList);  // 输出：[zhāng, sān]
```

### 4. 单个字符转换

```java
import com.gdxsoft.easyweb.utils.UPinYin;

// 汉字转拼音
String py1 = UPinYin.convertToPinyin('张', false);
System.out.println(py1);  // 输出：zhāng

// 姓氏优先
String py2 = UPinYin.convertToPinyin('李', true);
System.out.println(py2);  // 输出：lǐ

// 非汉字
String py3 = UPinYin.convertToPinyin('A', false);
System.out.println(py3);  // 输出：A

// 繁体转简体
char simple = UPinYin.convertToSimpleChinese('麗');
System.out.println(simple);  // 输出：丽
```

### 5. 汉字判断

```java
import com.gdxsoft.easyweb.utils.UPinYin;

// 判断是否为汉字
boolean isChinese1 = UPinYin.isChinese('张');  // true
boolean isChinese2 = UPinYin.isChinese('A');   // false
boolean isChinese3 = UPinYin.isChinese('〇');  // true（特殊汉字）
boolean isChinese4 = UPinYin.isChinese('1');   // false
```

---

## 多音字处理

### 多音字示例

```java
import com.gdxsoft.easyweb.utils.UPinYin;
import java.util.List;

// "长"字的多音
List<String> pinyins1 = UPinYin.convertToPinyinList"长大", false);
System.out.println(pinyins1);  // [zhǎng, dà]

List<String> pinyins2 = UPinYin.convertToPinyinList("长短", false);
System.out.println(pinyins2);  // [cháng, duǎn]

// "乐"字的多音
List<String> pinyins3 = UPinYin.convertToPinyinList("乐器", false);
System.out.println(pinyins3);  // [yuè, qì]

List<String> pinyins4 = UPinYin.convertToPinyinList("快乐", false);
System.out.println(pinyins4);  // [kuài, lè]

// "了"字的多音
List<String> pinyins5 = UPinYin.convertToPinyinList("了如指掌", false);
System.out.println(pinyins5);  // [liǎo, rú, zhǐ, zhǎng]

List<String> pinyins6 = UPinYin.convertToPinyinList("了解了", false);
System.out.println(pinyins6);  // [liǎo, jiě, le]
```

### 复姓识别

```java
import com.gdxsoft.easyweb.utils.UPinYin;
import java.util.List;

// 复姓 - 姓氏优先模式
List<String> pinyins1 = UPinYin.convertToPinyinList("司马懿", true);
System.out.println(pinyins1);  // [sī, mǎ, yì]

List<String> pinyins2 = UPinYin.convertToPinyinList("司马懿", false);
System.out.println(pinyins2);  // [sī, mǎ, yì]（自动识别）

// 其他复姓
List<String> pinyins3 = UPinYin.convertToPinyinList("欧阳修", true);
System.out.println(pinyins3);  // [ōu, yáng, xiū]

List<String> pinyins4 = UPinYin.convertToPinyinList("诸葛亮", true);
System.out.println(pinyins4);  // [zhū, gě, liàng]

List<String> pinyins5 = UPinYin.convertToPinyinList("尉迟恭", true);
System.out.println(pinyins5);  // [yù, chí, gōng]
```

---

## 繁体转简体

### 单个字符转换

```java
import com.gdxsoft.easyweb.utils.UPinYin;

// 繁体转简体
char c1 = UPinYin.convertToSimpleChinese('麗');  // 丽
char c2 = UPinYin.convertToSimpleChinese('國');  // 国
char c3 = UPinYin.convertToSimpleChinese('華');  // 华
char c4 = UPinYin.convertToSimpleChinese('語');  // 语

System.out.println(c1 + c2 + c3 + c4);  // 输出：丽华国语
```

### 字符串转换（逐字符）

```java
import com.gdxsoft.easyweb.utils.UPinYin;
import java.util.List;

// 繁体字符串转拼音
String traditional = "麗人";
List<String> pinyins = UPinYin.convertToPinyinList(traditional, false);
System.out.println(pinyins);  // [lì, rén]

// 自动转换为简体后获取拼音
String simple = "丽人";
List<String> pinyins2 = UPinYin.convertToPinyinList(simple, false);
System.out.println(pinyins2);  // [lì, rén]
```

---

## 完整使用示例

### 示例 1: 姓名拼音生成器

```java
import com.gdxsoft.easyweb.utils.UPinYin;
import java.util.List;

public class NamePinyinGenerator {
    
    /**
     * 生成姓名的拼音
     */
    public static String generatePinyin(String name) throws Exception {
        List<String> pinyins = UPinYin.convertToPinyinList(name, true);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pinyins.size(); i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(pinyins.get(i));
        }
        return sb.toString();
    }
    
    /**
     * 生成姓名首字母缩写
     */
    public static String generateInitials(String name) throws Exception {
        return UPinYin.convertToPinyinFirstAlpha(name, true);
    }
    
    /**
     * 生成无声调拼音
     */
    public static String generatePinyinWithoutTone(String name) throws Exception {
        return UPinYin.convertWithoutToneString(name, true);
    }
    
    public static void main(String[] args) throws Exception {
        String[] names = {"张三", "欧阳修", "司马懿", "诸葛亮"};
        
        for (String name : names) {
            System.out.println("姓名：" + name);
            System.out.println("拼音：" + generatePinyin(name));
            System.out.println("首字母：" + generateInitials(name));
            System.out.println("无声调：" + generatePinyinWithoutTone(name));
            System.out.println("---");
        }
    }
}
```

**输出**:
```
姓名：张三
拼音：zhāng sān
首字母：zs
无声调：zhangsan
---
姓名：欧阳修
拼音：ōu yáng xiū
首字母：oyx
无声调：ouyangxiu
---
姓名：司马懿
拼音：sī mǎ yì
首字母：smy
无声调：simayi
---
姓名：诸葛亮
拼音：zhū gě liàng
首字母：zgl
无声调：zhugeliang
---
```

---

### 示例 2: 中文搜索辅助

```java
import com.gdxsoft.easyweb.utils.UPinYin;
import java.util.List;

public class ChineseSearchHelper {
    
    /**
     * 检查搜索词是否匹配
     */
    public static boolean matches(String text, String search) throws Exception {
        if (text == null || search == null) {
            return false;
        }
        
        // 1. 直接文本匹配
        if (text.contains(search)) {
            return true;
        }
        
        // 2. 拼音首字母匹配
        String textInitials = UPinYin.convertToPinyinFirstAlpha(text, false);
        String searchInitials = UPinYin.convertToPinyinFirstAlpha(search, false);
        if (textInitials.toLowerCase().contains(searchInitials.toLowerCase())) {
            return true;
        }
        
        // 3. 无声调拼音匹配
        List<String> textPinyins = UPinYin.convertWithoutTone(text, false);
        StringBuilder textPinyinSb = new StringBuilder();
        for (String py : textPinyins) {
            textPinyinSb.append(py);
        }
        
        List<String> searchPinyins = UPinYin.convertWithoutTone(search, false);
        StringBuilder searchPinyinSb = new StringBuilder();
        for (String py : searchPinyins) {
            searchPinyinSb.append(py);
        }
        
        if (textPinyinSb.toString().contains(searchPinyinSb.toString())) {
            return true;
        }
        
        return false;
    }
    
    public static void main(String[] args) throws Exception {
        String text = "长沙市第一中学";
        
        System.out.println("搜索 '长沙': " + matches(text, "长沙"));      // true
        System.out.println("搜索 'cs': " + matches(text, "cs"));         // true (首字母)
        System.out.println("搜索 'changsha': " + matches(text, "changsha")); // true (拼音)
        System.out.println("搜索 '第一中学': " + matches(text, "第一中学"));  // true
        System.out.println("搜索 'dyzx': " + matches(text, "dyzx"));     // true (首字母)
    }
}
```

---

### 示例 3: 文件命名工具

```java
import com.gdxsoft.easyweb.utils.UPinYin;

public class FileNameUtil {
    
    /**
     * 将中文文件名转换为安全的英文文件名
     */
    public static String toSafeFileName(String chineseName) throws Exception {
        if (chineseName == null) {
            return null;
        }
        
        // 转换为无声调拼音
        String pinyin = UPinYin.convertWithoutToneString(chineseName, false);
        
        // 移除空格和特殊字符
        String safeName = pinyin.replaceAll("[^a-zA-Z0-9]", "");
        
        // 限制长度
        if (safeName.length() > 50) {
            safeName = safeName.substring(0, 50);
        }
        
        return safeName;
    }
    
    /**
     * 生成带时间戳的文件名
     */
    public static String generateFileName(String prefix, String extension) throws Exception {
        String safePrefix = toSafeFileName(prefix);
        long timestamp = System.currentTimeMillis();
        return safePrefix + "_" + timestamp + "." + extension;
    }
    
    public static void main(String[] args) throws Exception {
        System.out.println(toSafeFileName("我的文档"));        // wodewendang
        System.out.println(toSafeFileName("测试报告 2024"));   // ceshibaogao2024
        System.out.println(toSafeFileName("你好，世界！"));     // nihhaoshijie
        
        System.out.println(generateFileName("项目计划", "pdf"));  // xiangmujihua_xxxxxx.pdf
    }
}
```

---

### 示例 4: 通讯录排序

```java
import com.gdxsoft.easyweb.utils.UPinYin;
import java.util.*;

public class ContactSorter {
    
    static class Contact {
        String name;
        String phone;
        String pinyinFirst;
        
        Contact(String name, String phone) throws Exception {
            this.name = name;
            this.phone = phone;
            this.pinyinFirst = UPinYin.convertToPinyinFirstAlpha(name, true).toLowerCase();
        }
    }
    
    /**
     * 按拼音首字母排序通讯录
     */
    public static List<Contact> sortByPinyin(List<Contact> contacts) {
        contacts.sort((a, b) -> a.pinyinFirst.compareTo(b.pinyinFirst));
        return contacts;
    }
    
    /**
     * 按拼音首字母分组
     */
    public static Map<String, List<Contact>> groupByFirstLetter(List<Contact> contacts) 
            throws Exception {
        Map<String, List<Contact>> groups = new TreeMap<>();
        
        for (Contact contact : contacts) {
            String firstLetter = contact.pinyinFirst.substring(0, 1).toUpperCase();
            groups.computeIfAbsent(firstLetter, k -> new ArrayList<>()).add(contact);
        }
        
        return groups;
    }
    
    public static void main(String[] args) throws Exception {
        List<Contact> contacts = new ArrayList<>();
        contacts.add(new Contact("张三", "13800138001"));
        contacts.add(new Contact("李四", "13800138002"));
        contacts.add(new Contact("王五", "13800138003"));
        contacts.add(new Contact("欧阳修", "13800138004"));
        contacts.add(new Contact("刘备", "13800138005"));
        
        // 排序
        List<Contact> sorted = sortByPinyin(contacts);
        System.out.println("排序后:");
        for (Contact c : sorted) {
            System.out.println(c.name + " (" + c.pinyinFirst + ")");
        }
        
        // 分组
        Map<String, List<Contact>> groups = groupByFirstLetter(contacts);
        System.out.println("\n分组:");
        for (Map.Entry<String, List<Contact>> entry : groups.entrySet()) {
            System.out.print(entry.getKey() + ": ");
            for (Contact c : entry.getValue()) {
                System.out.print(c.name + " ");
            }
            System.out.println();
        }
    }
}
```

**输出**:
```
排序后:
刘备 (lb)
李四 (ls)
欧阳修 (oyx)
王五 (ww)
张三 (zs)

分组:
L: 刘备 李四 
O: 欧阳修 
W: 王五 
Z: 张三 
```

---

### 示例 5: 多音字学习卡片

```java
import com.gdxsoft.easyweb.utils.UPinYin;
import java.util.List;

public class PolyphonicCard {
    
    /**
     * 生成多音字学习卡片
     */
    public static void printPolyphonicCard(String text) throws Exception {
        List<String> pinyins = UPinYin.convertToPinyinList(text, false);
        
        System.out.println("原文：" + text);
        System.out.println("拼音：" + String.join(" ", pinyins));
        System.out.println();
        
        // 逐字分析
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (UPinYin.isChinese(c)) {
                String py = pinyins.get(i);
                System.out.printf("字：%c  拼音：%s%n", c, py);
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
        printPolyphonicCard("了如指掌 和 乐器 乐不思蜀");
        System.out.println();
        printPolyphonicCard("长沙市第一中学");
    }
}
```

**输出**:
```
原文：了如指掌 和 乐器 乐不思蜀
拼音：liǎo rú zhǐ zhǎng   hé   yuè qì   lè bù sī shǔ

字：了  拼音：liǎo
字：如  拼音：rú
字：指  拼音：zhǐ
字：掌  拼音：zhǎng
字：和  拼音：hé
字：乐  拼音：yuè
字：器  拼音：qì
字：乐  拼音：lè
字：不  拼音：bù
字：思  拼音：sī
字：蜀  拼音：shǔ

原文：长沙市第一中学
拼音：zhǎng shā shì dì yī zhōng xué
字：长  拼音：zhǎng
字：沙  拼音：shā
字：市  拼音：shì
字：第  拼音：dì
字：一  拼音：yī
字：中  拼音：zhōng
字：学  拼音：xué
```

---

## 拼音资源文件

### pinyin.txt - 汉字拼音库

包含 20,903 个汉字的拼音，格式：
```
〇=líng
一=yī
丁=dīng,zhēng
万=wàn,mò
```

### multi.txt - 多音字库

包含 907 条多音字和复姓，格式：
```
#复姓
万俟=mò,qí
司马=sī,mǎ
欧阳=ōu,yáng
```

### traditional.txt - 繁体简体对照表

繁体转简体映射，格式：
```
麗=丽
國=国
華=华
```

### xing.txt - 百家姓

包含 409 个姓氏，格式：
```
#百家姓
赵=zhào
钱=qián
孙=sūn
李=lǐ
```

---

## 注意事项

### 1. 多音字识别

- 多音字库优先匹配连续的多音字（如复姓）
- 单个多音字默认返回第一个读音
- 上下文相关的多音字需要应用层处理

### 2. 姓氏优先模式

- `xingFirst=true` 时优先匹配百家姓
- 复姓自动识别（如"欧阳"、"司马"）
- 建议处理人名时始终使用 `true`

### 3. 非汉字处理

- 非汉字字符原样返回
- 空格、标点符号保留
- 数字、英文字母保留

### 4. 声调处理

- 带声调拼音：zhāng, shān, zhōng
- 无声调拼音：zhang, shan, zhong
- 声调字母映射：ā→a, á→a, ǎ→a, à→a

### 5. 特殊汉字

- "〇"被识别为汉字（拼音：líng）
- 部分生僻字可能无拼音数据

### 6. 性能考虑

- 拼音库在类加载时初始化
- 使用 `ConcurrentHashMap` 支持并发访问
- 大量文本处理建议缓存结果

---

## 相关类

| 类名 | 包路径 | 说明 |
|------|--------|------|
| `UPinYin` | `com.gdxsoft.easyweb.utils` | 拼音转换工具 |
| `UFormat` | `com.gdxsoft.easyweb.utils` | 格式化工具 |
| `Utils` | `com.gdxsoft.easyweb.utils` | 通用工具类 |
