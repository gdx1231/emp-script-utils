/* 
 * Copyright 2008 The Apache Software Foundation or its licensors, as
 * applicable.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * A licence was granted to the ASF by Florian Sager on 30 November 2008
 */

package com.gdxsoft.easyweb.utils.Mail;

/*
 * Allowed signing algorithms by DKIM RFC 4871 with translation to different Java notations
 * 
 * @author Florian Sager, http://www.agitos.de, 22.11.2008
 */

public class DKIMAlgorithm {
	/**
	 * 算法 rsa_sha256
	 */
	public final static DKIMAlgorithm rsa_sha256 = new DKIMAlgorithm("rsa-sha256", "SHA256withRSA", "sha256");

	/**
	 * 算法 rsa_sha1
	 */
	public final static DKIMAlgorithm rsa_sha1 = new DKIMAlgorithm("rsa-sha1", "SHA1withRSA", "sha1");

	private String rfc4871Notation;

	private String signAlgorithm;
	private String digestAlorithm;

	/**
	 * 
	 * @param rfc4871Notation RFC 4871 format
	 * @param signAlgorithm   sign
	 * @param digestAlorithm  hashing digest
	 */
	public DKIMAlgorithm(String rfc4871Notation, String signAlgorithm, String digestAlorithm) {
		this.rfc4871Notation = rfc4871Notation;
		this.signAlgorithm = signAlgorithm;
		this.digestAlorithm = digestAlorithm;
	}

	/**
	 * rsa-sha256 / rsa-sha1
	 * 
	 * @return the rfc4871Notation name
	 */
	public String getRfc4871Notation() {
		return rfc4871Notation;
	}

	/**
	 * the sign algorithm
	 * 
	 * @return signAlgorithm
	 */
	public String getSignAlgorithm() {
		return signAlgorithm;
	}

	/**
	 * The digest digestAlorithm
	 * 
	 * @return digestAlorithm
	 */
	public String getDigestAlorithm() {
		return digestAlorithm;
	}

}
