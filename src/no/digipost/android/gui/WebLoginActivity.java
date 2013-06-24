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
import no.digipost.android.api.DigipostApiException;
import no.digipost.android.api.DigipostAuthenticationException;
import no.digipost.android.api.DigipostClientException;
import no.digipost.android.authentication.OAuth2;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class WebLoginActivity extends Activity {

	private WebView webViewOauth;

    @SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_web);
        webViewOauth = (WebView) findViewById(R.id.web_oauth);
        String url = OAuth2.getAuthorizeURL();
        webViewOauth.loadUrl(url);
        WebSettings settings = webViewOauth.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        webViewOauth.setWebViewClient(new MyWebViewClient());
	}

	private class MyWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(final WebView view, final String url) {

			String state_fragment = "&" + ApiConstants.STATE + "=";
			int state_start = url.indexOf(state_fragment);
			String code_fragment = "&" + ApiConstants.CODE + "=";
			int code_start = url.indexOf(code_fragment);

			if (code_start > -1) {
				String state = url.substring(state_start + state_fragment.length(), code_start);
				String code = url.substring(code_start + code_fragment.length(), url.length());

				new GetAccessTokenTask().execute(state, code);

				return true;
			}

			return false;
		}

		@Override
		public void onReceivedError(final WebView view, final int errorCode, final String description, final String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
            setResult(RESULT_CANCELED);
            finish();
		}
	}

	private class GetAccessTokenTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(final String... params) {

			try {
				OAuth2.retriveInitialAccessToken(params[0], params[1], WebLoginActivity.this);
                return null;
			} catch (DigipostApiException e) {
                return e.getMessage();
			} catch (DigipostClientException e) {
                return e.getMessage();
			} catch (DigipostAuthenticationException e) {
			    return e.getMessage();
			}
		}

		@Override
		protected void onPostExecute(final String result) {
            if (result != null) {
                showMessage(result);
                setResult(RESULT_CANCELED);
            } else {
                setResult(RESULT_OK);
            }

            finish();
		}

	}

    private void showMessage(final String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 200);
        toast.show();
    }
}