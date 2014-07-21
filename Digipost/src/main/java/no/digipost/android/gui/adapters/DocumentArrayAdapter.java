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

package no.digipost.android.gui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.Collection;

import no.digipost.android.R;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.model.Document;
import no.digipost.android.utilities.DataFormatUtilities;
import no.digipost.android.utilities.SettingsUtilities;

public class DocumentArrayAdapter extends ContentArrayAdapter<Document> {

    public DocumentArrayAdapter(final Context context, final int resource) {
        super(context, resource, new ArrayList<Document>());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = super.getView(position, convertView, parent);

        Document document = super.filtered.get(position);

        super.title.setText(document.getSubject());

        if ("UPLOADED".equals(document.getOrigin())) {
            super.subTitle.setText(R.string.uploaded);
        } else {
            super.subTitle.setText(document.getCreatorName());
        }

        super.metaTop.setText(DataFormatUtilities.getFormattedDate(document.getCreated()));
        super.metaMiddle.setText(DataFormatUtilities.getFormattedFileSize(Long.parseLong(document.getFileSize())));

        if (!document.getRead().equals("true")) {
            row.setBackgroundResource(R.drawable.content_list_item_unread);
            super.setTitleAndSubTitleBold();
        }

        super.setFilterTextColor();

        setMetaBottom(document);

        return row;
    }

    @Override
    public void replaceAll(Collection<? extends Document> collection) {
        if (!SettingsUtilities.getShowBankIDLettersPreference(context)) {
            ArrayList<Document> documents = new ArrayList<Document>();

            for (Document document : collection) {
                if (!document.getAuthenticationLevel().equals(ApiConstants.AUTHENTICATION_LEVEL_TWO_FACTOR)) {
                    documents.add(document);
                }
            }

            super.replaceAll(documents);
        } else {
            super.replaceAll(collection);
        }
    }

    private void setMetaBottom(Document document) {

        if (document.getAttachment().size() > 1) {
            super.subTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.paperclip_32, 0);
        }

        if (super.hideContentTypeImage) {
            super.contentTypeImage.setVisibility(View.GONE);
        } else if (document.getAuthenticationLevel().equals(ApiConstants.AUTHENTICATION_LEVEL_TWO_FACTOR)) {
            super.contentTypeImage.setImageDrawable(context.getResources().getDrawable(R.drawable.lock_32));
        }
    }

    @Override
    public Filter getFilter() {
        return (super.contentFilter != null) ? super.contentFilter : new DocumentFilter();
    }

    private class DocumentFilter extends Filter {
        @Override
        protected FilterResults performFiltering(final CharSequence constraint) {
            FilterResults results = new FilterResults();
            ArrayList<Document> i = new ArrayList<Document>();

            DocumentArrayAdapter.super.titleFilterText = null;
            DocumentArrayAdapter.super.subTitleFilterText = null;
            DocumentArrayAdapter.super.metaTopFilterText = null;

            if ((constraint != null) && (constraint.toString().length() > 0)) {
                String constraintLowerCase = constraint.toString().toLowerCase();

                for (Document l : DocumentArrayAdapter.super.objects) {
                    boolean addDocument = false;

                    if (l.getSubject().toLowerCase().contains(constraintLowerCase)) {
                        DocumentArrayAdapter.super.titleFilterText = constraint.toString();
                        addDocument = true;
                    }

                    if (l.getCreatorName().toLowerCase().contains(constraintLowerCase)) {
                        DocumentArrayAdapter.super.subTitleFilterText = constraint.toString();
                        addDocument = true;
                    }

                    if (DataFormatUtilities.getFormattedDate(l.getCreated()).toLowerCase().contains(constraintLowerCase)) {
                        DocumentArrayAdapter.super.metaTopFilterText = constraint.toString();
                        addDocument = true;
                    }

                    if (addDocument) {
                        i.add(l);
                    }
                }

                results.values = i;
                results.count = i.size();
            } else {

                synchronized (DocumentArrayAdapter.super.objects) {
                    results.values = DocumentArrayAdapter.super.objects;
                    results.count = DocumentArrayAdapter.super.objects.size();
                }
            }

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(final CharSequence constraint, final FilterResults results) {
            filtered = (ArrayList<Document>) results.values;
            notifyDataSetChanged();
        }
    }
}
