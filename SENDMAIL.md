# SendMail 邮件发送使用指南

## 概述

`SendMail` 是 emp-script-utils 提供的邮件发送工具类，位于 `com.gdxsoft.easyweb.utils.Mail` 包中。基于 JavaMail API 实现，提供简洁的流式 API 进行邮件发送。

**主要功能**:
- 支持 HTML/纯文本邮件
- 多收件人/抄送/密送
- 附件支持
- DKIM 签名
- SSL/TLS 加密
- 自定义邮件头
- 阅读回执
- SMTP 配置管理

**相关类**:
- `SendMail` - 邮件发送主类
- `SmtpCfg` - SMTP 服务器配置
- `SmtpCfgs` - SMTP 配置管理器
- `DKIMCfg` - DKIM 签名配置
- `Addr` - 邮件地址类
- `Attachment` - 附件类
- `UMail` - 邮件发送高层封装

---

## 快速开始

### 基本邮件发送

```java
import com.gdxsoft.easyweb.utils.Mail.SendMail;

SendMail mail = new SendMail()
    .setFrom("sender@example.com", "发件人姓名")
    .addTo("recipient@example.com", "收件人姓名")
    .setSubject("邮件主题")
    .setHtmlContent("<h1>邮件内容</h1>")
    .setCharset("UTF-8");

// 初始化 SMTP 服务器
mail.initProps("smtp.example.com", 587, "username", "password");

// 发送邮件
boolean success = mail.send();
if (success) {
    System.out.println("发送成功");
} else {
    System.out.println("发送失败：" + mail.getLastError());
}
```

### 使用 UMail 高层封装

```java
import com.gdxsoft.easyweb.utils.UMail;

// 简单发送
String result = UMail.sendHtmlMail(
    "sender@example.com",
    "发件人",
    "recipient@example.com",
    "收件人",
    "邮件主题",
    "<h1>邮件内容</h1>",
    null,  // 附件
    "UTF-8"
);
```

---

## 创建邮件

### 设置发件人

```java
import com.gdxsoft.easyweb.utils.Mail.SendMail;
import com.gdxsoft.easyweb.utils.Mail.Addr;

SendMail mail = new SendMail();

// 方式 1: 设置邮箱和姓名
mail.setFrom("sender@example.com", "发件人姓名");

// 方式 2: 只设置邮箱
mail.setFrom("sender@example.com");

// 方式 3: 使用 Addr 对象
Addr from = new Addr("sender@example.com", "发件人姓名");
mail.setFrom(from);
```

### 设置收件人

```java
import com.gdxsoft.easyweb.utils.Mail.SendMail;
import com.gdxsoft.easyweb.utils.Mail.Addr;

SendMail mail = new SendMail();

// 添加单个收件人
mail.addTo("recipient@example.com");
mail.addTo("recipient@example.com", "收件人姓名");

// 使用 Addr 对象
Addr to = new Addr("recipient@example.com", "收件人姓名");
mail.addTo(to);

// 批量添加收件人
String[] emails = {"a@example.com", "b@example.com"};
String[] names = {"用户 A", "用户 B"};
mail.addTos(emails, names);
```

### 设置抄送 (CC)

```java
SendMail mail = new SendMail();

// 添加单个抄送
mail.addCc("cc@example.com");
mail.addCc("cc@example.com", "抄送人姓名");

// 使用 Addr 对象
Addr cc = new Addr("cc@example.com", "抄送人姓名");
mail.addCc(cc);

// 批量添加抄送
String[] ccs = {"cc1@example.com", "cc2@example.com"};
String[] ccNames = {"抄送 A", "抄送 B"};
mail.addCcs(ccs, ccNames);
```

### 设置密送 (BCC)

```java
SendMail mail = new SendMail();

// 添加单个密送
mail.addBcc("bcc@example.com");
mail.addBcc("bcc@example.com", "密送人姓名");

// 批量添加密送
String[] bccs = {"bcc1@example.com", "bcc2@example.com"};
String[] bccNames = {"密送 A", "密送 B"};
mail.addBccs(bccs, bccNames);
```

### 设置回复人

```java
SendMail mail = new SendMail();

// 添加回复地址
mail.addReplyTo("reply@example.com");
mail.addReplyTo("reply@example.com", "回复人姓名");

// 批量添加回复人
String[] replyTos = {"reply1@example.com", "reply2@example.com"};
String[] replyNames = {"回复 A", "回复 B"};
mail.addReplyTos(replyTos, replyNames);
```

### 设置邮件内容

```java
SendMail mail = new SendMail();

// HTML 内容
mail.setHtmlContent("<h1>标题</h1><p>正文内容</p>");

// 纯文本内容
mail.setTextContent("纯文本正文");

// 同时设置 HTML 和纯文本（推荐，降低垃圾邮件评分）
mail.setHtmlContent("<h1>标题</h1><p>正文</p>");
mail.setTextContent("标题\n\n正文");

// 自动创建纯文本部分（默认开启）
mail.setAutoTextPart(true);
```

### 设置邮件主题

```java
SendMail mail = new SendMail();
mail.setSubject("邮件主题");
```

### 设置邮件编码

```java
SendMail mail = new SendMail();
mail.setCharset("UTF-8");  // 默认
mail.setCharset("GBK");    // 中文编码
```

---

## SMTP 配置

### 直接配置 SMTP

```java
import com.gdxsoft.easyweb.utils.Mail.SendMail;

SendMail mail = new SendMail();

// 初始化 SMTP 服务器
mail.initProps(
    "smtp.example.com",  // SMTP 服务器
    587,                  // 端口
    "username",           // 用户名
    "password"            // 密码
);

// 尝试使用 STARTTLS
mail.initProps("smtp.example.com", 587, "username", "password", true);
```

### 使用构造函数配置

```java
SendMail mail = new SendMail(
    "smtp.example.com",
    587,
    "username",
    "password"
);
```

### 手动设置 SSL

```java
SendMail mail = new SendMail();
mail.initProps("smtp.example.com", 587, "username", "password");

// 启用 SSL
mail.setUseSsl(true);
```

### 从配置文件加载 SMTP

创建 `ewa_conf.xml` 配置文件：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<config>
    <!-- Gmail 配置 -->
    <smtp host="smtp.gmail.com" port="587" user="user@gmail.com" pwd="app-password" starttls="true" default="true">
        <from email="@gmail.com" />
        <from email="@gdxsoft.com" />
    </smtp>

    <!-- QQ 邮箱配置 -->
    <smtp host="smtp.qq.com" port="465" user="user@qq.com" pwd="auth-code" ssl="true">
        <from email="@qq.com" />
    </smtp>

    <!-- 企业邮箱配置 -->
    <smtp host="mail.company.com" port="25" user="system@company.com" pwd="password">
        <from email="@company.com" />
        <to email="@company.com" />
    </smtp>

    <!-- DKIM 签名配置 -->
    <dkim domain="example.com" select="default" key="/path/to/private.key.der" />
</config>
```

Java 代码自动加载配置：

```java
import com.gdxsoft.easyweb.utils.Mail.SendMail;
import com.gdxsoft.easyweb.utils.Mail.SmtpCfgs;

SendMail mail = new SendMail();
mail.setFrom("user@example.com");
mail.addTo("recipient@example.com");

// 自动从配置文件中获取 SMTP 配置
// SmtpCfgs 会根据发件人/收件人自动选择匹配的 SMTP 服务器
boolean success = mail.send();
```

### SMTP 配置管理器

```java
import com.gdxsoft.easyweb.utils.Mail.SmtpCfg;
import com.gdxsoft.easyweb.utils.Mail.SmtpCfgs;
import javax.mail.Session;

// 获取默认 SMTP 配置
SmtpCfg defaultCfg = SmtpCfgs.getDefaultSmtpCfg();

// 根据发件人获取 SMTP 配置
SmtpCfg fromCfg = SmtpCfgs.getSmtpCfgByFromEmail("user@example.com");

// 根据收件人获取 SMTP 配置
SmtpCfg toCfg = SmtpCfgs.getSmtpCfgByToEmail("recipient@example.com");

// 创建邮件会话
Session session = SmtpCfgs.createMailSession(defaultCfg);

// 或者直接使用参数创建
Session session = SmtpCfgs.createMailSession(
    "smtp.example.com",  // host
    "username",          // user
    "password",          // password
    587,                 // port
    false,               // ssl
    true                 // startTls
);
```

---

## 附件处理

### 添加单个附件

```java
import com.gdxsoft.easyweb.utils.Mail.SendMail;
import java.io.File;

SendMail mail = new SendMail();

// 方式 1: 使用文件路径
mail.addAttach("/path/to/file.pdf");

// 方式 2: 使用 File 对象
File file = new File("/path/to/file.pdf");
mail.addAttach(file);

// 方式 3: 指定附件名称
mail.addAttach("文档.pdf", "/path/to/file.pdf");
mail.addAttach("文档.pdf", file);
```

### 添加多个附件

```java
SendMail mail = new SendMail();

// 批量添加附件
String[] paths = {"/path/to/file1.pdf", "/path/to/file2.pdf"};
String[] names = {"文件 1.pdf", "文件 2.pdf"};
mail.addAttachs(paths, names);
```

### 获取附件信息

```java
import com.gdxsoft.easyweb.utils.Mail.Attachment;
import java.util.HashMap;

SendMail mail = new SendMail();
mail.addAttach("/path/to/file.pdf");

// 获取所有附件
HashMap<String, Attachment> atts = mail.getAtts();
for (String key : atts.keySet()) {
    Attachment att = atts.get(key);
    System.out.println("附件名：" + att.getAttachName());
    System.out.println("路径：" + att.getSavePathAndName());
}
```

---

## 高级功能

### DKIM 签名

#### 配置 DKIM

```java
import com.gdxsoft.easyweb.utils.Mail.SendMail;
import com.gdxsoft.easyweb.utils.Mail.DKIMCfg;

SendMail mail = new SendMail();

// 方式 1: 直接设置参数
mail.setDkim(
    "example.com",           // 域名
    "/path/to/private.key.der",  // 私钥文件
    "default"                // 选择器
);

// 方式 2: 使用 DKIMCfg 对象
DKIMCfg cfg = new DKIMCfg();
cfg.setDomain("example.com");
cfg.setSelect("default");
cfg.setPrivateKeyPath("/path/to/private.key.der");
mail.setDkim(cfg);
```

#### DKIM 密钥生成

```bash
# 1. 生成 RSA 密钥对
openssl genrsa -out private.key.pem 2048

# 2. 提取公钥（用于 DNS TXT 记录）
openssl rsa -in private.key.pem -pubout -out public.key.pem

# 3. 转换为 DER 格式（JavaMail 需要）
openssl pkcs8 -topk8 -nocrypt -in private.key.pem -out private.key.der -outform der
```

#### DNS TXT 记录配置

在域名 DNS 中添加 TXT 记录：
- 主机名：`default._domainkey.example.com`
- 记录值：`v=DKIM1; k=rsa; p=MIIBIjANBgkqh...`（公钥内容）

### 自定义邮件头

```java
SendMail mail = new SendMail();

// 添加自定义头部
mail.addHeader("X-Custom-Header", "custom-value");
mail.addHeader("X-Priority", "1");  // 高优先级
mail.addHeader("X-Mailer", "MyApp/1.0");
```

### 阅读回执

```java
SendMail mail = new SendMail();

// 要求阅读回执（收件人阅读邮件时会提示回复发件人）
mail.setDispositionNotificationTo(true);
```

### 抄送给自己

```java
SendMail mail = new SendMail();

// 自动抄送给发件人
mail.setSendToSelf(true);
```

### 单一收件人模式（用于跟踪）

```java
SendMail mail = new SendMail();

// 设置多个收件人
mail.addTo("a@example.com");
mail.addTo("b@example.com");
mail.addTo("c@example.com");

// 但实际只发送给单一收件人（用于跟踪）
mail.setSingleTo("a@example.com", "用户 A");

// 所有收件人看到的都是自己单独收到邮件
```

### 自定义 Message-ID

```java
SendMail mail = new SendMail();

// 设置自定义 Message-ID
mail.setMessageId("custom-message-id-123@example.com");
```

### 调试模式

```java
SendMail mail = new SendMail();

// 启用调试模式
mail.setMailDebug(true);

// 使用自定义日志输出
MailLogHelper mailLog = new MailLogHelper();
mailLog.setShowConsole(true);
mail.getMailSession().setDebug(true);
mail.getMailSession().setDebugOut(mailLog);
```

---

## 发送邮件

### 基本发送

```java
SendMail mail = new SendMail();
mail.setFrom("sender@example.com")
    .addTo("recipient@example.com")
    .setSubject("主题")
    .setHtmlContent("内容")
    .initProps("smtp.example.com", 587, "user", "pass");

boolean success = mail.send();
if (!success) {
    Exception err = mail.getLastError();
    System.err.println("发送失败：" + err.getMessage());
}
```

### 获取 MimeMessage

```java
import javax.mail.internet.MimeMessage;

SendMail mail = new SendMail();
// ... 设置邮件内容 ...

// 获取 MimeMessage 对象（不发送）
MimeMessage mimeMessage = mail.getMimeMessage();

// 可以进一步修改或检查邮件
System.out.println("主题：" + mimeMessage.getSubject());
System.out.println("发件人：" + mimeMessage.getFrom());
```

### 使用自定义 Session

```java
import javax.mail.Session;
import com.gdxsoft.easyweb.utils.Mail.SmtpCfgs;

SendMail mail = new SendMail();
// ... 设置邮件内容 ...

// 创建 Session
Session session = SmtpCfgs.createMailSession(
    "smtp.example.com", "user", "pass", 587, false, true
);
mail.setMailSession(session);

// 发送
boolean success = mail.send();
```

---

## 完整使用示例

### 示例 1: 简单邮件发送

```java
import com.gdxsoft.easyweb.utils.Mail.SendMail;

public class SimpleEmail {
    public static void main(String[] args) {
        SendMail mail = new SendMail()
            .setFrom("sender@example.com", "发件人")
            .addTo("recipient@example.com", "收件人")
            .setSubject("测试邮件")
            .setHtmlContent("<h1>您好</h1><p>这是一封测试邮件</p>")
            .setCharset("UTF-8")
            .initProps("smtp.example.com", 587, "user", "password");

        boolean success = mail.send();
        System.out.println(success ? "发送成功" : "发送失败：" + mail.getLastError());
    }
}
```

### 示例 2: 带附件的邮件

```java
import com.gdxsoft.easyweb.utils.Mail.SendMail;

public class EmailWithAttachment {
    public static void main(String[] args) {
        SendMail mail = new SendMail()
            .setFrom("sender@example.com", "发件人")
            .addTo("recipient@example.com", "收件人")
            .addCc("manager@example.com", "经理")
            .setSubject("项目报告")
            .setHtmlContent("<h1>项目报告</h1><p>请查收附件中的报告</p>")
            .addAttach("/path/to/report.pdf", "项目报告.pdf")
            .addAttach("/path/to/data.xlsx", "数据表格.xlsx")
            .initProps("smtp.example.com", 587, "user", "password");

        boolean success = mail.send();
        System.out.println(success ? "发送成功" : "发送失败");
    }
}
```

### 示例 3: HTML 邮件模板

```java
import com.gdxsoft.easyweb.utils.Mail.SendMail;

public class HtmlEmailTemplate {
    public static void main(String[] args) {
        String htmlContent = buildHtmlEmail("张三", "订单 12345", "2024-03-23");
        
        SendMail mail = new SendMail()
            .setFrom("noreply@example.com", "订单通知")
            .addTo("customer@example.com", "张三")
            .setSubject("订单发货通知")
            .setHtmlContent(htmlContent)
            .setCharset("UTF-8")
            .initProps("smtp.example.com", 587, "user", "password");

        mail.send();
    }

    private static String buildHtmlEmail(String name, String orderId, String date) {
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head><style>" +
               "body{font-family:Arial,sans-serif;}" +
               ".container{max-width:600px;margin:0 auto;}" +
               ".header{background:#4CAF50;color:white;padding:20px;}" +
               ".content{padding:20px;}" +
               ".footer{background:#f1f1f1;padding:10px;text-align:center;}" +
               "</style></head>" +
               "<body>" +
               "<div class='container'>" +
               "<div class='header'><h1>订单发货通知</h1></div>" +
               "<div class='content'>" +
               "<p>尊敬的 " + name + "：</p>" +
               "<p>您的订单 <strong>" + orderId + "</strong> 已发货。</p>" +
               "<p>发货日期：" + date + "</p>" +
               "</div>" +
               "<div class='footer'>© 2024 公司名称</div>" +
               "</div>" +
               "</body>" +
               "</html>";
    }
}
```

### 示例 4: 批量发送邮件

```java
import com.gdxsoft.easyweb.utils.Mail.SendMail;
import java.util.*;

public class BatchEmailSender {
    
    static class Recipient {
        String email;
        String name;
        Recipient(String email, String name) {
            this.email = email;
            this.name = name;
        }
    }
    
    public static void sendBatchEmail(List<Recipient> recipients) {
        for (Recipient r : recipients) {
            SendMail mail = new SendMail()
                .setFrom("noreply@example.com", "公司通知")
                .addTo(r.email, r.name)
                .setSubject("重要通知")
                .setHtmlContent("<h1>尊敬的 " + r.name + "</h1><p>这是一封重要通知...</p>")
                .initProps("smtp.example.com", 587, "user", "password");
            
            boolean success = mail.send();
            System.out.println(r.email + ": " + (success ? "成功" : "失败"));
            
            // 避免发送过快
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }
    
    public static void main(String[] args) {
        List<Recipient> recipients = Arrays.asList(
            new Recipient("a@example.com", "用户 A"),
            new Recipient("b@example.com", "用户 B"),
            new Recipient("c@example.com", "用户 C")
        );
        sendBatchEmail(recipients);
    }
}
```

### 示例 5: 使用配置文件发送

```java
import com.gdxsoft.easyweb.utils.Mail.SendMail;
import com.gdxsoft.easyweb.utils.Mail.SmtpCfgs;
import com.gdxsoft.easyweb.utils.UPath;

public class ConfigBasedEmail {
    public static void main(String[] args) {
        // 加载配置文件（ewa_conf.xml）
        UPath.initPath();
        
        SendMail mail = new SendMail()
            .setFrom("user@example.com", "发件人")
            .addTo("recipient@example.com", "收件人")
            .setSubject("测试邮件")
            .setHtmlContent("<h1>邮件内容</h1>")
            .setCharset("UTF-8");
        
        // 自动从配置文件获取 SMTP 配置
        // SmtpCfgs 会根据发件人域名自动选择匹配的 SMTP 服务器
        boolean success = mail.send();
        System.out.println(success ? "发送成功" : "发送失败");
    }
}
```

### 示例 6: 邮件营销系统

```java
import com.gdxsoft.easyweb.utils.Mail.SendMail;
import java.util.*;
import java.util.concurrent.*;

public class EmailMarketingSystem {
    
    private String smtpHost;
    private int smtpPort;
    private String smtpUser;
    private String smtpPassword;
    private ExecutorService executor;
    
    public EmailMarketingSystem(String host, int port, String user, String pass) {
        this.smtpHost = host;
        this.smtpPort = port;
        this.smtpUser = user;
        this.smtpPassword = pass;
        this.executor = Executors.newFixedThreadPool(5);
    }
    
    public void sendCampaign(String subject, String htmlContent, List<String> recipients) {
        CountDownLatch latch = new CountDownLatch(recipients.size());
        
        for (String email : recipients) {
            executor.submit(() -> {
                try {
                    SendMail mail = new SendMail()
                        .setFrom("marketing@example.com", "营销中心")
                        .addTo(email)
                        .setSubject(subject)
                        .setHtmlContent(htmlContent)
                        .setCharset("UTF-8")
                        .initProps(smtpHost, smtpPort, smtpUser, smtpPassword);
                    
                    boolean success = mail.send();
                    System.out.println(email + ": " + (success ? "成功" : "失败"));
                } catch (Exception e) {
                    System.err.println(email + " 发送异常：" + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        try {
            latch.await();
            executor.shutdown();
            System.out.println("营销活动完成");
        } catch (InterruptedException e) {
        }
    }
    
    public static void main(String[] args) {
        EmailMarketingSystem system = new EmailMarketingSystem(
            "smtp.example.com", 587, "user", "password"
        );
        
        String subject = "特别优惠";
        String content = "<h1>限时优惠</h1><p>立即行动，享受 8 折优惠！</p>";
        
        List<String> recipients = Arrays.asList(
            "customer1@example.com",
            "customer2@example.com",
            "customer3@example.com"
        );
        
        system.sendCampaign(subject, content, recipients);
    }
}
```

---

## 邮件接收（POP3）

### 读取邮件

```java
import com.gdxsoft.easyweb.utils.Mail.MailDecode;
import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class Pop3MailReader {
    public static void readMails(String host, String username, String password) throws Exception {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        
        // 连接 POP3 服务器
        Store store = session.getStore("pop3");
        store.connect(host, username, password);
        
        // 获取收件箱
        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_ONLY);
        
        Message[] messages = folder.getMessages();
        System.out.println("邮件总数：" + messages.length);
        
        // 读取最新邮件
        for (int i = messages.length - 1; i >= 0 && i >= messages.length - 5; i--) {
            MimeMessage message = (MimeMessage) messages[i];
            MailDecode mailDecode = new MailDecode(message, "/path/to/attachments");
            
            System.out.println("主题：" + mailDecode.getSubject());
            System.out.println("发件人：" + mailDecode.getFrom());
            System.out.println("日期：" + mailDecode.getSentDate());
            System.out.println("正文：" + mailDecode.getBodyText());
            
            // 保存附件
            mailDecode.saveAttachments();
        }
        
        folder.close(false);
        store.close();
    }
}
```

---

## 注意事项

### 1. SMTP 服务器配置

- Gmail: smtp.gmail.com, 端口 587 (STARTTLS) 或 465 (SSL)
- QQ 邮箱：smtp.qq.com, 端口 465 (SSL), 使用授权码
- 163 邮箱：smtp.163.com, 端口 465 (SSL), 使用授权码
- 企业邮箱：咨询邮件服务提供商

### 2. 认证方式

- 大多数 SMTP 服务器需要认证
- Gmail 等需要应用专用密码
- 国内邮箱需要授权码而非登录密码

### 3. 发送限制

- 免费邮箱有每日发送限制
- 建议批量发送时添加延迟
- 大量发送建议使用专业邮件服务

### 4. 垃圾邮件防范

- 同时设置 HTML 和纯文本内容
- 设置正确的发件人信息
- 配置 SPF、DKIM、DMARC 记录
- 避免敏感词汇

### 5. 字符编码

- 推荐使用 UTF-8 编码
- 中文邮件设置正确的 charset
- 附件名使用 MIME 编码

### 6. 附件大小

- 大多数邮件服务商限制附件大小（通常 20-50MB）
- 大文件建议使用云存储链接

### 7. 错误处理

- 检查 `getLastError()` 获取详细错误
- 网络异常需要重试机制
- 记录发送日志便于排查问题

---

## 相关类

| 类名 | 包路径 | 说明 |
|------|--------|------|
| `SendMail` | `com.gdxsoft.easyweb.utils.Mail` | 邮件发送主类 |
| `SmtpCfg` | `com.gdxsoft.easyweb.utils.Mail` | SMTP 服务器配置 |
| `SmtpCfgs` | `com.gdxsoft.easyweb.utils.Mail` | SMTP 配置管理器 |
| `DKIMCfg` | `com.gdxsoft.easyweb.utils.Mail` | DKIM 签名配置 |
| `Addr` | `com.gdxsoft.easyweb.utils.Mail` | 邮件地址类 |
| `Attachment` | `com.gdxsoft.easyweb.utils.Mail` | 附件类 |
| `MailDecode` | `com.gdxsoft.easyweb.utils.Mail` | 邮件解码类 |
| `UMail` | `com.gdxsoft.easyweb.utils` | 邮件发送高层封装 |
| `UPath` | `com.gdxsoft.easyweb.utils` | 配置文件加载 |
