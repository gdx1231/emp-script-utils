package test.java;

import java.util.List;
import java.util.Properties;

import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.MimeMessage;

import com.gdxsoft.easyweb.utils.UMail;
import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.Mail.Attachment;
import com.gdxsoft.easyweb.utils.Mail.DKIMCfg;
import com.gdxsoft.easyweb.utils.Mail.MailDecode;
import com.gdxsoft.easyweb.utils.Mail.SendMail;

import org.junit.Test;

public class TestMail extends TestBase {
	/**
	 * PraseMimeMessage类测试
	 */
	public static void main(String args[]) throws Exception {
		TestMail t = new TestMail();
		t.testMail();
	}

	@Test
	public void testMail() throws Exception {
		String host = "127.0.0.1"; //

		String username = "lei.guo@oneworld.cc";
		String password = "";

		DKIMCfg cfg = new DKIMCfg();
		cfg.setDomain("oneworld.cc");
		cfg.setSelect("gdx");
		String der = UPath.getRealPath() + "resources/test.der";
		System.out.println(der);

		cfg.setPrivateKeyPath(der);
		super.printCaption("send mail to gdx1231@gmail.com");

		SendMail sm = new SendMail().setFrom(username).addTo("gdx1231@gmail.com").setSubject("发送smtp邮件")
				.setTextContent("发送smtp邮件").setDkim(cfg).setMessageId("<aa" + Math.random() + ">");

		System.out.println(" init " + host + "," + username + ", " + password);
		sm.initProps(host, 25, username, password);
		System.out.println(" start send ");
		sm.send();
		super.printCaption("send ok");

		/*
		 * super.printCaption("发送smtp邮件"); this.sendMail(host, username, password);
		 * 
		 * super.printCaption("读取pop3邮件"); this.readPop3Mails(host, username, password);
		 */
	}

	public void sendMail(String host, String username, String password) {
		if (host == null || username == null || password == null) {
			super.captionLength("skip test");
			return;
		}

		UMail.sendHtmlMail(username, username, "test", "sdkfklsd");
	}

	public void readPop3Mails(String host, String username, String password) throws Exception {

		if (host == null || username == null || password == null) {
			super.captionLength("skip test");
			return;
		}

		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		Store store = session.getStore("pop3");
		store.connect(host, username, password);
		Folder folder = store.getFolder("INBOX");
		folder.open(Folder.READ_ONLY);
		Message message[] = folder.getMessages();
		System.out.println("Messages's　length:　" + message.length);
		MailDecode pmm = null;
		int inc = 0;
		for (int i = message.length - 1; i >= 0; i--) {
			inc++;
			if (inc > 2) {
				break;
			}
			pmm = new MailDecode((MimeMessage) message[i], "D:\\image");
			System.out.println("Message　" + i + "　subject:　" + pmm.getSubject());
			System.out.println("Message　" + i + "　sentdate:　" + pmm.getSentDate());
			System.out.println("Message　" + i + "　replysign:　" + pmm.getReplySign());
			System.out.println("Message　新的" + i + "　hasRead:　" + pmm.isNew());
			System.out.println("Message　附件" + i + "　　containAttachment:　" + pmm.isContainAttach((Part) message[i]));
			System.out.println("Message　" + i + "　form:　" + pmm.getFrom());
			System.out.println("Message　" + i + "　to:　" + pmm.getMailAddress("to"));
			System.out.println("Message　" + i + "　cc:　" + pmm.getMailAddress("cc"));
			System.out.println("Message　" + i + "　bcc:　" + pmm.getMailAddress("bcc"));
			System.out.println("Message" + i + "　sentdate:　" + pmm.getSentDate());
			System.out.println("Message　" + i + "　Message-ID:　" + pmm.getMessageId());
			System.out.println("Message　正文" + i + "　bodycontent:　\r\n" + pmm.getBodyText());

			pmm.saveAttachments();

			List<Attachment> atts = pmm.getAtts();
			for (int k = 0; k < atts.size(); k++) {
				Attachment att = atts.get(k);
				System.out.println(att.toString());
			}
		}
	}

}
