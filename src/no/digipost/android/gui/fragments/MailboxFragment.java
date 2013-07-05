package no.digipost.android.gui.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import no.digipost.android.constants.ApplicationConstants;

public class MailboxFragment extends DocumentFragment {
    public MailboxFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        updateAccountMeta();

        return view;
    }

    public void updateAccountMeta() {
        super.updateAccountMeta(ApplicationConstants.MAILBOX);
    }
}
