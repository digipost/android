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

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import com.google.android.gms.analytics.GoogleAnalytics;
import no.digipost.android.analytics.GAEventController;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.authentication.AndroidLockSecurity;
import no.digipost.android.authentication.OAuth;
import no.digipost.android.gui.LoginActivity;
import no.digipost.android.gui.MainContentActivity;
import no.digipost.android.utilities.FileUtilities;
import no.digipost.android.utilities.SharedPreferencesUtilities;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((DigipostApplication) getApplication()).getTracker(DigipostApplication.TrackerName.APP_TRACKER);
		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onStart() {
		super.onStart();
		handleLaunchOrigin();
		FileUtilities.deleteTempFiles();
		checkAppVersion();
		checkTokenAndScreenlockStatus();
	}

	private void handleLaunchOrigin(){
		GoogleAnalytics.getInstance(this).reportActivityStart(this);
		if(getIntent() != null && getIntent().hasExtra("collapse_key")) {
			MainContentActivity.launchedFromPush = true;
			getIntent().removeExtra("collapse_key");
			GAEventController.sendLaunchEvent(this, GAEventController.LAUNCH_ORIGIN_PUSH);
		}else{
			MainContentActivity.launchedFromPush = false;
			GAEventController.sendLaunchEvent(this, GAEventController.LAUNCH_ORIGIN_NORMAL);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		GoogleAnalytics.getInstance(this).reportActivityStop(this);
		finish();
	}

	private void checkAppVersion(){
		boolean shouldDelete =  SharedPreferencesUtilities.shouldDeleteStoredRefreshToken(this);
		if(shouldDelete){
			SharedPreferencesUtilities.deleteRefreshtoken(this);
		}
	}

	private void checkTokenAndScreenlockStatus() {
		boolean canUseRefreshToken = AndroidLockSecurity.canUseRefreshTokens(this);
		boolean hasRefreshToken = (!SharedPreferencesUtilities.getEncryptedRefreshtokenCipher(this).isEmpty());
		if (canUseRefreshToken && hasRefreshToken) {
			new CheckTokenTask().execute();
		} else {
			startLoginActivity();
		}
	}

	private void startBaseActivity() {
		Intent i = new Intent(MainActivity.this, MainContentActivity.class);
		startActivity(i);
		finish();
	}

	private void startLoginActivity() {
		Intent i = new Intent(MainActivity.this, LoginActivity.class);
		startActivity(i);
		finish();
	}

	private class CheckTokenTask extends AsyncTask<Void, Void, String> {
		private boolean clearRefreshToken;

		public CheckTokenTask() {
			clearRefreshToken = false;
		}

		@Override
		protected String doInBackground(final Void... params) {

			try {
				OAuth.updateAccessTokenWithRefreshToken(MainActivity.this);
				return null;
			} catch (DigipostApiException e) {
				return e.getMessage();
			} catch (DigipostClientException e) {
				return e.getMessage();
			} catch (DigipostAuthenticationException e) {
				clearRefreshToken = true;
				return e.getMessage();
			}

		}

		@Override
		protected void onPostExecute(final String result) {
			if (result == null) {
				startBaseActivity();
			} else {
				if (clearRefreshToken) {
					SharedPreferencesUtilities.deleteRefreshtoken(MainActivity.this);
					startLoginActivity();
				} else {
					startBaseActivity();
				}
			}
		}
	}
}