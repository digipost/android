/**
 * Copyright (C) Posten Norge AS
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.digipost.android.gui.metadata;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.*;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.webkit.*;
import android.widget.ProgressBar;
import no.digipost.android.R;
import no.digipost.android.utilities.DialogUtitities;
import no.digipost.android.utilities.FileUtilities;
import no.digipost.android.utilities.Permissions;

import java.net.MalformedURLException;
import java.net.URL;

import static java.lang.String.format;

public class ExternalLinkWebview extends AppCompatActivity {

    private WebView webView;
    private String fileName;
    private String fileUrl;
    private BroadcastReceiver onComplete;
    private ProgressBar progressSpinner;


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

        progressSpinner = (ProgressBar) findViewById(R.id.externallink_spinner);
        webView = (WebView) findViewById(R.id.externallink_webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        Bundle bundle = getIntent().getExtras();
        fileUrl = bundle.getString("url", "https://www.digipost.no");

        enableCookies(webView);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressSpinner.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);

                if (actionBar != null) {
                    actionBar.setTitle(getShortUrl(view.getUrl()));
                }
            }

        });

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String content, String mimeType, long contentLength) {
                fileName = URLUtil.guessFileName(url, content, mimeType);
                fileUrl = url;
                onComplete = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        showDownloadSuccessDialog(context);
                    }
                };

                registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                if (!mimeType.equals("text/html")) {
                    if (FileUtilities.isStorageWriteAllowed(getApplicationContext())) {
                        showDownloadDialog(userAgent, content, mimeType, contentLength);
                    } else {
                        showMissingPermissionsDialog();
                    }
                }
            }
        });

        if (FileUtilities.isStorageWriteAllowed(this)) {
            webView.loadUrl(fileUrl);
        } else {
            showPermissionsDialog();
        }
    }

    private String getShortUrl(final String longUrl) {
        try {
            return new URL(longUrl).getHost();
        }catch (MalformedURLException e){
            return longUrl;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (onComplete != null) {
            unregisterReceiver(onComplete);
        }
    }

    private void showDownloadDialog(final String userAgent, final String content, final String mimeType, final long contentLength) {
        String hostUrl = "";
        try {
            hostUrl = new URL(fileUrl).getHost();
        } catch (MalformedURLException e) {
            hostUrl = fileUrl;
        }

        String title = getString(R.string.externallink_download_title);
        String message = format(getString(R.string.externallink_download_message), fileName, hostUrl);

        AlertDialog.Builder builder = DialogUtitities.getAlertDialogBuilderWithMessageAndTitle(this, message, title);
        builder.setPositiveButton(getString(R.string.externallink_download_title), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                ExternalDownloadManager dm = new ExternalDownloadManager(getApplicationContext());
                dm.downloadFile(fileName, fileUrl, userAgent, content, mimeType, contentLength);
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
        final Activity activity = this;
        AlertDialog.Builder builder = DialogUtitities.getAlertDialogBuilderWithMessage(activity, message);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                Permissions.requestWritePermissionsIfMissing(activity, activity);
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
        webView.loadUrl(fileUrl);
    }

    private void showMissingPermissionsDialog() {
        String message = getString(R.string.externallink_permissions);
        AlertDialog.Builder builder = DialogUtitities.getAlertDialogBuilderWithMessage(this, message);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.dismiss();
                finish();
            }
        });
        builder.create().show();
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

    private void enableCookies(WebView webView) {
        CookieManager.getInstance().setAcceptCookie(true);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_externallink_actionbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void openInExternalBrowser() {
        String url = this.webView.getUrl();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.externallink_actionbar_open_action) {
            openInExternalBrowser();
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }
}