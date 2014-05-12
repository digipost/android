package no.digipost.android.gui.fragments;

import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import no.digipost.android.R;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.constants.ApplicationConstants;

public class FolderFragment extends DocumentFragment {
    private int content = 0;

    public FolderFragment(int content) {
        this.content = content;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        super.listView.setMultiChoiceModeListener(new MultiChoiceModeListener());
        return view;
    }

    @Override
    public int getContent() {
        return content;
    }

    private class MultiChoiceModeListener extends DocumentMultiChoiceModeListener {

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            super.onCreateActionMode(actionMode, menu);

            MenuItem toArchive = menu.findItem(R.id.main_context_menu_archive);
            toArchive.setVisible(false);

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, android.view.MenuItem menuItem) {
            super.onActionItemClicked(actionMode, menuItem);

            if(menuItem.getItemId() == ApplicationConstants.MAILBOX){
                   moveDocument(ApiConstants.LOCATION_WORKAREA);
            }

            return true;
        }
    }
}
