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
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ScreenlockPreferenceActivity extends Activity {
	private Button yesButton, noButton;
	private ButtonListener listener;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_screenlockpreference);
		yesButton = (Button) (findViewById(R.id.screenlockactivity_yes_button));
		noButton = (Button) (findViewById(R.id.screenlockactivity_no_button));
		listener = new ButtonListener();
		yesButton.setOnClickListener(listener);
		noButton.setOnClickListener(listener);
	}

	private void yesButton() {
		startActivity(new Intent(KeyStore.UNLOCK_ACTION));
		finish();
	}

	private class ButtonListener implements View.OnClickListener {

		public void onClick(final View v) {
			if (v == yesButton) {
				yesButton();
			} else {
				finish();
			}
		}
	}
}
