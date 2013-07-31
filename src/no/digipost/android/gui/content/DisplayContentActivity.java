package no.digipost.android.gui.content;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.io.File;

import no.digipost.android.R;
import no.digipost.android.api.ContentOperations;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.documentstore.DocumentContentStore;
import no.digipost.android.model.Attachment;
import no.digipost.android.model.CurrentBankAccount;
import no.digipost.android.model.Invoice;
import no.digipost.android.model.Letter;
import no.digipost.android.utilities.DataFormatUtilities;
import no.digipost.android.utilities.DialogUtitities;
import no.digipost.android.utilities.FileUtilities;

public abstract class DisplayContentActivity extends Activity {
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    protected void hideProgressDialog() {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
    }

    private void openInvoiceContent(Attachment attachment, Letter letter, CurrentBankAccount account) {

        if (attachment.getInvoice().getPayment() != null) {
            showPaidInvoiceDialog(attachment.getInvoice());
        } else {
            String accountNumber = null;

            if (attachment.getInvoice().getSendToBank() != null) {
                // SEND TIL NETTBANK

                showSendToBankDialog(attachment, letter, account.getBankAccount().getAccountNumber());
            } else {
                showSendToBankDialog(attachment, letter, null);
            }
        }
    }

    private void showPaidInvoiceDialog(Invoice invoice){
        String timePaid = DataFormatUtilities.getFormattedDate(invoice.getPayment().getTimePaid());
        String debitorBankAccount = invoice.getPayment().getDebitorBankAccount();
        String bankHomepage = invoice.getPayment().getBankHomepage();

        String title = getString(R.string.dialog_send_to_bank_paid_title);
        String message = getString(R.string.dialog_send_to_bank_paid_message_start) + timePaid + "." + "\n"+"\n" + getString(R.string.dialog_send_to_bank_paid_message_end);

        AlertDialog.Builder builder = DialogUtitities.getAlertDialogBuilderWithMessageAndTitle(this, message, title);
        builder.setCancelable(false).setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.cancel();
            }
        });

        builder.create().show();

    }

    private void showSendToBankDialog(final Attachment attachment, final Letter letter, final String accountNumber) {
        String title = getString(R.string.dialog_send_to_bank_not_paid_title);
        String message = getString(R.string.dialog_send_to_bank_not_paid_message_start) + "\n\n";

        if (accountNumber != null) {
            message += getString(R.string.dialog_send_to_bank_not_paid_message_end) + "\n" + accountNumber;
        }

        AlertDialog.Builder builder = DialogUtitities.getAlertDialogBuilderWithMessageAndTitle(this, message, title);
        builder.setPositiveButton(getString(R.string.dialog_send_to_bank), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {

                if (accountNumber != null) {
                    sendToBank(attachment, letter);
                } else {
                    showSendToBankNotEnabledDialog();
                }

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
        String message = getString(R.string.dialog_send_to_bank_not_enabled_message_start) + "\n\n" + getString(R.string.dialog_send_to_bank_not_enabled_message_end);;

        AlertDialog.Builder builder = DialogUtitities.getAlertDialogBuilderWithMessageAndTitle(this, message, title);
        builder.setCancelable(false).setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.cancel();
            }
        });

        builder.create().show();
    }

    private void sendToBank(final Attachment attachment, final Letter letter) {
        SendToBankTask task = new SendToBankTask(attachment, letter);
        task.execute();
    }

    protected class SendToBankTask extends AsyncTask<Void, Void, Boolean> {
        private String errorMessage;
        private Letter letter;
        private Attachment attachment;
        private boolean invalidToken;

        public SendToBankTask(final Attachment attachment, final Letter letter) {
            this.letter = letter;
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
                DocumentContentStore.setDocumentParent(ContentOperations.getSelfLetter(DisplayContentActivity.this, letter));
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
            if (result == true) {
                //updateAdapterLetter(letter, listPosition);
                DialogUtitities.showToast(DisplayContentActivity.this, "FY FAEN, DET FUNKA!!");
                // ToDo
            } else {
                if (invalidToken) {
                    finishActivityWithAction(ApiConstants.LOGOUT);
                }
                DialogUtitities.showToast(DisplayContentActivity.this, errorMessage);
            }
        }
    }

    protected void openInvoiceTask() {
        OpenInvoiceTask task = new OpenInvoiceTask(DocumentContentStore.getDocumentAttachment(), DocumentContentStore.getDocumentParent());
        task.execute();
    }

    private class OpenInvoiceTask extends AsyncTask<Void, Void, CurrentBankAccount> {
        private String errorMessage;
        private Letter letter;
        private Attachment attachment;
        private boolean invalidToken;

        public OpenInvoiceTask(final Attachment attachment, final Letter letter) {
            this.invalidToken = false;
            this.letter = letter;
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
                openInvoiceContent(attachment, letter, currentBankAccount);
            }
        }
    }

    private void finishActivityWithAction(String action) {
        Intent intent = new Intent();
        intent.putExtra(ApiConstants.ACTION, action);
        setResult(RESULT_OK, intent);
        finish();
    }

    protected void openFileWithIntent() {
        if (DocumentContentStore.getDocumentContent() == null) {
            DialogUtitities.showToast(this, getString(R.string.error_failed_to_open_with_intent));
            finish();
        }

        try {
            FileUtilities.openFileWithIntent(this, DocumentContentStore.getDocumentAttachment().getFileType(), DocumentContentStore.getDocumentContent());
        } catch (ActivityNotFoundException e) {
            DialogUtitities.showToast(this, getString(R.string.error_no_activity_to_open_file));
        } catch (Exception e) {
            DialogUtitities.showToast(this, getString(R.string.error_failed_to_open_with_intent));
        }
    }

    protected void promtSaveToSD() {
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

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showContentProgressDialog(this, "lagrer...");
        }

        @Override
        protected Boolean doInBackground(Void... parameters) {
            File file = null;

            try {
                file = FileUtilities.writeFileToSD(DocumentContentStore.getDocumentAttachment().getSubject(), DocumentContentStore.getDocumentAttachment().getFileType(), DocumentContentStore.getDocumentContent());
            } catch (Exception e) {
                return false;
            }

            FileUtilities.makeFileVisible(DisplayContentActivity.this, file);

            return true;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            hideProgressDialog();
        }

        @Override
        protected void onPostExecute(Boolean saved) {
            super.onPostExecute(saved);

            if (saved) {
                DialogUtitities.showToast(DisplayContentActivity.this, getString(R.string.pdf_saved_to_sd));
            } else {
                DialogUtitities.showToast(DisplayContentActivity.this, getString(R.string.pdf_save_to_sd_failed));
            }

            hideProgressDialog();
        }
    }
}
