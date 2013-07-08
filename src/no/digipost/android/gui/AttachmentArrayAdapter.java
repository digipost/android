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

package no.digipost.android.gui;

import java.util.ArrayList;

import no.digipost.android.R;
import no.digipost.android.model.Attachment;
import no.digipost.android.utilities.DataFormatUtilities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AttachmentArrayAdapter extends ArrayAdapter<Attachment> {
	private final Context con;
	private final ArrayList<Attachment> attachments;



	public AttachmentArrayAdapter(final Context context, final int resource, final ArrayList<Attachment> objects) {
		super(context, resource, objects);
		con = context;
		attachments = objects;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row = inflater.inflate(R.layout.attachentdialog_list_item, parent, false);

		Attachment attachment = attachments.get(position);

		TextView title = (TextView)row.findViewById(R.id.attachment_title);
		TextView filetype = (TextView)row.findViewById(R.id.attachment_filetype);
		TextView filesize =  (TextView)row.findViewById(R.id.attachment_filesize);

		title.setText(attachment.getSubject());
		filetype.setText(attachment.getFileType());
		filesize.setText(DataFormatUtilities.getFormattedFileSize(attachment.getFileSize()));

		return row;
	}

    public void placeMainOnTop() {
        Attachment main = findMain();
        remove(main);
        insert(main, 0);
        notifyDataSetChanged();
    }

	public Attachment findMain() {
		for(Attachment a : attachments) {
			if(a.getMainDocument().equals("true")) {
				return a;
			}
		}
		return null;
	}
}
