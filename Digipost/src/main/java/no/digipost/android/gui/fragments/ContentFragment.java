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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.*;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

import no.digipost.android.R;
import no.digipost.android.api.ContentOperations;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.gui.recyclerview.*;
import no.digipost.android.model.Attachment;
import no.digipost.android.model.Document;
import no.digipost.android.model.Receipt;
import no.digipost.android.utilities.*;

import static java.lang.String.format;

public abstract class ContentFragment<CONTENT_TYPE> extends Fragment {
    public static final String INTENT_CONTENT = "content";
    public static final String INTENT_SEND_TO_BANK = "sendToBank";

    ActivityCommunicator activityCommunicator;

    protected Context context;

    protected RecyclerViewEmptySupport recyclerView;
    protected SwipeRefreshLayoutWithEmpty swipeRefreshLayout;
    protected View listEmptyViewNoConnection;
    protected View listEmptyViewNoContent;
    protected TextView listEmptyViewTitle;
    protected TextView listEmptyViewText;
    protected ImageView listEmptyViewImage;
    protected TextView listTopText;
    protected ProgressDialog progressDialog;
    protected boolean progressDialogIsVisible = false;
    protected boolean taskIsRunning = false;
    protected ActionMode contentActionMode;
    protected View spinnerLayout;
    public static boolean activityDrawerOpen;
    protected boolean loadingMoreContent = false;
    public abstract int getContent();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        View view = inflater.inflate(R.layout.fragment_layout_listview, container, false);
        Button networkRetryButton = (Button) view.findViewById(R.id.fragment_content_network_retry_button);
        networkRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateAccountMeta(true);
            }
        });

        spinnerLayout = (View) view.findViewById(R.id.fragment_content_spinner_layout);
        recyclerView = (RecyclerViewEmptySupport) view.findViewById(R.id.fragment_content_recyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(context));
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(context, recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                recyclerViewOnClick(position);
            }
            @Override
            public void onLongClick(View view, int position) {
                recyclerViewOnLongClick(position);
            }
        }));

        swipeRefreshLayout = (SwipeRefreshLayoutWithEmpty) view.findViewById(R.id.fragment_content_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayoutWithEmpty.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadingMoreContent = true;
                refreshItems();
            }
        });

        listEmptyViewNoContent = view.findViewById(R.id.fragment_content_list_no_content);
        listEmptyViewText = (TextView) view.findViewById(R.id.fragment_content_list_emptyview_text);
        listEmptyViewTitle = (TextView) view.findViewById(R.id.fragment_content_list_emptyview_title);
        listEmptyViewNoConnection = view.findViewById(R.id.fragment_content_list_emptyview_no_connection);
        listEmptyViewImage = (ImageView) view.findViewById(R.id.fragment_content_list_emptyview_image);
        listTopText = (TextView) view.findViewById(R.id.fragment_content_listview_top_text);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setEmptyView(listEmptyViewNoContent);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int totalItem = layoutManager.getItemCount();
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                if (!loadingMoreContent && lastVisibleItem == totalItem - 1) {
                    loadingMoreContent = true;
                    loadMoreContent();
                }
            }
        });

        refreshItems();

        return view;
    }


    abstract void recyclerViewOnClick(int position);

    abstract void recyclerViewOnLongClick(int position);

    public abstract void finishActionMode();
    public abstract void loadMoreContent();
    public abstract void clearExistingContent();

    public void refreshItems() {
        showBackgroundLoadingSpinner();
        clearExistingContent();
        updateAccountMeta(true);
        activityCommunicator.onStartRefreshContent();
        onItemsLoadComplete();
    }

    void onItemsLoadComplete() {
        activityCommunicator.onEndRefreshContent();
        if(contentActionMode != null) contentActionMode.finish();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activityCommunicator = (ActivityCommunicator) activity;
    }

    @Override
    public void onResume() {
        super.onResume();
        FileUtilities.deleteTempFiles();
    }

    protected void hideBackgroundLoadingSpinner(){
        spinnerLayout.setVisibility(View.GONE);
        swipeRefreshLayout.setVisibility(View.VISIBLE);
    }

    protected void showBackgroundLoadingSpinner(){
        spinnerLayout.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setVisibility(View.GONE);
    }

    protected void setListEmptyViewText(String title, String text) {
        if(title == null) {
            listEmptyViewTitle.setText("");
            listEmptyViewText.setText(text);
            listEmptyViewImage.setVisibility(View.VISIBLE);
        }else{
            if(text != null) listEmptyViewText.setText(text);
            listEmptyViewImage.setVisibility(View.GONE);
        }
    }

    protected void clearEmptyTextView(){
        listEmptyViewTitle.setText("");
        listEmptyViewText.setText("");
    }

    protected void setTopText(String text) {
        listTopText.setVisibility(View.VISIBLE);
        listTopText.setText(text);
    }

    protected void setListEmptyViewNoNetwork(boolean visible) {
        if(visible) {
            listEmptyViewNoConnection.setVisibility(View.VISIBLE);
        }else{
            listEmptyViewNoConnection.setVisibility(View.GONE);
        }

    }

    private void executeContentDeleteTask(List<CONTENT_TYPE> content) {
        ContentDeleteTask documentDeleteTask = new ContentDeleteTask(content);
        documentDeleteTask.execute();
    }

    protected void deleteContent(List<CONTENT_TYPE> content) {
        if (SettingsUtilities.getConfirmDeletePreference(context)) {
            showDeleteContentDialog(content);
        } else {
            executeContentDeleteTask(content);
        }
    }

    private void showDeleteContentDialog(final List<CONTENT_TYPE> content) {
        String dialogTitle = getString(R.string.delete);
        String deleteButtonText = getString(R.string.delete);
        String cancelButtonText = getString(R.string.abort);
        String messageText = getActionDeletePromptString(content.size());

        if(SharedPreferencesUtilities.gotAnyBankAgreements(context) && getContent() != ApplicationConstants.RECEIPTS){
            if(numberOfInvoices(content) > 0){
                int numberOfFiles = content.size();
                String filesText = numberOfFiles +" "+ (numberOfFiles == 1 ? getString(R.string.invoice_delete_file_single) : getString(R.string.invoice_delete_file_plural));
                int numberOfInvoices = numberOfInvoices(content);
                String invoicesText = numberOfInvoices +" "+ (numberOfInvoices == 1 ? getString(R.string.invoice_delete_invoice_single) : getString(R.string.invoice_delete_invoice_plural));
                messageText = format(getString(R.string.invoice_delete_multiple_files_including_n_invoices),filesText, invoicesText);
            }
        }

        AlertDialog.Builder alertDialogBuilder = DialogUtitities.getAlertDialogBuilderWithMessageAndTitle(context, messageText, dialogTitle);

        alertDialogBuilder.setPositiveButton(deleteButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finishActionMode();
                executeContentDeleteTask(content);
                dialogInterface.dismiss();
            }
        });

        alertDialogBuilder.setNegativeButton(cancelButtonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finishActionMode();
                dialogInterface.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private int numberOfInvoices(final List<CONTENT_TYPE> content){
        int numberOfInvoices = 0;
        for (CONTENT_TYPE object : content){
            Document document = (Document) object;
            ArrayList<Attachment> attachments = document.getAttachment();
            for(Attachment attachment : attachments){
                if(attachment.getInvoice() != null){
                    numberOfInvoices += 1;
                }
            }
        }

        return numberOfInvoices;
    }

    private String getContentTypeString(int count) {
        int res;
        if (getContent() == ApplicationConstants.RECEIPTS) {
            if (count > 1) {
                res = R.string.receipt_plural;
            } else {
                res = R.string.receipt_singular;
            }
        } else {
            if (count > 1) {
                res = R.string.document_plural;
            } else {
                res = R.string.document_singular;
            }
        }
        return getString(res);
    }

    private String getActionDeletePromptString(int count) {
        String type = getContentTypeString(count);

        if (count > 1) {
            return format(getString(R.string.delete_multiple), count, type);
        }

        String pronomen = getContent() == ApplicationConstants.RECEIPTS ? "denne" : "dette";
        return format(getString(R.string.delete_single), pronomen, type);
    }

    protected class ContentDeleteTask extends AsyncTask<Void, CONTENT_TYPE, String> {
        private List<CONTENT_TYPE> content;
        private boolean invalidToken;
        private int progress;

        protected ContentDeleteTask(List<CONTENT_TYPE> content) {
            this.content = content;
            this.progress = 0;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showContentProgressDialog(this, "");
        }

        @Override
        protected String doInBackground(final Void... params) {
            try {
                for (CONTENT_TYPE object : content) {
                    if (!isCancelled()) {
                        publishProgress(object);
                        progress++;
                        ContentOperations.deleteContent(context, object);
                    }
                }

                return null;
            } catch (DigipostApiException e) {
                return e.getMessage();
            } catch (DigipostClientException e) {
                return e.getMessage();
            } catch (DigipostAuthenticationException e) {
                invalidToken = true;
                return e.getMessage();
            }
        }

        @Override
        protected void onProgressUpdate(CONTENT_TYPE... values) {
            super.onProgressUpdate(values);

            if (values[0] instanceof Document) {
                Document document = (Document) values[0];
                progressDialog.setMessage(format(getString(R.string.delete_progress_document), document.getSubject(),
                        progress, content.size()));
            } else if (values[0] instanceof Receipt) {
                Receipt receipt = (Receipt) values[0];
                progressDialog.setMessage(format(getString(R.string.delete_progress_receipt), receipt.getStoreName(),
                        DataFormatUtilities.getFormattedDateTime(receipt.getTimeOfPurchase()), progress, content.size()));
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            taskIsRunning = false;
            DialogUtitities.showToast(context, format(getString(R.string.delete_cancelled), progress, content.size()));
            hideProgressDialog();
        }

        @Override
        protected void onPostExecute(final String result) {
            super.onPostExecute(result);
            if (isAdded()) {
                taskIsRunning = false;
                if (result != null) {
                    DialogUtitities.showToast(context, result);

                    if (invalidToken) {
                        activityCommunicator.requestLogOut();
                    }
                }
                hideProgressDialog();
                refreshItems();
            }
        }
    }

    protected void showContentProgressDialog(final AsyncTask task, String message) {
        taskIsRunning = true;
        progressDialog = DialogUtitities.getProgressDialogWithMessage(context, message);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.abort), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int which) {
                dialog.dismiss();
                taskIsRunning = false;
                task.cancel(true);
            }
        });
        progressDialog.show();
    }

    protected void hideProgressDialog() {
        if(isAdded()) {
            if (!taskIsRunning) {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                    progressDialog = null;
                }
            }
        }
    }

    public abstract void updateAccountMeta(boolean clearContent);

    public interface ActivityCommunicator {
        void onStartRefreshContent();

        void onEndRefreshContent();

        void requestLogOut();

        void onUpdateAccountMeta();
    }
}
