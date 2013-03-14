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
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class ReceiptListAdapter extends ArrayAdapter<Receipt> {
	private final Context con;
	private final ArrayList<Receipt> receipts;
	public boolean showboxes;
	public boolean[] checked;
	CheckBox checkbox;
	View mainview;
	int bottombar;

	public ReceiptListAdapter(final Context context, final int textViewResourceId, final ArrayList<Receipt> objects, final View mainview, final int bottombar) {
		super(context, textViewResourceId, objects);
		con = context;
		receipts = objects;
		this.mainview = mainview;
		this.bottombar = bottombar;
		showboxes = false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row = inflater.inflate(R.layout.mailbox_list_item, parent, false);

		Drawable even = con.getResources().getDrawable(R.drawable.list_selector_even);
		Drawable odd = con.getResources().getDrawable(R.drawable.list_selector_odd);

		row.setBackgroundDrawable((position % 2 == 0) ? even : odd);

		Receipt receipt = receipts.get(position);

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

	public void remove(final View rowView, final Receipt object) {
		final Animation animation = AnimationUtils.loadAnimation(rowView.getContext(), R.anim.list_splashfadeout);
		rowView.startAnimation(animation);
		Handler handle = new Handler();
		handle.postDelayed(new Runnable() {

			public void run() {
				receipts.remove(object);
				notifyDataSetChanged();
				animation.cancel();
			}
		}, 1000);
	}

	@Override
	public Receipt getItem(final int position) {
		return receipts.get(position);
	}

	public void updateList(final ArrayList<Receipt> list) {
		receipts.clear();
		receipts.addAll(list);
		notifyDataSetChanged();
	}

	public void setInitialcheck(final int position) {
		checked = new boolean[receipts.size()];
		checked[position] = true;
		mainview.findViewById(bottombar).setVisibility(View.VISIBLE);
		showboxes = true;
	}

	public void clearCheckboxes() {
		checked = null;
		mainview.findViewById(bottombar).setVisibility(View.GONE);
		showboxes = false;
	}

	public boolean[] getCheckedDocuments() {
		return checked;
	}

	public int checkedCount() {
		int counter = 0;
		for(int i = 0; i < checked.length; i++) {
			counter = (checked[i]) ? counter +1 : counter ;
		}
		return counter;
	}

	public void setShowboxes(final boolean state) {
		showboxes = state;
	}

	public boolean getShowBoxes() {
		return showboxes;
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
}
