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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.List;

import no.digipost.android.R;
import no.digipost.android.api.ContentOperations;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.documentstore.DocumentContentStore;
import no.digipost.android.gui.MainContentActivity;
import no.digipost.android.gui.adapters.ReceiptAdapter;
import no.digipost.android.gui.content.HtmlAndReceiptActivity;
import no.digipost.android.gui.recyclerview.*;
import no.digipost.android.model.Receipt;
import no.digipost.android.model.Receipts;
import no.digipost.android.utilities.DialogUtitities;

public class ReceiptFragment extends ContentFragment<Receipt> {

    protected ReceiptAdapter receiptAdapter;
    protected boolean multiSelectEnabled;
    protected int currentListPosition;

    public static ReceiptFragment newInstance() {
        return new ReceiptFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        receiptAdapter = new ReceiptAdapter(context, new ArrayList<Receipt>());
        recyclerView.setAdapter(receiptAdapter);
        return view;
    }

    @Override
    void recyclerViewOnClick(int position){
        currentListPosition = position;
        if(multiSelectEnabled){
            receiptAdapter.select(position);
        }else {
            openReceipt(receiptAdapter.getReceipts().get(position));
        }
    }

    @Override
    void recyclerViewOnLongClick(int position){
        currentListPosition = position;

        if(multiSelectEnabled) {
            receiptAdapter.select(position);
        }else{
            beginActionMode(position);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAccountMeta();
    }

    private void beginActionMode(int position){
        multiSelectEnabled = true;
        contentActionMode = getActivity().startActionMode(new ReceiptFragment.SelectActionModeCallback());
        receiptAdapter.setSelectable(multiSelectEnabled);
        receiptAdapter.select(position);
    }

    @Override
    public void finishActionMode(){
        multiSelectEnabled = false;
        contentActionMode = null;
        receiptAdapter.setSelectable(multiSelectEnabled);
    }

    private class SelectActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.activity_main_content_receipt_context, menu);
            return true;
        }
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.main_context_menu_delete_receipt:
                    ReceiptFragment.super.deleteContent(receiptAdapter.getSelected());
                    mode.finish();
                    finishActionMode();
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
    public int getContent() {
        return ApplicationConstants.RECEIPTS;
    }

    private void checkStatusAndDisplayReceipts(Receipts newReceipts) {
        if (isAdded()) {

            receiptAdapter.updateContent(newReceipts.getReceipt());
            int numberOfCards = Integer.parseInt(newReceipts.getNumberOfCards());
            int numberOfCardsReadyForVerification = Integer.parseInt(newReceipts.getNumberOfCardsReadyForVerification());
            int numberOfReceiptsHiddenUntilVerification = Integer.parseInt(newReceipts.getNumberOfReceiptsHiddenUntilVerification());

            if (receiptAdapter.getReceipts().size() == 0) {
                if (numberOfCards == 0) {
                    setListEmptyViewText(getString(R.string.emptyview_receipt_intro_title), getString(R.string.emptyview_receipt_intro_message));
                } else if (numberOfCardsReadyForVerification > 0) {
                    setListEmptyViewText(getString(R.string.emptyview_receipt_verification_title),
                            getString(R.string.emptyview_receipt_verification_message));
                } else {
                    setListEmptyViewText(getString(R.string.emptyview_receipt_registrated_title),
                            getString(R.string.emptyview_receipt_registrated_message));
                }
            } else {
                if (numberOfCards == 0) {

                    setTopText(getString(R.string.receipt_toptext_register_cards));
                } else if (numberOfReceiptsHiddenUntilVerification == 1) {

                    setTopText(getString(R.string.receipt_toptext_one_hidden_receipt));
                } else if (numberOfCardsReadyForVerification > 1) {

                    setTopText(String.format(getString(R.string.receipt_toptext_multiple_hidden_receipts), numberOfReceiptsHiddenUntilVerification));
                }
            }
        }
    }

    public void updateAccountMeta() {
        GetReceiptsMetaTask task = new GetReceiptsMetaTask();
        task.execute();
    }

    public void openReceipt(Receipt receipt){
        GetReceiptContentTask task = new GetReceiptContentTask(receipt);
        task.execute();
    }

    private void openReceiptHTMLContent(String receiptContent) {
        Intent intent = new Intent(getActivity(), HtmlAndReceiptActivity.class);
        intent.putExtra(INTENT_CONTENT, getContent());
        intent.putExtra(ApiConstants.GET_RECEIPT, receiptContent);
        startActivityForResult(intent, MainContentActivity.INTENT_REQUESTCODE);
    }

    private class GetReceiptContentTask extends AsyncTask<Void, Void, String> {
        private Receipt receipt;
        private String errorMessage;
        private boolean invalidToken;

        private GetReceiptContentTask(Receipt receipt) {
            this.receipt = receipt;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!ReceiptFragment.super.progressDialogIsVisible)
                ReceiptFragment.super.showContentProgressDialog(this, context.getString(R.string.loading_content));
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                return ContentOperations.getReceiptContentHTML(context, receipt);
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
        protected void onPostExecute(String receiptHTMLString) {
            super.onPostExecute(receiptHTMLString);
            if(isAdded()) {
                ReceiptFragment.super.taskIsRunning = false;
                ReceiptFragment.super.hideProgressDialog();

                if (receiptHTMLString != null) {
                    DocumentContentStore.setContent(receipt);
                    openReceiptHTMLContent(receiptHTMLString);
                } else {
                    if (invalidToken) {
                        activityCommunicator.requestLogOut();
                    }

                    DialogUtitities.showToast(ReceiptFragment.this.getActivity(), errorMessage);
                }
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            ReceiptFragment.super.taskIsRunning = false;
            ReceiptFragment.super.hideProgressDialog();
        }
    }

    private class GetReceiptsMetaTask extends AsyncTask<Void, Void, Receipts> {
        private String errorMessage;
        private boolean invalidToken;

        private GetReceiptsMetaTask() {
            errorMessage = "";
            invalidToken = false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            activityCommunicator.onStartRefreshContent();
        }

        @Override
        protected Receipts doInBackground(final Void... params) {
            try {
                return ContentOperations.getAccountContentMetaReceipt(context);
            } catch (DigipostApiException e) {
                errorMessage = e.getMessage();
                return null;
            } catch (DigipostClientException e) {
                errorMessage = e.getMessage();
                return null;
            } catch (DigipostAuthenticationException e) {
                errorMessage = e.getMessage();
                invalidToken = true;
                return null;
            }
        }

        @Override
        protected void onPostExecute(final Receipts receipts) {
            super.onPostExecute(receipts);
            if (receipts != null) {
                checkStatusAndDisplayReceipts(receipts);
            } else {
                DialogUtitities.showToast(ReceiptFragment.this.context, errorMessage);

                if (invalidToken) {
                    activityCommunicator.requestLogOut();
                } else if (receiptAdapter.isEmpty()) {
                    ReceiptFragment.super.setListEmptyViewNoNetwork(true);
                }
            }

            activityCommunicator.onUpdateAccountMeta();
            activityCommunicator.onEndRefreshContent();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            activityCommunicator.onEndRefreshContent();
        }
    }

    private void deleteReceipt(Receipt receipt) {
        List<Receipt> receiptsToBeDelete = new ArrayList<>();
        receiptsToBeDelete.add(receipt);
        ContentDeleteTask task = new ContentDeleteTask(receiptsToBeDelete);
        task.execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == MainContentActivity.INTENT_REQUESTCODE) {
                if(data.hasExtra(ApiConstants.FRAGMENT_ACTIVITY_RESULT_ACTION)) {
                    String action = data.getStringExtra(ApiConstants.FRAGMENT_ACTIVITY_RESULT_ACTION);

                    if (action.equals(ApiConstants.DELETE)) {
                        deleteReceipt(DocumentContentStore.getDocumentReceipt());
                    }
                }
            }
        }
    }
}
