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

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ContentViewHolder> {

    protected ArrayList<Object> contents;

    public class ContentViewHolder extends RecyclerView.ViewHolder{

        public TextView content_title;

        public ContentViewHolder(View view){
            super(view);
            content_title = (TextView) view.findViewById(R.id.content_title);
        }
    }

    public ContentAdapter(ArrayList<Object> contents){
        this.contents = contents;
    }

    @Override
    public ContentAdapter.ContentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_list_item,parent,false);
        return new ContentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ContentAdapter.ContentViewHolder holder, int position) {
    }

    @Override
    public int getItemCount() {
        return contents.size();
    }
}