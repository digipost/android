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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import com.google.android.gms.analytics.GoogleAnalytics;
import no.digipost.android.DigipostApplication;
import no.digipost.android.R;

public class ScreenlockPreferenceActivity extends AppCompatActivity {
    private static final String UNLOCK_ACTION = "com.android.credentials.UNLOCK";
    private Button yesButton;
    private Button privacyButton;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((DigipostApplication) getApplication()).getTracker(DigipostApplication.TrackerName.APP_TRACKER);
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
        setContentView(R.layout.activity_screenlockpreference);
        yesButton = (Button) (findViewById(R.id.screenlockactivity_yes_button));
        Button noButton = (Button) (findViewById(R.id.screenlockactivity_no_button));
        privacyButton = (Button) findViewById(R.id.login_privacyButton);
        ButtonListener listener = new ButtonListener();
        yesButton.setOnClickListener(listener);
        noButton.setOnClickListener(listener);
        privacyButton.setOnClickListener(listener);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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

    private void yesButton() {
        startActivity(new Intent(UNLOCK_ACTION));
        finish();
    }

    private void openPrivacyBrowser() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.digipost.no/juridisk/"));
        startActivity(browserIntent);
    }

    private class ButtonListener implements View.OnClickListener {

        public void onClick(final View v) {
            if (v == yesButton) {
                yesButton();
            } else if (v == privacyButton) {
                openPrivacyBrowser();
            } else {
                finish();
            }
        }
    }
}
