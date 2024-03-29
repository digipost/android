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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.*;
import android.webkit.WebSettings.LayoutAlgorithm;
import no.digipost.android.DigipostApplication;
import no.digipost.android.R;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.authentication.DigipostOauthScope;
import no.digipost.android.authentication.OAuth;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.utilities.DialogUtitities;
import no.digipost.android.utilities.NetworkUtilities;

public class WebLoginActivity extends AppCompatActivity {

    private final String SUCCESS = "SUCCESS";
    private DigipostOauthScope authenticationScope = DigipostOauthScope.FULL;
    private int currentListPosition = -1;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((DigipostApplication) getApplication()).getTracker(DigipostApplication.TrackerName.APP_TRACKER);
        setContentView(R.layout.fragment_login_webview);
        setAuthenticationScope(getIntent().getExtras());
        checkIfAppIsOnline();
        WebView webView = (WebView) findViewById(R.id.login_webview);
        clearWebViewCacheAndCookies(webView);
        setupWebView(webView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Avbryt");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupWebView(WebView webView){
        String url = OAuth.getAuthorizeURL(authenticationScope);
        enableCookies(webView);
        webView.loadUrl(url);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(false);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(true);
        webView.setWebViewClient(new MyWebViewClient());
    }

    private void setAuthenticationScope(Bundle extras){
        authenticationScope = DigipostOauthScope.fromApiConstant(extras.getString("authenticationScope"));
        if(authenticationScope == DigipostOauthScope.NONE) {
            authenticationScope = DigipostOauthScope.FULL;
        } else if (authenticationScope != DigipostOauthScope.FULL) {
            currentListPosition = extras.getInt("currentListPosition");
        }

    }

    private void checkIfAppIsOnline(){
        if (!NetworkUtilities.isOnline()) {
            DialogUtitities.showToast(getApplicationContext(), getString(R.string.error_your_network));
            finish();
        }
    }

    private void clearWebViewCacheAndCookies(WebView webView){
        webView.clearCache(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else{
            CookieSyncManager cookieSyncMngr=CookieSyncManager.createInstance(getApplicationContext());
            cookieSyncMngr.startSync();
            CookieManager cookieManager=CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }

    private void enableCookies(WebView webView){
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class MyWebViewClient extends WebViewClient {

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
            return handleUrl(url);
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request){
            return handleUrl(request.getUrl().toString());
        }

        private boolean handleUrl(String url){
            if (url.contains("localhost")) {
                new GetTokenTask().execute(url);
                return true;
            }
            return false;
        }
    }

    private class GetTokenTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(final String... params) {
            try {
                String url = params[0];
                String state_fragment = "&" + ApiConstants.STATE + "=";
                int state_start = url.indexOf(state_fragment);
                String code_fragment = "&" + ApiConstants.CODE + "=";

                int code_start = url.indexOf(code_fragment);
                String state = url.substring(state_start + state_fragment.length(), code_start);
                String code = url.substring(code_start + code_fragment.length(), url.length());

                OAuth.retrieveMainAccess(state, code, WebLoginActivity.this, authenticationScope);
                return SUCCESS;
            } catch (DigipostApiException e) {
                e.printStackTrace();
                return e.getMessage();
            } catch (DigipostClientException e) {
                e.printStackTrace();
                return e.getMessage();
            } catch (DigipostAuthenticationException e) {
                e.printStackTrace();
                return e.getMessage();
            }catch (Exception e){
                e.printStackTrace();
                return getString(R.string.error_bad_request);
            }
        }

        @Override
        protected void onPostExecute(final String result) {
            if(SUCCESS.equals(result)) {
                Intent resultIntent = new Intent();
                if(authenticationScope != DigipostOauthScope.FULL && currentListPosition != -1)
                    resultIntent.putExtra("currentListPosition", currentListPosition);
                setResult(RESULT_OK, resultIntent);
            }else{
                DialogUtitities.showToast(getApplicationContext(), result);
                setResult(RESULT_CANCELED);
            }
            finish();
        }
    }
}
