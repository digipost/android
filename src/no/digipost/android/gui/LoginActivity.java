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
import no.digipost.android.api.ApiConstants;
import no.digipost.android.authentication.KeyStore;
import no.digipost.android.authentication.SharedPreferencesUtil;

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
import android.widget.CheckBox;
import android.widget.Toast;

public class LoginActivity extends Activity {
	private Button loginButton, privacyButton, registrationButton;
    private CheckBox stayLoggedInCheckBox;
	private ButtonListener listener;
	private NetworkConnection networkConnection;
    private KeyStore ks;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
        ks = KeyStore.getInstance();
        listener = new ButtonListener();
        stayLoggedInCheckBox = (CheckBox)findViewById(R.id.login_stay_logged_in_checkbox);
        //TODO OPEN YES NO DIALOG LYTTER
        loginButton = (Button) findViewById(R.id.login_loginButton);
		loginButton.setOnClickListener(listener);
		privacyButton = (Button) findViewById(R.id.login_privacyButton);
		privacyButton.setOnClickListener(listener);
		registrationButton = (Button) findViewById(R.id.login_registrationButton);
		registrationButton.setOnClickListener(listener);
		networkConnection = new NetworkConnection(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableCheckBoxIfScreenlock();
        deleteOldRefreshtoken();
    }

    private void deleteOldRefreshtoken(){
        if (ks.state() == KeyStore.State.UNLOCKED) {
            SharedPreferencesUtil.deleteRefreshtoken(this);
        }
    }
    private void enableCheckBoxIfScreenlock(){
        if (ks.state() == KeyStore.State.UNLOCKED) {
            stayLoggedInCheckBox.setChecked(true);
        }else{
            stayLoggedInCheckBox.setChecked(false);
        }
    }

    private void startLoginProcess(){
        if (stayLoggedInCheckBox.isChecked()) {
            SharedPreferencesUtil.storeScreenlockChoice(this,ApiConstants.SCREENLOCK_CHOICE_YES);
            KeyStore ks = KeyStore.getInstance();
            if (ks.state() != KeyStore.State.UNLOCKED) {
                startActivity(new Intent(KeyStore.UNLOCK_ACTION));
            }else{
                openWebView();
            }
        }else{
            SharedPreferencesUtil.storeScreenlockChoice(this,ApiConstants.SCREENLOCK_CHOICE_NO);
            openWebView();
        }
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
		Intent i = new Intent(LoginActivity.this, BaseFragmentActivity.class);
		startActivity(i);
		finish();
	}

	private class WebFragmentHandler extends Handler {
		@Override
		public void handleMessage(final Message msg) {
			super.handleMessage(msg);
			if (msg.what == ApiConstants.ERROR_OK) {
				startBaseActivity();
			} else if (msg.what == ApiConstants.ERROR_DEVICE) {
				showMessage(getString(R.string.error_your_network));
			} else if (msg.what == ApiConstants.ERROR_GENERAL) {
				showMessage(getString(R.string.error_wrong_credentials));
			} else if (msg.what == ApiConstants.ERROR_SERVER) {
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
                startLoginProcess();
			} else if (v == privacyButton) {
				openPrivayBrowser();
			} else if (v == registrationButton) {
				openRegistrationDialog();
			}
		}
	}
}
