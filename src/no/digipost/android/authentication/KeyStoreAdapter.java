/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.digipost.android.authentication;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import no.digipost.android.constants.ApiConstants;

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
