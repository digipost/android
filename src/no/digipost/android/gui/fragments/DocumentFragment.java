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
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
        super.listAdapter = new LetterArrayAdapter(getActivity(), R.layout.content_list_item);
        super.listView.setAdapter(listAdapter);
        super.listView.setOnItemClickListener(new DocumentListOnItemClickListener());
        return view;
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

    private void findDocumentAttachments(final Letter letter) {
        ArrayList<Attachment> attachments = letter.getAttachment();

        if (attachments.size() > 1) {
            showAttachmentDialog(letter.getAttachment());
        } else {
            executeGetAttachmentContentTask(attachments.get(0));
        }
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
            DocumentFragment.super.showProgressDialog(this);
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

            if (result != null) {
                DocumentContentStore.setContent(result, attachment);
                openAttachmentContent(attachment);
            } else {
                if (invalidToken) {
                    // ToDo logge ut
                }

                DialogUtitities.showToast(DocumentFragment.this.getActivity(), errorMessage);
            }

            DocumentFragment.super.hideProgressDialog();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            DocumentFragment.super.hideProgressDialog();
            DocumentContentStore.clearContent();
        }
    }

    private class DocumentListOnItemClickListener implements AdapterView.OnItemClickListener {
        public void onItemClick(final AdapterView<?> arg0, final View arg1, final int position, final long arg3) {
            findDocumentAttachments((Letter) DocumentFragment.super.listAdapter.getItem(position));
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

    protected void updateAccountMeta(int content){
        GetAccountMetaTask task = new GetAccountMetaTask(content);
        task.execute();
    }

    protected class GetAccountMetaTask extends AsyncTask<Void, Void, ArrayList<Letter>> {
        private final int content;
        private String errorMessage;
        private boolean invalidToken;

        public GetAccountMetaTask(final int content) {
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
}
