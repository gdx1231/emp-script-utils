
package com.gdxsoft.easyweb.utils.Mail;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import jakarta.mail.MessagingException;

import com.gdxsoft.easyweb.utils.UDigest;
import com.gdxsoft.easyweb.utils.URsa;
import com.sun.mail.util.CRLFOutputStream;

/*
 *   DKIM RFC 4871.
 * 
 */

public class DKIMSigner {

	private static int MAXHEADERLENGTH = 67;

	/**
	 * 最小的签名包含的字段列表
	 */
	private static String[] miniHeaders = "From,To,Subject".split(",");

	private String[] defaultHeadersToSign = new String[] { "Content-Description", "Content-ID", "Content-Type",
			"Content-Transfer-Encoding", "Cc", "Date", "From", "In-Reply-To", "List-Subscribe", "List-Post",
			"List-Owner", "List-Id", "List-Archive", "List-Help", "List-Unsubscribe", "MIME-Version", "Message-ID",
			"Resent-Sender", "Resent-Cc", "Resent-Date", "Resent-To", "Reply-To", "References", "Resent-Message-ID",
			"Resent-From", "Sender", "Subject", "To" };

	private DKIMAlgorithm signingAlgorithm = DKIMAlgorithm.rsa_sha256; // use rsa-sha256 by default, see RFC
																		// 4871
	private URsa rsa;
	private String signingDomain;
	private String selector;
	private String identity = null;
	private boolean lengthParam = false;
	private boolean zParam = false;
	private IDKIMCanonicalization headerCanonicalization = new DKIMCanonicalizationRelaxedImpl();
	private IDKIMCanonicalization bodyCanonicalization = new DKIMCanonicalizationSimpleImpl();
	private PrivateKey privkey;

	/**
	 * 初始化 DKIM
	 * 
	 * @param signingDomain 域名
	 * @param selector      选择
	 * @param privkey       私匙
	 * @throws Exception
	 */
	public DKIMSigner(String signingDomain, String selector, PrivateKey privkey) throws Exception {
		initDKIMSigner(signingDomain, selector, privkey);
	}

	/**
	 * 初始化 DKIM
	 * 
	 * @param signingDomain   域名
	 * @param selector        选择
	 * @param privkeyFilename 私匙路径
	 * @throws Exception
	 */
	public DKIMSigner(String signingDomain, String selector, String privkeyFilename) throws Exception {

		this.signingDomain = signingDomain;
		this.selector = selector.trim();
		
		DKIMAlgorithm algorithm = this.signingAlgorithm;
		
		this.rsa = new URsa();
		this.rsa.initPrivateKey(privkeyFilename);
		
		// 摘要算法
		this.rsa.setDigestAlgorithm(algorithm.getJavaHashNotation());
		// 签名算法
		this.rsa.setSignAlgorithm(algorithm.getJavaSecNotation());

		/*
		 * File privKeyFile = new File(privkeyFilename);
		 * 
		 * // read private key DER file DataInputStream dis = new DataInputStream(new
		 * FileInputStream(privKeyFile)); byte[] privKeyBytes = new byte[(int)
		 * privKeyFile.length()]; dis.read(privKeyBytes); dis.close();
		 * 
		 * KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		 * 
		 * // decode private key PKCS8EncodedKeySpec privSpec = new
		 * PKCS8EncodedKeySpec(privKeyBytes); RSAPrivateKey privKey = (RSAPrivateKey)
		 * keyFactory.generatePrivate(privSpec);
		 * 
		 * initDKIMSigner(signingDomain, selector, privKey);
		 */
	}

	/**
	 * 初始化对象
	 * 
	 * @param signingDomain 域名
	 * @param selector      选择
	 * @param privkey       私匙
	 * @throws Exception
	 */
	private void initDKIMSigner(String signingDomain, String selector, PrivateKey privkey) throws Exception {

		if (!DKIMUtil.isValidDomain(signingDomain)) {
			throw new Exception(signingDomain + " is an invalid signing domain");
		}

		this.signingDomain = signingDomain;
		this.selector = selector.trim();
		this.privkey = privkey;
		this.setSigningAlgorithm(this.signingAlgorithm);
	}

	/**
	 * 获取 identity
	 * 
	 * @return identity
	 */
	public String getIdentity() {
		return identity;
	}

	/**
	 * 设置 identity
	 * 
	 * @param identity
	 * @throws Exception
	 */
	public void setIdentity(String identity) throws Exception {

		if (identity != null) {
			identity = identity.trim();
			if (!identity.endsWith("@" + signingDomain) && !identity.endsWith("." + signingDomain)) {
				throw new Exception(
						"The domain part of " + identity + " has to be " + signingDomain + " or its subdomain");
			}
		}

		this.identity = identity;
	}

	public IDKIMCanonicalization getBodyCanonicalization() {
		return bodyCanonicalization;
	}

	public void setBodyCanonicalization(IDKIMCanonicalization bodyCanonicalization) throws Exception {
		this.bodyCanonicalization = bodyCanonicalization;
	}

	public IDKIMCanonicalization getHeaderCanonicalization() {
		return headerCanonicalization;
	}

	public void setHeaderCanonicalization(IDKIMCanonicalization headerCanonicalization) throws Exception {
		this.headerCanonicalization = headerCanonicalization;
	}

	public String[] getDefaultHeadersToSign() {
		return defaultHeadersToSign;
	}

	public void addHeaderToSign(String header) {

		if (header == null || "".equals(header))
			return;

		int len = this.defaultHeadersToSign.length;
		String[] headersToSign = new String[len + 1];
		for (int i = 0; i < len; i++) {
			if (header.equals(this.defaultHeadersToSign[i])) {
				return;
			}
			headersToSign[i] = this.defaultHeadersToSign[i];
		}

		headersToSign[len] = header;

		this.defaultHeadersToSign = headersToSign;
	}

	public void removeHeaderToSign(String header) {

		if (header == null || "".equals(header))
			return;

		int len = this.defaultHeadersToSign.length;
		if (len == 0)
			return;

		String[] headersToSign = new String[len - 1];

		int found = 0;
		for (int i = 0; i < len - 1; i++) {

			if (header.equals(this.defaultHeadersToSign[i + found])) {
				found = 1;
			}
			headersToSign[i] = this.defaultHeadersToSign[i + found];
		}

		this.defaultHeadersToSign = headersToSign;
	}

	public void setLengthParam(boolean lengthParam) {
		this.lengthParam = lengthParam;
	}

	public boolean getLengthParam() {
		return lengthParam;
	}

	public boolean isZParam() {
		return zParam;
	}

	public void setZParam(boolean param) {
		zParam = param;
	}

	public DKIMAlgorithm getSigningAlgorithm() {
		return signingAlgorithm;
	}

	/**
	 * 设置算法
	 * 
	 * @param algorithm
	 * @throws Exception
	 */
	public void setSigningAlgorithm(DKIMAlgorithm algorithm) throws Exception {
		this.rsa = new URsa();
		this.rsa.setPrivateKey((RSAPrivateKey) this.privkey);
		// 摘要算法
		this.rsa.setDigestAlgorithm(algorithm.getJavaHashNotation());
		// 签名算法
		this.rsa.setSignAlgorithm(algorithm.getJavaSecNotation());

		this.signingAlgorithm = algorithm;
	}

	/**
	 * 序列号签名
	 * 
	 * @param dkimSignature
	 * @return
	 */
	private String serializeDKIMSignature(Map<String, String> dkimSignature) {

		Set<Entry<String, String>> entries = dkimSignature.entrySet();
		StringBuffer buf = new StringBuffer(), fbuf;
		int pos = 0;

		Iterator<Entry<String, String>> iter = entries.iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();

			// buf.append(entry.getKey()).append("=").append(entry.getValue()).append(";\t");

			fbuf = new StringBuffer();
			fbuf.append(entry.getKey()).append("=").append(entry.getValue()).append(";");

			if (pos + fbuf.length() + 1 > MAXHEADERLENGTH) {

				pos = fbuf.length();

				// line folding : this doesn't work "sometimes" --> maybe someone likes to debug
				// this
				/*
				 * int i = 0; while (i<pos) { if (fbuf.substring(i).length()>MAXHEADERLENGTH) {
				 * buf.append("\r\n\t").append(fbuf.substring(i, i+MAXHEADERLENGTH)); i +=
				 * MAXHEADERLENGTH; } else { buf.append("\r\n\t").append(fbuf.substring(i)); pos
				 * -= i; break; } }
				 */

				buf.append("\r\n\t").append(fbuf);

			} else {
				buf.append(" ").append(fbuf);
				pos += fbuf.length() + 1;
			}
		}

		buf.append("\r\n\tb=");

		return buf.toString().trim();
	}

	/**
	 * 折叠签名内容，不超过没行 67个字符
	 * 
	 * @param s      签名
	 * @param offset 偏移量
	 * @return
	 */
	private String foldSignedSignature(String s, int offset) {

		int i = 0;
		StringBuffer buf = new StringBuffer();

		while (true) {
			if (offset > 0 && s.substring(i).length() > MAXHEADERLENGTH - offset) {
				buf.append(s.substring(i, i + MAXHEADERLENGTH - offset));
				i += MAXHEADERLENGTH - offset;
				offset = 0;
			} else if (s.substring(i).length() > MAXHEADERLENGTH) {
				buf.append("\r\n\t").append(s.substring(i, i + MAXHEADERLENGTH));
				i += MAXHEADERLENGTH;
			} else {
				buf.append("\r\n\t").append(s.substring(i));
				break;
			}
		}

		return buf.toString();
	}

	/**
	 * 签名邮件
	 * 
	 * @param message 邮件
	 * @return 签名
	 * @throws Exception
	 * @throws MessagingException
	 */
	public String sign(SMTPDKIMMessage message) throws Exception {

		Map<String, String> dkimSignature = new LinkedHashMap<String, String>();
		dkimSignature.put("v", "1");
		dkimSignature.put("a", this.signingAlgorithm.getRfc4871Notation());
		dkimSignature.put("q", "dns/txt");
		dkimSignature.put("c", getHeaderCanonicalization().getType() + "/" + getBodyCanonicalization().getType());
		dkimSignature.put("t", ((long) new Date().getTime() / 1000) + "");
		dkimSignature.put("s", this.selector);
		dkimSignature.put("d", this.signingDomain);

		// set identity inside signature
		if (identity != null) {
			dkimSignature.put("i", DKIMUtil.QuotedPrintable(identity));
		}

		StringBuffer headerContent = this.signHeader(message, dkimSignature);

		this.signBody(message, dkimSignature);

		// create signature
		String serializedSignature = serializeDKIMSignature(dkimSignature);

		byte[] signData = headerContent
				.append(this.headerCanonicalization.canonicalizeHeader("DKIM-Signature", " " + serializedSignature))
				.toString().getBytes();

		String signedSignature = this.rsa.signBase64(signData);
		// 折行
		String foldedSign = this.foldSignedSignature(signedSignature, 3);
		return "DKIM-Signature: " + serializedSignature + foldedSign;
	}

	private StringBuffer signHeader(SMTPDKIMMessage message, Map<String, String> dkimSignature) throws Exception {
		// 获取最小的签名包含的字段列表
		List<String> minimumHeders = new ArrayList<String>();
		for (int i = 0; i < miniHeaders.length; i++) {
			minimumHeders.add(miniHeaders[i].trim());
		}

		// intersect defaultHeadersToSign with available headers
		StringBuffer headerList = new StringBuffer();
		StringBuffer headerContent = new StringBuffer();
		StringBuffer zParamString = new StringBuffer();

		// 获取指定名称的邮件头
		Enumeration<String> headerLines = message.getMatchingHeaderLines(defaultHeadersToSign);
		int inc = 0;
		while (headerLines.hasMoreElements()) {
			String header = headerLines.nextElement();

			String[] headerParts = DKIMUtil.splitHeader(header);
			String name = headerParts[0];
			String value = headerParts[1];

			if (inc > 0) {
				headerList.append(":");
			}
			headerList.append(name);

			String canonicalizedHeader = this.headerCanonicalization.canonicalizeHeader(name, value);
			headerContent.append(canonicalizedHeader).append("\r\n");

			minimumHeders.remove(name);
			// add optional z= header list, DKIM-Quoted-Printable
			if (this.zParam) {
				zParamString.append("|");
				String zv = DKIMUtil.QuotedPrintable(value.trim()).replace("|", "=7C");
				zParamString.append(name).append(":").append(zv);
			}
			inc++;
		}

		if (!minimumHeders.isEmpty()) {
			String err = "Could not find the header fields " + DKIMUtil.concatArray(minimumHeders, ", ")
					+ " for signing";
			throw new Exception(err);
		}

		// h 参数，所有参与签名的头部
		dkimSignature.put("h", headerList.toString());

		if (this.zParam) {
			dkimSignature.put("z", zParamString.toString());
		}

		return headerContent;
	}

	/**
	 * 对邮件体进行签名, bh参数
	 * 
	 * @param message
	 * @param dkimSignature
	 * @throws Exception
	 */
	private void signBody(SMTPDKIMMessage message, Map<String, String> dkimSignature) throws Exception {
		// process body
		String body = message.getEncodedBody();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		CRLFOutputStream crlfos = new CRLFOutputStream(baos);
		try {
			crlfos.write(body.getBytes());
		} catch (IOException e) {
			throw new Exception("The body conversion to MIME canonical CRLF line terminator failed", e);
		}
		body = baos.toString();

		body = this.bodyCanonicalization.canonicalizeBody(body);

		if (this.lengthParam) {
			dkimSignature.put("l", body.length() + "");
		}

		String bh = UDigest.digestBase64(body.getBytes(), this.signingAlgorithm.getJavaHashNotation());
		// calculate and encode body hash
		dkimSignature.put("bh", bh);

	}
}
