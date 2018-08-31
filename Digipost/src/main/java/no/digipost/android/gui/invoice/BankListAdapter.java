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

package no.digipost.android.gui.invoice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import no.digipost.android.R;
import no.digipost.android.model.Bank;

import java.util.ArrayList;

public class BankListAdapter extends ArrayAdapter<Bank> {

    private ArrayList<Bank> banks;

    public class ViewHolder {
        private ImageView bankLogo;
        private TextView bankName;
    }

    public BankListAdapter(Context context, int textViewResourceId, ArrayList<Bank> banks) {
        super(context, textViewResourceId, banks);
        this.banks = banks;
    }

    public Bank getItem(int position){
        return banks.get(position);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        BankListAdapter.ViewHolder viewHolder;

        final Bank bank = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.invoice_bank_list_item, parent, false);
            viewHolder = new BankListAdapter.ViewHolder();
            viewHolder.bankLogo = (ImageView) convertView.findViewById(R.id.invoice_options_bank_item_list_logo);
            viewHolder.bankLogo.setContentDescription(bank.getName());
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (BankListAdapter.ViewHolder) convertView.getTag();
        }

        int logoResourceId = getContext().getResources().getIdentifier((bank.getLogo()), "drawable", getContext().getPackageName());
        if(logoResourceId != 0) {
            viewHolder.bankLogo.setImageResource(logoResourceId);
        }else {
            viewHolder.bankLogo.setVisibility(View.GONE);
            viewHolder.bankName = (TextView) convertView.findViewById(R.id.invoice_options_bank_item_list_name);
            viewHolder.bankName.setVisibility(View.VISIBLE);
            viewHolder.bankName.setText(bank.getName());
        }

        return convertView;
    }

}