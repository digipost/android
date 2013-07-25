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
import no.digipost.android.authentication.KeyStore;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.utilities.DialogUtitities;
import no.digipost.android.utilities.NetworkUtilities;
import no.digipost.android.utilities.SharedPreferencesUtilities;
import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.google.analytics.tracking.android.EasyTracker;

public class LoginActivity extends Activity {
	private Button loginButton, privacyButton, registrationButton;
	private CheckBox stayLoggedInCheckBox;
	private ButtonListener listener;
	private KeyStore ks;
    private Context context;

	private final int WEB_LOGIN_REQUEST = 1;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		ks = KeyStore.getInstance();
        context = this;
		listener = new ButtonListener();
		loginButton = (Button) findViewById(R.id.login_loginButton);
		loginButton.setOnClickListener(listener);
		privacyButton = (Button) findViewById(R.id.login_privacyButton);
		privacyButton.setOnClickListener(listener);
		registrationButton = (Button) findViewById(R.id.login_registrationButton);
		registrationButton.setOnClickListener(listener);
		stayLoggedInCheckBox = (CheckBox) findViewById(R.id.login_remember_me);
	}

	@Override
	protected void onResume() {
		super.onResume();
		enableCheckBoxIfScreenlock();
		deleteOldRefreshtoken();
		stayLoggedInCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (stayLoggedInCheckBox.isChecked()) {
					KeyStore ks = KeyStore.getInstance();
					if (ks.state() != KeyStore.State.UNLOCKED) {
						Intent i = new Intent(LoginActivity.this, ScreenlockPreferenceActivity.class);
						startActivity(i);
					}
				}
			}
		});
	}

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this);
    }

    private void deleteOldRefreshtoken() {
		if (ks.state() == KeyStore.State.UNLOCKED) {
			SharedPreferencesUtilities.deleteRefreshtoken(this);
		}
	}

	private void enableCheckBoxIfScreenlock() {
		if (ks.state() == KeyStore.State.UNLOCKED) {
			stayLoggedInCheckBox.setChecked(true);
		} else {
			stayLoggedInCheckBox.setChecked(false);
		}
	}

	private void startLoginProcess() {
		if (stayLoggedInCheckBox.isChecked()) {
			SharedPreferencesUtilities.storeScreenlockChoice(this, ApplicationConstants.SCREENLOCK_CHOICE_YES);
			openWebView();
		} else {
			SharedPreferencesUtilities.storeScreenlockChoice(this, ApplicationConstants.SCREENLOCK_CHOICE_NO);
			openWebView();
		}
	}

	private void openWebView() {
		if (NetworkUtilities.isOnline()) {
			Intent i = new Intent(this, WebLoginActivity.class);
			startActivityForResult(i, WEB_LOGIN_REQUEST);
		} else {
            DialogUtitities.showToast(context, getString(R.string.error_your_network));
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == WEB_LOGIN_REQUEST) {
			if (resultCode == RESULT_OK) {
                startMainContentActivity();
			}
		}
	}

	private void startMainContentActivity() {
		loginButton.setVisibility(View.INVISIBLE);
		Intent i = new Intent(LoginActivity.this, MainContentActivity.class);
		startActivity(i);
		finish();
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
