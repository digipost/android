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
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import no.digipost.android.R;
import no.digipost.android.constants.ApplicationConstants;

public class DrawerArrayAdapter<String> extends ArrayAdapter<String>{
    protected Context context;
    private TextView linkView;

    public DrawerArrayAdapter(final Context context, final int resource,final String[] links) {
        super(context, resource, links);
        this.context = context;
    }

    public View getView(final int position, final View convertView, final ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View row = inflater.inflate(R.layout.drawer_list_item, parent, false);
        linkView = (TextView) row.findViewById(R.id.drawer_link_name);
        setLinkView(linkView, position);

        return row;
    }

    private void setLinkView(TextView linkView, int position) {
        switch (position){
            case ApplicationConstants.MAILBOX:
                linkView.setText(R.string.mailbox);
                linkView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.envelope,0,0,0);
                break;
            case ApplicationConstants.RECEIPTS:
                linkView.setText(R.string.receipts);
                linkView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.credit_card,0,0,0);
                break;
            case ApplicationConstants.WORKAREA:
                linkView.setText(R.string.workarea);
                break;
            case ApplicationConstants.ARCHIVE:
                linkView.setText(R.string.archive);
                break;
        }
    }
}