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
import no.digipost.android.model.Receipt;
import no.digipost.android.utilities.ApplicationUtilities;
import no.digipost.android.utilities.DataFormatUtilities;
import no.digipost.android.utilities.DialogUtitities;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;

public class HtmlAndReceiptActivity extends Activity {

	private WebView webView;
	private int content_type;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_html_and_receipt);
		ApplicationUtilities.setScreenRotationFromPreferences(this);

		content_type = getIntent().getIntExtra(ContentFragment.INTENT_CONTENT, 0);

		setupWebView();
		setupActionBar();

		loadContent();
	}

	private void loadContent() {
		String html = "";
		if (content_type == ApplicationConstants.RECEIPTS) {
			html = getIntent().getStringExtra(ApiConstants.GET_RECEIPT);
		} else {
			try {
				html = new String(DocumentContentStore.documentContent, ApplicationConstants.ENCODING);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		webView.loadDataWithBaseURL(null, html, ApplicationConstants.MIME, ApplicationConstants.ENCODING, null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_html_actionbar, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem toArchive = menu.findItem(R.id.htmlmenu_archive);
		MenuItem toWorkarea = menu.findItem(R.id.htmlmenu_workarea);

		if (content_type == ApplicationConstants.WORKAREA) {
			toArchive.setVisible(true);
		} else if (content_type == ApplicationConstants.ARCHIVE) {
			toWorkarea.setVisible(true);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		case R.id.htmlmenu_delete:
			promptAction(getString(R.string.dialog_prompt_delete_document), ApiConstants.DELETE);
			return true;
		case R.id.htmlmenu_archive:
			promptAction(getString(R.string.dialog_prompt_document_toArchive), ApiConstants.LOCATION_ARCHIVE);
			return true;
		case R.id.htmlmenu_workarea:
			promptAction(getString(R.string.dialog_prompt_document_toWorkarea), ApiConstants.LOCATION_WORKAREA);
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
		getActionBar().setHomeButtonEnabled(true);

		if (content_type != ApplicationConstants.RECEIPTS) {
			Attachment documentMeta = DocumentContentStore.documentMeta;
			getActionBar().setTitle(documentMeta.getSubject());
			getActionBar().setSubtitle(DocumentContentStore.documentParent.getCreatorName());
		} else {
			Receipt receiptMeta = DocumentContentStore.documentReceipt;
			getActionBar().setTitle(receiptMeta.getStoreName());
			getActionBar().setSubtitle(DataFormatUtilities.getFormattedDateTime(receiptMeta.getTimeOfPurchase()));
		}
	}

	private void promptAction(String message, final String action) {

		if (content_type == ApplicationConstants.RECEIPTS)
			message = getString(R.string.dialog_prompt_delete_receipt);

		String positiveButton = getString(R.string.yes);
		if (action.equals(ApiConstants.DELETE)) {
			positiveButton = getString(R.string.delete);
		} else if (action.equals(ApiConstants.LOCATION_ARCHIVE) || action.equals(ApiConstants.LOCATION_WORKAREA)) {
			positiveButton = getString(R.string.move);
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
		i.putExtra(ApiConstants.ACTION, action);
		i.putExtra(ContentFragment.INTENT_CONTENT, content_type);
		setResult(RESULT_OK, i);
		finish();
	}
}
