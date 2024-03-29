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
import android.view.*;
import android.widget.AdapterView;
import android.widget.ListView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import no.digipost.android.DigipostApplication;
import no.digipost.android.R;
import no.digipost.android.api.ContentOperations;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.authentication.TokenStore;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.documentstore.DocumentContentStore;
import no.digipost.android.gui.MainContentActivity;
import no.digipost.android.gui.WebLoginActivity;
import no.digipost.android.gui.adapters.AttachmentArrayAdapter;
import no.digipost.android.gui.adapters.DocumentAdapter;
import no.digipost.android.gui.adapters.FolderArrayAdapter;
import no.digipost.android.gui.content.HtmlAndReceiptActivity;
import no.digipost.android.gui.content.ImageActivity;
import no.digipost.android.gui.content.MuPDFActivity;
import no.digipost.android.gui.content.UnsupportedDocumentFormatActivity;
import no.digipost.android.gui.fingerprint.FingerprintActivity;
import no.digipost.android.model.Attachment;
import no.digipost.android.model.Document;
import no.digipost.android.model.Documents;
import no.digipost.android.model.Folder;
import no.digipost.android.utilities.DialogUtitities;
import no.digipost.android.utilities.JSONUtilities;
import no.digipost.android.utilities.NetworkUtilities;

import javax.ws.rs.core.HttpHeaders;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static java.util.Arrays.asList;

public class DocumentFragment extends ContentFragment<Document> {

    private AttachmentArrayAdapter attachmentAdapter;
    private FolderArrayAdapter folderAdapter;
    public static boolean updateCurrentDocument = false;
    private AsyncHttpClient asyncHttpClient;
    private int content = 0;
    private int currentListPosition = 0;
    private Dialog folderDialog;
    private ProgressDialog updateProgressDialog;
    private Dialog attachmentDialog;
    private boolean openAttachment = true;
    private static String EXTRA_CONTENT = "content";
    private static final int INTENT_OPEN_ATTACHMENT_CONTENT = 0;
    private static final int INTENT_ID_PORTEN_WEBVIEW_LOGIN = 1;
    protected DocumentAdapter documentAdapter;
    protected boolean multiSelectEnabled;
    private ActionMode internalActionMode;

    public static DocumentFragment newInstance(int content) {
        DocumentFragment fragment = new DocumentFragment();
        Bundle bundle = new Bundle(1);
        bundle.putInt(EXTRA_CONTENT, content);
        fragment.setArguments(bundle);
        return fragment ;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.content = getArguments().getInt(EXTRA_CONTENT,0);
    }

    @Override
    public int getContent() {
        return content;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        setupNewDocumentAdapter();
        return view;
    }

    private void setupNewDocumentAdapter(){
        documentAdapter = new DocumentAdapter(context, new ArrayList<Document>());
        recyclerView.setAdapter(documentAdapter);
    }

    public void loadMoreContent(){
        if(documentAdapter != null && documentAdapter.remainingContentToGet()) {
            updateAccountMeta(false);
        }
    }

    public void clearExistingContent(){
        if(documentAdapter != null)
            documentAdapter.clearExistingContent();
    }

    @Override
    void recyclerViewOnClick(int position){
        currentListPosition = position;
        if(position != -1) {
            if (multiSelectEnabled) {
                documentAdapter.select(position);
            } else {
                Document document = documentAdapter.getDocuments().get(position);
                if(document != null) {
                    openUpdatedDocument(document);
                }
            }
        }
    }

    @Override
    void recyclerViewOnLongClick(int position){
        currentListPosition = position;
        if(multiSelectEnabled) {
            documentAdapter.select(position);
        }else{
            beginActionMode(position);
        }
    }

    private void beginActionMode(int position){
        if(!activityDrawerOpen) {
            multiSelectEnabled = true;
            contentActionMode = getActivity().startActionMode(new SelectActionModeCallback());
            documentAdapter.setSelectable(multiSelectEnabled);
            documentAdapter.select(position);
        }
    }

    @Override
    public void finishActionMode(){
        if(contentActionMode != null)contentActionMode.finish();
        if(internalActionMode != null)internalActionMode.finish();
        multiSelectEnabled = false;
        documentAdapter.setSelectable(false);
    }

    private class SelectActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            internalActionMode = mode;
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.activity_main_content_context, menu);
            return true;
        }
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            internalActionMode = mode;
            switch (item.getItemId()) {
                case R.id.main_context_menu_move:
                    showMoveToFolderDialog(documentAdapter.getSelected());
                    return true;
                case R.id.main_context_menu_delete:
                    DocumentFragment.super.deleteContent(documentAdapter.getSelected());
                    return true;
                default:
                    finishActionMode();
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            finishActionMode();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        dismissUpdateProgressDialogIfExisting();
        refreshItems();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == DocumentFragment.INTENT_OPEN_ATTACHMENT_CONTENT) {
                if (data.hasExtra(ApiConstants.FRAGMENT_ACTIVITY_RESULT_ACTION)) {

                    String action = data.getStringExtra(ApiConstants.FRAGMENT_ACTIVITY_RESULT_ACTION);

                    if (action.equals(ApiConstants.MOVE)) {
                        dismissAttachmentDialog();
                        updateCurrentDocument = false;
                        String toLocation = data.getStringExtra(ApiConstants.FRAGMENT_ACTIVITY_RESULT_LOCATION);
                        String folderId = data.getStringExtra(ApiConstants.FRAGMENT_ACTIVITY_RESULT_FOLDERID);
                        executeDocumentMoveTask(DocumentContentStore.getDocumentParent(),null, toLocation, folderId);

                    } else if (action.equals(ApiConstants.DELETE)) {
                        dismissAttachmentDialog();
                        updateCurrentDocument = false;
                        deleteDocument(DocumentContentStore.getDocumentParent());
                    }
                }
            }else if(requestCode == DocumentFragment.INTENT_ID_PORTEN_WEBVIEW_LOGIN){
                currentListPosition = data.getExtras().getInt("currentListPosition");
                openUpdatedDocument(currentListPosition);
            }
        }

        if(updateCurrentDocument ){
            documentAdapter.replaceAtPosition(DocumentContentStore.getDocumentParent(),currentListPosition);
            updateCurrentDocument = false;
        }

        DocumentContentStore.clearContent();
    }

    private void showMoveToFolderDialog(ArrayList<Document> documents) {
        folderDialog = null;
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.generic_dialog_layout, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setNegativeButton(getString(R.string.abort),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finishActionMode();
                    }
                }
        );

        builder.setView(view);
        ListView moveToFolderListView = (ListView) view.findViewById(R.id.generic_dialog_listview);
        ArrayList<Folder> folders = getMoveFolders();
        folderAdapter = new FolderArrayAdapter(getActivity(), R.layout.generic_dialog_list_item, folders);
        moveToFolderListView.setAdapter(folderAdapter);
        moveToFolderListView.setOnItemClickListener(new MoveToFolderListOnItemClickListener(documents));

        builder.setTitle(getString(R.string.move_to));
        folderDialog = builder.create();
        folderDialog.show();
    }

    private ArrayList<Folder> getMoveFolders() {
        ArrayList<Folder> moveLocations = new ArrayList<>();

        if (MainContentActivity.folders != null) {
            if (MainContentActivity.fragmentName != null) {


                if (MainContentActivity.folders != null) {

                    //Mapper
                    ArrayList<Folder> folders = new ArrayList<>();
                    for (Folder f : MainContentActivity.folders) {
                        if (!MainContentActivity.fragmentName.equals(f.getName())) {
                            folders.add(f);
                        }
                    }

                    //Postkassen
                    if (folders.size() != MainContentActivity.folders.size()) {
                        Folder postkassen = new Folder();
                        postkassen.setName(getString(R.string.drawer_inbox));
                        moveLocations.add(0, postkassen);
                    }

                    moveLocations.addAll(folders);
                }
            }
            return moveLocations;
        } else {
            return null;
        }
    }

    private void showAttachmentDialog(final Document document) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.generic_dialog_layout, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setNegativeButton(getString(R.string.close),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        dismissUpdateProgressDialogIfExisting();
                    }
                }
        );
        builder.setView(view);

        ListView attachmentListView = (ListView) view.findViewById(R.id.generic_dialog_listview);
        attachmentAdapter = new AttachmentArrayAdapter(getActivity(), R.layout.generic_dialog_list_item, document.getAttachment());
        attachmentListView.setAdapter(attachmentAdapter);
        attachmentListView.setOnItemClickListener(new AttachmentListOnItemClickListener(document));

        builder.setTitle(attachmentAdapter.getMainSubject());
        attachmentDialog = builder.create();
        attachmentDialog.show();
    }

    private void dismissAttachmentDialog() {
        if (attachmentDialog != null) {
            attachmentDialog.dismiss();
        }
    }

    private void openListItem(final Document document) {
        if (document.requiresHighAuthenticationLevel()) {
            handleHighAuthenticationLevelDocument(document);
        } else if (document.getAttachment().size() == 1 && document.getAttachment().get(0).isUserKeyEncrypted()) {
            showUserKeyEncryptedDialog();
        } else if (document.getAttachment().size() == 1 && document.getAttachment().get(0).getOpeningReceiptUri() != null) {
            showOpeningReceiptDialog(document, document.getAttachment().get(0), 0);
        } else {
            findDocumentAttachments(document);
        }
    }

    private void handleHighAuthenticationLevelDocument(Document document){
        if (TokenStore.hasValidTokenForScope(document.getRequiredAuthenticationScope())){
            findDocumentAttachments(document);
        }else{
            openHighAuthenticationLevelDialog(document);
        }
    }

    private void openHighAuthenticationLevelDialog(final Document document){
        String title = getString(R.string.dialog_id_porten_title);
        String message = getString(R.string.dialog_id_porten_message);

        AlertDialog.Builder builder = DialogUtitities.getAlertDialogBuilderWithMessageAndTitle(context, message, title);

        builder.setPositiveButton(getString(R.string.dialog_id_porten_unlock), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                openHighAuthenticationWebView(document);
                if(dialog != null) dialog.dismiss();
            }
        }).setCancelable(false).setNegativeButton(getString(R.string.abort), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                if(dialog != null) dialog.cancel();
                dismissUpdateProgressDialogIfExisting();
            }
        });

        builder.create().show();
    }

    private void openHighAuthenticationWebView(Document document){
        if (NetworkUtilities.isOnline()) {
            Intent i = new Intent(getActivity(), WebLoginActivity.class);
            i.putExtra("authenticationScope", document.getRequiredAuthenticationScope().asApiConstant());
            i.putExtra("currentListPosition", currentListPosition);
            startActivityForResult(i, DocumentFragment.INTENT_ID_PORTEN_WEBVIEW_LOGIN);
        } else {
            DialogUtitities.showToast(context, getString(R.string.error_your_network));
        }
    }

    private void findDocumentAttachments(final Document document) {
        ArrayList<Attachment> attachments = document.getAttachment();
        if (attachments.size() > 1) {
            showAttachmentDialog(document);
        } else {
            Attachment attachment = document.getAttachment().get(0);
            if (TokenStore.hasValidTokenForScope(document.getRequiredAuthenticationScope())) {
                getAttachmentContent(document, 0, attachment);
            }
        }
    }

    private void sendOpeningReceipt(final Document document, final Attachment attachment, int attachmentPosition) {
        SendOpeningReceiptTask task = new SendOpeningReceiptTask(document, attachment, attachmentPosition);
        task.execute();
    }

    private void showOpeningReceiptDialog(final Document document, final Attachment attachment, final int attachmentPosition) {

        String title = getString(R.string.dialog_opening_receipt_title);
        String message = getString(R.string.dialog_opening_receipt_message);

        AlertDialog.Builder builder = DialogUtitities.getAlertDialogBuilderWithMessageAndTitle(context, message, title);

        builder.setPositiveButton(getString(R.string.dialog_opening_receipt_yes), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                sendOpeningReceipt(document, attachment, attachmentPosition);
                dialog.dismiss();
            }
        }).setCancelable(false).setNegativeButton(getString(R.string.abort), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.cancel();
                dismissUpdateProgressDialogIfExisting();
            }
        });

        builder.create().show();
    }

    private void showUserKeyEncryptedDialog() {
        String title = getString(R.string.dialog_error_user_key_encrypted_title);
        String message = getString(R.string.dialog_error_user_key_encrypted_message);

        AlertDialog.Builder builder = DialogUtitities.getAlertDialogBuilderWithMessageAndTitle(context, message, title);

        builder.setNegativeButton(R.string.abort, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                dismissUpdateProgressDialogIfExisting();
            }
        });

        builder.create().show();
    }

    private void openAttachmentContent(final Attachment attachment) {
        String fileType = attachment.getFileType();
        Class nextActivity = getActivityFromFiletype(fileType);

        Bundle bundle = new Bundle();
        if (attachment.getType().equals(ApiConstants.INVOICE) && attachment.getInvoice() != null) {
            bundle.putBoolean(INTENT_SEND_TO_BANK, true);
        }
        bundle.putInt(INTENT_CONTENT, getContent());
        bundle.putBoolean(INTENT_ATTACHMENT_IS_SENSITIVE, attachment.requiresHighAuthenticationLevel());
        if (attachment.requiresHighAuthenticationLevel()) {
            FingerprintActivity.Companion.startActivityWithFingerprint(context, nextActivity, attachment.getRequiredAuthenticationScope().getLevel(), getString(R.string.fingerprint_open_secure_document), bundle);
        } else {
            Intent intent = new Intent(context, nextActivity);
            intent.putExtras(bundle);
            startActivityForResult(intent, DocumentFragment.INTENT_OPEN_ATTACHMENT_CONTENT);
        }
    }

    private Class<?> getActivityFromFiletype(String fileType) {
        if (ApiConstants.FILETYPE_PDF.equals(fileType)) {
            return MuPDFActivity.class;
        } else if (ApiConstants.FILETYPE_HTML.equals(fileType)) {
            return HtmlAndReceiptActivity.class;
        } else if (asList(ApiConstants.INSTANCE.getFILETYPES_IMAGE()).contains(fileType)) {
            return ImageActivity.class;
        } else {
            return UnsupportedDocumentFormatActivity.class;
        }
    }

    private class OpenUpdatedDocumentTask extends AsyncTask<Void, Void, Boolean> {

        private Document document;

        private OpenUpdatedDocumentTask(final Document document){
            this.document = document;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showUpdateProgressDialog();
        }

        @Override
        protected Boolean doInBackground(final Void... params) {
            try {
                this.document = ContentOperations.getDocumentSelf(getActivity(), document);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if(success){
                openListItem(this.document);
            } else {
                dismissUpdateProgressDialogIfExisting();
                DialogUtitities.showLongToast(context, getString(R.string.error_failed_to_open_with_intent));
            }
        }
    }

    private void showUpdateProgressDialog() {
        dismissUpdateProgressDialogIfExisting();

        if(updateProgressDialog == null){
            updateProgressDialog = DialogUtitities.getProgressDialogWithMessage(context, getString(R.string.loading_content));
        }

        updateProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.abort), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int which) {
                dismissUpdateProgressDialogIfExisting();
                openAttachment = false;

                if (asyncHttpClient != null) {
                    asyncHttpClient.cancelRequests(context, true);
                }
            }
        });

        updateProgressDialog.show();
    }

    private void dismissUpdateProgressDialogIfExisting(){
        if(updateProgressDialog != null) {
            updateProgressDialog.dismiss();
            updateProgressDialog = null;
        }
    }

    private void getAttachmentContent(final Document parentDocument, final int attachmentListPosition, final Attachment attachment) {
        if (parentDocument != null && attachment != null) {
            asyncHttpClient = new AsyncHttpClient();
            asyncHttpClient.addHeader(HttpHeaders.USER_AGENT, DigipostApplication.USER_AGENT);
            asyncHttpClient.addHeader(ApiConstants.ACCEPT, ApiConstants.CONTENT_OCTET_STREAM);
            asyncHttpClient.addHeader(ApiConstants.AUTHORIZATION, ApiConstants.BEARER + TokenStore.getAccessTokenForScope(parentDocument.getRequiredAuthenticationScope()));
            asyncHttpClient.get(context, attachment.getContentUri(), new AsyncHttpResponseHandler() {

                @Override
                public void onStart() {
                    super.onStart();
                    openAttachment = true;
                }

                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                    if (openAttachment) {
                        parentDocument.markAsRead();

                        DocumentContentStore.setContent(responseBody, parentDocument, attachmentListPosition);
                        DocumentContentStore.setMoveFolders(getMoveFolders());
                        openAttachmentContent(attachment);

                        ArrayList<Attachment> attachments = parentDocument.getAttachment();

                        if (attachments.size() > 1) {
                            attachmentAdapter.setAttachments(attachments);
                        }

                        activityCommunicator.onUpdateAccountMeta();
                    }
                }


                @Override
                public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                    openAttachment = false;
                    String errorMessage = getString(R.string.error_digipost_api);

                    if (statusCode == NetworkUtilities.HTTP_STATUS_UNAUTHORIZED) {
                        errorMessage = getString(R.string.error_invalid_token);
                    } else if (statusCode == NetworkUtilities.HTTP_STATUS_BAD_REQUEST) {
                        errorMessage = getString(R.string.error_bad_request);
                    } else if (statusCode == NetworkUtilities.HTTP_STATUS_INTERNAL_SERVER_ERROR) {
                        errorMessage = getString(R.string.error_digipost_api);
                    }

                    DialogUtitities.showToast(DocumentFragment.this.getActivity(), errorMessage);
                    updateAccountMeta(true);
                }

                @Override
                public void onCancel() {
                    super.onCancel();
                    openAttachment = false;
                }

                @Override
                public void onFinish() {
                    super.onFinish();
                    dismissUpdateProgressDialogIfExisting();
                }
            });
        }
    }

    @Override
    public void updateAccountMeta(boolean clearContent) {
        if (getContent() > ApplicationConstants.numberOfStaticFolders) {
            if (getContent() - ApplicationConstants.numberOfStaticFolders == MainContentActivity.numberOfFolders) {
                content = ApplicationConstants.MAILBOX;
            }
        }

        if(documentAdapter==null)setupNewDocumentAdapter();
        String unixTimeOfNextDocument = documentAdapter.getGetUnixTimeOfNextDocument();
        GetDocumentMetaTask task = new GetDocumentMetaTask(getContent(),clearContent, unixTimeOfNextDocument);
        task.execute();
    }

    private void setEmptyViewText() {
        int contentType = getContent();
        int textResource = contentType == ApplicationConstants.MAILBOX ? R.string.emptyview_mailbox : R.string.emptyview_folder;
        setListEmptyViewText(null, getString(textResource));
    }

    private void executeDocumentMoveTask(Document document, ArrayList<Document> documents, String toLocation, String folderId) {

        if (document != null) {
            documents = new ArrayList<>();
            documents.add(document);
        }
        finishActionMode();
        DocumentMoveTask documentMoveTask = new DocumentMoveTask(documents, toLocation, folderId);
        documentMoveTask.execute();
    }

    private void deleteDocument(Document document) {
        List<Document> documents = new ArrayList<>();
        documents.add(document);
        ContentDeleteTask contentDeleteTask = new ContentDeleteTask(documents);
        contentDeleteTask.execute();
    }
        
    private void openUpdatedDocument(int position){
        Document document = documentAdapter.getItem(position);
        new OpenUpdatedDocumentTask(document).execute();
    }

    private void openUpdatedDocument(Document document){
        new OpenUpdatedDocumentTask(document).execute();
    }

    private class MoveToFolderListOnItemClickListener implements AdapterView.OnItemClickListener {
        private ArrayList<Document> documents;
        private MoveToFolderListOnItemClickListener(ArrayList<Document> documents) {
            this.documents = documents;
        }

        public void onItemClick(final AdapterView<?> arg0, final View arg1, final int position, final long arg3) {
            Folder folder = folderAdapter.getItem(position);
            int folderId = folder.getId();
            String location;

            if (folderId == 0) {
                location = "INBOX";
            } else {
                location = "FOLDER";
            }

            executeDocumentMoveTask(null, documents, location, ""+folderId);
            if (folderDialog != null) {
                folderDialog.dismiss();
                folderDialog = null;
            }
        }
    }

    private class AttachmentListOnItemClickListener implements AdapterView.OnItemClickListener {
        private Document parentDocument;

        private AttachmentListOnItemClickListener(Document parentDocument) {
            this.parentDocument = parentDocument;
        }

        public void onItemClick(final AdapterView<?> arg0, final View arg1, final int position, final long arg3) {
            Attachment attachment = attachmentAdapter.getItem(position);
            if (attachment.isUserKeyEncrypted()) {
                showUserKeyEncryptedDialog();
            } else if (attachment.getOpeningReceiptUri() != null) {
                showOpeningReceiptDialog(parentDocument, attachment, position);
            } else {
                getAttachmentContent(parentDocument, position, attachment);
            }
        }
    }

    private class GetDocumentMetaTask extends AsyncTask<Void, Void, Documents> {
        private final int content;
        private String errorMessage;
        private boolean invalidToken;
        private boolean clearContent;
        private String unixTimeOfNextDocument;

        private GetDocumentMetaTask(final int content, final boolean clearContent, final String unixTimeOfNextDocument) {
            this.content = content;
            this.clearContent = clearContent;
            this.unixTimeOfNextDocument = unixTimeOfNextDocument;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            activityCommunicator.onStartRefreshContent();
        }

        @Override
        protected Documents doInBackground(final Void... params) {
            try {
                return ContentOperations.getAccountContentMetaDocument(context, content, unixTimeOfNextDocument);
            } catch (DigipostApiException | DigipostClientException e) {
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
        protected void onPostExecute(final Documents newDocuments) {
            super.onPostExecute(newDocuments);
            DocumentFragment.super.hideBackgroundLoadingSpinner();

            if(clearContent){
                documentAdapter.clearExistingContent();
            }

            if (isAdded()) {
                if(listEmptyViewImage != null) listEmptyViewImage.setVisibility(View.GONE);
                DocumentFragment.super.taskIsRunning = false;
                if (newDocuments != null) {
                    ArrayList<Document> documents = newDocuments.getDocument();
                    documentAdapter.updateContent(documents);
                    if (documents != null && !documents.isEmpty()) {
                        DocumentFragment.super.setListEmptyViewNoNetwork(false);
                    } else {
                        setEmptyViewText();
                    }
                } else {
                    if (invalidToken) {
                        activityCommunicator.requestLogOut();
                    }
                    DocumentFragment.super.setListEmptyViewNoNetwork(true);
                    DialogUtitities.showToast(DocumentFragment.this.context, errorMessage);
                }
                loadingMoreContent = false;
                activityCommunicator.onUpdateAccountMeta();
                activityCommunicator.onEndRefreshContent();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            loadingMoreContent = false;
            activityCommunicator.onEndRefreshContent();
        }
    }

    private class DocumentMoveTask extends AsyncTask<Void, Document, String> {
        private List<Document> documents;
        private String toLocation;
        private String folderId;
        private boolean invalidToken;
        private int progress;

        private DocumentMoveTask(List<Document> documents, String toLocation, String folderId) {
            this.documents = documents;
            this.toLocation = toLocation;
            this.folderId = folderId;
            this.progress = 0;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DocumentFragment.super.showContentProgressDialog(this, "Flytter");
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                for (Document document : documents) {
                    if (!isCancelled()) {
                        publishProgress(document);
                        progress++;
                        document.setLocation(toLocation);
                        document.setFolderId(folderId);
                        ContentOperations.moveDocument(context, document);
                    }
                }

                return null;
            } catch (DigipostAuthenticationException e) {
                Log.e(getClass().getName(), e.getMessage(), e);
                invalidToken = true;
                return e.getMessage();
            } catch (DigipostApiException | DigipostClientException e) {
                Log.e(getClass().getName(), e.getMessage(), e);
                return e.getMessage();
            }
        }

        @Override
        protected void onProgressUpdate(Document... values) {
                super.onProgressUpdate(values);

            DocumentFragment.super.progressDialog.setMessage("Flytter " + values[0].getSubject() + " (" + progress + "/" + documents.size()
                    + ")");
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            DocumentFragment.super.hideProgressDialog();
            DialogUtitities.showToast(context, progress + " av " + documents.size() + " ble flyttet.");
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (isAdded()) {
                DocumentFragment.super.taskIsRunning = false;
                DocumentFragment.super.hideProgressDialog();

                if (result != null) {
                    DialogUtitities.showToast(context, result);

                    if (invalidToken) {
                        activityCommunicator.requestLogOut();
                    }
                } else {
                    dismissAttachmentDialog();
                }
            }
            refreshItems();
        }
    }

    private class SendOpeningReceiptTask extends AsyncTask<Void, Void, Boolean> {
        private String errorMessage;
        private Document document;
        private Attachment attachment;
        private boolean invalidToken;
        private int attachmentPosition;

        private SendOpeningReceiptTask(final Document document, final Attachment attachment,int attachmentPosition) {
            invalidToken = false;
            this.document = document;
            this.attachment = attachment;
            this.attachmentPosition = attachmentPosition;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!DocumentFragment.super.progressDialogIsVisible) {
                DocumentFragment.super.showContentProgressDialog(this, context.getString(R.string.loading_content));
            }
        }

        @Override
        protected Boolean doInBackground(final Void... params) {
            try {
                document = (Document) JSONUtilities.processJackson(Document.class, ContentOperations.sendOpeningReceipt(context, attachment));
                attachment = document.getAttachment().get(attachmentPosition);
                return true;
            } catch (DigipostApiException | DigipostClientException e) {
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
            DocumentFragment.super.taskIsRunning = false;
            DocumentFragment.super.progressDialogIsVisible = false;
            DocumentFragment.super.hideProgressDialog();
            DocumentContentStore.clearContent();
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            super.onPostExecute(result);
            DocumentFragment.super.taskIsRunning = false;
            DocumentFragment.super.progressDialogIsVisible = false;
            DocumentFragment.super.hideProgressDialog();
            if (isAdded()) {
                if (result) {
                    getAttachmentContent(document, attachmentPosition, attachment);
                } else {
                    if (invalidToken) {
                        activityCommunicator.requestLogOut();
                    }
                    DialogUtitities.showToast(DocumentFragment.this.getActivity(), errorMessage);
                }
            }
        }
    }
}