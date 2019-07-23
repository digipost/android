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
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import no.digipost.android.R;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.model.Document;
import no.digipost.android.utilities.FormatUtilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static no.digipost.android.model.Origin.PUBLIC_ENTITY;
import static no.digipost.android.model.Origin.UPLOADED;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {

    private Context context;
    private ArrayList<Document> documents;
    private SparseBooleanArray selectedPositions = new SparseBooleanArray();
    private DocumentViewHolder documentViewHolder;
    private boolean multiSelectEnabled;
    private String getUnixTimeOfNextDocument;
    private boolean fetchedLastDocument;

    public DocumentAdapter(Context context, ArrayList<Document> documents){
        this.documents = documents;
        this.context = context;
    }

    public Document getItem(int position){
        return documents.get(position);
    }

    public ArrayList<Document> getDocuments(){
        return documents;
    }

    public void updateContent(ArrayList<Document> documents){

        if(documents != null) {
            if(documents.size() > 0)
                this.getUnixTimeOfNextDocument = getUnixTimeOfNextDocument(documents.get(documents.size()-1));

            if(documents.size() >= ApiConstants.GET_DOCUMENT_LIMIT_N){
                documents.remove(documents.size()-1);
            }else{
                fetchedLastDocument = true;
            }

            if(this.documents != null && this.documents.size() > 0){
                this.documents.addAll(documents);
            }else{
                this.documents = documents;
                resetMultiSelectAndContentState();
            }
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
        getUnixTimeOfNextDocument = null;
        if(this.documents != null) this.documents.removeAll(this.documents);
        this.documents = null;
        notifyDataSetChanged();
    }

    private String getUnixTimeOfNextDocument(Document document){
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
            return Long.toString(formatter.parse(document.getCreated()).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public DocumentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_list_item,parent,false);
        documentViewHolder = new DocumentViewHolder(view);
        return documentViewHolder;
    }

    public void setSelectable(boolean multiSelectEnabled){
        this.multiSelectEnabled = multiSelectEnabled;
        selectedPositions = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    public String getGetUnixTimeOfNextDocument(){
        return this.getUnixTimeOfNextDocument;
    }

    public void select(int position){
        selectedPositions.put(position, !selectedPositions.get(position));
        notifyItemChanged(position);
    }

    private boolean isSelected(int position){
        return selectedPositions.get(position);
    }

    public ArrayList<Document> getSelected(){
        ArrayList<Document> selectedDocuments = new ArrayList<  >();
        for(int i = 0; i < selectedPositions.size(); i++){
            if(selectedPositions.valueAt(i)) {
                int documentIndex = selectedPositions.keyAt(i);
                selectedDocuments.add(documents.get(documentIndex));
            }
        }
        return selectedDocuments;
    }

    public void replaceAtPosition(Document document, int position){
        documents.set(position, document);
        notifyItemChanged(position);
    }

    @Override
    public int getItemCount() {
        return documents != null ? documents.size() : 0;
    }

    @Override
    public void onBindViewHolder(DocumentViewHolder documentViewHolder, int position) {
        this.documentViewHolder = documentViewHolder;
        documentViewHolder.bindDocument(documents.get(position), position);
    }

    public class DocumentViewHolder extends RecyclerView.ViewHolder{
        private TextView title, subTitle, metaTop, metaTypeDescription;
        private ImageView contentTypeImage, contentStatusImage;
        private View view;
        private CheckBox checkbox;
        private Drawable lock = context.getResources().getDrawable(R.drawable.lock_32);

        public DocumentViewHolder(View view){
            super(view);
            title = (TextView) view.findViewById(R.id.content_title);
            subTitle = (TextView) view.findViewById(R.id.content_subTitle);
            metaTop = (TextView) view.findViewById(R.id.content_meta_top);
            contentTypeImage = (ImageView) view.findViewById(R.id.content_type_image);
            checkbox = (CheckBox) view.findViewById(R.id.content_checkbox);
            metaTypeDescription = (TextView) view.findViewById(R.id.content_meta_type_description);
            contentStatusImage = (ImageView) view.findViewById(R.id.content_status_image);
            this.view = view;
        }

        public void bindDocument(Document document, int position){
            title.setText(document.getSubject());
            subTitle.setText(getSubTitleText(document));
            metaTop.setText(FormatUtilities.formatDateStringColloquial(document.getCreated()));

            if (!document.isRead()) {
                itemView.setBackgroundResource(R.drawable.content_list_item_unread);
                title.setTypeface(null, Typeface.BOLD);
                subTitle.setTypeface(null, Typeface.BOLD);
            }else{
                itemView.setBackgroundResource(R.drawable.content_list_item);
                title.setTypeface(null, Typeface.NORMAL);
                subTitle.setTypeface(null, Typeface.NORMAL);
            }

            if (document.getAttachment().size() > 1) {
                subTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.paperclip_32, 0);
            }else{
                subTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            }

            contentTypeImage.setImageDrawable(lock);
            if (document.requiresHighAuthenticationLevel()) {
                contentTypeImage.setVisibility(View.VISIBLE);
            }else{
                contentTypeImage.setVisibility(View.INVISIBLE);
            }

            boolean selected = isSelected(position);
            checkbox.setChecked(selected);
            view.setActivated(selected);
            view.setSelected(selected);

            if(multiSelectEnabled){
                checkbox.setVisibility(View.VISIBLE);
            }else{
                checkbox.setVisibility(View.GONE);
            }

            if (document.isInvoice()) {
                metaTypeDescription.setTypeface(null, Typeface.ITALIC);
                metaTypeDescription.setVisibility(View.VISIBLE);
                contentStatusImage.setVisibility(View.VISIBLE);
                if (document.hasCollectionNotice()) {
                    metaTypeDescription.setText(context.getString(R.string.list_document_type_collection_notice));
                } else if (document.isPaid()) {
                    contentStatusImage.setImageDrawable(context.getResources().getDrawable(R.drawable.added_to_payments_32px));
                    metaTypeDescription.setText(R.string.list_document_type_invoice_paid);
                } else {
                    contentStatusImage.setImageDrawable(context.getResources().getDrawable(R.drawable.unpaid_32px));
                    metaTypeDescription.setText(R.string.list_document_type_invoice_unpaid);
                }
            } else {
                metaTypeDescription.setText("");
                metaTypeDescription.setVisibility(View.GONE);
                contentStatusImage.setVisibility(View.GONE);
            }
        }

        private String getSubTitleText(Document document){
            if (document.getOrigin() == UPLOADED) {
                return context.getString(R.string.uploaded);
            } else if (document.getOrigin() == PUBLIC_ENTITY) {
                return context.getString(R.string.public_entity, document.getCreatorName());
            } else {
                return document.getCreatorName();
            }
        }
    }
}