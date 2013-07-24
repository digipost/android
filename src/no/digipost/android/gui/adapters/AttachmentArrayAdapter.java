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

import java.util.ArrayList;

import no.digipost.android.R;
import no.digipost.android.model.Attachment;
import no.digipost.android.utilities.DataFormatUtilities;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AttachmentArrayAdapter extends ArrayAdapter<Attachment> {
    protected Context con;
    protected ArrayList<Attachment> attachments;

    public AttachmentArrayAdapter(final Context context, final int resource, final ArrayList<Attachment> objects) {
        super(context, resource, objects);
        con = context;
        attachments = objects;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.attachmentdialog_list_item, parent, false);

        Attachment attachment = attachments.get(position);

        TextView title = (TextView) row.findViewById(R.id.attachment_title);

        if (!attachment.getRead().equals("true")) {
            title.setTypeface(null, Typeface.BOLD);
            row.setBackgroundResource(R.drawable.content_list_item_unread);
        }

        TextView filetype = (TextView) row.findViewById(R.id.attachment_filetype);
        TextView filesize = (TextView) row.findViewById(R.id.attachment_filesize);

        title.setText(attachment.getSubject());
        filetype.setText(attachment.getFileType());
        filesize.setText(DataFormatUtilities.getFormattedFileSize(Long.parseLong(attachment.getFileSize())));
        return row;
    }

    public void placeMainOnTop() {
        Attachment main = findMain();
        remove(main);
        insert(main, 0);
        notifyDataSetChanged();
    }

    public void setAttachments(final ArrayList<Attachment> attachments) {
        this.attachments = attachments;
        notifyDataSetChanged();
    }


    public Attachment findMain() {
        for (Attachment a : attachments) {
            if (a.getMainDocument().equals("true")) {
                return a;
            }
        }
        return null;
    }
}