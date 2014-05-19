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
import android.view.View;
import android.widget.Button;

import com.google.analytics.tracking.android.EasyTracker;

import no.digipost.android.R;

public class ScreenlockPreferenceActivity extends Activity {
	private static final String UNLOCK_ACTION = "com.android.credentials.UNLOCK";
	private Button yesButton;
    private Button privacyButton;

    @Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_screenlockpreference);
		yesButton = (Button) (findViewById(R.id.screenlockactivity_yes_button));
        Button noButton = (Button) (findViewById(R.id.screenlockactivity_no_button));
        privacyButton = (Button) findViewById(R.id.login_privacyButton);
        ButtonListener listener = new ButtonListener();
		yesButton.setOnClickListener(listener);
		noButton.setOnClickListener(listener);
        privacyButton.setOnClickListener(listener);
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

    private void yesButton() {
        startActivity(new Intent(UNLOCK_ACTION));
		finish();
	}

    private void openPrivayBrowser() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.digipost.no/juridisk/"));
        startActivity(browserIntent);
    }

	private class ButtonListener implements View.OnClickListener {

		public void onClick(final View v) {
			if (v == yesButton) {
				yesButton();
			} else if(v== privacyButton){
                openPrivayBrowser();
            } else{
				finish();
			}
		}
	}
}
