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
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import no.digipost.android.DigipostApplication;
import no.digipost.android.R;
import no.digipost.android.analytics.GAEventController;
import no.digipost.android.authentication.AndroidLockSecurity;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.gcm.GCMController;
import no.digipost.android.utilities.DialogUtitities;
import no.digipost.android.utilities.NetworkUtilities;
import no.digipost.android.utilities.SharedPreferencesUtilities;

public class LoginActivity extends Activity {
    private final int WEB_OAUTH_LOGIN_REQUEST = 0;
    private Button loginButton, privacyButton, registrationButton;
    private CheckBox rememberCheckbox;
    private Context context;
    private enum Login{
        IDPORTEN,
        NORMAL
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((DigipostApplication) getApplication()).getTracker(DigipostApplication.TrackerName.APP_TRACKER);
        setContentView(R.layout.activity_login);
        context = this;
        ButtonListener listener = new ButtonListener();
        loginButton = (Button) findViewById(R.id.login_loginButton);
        loginButton.setOnClickListener(listener);
        privacyButton = (Button) findViewById(R.id.login_privacyButton);
        privacyButton.setOnClickListener(listener);
        registrationButton = (Button) findViewById(R.id.login_registrationButton);
        registrationButton.setOnClickListener(listener);
        rememberCheckbox = (CheckBox) findViewById(R.id.login_remember_me);
        LinearLayout idPortenLayout = (LinearLayout) findViewById(R.id.login_id_porten_layout);
        idPortenLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                openWebView(Login.IDPORTEN);
            }
        });

        TextView idPortenTextView = (TextView) findViewById(R.id.login_id_porten_textview);
        final SpannableStringBuilder str = new SpannableStringBuilder(getString(R.string.login_id_porten_login_text_button));
        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, 22, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        idPortenTextView.setText(str);
    }

    @Override
    protected void onResume() {
        super.onResume();
        GCMController.reset(getApplicationContext());
        enableCheckBoxIfScreenlock();

        rememberCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                if (rememberCheckbox.isChecked()) {
                    if (!AndroidLockSecurity.screenLockEnabled(getApplicationContext())) {
                        Intent i = new Intent(LoginActivity.this, ScreenlockPreferenceActivity.class);
                        startActivity(i);
                    }
                }
            }
        });
        privacyButton.setTextColor(getResources().getColor(R.color.grey_privacy_button_text));
        registrationButton.setTextColor(getResources().getColor(R.color.login_grey_text));
    }

    private void enableCheckBoxIfScreenlock() {
        if (!AndroidLockSecurity.canUseRefreshTokens(this)) {
            rememberCheckbox.setChecked(false);
        }
    }

    private void startLoginProcess() {
        if (rememberCheckbox.isChecked()) {
            SharedPreferencesUtilities.storeScreenlockChoice(this, ApplicationConstants.SCREENLOCK_CHOICE_YES);
            openWebView(Login.NORMAL);
        } else {
            SharedPreferencesUtilities.storeScreenlockChoice(this, ApplicationConstants.SCREENLOCK_CHOICE_NO);
            openWebView(Login.NORMAL);
        }
    }

    private void openWebView(Login target) {
        if (NetworkUtilities.isOnline()) {
            Intent i = new Intent(this, WebLoginActivity.class);
            if(Login.IDPORTEN == target){
                i.putExtra("authenticationScope", ApiConstants.SCOPE_IDPORTEN_4);
            }else {
                i.putExtra("authenticationScope", ApiConstants.SCOPE_FULL);
            }
            startActivityForResult(i, WEB_OAUTH_LOGIN_REQUEST);

        } else {
            DialogUtitities.showToast(context, getString(R.string.error_your_network));
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == WEB_OAUTH_LOGIN_REQUEST) {
            if (resultCode == RESULT_OK) {
                startMainContentActivity();
            }
        }
    }

    private void startMainContentActivity() {
        GAEventController.sendRememberMeEvent(this, rememberCheckbox.isChecked());
        loginButton.setVisibility(View.INVISIBLE);
        Intent i = new Intent(LoginActivity.this, MainContentActivity.class);
        startActivity(i);
        finish();
    }

    private void openPrivacyBrowser() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.digipost.no/juridisk/"));
        startActivity(browserIntent);
    }

    public void openRegistrationDialog() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.digipost.no/app/registrering?utm_source=android_app&utm_medium=app&utm_campaign=app-link&utm_content=ny_bruker#/"));
        startActivity(browserIntent);
    }

    private class ButtonListener implements OnClickListener {

        public void onClick(final View v) {
            if (v == loginButton) {
                startLoginProcess();
            } else if (v == privacyButton) {
                privacyButton.setTextColor(getResources().getColor(R.color.grey_filesize));
                openPrivacyBrowser();
            } else if (v == registrationButton) {
                registrationButton.setTextColor(getResources().getColor(R.color.grey_filesize));
                openRegistrationDialog();
            }
        }
    }
}
