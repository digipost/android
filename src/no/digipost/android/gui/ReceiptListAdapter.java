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
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import no.digipost.android.R;
import no.digipost.android.model.Receipt;
import android.content.Context;
import android.graphics.Color;
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
import android.widget.TextView;

public class ReceiptListAdapter extends ArrayAdapter<Receipt> {
	public static final String TEXT_HIGHLIGHT_COLOR = "#EBEB86";

	private final Context con;
	private final ArrayList<Receipt> receipts;
	private ArrayList<Receipt> filtered;
	private final Filter filter;
	public boolean showboxes;
	public boolean[] checked;
	private CheckBox checkbox;

	private String storeNameFilterText;
	private String dateFilterText;

	public ReceiptListAdapter(final Context context, final int textViewResourceId, final ArrayList<Receipt> objects) {
		super(context, textViewResourceId, objects);
		con = context;
		filtered = objects;
		receipts = filtered;
		showboxes = false;
		filter = new ReceiptFilter();
		storeNameFilterText = null;
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

		Receipt receipt = filtered.get(position);

		ArrayList<String> cards = receipt.getCard();
		TextView cardnumber = (TextView) row.findViewById(R.id.mail_creator);

		for (int i = 0; i < cards.size(); i++) {
			cardnumber.append(cards.get(i));

			if (i < cards.size() - 1) {
				cardnumber.append(", ");
			}
		}

		TextView subject = (TextView) row.findViewById(R.id.mail_subject);
		subject.setText(receipt.getStoreName());
		TextView date = (TextView) row.findViewById(R.id.mail_date);
		date.setText(getDateFormatted(receipt.getTimeOfPurchase()));
		TextView price = (TextView) row.findViewById(R.id.mail_size_price);
		price.setTextColor(con.getResources().getColor(R.color.green_price));
		String currency = receipt.getCurrency();
		if (currency.equals("NOK")) {
			currency = "kr.";
		}
		String amount = receipt.getAmount();
		Double number = Double.valueOf(amount);
		DecimalFormat dec = new DecimalFormat("#.00");
		amount = dec.format(number);

		price.setText(amount + " " + currency);

		if (storeNameFilterText != null) {
			changeFilteredTextcolor(subject, storeNameFilterText);
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
	public Receipt getItem(final int position) {
		return filtered.get(position);
	}

	@Override
	public int getCount() {
		return filtered.size();
	}

	@Override
	public void remove(final Receipt object) {
		filtered.remove(object);
		notifyDataSetChanged();
	}

	public void updateList(final ArrayList<Receipt> list) {
		receipts.clear();
		receipts.addAll(list);
		filtered = receipts;
		notifyDataSetChanged();
	}

	public void setInitialcheck(final int position) {
		checked = new boolean[receipts.size()];
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
		String date_substring = date.substring(0, 16);
		SimpleDateFormat fromApi = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
		SimpleDateFormat guiFormat = new SimpleDateFormat("d. MMM yyyy, HH:mm", Locale.getDefault());
		String formatted = null;
		try {
			formatted = guiFormat.format(fromApi.parse(date_substring));
		} catch (ParseException e) {
			// Ignore
		}
		return formatted;
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
		storeNameFilterText = null;
		dateFilterText = null;
		notifyDataSetChanged();
	}

	@Override
	public Filter getFilter() {
		return (filter != null) ? filter : new ReceiptFilter();
	}

	private class ReceiptFilter extends Filter {
		@Override
		protected FilterResults performFiltering(final CharSequence constraint) {
			FilterResults results = new FilterResults();
			ArrayList<Receipt> i = new ArrayList<Receipt>();
			storeNameFilterText = null;
			dateFilterText = null;

			if ((constraint != null) && (constraint.toString().length() > 0)) {
				String constraintLowerCase = constraint.toString().toLowerCase();

				for (Receipt r : receipts) {
					boolean addReceipt = false;

					if (r.getStoreName().toLowerCase().contains(constraintLowerCase)) {
						storeNameFilterText = constraint.toString();
						addReceipt = true;
					}

					if (getDateFormatted(r.getTimeOfPurchase()).toLowerCase().contains(constraintLowerCase)) {
						dateFilterText = constraint.toString();
						addReceipt = true;
					}

					if (addReceipt) {
						i.add(r);
					}
				}

				results.values = i;
				results.count = i.size();
			} else {

				synchronized (receipts) {
					results.values = receipts;
					results.count = receipts.size();
				}
			}

			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(final CharSequence constraint, final FilterResults results) {
			filtered = (ArrayList<Receipt>) results.values;
			notifyDataSetChanged();
		}
	}
}
