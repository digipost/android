package no.digipost.android.gui.fragments;


import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import no.digipost.android.R;
import no.digipost.android.api.LetterOperations;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.gui.LetterArrayAdapter;
import no.digipost.android.model.Letter;

public abstract class DocumentFragment extends ContentFragment {

    protected LetterArrayAdapter letterAdapter;

    public DocumentFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        letterAdapter = new LetterArrayAdapter(getActivity(), R.layout.mailbox_list_item, new ArrayList<Letter>());
        super.listView.setAdapter(letterAdapter);
        return view;
    }

    protected void updateAccountMeta(int content){
        GetAccountMetaTask task = new GetAccountMetaTask(content);
        task.execute();
    }

    protected class GetAccountMetaTask extends AsyncTask<Void, Void, ArrayList<Letter>> {
        private final int content;
        private String errorMessage = "";

        public GetAccountMetaTask(final int content) {
            this.content = content;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<Letter> doInBackground(final Void... params) {
            try {
                return DocumentFragment.super.letterOperations.getAccountContentMeta(content);
            } catch (DigipostApiException e) {
                errorMessage = e.getMessage();
                return null;
            } catch (DigipostClientException e) {
                errorMessage = e.getMessage();
                return null;
            } catch (DigipostAuthenticationException e) {
                errorMessage = e.getMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute(final ArrayList<Letter> result) {
            super.onPostExecute(result);
            if(result != null){
                letterAdapter.updateList(result,"");
            }
        }
    }
}
