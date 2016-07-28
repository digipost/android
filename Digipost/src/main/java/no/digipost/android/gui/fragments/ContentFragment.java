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
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.util.List;

import no.digipost.android.R;
import no.digipost.android.api.ContentOperations;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.gui.adapters.ContentArrayAdapter;
import no.digipost.android.model.Document;
import no.digipost.android.model.Receipt;
import no.digipost.android.utilities.DataFormatUtilities;
import no.digipost.android.utilities.DialogUtitities;
import no.digipost.android.utilities.FileUtilities;
import no.digipost.android.utilities.SettingsUtilities;

import static java.lang.String.format;

public abstract class ContentFragment<CONTENT_TYPE> extends Fragment {
    public static final String INTENT_CONTENT = "content";
    public static final String INTENT_SEND_TO_BANK = "sendToBank";

    ActivityCommunicator activityCommunicator;

    protected Context context;

    //protected ListView listView;
    protected RecyclerView recyclerView;
    protected View listEmptyViewNoConnection;
    protected View listEmptyViewNoContent;
    protected TextView listEmptyViewTitle;
    protected TextView listEmptyViewText;

    protected TextView listTopText;
    //protected ContentArrayAdapter<CONTENT_TYPE> listAdapter;
    protected ProgressDialog progressDialog;
    protected boolean progressDialogIsVisible = false;
    protected boolean taskIsRunning = false;
    protected ActionMode contentActionMode;


    public abstract int getContent();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        View view = inflater.inflate(R.layout.fragment_layout_listview, container, false);
        Button networkRetryButton = (Button) view.findViewById(R.id.fragment_content_network_retry_button);
        networkRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateAccountMeta();
            }
        });

        /*
        listEmptyViewNoConnection = view.findViewById(R.id.fragment_content_list_emptyview_no_connection);
        listEmptyViewNoContent = view.findViewById(R.id.fragment_content_list_no_content);
        listEmptyViewTitle = (TextView) view.findViewById(R.id.fragment_content_list_emptyview_title);
        listEmptyViewText = (TextView) view.findViewById(R.id.fragment_content_list_emptyview_text);
        listTopText = (TextView) view.findViewById(R.id.fragment_content_listview_top_text);


        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setFastScrollEnabled(true);
        */

        return view;
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

    protected void setListEmptyViewText(String title, String text) {
        /*

        //TODO ListView
        if (title != null) {
            listEmptyViewTitle.setText(title);
        }
        if (text != null) {
            listEmptyViewText.setText(text);
        }
        listView.setEmptyView(listEmptyViewNoContent);
        */
    }

    protected void setTopText(String text) {
        listTopText.setVisibility(View.VISIBLE);
        listTopText.setText(text);
    }

    protected void setListEmptyViewNoNetwork(boolean visible) {
        /*
        //TODO replace
        if (visible) {
            listView.setEmptyView(listEmptyViewNoConnection);
        } else {
            listView.setEmptyView(null);
        }
        */
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
        AlertDialog.Builder alertDialogBuilder = DialogUtitities.getAlertDialogBuilderWithMessageAndTitle(context,
                getActionDeletePromtString(content.size()), getString(R.string.delete));
        alertDialogBuilder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                executeContentDeleteTask(content);
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

    private String getActionDeletePromtString(int count) {
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
                Log.e(getClass().getName(), e.getMessage(), e);
                return e.getMessage();
            } catch (DigipostClientException e) {
                Log.e(getClass().getName(), e.getMessage(), e);
                return e.getMessage();
            } catch (DigipostAuthenticationException e) {
                Log.e(getClass().getName(), e.getMessage(), e);
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
            updateAccountMeta();
        }

        @Override
        protected void onPostExecute(final String result) {
            super.onPostExecute(result);
            if(isAdded()) {
                taskIsRunning = false;
                if (result != null) {
                    DialogUtitities.showToast(context, result);

                    if (invalidToken) {
                        activityCommunicator.requestLogOut();
                    }
                }

                hideProgressDialog();
                updateAccountMeta();
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

    public void filterList(String filterQuery) {
        /*
        //TODO Replace
        listAdapter.getFilter().filter(filterQuery);
        */
    }

    public void clearFilter() {
        /*
        TODO Replace
        listAdapter.clearFilter();
         */
    }

    public abstract void updateAccountMeta();

    public interface ActivityCommunicator {
        void onStartRefreshContent();

        void onEndRefreshContent();

        void requestLogOut();

        void onUpdateAccountMeta();
    }
}
