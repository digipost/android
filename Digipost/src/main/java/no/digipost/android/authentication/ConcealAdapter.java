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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.facebook.android.crypto.keychain.AndroidConceal;
import com.facebook.crypto.CryptoConfig;
import com.facebook.crypto.keychain.KeyChain;
import org.apache.commons.io.output.ByteArrayOutputStream;
import android.content.Context;
import android.util.Log;
import android.util.Base64;
import com.facebook.crypto.Entity;
import com.facebook.crypto.exception.CryptoInitializationException;
import com.facebook.crypto.exception.KeyChainException;
import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;

public class ConcealAdapter implements CryptoAdapter{

	private final com.facebook.crypto.Crypto crypto;
	Entity entity = Entity.create("refresh_token");

	public ConcealAdapter(final Context context) {
		KeyChain keyChain = new SharedPrefsBackedKeyChain(context, CryptoConfig.KEY_256);
		crypto = AndroidConceal.get().createCrypto256Bits(keyChain);
	}

	public boolean isAvailable() {
		return crypto.isAvailable();
	}

	public String decrypt(final String cipherText) {
		try {
			InputStream inputStream = crypto.getCipherInputStream(
			  new ByteArrayInputStream(Base64.decode(cipherText.getBytes(), Base64.DEFAULT)),
			  entity);

			int read;
			byte[] buffer = new byte[1024];
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			while ((read = inputStream.read(buffer)) != -1) {
			  out.write(buffer, 0, read);
			}
			inputStream.close();
			out.close();
			return new String(out.toByteArray());
		} catch (Exception e) {
			Log.w("Crypto", e);
		}
		return null;
	}

	public String encrypt(final String plaintext) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			OutputStream outputStream = crypto.getCipherOutputStream(out, entity);
			outputStream.write(plaintext.getBytes());
			outputStream.close();
			return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
		} catch (CryptoInitializationException e) {
			e.printStackTrace();
			return null;
		} catch (KeyChainException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}
}
