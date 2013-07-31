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

import no.digipost.android.R;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.documentstore.DocumentContentStore;
import no.digipost.android.gui.MainContentActivity;
import no.digipost.android.gui.fragments.ContentFragment;
import no.digipost.android.utilities.ApplicationUtilities;
import no.digipost.android.utilities.DialogUtitities;
import no.digipost.android.utilities.FileUtilities;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;

public class UnsupportedDocumentFormatActivity extends DisplayContentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_unsupported);
		ApplicationUtilities.setScreenRotationFromPreferences(this);

		if (DocumentContentStore.getDocumentContent() == null) {
			DialogUtitities.showToast(this, "En feil oppstod under åpning av dokumentet.");
			finish();
		}

		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setTitle(DocumentContentStore.getDocumentAttachment().getSubject());
		getActionBar().setSubtitle(DocumentContentStore.getDocumentParent().getCreatorName());

		TextView message = (TextView) findViewById(R.id.unsupported_message);
		message.setText(getString(R.string.unsupported_message_top) + " (." + DocumentContentStore.getDocumentAttachment().getFileType() + ").");

		Button open = (Button) findViewById(R.id.unsupported_open_button);
		open.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				UnsupportedDocumentFormatActivity.super.openFileWithIntent();
			}
		});

		Button cancel = (Button) findViewById(R.id.unsupported_cancel_button);
		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_image_html_unsupported_actionbar, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem toArchive = menu.findItem(R.id.menu_image_html_unsupported_archive);
		MenuItem toWorkarea = menu.findItem(R.id.menu_image_html_unsupported_workarea);
        MenuItem sendToBank = menu.findItem(R.id.menu_image_html_unsupported_send_to_bank);

		int content = getIntent().getIntExtra(ContentFragment.INTENT_CONTENT, 0);

		if (content == ApplicationConstants.WORKAREA) {
			toWorkarea.setVisible(false);
		} else if (content == ApplicationConstants.ARCHIVE) {
			toArchive.setVisible(false);
		}

        boolean sendToBankVisible = getIntent().getBooleanExtra(ContentFragment.INTENT_SEND_TO_BANK, false);

        if (sendToBankVisible) {
            sendToBank.setVisible(true);
        } else {
            sendToBank.setVisible(false);
        }

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
        case R.id.menu_image_html_unsupported_send_to_bank:
            super.openInvoiceTask();
            return true;
		case R.id.menu_image_html_unsupported_delete:
			promtAction(getString(R.string.dialog_prompt_delete_document), ApiConstants.DELETE);
			return true;
		case R.id.menu_image_html_unsupported_archive:
			promtAction(getString(R.string.dialog_prompt_document_toArchive), ApiConstants.LOCATION_ARCHIVE);
			return true;
		case R.id.menu_image_html_unsupported_workarea:
			promtAction(getString(R.string.dialog_prompt_document_toWorkarea), ApiConstants.LOCATION_WORKAREA);
			return true;
		case R.id.menu_image_html_unsupported_open_external:
			super.openFileWithIntent();
			return true;
		case R.id.menu_image_html_unsupported_save:
			super.promtSaveToSD();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void executeAction(String action) {
		Intent i = new Intent(UnsupportedDocumentFormatActivity.this, MainContentActivity.class);
		i.putExtra(ApiConstants.ACTION, action);

		setResult(RESULT_OK, i);
		finish();
	}

	private void promtAction(final String message, final String action) {
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

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (isFinishing()) {
			FileUtilities.deleteTempFiles();
		}
	}
}
