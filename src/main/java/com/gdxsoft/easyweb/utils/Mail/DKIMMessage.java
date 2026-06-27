package com.gdxsoft.easyweb.utils.Mail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeUtility;

public class DKIMMessage extends MimeMessage {
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
	 * Output the message as an RFC 822 format stream, without specified headers.
	 */
	@Override
	public void writeTo(OutputStream os, String[] ignoreList) throws IOException, MessagingException {

		ByteArrayOutputStream osBody = new ByteArrayOutputStream();

		if (!saved) {
			saveChanges();
		}

		// Write body to buffer
		if (modified) {
			OutputStream osEncoding = MimeUtility.encode(osBody, this.getEncoding());
			this.getDataHandler().writeTo(osEncoding);
			osEncoding.flush();
		} else {
			if (content == null) {
				InputStream is = getContentStream();
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

		// Sign the message
		String signatureHeaderLine;
		try {
			signatureHeaderLine = signer.sign(this);
		} catch (Exception e) {
			throw new MessagingException(e.getLocalizedMessage(), e);
		}

		// Write headers manually with CRLF (replaces com.sun.mail.util.LineOutputStream)
		writeLine(os, signatureHeaderLine);

		Enumeration<?> hdrLines = getNonMatchingHeaderLines(ignoreList);
		while (hdrLines.hasMoreElements()) {
			writeLine(os, (String) hdrLines.nextElement());
		}

		// CRLF separator between header and content
		writeLine(os, "");

		// Write body
		os.write(osBody.toByteArray());
		os.flush();
	}

	private void writeLine(OutputStream os, String line) throws IOException {
		os.write((line + "\r\n").getBytes(java.nio.charset.StandardCharsets.US_ASCII));
	}

	public String getEncodedBody() {
		return encodedBody;
	}

	public void setEncodedBody(String encodedBody) {
		this.encodedBody = encodedBody;
	}

	// Don't allow to switch to 8-bit MIME
	public void setAllow8bitMIME(boolean allow) {
		// no-op: keep 7-bit ascii for DKIM compatibility
	}
}
