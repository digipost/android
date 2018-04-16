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

package no.digipost.android.gui.content;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.google.android.gms.analytics.GoogleAnalytics;
import no.digipost.android.DigipostApplication;
import no.digipost.android.R;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.documentstore.DocumentContentStore;
import no.digipost.android.gui.fragments.ContentFragment;
import no.digipost.android.gui.metadata.ExternalLinkWebview;
import no.digipost.android.model.Attachment;
import no.digipost.android.model.Receipt;
import no.digipost.android.utilities.FormatUtilities;
import java.net.URL;

public class HtmlAndReceiptActivity extends DisplayContentActivity {

    private WebView webView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((DigipostApplication) getApplication()).getTracker(DigipostApplication.TrackerName.APP_TRACKER);
        setContentView(R.layout.activity_html_and_receipt);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInformationDialog();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setupWebView();
        setupActionBar();
        loadContent();
        if (super.shouldShowInvoiceOptionsDialog(this)) {
            super.showInvoiceOptionsDialog(this);
        }
        super.setupMetadataView();
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

    private void setupWebView() {
        webView = (WebView) findViewById(R.id.web_html);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                overrideUrlLoading(url);
                return content_type != ApplicationConstants.RECEIPTS || super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    overrideUrlLoading(request.getUrl().toString());
                }
                return content_type != ApplicationConstants.RECEIPTS || super.shouldOverrideUrlLoading(view, request);
            }
        });
    }

    private void overrideUrlLoading(String url) {
        if(url != null) {
            openExternalLink(url);
            this.webView.stopLoading();
        }
    }

    private void openExternalLink(final String url) {
        try {
            String scheme = new URL(url).toURI().getScheme();

            if (scheme.equals("https")) {
                Intent intent = new Intent(getApplicationContext(), ExternalLinkWebview.class);
                intent.putExtra("url", url);
                startActivity(intent);
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            }
        }catch (Exception e){
            //Ignore
        }
    }

    private void setupActionBar() {
        try {
            if(getSupportActionBar() != null) {
                if (content_type != ApplicationConstants.RECEIPTS) {
                    Attachment documentMeta = DocumentContentStore.getDocumentAttachment();
                    setActionBar(documentMeta.getSubject());
                } else {
                    Receipt receiptMeta = DocumentContentStore.getDocumentReceipt();
                    getSupportActionBar().setTitle(receiptMeta.getStoreName());
                    getSupportActionBar().setSubtitle(FormatUtilities.getFormattedDateTime(receiptMeta.getTimeOfPurchase()));
                }
            }
        } catch (NullPointerException e) {
            //IGNORE
        }
    }

    private void loadContent() {
        String html = "";
        if (content_type == ApplicationConstants.RECEIPTS) {
            html = getIntent().getStringExtra(ApiConstants.GET_RECEIPT);
        } else {
            try {
                html = new String(DocumentContentStore.getDocumentContent(), ApiConstants.ENCODING);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        webView.loadDataWithBaseURL(null, html, ApiConstants.MIME, ApiConstants.ENCODING, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_html_actionbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        sendToBank = menu.findItem(R.id.htmlmenu_send_to_bank);

        if (content_type != ApplicationConstants.RECEIPTS) {
            menu.findItem(R.id.htmlmenu_move).setVisible(true);
        }else{
            menu.findItem(R.id.htmlmenu_info).setVisible(false);
        }

        boolean sendToBankVisible = getIntent().getBooleanExtra(ContentFragment.INTENT_SEND_TO_BANK, false);

        if (ApplicationConstants.FEATURE_SEND_TO_BANK_VISIBLE) {
            super.setSendToBankMenuText(sendToBankVisible);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.htmlmenu_send_to_bank:
                super.openInvoiceTask();
                return true;
            case R.id.htmlmenu_delete:
                deleteAction(this);
                return true;
            case R.id.htmlmenu_move:
                showMoveToFolderDialog();
                return true;
            case R.id.htmlmenu_info:
                showInformationDialog();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
