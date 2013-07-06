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
import no.digipost.android.documentstore.ImageStore;
import no.digipost.android.documentstore.UnsupportedFileStore;
import no.digipost.android.utilities.FileUtilities;

public class UnsupportedDocumentFormatActivity extends Activity {
    private ImageButton toArchive;
    private ImageButton toWorkarea;
    private ImageButton delete;
    private ImageButton digipostIcon;
    private ImageButton optionsButton;
    private Button open;

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unsupported);

        intent = getIntent();

        createButtons();
        createUI();
    }

    private void createUI() {

        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (UnsupportedFileStore.unsupportedDocument != null) {
                    openFileWithIntent(UnsupportedFileStore.unsupportedDocumentFileType, UnsupportedFileStore.unsupportedDocument);
                } else {
                    showMessage("Kunne ikke åpne dokumentet");
                    finish();
                }
            }
        });
        final String toolbarType = intent.getExtras().getString(ApiConstants.LOCATION_FROM);

        if (toolbarType.equals(ApiConstants.LOCATION_WORKAREA)) {
            makeWorkareaToolbar();
        } else if (toolbarType.equals(ApiConstants.LOCATION_ARCHIVE)) {
            makeArchiveToolbar();
        }

        optionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMenu(view);
            }
        });

        digipostIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        toArchive.setOnClickListener(new View.OnClickListener() {

            public void onClick(final View v) {
                String message = "";
                if (toolbarType.equals(ApiConstants.LOCATION_INBOX)) {
                    message = getString(R.string.dialog_prompt_letter_toArchive);
                } else {
                    message = getString(R.string.dialog_prompt_document_toArchive);
                }

                showWarning(message, ApiConstants.LOCATION_ARCHIVE);
            }
        });

        toWorkarea.setOnClickListener(new View.OnClickListener() {

            public void onClick(final View v) {
                String message = "";
                if (toolbarType.equals(ApiConstants.LOCATION_INBOX)) {
                    message = getString(R.string.dialog_prompt_letter_toWorkarea);
                } else {
                    message = getString(R.string.dialog_prompt_document_toWorkarea);
                }
                showWarning(message, ApiConstants.LOCATION_WORKAREA);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {

            public void onClick(final View v) {
                String message = "";

                if (toolbarType.equals(ApiConstants.LOCATION_INBOX)) {
                    message = getString(R.string.dialog_prompt_delete_letter);
                } else {
                    message = getString(R.string.dialog_prompt_delete_document);
                }
                showWarning(message, ApiConstants.DELETE);
            }
        });
    }

    private void createButtons() {
        toArchive = (ImageButton) findViewById(R.id.unsupported_toArchive);
        toWorkarea = (ImageButton) findViewById(R.id.unsupported_toWorkarea);
        delete = (ImageButton) findViewById(R.id.unsupported_delete);
        digipostIcon = (ImageButton) findViewById(R.id.unsupported_digipost_icon);
        open = (Button) findViewById(R.id.unsupported_openBtn);
        optionsButton = (ImageButton) findViewById(R.id.unsupported_optionsbtn);
    }

    private void makeArchiveToolbar() {
        toArchive.setVisibility(View.GONE);
    }

    private void makeWorkareaToolbar() {
        toWorkarea.setVisibility(View.GONE);
    }

    private void openFileWithIntent(final String documentFileType, final byte[] data) {
        if (data == null) {
            showMessage(getString(R.string.error_failed_to_open_with_intent));
            finish();
        }

        try {
            FileUtilities.openFileWithIntent(this, documentFileType, data);
        } catch (IOException e) {
            showMessage(getString(R.string.error_failed_to_open_with_intent));
        } catch (ActivityNotFoundException e) {
            showMessage(getString(R.string.error_no_activity_to_open_file));
        }
    }

    private void showMessage(final String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void showWarning(final String text, final String action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(text).setPositiveButton("Ja", new DialogInterface.OnClickListener() {
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

    private void singleLetterOperation(final String action) {
        Intent i = new Intent(UnsupportedDocumentFormatActivity.this, BaseFragmentActivity.class);
        i.putExtra(ApiConstants.LOCATION_FROM, intent.getExtras().getString(ApiConstants.LOCATION_FROM));
        i.putExtra(ApiConstants.ACTION, action);
        i.putExtra(ApiConstants.DOCUMENT_TYPE, ApiConstants.LETTER);
        setResult(RESULT_OK, i);
        finish();
    }

    public void showMenu(final View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            public boolean onMenuItemClick(final MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.showdocumentmenu_open_external:
                        openFileWithIntent(UnsupportedFileStore.unsupportedDocumentFileType, UnsupportedFileStore.unsupportedDocument);
                        return true;
                    case R.id.showdocumentmenu_save:
                        promtSaveToSD();
                        return true;
                }
                return false;
            }
        });
        popup.inflate(R.menu.activity_showdocument_general);
        popup.show();
    }

    private void promtSaveToSD() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.pdf_promt_save_to_sd).setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                new SaveDocumentToSDTask().execute();
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

    private class SaveDocumentToSDTask extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog progressDialog;

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
                file = FileUtilities.writeFileToSD(UnsupportedFileStore.unsupportedDocumentFileName, UnsupportedFileStore.unsupportedDocumentFileType, UnsupportedFileStore.unsupportedDocument);
            } catch (IOException e) {
                return false;
            }

            FileUtilities.makeFileVisible(UnsupportedDocumentFormatActivity.this, file);

            return true;
        }

        @Override
        protected void onPostExecute(Boolean saved) {
            super.onPostExecute(saved);

            if (saved) {
                showMessage(getString(R.string.pdf_saved_to_sd));
            } else {
                showMessage(getString(R.string.pdf_save_to_sd_failed));
            }

            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isFinishing()) {
            UnsupportedFileStore.unsupportedDocument = null;
            UnsupportedFileStore.unsupportedDocumentFileType = null;
            UnsupportedFileStore.unsupportedDocumentFileName = null;
        } else {
            FileUtilities.deleteTempFiles();
        }
    }
}
