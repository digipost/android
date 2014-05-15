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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import no.digipost.android.R;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.gui.MainContentActivity;
import no.digipost.android.model.Folder;

public class DrawerArrayAdapter<String> extends ArrayAdapter<String> {
	protected Context context;
	private TextView linkName;
	private TextView unreadView;
	private String[] links;
    private View line;
	private int unreadLetters;
	private int currentView;
	private ArrayList<Folder> folders;

	public DrawerArrayAdapter(final Context context, final int resource, final String[] links, ArrayList<Folder> folders,
			final int unreadLetters) {
		super(context, resource, links);
		this.context = context;
		this.links = links;
		this.unreadLetters = unreadLetters;
		currentView = ApplicationConstants.MAILBOX;
		this.folders = folders;
	}

	public View getView(final int position, final View convertView, final ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View row = inflater.inflate(R.layout.drawer_list_item, parent, false);
		this.linkName = (TextView) row.findViewById(R.id.drawer_link_name);
		this.unreadView = (TextView) row.findViewById(R.id.drawer_link_unread);
        this.line = (View) row.findViewById(R.id.drawer_line);
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
		unreadView.setText((CharSequence) (" " + unreadLetters));
		unreadView.setVisibility(View.VISIBLE);
	}

	private void setupLinkView(View row, int position) {

		linkName.setText((CharSequence) links[position]);

		if (position == ApplicationConstants.MAILBOX) {
			linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.envelope, 0, 0, 0);
			updateUnreadView(row);

		} else if (position == ApplicationConstants.RECEIPTS) {
			linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.credit_card, 0, 0, 0);

		} else if (links[position].equals(ApplicationConstants.DRAWER_MY_FOLDERS)) {
			drawLabel(row);

		} else if (links[position].equals(ApplicationConstants.DRAWER_MY_ACCOUNT)) {
			drawLabel(row);

		} else if (links[position].equals(ApplicationConstants.DRAWER_CHANGE_ACCOUNT)) {
			linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.account_32px, 0, 0, 0);

		} else if (links[position].equals(ApplicationConstants.DRAWER_SETTINGS)) {
			linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.admin_32px, 0, 0, 0);

		} else if (links[position].equals(ApplicationConstants.DRAWER_HELP)) {
			linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.help_32px, 0, 0, 0);

		} else if (links[position].equals(ApplicationConstants.DRAWER_LOGOUT)) {
			linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.logout_32px, 0, 0, 0);
		} else {

			CharSequence type = "FOLDER";

            if(folders != null){
                type = (CharSequence) folders.get(position - ApplicationConstants.numberOfStaticFolders).getIcon();
            }

			if (type.equals("PAPER")) {
				linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.file_32, 0, 0, 0);
			} else if (type.equals("TAGS")) {
                linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tags_32, 0, 0, 0);
            }else if(type.equals("LETTER")) {
                linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.envelope_32, 0, 0, 0);
			} else if (type.equals("HEART")) {
				linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.heart_32, 0, 0, 0);
			} else if (type.equals("TROPHY")) {
				linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.trophy_32, 0, 0, 0);
			} else if (type.equals("BOX")) {
				linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.archive_32, 0, 0, 0);
			} else if (type.equals("HOME")) {
				linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.home_32, 0, 0, 0);
			} else if (type.equals("STAR")) {
				linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.star_32, 0, 0, 0);
			} else if (type.equals("SUITCASE")) {
				linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.suitcase_32, 0, 0, 0);
			} else if (type.equals("CAMERA")) {
				linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.camera32, 0, 0, 0);
			} else if (type.equals("MONEY")) {
                linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.usa_32, 0, 0, 0);
			} else {
				linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.folder_close, 0, 0, 0);
			}
		}
	}

	private void drawLabel(View row) {
		linkName.setTextColor(context.getResources().getColor(R.color.main_drawer_grey_text));
		linkName.setTextSize(16);
		linkName.setGravity(Gravity.BOTTOM);
		linkName.setTypeface(null, Typeface.BOLD);
		linkName.setPadding(0, 0, 0, 5);
        line.setVisibility(View.GONE);
    }

	@Override
	public boolean isEnabled(int position) {
		if (position != ApplicationConstants.FOLDERS_LABEL
				&& position != MainContentActivity.numberOfFolders + ApplicationConstants.numberOfStaticFolders) {
			return true;
		} else {
			return false;
		}
	}
}
