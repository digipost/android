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

import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.model.Letter;

public class ArchiveFragment extends DocumentFragment {
    public ArchiveFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        super.listView.setMultiChoiceModeListener(new ArchiveMultiChoiceModeListener());

        return view;
    }

    @Override
    public void updateAccountMeta() {
        GetDocumentMetaTask task = new GetDocumentMetaTask(ApplicationConstants.ARCHIVE);
        task.execute();
    }

    private class ArchiveMultiChoiceModeListener extends DocumentMultiChoiceModeListener {

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, android.view.MenuItem menuItem) {
            super.onActionItemClicked(actionMode, menuItem);

            switch (menuItem.getItemId()) {

            }

            ArchiveFragment.super.listAdapter.setCheckboxVisible(false);
            actionMode.finish();

            return true;
        }
    }
}
