 

package com.gdxsoft.easyweb.utils.Mail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.mail.util.QPEncoderStream;

public class DKIMUtil {

	 

	protected static String[] splitHeader(String header) throws Exception {
		int colonPos = header.indexOf(':');
		if (colonPos == -1) {
			throw new Exception("The header string " + header + " is no valid RFC 822 header-line");
		}
		return new String[] { header.substring(0, colonPos), header.substring(colonPos + 1) };
	}

	protected static String concatArray(List<?> l, String separator) {
		StringBuffer buf = new StringBuffer();
		Iterator<?> iter = l.iterator();
		while (iter.hasNext()) {
			buf.append(iter.next()).append(separator);
		}

		return buf.substring(0, buf.length() - separator.length());
	}

	protected static boolean isValidDomain(String domainname) {
		Pattern pattern = Pattern.compile("(.+)\\.(.+)");
		Matcher matcher = pattern.matcher(domainname);
		return matcher.matches();
	}

	// FSTODO: converts to "platforms default encoding" might be wrong ?
	protected static String QuotedPrintable(String s) {
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
