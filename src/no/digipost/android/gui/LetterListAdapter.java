/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.digipost.android.gui;

//import android.R;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import no.digipost.android.R;
import no.digipost.android.model.Letter;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class LetterListAdapter extends ArrayAdapter<Letter> {
	private final Context con;
	private final ArrayList<Letter> letters;
	public static boolean showboxes = false;
	public boolean[] checked;
	CheckBox checkbox;

	public LetterListAdapter(final Context context, final int textViewResourceId, final ArrayList<Letter> objects) {
		super(context, textViewResourceId, objects);
		con = context;
		letters = objects;
	}

	@SuppressWarnings("deprecation")
	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row = inflater.inflate(R.layout.mailbox_list_item, parent, false);

		Drawable even = con.getResources().getDrawable(R.drawable.list_selector_even);
		Drawable odd = con.getResources().getDrawable(R.drawable.list_selector_odd);

		row.setBackgroundDrawable((position % 2 == 0) ? even : odd);

		TextView subject = (TextView) row.findViewById(R.id.mail_subject);
		subject.setText(letters.get(position).getSubject());

		TextView date = (TextView) row.findViewById(R.id.mail_date);
		date.setText(getDateFormatted(letters.get(position).getCreated()));
		TextView creator = (TextView) row.findViewById(R.id.mail_creator);
		creator.setText(letters.get(position).getCreatorName());

		checkbox = (CheckBox) row.findViewById(R.id.mailbox_checkbox);

		if (checked != null) {

			if (checked[position]) {
				checkbox.setChecked(true);
			}

			checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(final CompoundButton arg0, final boolean state) {
					// TODO Auto-generated method stub
					checked[position] = state;
				}
			});

			if (showboxes) {
				checkbox.setVisibility(View.VISIBLE);
			}
		}
		return row;
	}

	@Override
	public Letter getItem(final int position) {
		return letters.get(position);
	}

	public void updateList(final ArrayList<Letter> list) {
		letters.addAll(list);
		notifyDataSetChanged();
	}

	public void setInitialcheck(final int position) {
		checked = new boolean[letters.size()];
		checked[position] = true;
	}

	public void clearCheckboxes() {
		checked = null;
	}

	public boolean[] getCheckedDocuments() {
		return checked;
	}

	private String getDateFormatted(final String date) {
		String date_substring = date.substring(0, 10);
		SimpleDateFormat fromApi = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat guiFormat = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
		String formatted = null;
		try {
			formatted = guiFormat.format(fromApi.parse(date_substring));
		} catch (ParseException e) {
			// Ignore
		}
		return formatted;
	}
}
