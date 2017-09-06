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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.google.android.gms.analytics.GoogleAnalytics;
import no.digipost.android.DigipostApplication;
import no.digipost.android.R;
import no.digipost.android.analytics.GAEventController;
import no.digipost.android.api.ContentOperations;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.documentstore.DocumentContentStore;
import no.digipost.android.gui.MainContentActivity;
import no.digipost.android.gui.adapters.FolderArrayAdapter;
import no.digipost.android.gui.fragments.ContentFragment;
import no.digipost.android.gui.fragments.DocumentFragment;
import no.digipost.android.gui.invoice.InvoiceBankAgreements;
import no.digipost.android.gui.invoice.InvoiceOptionsActivity;
import no.digipost.android.gui.metadata.AppointmentView;
import no.digipost.android.gui.metadata.ExternalLinkView;
import no.digipost.android.model.*;
import no.digipost.android.utilities.*;

import java.util.ArrayList;

import static java.lang.String.format;
import static java.lang.String.valueOf;

public abstract class DisplayContentActivity extends AppCompatActivity {

    protected MenuItem sendToBank;
    protected int content_type;
    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;
    private FolderArrayAdapter folderAdapter;
    private String location;
    private String folderId;
    private GridLayout gridLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);
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

    protected void setupMetadataView() {
        if(content_type != ApplicationConstants.RECEIPTS) {
            showMetadata();
        }
    }

    private void showMetadata() {
        Attachment attachment = DocumentContentStore.getDocumentAttachment();
        if (DocumentContentStore.getDocumentAttachment() != null) {
            ArrayList<Metadata> metadataList = attachment.getMetadata();
            
            for (Metadata metadata : metadataList) {
                if (metadata.type.equals(Metadata.APPOINTMENT)) {
                    addAppointmentView(metadata);
                } else if (metadata.type.equals(Metadata.EXTERNAL_LINK)) {
                    addExternalLinkView(metadata);
                }
            }
            ScrollView containerScrollView = (ScrollView) findViewById(R.id.container_scrollview);
            if (metadataList.size() == 0) {
                containerScrollView.setFocusable(false);
            }else {
                containerScrollView.setFocusable(true);

            }
        }
    }

    private Metadata mockExternalLink() {
        Metadata metadata = new Metadata();
        metadata.type = "ExternalLink";
        metadata.description = "Avsender ber deg akseptere eller avslå tilbudet om barnehageplass.";
        metadata.deadline = "2017-09-09T13:37:00+02:00";
        metadata.buttonText = "Gå til Digipost";
        metadata.url = "https://www.digipost.no";
        metadata.urlIsActive = true;
        return metadata;
    }

    private void addExternalLinkView(Metadata metadata) {
        ExternalLinkView externalLinkView = ExternalLinkView.newInstance();
        externalLinkView.setExternallink(metadata);
        addViewToContainerLayout(externalLinkView);
    }

    private void addAppointmentView(Metadata metadata) {
        metadata.title = "Du har fått en innkalling fra " + DocumentContentStore.getDocumentParent().getCreatorName();
        AppointmentView appointmentView = AppointmentView.newInstance();
        appointmentView.setAppointment(metadata);
        addViewToContainerLayout(appointmentView);
    }

    private void addViewToContainerLayout(Fragment fragment){
        LinearLayout containerLayout = (LinearLayout) findViewById(R.id.container_layout);
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        int randomId = (int) (Math.random()*100);
        ll.setId(randomId);
        getFragmentManager().beginTransaction().add(ll.getId(), fragment, "MetadataView" + randomId).commit();
        containerLayout.addView(ll,0);
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

    protected boolean shouldShowInvoiceOptionsDialog(final Activity activity) {
        final Attachment attachment = DocumentContentStore.getDocumentAttachment();

        boolean attachmentIsInvoice = attachment != null && attachment.getType().equals(ApiConstants.INVOICE) && attachment.getInvoice() != null;
        boolean hasNoActiveBankAgreements = !InvoiceBankAgreements.hasActiveAgreements(getApplicationContext());
        boolean showInvoiceOptionsTips = SharedPreferencesUtilities.showInvoiceOptionsDialog(getApplicationContext());

        return attachmentIsInvoice && hasNoActiveBankAgreements && showInvoiceOptionsTips;
    }

    protected void showInvoiceOptionsDialog(final Activity activity) {
        final Attachment attachment = DocumentContentStore.getDocumentAttachment();

        AlertDialog invoiceOptionsDialog = null;
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.generic_dialog_layout, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(DisplayContentActivity.this)
                .setPositiveButton(R.string.invoice_dialog_choose_bank_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        GAEventController.sendInvoiceClickedChooseBankDialog(DisplayContentActivity.this, getString(R.string.invoice_dialog_choose_bank_button));
                        openBankOptionsActivity(attachment.getSubject(), activity);
                        dialogInterface.dismiss();
                    }
                }).setNeutralButton(R.string.invoice_dialog_later_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        GAEventController.sendInvoiceClickedChooseBankDialog(DisplayContentActivity.this, getString(R.string.invoice_dialog_later_button));
                        dialogInterface.dismiss();
                    }
                }).setNegativeButton(R.string.invoice_dialog_forget_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        GAEventController.sendInvoiceClickedChooseBankDialog(DisplayContentActivity.this, getString(R.string.invoice_dialog_forget_button));
                        SharedPreferencesUtilities.hideInvoiceOptionsDialog(getApplicationContext());
                        dialogInterface.dismiss();
                    }
                });

        builder.setView(view);
        builder.setTitle(getResources().getString(R.string.invoice_dialog_title));
        builder.setMessage(getResources().getString(R.string.invoice_dialog_subtitle));

        invoiceOptionsDialog = builder.create();
        invoiceOptionsDialog.show();

        final Button positiveButton = invoiceOptionsDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
        positiveButtonLL.gravity = Gravity.CENTER;
        positiveButton.setLayoutParams(positiveButtonLL);

        final Button neutralButton = invoiceOptionsDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        LinearLayout.LayoutParams neutralButtonLL = (LinearLayout.LayoutParams) neutralButton.getLayoutParams();
        neutralButtonLL.gravity = Gravity.CENTER_VERTICAL;
        neutralButton.setLayoutParams(neutralButtonLL);

        final Button negativeButton = invoiceOptionsDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        LinearLayout.LayoutParams negativeButtonLL = (LinearLayout.LayoutParams) negativeButton.getLayoutParams();
        negativeButtonLL.gravity = Gravity.CENTER;
        negativeButton.setLayoutParams(negativeButtonLL);
    }

    private void openBankOptionsActivity(final String invoiceSubject, final Activity activity) {
        Intent i = new Intent(activity, InvoiceOptionsActivity.class);
        i.putExtra(InvoiceOptionsActivity.INTENT_ACTIONBAR_TITLE, invoiceSubject);
        activity.startActivity(i);
    }

    protected void showInformationDialog() {
        if (content_type != ApplicationConstants.RECEIPTS && DocumentContentStore.getDocumentParent() != null && DocumentContentStore.getDocumentAttachment() != null) {
            new AlertDialog.Builder(this).setMessage(getFormattedInfoText()).setNegativeButton(getString(R.string.close), null).show();
        }
    }

    private Spanned getFormattedInfoText(){
        String documentSubject = DocumentContentStore.getDocumentParent().getSubject();
        String attachmentSubject = DocumentContentStore.getDocumentAttachment().getSubject();

        String infoText = String.format("<b>%1$s</b>", documentSubject);

        if(!attachmentSubject.equals(documentSubject)) {
            infoText += String.format("<br><br> %1$s ", DocumentContentStore.getDocumentAttachment().getSubject());
        }

        infoText += String.format("<br><br> %1$s", DocumentContentStore.getDocumentParent().getCreatorName());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(infoText,Html.FROM_HTML_MODE_LEGACY);
        }else{
            return Html.fromHtml(infoText);
        }
    }

    protected void deleteAction(final Activity originActivity) {

        String message = getString(R.string.dialog_prompt_delete_document);
        String positiveAction = getString(R.string.delete);
        String negativeAction = getString(R.string.abort);

        Invoice invoice = DocumentContentStore.getDocumentAttachment().getInvoice();

        if (content_type == ApplicationConstants.RECEIPTS) {
            message = getString(R.string.dialog_prompt_delete_receipt);

        }else if(invoice != null){

            Payment payment = invoice.getPayment();

            if (InvoiceBankAgreements.hasActiveAgreementType(getApplicationContext(), InvoiceBankAgreements.TYPE_2)) {
                if (payment != null) {
                    message = getString(R.string.invoice_delete_dialog_paid_invoice_type_2_agreement);
                } else {
                    message = getString(R.string.invoice_delete_dialog_unpaid_invoice_type_2_agreement);
                }
            } else if (InvoiceBankAgreements.hasActiveAgreementType(getApplicationContext(), InvoiceBankAgreements.TYPE_1)) {
                if (payment != null) {
                    message = getString(R.string.invoice_delete_dialog_paid_invoice_type_1_agreement);
                } else {
                    message = getString(R.string.invoice_delete_dialog_unpaid_invoice_type_1_agreement);
                }
            } else {
                message = getString(R.string.invoice_delete_dialog_invoice_no_agreement);
            }
        }

        showActionDialog(originActivity, ApiConstants.DELETE, message, positiveAction, negativeAction );
    }

    protected void showActionDialog(final Activity originActivity, final String action ,final String message ,final String positiveAction,final String negativeAction){
        AlertDialog.Builder builder = DialogUtitities.getAlertDialogBuilderWithMessage(this, message);
        builder.setPositiveButton(positiveAction, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                executeAction(originActivity, action);
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(negativeAction, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.create().show();
    }

    private void executeAction(final Activity originActivity, String action) {
        Intent intent = new Intent(originActivity, MainContentActivity.class);
        intent.putExtra(ApiConstants.FRAGMENT_ACTIVITY_RESULT_ACTION, action);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void setActionBar(String title) {
        if(getSupportActionBar()!=null) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    protected void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    protected void setSendToBankMenuText(boolean sendToBankVisible) {
        boolean hasActiveType2Agreement = InvoiceBankAgreements.hasActiveAgreementType(getApplicationContext(), InvoiceBankAgreements.TYPE_2);

        if(hasActiveType2Agreement){
            sendToBank.setVisible(false);
        }else {

            if (sendToBankVisible) {
                sendToBank.setVisible(true);
                Payment payment = DocumentContentStore.getDocumentAttachment().getInvoice() == null ? null : DocumentContentStore
                        .getDocumentAttachment()
                        .getInvoice()
                        .getPayment();
                if (payment != null) {
                    sendToBank.setTitle(getString(R.string.sent_to_bank));
                }else{
                    boolean hasNoActiveBankAgreements = !InvoiceBankAgreements.hasActiveAgreements(getApplicationContext());
                    if(hasNoActiveBankAgreements){
                        sendToBank.setTitle(R.string.invoice_payment_tips_button);
                    }
                }
            } else {
                sendToBank.setVisible(false);
            }
        }
    }

    private void openInvoiceContent(Attachment attachment, Document document, CurrentBankAccount account) {
        Payment payment = attachment.getInvoice().getPayment();

        if (payment != null) {
            showPaidInvoiceDialog(attachment.getInvoice());
        } else {
            if (attachment.getInvoice().getSendToBank() != null) {
                String accountNumber = account == null ? "***********" : account.getBankAccount().getAccountNumber();
                showSendToBankDialog(attachment, document, accountNumber);
            } else {
                showInvoiceOptionsDialog(this);
            }
        }
    }

    private void showPaidInvoiceDialog(Invoice invoice) {
        String timePaid = FormatUtilities.getFormattedDate(invoice.getPayment().getTimePaid());

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
        View view = inflater.inflate(R.layout.generic_dialog_layout, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this).setNegativeButton(getString(R.string.close),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );

        builder.setView(view);

        ListView moveToFolderListView = (ListView) view.findViewById(R.id.generic_dialog_listview);

        ArrayList<Folder> folders = DocumentContentStore.getMoveFolders();
        folderAdapter = new FolderArrayAdapter(this, R.layout.generic_dialog_list_item, folders);
        moveToFolderListView.setAdapter(folderAdapter);
        moveToFolderListView.setOnItemClickListener(new MoveToFolderListOnItemClickListener());

        builder.setTitle(R.string.move_to);
        alertDialog = builder.create();
        alertDialog.show();
    }

    protected void openFileWithIntent() {
        Permissions.requestWritePermissionsIfMissing(getApplicationContext(), DisplayContentActivity.this);

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
        Permissions.requestWritePermissionsIfMissing(getApplicationContext(), DisplayContentActivity.this);

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
}
