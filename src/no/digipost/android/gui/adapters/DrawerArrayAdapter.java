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
	private int unreadLetters;
    private int numberOfMailboxes;
	private int currentView;
    private ArrayList<Folder> folders;


	public DrawerArrayAdapter(final Context context, final int resource, final String[] links,ArrayList<Folder> folders, final int numberOfMailboxes, final int unreadLetters) {
		super(context, resource, links);
		this.context = context;
		this.links = links;
		this.unreadLetters = unreadLetters;
		currentView = ApplicationConstants.MAILBOX;
        this.folders = folders;
        this.numberOfMailboxes = numberOfMailboxes;
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
				unreadView.setBackgroundResource(R.color.main_dark_grey);
			}
		}
	}

	private void setupLinkView(View row, int position) {

		linkName.setText((CharSequence) links[position]);

		if(position == ApplicationConstants.MAILBOX+numberOfMailboxes) {
            linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.envelope, 0, 0, 0);
            updateUnreadView(row);

        }else if(position == ApplicationConstants.RECEIPTS+numberOfMailboxes){
			linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.credit_card, 0, 0, 0);

        }else if(position == ApplicationConstants.MAILBOX_LABEL+numberOfMailboxes){
            drawCategory(row);

        }else if(position == ApplicationConstants.FOLDERS_LABEL+numberOfMailboxes) {
            drawCategory(row);

        }else if(position < MainContentActivity.numberOfMailboxes+numberOfMailboxes) {
            //Mailbox

        }else{
            linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.folder_close, 0, 0, 0);
        }
	}

    private void drawCategory(View row){
        linkName.setTextColor(context.getResources().getColor(R.color.main_drawer_grey_text));
        linkName.setTextSize(14);
        linkName.setGravity(Gravity.BOTTOM);
        linkName.setTypeface(null, Typeface.BOLD);
        linkName.setPadding(0,0,0,5);
        row.setBackgroundResource(R.drawable.main_drawer_label);
    }

	@Override
	public boolean isEnabled(int position) {
        if(position != ApplicationConstants.MAILBOX_LABEL+numberOfMailboxes && position != ApplicationConstants.FOLDERS_LABEL+numberOfMailboxes){
            return true;
        }else{
            return false;
        }
	}
}
