package com.gdxsoft.easyweb.utils.Mail;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.Address;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;

import com.gdxsoft.easyweb.utils.UMail;
import com.gdxsoft.easyweb.utils.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 发送邮件 https://www.checktls.com/TestReceiver 测试smtp ssl配置
 * 
 * @author 郭磊
 *
 */
public class SendMail {
	private Logger log = LoggerFactory.getLogger(SendMail.class);

	Properties props;
	private String smtp_uid;
	private String smtp_pwd;
	private Session mailSession;
	private boolean is_mail_debug_ = false; // 是否显示debug信息

	private String messageId; // 如:供应商标识.messageID

	private Addr from_;
	private Addr sender_;

	private Addr singleTo_;

	private HashMap<String, Addr> tos_ = new HashMap<String, Addr>();
	private HashMap<String, Addr> ccs_ = new HashMap<String, Addr>();
	private HashMap<String, Addr> bccs_ = new HashMap<String, Addr>();

	private HashMap<String, Addr> replayTos_ = new HashMap<String, Addr>();

	private HashMap<String, Attachment> atts_ = new HashMap<String, Attachment>();

	private String subject_;
	private String htmlContent_;
	private String textContent_;
	private boolean isSendToSelf_; // 抄送给自己
	private boolean isDispositionNotificationTo_; //// 要求阅读回执(收件人阅读邮件时会提示回复发件人,表明邮件已收到,并已阅读)
	private boolean isAutoTextPart_ = true; // 自动创建纯文字部分
	private String charset_ = "utf-8";
	private DKIMSigner dkimSigner_;

	private MimeMessage mimeMessage_;

	private Exception lastError;

	private HashMap<String, String> headers_ = new HashMap<String, String>();

	/**
	 * 初始化发送邮件
	 */
	public SendMail() {

	}

	/**
	 * 初始化发送邮件
	 * 
	 * @param host 服务器
	 * @param port 端口
	 * @param uid  用户
	 * @param pwd  密码
	 */
	public SendMail(String host, int port, String uid, String pwd) {
		this.initProps(host, port, uid, pwd);
	}

	/**
	 * 设置发件人
	 * 
	 * @param fromEmail 发件人邮件
	 * @param fromName  发件人姓名
	 */
	public SendMail setFrom(String fromEmail, String fromName) {
		this.from_ = new Addr(fromEmail, fromName);
		return this;
	}

	/**
	 * 设置发件人
	 * 
	 * @param fromEmail 发件人邮件
	 * @return SendMail
	 */
	public SendMail setFrom(String fromEmail) {
		this.from_ = new Addr(fromEmail, null);
		return this;
	}

	/**
	 * 设置发件人
	 * 
	 * @param from 发件人
	 * @return SendMail
	 */
	public SendMail setFrom(Addr from) {
		this.from_ = from;
		return this;
	}

	/**
	 * 获取发件人
	 * 
	 * @return the sender_
	 */
	public Addr getSender() {
		return sender_;
	}

	/**
	 * 设置发件人
	 * 
	 * @param sender the sender_ to set
	 * @return SendMail
	 */
	public SendMail setSender(Addr sender) {
		sender_ = sender;
		return this;
	}

	/**
	 * 设置发件人
	 * 
	 * @param senderEmail 发件人邮件
	 * @param senderName  发件人姓名
	 * @return SendMail
	 */
	public SendMail setSender(String senderEmail, String senderName) {
		this.sender_ = new Addr(senderEmail, senderName);
		return this;
	}

	/**
	 * 设置发件人
	 * 
	 * @param senderEmail 发件人邮件
	 * @return SendMail
	 */
	public SendMail setSender(String senderEmail) {
		this.sender_ = new Addr(senderEmail, null);
		return this;
	}

	/**
	 * 批量添加收件人
	 * 
	 * @param tos     收件人邮件数组
	 * @param toNames 收件人名称数组， tos.length = toNames.length
	 * @return SendMail
	 */
	public SendMail addTos(String[] tos, String[] toNames) {
		if (tos != null) {
			for (int i = 0; i < tos.length; i++) {
				String to = tos[i];
				String toName = null;
				if (toNames != null && toNames.length > i) {
					toName = toNames[i];
				}

				this.addTo(to, toName);
			}
		}
		return this;
	}

	/**
	 * 添加收件人
	 * 
	 * @param toEmail 收件人邮件
	 * @return SendMail
	 */
	public SendMail addTo(String toEmail) {
		return this.addTo(toEmail, null);
	}

	/**
	 * 添加收件人
	 * 
	 * @param toEmail 收件人邮件
	 * @param toName  收件人姓名
	 * 
	 * @return SendMail
	 */
	public SendMail addTo(String toEmail, String toName) {
		String keytoEmail = toEmail.trim().toLowerCase();
		Addr to = new Addr(toEmail, toName);
		tos_.put(keytoEmail, to);
		return this;
	}

	/**
	 * 添加收件人
	 * 
	 * @param to 收件人
	 * @return SendMail
	 */
	public SendMail addTo(Addr to) {
		String keytoEmail = to.getEmail().toUpperCase().trim();
		tos_.put(keytoEmail, to);
		return this;
	}

	/**
	 * 添加回复人
	 * 
	 * @param replyTo 回复人邮件地址
	 * 
	 * @return SendMail
	 */
	public SendMail addReplyTo(String replyTo) {
		return this.addReplyTo(replyTo, null);
	}

	/**
	 * 添加回复人
	 * 
	 * @param replyToEmail 回复人邮件
	 * @param replyToName  回复人名称
	 * @return SendMail
	 */
	public SendMail addReplyTo(String replyToEmail, String replyToName) {
		String keytoEmail = replyToEmail.trim().toLowerCase();
		Addr to = new Addr(replyToEmail, replyToName);
		this.replayTos_.put(keytoEmail, to);
		return this;
	}

	/**
	 * 添加回复人
	 * 
	 * @param replyTo 回复人地址
	 * @return SendMail
	 */
	public SendMail addReplyTo(Addr replyTo) {
		String keytoEmail = replyTo.getEmail().toUpperCase().trim();
		replayTos_.put(keytoEmail, replyTo);
		return this;
	}

	/**
	 * 批量添加回复人
	 * 
	 * @param tos     回复人邮件地址数组
	 * @param toNames 回复人姓名数组
	 * @return SendMail
	 */
	public SendMail addReplyTos(String[] tos, String[] toNames) {
		if (tos != null) {
			for (int i = 0; i < tos.length; i++) {
				String to = tos[i];
				String toName = null;
				if (toNames != null && toNames.length > i) {
					toName = toNames[i];
				}

				this.addReplyTo(to, toName);
			}
		}

		return this;
	}

	/**
	 * 添加抄送人
	 * 
	 * @param ccEmail 抄送人邮件
	 * @return SendMail
	 */
	public SendMail addCc(String ccEmail) {
		this.addCc(ccEmail, null);
		return this;
	}

	/**
	 * 添加抄送人
	 * 
	 * @param ccEmail 抄送人邮件
	 * @param ccName  抄送人姓名
	 * @return SendMail
	 */
	public SendMail addCc(String ccEmail, String ccName) {
		String keytoEmail = ccEmail.trim().toLowerCase();
		Addr to = new Addr(ccEmail, ccName);
		ccs_.put(keytoEmail, to);
		return this;
	}

	/**
	 * 添加抄送人
	 * 
	 * @param cc 抄送人
	 * @return SendMail
	 */
	public SendMail addCc(Addr cc) {
		String keytoEmail = cc.getEmail().toUpperCase().trim();
		bccs_.put(keytoEmail, cc);
		return this;
	}

	/**
	 * 批量添加 抄送人
	 * 
	 * @param ccs     抄送人邮件数组
	 * @param ccNames 抄送人姓名数组
	 * @return SendMail
	 */
	public SendMail addCcs(String[] ccs, String[] ccNames) {
		if (ccs != null) {
			for (int i = 0; i < ccs.length; i++) {
				String to = ccs[i];
				String toName = null;
				if (ccNames != null && ccNames.length > i) {
					toName = ccNames[i];
				}

				this.addCc(to, toName);
			}
		}
		return this;
	}

	/**
	 * 添加密送
	 * 
	 * @param bccEmail 密送邮件
	 * @return SendMail
	 */
	public SendMail addBcc(String bccEmail) {
		this.addBcc(bccEmail, null);
		return this;
	}

	/**
	 * 添加密送
	 * 
	 * @param bccEmail 密送邮件
	 * @param bccName  密送人姓名
	 * @return SendMail
	 */
	public SendMail addBcc(String bccEmail, String bccName) {
		String keytoEmail = bccEmail.trim().toLowerCase();
		Addr to = new Addr(bccEmail, bccName);
		bccs_.put(keytoEmail, to);
		return this;
	}

	/**
	 * 添加密送
	 * 
	 * @param bcc 密送人
	 * @return SendMail
	 */
	public SendMail addBcc(Addr bcc) {
		String keytoEmail = bcc.getEmail().toUpperCase().trim();
		bccs_.put(keytoEmail, bcc);
		return this;
	}

	/**
	 * 批量添加 密送人
	 * 
	 * @param bccs     密送邮件数组
	 * @param bccNames 密送人姓名数组
	 * @return SendMail
	 */
	public SendMail addBccs(String[] bccs, String[] bccNames) {
		if (bccs != null) {
			for (int i = 0; i < bccs.length; i++) {
				String to = bccs[i];
				String toName = null;
				if (bccNames != null && bccNames.length > i) {
					toName = bccNames[i];
				}

				this.addBcc(to, toName);
			}
		}
		return this;
	}

	/**
	 * 添加附件
	 * 
	 * @param file 附件文件
	 * @return SendMail
	 */
	public SendMail addAttach(File file) {
		String name = file.getName();
		String path = file.getAbsolutePath();

		Attachment att = this.createAtt(name, path);
		this.atts_.put(att.getAttachName(), att);

		return this;
	}

	/**
	 * 添加附件
	 * 
	 * @param attName 附件名称
	 * @param file    附件文件
	 * @return SendMail
	 */
	public SendMail addAttach(String attName, File file) {
		String path = file.getAbsolutePath();

		if (attName == null || attName.trim().length() == 0) {
			attName = file.getName();
		}

		Attachment att = this.createAtt(attName, path);
		this.atts_.put(att.getAttachName(), att);

		return this;
	}

	/**
	 * 创建附件
	 * 
	 * @param attName 附件名称
	 * @param path    附件路径
	 * @return Attachment 附件
	 */
	private Attachment createAtt(String attName, String path) {
		if (attName == null || attName.trim().length() == 0) {
			File file = new File(path);
			attName = file.getName();
		}

		Attachment att = new Attachment();
		att.setAttachName(attName);
		att.setSavePathAndName(path);

		return att;
	}

	/**
	 * 添加附件
	 * 
	 * @param path 附件路径
	 * @return SendMail
	 */
	public SendMail addAttach(String path) {
		File f = new File(path);
		return this.addAttach(f);
	}

	/**
	 * 添加附件
	 * 
	 * @param attName 附加名称
	 * @param path    附件路径
	 * @return SendMail
	 */
	public SendMail addAttach(String attName, String path) {
		Attachment att = this.createAtt(attName, path);
		this.atts_.put(att.getAttachName(), att);
		return this;
	}

	/**
	 * 批量添加 附件
	 * 
	 * @param attachPaths 附件路径数组
	 * @param attNames    附件名称数组
	 * @return SendMail
	 */
	public SendMail addAttachs(String[] attachPaths, String[] attNames) {
		if (attachPaths != null) {
			for (int i = 0; i < attachPaths.length; i++) {
				String attachPath = attachPaths[i];
				if (attNames != null && attNames.length > i) {
					String attachName = attNames[i];
					this.addAttach(attachName, attachPath);
				} else {
					this.addAttach(attachPath);
				}
			}
		}
		return this;
	}

	/**
	 * 初始化SMTP属性
	 * 
	 * @param host SMTP服务器
	 * @param port SMTP端口
	 * @param uid  发件人
	 * @param pwd  发件人密码
	 * @return SendMail
	 */
	public SendMail initProps(String host, int port, String uid, String pwd) {
		return this.initProps(host, port, uid, pwd, false);
	}

	/**
	 * 初始化SMTP属性
	 * 
	 * @param host        SMTP服务器
	 * @param port        SMTP端口
	 * @param uid         发件人
	 * @param pwd         发件人密码
	 * @param tryStartTls 尝试用starttls命令发邮件
	 * @return SendMail
	 */
	public SendMail initProps(String host, int port, String uid, String pwd, boolean tryStartTls) {
		props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		// props.setProperty("mail.host", host);
		props.setProperty("mail.smtp.host", host);
		props.setProperty("mail.smtp.port", port + "");
		if (uid != null && uid.trim().length() > 0) {
			props.setProperty("mail.smtp.auth", "true");
		} else {
			props.setProperty("mail.smtp.auth", "false");
		}
		smtp_uid = uid;
		smtp_pwd = pwd;

		if (port == 465) { // smtps 端口
			// 信任服务器的证书
			props.put("mail.smtp.ssl.trust", host);
			props.put("mail.smtp.ssl.trust", host);
			this.setUseSsl(true);
		} else if (tryStartTls) {
			// If true, requires the use of the STARTTLS command. If the server doesn't
			// support the STARTTLS command, or the command fails, the connect method will
			// fail. Defaults to false.
			// props.put("mail.smtp.starttls.required", "true");

			// If true, enables the use of the STARTTLS command (if supported by the server)
			// to switch the connection to a TLS-protected connection before issuing any
			// login commands. Defaults to false.
			props.put("mail.smtp.starttls.enable", "true");
			// 信任服务器的证书
			props.put("mail.smtp.ssl.trust", host);
		}
		// System.out.println("SMTP: " + host + ":" + port + ", uid=" + uid + ", pwd=" +
		// pwd);

		return this;
	}

	/**
	 * 获取MailSession
	 * 
	 * @return SendMail
	 */
	public Session getMailSession() {
		if (mailSession == null) {
			if (props == null) {
				// 默认的发件, ewa_conf中定义
				mailSession = UMail.getMailSession();
			} else {
				if (smtp_uid != null && smtp_uid.trim().length() > 0) {
					MailAuth auth = new MailAuth(smtp_uid, smtp_pwd);
					props.setProperty("mail.smtp.auth", "true");
					mailSession = Session.getInstance(props, auth);
				} else {
					props.setProperty("mail.smtp.auth", "false");
					mailSession = Session.getInstance(props, null);
				}
			}
			mailSession.setDebug(this.is_mail_debug_);

		}

		return mailSession;
	}

	/**
	 * 转换为 InternetAddress格式
	 * 
	 * @param addr 邮件地址
	 * @return InternetAddress
	 */
	public InternetAddress getAddress(Addr addr) {
		InternetAddress iaFrom = new InternetAddress();
		iaFrom.setAddress(addr.getEmail().trim());
		if (addr.getName() != null) {
			try {
				iaFrom.setPersonal(addr.getName(), this.charset_);
			} catch (UnsupportedEncodingException e) {
				log.error(e.getMessage());
			}
		}

		return iaFrom;
	}

	/**
	 * 转换为 InternetAddress 数组
	 * 
	 * @param addrs 邮件地址map
	 * @return InternetAddress数组
	 */
	private InternetAddress[] getAddresses(HashMap<String, Addr> addrs) {
		InternetAddress[] addresses = new InternetAddress[addrs.size()];
		int inc = 0;
		for (String key : addrs.keySet()) {
			Addr addr = addrs.get(key);
			InternetAddress iaFrom = this.getAddress(addr);
			addresses[inc] = iaFrom;
			inc++;
		}
		return addresses;
	}

	/**
	 * 获取邮件
	 * 
	 * @return 邮件
	 * @throws MessagingException
	 */
	public MimeMessage getMimeMessage() throws MessagingException {
		if (this.mimeMessage_ != null) {
			return this.mimeMessage_;
		}
		
		this.createMinMessage();
		return this.mimeMessage_;
	}

	/**
	 * 获取邮件内容
	 * 
	 * @return 邮件内容
	 * @throws MessagingException
	 */
	private MimeBodyPart getMailContent() throws MessagingException {
		if (this.textContent_ == null && this.isAutoTextPart_ && this.htmlContent_ != null) {
			this.textContent_ = Utils.filterHtml(this.htmlContent_);
		}
		MimeBodyPart contentPart = new MimeBodyPart();

		// 混合了正文和 html 降低邮件垃圾评分
		// http://www.mail-tester.com/
		if (this.textContent_ != null && this.htmlContent_ != null) {
			// 创建一个MIME子类型为"alternative"的MimeMultipart对象
			// 并作为前面创建的 contentPart 对象的邮件内容
			MimeMultipart htmlMultipart = new MimeMultipart("alternative");

			contentPart.setContent(htmlMultipart);
			// 纯文本正文
			MimeBodyPart textBodypart = new MimeBodyPart();
			textBodypart.setText(textContent_, charset_);
			htmlMultipart.addBodyPart(textBodypart);

			// html正文
			MimeBodyPart htmlBodypart = new MimeBodyPart();
			htmlBodypart.setContent(this.htmlContent_, "text/html;charset=" + this.charset_);
			htmlMultipart.addBodyPart(htmlBodypart);

		} else if (this.textContent_ != null) {
			// 纯文本正文
			contentPart.setText(textContent_, charset_);
		} else if (this.htmlContent_ != null) {
			// html正文
			contentPart.setContent(this.htmlContent_, "text/html;charset=" + this.charset_);

		} else {// 没有正文
			contentPart.setText("", charset_);
		}

		return contentPart;
	}

	/**
	 * 创建邮件
	 * 
	 * @return 邮件
	 * @throws MessagingException
	 */
	public MimeMessage createMinMessage() throws MessagingException {
		MimeMessage mm;
		if (this.messageId != null && this.messageId.trim().length() > 0) {
			// 更改邮件的头的message_id
			CustomMimeMessage cmm = new CustomMimeMessage(mailSession);
			cmm.setOldMessageId(this.messageId);
			cmm.updateMessageID();
			this.addHeader("X-EWA-MESSAGE-ID", this.messageId.trim());
			mm = cmm;
		} else {
			mm = new MimeMessage(mailSession);
		}
		this.mimeMessage_ = mm;

		mm.setSubject(this.encodeAttName(this.getSubject()));

		InternetAddress iaFrom = this.getAddress(this.from_);
		mm.setFrom(iaFrom);

		InternetAddress[] tos = this.getAddresses(this.tos_);
		mm.setRecipients(Message.RecipientType.TO, tos); // 收件人

		// 抄送
		if (this.ccs_.size() > 0) {
			InternetAddress[] ccs = this.getAddresses(this.ccs_);
			mm.setRecipients(Message.RecipientType.CC, ccs);
		}
		if (this.isSendToSelf_) {// 抄送给自己
			this.addBcc(this.from_);
		}

		// 秘送
		if (this.bccs_.size() > 0) {
			InternetAddress[] bccs = this.getAddresses(this.bccs_);
			mm.setRecipients(Message.RecipientType.BCC, bccs);
		}

		// 回复人
		if (this.replayTos_.size() > 0) {
			InternetAddress[] replayTos = this.getAddresses(this.replayTos_);
			mm.setReplyTo(replayTos);
		}
		//// 创建一个MIME子类型为"mixed"的MimeMultipart对象，表示这是一封混合组合类型的邮件
		Multipart multipart = new MimeMultipart("mixed");
		mm.setContent(multipart);

		if (this.messageId != null && this.messageId.trim().length() > 0) {
			this.addHeader("X-EWA-MESSAGE-ID", this.messageId.trim());
		}
		// 要求回执，妈的被他害了，收了无数的垃圾邮件，Mac的邮件会自动发送，因此变成了反弹垃圾邮件
		if (this.isDispositionNotificationTo_) {
			this.addHeader("Disposition-Notification-To", from_.getEmail());
		}
		if (this.sender_ != null) {
			// 设置邮件的sender参数，看看能不能躲过垃圾邮件检测
			InternetAddress sender = this.getAddress(this.sender_);
			mm.setSender(sender);
		}
		// 设置正文
		MimeBodyPart textBody = this.getMailContent();
		multipart.addBodyPart(textBody);

		for (String attName : this.atts_.keySet()) {
			Attachment att = this.atts_.get(attName);
			BodyPart affixBody = new MimeBodyPart();
			DataSource source = new FileDataSource(att.getSavePathAndName());
			// 添加附件的内容
			affixBody.setDataHandler(new DataHandler(source));

			String fileName = this.encodeAttName(attName);
			affixBody.setFileName(fileName);

			multipart.addBodyPart(affixBody);
		}

		// 设置发送时间
		mm.setSentDate(new Date());

		if (!this.headers_.containsKey("X-Mailer")) {
			this.headers_.put("X-Mailer", "EWA2(java) gdxsoft.com");
		}

		// 添加头部
		for (String name : this.headers_.keySet()) {
			String value = this.headers_.get(name);
			if (value != null) {
				mm.addHeader(name, value);
			}
		}

		try {
			mm = this.dkimSign(mm);
			this.mimeMessage_ = mm;
			
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return mm;
	}

	/**
	 * 将头部放到缓存中
	 * 
	 * @param name  名称
	 * @param value 值
	 * @return SendMail
	 */
	public SendMail addHeader(String name, String value) {
		headers_.put(name, value);
		return this;
	}

	/**
	 * 发送邮件
	 * 
	 * @return 发送结果
	 */
	public boolean send() {
		Session mailSession = getMailSession();
		MimeMessage mm;
		try {
			mm = this.getMimeMessage();
		} catch (Exception e1) {
			this.lastError = e1;
			log.error(e1.getMessage());
			return false;
		}

		Transport transport = null;
		try {
			transport = mailSession.getTransport();
			transport.connect();

			if (this.singleTo_ == null) {
				Address[] recs = mm.getAllRecipients();
				Transport.send(mm, recs);
			} else {
				// 跟踪需要，单一发送独立邮件到独立的邮箱，单是显示收件人为多个
				Address[] singleTo = new Address[1];
				singleTo[0] = this.getAddress(singleTo_);
				Transport.send(mm, singleTo);
			}
			return true;
		} catch (Exception e) {
			this.lastError = e;
			log.error(e.getMessage());
			return false;
		} finally {
			if (transport != null) {
				try {
					transport.close();
				} catch (MessagingException e) {
					log.error(e.getMessage());
				}
			}
		}
	}

	/**
	 * 获取附件中文名称
	 * 
	 * @param attName
	 * @return
	 */
	private String encodeAttName(String attName) {
		// https://stackoverflow.com/questions/31799960/java-mail-mimeutility-encodetext-unsupportedencodingexception-base64
		try {
			// 中文附件标题名在发送时不会变成乱码
			// String fileName = "=?UTF-8?B?" +
			// UConvert.ToBase64String(attName.getBytes("UTF-8")) + "?=";
			// B" or "Q";
			String fileName = MimeUtility.encodeText(attName, this.charset_, "Q");
			return fileName;
		} catch (UnsupportedEncodingException e) {
			log.error(e.getMessage());
			return attName;
		}

	}

	/**
	 * 设置邮件DKIM
	 * 
	 * @param domain             域名，需要和发件人域名一致
	 * @param privateKeyFilePath 私有文件路径
	 * @param select             选择，默认default
	 * @return SendMail
	 */
	public SendMail setDkim(String domain, String privateKeyFilePath, String select) {
		if (select == null) {
			select = "default";
		}
		try {
			this.dkimSigner_ = new DKIMSigner(domain, select, privateKeyFilePath);
		} catch (Exception e) {
		}

		return this;
	}

	/**
	 * 设置邮件DKIM
	 * 
	 * @param cfg DKIMCfg
	 * @return SendMail
	 */
	public SendMail setDkim(DKIMCfg cfg) {
		if (cfg != null) {
			try {
				this.dkimSigner_ = new DKIMSigner(cfg.getDomain(), cfg.getSelect(), cfg.getPrivateKeyPath());
			} catch (Exception e) {
				log.warn(e.getMessage());
			}
		}

		return this;
	}

	/**
	 * 签名邮件
	 * 
	 * @param mm 原始邮件
	 * @return 签名后邮件
	 * @throws MessagingException
	 * @throws DKIMSignerException
	 */
	public MimeMessage dkimSign(MimeMessage mm) throws Exception {
		if (this.dkimSigner_ != null) {
			dkimSigner_.setIdentity(this.from_.getEmail());
			mm = new SMTPDKIMMessage(mm, dkimSigner_);
		}
		return mm;
	}

	/**
	 * 获取 messageId
	 * 
	 * @return messageId
	 */
	public String getMessageId() {
		return messageId;

	}

	/**
	 * 设置 messageId
	 * 
	 * @param messageId messageId
	 * @return SendMail
	 */
	public SendMail setMessageId(String messageId) {
		this.messageId = messageId;
		return this;
	}

	/**
	 * 标题
	 * 
	 * @return the 标题
	 */
	public String getSubject() {
		return subject_;
	}

	/**
	 * 标题
	 * 
	 * @param subject 标题
	 */
	public SendMail setSubject(String subject) {
		this.subject_ = subject;
		return this;
	}

	/**
	 * 正文html
	 * 
	 * @return the 正文html
	 */
	public String getHtmlContent() {
		return htmlContent_;
	}

	/**
	 * 正文html
	 * 
	 * @param htmlContent the htmlContent_ to set
	 */
	public SendMail setHtmlContent(String htmlContent) {
		this.htmlContent_ = htmlContent;
		return this;
	}

	/**
	 * 正文 纯文本
	 * 
	 * @return the 纯文本
	 */
	public String getTextContent() {
		return textContent_;
	}

	/**
	 * 纯文本
	 * 
	 * @param textContent the textContent_ to set
	 */
	public SendMail setTextContent(String textContent) {
		this.textContent_ = textContent;
		return this;
	}

	/**
	 * 发件人
	 * 
	 * @return the 发件人
	 */
	public Addr getFrom() {
		return from_;
	}

	/**
	 * 收件人map
	 * 
	 * @return the 收件人
	 */
	public HashMap<String, Addr> getTos() {
		return tos_;
	}

	/**
	 * 抄送map
	 * 
	 * @return the 抄送map
	 */
	public HashMap<String, Addr> getCcs() {
		return ccs_;
	}

	/**
	 * 密送map
	 * 
	 * @return the 密送map
	 */
	public HashMap<String, Addr> getBccs() {
		return bccs_;
	}

	/**
	 * 获取邮件编码
	 * 
	 * @return the 邮件编码
	 */
	public String getCharset() {
		return charset_;
	}

	/**
	 * 设置邮件编码
	 * 
	 * @param charset the charset_ to set
	 * @return SendMail
	 */
	public SendMail setCharset(String charset) {
		this.charset_ = charset;
		return this;
	}

	/**
	 * 是否抄送给自己
	 * 
	 * @return the 是否抄送给自己
	 */
	public boolean isSendToSelf() {
		return isSendToSelf_;
	}

	/**
	 * 抄送给自己
	 * 
	 * @param sendToSelf the isSendToSelf_ to set
	 * @return SendMail
	 */
	public SendMail setSendToSelf(boolean sendToSelf) {
		this.isSendToSelf_ = sendToSelf;
		return this;
	}

	/**
	 * 要求阅读回执(收件人阅读邮件时会提示回复发件人,表明邮件已收到,并已阅读)
	 * 
	 * @return the 要求阅读回执
	 */
	public boolean isDispositionNotificationTo() {
		return isDispositionNotificationTo_;
	}

	/**
	 * 要求阅读回执(收件人阅读邮件时会提示回复发件人,表明邮件已收到,并已阅读)
	 * 
	 * @param dispositionNotificationTo 要求阅读回执
	 * @return SendMail
	 */
	public SendMail setDispositionNotificationTo(boolean dispositionNotificationTo) {
		this.isDispositionNotificationTo_ = dispositionNotificationTo;
		return this;
	}

	/**
	 * 是否自动创建html邮件的纯文本部分，便于降低垃圾邮件判别的评分，默认true
	 * 
	 * @return the isAutoTextPart_
	 */
	public boolean isAutoTextPart() {
		return isAutoTextPart_;
	}

	/**
	 * 是否自动创建html邮件的纯文本部分，便于降低垃圾邮件判别的评分，默认true
	 * 
	 * @param autoTextPart the isAutoTextPart_ to set
	 * @return SendMail
	 */
	public SendMail setAutoTextPart(boolean autoTextPart) {
		this.isAutoTextPart_ = autoTextPart;
		return this;
	}

	/**
	 * 邮件的回复头
	 * 
	 * @return the replayTos_
	 */
	public HashMap<String, Addr> getReplayTos() {
		return replayTos_;
	}

	/**
	 * 是否跟踪邮件发送细节
	 * 
	 * @return the is_mail_debug_
	 */
	public boolean isMailDebug() {
		return is_mail_debug_;
	}

	/**
	 * 是否跟踪邮件发送细节
	 * 
	 * @param mailDebug the is_mail_debug_ to set
	 * @return SendMail
	 */
	public SendMail setMailDebug(boolean mailDebug) {
		this.is_mail_debug_ = mailDebug;
		return this;
	}

	/**
	 * 获取最后的错误
	 * 
	 * @return the lastError
	 */
	public Exception getLastError() {
		return lastError;
	}

	/**
	 * 单一收件人，TO为多人，实际发送此人，用于跟踪
	 * 
	 * @return the singleTo_
	 */
	public Addr getSingleTo() {
		return singleTo_;
	}

	/**
	 * 设置单一收件人，TO为多人，实际发送此人，用于跟踪
	 * 
	 * @param singleToEmail 邮件地址
	 * @param singleToName  名称
	 * 
	 * @return SendMail
	 */
	public SendMail setSingleTo(String singleToEmail, String singleToName) {
		Addr addr = new Addr();
		addr.setEmail(singleToEmail);
		addr.setName(singleToName);
		this.singleTo_ = addr;
		return this;
	}

	/**
	 * 设定邮件内容
	 * 
	 * @param mimeMessage the mineMessage_ to set
	 * @return SendMail
	 */
	public SendMail setMimeMessage(MimeMessage mimeMessage) {
		this.mimeMessage_ = mimeMessage;
		return this;
	}

	/**
	 * 获取 发送邮件配置信息，用于修改
	 * 
	 * @return the props
	 */
	public Properties getProps() {
		return props;
	}

	/**
	 * 设置是否用 ssl协议进行发送邮件，端口465默认打开此协议
	 * 
	 * @param ssl ssl协议进行发送邮件
	 * @return SendMail
	 */
	public SendMail setUseSsl(boolean ssl) {
		props.put("mail.smtp.ssl.enable", ssl);
		return this;
	}

}
