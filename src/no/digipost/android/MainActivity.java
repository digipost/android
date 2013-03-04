/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.digipost.android;

import no.digipost.android.api.ApiConstants;
import no.digipost.android.authentication.KeyStore;
import no.digipost.android.authentication.KeyStoreAdapter;
import no.digipost.android.authentication.OAuth2;
import no.digipost.android.gui.BaseActivity;
import no.digipost.android.gui.LoginActivity;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class MainActivity extends Activity {

	public static final String UNLOCK_ACTION = "com.android.credentials.UNLOCK";
	private Context context;
	private KeyStore ks;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;
	}

	@Override
	protected void onResume() {
		super.onResume();
		ks = KeyStore.getInstance();
		checkKeyStoreStatus();
	}

	@Override
	protected void onStop() {
		super.onStop();
		finish();
	}

	private void checkKeyStoreStatus() {
		if (ks.state() != KeyStore.State.UNLOCKED) {
			unlockKeyStore();
		} else {
			checkTokenStatus();
		}
	}

	private void unlockKeyStore() {
		if (ks.state() == KeyStore.State.UNLOCKED) {
			return;
		}
		try {
			startActivity(new Intent(UNLOCK_ACTION));
		} catch (ActivityNotFoundException e) {
			return;
		}
	}

	private void checkTokenStatus() {
		if (checkToken()) {
			startBaseActivity();
		} else {
			startLoginActivity();
		}
	}

	private void startBaseActivity() {
		Intent i = new Intent(MainActivity.this, BaseActivity.class);
		startActivity(i);
		finish();
	}

	private void startLoginActivity() {
		Intent i = new Intent(MainActivity.this, LoginActivity.class);
		startActivity(i);
		finish();
	}

	private boolean checkToken() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		String encrypted_refresh_token = settings.getString(ApiConstants.REFRESH_TOKEN, "");
		if (encrypted_refresh_token.equals("")) {
			return false;
		} else {
			KeyStoreAdapter ksa = new KeyStoreAdapter();
			String refresh_token = ksa.decrypt(encrypted_refresh_token);
			OAuth2.retriveAccessTokenSuccess(refresh_token);
			return true;
		}
	}
}
