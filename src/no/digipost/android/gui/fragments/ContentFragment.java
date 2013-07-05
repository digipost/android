package no.digipost.android.gui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import no.digipost.android.R;
import no.digipost.android.api.LetterOperations;
import no.digipost.android.gui.adapters.ContentArrayAdapter;

public abstract class ContentFragment extends Fragment {
    protected ListView listView;
    protected ContentArrayAdapter listAdapter;
    protected LetterOperations letterOperations;

    public ContentFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_layout_listview, container, false);
        listView = (ListView) view.findViewById(R.id.fragment_content_listview);
        letterOperations = new LetterOperations(getActivity().getApplicationContext());

        return view;
    }

    public void filterList(String filterQuery) {
        listAdapter.getFilter().filter(filterQuery);
    }
}