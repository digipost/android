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


package no.digipost.android.gui.content.invoice;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import com.google.android.gms.analytics.GoogleAnalytics;
import no.digipost.android.R;
import no.digipost.android.analytics.GAEventController;

import java.util.ArrayList;

public class InvoiceOptionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_options);
        setupActionBar();
        setupListView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    private void setupActionBar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();

        if(actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeButtonEnabled(true);
            String invoiceSubject = getIntent().getExtras().getString("InvoiceSubject");
            actionbar.setTitle(invoiceSubject);
            actionbar.setDisplayShowTitleEnabled(true);
        }
    }

    private void setupListView(){
        ListView listView = (ListView)findViewById(R.id.invoice_options_banks_listview);
        InvoiceBankAdapter adapter = new InvoiceBankAdapter(this, R.layout.invoice_bank_list_item, getInvoiceBanks());
        listView.setAdapter(adapter);
    }

    private ArrayList<InvoiceBank> getInvoiceBanks(){
        ArrayList<InvoiceBank> invoiceBanks = new ArrayList<>();
        invoiceBanks.add(new InvoiceBank("DNB", "https://m.dnb.no/privat/nettbank-mobil-og-kort/elektronisk-faktura.html", "invoice_bank_logo_dnb", true));
        invoiceBanks.add(new InvoiceBank("KLP", "", "invoice_bank_logo_klp", false));
        invoiceBanks.add(new InvoiceBank("Skandiabanken", "", "invoice_bank_logo_skandiabanken", false));
        return invoiceBanks;
    }

    private void openBankActivity(InvoiceBank invoiceBank){
        GAEventController.sendInvoiceOpenBankViewFromListEvent(this, invoiceBank.getName());
        Intent i = new Intent(InvoiceOptionsActivity.this, InvoiceBankActivity.class);
        i.putExtra("InvoiceBank", invoiceBank);
        i.putExtra("InvoiceSubject", getSupportActionBar().getTitle());
        startActivity(i);
    }

    private class InvoiceBankAdapter extends ArrayAdapter<InvoiceBank> {
        ArrayList<InvoiceBank> invoiceBanks;

        private class ViewHolder {private ImageView bankLogo;}

        private InvoiceBankAdapter(Context context, int textViewResourceId, ArrayList<InvoiceBank> invoiceBanks) {
            super(context, textViewResourceId, invoiceBanks);
            this.invoiceBanks = invoiceBanks;
        }

        public InvoiceBank getItem(int position){
            return invoiceBanks.get(position);
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            final InvoiceBank invoiceBank = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(this.getContext()).inflate(R.layout.invoice_bank_list_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.bankLogo = (ImageView) convertView.findViewById(R.id.invoice_options_bank_item_list_logo);
                viewHolder.bankLogo.setContentDescription(invoiceBank.getName());
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }


            int logoResourceId = getContext().getResources().getIdentifier(invoiceBank.getLogo(), "drawable", getContext().getPackageName());
            viewHolder.bankLogo.setImageResource(logoResourceId);

            convertView.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View v) {
                    openBankActivity(invoiceBank);
                }
            });

            return convertView;
        }
    }
}