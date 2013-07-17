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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import no.digipost.android.R;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.documentstore.DocumentContentStore;
import no.digipost.android.documentstore.ImageStore;
import no.digipost.android.documentstore.UnsupportedFileStore;
import no.digipost.android.gui.fragments.ContentFragment;
import no.digipost.android.model.Attachment;
import no.digipost.android.utilities.ApplicationUtilities;
import no.digipost.android.utilities.DialogUtitities;
import no.digipost.android.utilities.FileUtilities;

public class UnsupportedDocumentFormatActivity extends Activity {
    private Attachment documentMeta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unsupported);
        ApplicationUtilities.setScreenRotationFromPreferences(this);

        documentMeta = DocumentContentStore.documentMeta;

        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setTitle(DocumentContentStore.documentMeta.getSubject());
        getActionBar().setSubtitle(DocumentContentStore.documentParent.getCreatorName());

        TextView message = (TextView) findViewById(R.id.unsupported_message);
        message.setText(getString(R.string.unsupported_message_top) + " (." + DocumentContentStore.documentMeta.getFileType() + ").");

        Button open = (Button) findViewById(R.id.unsupported_open_button);
        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileWithIntent(DocumentContentStore.documentMeta.getFileType(), DocumentContentStore.documentContent);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_image_html_unsupported_actionbar, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem toArchive = menu.findItem(R.id.menu_image_html_unsupported_archive);
        MenuItem toWorkarea = menu.findItem(R.id.menu_image_html_unsupported_workarea);

        int content = getIntent().getIntExtra(ContentFragment.INTENT_CONTENT, 0);

        if (content == ApplicationConstants.WORKAREA) {
            toWorkarea.setVisible(false);
        } else if (content == ApplicationConstants.ARCHIVE) {
            toArchive.setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
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
                openFileWithIntent(DocumentContentStore.documentMeta.getFileType(), DocumentContentStore.documentContent);
                return true;
            case R.id.menu_image_html_unsupported_save:
                promtSaveToSD();
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

    private void openFileWithIntent(final String documentFileType, final byte[] data) {
        if (data == null) {
            DialogUtitities.showToast(this, getString(R.string.error_failed_to_open_with_intent));
            finish();
        }

        try {
            FileUtilities.openFileWithIntent(this, documentFileType, data);
        } catch (ActivityNotFoundException e) {
            DialogUtitities.showToast(this, getString(R.string.error_no_activity_to_open_file));
        } catch (Exception e) {
            DialogUtitities.showToast(this, getString(R.string.error_failed_to_open_with_intent));
        }
    }

    private void promtSaveToSD() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.pdf_promt_save_to_sd).setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.dismiss();
                new SaveDocumentToSDTask().execute();
            }
        }).setCancelable(false).setNegativeButton(getString(R.string.abort), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private class SaveDocumentToSDTask extends AsyncTask<Void, Void, Boolean> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(UnsupportedDocumentFormatActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getString(R.string.loading_content));
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... parameters) {
            File file = null;

            try {
                file = FileUtilities.writeFileToSD(documentMeta.getSubject(), documentMeta.getFileType(), DocumentContentStore.documentContent);
            } catch (Exception e) {
                return false;
            }

            FileUtilities.makeFileVisible(UnsupportedDocumentFormatActivity.this, file);

            return true;
        }

        @Override
        protected void onPostExecute(Boolean saved) {
            super.onPostExecute(saved);
            progressDialog.dismiss();

            if (saved) {
                DialogUtitities.showToast(UnsupportedDocumentFormatActivity.this, getString(R.string.pdf_saved_to_sd));
            } else {
                DialogUtitities.showToast(UnsupportedDocumentFormatActivity.this, getString(R.string.pdf_save_to_sd_failed));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isFinishing()) {
            FileUtilities.deleteTempFiles();
        }
    }
}
