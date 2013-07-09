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

package no.digipost.android.gui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import no.digipost.android.R;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.documentstore.DocumentContentStore;
import no.digipost.android.gui.AttachmentArrayAdapter;
import no.digipost.android.gui.MuPDFActivity;
import no.digipost.android.gui.adapters.LetterArrayAdapter;
import no.digipost.android.model.Attachment;
import no.digipost.android.model.Letter;
import no.digipost.android.utilities.DialogUtitities;

public abstract class DocumentFragment extends ContentFragment {
    public static final int REQUESTCODE_INTENT = 0;

    public DocumentFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        super.listAdapter = new LetterArrayAdapter(getActivity(), R.layout.content_list_item, new CheckBoxOnClickListener());
        super.listView.setAdapter(listAdapter);
        super.listView.setOnItemClickListener(new DocumentListOnItemClickListener());

        updateAccountMeta();

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == REQUESTCODE_INTENT) {
                // ToDo håndtere handling fra dokumentvisning
            }
        }
    }

    private class DocumentListOnItemClickListener implements AdapterView.OnItemClickListener {
        public void onItemClick(final AdapterView<?> arg0, final View view, final int position, final long arg3) {
            openListItem((Letter) DocumentFragment.super.listAdapter.getItem(position));
        }
    }

    private class AttachmentListOnItemClickListener implements AdapterView.OnItemClickListener {
        private AttachmentArrayAdapter attachmentArrayAdapter;

        public AttachmentListOnItemClickListener(AttachmentArrayAdapter attachmentArrayAdapter) {
            this.attachmentArrayAdapter = attachmentArrayAdapter;
        }

        public void onItemClick(final AdapterView<?> arg0, final View arg1, final int position, final long arg3) {
            executeGetAttachmentContentTask(attachmentArrayAdapter.getItem(position));
        }
    }

    private void showAttachmentDialog(final ArrayList<Attachment> attachments) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.attachmentdialog_layout, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        ListView attachmentListView = (ListView) view.findViewById(R.id.attachmentdialog_listview);
        AttachmentArrayAdapter attachmentAdapter = new AttachmentArrayAdapter(getActivity(), R.layout.attachentdialog_list_item, attachments);

        attachmentListView.setAdapter(attachmentAdapter);
        attachmentListView.setOnItemClickListener(new AttachmentListOnItemClickListener(attachmentAdapter));
        attachmentAdapter.placeMainOnTop();

        Dialog attachmentDialog = builder.create();
        attachmentDialog.show();
    }

    private void openListItem(final Letter letter){
        if (letter.getOpeningReceiptUri() != null) {
            showOpeningReceiptDialog(letter);
        } else {
            findDocumentAttachments(letter);
        }
    }

    private void findDocumentAttachments(final Letter letter) {
        ArrayList<Attachment> attachments = letter.getAttachment();

        if (attachments.size() > 1) {
            showAttachmentDialog(letter.getAttachment());
        } else {
            executeGetAttachmentContentTask(attachments.get(0));
        }
    }

    private void sendOpeningReceipt(final Letter letter){
        SendOpeningReceiptTask task = new SendOpeningReceiptTask(letter);
        task.execute();
    }

    private void showOpeningReceiptDialog(final Letter letter){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(getString(R.string.dialog_answer_opening_receipt_message)).setPositiveButton(getString(R.string.dialog_answer_opening_receipt_yes), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                sendOpeningReceipt(letter);
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

    private void openAttachmentContent(final Attachment attachment) {
        if (attachment.getAuthenticationLevel().equals(ApiConstants.AUTHENTICATION_LEVEL_TWO_FACTOR)) {
            // ToDo vise mld om at dokument ikke kan åpnes
            return;
        }

        String fileType = attachment.getFileType();
        Intent intent = null;

        if (fileType.equals(ApiConstants.FILETYPE_PDF)) {
            intent = new Intent(getActivity(), MuPDFActivity.class);
        } else if (fileType.equals(ApiConstants.FILETYPE_HTML)) {
            // ToDo åpne HTML
        } else {
            for (String imageFiletype : ApiConstants.FILETYPES_IMAGE) {
                if (fileType.equals(imageFiletype)) {
                    // ToDo åpne bilde
                }
            }
        }

        // ToDo åpne ustøttet

        startActivityForResult(intent, REQUESTCODE_INTENT);
    }

    private void executeGetAttachmentContentTask(Attachment attachment) {
        GetAttachmentContentTask getAttachmentContentTask = new GetAttachmentContentTask(attachment);
        getAttachmentContentTask.execute();
    }

    private class GetAttachmentContentTask extends AsyncTask<Void, Void, byte[]> {
        private Attachment attachment;
        private String errorMessage;
        private boolean invalidToken;

        public GetAttachmentContentTask(Attachment attachment) {
            this.attachment = attachment;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DocumentFragment.super.showContentProgressDialog(this, context.getString(R.string.loading_content));
        }

        @Override
        protected byte[] doInBackground(Void... voids) {
            try {
                return DocumentFragment.super.letterOperations.getDocumentContent(attachment);
            } catch (DigipostAuthenticationException e) {
                Log.e(getClass().getName(), e.getMessage(), e);
                errorMessage = e.getMessage();
                invalidToken = true;
                return null;
            } catch (DigipostApiException e) {
                Log.e(getClass().getName(), e.getMessage(), e);
                errorMessage = e.getMessage();
                return null;
            } catch (DigipostClientException e) {
                Log.e(getClass().getName(), e.getMessage(), e);
                errorMessage = e.getMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute(byte[] result) {
            super.onPostExecute(result);
            DocumentFragment.super.hideProgressDialog();

            if (result != null) {
                DocumentContentStore.setContent(result, attachment);
                openAttachmentContent(attachment);
            } else {
                if (invalidToken) {
                    // ToDo logge ut
                }

                DialogUtitities.showToast(DocumentFragment.this.getActivity(), errorMessage);
            }
            updateAccountMeta();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            DocumentFragment.super.hideProgressDialog();
            DocumentContentStore.clearContent();
        }
    }

    protected class GetDocumentMetaTask extends AsyncTask<Void, Void, ArrayList<Letter>> {
        private final int content;
        private String errorMessage;
        private boolean invalidToken;

        public GetDocumentMetaTask(final int content) {
            this.content = content;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            activityCommunicator.onStartRefreshContent();
        }

        @Override
        protected ArrayList<Letter> doInBackground(final Void... params) {
            try {
                return DocumentFragment.super.letterOperations.getAccountContentMeta(content);
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
        protected void onPostExecute(final ArrayList<Letter> letters) {
            super.onPostExecute(letters);
            if(letters != null){
                DocumentFragment.super.listAdapter.replaceAll(letters);
            } else {
                if (invalidToken) {
                    // ToDo logge ut
                }

                DialogUtitities.showToast(DocumentFragment.this.context, errorMessage);
            }

            activityCommunicator.onEndRefreshContent();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            activityCommunicator.onEndRefreshContent();
        }
    }

    protected void moveDocuments(String toLocation) {
        ArrayList<Letter> letters = listAdapter.getCheckedItems();

        DocumentMoveTask documentMoveTask = new DocumentMoveTask(letters, toLocation);
        documentMoveTask.execute();
    }

    protected void showMoveDocumentsDialog(final String toLocation, final DocumentMultiChoiceModeListener documentMultiChoiceModeListener, final ActionMode actionMode) {
        AlertDialog.Builder alertDialogBuilder = DialogUtitities.getAlertDialogBuilderWithMessage(context, getString(R.string.dialog_prompt_move_documents));
        alertDialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                moveDocuments(toLocation);
                documentMultiChoiceModeListener.onDestroyActionMode(actionMode);
                dialogInterface.dismiss();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.abort, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private class DocumentMoveTask extends AsyncTask<Void, Integer, String> {
        private ArrayList<Letter> letters;
        private String toLocation;
        private boolean invalidToken;

        public DocumentMoveTask(ArrayList<Letter> letters, String toLocation) {
            this.letters = letters;
            this.toLocation = toLocation;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DocumentFragment.super.showContentProgressDialog(this, "");
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                Integer progress = 0;

                for (Letter letter : letters) {
                    publishProgress(++progress);
                    letter.setLocation(toLocation);
                    letterOperations.moveDocument(letter);
                }

                return null;
            } catch (DigipostAuthenticationException e) {
                Log.e(getClass().getName(), e.getMessage(), e);
                invalidToken = true;
                return e.getMessage();
            } catch (DigipostApiException e) {
                Log.e(getClass().getName(), e.getMessage(), e);
                return e.getMessage();
            } catch (DigipostClientException e) {
                Log.e(getClass().getName(), e.getMessage(), e);
                return e.getMessage();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            DocumentFragment.super.progressDialog.setMessage(values[0] + " av " + letters.size() + " flyttet");
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            DocumentFragment.super.hideProgressDialog();
            updateAccountMeta();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            DocumentFragment.super.hideProgressDialog();

            if (result != null) {
                DialogUtitities.showToast(context, result);

                if (invalidToken) {
                    // ToDo logge ut
                }
            }

            updateAccountMeta();
        }
    }

    protected class SendOpeningReceiptTask extends AsyncTask<Void, Void, Boolean> {
        private String errorMessage;
        private Letter letter;
        private boolean invalidToken;

        public SendOpeningReceiptTask(final Letter letter) {
            invalidToken = false;
            this.letter = letter;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DocumentFragment.super.showContentProgressDialog(this, context.getString(R.string.loading_content));
        }

        @Override
        protected Boolean doInBackground(final Void... params) {
            try {
                DocumentFragment.super.letterOperations.sendOpeningReceipt(letter);
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
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            DocumentFragment.super.hideProgressDialog();

            if (result != null) {

                UpdateSelfAndOpenTask task = new UpdateSelfAndOpenTask(letter);
                task.execute();
            } else {
                if (invalidToken) {
                    // ToDo logge ut
                }
                DialogUtitities.showToast(DocumentFragment.this.getActivity(), errorMessage);
            }
        }
    }

    protected class UpdateSelfAndOpenTask extends AsyncTask<Void, Void, Boolean> {
        private String errorMessage;
        private Letter letter;
        private boolean invalidToken;

        public UpdateSelfAndOpenTask(final Letter letter) {
            invalidToken = false;
            this.letter = letter;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DocumentFragment.super.showContentProgressDialog(this, context.getString(R.string.loading_content));
        }

        @Override
        protected Boolean doInBackground(final Void... params) {
            try {
                letter = DocumentFragment.super.letterOperations.getSelfLetter(letter);
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
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            DocumentFragment.super.hideProgressDialog();

            if (result != null) {
                findDocumentAttachments(letter);
            } else {
                if (invalidToken) {
                    // ToDo logge ut
                }
                DialogUtitities.showToast(DocumentFragment.this.getActivity(), errorMessage);
            }
        }
    }

    protected class DocumentMultiChoiceModeListener extends ContentMultiChoiceModeListener {

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, android.view.MenuItem menuItem) {
            super.onActionItemClicked(actionMode, menuItem);

            switch (menuItem.getItemId()) {
                case R.id.main_context_menu_delete:
                    DocumentFragment.super.showDeleteContentDialog(getString(R.string.dialog_prompt_delete_documents), this, actionMode);
                    break;
            }

            return true;
        }
    }
}
