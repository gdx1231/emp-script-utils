
package com.gdxsoft.easyweb.utils.Mail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gdxsoft.easyweb.utils.UDigest;
import com.gdxsoft.easyweb.utils.URsa;
import com.sun.mail.util.CRLFOutputStream;
import com.sun.mail.util.QPEncoderStream;

import jakarta.mail.MessagingException;

/*
 *   DKIM RFC 4871.
 * 
 */

public class DKIMSigner {

	private Logger LOGGER = LoggerFactory.getLogger(DKIMSigner.class);
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

		this.privkey = this.rsa.getPrivateKey();

		// 摘要算法, sha256 /sha1
		this.rsa.setDigestAlgorithm(algorithm.getDigestAlorithm());
		// 签名算法 SHA256withRSA/ SHA1withRSA
		this.rsa.setSignAlgorithm(algorithm.getSignAlgorithm());

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

		if (!isValidDomain(signingDomain)) {
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
		this.rsa.setDigestAlgorithm(algorithm.getDigestAlorithm());
		// 签名算法
		this.rsa.setSignAlgorithm(algorithm.getSignAlgorithm());

		this.signingAlgorithm = algorithm;
	}

	/**
	 * 序列化签名数据
	 * 
	 * @param dkimSignature 签名Map
	 * @return
	 */
	private String serializeDKIMSignature(Map<String, String> dkimSignature) {
		StringBuilder buf = new StringBuilder();
		int pos = 0;
		for (String key : dkimSignature.keySet()) {
			String value = dkimSignature.get(key);
			StringBuilder fbuf = new StringBuilder();
			fbuf.append(key).append("=").append(value).append(";");

			if (pos + fbuf.length() + 1 > MAXHEADERLENGTH) {
				pos = fbuf.length();
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
		StringBuilder buf = new StringBuilder();

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
		long t = new Date().getTime() / 1000l;

		Map<String, String> dkimSignature = new LinkedHashMap<String, String>();
		dkimSignature.put("v", "1");
		dkimSignature.put("a", this.signingAlgorithm.getRfc4871Notation()); // rsa-sha256
		dkimSignature.put("q", "dns/txt");
		// relaxed/simple;
		dkimSignature.put("c", getHeaderCanonicalization().getType() + "/" + getBodyCanonicalization().getType());
		dkimSignature.put("t", t + "");
		dkimSignature.put("s", this.selector);
		dkimSignature.put("d", this.signingDomain);

		// set identity inside signature
		if (identity != null) {
			dkimSignature.put("i", QuotedPrintable(identity));
		}

		StringBuilder headerContent = this.signHeader(message, dkimSignature);

		// bh 参数 body 的 sha256 摘要
		this.digestBody(message, dkimSignature);

		// create signature
		String serializedSignature = serializeDKIMSignature(dkimSignature);

		String canonicalizeDkimSignature = this.headerCanonicalization.canonicalizeHeader("DKIM-Signature",
				serializedSignature);
		headerContent.append(canonicalizeDkimSignature);

		// 签名算法 RSAWithSHA256
		byte[] signData = headerContent.toString().getBytes();
		String signedSignature = this.rsa.signBase64(signData);

		// 折行
		String foldedSign = this.foldSignedSignature(signedSignature, 3);

		String signatureHeader = "DKIM-Signature: " + serializedSignature + foldedSign;

		LOGGER.debug(signatureHeader);

		return signatureHeader;
	}

	/**
	 * 头部组合签名
	 * 
	 * @param message
	 * @param dkimSignature
	 * @return
	 * @throws Exception
	 */
	private StringBuilder signHeader(SMTPDKIMMessage message, Map<String, String> dkimSignature) throws Exception {
		// 获取最小的签名包含的字段列表
		List<String> minimumHeders = new ArrayList<String>();
		for (int i = 0; i < miniHeaders.length; i++) {
			minimumHeders.add(miniHeaders[i].trim());
		}

		// intersect defaultHeadersToSign with available headers
		StringBuilder headerList = new StringBuilder();
		StringBuilder headerContent = new StringBuilder();
		StringBuilder zParamString = new StringBuilder();

		// 获取指定名称的邮件头
		Enumeration<String> headerLines = message.getMatchingHeaderLines(defaultHeadersToSign);
		int inc = 0;
		while (headerLines.hasMoreElements()) {
			String header = headerLines.nextElement();

			String[] headerParts = splitHeader(header);
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
				String zv = QuotedPrintable(value.trim()).replace("|", "=7C");
				zParamString.append(name).append(":").append(zv);
			}
			inc++;
		}

		if (!minimumHeders.isEmpty()) {
			String err = "Could not find the header fields " + concatArray(minimumHeders, ", ") + " for signing";
			LOGGER.error(err);
			throw new Exception(err);
		}

		// h 参数，所有参与签名的头部
		dkimSignature.put("h", headerList.toString());

		if (this.zParam) {
			dkimSignature.put("z", zParamString.toString());
		}
		LOGGER.debug(headerContent.toString());
		return headerContent;
	}

	/**
	 * 对邮件体进行签名, bh参数
	 * 
	 * @param message
	 * @param dkimSignature
	 * @throws Exception
	 */
	private void digestBody(SMTPDKIMMessage message, Map<String, String> dkimSignature) throws Exception {
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
		// sha256 or sha1
		String bh = UDigest.digestBase64(body.getBytes(), this.signingAlgorithm.getDigestAlorithm());
		// calculate and encode body hash
		dkimSignature.put("bh", bh);

	}

	private static String[] splitHeader(String header) throws Exception {
		int colonPos = header.indexOf(':');
		if (colonPos == -1) {
			throw new Exception("The header string " + header + " is no valid RFC 822 header-line");
		}
		return new String[] { header.substring(0, colonPos), header.substring(colonPos + 1) };
	}

	private static String concatArray(List<?> l, String separator) {
		StringBuffer buf = new StringBuffer();
		Iterator<?> iter = l.iterator();
		while (iter.hasNext()) {
			buf.append(iter.next()).append(separator);
		}

		return buf.substring(0, buf.length() - separator.length());
	}

	private static boolean isValidDomain(String domainname) {
		Pattern pattern = Pattern.compile("(.+)\\.(.+)");
		Matcher matcher = pattern.matcher(domainname);
		return matcher.matches();
	}

	private static String QuotedPrintable(String s) {
		QPEncoderStream encodeStream = null;
		try {
			ByteArrayOutputStream boas = new ByteArrayOutputStream();
			encodeStream = new QPEncoderStream(boas);
			encodeStream.write(s.getBytes());

			String encoded = boas.toString();
			encoded = encoded.replaceAll(";", "=3B");
			encoded = encoded.replaceAll(" ", "=20");

			return encoded;

		} catch (IOException ioe) {
		} finally {
			if (encodeStream != null)
				try {
					encodeStream.close();
				} catch (IOException e) {
				}
		}
		return null;
	}
}
