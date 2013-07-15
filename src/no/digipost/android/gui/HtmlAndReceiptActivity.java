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

package no.digipost.android.gui;

import no.digipost.android.R;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.documentstore.DocumentContentStore;
import no.digipost.android.gui.fragments.ContentFragment;
import no.digipost.android.model.Attachment;
import no.digipost.android.utilities.DialogUtitities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

public class HtmlAndReceiptActivity extends Activity {

    private WebView webView;

    private String from;
    private int content;
    private Attachment documentMeta;
    private AlertDialog mAlertDialog;
    private AlertDialog.Builder mAlertBuilder;

    private ActionMode.Callback selectActionModeCallback;
    private ActionMode selectActionMode;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_html_and_receipt);

        documentMeta = DocumentContentStore.documentMeta;

        getActionBar().setTitle(documentMeta.getSubject());
        getActionBar().setSubtitle(DocumentContentStore.documentParent.getCreatorName());
        getActionBar().setHomeButtonEnabled(true);

        selectActionModeCallback = new SelectActionModeCallback();

        mAlertBuilder = new AlertDialog.Builder(this);


        String html = getIntent().getExtras().getString(ApiConstants.FILETYPE_HTML);
        String mime = "text/html";
        String encoding = "utf-8";

        try{
        html = new String(DocumentContentStore.documentContent, "UTF-8");
        }catch(Exception e){
            e.printStackTrace();
        }
        content = getIntent().getIntExtra(ContentFragment.INTENT_CONTENT, 0);

        webView = (WebView) findViewById(R.id.web_html);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.loadDataWithBaseURL(null, html, mime, encoding, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_mupdf_actionbar, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem toArchive = menu.findItem(R.id.pdfmenu_archive);
        MenuItem toWorkarea = menu.findItem(R.id.pdfmenu_workarea);

        int content = getIntent().getIntExtra(ContentFragment.INTENT_CONTENT, 0);

        if (content == ApplicationConstants.WORKAREA) {
            toWorkarea.setVisible(false);
        } else if (content == ApplicationConstants.ARCHIVE) {
            toArchive.setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }


    private class SelectActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.activity_mupdf_context, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.mupdf_context_menu_copy:
                    actionMode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.pdfmenu_delete:
                promptAction(getString(R.string.dialog_prompt_delete_document), ApiConstants.DELETE);
                return true;
            case R.id.pdfmenu_archive:
                promptAction(getString(R.string.dialog_prompt_document_toArchive), ApiConstants.LOCATION_ARCHIVE);
                return true;
            case R.id.pdfmenu_workarea:
                promptAction(getString(R.string.dialog_prompt_document_toWorkarea), ApiConstants.LOCATION_WORKAREA);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void promptAction(final String message, final String action) {
        AlertDialog.Builder builder = DialogUtitities.getAlertDialogBuilderWithMessage(this, message);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
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
        i.putExtra(ApiConstants.ACTION, action);

        setResult(RESULT_OK, i);
        finish();
    }


    private void singleLetterOperation(final String action) {
        Intent i = new Intent(HtmlAndReceiptActivity.this, BaseFragmentActivity.class);
        i.putExtra(ApiConstants.LOCATION_FROM, from);
        i.putExtra(ApiConstants.ACTION, action);
        i.putExtra(ApiConstants.CONTENT_TYPE, content);
        setResult(RESULT_OK, i);
        finish();
    }

    private void showWarning(final String text, final String action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(text).setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                singleLetterOperation(action);
                dialog.dismiss();
            }
        }).setCancelable(false).setNegativeButton(getString(R.string.abort), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
