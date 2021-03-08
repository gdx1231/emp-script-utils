package com.gdxsoft.easyweb.utils;

import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.modes.AEADBlockCipher;

/**
 * Inner class for BouncyCastle encrypt/decrypt
 */
public class OpCipher {
	UAes parant;
	boolean encrypt;
	Cipher cipher;
	BufferedBlockCipher cipherBufferBlock;
	AEADBlockCipher aeadBlockCipher;
	byte[] iv;
	byte[] key;

	public byte[] processBytes(byte[] source) throws DataLengthException, IllegalStateException,
			InvalidCipherTextException, IllegalBlockSizeException, BadPaddingException {
		if (this.cipherBufferBlock != null) {
			byte[] buffer = new byte[cipherBufferBlock.getOutputSize(source.length)];
			int pos = cipherBufferBlock.processBytes(source, 0, source.length, buffer, 0);
			pos += cipherBufferBlock.doFinal(buffer, pos);

			return Arrays.copyOf(buffer, pos);
		} else if (this.aeadBlockCipher != null) {
			byte[] buffer = new byte[aeadBlockCipher.getOutputSize(source.length)];
			int pos = aeadBlockCipher.processBytes(source, 0, source.length, buffer, 0);
			pos += aeadBlockCipher.doFinal(buffer, pos);

			return Arrays.copyOf(buffer, pos);
		} else if (cipher != null) {

			if (this.encrypt && !parant.getBlockCipherMode().equals("GCM") && !parant.getBlockCipherMode().equals("CCM")
					&& parant.getPaddingMethod().equals(UAes.NoPadding)) {
				// 填充Padding
				int blockSize = cipher.getBlockSize();
				int plaintextLength = source.length;
				if (plaintextLength % blockSize != 0) {
					plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
				}
				byte[] plaintext = new byte[plaintextLength];
				System.arraycopy(source, 0, plaintext, 0, source.length);

				return cipher.doFinal(plaintext);
			} else {
				return cipher.doFinal(source);
			}
		} else {
			return null;
		}
	}

	public UAes getParant() {
		return parant;
	}

	public boolean isEncrypt() {
		return encrypt;
	}

	public Cipher getCipher() {
		return cipher;
	}

	public BufferedBlockCipher getCipherBufferBlock() {
		return cipherBufferBlock;
	}

	public AEADBlockCipher getAeadBlockCipher() {
		return aeadBlockCipher;
	}

	public byte[] getIv() {
		return iv;
	}

	public byte[] getKey() {
		return key;
	}
}