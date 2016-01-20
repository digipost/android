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

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

import com.google.android.gms.analytics.GoogleAnalytics;
import no.digipost.android.DigipostApplication;
import no.digipost.android.R;
import no.digipost.android.api.ContentOperations;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.documentstore.DocumentContentStore;
import no.digipost.android.gui.adapters.FolderArrayAdapter;
import no.digipost.android.gui.fragments.ContentFragment;
import no.digipost.android.gui.fragments.DocumentFragment;
import no.digipost.android.model.Attachment;
import no.digipost.android.model.CurrentBankAccount;
import no.digipost.android.model.Document;
import no.digipost.android.model.Folder;
import no.digipost.android.model.Invoice;
import no.digipost.android.model.Payment;
import no.digipost.android.utilities.DataFormatUtilities;
import no.digipost.android.utilities.DialogUtitities;
import no.digipost.android.utilities.FileUtilities;

import static java.lang.String.format;
import static java.lang.String.valueOf;

public abstract class DisplayContentActivity extends Activity {

    protected MenuItem sendToBank;
    protected int content_type;
    private final static int REQUEST_WRITE_EXTERNAL_STORAGE = 0;
    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;
    private FolderArrayAdapter folderAdapter;
    private String location;
    private String folderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((DigipostApplication) getApplication()).getTracker(DigipostApplication.TrackerName.APP_TRACKER);
        content_type = getIntent().getIntExtra(ContentFragment.INTENT_CONTENT, 0);
        if(content_type != ApplicationConstants.RECEIPTS) {
            if (DocumentContentStore.getDocumentAttachment() == null || DocumentContentStore.getDocumentParent() == null) {
                DialogUtitities.showToast(this, getString(R.string.error_failed_to_open_document));
                finish();
            }
        }
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

    protected void showContentProgressDialog(final AsyncTask task, String message) {
        progressDialog = DialogUtitities.getProgressDialogWithMessage(this, message);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.abort), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int which) {
                dialog.dismiss();
                task.cancel(true);
            }
        });

        progressDialog.show();
    }

    public void setActionBar(String title, String subTitle) {
        getActionBar().setTitle(title);
        getActionBar().setSubtitle(subTitle);
        getActionBar().setHomeButtonEnabled(true);
    }

    protected void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    protected void setSendToBankMenuText(boolean sendToBankVisible) {

        if (sendToBankVisible) {
            sendToBank.setVisible(true);
            Payment payment = DocumentContentStore.getDocumentAttachment().getInvoice() == null ? null : DocumentContentStore
                    .getDocumentAttachment()
                    .getInvoice()
                    .getPayment();
            if (payment != null) {
                sendToBank.setTitle(getString(R.string.sent_to_bank));
            }
        } else {
            sendToBank.setVisible(false);
        }
    }

    private void openInvoiceContent(Attachment attachment, Document document, CurrentBankAccount account) {

        if (attachment.getInvoice().getPayment() != null) {
            showPaidInvoiceDialog(attachment.getInvoice());
        } else {
            if (attachment.getInvoice().getSendToBank() != null) {
                String accountNumber = account == null ? "***********" : account.getBankAccount().getAccountNumber();
                showSendToBankDialog(attachment, document, accountNumber);
            } else {
                showSendToBankNotEnabledDialog();
            }
        }
    }

    private void showPaidInvoiceDialog(Invoice invoice) {
        String timePaid = DataFormatUtilities.getFormattedDate(invoice.getPayment().getTimePaid());

        String title = getString(R.string.dialog_send_to_bank_paid_title);
        String message = format(getString(R.string.dialog_send_to_bank_paid_message), timePaid);

        AlertDialog.Builder builder = DialogUtitities.getAlertDialogBuilderWithMessageAndTitle(this, message, title);
        builder.setCancelable(false).setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.cancel();
            }
        });

        builder.create().show();

    }

    private void showSendToBankDialog(final Attachment attachment, final Document document, final String accountNumber) {
        String title = getString(R.string.dialog_send_to_bank_not_paid_title);
        String message = getString(R.string.dialog_send_to_bank_not_paid_message_start) + "\n";

        if (accountNumber != null) {
            message += "\n" + String.format(getString(R.string.dialog_send_to_bank_not_paid_message_end), accountNumber);
        }

        AlertDialog.Builder builder = DialogUtitities.getAlertDialogBuilderWithMessageAndTitle(this, message, title);
        builder.setPositiveButton(getString(R.string.dialog_send_to_bank), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                sendToBank(attachment, document);
                dialog.dismiss();
            }
        }).setCancelable(false).setNegativeButton(getString(R.string.abort), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.cancel();
            }
        });

        builder.create().show();
    }

    private void showSendToBankNotEnabledDialog() {
        String title = getString(R.string.dialog_send_to_bank_not_enabled_title);
        String message = getString(R.string.dialog_send_to_bank_not_enabled_message);

        AlertDialog.Builder builder = DialogUtitities.getAlertDialogBuilderWithMessageAndTitle(this, message, title);
        builder.setCancelable(false).setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.cancel();
            }
        });

        builder.create().show();
    }

    private void sendToBank(final Attachment attachment, final Document document) {
        SendToBankTask task = new SendToBankTask(attachment, document);
        task.execute();
    }

    protected void openInvoiceTask() {
        OpenInvoiceTask task = new OpenInvoiceTask(DocumentContentStore.getDocumentAttachment(), DocumentContentStore.getDocumentParent());
        task.execute();
    }

    private void finishActivityWithAction(String action) {
        Intent intent = new Intent();
        intent.putExtra(ApiConstants.FRAGMENT_ACTIVITY_RESULT_ACTION, action);
        intent.putExtra(ContentFragment.INTENT_CONTENT, content_type);

        if (action.equals(ApiConstants.MOVE)) {
            intent.putExtra(ApiConstants.FRAGMENT_ACTIVITY_RESULT_LOCATION, location);
            intent.putExtra(ApiConstants.FRAGMENT_ACTIVITY_RESULT_FOLDERID, folderId);
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    protected void showMoveToFolderDialog() {
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.attachmentdialog_layout, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this).setNegativeButton(getString(R.string.close),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );

        builder.setView(view);

        ListView moveToFolderListView = (ListView) view.findViewById(R.id.attachmentdialog_listview);

        ArrayList<Folder> folders = DocumentContentStore.getMoveFolders();
        folderAdapter = new FolderArrayAdapter(this, R.layout.attachmentdialog_list_item, folders);
        moveToFolderListView.setAdapter(folderAdapter);
        moveToFolderListView.setOnItemClickListener(new MoveToFolderListOnItemClickListener());

        builder.setTitle(R.string.move_to);
        alertDialog = builder.create();
        alertDialog.show();
    }

    protected void openFileWithIntent() {

        if (DocumentContentStore.getDocumentContent() == null) {
            DialogUtitities.showToast(this, getString(R.string.error_failed_to_open_with_intent));
            finish();
        }

        try {
            FileUtilities.openFileWithIntent(this, DocumentContentStore.getDocumentAttachment().getFileType(),
                    DocumentContentStore.getDocumentContent());
        } catch (ActivityNotFoundException e) {
            DialogUtitities.showToast(this, getString(R.string.error_no_activity_to_open_file));
        } catch (Exception e) {
            DialogUtitities.showToast(this, getString(R.string.error_failed_to_open_with_intent));
        }
    }

    protected void downloadFile() {
        requestWritePermissionsIfMissing();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.pdf_promt_save_to_sd).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {

                new DownloadDocumentTask().execute();
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

    protected class SendToBankTask extends AsyncTask<Void, Void, Boolean> {
        private String errorMessage;
        private Document document;
        private Attachment attachment;
        private boolean invalidToken;

        public SendToBankTask(final Attachment attachment, final Document document) {
            this.document = document;
            this.attachment = attachment;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showContentProgressDialog(this, DisplayContentActivity.this.getString(R.string.sending_to_bank));
        }

        @Override
        protected Boolean doInBackground(final Void... params) {
            try {
                ContentOperations.sendToBank(DisplayContentActivity.this, attachment);
                DocumentContentStore.setDocumentParent(ContentOperations.getDocumentSelf(DisplayContentActivity.this, document));
                return true;
            } catch (DigipostApiException e) {
                errorMessage = e.getMessage();
                return false;
            } catch (DigipostClientException e) {
                errorMessage = e.getMessage();
                return false;
            } catch (DigipostAuthenticationException e) {
                invalidToken = true;
                errorMessage = e.getMessage();
                return false;
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            hideProgressDialog();
        }

        @Override
        protected void onPostExecute(final Boolean result) {

            hideProgressDialog();
            if (result) {
                DialogUtitities.showToast(DisplayContentActivity.this,
                        DisplayContentActivity.this.getString(R.string.dialog_send_to_bank_paid_title));
                DocumentFragment.updateCurrentDocument = true;
            } else {
                if (invalidToken) {
                    finishActivityWithAction(ApiConstants.LOGOUT);
                }
                DialogUtitities.showToast(DisplayContentActivity.this, errorMessage);
            }
            setSendToBankMenuText(sendToBank.isVisible());
        }
    }

    private class OpenInvoiceTask extends AsyncTask<Void, Void, CurrentBankAccount> {
        private String errorMessage;
        private Document document;
        private Attachment attachment;
        private boolean invalidToken;

        public OpenInvoiceTask(final Attachment attachment, final Document document) {
            this.invalidToken = false;
            this.document = document;
            this.attachment = attachment;
        }

        @Override
        protected CurrentBankAccount doInBackground(Void... voids) {
            try {
                return ContentOperations.getCurrentBankAccount(DisplayContentActivity.this);
            } catch (DigipostApiException e) {
                Log.e(getClass().getName(), e.getMessage(), e);
                errorMessage = e.getMessage();
                return null;
            } catch (DigipostClientException e) {
                Log.e(getClass().getName(), e.getMessage(), e);
                errorMessage = e.getMessage();
                return null;
            } catch (DigipostAuthenticationException e) {
                Log.e(getClass().getName(), e.getMessage(), e);
                errorMessage = e.getMessage();
                invalidToken = true;
                return null;
            }
        }

        @Override
        protected void onPostExecute(CurrentBankAccount currentBankAccount) {
            super.onPostExecute(currentBankAccount);
            if (invalidToken) {
                DialogUtitities.showToast(DisplayContentActivity.this, errorMessage);
                finishActivityWithAction(ApiConstants.LOGOUT);
            } else {
                openInvoiceContent(attachment, document, currentBankAccount);
            }
        }
    }

    private class MoveToFolderListOnItemClickListener implements AdapterView.OnItemClickListener {
        public MoveToFolderListOnItemClickListener() {
        }

        public void onItemClick(final AdapterView<?> arg0, final View arg1, final int position, final long arg3) {

            Folder folder = folderAdapter.getItem(position);
            folderId = valueOf(folder.getId());

            if (folderId.equals("0")) {
                location = "INBOX";
            } else {
                location = "FOLDER";
            }

            alertDialog.dismiss();
            alertDialog = null;
            finishActivityWithAction(ApiConstants.MOVE);

        }
    }

    private class DownloadDocumentTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            showContentProgressDialog(this, DisplayContentActivity.this.getString(R.string.saving));
        }

        @Override
        protected Boolean doInBackground(Void... parameters) {
            try {
                FileUtilities.writeFileToDevice(getApplicationContext());
                return true;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            hideProgressDialog();
        }

        @Override
        protected void onPostExecute(Boolean downloadSuccessfull) {
            super.onPostExecute(downloadSuccessfull);

            if (downloadSuccessfull) {
                DialogUtitities.showToast(DisplayContentActivity.this, getString(R.string.pdf_saved_to_sd));
            } else {
                DialogUtitities.showToast(DisplayContentActivity.this, getString(R.string.pdf_save_to_sd_failed));
            }

            hideProgressDialog();
        }
    }

    public void requestWritePermissionsIfMissing(){
        if(!FileUtilities.isStorageWriteAllowed(getApplicationContext())) {
            ActivityCompat.requestPermissions(DisplayContentActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }
}
