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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import com.google.android.gms.analytics.GoogleAnalytics;
import no.digipost.android.DigipostApplication;
import no.digipost.android.R;
import no.digipost.android.authentication.KeyStoreAdapter;
import no.digipost.android.authentication.Security;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.utilities.DialogUtitities;
import no.digipost.android.utilities.NetworkUtilities;
import no.digipost.android.utilities.SharedPreferencesUtilities;

public class LoginActivity extends Activity {
    private Button loginButton, privacyButton, registrationButton;
    private CheckBox rememberMe;
    private KeyStoreAdapter ks;
    private Context context;

    private final int WEB_LOGIN_REQUEST = 1;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((DigipostApplication) getApplication()).getTracker(DigipostApplication.TrackerName.APP_TRACKER);
        setContentView(R.layout.activity_login);
        context = this;
        ks = new KeyStoreAdapter(this);
        ButtonListener listener = new ButtonListener();
        loginButton = (Button) findViewById(R.id.login_loginButton);
        loginButton.setOnClickListener(listener);
        privacyButton = (Button) findViewById(R.id.login_privacyButton);
        privacyButton.setOnClickListener(listener);
        registrationButton = (Button) findViewById(R.id.login_registrationButton);
        registrationButton.setOnClickListener(listener);
        rememberMe = (CheckBox) findViewById(R.id.login_remember_me);

        if (!ks.isAvailable()) {
            rememberMe.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableCheckBoxIfScreenlock();
        deleteOldRefreshtoken();
        rememberMe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                if (rememberMe.isChecked()) {
                    if (!Security.screenLockEnabled(getApplicationContext())) {
                        Intent i = new Intent(LoginActivity.this, ScreenlockPreferenceActivity.class);
                        startActivity(i);
                    }
                }
            }
        });
        privacyButton.setTextColor(getResources().getColor(R.color.login_grey_text));
        registrationButton.setTextColor(getResources().getColor(R.color.login_grey_text));
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    private void deleteOldRefreshtoken() {
        if (ks.isAvailable()) {
            SharedPreferencesUtilities.deleteRefreshtoken(this);
        }
    }

    private void enableCheckBoxIfScreenlock() {
        if (!Security.canUseRefreshTokens(this)) {
            rememberMe.setChecked(false);
        }
    }

    private void startLoginProcess() {
        if (rememberMe.isChecked()) {
            SharedPreferencesUtilities.storeScreenlockChoice(this, ApplicationConstants.SCREENLOCK_CHOICE_YES);
            openWebView();
        } else {
            SharedPreferencesUtilities.storeScreenlockChoice(this, ApplicationConstants.SCREENLOCK_CHOICE_NO);
            openWebView();
        }
    }

    private void openWebView() {
        Log.d("LoginActivity","openWebView");
        if (NetworkUtilities.isOnline()) {
            Intent i = new Intent(this, WebLoginActivity.class);
            startActivityForResult(i, WEB_LOGIN_REQUEST);
        } else {
            DialogUtitities.showToast(context, getString(R.string.error_your_network));
        }

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
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
                privacyButton.setTextColor(getResources().getColor(R.color.grey_filesize));
                openPrivayBrowser();
            } else if (v == registrationButton) {
                registrationButton.setTextColor(getResources().getColor(R.color.grey_filesize));
                openRegistrationDialog();
            }
        }
    }
}
