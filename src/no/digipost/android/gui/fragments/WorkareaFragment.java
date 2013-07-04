package no.digipost.android.gui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import no.digipost.android.constants.ApplicationConstants;

public class WorkareaFragment extends DocumentFragment {
    public WorkareaFragment(){}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        super.updateAccountMeta(ApplicationConstants.WORKAREA);
        return view;
    }
}
