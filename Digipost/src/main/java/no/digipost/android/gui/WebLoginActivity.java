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
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.*;
import android.webkit.WebSettings.LayoutAlgorithm;
import com.google.android.gms.analytics.GoogleAnalytics;
import no.digipost.android.DigipostApplication;
import no.digipost.android.R;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.authentication.Auth;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.utilities.DialogUtitities;
import no.digipost.android.utilities.NetworkUtilities;

public class WebLoginActivity extends Activity {

    private final String SUCCESS = "SUCCESS";
    private String authenticationScope = ApiConstants.SCOPE_FULL;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((DigipostApplication) getApplication()).getTracker(DigipostApplication.TrackerName.APP_TRACKER);
        setContentView(R.layout.fragment_login_webview);
        authenticationScope = getIntent().getExtras().getString("authScope");

        if (!NetworkUtilities.isOnline()) {
            DialogUtitities.showToast(getApplicationContext(), getString(R.string.error_your_network));
            finish();
        }

        WebView webView = (WebView) findViewById(R.id.login_webview);
        String url = Auth.getAuthorizeURL(authenticationScope);
        webView.loadUrl(url);
        Log.d("WebView", "authenticationScope: "+authenticationScope);
        Log.d("WebView", "Loading url: "+ url);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        webView.getSettings().setDomStorageEnabled(true);

        webView.setWebViewClient(new MyWebViewClient());

        getActionBar().setTitle(R.string.login_loginbutton_text);
        getActionBar().setHomeButtonEnabled(true);
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
                new GetTokenTask().execute(state, code);
                return true;
            }

            return false;
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError err) {
            super.onReceivedError(view,req,err);
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private class GetTokenTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(final String... params) {
            try {
                Auth.retrieveToken(params[0], params[1], WebLoginActivity.this, authenticationScope);
                return SUCCESS;
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
            if(SUCCESS.equals(result)) {
                setResult(RESULT_OK);
            }else{
                DialogUtitities.showToast(getApplicationContext(), result);
                setResult(RESULT_CANCELED);
            }
            finish();
        }
    }
}
