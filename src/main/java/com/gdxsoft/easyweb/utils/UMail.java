package com.gdxsoft.easyweb.utils;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.utils.Mail.DKIMCfg;
import com.gdxsoft.easyweb.utils.Mail.SendMail;
import com.gdxsoft.easyweb.utils.Mail.SmtpCfg;
import com.gdxsoft.easyweb.utils.Mail.SmtpCfgs;

public class UMail {
	private static Logger LOG = LoggerFactory.getLogger(UMail.class);

	/**
	 * Get the default MailSession (UPath)
	 * 
	 * @return MailSession
	 */
	@Deprecated
	public static Session getMailSession() {
		return SmtpCfgs.createMailSession(SmtpCfgs.getDefaultSmtpCfg());
	}

	/**
	 * Get a mime message
	 * 
	 * @param from    The mail from
	 * @param to      The mail to
	 * @param subject The mail subject
	 * @param content The mail content
	 * @return message
	 * @throws UnsupportedEncodingException
	 * @throws MessagingException
	 */
	public static MimeMessage getMimeMessage(String from, String to, String subject, String content)
			throws UnsupportedEncodingException, MessagingException {
		return getMimeMessage(from, from, to, to, subject, content, null, "utf-8");
	}

	/**
	 * Get a mime message
	 * 
	 * @param from     The mail from
	 * @param fromName The mail from name
	 * @param to       The mail to
	 * @param toName   The mail to name
	 * @param subject  The mail subject
	 * @param content  The mail content
	 * @param atts     The attachments
	 * @param charset  The mail character set
	 * @return message
	 * 
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	public static MimeMessage getMimeMessage(String from, String fromName, String to, String toName, String subject,
			String content, String[] atts, String charset) throws MessagingException, UnsupportedEncodingException {

		SendMail sendmail = new SendMail();

		sendmail.setFrom(from, fromName).addTo(to, toName).setSubject(subject).setHtmlContent(content)
				.setCharset(charset);
		if (atts != null) {
			for (int i = 0; i < atts.length; i++) {
				String filePath = atts[i];
				if (filePath == null || filePath.trim().length() == 0) {
					continue;
				}
				sendmail.addAttach(filePath);
			}
		}
		return sendmail.getMimeMessage();
	}

	/**
	 * Get a mime message
	 * 
	 * @param from         The mail from
	 * @param fromName     The mail from name
	 * @param tos          The mail tos
	 * @param toNames      The mail to names
	 * @param ccs          The mail ccs
	 * @param ccNames      The mail cc names
	 * @param bccs         The mail bccs
	 * @param bccNames     The mail bcc names
	 * @param replyTos     The mail replayTos
	 * @param replyToNames The mail replayTo names
	 * @param sender       The mail sender
	 * @param senderName   The mail sender name
	 * @param subject      The mail subject
	 * @param content      The mail content
	 * @param atts         The mail attachments
	 * @param charset      The mail character set
	 * @return message
	 * 
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	public static MimeMessage getMimeMessage(String from, String fromName, String[] tos, String[] toNames, String[] ccs,
			String[] ccNames, String[] bccs, String[] bccNames, String[] replyTos, String[] replyToNames, String sender,
			String senderName, String subject, String content, String[] atts, String charset)
			throws MessagingException, UnsupportedEncodingException {

		SendMail sendmail = UMail.createSendMail(from, fromName, tos, toNames, ccs, ccNames, bccs, bccNames, replyTos,
				replyToNames, sender, senderName, subject, content, atts, charset);
		return sendmail.getMimeMessage();
	}

	/**
	 * Create a SendMail
	 * 
	 * @param from         The mail from
	 * @param fromName     The mail from name
	 * @param tos          The mail tos
	 * @param toNames      The mail to names
	 * @param ccs          The mail ccs
	 * @param ccNames      The mail cc names
	 * @param bccs         The mail bccs
	 * @param bccNames     The mail bcc names
	 * @param replyTos     The mail replayTos
	 * @param replyToNames The mail replayTo names
	 * @param sender       The mail sender
	 * @param senderName   The mail sender name
	 * @param subject      The mail subject
	 * @param content      The mail content
	 * @param atts         The mail attachments
	 * @param charset      The mail character set
	 * @return SendMail
	 */
	public static SendMail createSendMail(String from, String fromName, String[] tos, String[] toNames, String[] ccs,
			String[] ccNames, String[] bccs, String[] bccNames, String[] replyTos, String[] replyToNames, String sender,
			String senderName, String subject, String content, String[] atts, String charset) {
		SendMail sendmail = new SendMail();

		sendmail.setFrom(from, fromName).addTos(tos, toNames).addCcs(ccs, ccNames).addBccs(bccs, bccNames)
				.addReplyTos(replyTos, replyToNames).addAttachs(atts, null).setSubject(subject).setHtmlContent(content);
		if (sender != null) {
			sendmail.setSender(sender, senderName);
		}
		if (charset != null) {
			sendmail.setCharset(charset);
		}

		DKIMCfg cfg = SmtpCfgs.getDkim(from);
		if (cfg != null) {
			sendmail.setDkim(cfg);
		}

		return sendmail;
	}

	/**
	 * Get the domain from the email
	 * 
	 * @param email The email
	 * @return The domain
	 */
	public static String getEmailDomain(String email) {
		if (email == null || email.trim().length() == 0) {
			return null;
		}
		String[] aa = email.split("\\@");
		if (aa.length == 2) {
			String domain = aa[1];
			return domain;
		}
		return null;
	}


	/**
	 * Send a email
	 * 
	 * @param from         The mail from
	 * @param fromName     The mail from name
	 * @param tos          The mail tos
	 * @param toNames      The mail to names
	 * @param ccs          The mail ccs
	 * @param ccNames      The mail cc names
	 * @param bccs         The mail bccs
	 * @param bccNames     The mail bcc names
	 * @param replyTos     The mail replayTos
	 * @param replyToNames The mail replayTo names
	 * @param sender       The mail sender
	 * @param senderName   The mail sender name
	 * @param subject      The mail subject
	 * @param content      The mail content
	 * @param atts         The mail attachments
	 * @param charset      The mail character set
	 * @return result
	 */
	public static String sendHtmlMail(String from, String fromName, String[] tos, String[] toNames, String[] ccs,
			String[] ccNames, String[] bccs, String[] bccNames, String[] replyTos, String[] replyToNames, String sender,
			String senderName, String subject, String content, String[] atts, String charset) {
		try {
			MimeMessage mm = UMail.getMimeMessage(from, fromName, tos, toNames, ccs, ccNames, bccs, bccNames, replyTos,
					replyToNames, sender, senderName, subject, content, atts, charset);
			return sendMail(mm);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			return e.getMessage();
		}
	}

	/**
	 * Send a email
	 * 
	 * @param from         The mail from
	 * @param fromName     The mail from name
	 * @param tos          The mail tos, separate with commas
	 * @param toNames      The mail to names, separate with commas
	 * @param replyTos     The mail replayTos, separate with commas
	 * @param replyToNames The mail replayTo names separate with commas
	 * @param subject      The mail subject
	 * @param content      The mail content
	 * @param atts         The mail attachments
	 * @return result
	 */
	public static String sendHtmlMail(String from, String fromName, String tos, String toNames, String replyTos,
			String replyToNames, String subject, String content, String[] atts, String charset) {
		String[] tos1 = tos.split(",");
		String[] toNames1 = toNames == null ? null : toNames.split(",");

		String[] replyTos1 = replyTos == null ? null : replyTos.split(",");
		String[] replyToNames1 = replyToNames == null ? null : replyToNames.split(",");
		return UMail.sendHtmlMail(from, fromName, tos1, toNames1, null, null, null, null, replyTos1, replyToNames1,
				null, null, subject, content, atts, charset);

	}

	/**
	 * Send a email
	 * 
	 * @param from     The mail from
	 * @param fromName The mail from name
	 * @param tos      The mail tos, separate with commas
	 * @param toNames  The mail to names, separate with commas
	 * @param subject  The mail subject
	 * @param content  The mail content
	 * @param atts     The mail attachments
	 * @param charset  The character set
	 * @return 是否成功
	 */
	public static String sendHtmlMail(String from, String fromName, String tos, String toNames, String subject,
			String content, String[] atts, String charset) {
		return UMail.sendHtmlMail(from, fromName, tos, toNames, null, null, subject, content, atts, charset);
	}

	/**
	 * Send a email
	 * 
	 * @param from     The mail from
	 * @param fromName The mail from name
	 * @param tos      The mail tos, separate with commas
	 * @param toNames  The mail to names, separate with commas
	 * @param subject  The mail subject
	 * @param content  The mail content
	 * @return result
	 */
	public static String sendHtmlMail(String from, String fromName, String tos, String toNames, String subject,
			String content) {
		return sendHtmlMail(from, fromName, tos, toNames, subject, content, null, "utf-8");
	}

	/**
	 * Send a email
	 * 
	 * @param from     The mail from
	 * @param fromName The mail from name
	 * @param tos      The mail tos, separate with commas
	 * @param toNames  The mail to names, separate with commas
	 * @param subject  The mail subject
	 * @param content  The mail content
	 * @param atts     The mail attachments
	 * @return result
	 */
	public static String sendHtmlMail(String from, String fromName, String tos, String toNames, String subject,
			String content, String[] atts) {
		return sendHtmlMail(from, fromName, tos, toNames, subject, content, atts, "utf-8");
	}

	/**
	 * Send a email
	 * 
	 * @param from    The mail from
	 * @param tos     The mail tos, separate with commas
	 * @param subject The mail subject
	 * @param content The mail content
	 * @param atts    The mail attachments
	 * @return result
	 */
	public static String sendHtmlMail(String from, String tos, String subject, String content, String[] atts) {
		return sendHtmlMail(from, "", tos, "", subject, content, atts, "utf-8");
	}

	/**
	 * Send a email
	 * 
	 * @param from    The mail from
	 * @param tos     The mail tos, separate with commas
	 * @param subject The mail subject
	 * @param content The mail content
	 * @return result
	 */
	public static String sendHtmlMail(String from, String tos, String subject, String content) {
		return sendHtmlMail(from, "", tos, "", subject, content, null, "utf-8");
	}

	/**
	 * Send a email
	 * 
	 * @param message The email message
	 * @return result
	 */
	public static String sendMail(MimeMessage message) {
		
		SmtpCfg smtpCfg = SmtpCfgs.getSmtpCfg(message);
		Session mailSession = SmtpCfgs.createMailSession(smtpCfg);
		
		try {
			Transport transport = mailSession.getTransport();
			transport.connect();
			Transport.send(message, message.getAllRecipients());
			transport.close();
			return null;
		} catch (Exception e) {
			LOG.error(e.getMessage());
			return e.getMessage();
		}
	}

}
