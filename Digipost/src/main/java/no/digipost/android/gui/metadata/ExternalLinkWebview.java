/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.digipost.android.gui.metadata;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.*;
import no.digipost.android.R;
import no.digipost.android.utilities.DialogUtitities;
import no.digipost.android.utilities.Permissions;

public class ExternalLinkWebview extends AppCompatActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_externallink_webview);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setBackgroundDrawable(new ColorDrawable(0xff454545));
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = this.getWindow();
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.metadata_externalbrowser_top_background));
            }
        }

        WebView webView = (WebView) findViewById(R.id.externallink_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        Bundle bundle = getIntent().getExtras();
        String url = bundle.getString("url", "https://www.digipost.no");
        enableCookies(webView);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(actionBar != null) {
                    actionBar.setTitle(view.getTitle());
                    actionBar.setSubtitle(view.getUrl());
                }
            }

        });
        webView.setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart(String url, String userAgent, String content, String mimeType, long contentLength) {
                if (!mimeType.equals("text/html")) {
                    showDownloadDialog(url);
                }
            }
        });
        webView.loadUrl("https://www.digipost.no/testbrev.pdf");
    }

    private void showDownloadDialog(final String url) {
        String title = getString(R.string.externallink_download_title);
        String message = getString(R.string.externallink_download_message);

        AlertDialog.Builder builder = DialogUtitities.getAlertDialogBuilderWithMessageAndTitle(this, message, title);

        builder.setPositiveButton(getString(R.string.externallink_download_title), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                boolean writePermissionsAllowed = Permissions.requestWritePermissionsIfMissing(getApplicationContext(), getParent());
                while(!writePermissionsAllowed) {
                    writePermissionsAllowed = Permissions.requestWritePermissionsIfMissing(getApplicationContext(), getParent());
                }
                downloadFile(url);
                dialog.dismiss();
            }
        }).setCancelable(false).setNegativeButton(getString(R.string.abort), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.cancel();
                finish();
            }
        });

        builder.create().show();
    }

    private void downloadFile(final String url) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        String IDPORTEN = "https://idporten.difi.no";
        String idPortenSession = CookieManager.getInstance().getCookie(IDPORTEN);
        if (idPortenSession != null) request.addRequestHeader(IDPORTEN, idPortenSession);
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, url);

        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        dm.enqueue(request);
    }

    private void showDownloadSuccessDialog() {
        String message = "";
        String title = "";
        AlertDialog.Builder builder = DialogUtitities.getAlertDialogBuilderWithMessageAndTitle(getApplicationContext(),message,title);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.dismiss();
                finish();
            }
        });
        builder.create().show();
    }

    private void enableCookies(WebView webView){
        CookieManager.getInstance().setAcceptCookie(true);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }
}
