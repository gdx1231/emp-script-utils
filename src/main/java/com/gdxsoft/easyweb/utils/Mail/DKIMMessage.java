package com.gdxsoft.easyweb.utils.Mail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import com.sun.mail.smtp.SMTPMessage;
import com.sun.mail.util.LineOutputStream;

public class DKIMMessage extends SMTPMessage {
	private String customerMessageId;

	private DKIMSigner signer;
	private String encodedBody;

	public DKIMMessage(Session session, DKIMSigner signer) {
		super(session);
		this.signer = signer;
	}

	public DKIMMessage(MimeMessage message, DKIMSigner signer) throws MessagingException {
		super(message);
		this.signer = signer;
	}

	public DKIMMessage(Session session, InputStream is, DKIMSigner signer) throws MessagingException {
		super(session, is);
		this.signer = signer;
	}

	@Override
	protected void updateMessageID() throws MessagingException {
		if (this.customerMessageId != null) {
			setHeader("Message-ID", customerMessageId);
		} else {
			super.updateMessageID();
		}
	}

	/**
	 * Redefined the MessageId
	 * @return the customerMessageId
	 */
	public String getCustomerMessageId() {
		return customerMessageId;
	}

	/**
	 * Redefined the MessageId
	 * @param customerMessageId the customerMessageId to set
	 */
	public void seCustomerMessageId(String customerMessageId) {
		this.customerMessageId = customerMessageId;
	}

	/**
	 * Output the message as an RFC 822 format stream, without specified headers. If
	 * the <code>saved</code> flag is not set, the <code>saveChanges</code> method
	 * is called. If the <code>modified</code> flag is not set and the
	 * <code>content</code> array is not null, the <code>content</code> array is
	 * written directly, after writing the appropriate message headers.
	 *
	 */
	public void writeTo(OutputStream os, String[] ignoreList) throws IOException, MessagingException {

		ByteArrayOutputStream osBody = new ByteArrayOutputStream();

		// Inside saveChanges() it is assured that content encodings are set in all
		// parts of the body
		if (!saved) {
			saveChanges();
		}

		// First, write out the body to the body buffer
		if (modified) {
			// Finally, the content. Encode if required.
			OutputStream osEncoding = MimeUtility.encode(osBody, this.getEncoding());
			this.getDataHandler().writeTo(osEncoding);
			osEncoding.flush(); // Needed to complete encoding
		} else {
			// Else, the content is untouched, so we can just output it
			// Finally, the content.
			if (content == null) {
				// call getContentStream to give subclass a chance to
				// provide the data on demand
				InputStream is = getContentStream();
				// now copy the data to the output stream
				byte[] buf = new byte[8192];
				int len;
				while ((len = is.read(buf)) > 0)
					osBody.write(buf, 0, len);
				is.close();
				buf = null;
			} else {
				osBody.write(content);
			}
			osBody.flush();
		}
		encodedBody = osBody.toString();

		// Second, sign the message
		String signatureHeaderLine;
		try {
			signatureHeaderLine = signer.sign(this);
		} catch (Exception e) {
			throw new MessagingException(e.getLocalizedMessage(), e);
		}

		// Third, write out the header to the header buffer
		LineOutputStream los = new LineOutputStream(os);

		// set generated signature to the top
		los.writeln(signatureHeaderLine);

		Enumeration<?> hdrLines = getNonMatchingHeaderLines(ignoreList);
		while (hdrLines.hasMoreElements()) {
			los.writeln((String) hdrLines.nextElement());
		}

		// The CRLF separator between header and content
		los.writeln();

		// Send signed mail to waiting DATA command
		os.write(osBody.toByteArray());
		os.flush();
	}

	public String getEncodedBody() {
		return encodedBody;
	}

	public void setEncodedBody(String encodedBody) {
		this.encodedBody = encodedBody;
	}

	// Don't allow to switch to 8-bit MIME, instead 7-bit ascii should be kept
	// 'cause in forwarding scenarios a change to Content-Transfer-Encoding
	// to 7-bit ascii breaks DKIM signatures
	public void setAllow8bitMIME(boolean allow) {
		// super.setAllow8bitMIME(false);
	}
}
