package no.digipost.android.authentication;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import android.util.Base64;
import android.util.Log;

public class Crypto {

	private static final String TAG = Crypto.class.getSimpleName();

	private static String DELIMITER = "]";

	private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
	private static int KEY_LENGTH = 256;

	private static SecureRandom random = new SecureRandom();

	private Crypto() {
	}

	public static SecretKey generateKey() {
		try {
			KeyGenerator kg = KeyGenerator.getInstance("AES");
			kg.init(KEY_LENGTH);
			SecretKey key = kg.generateKey();

			return key;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}

	}

	public static byte[] generateIv(final int length) {
		byte[] b = new byte[length];
		random.nextBytes(b);

		return b;
	}

	public static String encrypt(final String plaintext, final SecretKey key) {
		try {
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);

			byte[] iv = generateIv(cipher.getBlockSize());
			Log.d(TAG, "IV: " + toHex(iv));
			IvParameterSpec ivParams = new IvParameterSpec(iv);
			cipher.init(Cipher.ENCRYPT_MODE, key, ivParams);
			Log.d(TAG, "Cipher IV: " + (cipher.getIV() == null ? null : toHex(cipher.getIV())));
			byte[] cipherText = cipher.doFinal(plaintext.getBytes("UTF-8"));

			return String.format("%s%s%s", toBase64(iv), DELIMITER, toBase64(cipherText));
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String toHex(final byte[] bytes) {
		StringBuffer buff = new StringBuffer();
		for (byte b : bytes) {
			buff.append(String.format("%02X", b));
		}

		return buff.toString();
	}

	public static String toBase64(final byte[] bytes) {
		return Base64.encodeToString(bytes, Base64.NO_WRAP);
	}

	public static byte[] fromBase64(final String base64) {
		return Base64.decode(base64, Base64.NO_WRAP);
	}

	public static String decrypt(final String ciphertext, final SecretKey key) {
		try {
			String[] fields = ciphertext.split(DELIMITER);
			if (fields.length != 2) {
				throw new IllegalArgumentException("Invalid encypted text format");
			}

			byte[] iv = fromBase64(fields[0]);
			byte[] cipherBytes = fromBase64(fields[1]);
			Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
			IvParameterSpec ivParams = new IvParameterSpec(iv);
			cipher.init(Cipher.DECRYPT_MODE, key, ivParams);
			Log.d(TAG, "Cipher IV: " + toHex(cipher.getIV()));
			byte[] plaintext = cipher.doFinal(cipherBytes);
			String plainrStr = new String(plaintext, "UTF-8");

			return plainrStr;
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

}
