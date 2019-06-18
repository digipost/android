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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import no.digipost.android.DigipostApplication;
import no.digipost.android.R;
import no.digipost.android.analytics.GAEventController;
import no.digipost.android.authentication.DigipostOauthScope;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.gcm.GCMController;
import no.digipost.android.utilities.DialogUtitities;
import no.digipost.android.utilities.NetworkUtilities;

public class LoginActivity extends Activity {
    private final int WEB_OAUTH_LOGIN_REQUEST = 0;
    private Button loginButton, privacyButton, registrationButton, idPortenButton;
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
        loginButton = findViewById(R.id.login_passwordButton);
        loginButton.setOnClickListener(listener);
        loginButton.setTransformationMethod(null);
        privacyButton = (Button) findViewById(R.id.login_privacyButton);
        privacyButton.setOnClickListener(listener);
        privacyButton.setTransformationMethod(null);

        registrationButton = findViewById(R.id.login_registrationButton);
        registrationButton.setOnClickListener(listener);
        registrationButton.setTransformationMethod(null);

        idPortenButton = findViewById(R.id.login_idportenButton);
        idPortenButton.setOnClickListener(listener);
        idPortenButton.setTransformationMethod(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        GCMController.reset(getApplicationContext());
    }

    private void openWebView(Login target) {
        if (NetworkUtilities.isOnline()) {
            Intent i = new Intent(this, WebLoginActivity.class);

            if(Login.IDPORTEN == target){
                i.putExtra("authenticationScope", DigipostOauthScope.FULL_IDPORTEN4.asApiConstant());
            }else {
                i.putExtra("authenticationScope", DigipostOauthScope.FULL.asApiConstant());
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
        loginButton.setVisibility(View.INVISIBLE);
        Intent i = new Intent(LoginActivity.this, MainContentActivity.class);
        startActivity(i);
        finish();
    }

    private void openExternalBrowserWithUrl(String url){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    private class ButtonListener implements OnClickListener {

        public void onClick(final View v) {
            if (v == loginButton) {
                GAEventController.sendLoginClickEvent(LoginActivity.this, "passord");
                openWebView(Login.NORMAL);
            } else if (v == privacyButton) {
                GAEventController.sendLoginClickEvent(LoginActivity.this, "personvern");
                openExternalBrowserWithUrl(ApiConstants.URL_PRIVACY);
            } else if (v == registrationButton) {
                GAEventController.sendLoginClickEvent(LoginActivity.this, "registrering");
                openExternalBrowserWithUrl(ApiConstants.URL_REGISTRATION);
            }else if(v == idPortenButton){
                GAEventController.sendLoginClickEvent(LoginActivity.this, "idporten");
                openWebView(Login.IDPORTEN);
            }
        }
    }
}
