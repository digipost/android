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

        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import no.digipost.android.R;
        import no.digipost.android.model.Receipt;
        import java.util.ArrayList;

public class ReceiptAdapter extends ContentAdapter {

    public ReceiptAdapter(ArrayList<Object> documents){
        super(documents);
    }

    @Override
    public ReceiptAdapter.ContentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.content_list_item,parent,false);
        return new ContentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ContentViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Receipt receipt = (Receipt) contents.get(position);
        holder.content_title.setText(receipt.getStoreName());
    }
}