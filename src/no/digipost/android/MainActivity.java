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

import no.digipost.android.api.DigipostApiException;
import no.digipost.android.api.DigipostClientException;
import no.digipost.android.authentication.KeyStore;
import no.digipost.android.authentication.OAuth2;
import no.digipost.android.gui.BaseActivity;
import no.digipost.android.gui.LoginActivity;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

public class MainActivity extends Activity {

	private boolean pinQuestion;
	private KeyStore ks;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onResume() {
		super.onResume();
		ks = KeyStore.getInstance();
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
			if (!pinQuestion) {
				pinQuestion = true;
				startActivity(new Intent(KeyStore.UNLOCK_ACTION));
			} else {
				finish();
			}
		} catch (ActivityNotFoundException e) {
			return;
		}
	}

	private void checkTokenStatus() {
		if (OAuth2.getEncryptedRefreshToken(this).isEmpty()) {
			startLoginActivity();
		} else {
			new CheckTokenTask().execute();
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

	private class CheckTokenTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(final Void... params) {

			try {
				OAuth2.updateAccessToken(getApplicationContext());
				return null;
			} catch (DigipostApiException e) {
				return e.getMessage();
			} catch (DigipostClientException e) {
				return e.getMessage();
			}

		}

		@Override
		protected void onPostExecute(final String result) {
			startBaseActivity();
		}
	}
}
