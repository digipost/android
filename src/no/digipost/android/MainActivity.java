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

import java.util.concurrent.ExecutionException;

import no.digipost.android.api.ApiConstants;
import no.digipost.android.authentication.KeyStore;
import no.digipost.android.authentication.KeyStoreAdapter;
import no.digipost.android.authentication.OAuth2;
import no.digipost.android.authentication.Secret;
import no.digipost.android.gui.BaseActivity;
import no.digipost.android.gui.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class MainActivity extends Activity {

	public static final String UNLOCK_ACTION = "com.android.credentials.UNLOCK";
	Context context;
	KeyStore ks;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;
		ks = KeyStore.getInstance();
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkKeyStoreStatus();

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
		try {
			CheckTokenTask task = new CheckTokenTask();
			boolean hasValidToken = task.execute().get();
			if (hasValidToken) {
				startBaseActivity();
			} else {
				startLoginActivity();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
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

	private class CheckTokenTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(final String... params) {
			try {
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
				String encrypted_refresh_token = settings.getString(ApiConstants.REFRESH_TOKEN, "");
				if (encrypted_refresh_token.equals("")) {
					return false;
				} else {

					KeyStoreAdapter ksa = new KeyStoreAdapter();
					String refresh_token = ksa.decrypt(encrypted_refresh_token);
					JSONObject data;
					try {
						data = OAuth2.getRefreshAccessToken(refresh_token);
						Secret.ACCESS_TOKEN = data.getString(ApiConstants.ACCESS_TOKEN);
						return true;

					} catch (JSONException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				return false;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
	}
}
