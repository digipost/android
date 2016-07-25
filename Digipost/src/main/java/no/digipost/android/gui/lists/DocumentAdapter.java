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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import no.digipost.android.R;
import no.digipost.android.model.Document;

import java.util.ArrayList;
import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {

    private ArrayList<Document> documents;

    public DocumentAdapter(ArrayList<Document> documents){
        this.documents = documents;
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
    public DocumentAdapter.DocumentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_list_item,parent,false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DocumentViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onBindViewHolder(DocumentViewHolder holder, int position) {
        Document document = documents.get(position);
        holder.content_title.setText(document.getSubject());
    }

    @Override
    public int getItemCount() {
        return documents != null ? documents.size() : 0;
    }

    protected class DocumentViewHolder extends RecyclerView.ViewHolder{

        private TextView content_title;

        private DocumentViewHolder(View view){
            super(view);
            content_title = (TextView) view.findViewById(R.id.content_title);
        }
    }
}