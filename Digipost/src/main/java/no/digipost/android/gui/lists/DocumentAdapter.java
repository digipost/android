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

package no.digipost.android.gui.lists;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import no.digipost.android.R;
import no.digipost.android.model.Document;
import no.digipost.android.utilities.DataFormatUtilities;

import java.util.ArrayList;
import java.util.List;

import static no.digipost.android.model.Origin.PUBLIC_ENTITY;
import static no.digipost.android.model.Origin.UPLOADED;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {

    private Context context;
    private ArrayList<Document> documents;

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
        this.documents = documents;
    }

    @Override
    public DocumentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_list_item,parent,false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DocumentViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);

    }

    @Override
    public int getItemCount() {
        return documents != null ? documents.size() : 0;
    }

    @Override
    public void onBindViewHolder(DocumentViewHolder holder, int position) {
        Document document = documents.get(position);
        holder.title.setText(document.getSubject());
        holder.subTitle.setText(getSubTitleText(document));
        holder.metaTop.setText(DataFormatUtilities.getFormattedDate(document.getCreated()));
        holder.metaMiddle.setText(DataFormatUtilities.getFormattedFileSize(Long.parseLong(document.getFileSize())));

        if (!document.isRead()) {
            holder.title.setTypeface(null, Typeface.BOLD);
            holder.subTitle.setTypeface(null, Typeface.BOLD);
        }

        if (document.getAttachment().size() > 1) {
            holder.subTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.paperclip_32, 0);
        }

        if (document.requiresHighAuthenticationLevel()) {
            holder.contentTypeImage.setImageDrawable(context.getResources().getDrawable(R.drawable.lock_32));
        }else{
            holder.contentTypeImage.setVisibility(View.GONE);
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


    public class DocumentViewHolder extends RecyclerView.ViewHolder{

        private TextView title, subTitle, metaTop,metaMiddle;
        private ImageView metaBottom, contentTypeImage;

        private DocumentViewHolder(View view){
            super(view);
            title = (TextView) view.findViewById(R.id.content_title);
            subTitle = (TextView) view.findViewById(R.id.content_subTitle);
            this.metaTop = (TextView) view.findViewById(R.id.content_meta_top);
            this.metaMiddle = (TextView) view.findViewById(R.id.content_meta_middle);
            this.metaBottom = (ImageView) view.findViewById(R.id.content_meta_bottom);
            contentTypeImage = (ImageView) view.findViewById((R.id.content_type_image));
        }
    }
}