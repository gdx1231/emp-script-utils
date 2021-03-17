package test.java;

import java.util.List;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;

import com.gdxsoft.easyweb.utils.UPath;
import com.gdxsoft.easyweb.utils.Mail.Attachment;
import com.gdxsoft.easyweb.utils.Mail.MailDecode;
import com.gdxsoft.easyweb.utils.Mail.MailLogHelper;
import com.gdxsoft.easyweb.utils.Mail.SendMail;

import org.junit.jupiter.api.Test;

public class TestMail extends TestBase {
	private int switchValue = 1;

	/**
	 * PraseMimeMessage类测试
	 */
	public static void main(String args[]) throws Exception {
		UPath.getCachedPath();
		TestMail t = new TestMail();
		t.switchValue = 1;
		t.testMail();
	}

	@Test
	public void testMail() throws Exception {
		super.printCaption("Test mail");
		if (switchValue == 0) {
			return;
		}
		String subject = "日本零碳终极武器——太空光伏发电";
		String content = "太空光伏发电”是指利用分布在太空的光伏电池板来发电，并通过微波向地面传输电力的技术。京都大学教授松本纮从1980年代就开始了相关研究，他的学生筱原真毅成功利用微波炉改进的设备让电视屏幕亮起";
		try {
			this.testMail("reminder@notify.gyap.org", "gdx1231@126.com", subject, content);
		} catch (Exception err) {
			System.out.println(err.getMessage());
		}
//		this.testMail("guolei@sina.com", "ios@oneworld.cc", subject, content);
//		this.testMail("guolei@sina.com.cn", "feng.wang@oneworld.cc", subject, content);
//
//		this.testMail("1231gdx@sohu.COM", "gl@oneworld.cc", subject, content);
//		this.testMail("Gdx1231@sohu.COM", "aws@oneworld.cc", subject, content);
//
//		this.testMail("gl@oneworld.cc", "gdx1231@gmail.com", subject, content);
//		this.testMail("aws@oneworld.cc", "lei.guo@gyap.org", subject, content);

	}

	public void testMail(String from, String to, String subject, String content) {
		SendMail sm = new SendMail().setMailDebug(true).setFrom(from).addTo(to).setSubject(subject)
				.setHtmlContent(content).setCharset("gbk");

		MailLogHelper maillog = new MailLogHelper();
		maillog.setShowConsole(true);
		sm.getMailSession().setDebug(true);
		sm.getMailSession().setDebugOut(maillog);

		super.printCaption(from + " " + to);
		sm.send();
		super.printCaption("send ok");
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
