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

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.*;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
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
import no.digipost.android.utilities.FileUtilities;
import no.digipost.android.utilities.Permissions;
import java.net.MalformedURLException;
import java.net.URL;

import static java.lang.String.format;

public class ExternalLinkWebview extends AppCompatActivity{

    private WebView webView;
    private String fileName;
    private String fileUrl;
    private Activity activity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_externallink_webview);
        this.activity = activity;
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

        webView = (WebView) findViewById(R.id.externallink_webview);
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
                fileName = URLUtil.guessFileName(url, content,mimeType);
                fileUrl = url;

                BroadcastReceiver onComplete = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        showDownloadSuccessDialog(context);
                    }
                };
                registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                if (!mimeType.equals("text/html")) {
                    showDownloadDialog();
                }
            }
        });

        if(FileUtilities.isStorageWriteAllowed(this)){
            webView.loadUrl("https://www.sundar.no/testbrev.pdf");
        }else{
            showPermissionsDialog();
        }
    }

    private void showDownloadDialog() {
        String hostUrl = "";
        try {
            hostUrl = new URL(fileUrl).getHost();
        }catch (MalformedURLException e){
            hostUrl = fileUrl;
        }

        String title = getString(R.string.externallink_download_title);
        String message = format(getString(R.string.externallink_download_message), fileName, hostUrl);

        AlertDialog.Builder builder = DialogUtitities.getAlertDialogBuilderWithMessageAndTitle(this, message, title);
        builder.setPositiveButton(getString(R.string.externallink_download_title), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                downloadFile(fileUrl);
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

    private void showPermissionsDialog() {
        String message = getString(R.string.externallink_permissions);
        AlertDialog.Builder builder = DialogUtitities.getAlertDialogBuilderWithMessage(this, message);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                Permissions.requestWritePermissionsIfMissing(getBaseContext(), activity);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(FileUtilities.isStorageWriteAllowed(this)){
            webView.loadUrl("https://www.digipost.no/testbrev.pdf");
        }
    }

    private void downloadFile(final String url) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        String idPortenSession = CookieManager.getInstance().getCookie("https://idporten.difi.no");
        if (idPortenSession != null) request.addRequestHeader("Cookie", idPortenSession);
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, url);

        DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        dm.enqueue(request);
    }

    private void showDownloadSuccessDialog(final Context downloadContext) {
        String message = format(getString(R.string.externallink_download_success_message), fileName);
        AlertDialog.Builder builder = DialogUtitities.getAlertDialogBuilderWithMessage(downloadContext, message);
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