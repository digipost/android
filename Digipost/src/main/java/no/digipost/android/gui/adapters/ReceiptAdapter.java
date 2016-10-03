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
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import no.digipost.android.R;
import no.digipost.android.model.Receipt;
import no.digipost.android.utilities.DataFormatUtilities;

import java.util.ArrayList;

public class ReceiptAdapter extends RecyclerView.Adapter<ReceiptAdapter.ReceiptViewHolder> {

    private Context context;
    private ArrayList<Receipt> receipts;
    private SparseBooleanArray selectedPositions = new SparseBooleanArray();
    private ReceiptViewHolder receiptViewHolder;
    private boolean multiSelectEnabled;
    private boolean fetchedLastDocument;

    public ReceiptAdapter(Context context, ArrayList<Receipt> receipts){
        this.receipts = receipts;
        this.context = context;
    }

    public Receipt getItem(int position){
        return receipts.get(position);
    }

    public ArrayList<Receipt> getReceipts(){
        return receipts;
    }

    public void updateContent(ArrayList<Receipt> receipts){
        if(receipts.size() == 0) fetchedLastDocument = true;
        if(this.receipts != null){
            this.receipts.addAll(receipts);
        }else {
            this.receipts = receipts;
            resetMultiSelectAndContentState();
        }
        notifyDataSetChanged();
    }

    private void resetMultiSelectAndContentState(){
        selectedPositions = new SparseBooleanArray();
        multiSelectEnabled = false;
    }

    public boolean remainingContentToGet(){
        return !fetchedLastDocument;
    }

    public void clearExistingContent(){
        fetchedLastDocument = false;
        this.receipts = null;
        notifyDataSetChanged();
    }

    @Override
    public ReceiptViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_list_item,parent,false);
        receiptViewHolder = new ReceiptViewHolder(view);
        return receiptViewHolder;
    }

    public void setSelectable(boolean multiSelectEnabled){
        this.multiSelectEnabled = multiSelectEnabled;
        selectedPositions = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public void select(int position){
        selectedPositions.put(position, !selectedPositions.get(position));
        notifyItemChanged(position);
    }

    private boolean isSelected(int position){
        return selectedPositions.get(position);
    }

    public ArrayList<Receipt> getSelected(){
        ArrayList<Receipt> selectedReceipts = new ArrayList<  >();
        for(int i = 0; i < selectedPositions.size(); i++){
            int index = selectedPositions.keyAt(i);
            selectedReceipts.add(receipts.get(index));
        }
        return selectedReceipts;
    }

    public void replaceAtPosition(Receipt receipt, int position){
        receipts.set(position, receipt);
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return receipts != null ? receipts.size() : 0;
    }

    public boolean isEmpty(){
        return receipts.size() == 0;
    }

    @Override
    public void onBindViewHolder(ReceiptViewHolder receiptViewHolder, int position) {
        this.receiptViewHolder = receiptViewHolder;
        receiptViewHolder.bindReceipt(receipts.get(position), position);
    }

    public class ReceiptViewHolder extends RecyclerView.ViewHolder{
        private TextView title, subTitle, metaTop;
        private View view;
        private CheckBox checkbox;

        public ReceiptViewHolder(View view){
            super(view);
            title = (TextView) view.findViewById(R.id.content_title);
            subTitle = (TextView) view.findViewById(R.id.content_subTitle);
            metaTop = (TextView) view.findViewById(R.id.content_meta_top);
            checkbox = (CheckBox) view.findViewById(R.id.content_checkbox);
            this.view = view;
        }

        public void bindReceipt(Receipt receipt, int position){
            title.setText(receipt.getStoreName());
            subTitle.setText(DataFormatUtilities.getFormattedAmount(receipt.getAmount()) + " "
                    + DataFormatUtilities.getFormattedCurrency(receipt.getCurrency()));
            subTitle.setTextColor(context.getResources().getColor(R.color.green));
            metaTop.setText(DataFormatUtilities.getFormattedDate(receipt.getTimeOfPurchase()));

            boolean selected = isSelected(position);
            checkbox.setChecked(selected);
            view.setActivated(selected);
            view.setSelected(selected);

            if(multiSelectEnabled){
                checkbox.setVisibility(View.VISIBLE);
            }else{
                checkbox.setVisibility(View.GONE);
            }
        }
    }
}