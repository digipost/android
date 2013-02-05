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
import android.widget.TextView;

public class LetterListAdapter extends ArrayAdapter<Letter> {
	private final Context con;
	private final ArrayList<Letter> letters;
	public static boolean showboxes = false;

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

		CheckBox checkbox = (CheckBox) row.findViewById(R.id.mailbox_checkbox);

		if (showboxes) {
			checkbox.setVisibility(View.VISIBLE);
		}
		return row;
	}

	private String getDateFormatted(final String date) {
		String date_substring = date.substring(0, 10);
		SimpleDateFormat fromApi = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat guiFormat = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
		String formatted = null;
		try {
			formatted = guiFormat.format(fromApi.parse(date_substring));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return formatted;
	}
}
