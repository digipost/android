package no.digipost.android.gui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import no.digipost.android.R;
import no.digipost.android.api.LetterOperations;
import no.digipost.android.gui.adapters.ContentArrayAdapter;
import no.digipost.android.utilities.DialogUtitities;

public abstract class ContentFragment extends Fragment {
    ActivityCommunicator activityCommunicator;

    protected Context context;
    protected LetterOperations letterOperations;

    protected ListView listView;
    protected ContentArrayAdapter listAdapter;

    protected ProgressDialog progressDialog;

    public ContentFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        context = getActivity();
        letterOperations = new LetterOperations(context);

        View view = inflater.inflate(R.layout.fragment_layout_listview, container, false);
        listView = (ListView) view.findViewById(R.id.fragment_content_listview);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activityCommunicator = (ActivityCommunicator) activity;
    }

    protected void showProgressDialog(final AsyncTask task) {
        progressDialog = DialogUtitities.getProgressDialogWithMessage(context, context.getString(R.string.loading_content));
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.abort), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int which) {
                dialog.dismiss();
                task.cancel(true);
            }
        });
        progressDialog.show();
    }

    protected void hideProgressDialog() {
        progressDialog.dismiss();
        progressDialog = null;
    }

    public void filterList(String filterQuery) {
        listAdapter.getFilter().filter(filterQuery);
    }

    public abstract void updateAccountMeta();

    public interface ActivityCommunicator {
        public void onStartRefreshContent();
        public void onEndRefreshContent();
    }
}