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
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.model.Letter;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

public class LetterArrayAdapter extends ArrayAdapter<Letter> {
	public static final String TEXT_HIGHLIGHT_COLOR = "#EBEB86";

	private final Context con;
	private final ArrayList<Letter> letters;
	private ArrayList<Letter> filtered;
	private final Filter filter;
	public boolean showboxes;
	public boolean[] checked;
	private CheckBox checkbox;

	private String subjectFilterText;
	private String creatorFilterText;
	private String dateFilterText;

	public LetterArrayAdapter(final Context context, final int textViewResourceId, final ArrayList<Letter> objects) {
		super(context, textViewResourceId, objects);
		con = context;
		filtered = objects;
		letters = filtered;
		showboxes = false;
		filter = new LetterFilter();
		subjectFilterText = null;
		creatorFilterText = null;
		dateFilterText = null;
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
		if (filtered.get(position).getRead().equals("false")) {
			subject.setTypeface(null, Typeface.BOLD);
		}
		subject.setText(filtered.get(position).getSubject());

		TextView date = (TextView) row.findViewById(R.id.mail_date);
		date.setText(getDateFormatted(filtered.get(position).getCreated()));
		TextView creator = (TextView) row.findViewById(R.id.mail_creator);
		creator.setText(filtered.get(position).getCreatorName());
		TextView size = (TextView) row.findViewById(R.id.mail_size_price);
		ImageView attachment = (ImageView) row.findViewById(R.id.document_attachment);
		if (filtered.get(position).getAttachment().size() > 1) {
			attachment.setVisibility(View.VISIBLE);
		}
		ImageView locked = (ImageView) row.findViewById(R.id.document_locked);
		if (filtered.get(position).getAuthenticationLevel().equals(ApiConstants.AUTHENTICATION_LEVEL_TWO_FACTOR)) {
			locked.setVisibility(View.VISIBLE);
			size.setVisibility(View.INVISIBLE);
		} else {
			size.setText(getSizeFormatted(filtered.get(position).getFileSize()));
		}

		if (creatorFilterText != null) {
			changeFilteredTextcolor(creator, creatorFilterText);
		}

		if (subjectFilterText != null) {
			changeFilteredTextcolor(subject, subjectFilterText);
		}

		if (dateFilterText != null) {
			changeFilteredTextcolor(date, dateFilterText);
		}

		checkbox = (CheckBox) row.findViewById(R.id.mailbox_checkbox);

		if (checked != null) {

			if (checked[position]) {
                checkbox.setChecked(true);
			}

			checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				public void onCheckedChanged(final CompoundButton arg0, final boolean state) {
					checked[position] = state;
				}
			});
		}

		if (showboxes) {
			checkbox.setVisibility(View.VISIBLE);
		}

		return row;
	}

	@Override
	public void add(final Letter object) {
		filtered.add(object);
		notifyDataSetChanged();
	}

	@Override
	public Letter getItem(final int position) {
		return filtered.get(position);
	}

	@Override
	public int getCount() {
		return filtered.size();
	}

	@Override
	public void remove(final Letter object) {
		filtered.remove(object);
		notifyDataSetChanged();
	}

	public void updateList(final ArrayList<Letter> list, final String searchContraint) {
		letters.clear();
		letters.addAll(list);

		filter.filter(searchContraint);
	}

	public void setInitialcheck(final int position) {
		checked = new boolean[filtered.size()];
		checked[position] = true;
		showboxes = true;
		notifyDataSetChanged();
	}

	public void clearCheckboxes() {
		checked = null;
		showboxes = false;
		notifyDataSetChanged();
	}

	public boolean[] getCheckedDocuments() {
		return checked;
	}

	public boolean getShowBoxes() {
		return showboxes;
	}

	public int checkedCount() {
		int counter = 0;
		for (int i = 0; i < checked.length; i++) {
			counter = (checked[i]) ? counter + 1 : counter;
		}
		return counter;
	}

	private String getDateFormatted(final String date) {
		String date_substring = date.substring(0, 10);
		SimpleDateFormat fromApi = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat guiFormat = new SimpleDateFormat("d. MMM yyyy", Locale.getDefault());
		String formatted = null;
		try {
			formatted = guiFormat.format(fromApi.parse(date_substring));
		} catch (ParseException e) {
		}
		return formatted;
	}

	private String getSizeFormatted(final String byteString) {
		long bytes = Long.parseLong(byteString);
		String[] units = new String[] { "", "KB", "MB", "GB" };
		for (int i = 3; i > 0; i--) {
			double exp = Math.pow(1024, i);
			if (bytes > exp) {
				float n = (float) (bytes / exp);
				if (i == 1) {
					return (int) n + " " + units[i];
				}
				return String.format("%3.1f %s", n, units[i]);
			}
		}
		return Long.toString(bytes);
	}

	public static void changeFilteredTextcolor(final TextView v, final String filterText) {
		int l = filterText.length();
		int i = v.getText().toString().toLowerCase().indexOf(filterText.toLowerCase());

		if (i < 0) {
			return;
		}

		Spannable sb = new SpannableString(v.getText().toString());
		sb.setSpan(new BackgroundColorSpan(Color.parseColor(TEXT_HIGHLIGHT_COLOR)), i, i + l, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		v.setText(sb);
	}

	public void clearFilter() {
		creatorFilterText = null;
		subjectFilterText = null;
		dateFilterText = null;
		notifyDataSetChanged();
	}

	@Override
	public Filter getFilter() {
		return (filter != null) ? filter : new LetterFilter();
	}

	private class LetterFilter extends Filter {
		@Override
		protected FilterResults performFiltering(final CharSequence constraint) {
			FilterResults results = new FilterResults();
			ArrayList<Letter> i = new ArrayList<Letter>();
			creatorFilterText = null;
			subjectFilterText = null;
			dateFilterText = null;

			if ((constraint != null) && (constraint.toString().length() > 0)) {
				String constraintLowerCase = constraint.toString().toLowerCase();

				for (Letter l : letters) {
					boolean addLetter = false;

					if (l.getCreatorName().toLowerCase().contains(constraintLowerCase)) {
						creatorFilterText = constraint.toString();
						addLetter = true;
					}

					if (l.getSubject().toLowerCase().contains(constraintLowerCase)) {
						subjectFilterText = constraint.toString();
						addLetter = true;
					}

					if (getDateFormatted(l.getCreated()).toLowerCase().contains(constraintLowerCase)) {
						dateFilterText = constraint.toString();
						addLetter = true;
					}

					if (addLetter) {
						i.add(l);
					}
				}

				results.values = i;
				results.count = i.size();
			} else {

				synchronized (letters) {
					results.values = letters;
					results.count = letters.size();
				}
			}

			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(final CharSequence constraint, final FilterResults results) {
			filtered = (ArrayList<Letter>) results.values;
			notifyDataSetChanged();
		}
	}
}
