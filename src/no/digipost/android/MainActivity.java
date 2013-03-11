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

import no.digipost.android.authentication.KeyStore;
import no.digipost.android.authentication.OAuth2;
import no.digipost.android.gui.BaseActivity;
import no.digipost.android.gui.LoginActivity;
import no.digipost.android.gui.NetworkConnection;
import android.accounts.NetworkErrorException;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends Activity {

	public static final String UNLOCK_ACTION = "com.android.credentials.UNLOCK";
	private Context context;
	private boolean pinQuestion;
	private KeyStore ks;
	private NetworkConnection networkConnection;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;

		networkConnection = new NetworkConnection(this);

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
				startActivity(new Intent(UNLOCK_ACTION));
			} else {
				finish();
			}
		} catch (ActivityNotFoundException e) {
			return;
		}
	}

	private void checkTokenStatus() {
		boolean hasInternetAccess = networkConnection.isOnline();
		if (OAuth2.getEncryptedRefreshToken(this) != "") {
			if (hasInternetAccess) {
				new CheckTokenTask().execute();
			} else {
				showMessage(getString(R.string.error_your_network));
				startBaseActivity();
			}
		} else {
			if (!hasInternetAccess) {
				showMessage(getString(R.string.error_your_network));
			}

			startLoginActivity();
		}
	}

	public void showMessage(final String message) {
		Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
		toast.show();
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
				OAuth2.updateRefreshTokenSuccess(context);
				return null;
			} catch (IllegalStateException e) {
				return e.getMessage();
			} catch (NetworkErrorException e) {
				return e.getMessage();
			}
		}

		@Override
		protected void onPostExecute(final String result) {
			if (result == null) {
				startBaseActivity();
			} else {
				showMessage(result);
				startLoginActivity();
			}
		}
	}
}
