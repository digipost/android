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

import no.digipost.android.R;
import no.digipost.android.constants.ApplicationConstants;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DrawerArrayAdapter<String> extends ArrayAdapter<String> {
	protected Context context;
	private TextView linkName;
	private TextView unreadView;
	private String[] links;
	private int unreadLetters;
	private int currentView;

	public DrawerArrayAdapter(final Context context, final int resource, final String[] links, final int unreadLetters) {
		super(context, resource, links);
		this.context = context;
		this.links = links;
		this.unreadLetters = unreadLetters;
		currentView = ApplicationConstants.MAILBOX;
	}

	public View getView(final int position, final View convertView, final ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View row = inflater.inflate(R.layout.drawer_list_item, parent, false);
		this.linkName = (TextView) row.findViewById(R.id.drawer_link_name);
		this.unreadView = (TextView) row.findViewById(R.id.drawer_link_unread);

		setupLinkView(row, position);

		return row;
	}

	public void setUnreadLetters(int unreadLetters) {
		this.unreadLetters = unreadLetters;
		notifyDataSetChanged();
	}

	public void updateDrawer(int currentView) {
		this.currentView = currentView;
		notifyDataSetChanged();
	}

	private void updateUnreadView(View row) {

		if (unreadLetters != 0) {

			unreadView.setText((CharSequence) (" " + unreadLetters));
			unreadView.setVisibility(View.VISIBLE);

			if (currentView == ApplicationConstants.MAILBOX) {
				unreadView.setBackgroundResource(R.color.main_drawer_dark_blue);
			}
		}
	}

	private void setupLinkView(View row, int position) {

		linkName.setText((CharSequence) links[position]);

		switch (position) {
		case ApplicationConstants.MAILBOX:
			linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.envelope, 0, 0, 0);
			updateUnreadView(row);
			break;

		case ApplicationConstants.RECEIPTS:
			linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.credit_card, 0, 0, 0);
			break;

		case ApplicationConstants.WORKAREA:
			linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.folder_close, 0, 0, 0);
			break;

		case ApplicationConstants.ARCHIVE:
			linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.folder_close, 0, 0, 0);
			break;

		default:
			linkName.setTextColor(context.getResources().getColor(R.color.main_drawer_dark_grey_text));
			linkName.setTextSize(13);
			row.setBackgroundResource(R.drawable.main_drawer_label);
		}
	}

	@Override
	public boolean isEnabled(int position) {
		switch (position) {
		case ApplicationConstants.MAILBOX:
			return true;
		case ApplicationConstants.RECEIPTS:
			return true;
		case ApplicationConstants.WORKAREA:
			return true;
		case ApplicationConstants.ARCHIVE:
			return true;
		default:
			return false;
		}
	}
}
