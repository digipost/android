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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import no.digipost.android.R;
import no.digipost.android.model.Attachment;

import java.util.ArrayList;

public class AttachmentArrayAdapter extends ArrayAdapter<Attachment> {
    private Context context;
    private ArrayList<Attachment> attachments;

    public AttachmentArrayAdapter(final Context context, final int resource, final ArrayList<Attachment> objects) {
        super(context, resource, objects);
        this.context = context;
        attachments = objects;
        placeMainOnTop();
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.generic_dialog_list_item, parent, false);

        Attachment attachment = attachments.get(position);

        TextView title = (TextView) row.findViewById(R.id.generic_dialog_list_item_title);

        if (!attachment.isRead()) {
            title.setTypeface(null, Typeface.BOLD);
        }
        title.setText(attachment.getSubject());

        return row;
    }

    public void placeMainOnTop() {
        attachments.add(0, getMainDocument());
        notifyDataSetChanged();
    }

    public String getMainSubject() {
        return attachments.get(0).getSubject();
    }

    public void setAttachments(final ArrayList<Attachment> attachments) {
        this.attachments = attachments;
        placeMainOnTop();
    }

    @Override
    public Attachment getItem(int position) {
        return attachments.get(position);
    }

    public Attachment getMainDocument() {
        for (Attachment a : attachments) {
            if (a.isMainDocument()) {
                attachments.remove(a);
                return a;
            }
        }
        return null;
    }
}