package no.digipost.android.authentication;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import no.digipost.android.api.ApiConstants;

public class KeyStoreAdapter {

	KeyStore ks;

	public KeyStoreAdapter() {
		ks = KeyStore.getInstance();
	}

	public String decrypt(final String ciphertext) {
		byte[] keyBytes = ks.get(ApiConstants.REFRESH_TOKEN);
		if (keyBytes == null) {
			return null;
		}
		SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
		String plaintext = Crypto.decrypt(ciphertext, key);
		return plaintext;
	}

	public String encrypt(final String plaintext) {
		SecretKey key = Crypto.generateKey();
		ks.put(ApiConstants.REFRESH_TOKEN, key.getEncoded());
		String ciphertext = Crypto.encrypt(plaintext, key);
		return ciphertext;
	}
}