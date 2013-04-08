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
package no.digipost.android.gui;

import no.digipost.android.R;
import no.digipost.android.api.ErrorHandling;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class LoginActivity extends Activity {
	private Button loginButton, privacyButton, registrationButton;
	private ButtonListener listener;
	private NetworkConnection networkConnection;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		listener = new ButtonListener();
		loginButton = (Button) findViewById(R.id.login_loginButton);
		loginButton.setOnClickListener(listener);
		privacyButton = (Button) findViewById(R.id.login_privacyButton);
		privacyButton.setOnClickListener(listener);
		registrationButton = (Button) findViewById(R.id.login_registrationButton);
		registrationButton.setOnClickListener(listener);
		networkConnection = new NetworkConnection(this);
	}

	private void openWebView() {
		if (networkConnection.isOnline()) {
			WebLoginDialogFragment webView = new WebLoginDialogFragment(new WebFragmentHandler());
			webView.show(getFragmentManager(), "webView");
		} else {
			showMessage(getString(R.string.error_your_network));
		}

	}

	private void showMessage(final String message) {
		Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 200);
		toast.show();
	}

	private void startBaseActivity() {
		loginButton.setVisibility(View.INVISIBLE);
		Intent i = new Intent(LoginActivity.this, BaseActivity.class);
		startActivity(i);
		finish();
	}

	private class WebFragmentHandler extends Handler {
		@Override
		public void handleMessage(final Message msg) {
			super.handleMessage(msg);
			if (msg.what == ErrorHandling.ERROR_OK) {
				startBaseActivity();
			} else if (msg.what == ErrorHandling.ERROR_DEVICE) {
				showMessage(getString(R.string.error_your_network));
			} else if (msg.what == ErrorHandling.ERROR_GENERAL) {
				showMessage(getString(R.string.error_wrong_credentials));
			} else if (msg.what == ErrorHandling.ERROR_SERVER) {
				showMessage(getString(R.string.error_digipost_api));
			}
		}
	}

	private void openPrivayBrowser() {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.digipost.no/juridisk/"));
		startActivity(browserIntent);
	}

	public void openRegistrationDialog() {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.digipost.no/app/registrering#/"));
		startActivity(browserIntent);
	}

	private class ButtonListener implements OnClickListener {

		public void onClick(final View v) {
			if (v == loginButton) {
				openWebView();
			} else if (v == privacyButton) {
				openPrivayBrowser();
			} else if (v == registrationButton) {
				openRegistrationDialog();
			}
		}
	}
}
