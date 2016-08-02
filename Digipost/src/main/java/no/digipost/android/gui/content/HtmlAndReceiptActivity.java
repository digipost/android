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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import com.google.android.gms.analytics.GoogleAnalytics;
import no.digipost.android.DigipostApplication;
import no.digipost.android.R;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.documentstore.DocumentContentStore;
import no.digipost.android.gui.MainContentActivity;
import no.digipost.android.gui.fragments.ContentFragment;
import no.digipost.android.model.Attachment;
import no.digipost.android.model.Receipt;
import no.digipost.android.utilities.DataFormatUtilities;
import no.digipost.android.utilities.DialogUtitities;

public class HtmlAndReceiptActivity extends DisplayContentActivity {

    private WebView webView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((DigipostApplication) getApplication()).getTracker(DigipostApplication.TrackerName.APP_TRACKER);
        setContentView(R.layout.activity_html_and_receipt);
        Toolbar toolbar = (Toolbar) findViewById(R.id.html_toolbar);
        setSupportActionBar(toolbar);
        setupWebView();
        setupActionBar();
        loadContent();
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
        MenuItem move = menu.findItem(R.id.htmlmenu_move);

        if (content_type != ApplicationConstants.RECEIPTS) {
            move.setVisible(true);
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
                deleteAction(getString(R.string.dialog_prompt_delete_document), ApiConstants.DELETE);
                return true;
            case R.id.htmlmenu_move:
                showMoveToFolderDialog();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupWebView() {
        webView = (WebView) findViewById(R.id.web_html);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setLoadWithOverviewMode(true);
    }

    private void setupActionBar() {
        try {
            if (content_type != ApplicationConstants.RECEIPTS) {
                Attachment documentMeta = DocumentContentStore.getDocumentAttachment();
                getSupportActionBar().setTitle(documentMeta.getSubject());
                getSupportActionBar().setSubtitle(DocumentContentStore.getDocumentParent().getCreatorName());
            } else {
                Receipt receiptMeta = DocumentContentStore.getDocumentReceipt();
                getSupportActionBar().setTitle(receiptMeta.getStoreName());
                getSupportActionBar().setSubtitle(DataFormatUtilities.getFormattedDateTime(receiptMeta.getTimeOfPurchase()));
            }
        } catch (NullPointerException e) {
            //IGNORE
        }
    }

    private void deleteAction(String message, final String action) {

        if (content_type == ApplicationConstants.RECEIPTS)
            message = getString(R.string.dialog_prompt_delete_receipt);

        String positiveButton = getString(R.string.yes);
        if (action.equals(ApiConstants.DELETE)) {
            positiveButton = getString(R.string.delete);
        } else if (action.equals(ApiConstants.MOVE)) {
            showMoveToFolderDialog();
        }

        AlertDialog.Builder builder = DialogUtitities.getAlertDialogBuilderWithMessage(this, message);
        builder.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                executeAction(action);
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(R.string.abort, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.create().show();
    }

    private void executeAction(String action) {
        Intent i = new Intent(HtmlAndReceiptActivity.this, MainContentActivity.class);
        i.putExtra(ApiConstants.FRAGMENT_ACTIVITY_RESULT_ACTION, action);
        setResult(RESULT_OK, i);
        finish();
    }
}
