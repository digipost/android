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

import no.digipost.android.R;
import no.digipost.android.model.Receipt;
import no.digipost.android.utilities.DataFormatUtilities;

public class ReceiptArrayAdapter extends ContentArrayAdapter<Receipt> {

    public ReceiptArrayAdapter(final Context context, final int resource) {
        super(context, resource, new ArrayList<Receipt>());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = super.getView(position, convertView, parent);

        Receipt receipt = super.filtered.get(position);

        super.title.setText(receipt.getStoreName());
        super.subTitle.setText(DataFormatUtilities.getFormattedAmount(receipt.getAmount()) + " "
                + DataFormatUtilities.getFormattedCurrency(receipt.getCurrency()));
        super.metaTop.setText(DataFormatUtilities.getFormattedDate(receipt.getTimeOfPurchase()));
        super.subTitle.setTextColor(context.getResources().getColor(R.color.green));
        super.setFilterTextColor();

        if (super.hideContentTypeImage) {
            super.contentTypeImage.setVisibility(View.GONE);
        }

        return row;
    }

    private String getCardsString(Receipt receipt) {
        ArrayList<String> cards = receipt.getCard();
        StringBuilder cardsString = new StringBuilder();

        for (int i = 0; i < cards.size(); i++) {
            cardsString.append(cards.get(i));

            if (i < (cards.size() - 1)) {
                cardsString.append(", ");
            }
        }

        return cardsString.toString();
    }

    @Override
    public Filter getFilter() {
        return (super.contentFilter != null) ? super.contentFilter : new ReceiptFilter();
    }

    private class ReceiptFilter extends Filter {
        @Override
        protected FilterResults performFiltering(final CharSequence constraint) {
            FilterResults results = new FilterResults();
            ArrayList<Receipt> i = new ArrayList<Receipt>();

            ReceiptArrayAdapter.super.titleFilterText = null;
            ReceiptArrayAdapter.super.subTitleFilterText = null;
            ReceiptArrayAdapter.super.metaTopFilterText = null;

            if ((constraint != null) && (constraint.toString().length() > 0)) {
                String constraintLowerCase = constraint.toString().toLowerCase();

                for (Receipt r : ReceiptArrayAdapter.super.objects) {
                    boolean addReceipt = false;

                    if (r.getStoreName().toLowerCase().contains(constraintLowerCase)) {
                        ReceiptArrayAdapter.super.titleFilterText = constraint.toString();
                        addReceipt = true;
                    }

                    if (getCardsString(r).toLowerCase().contains(constraintLowerCase)) {
                        ReceiptArrayAdapter.super.subTitleFilterText = constraint.toString();
                        addReceipt = true;
                    }

                    if (DataFormatUtilities.getFormattedDate(r.getTimeOfPurchase()).toLowerCase().contains(constraintLowerCase)) {
                        ReceiptArrayAdapter.super.metaTopFilterText = constraint.toString();
                        addReceipt = true;
                    }

                    if (addReceipt) {
                        i.add(r);
                    }
                }

                results.values = i;
                results.count = i.size();
            } else {

                synchronized (ReceiptArrayAdapter.super.objects) {
                    results.values = ReceiptArrayAdapter.super.objects;
                    results.count = ReceiptArrayAdapter.super.objects.size();
                }
            }

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(final CharSequence constraint, final FilterResults results) {
            filtered = (ArrayList<Receipt>) results.values;
            notifyDataSetChanged();
        }
    }
}
