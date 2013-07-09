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

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
        super.listAdapter = new ReceiptArrayAdapter(getActivity(), R.layout.content_list_item, new CheckBoxOnClickListener());
        super.listView.setAdapter(listAdapter);
        super.listView.setMultiChoiceModeListener(new ReceiptMultiChoiceModeListener());

        updateAccountMeta();

        return view;
    }

    public void updateAccountMeta(){
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
            activityCommunicator.onStartRefreshContent();
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
                ReceiptFragment.super.listAdapter.replaceAll(receipts);
            } else {
                DialogUtitities.showToast(ReceiptFragment.this.context, errorMessage);

                if (invalidToken) {
                    // ToDo logge ut
                }
            }

            activityCommunicator.onEndRefreshContent();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            activityCommunicator.onEndRefreshContent();
        }
    }

    private void deleteReceipt() {
        ArrayList<Receipt> receipts = super.listAdapter.getCheckedItems();

        ReceiptDeleteTask receiptDeleteTask = new ReceiptDeleteTask(receipts);
        receiptDeleteTask.execute();
    }

    private class ReceiptDeleteTask extends AsyncTask<Void, Integer, String> {
        private ArrayList<Receipt> receipts;
        private boolean invalidToken;

        public ReceiptDeleteTask(ArrayList<Receipt> receipts) {
            this.receipts = receipts;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ReceiptFragment.super.showContentProgressDialog(this, "");
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                int progress = 0;

                for (Receipt receipt : receipts) {
                    publishProgress(++progress);
                    ReceiptFragment.super.letterOperations.deleteReceipt(receipt);
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
            ReceiptFragment.super.progressDialog.setMessage(values[0] + " av " + receipts.size() + " slettet");
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            ReceiptFragment.super.hideProgressDialog();
            updateAccountMeta();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                DialogUtitities.showToast(context, result);

                if (invalidToken) {
                    // ToDo logge ut
                }
            }

            ReceiptFragment.super.hideProgressDialog();
            updateAccountMeta();
        }
    }

    private class ReceiptMultiChoiceModeListener extends ContentMultiChoiceModeListener {

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            super.onCreateActionMode(actionMode, menu);

            MenuItem moveDocument = menu.findItem(R.id.main_context_menu_folder);
            moveDocument.setVisible(false);

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, android.view.MenuItem menuItem) {
            super.onActionItemClicked(actionMode, menuItem);

            switch (menuItem.getItemId()) {
                case R.id.main_context_menu_delete:
                    deleteReceipt();
                    onFinishActionMode(actionMode);
                    break;
            }

            return true;
        }
    }
}
