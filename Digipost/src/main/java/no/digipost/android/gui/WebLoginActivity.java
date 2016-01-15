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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.*;
import android.webkit.WebSettings.LayoutAlgorithm;
import com.google.android.gms.analytics.GoogleAnalytics;
import no.digipost.android.DigipostApplication;
import no.digipost.android.R;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.authentication.OAuth2;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.utilities.DialogUtitities;
import no.digipost.android.utilities.NetworkUtilities;

public class WebLoginActivity extends Activity {

    private WebView webViewOauth;
    private Context context;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((DigipostApplication) getApplication()).getTracker(DigipostApplication.TrackerName.APP_TRACKER);
        setContentView(R.layout.fragment_web);
        context = this;

        if (!NetworkUtilities.isOnline()) {
            DialogUtitities.showToast(context, getString(R.string.error_your_network));
            finish();
        }

        getActionBar().setTitle(R.string.login_loginbutton_text);
        getActionBar().setHomeButtonEnabled(true);
        webViewOauth = (WebView) findViewById(R.id.web_oauth);
        String url = OAuth2.getAuthorizeURL();
        webViewOauth.loadUrl(url);
        WebSettings settings = webViewOauth.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        webViewOauth.setWebViewClient(new MyWebViewClient());
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
            Log.d("WebView","shouldOverrideUrlLoading");
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

        @SuppressWarnings("deprecation")
        @Override
        public void onReceivedError(final WebView view, final int errorCode, final String description, final String failingUrl) {
            Log.d("WebView","onReceivedError");
            super.onReceivedError(view, errorCode, description, failingUrl);
            setResult(RESULT_CANCELED);
            finish();
        }

        @TargetApi(android.os.Build.VERSION_CODES.M)
        @Override
        public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError err) {
            onReceivedError(view, err.getErrorCode(), err.getDescription().toString(), req.getUrl().toString());
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private class GetAccessTokenTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(final String... params) {
            Log.d("WebView","GetAccessTokenTask");
            try {
                OAuth2.retriveInitialAccessToken(params[0], params[1], WebLoginActivity.this);
                Log.d("WebView","retriveInitialAccessToken");
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
                DialogUtitities.showToast(context, result);
                setResult(RESULT_CANCELED);
            } else {
                setResult(RESULT_OK);
            }

            finish();
        }

    }
}
