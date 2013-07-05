package no.digipost.android.gui.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import no.digipost.android.R;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.gui.adapters.LetterArrayAdapter;
import no.digipost.android.gui.adapters.ReceiptArrayAdapter;
import no.digipost.android.model.Receipt;
import no.digipost.android.utilities.DialogUtitities;

public class ReceiptFragment extends ContentFragment{
    public ReceiptFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        super.listAdapter = new ReceiptArrayAdapter(getActivity(), R.layout.content_list_item);
        super.listView.setAdapter(listAdapter);

        updateAccountMeta();

        return view;
    }

    private void updateAccountMeta(){
        GetReceiptsMetaTask task = new GetReceiptsMetaTask();
        task.execute();
    }

    private class GetReceiptsMetaTask extends AsyncTask<Void, Void, ArrayList<Receipt>> {
        private String errorMessage;
        private boolean invalidToken;

        public GetReceiptsMetaTask() {
            errorMessage = "";
            invalidToken = false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ReceiptFragment.super.showProgressDialog(this);
        }

        @Override
        protected ArrayList<Receipt> doInBackground(final Void... params) {
            try {
                return ReceiptFragment.super.letterOperations.getAccountContentMetaReceipt();
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
        protected void onPostExecute(final ArrayList<Receipt> receipts) {
            super.onPostExecute(receipts);

            if (receipts != null) {
                ReceiptFragment.super.listAdapter.addAll(receipts);
            } else {
                DialogUtitities.showToast(ReceiptFragment.this.context, errorMessage);
            }

            ReceiptFragment.super.hideProgressDialog();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            ReceiptFragment.super.hideProgressDialog();
        }
    }
}
