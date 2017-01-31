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
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.google.android.gms.analytics.GoogleAnalytics;
import no.digipost.android.R;
import no.digipost.android.analytics.GAEventController;
import no.digipost.android.model.Bank;

import java.util.ArrayList;

public class InvoiceOptionsActivity extends AppCompatActivity {
    public static final String INTENT_ACTIONBAR_TITLE = "actionBarTitle";
    public static final String INTENT_BANK_NAME = "bankName";

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
            String invoiceSubject = getIntent().getExtras().getString(INTENT_ACTIONBAR_TITLE);
            actionbar.setTitle(invoiceSubject);
            actionbar.setDisplayShowTitleEnabled(true);
        }
    }

    private void setupListView(){
        ArrayList<Bank> banks = InvoiceBankAgreements.getBanks(getApplicationContext());
        ListView listView = (ListView)findViewById(R.id.invoice_options_banks_listview);
        OptionsListAdapter adapter = new OptionsListAdapter(this, R.layout.invoice_bank_list_item, banks);
        listView.setAdapter(adapter);
    }

    private void openBankActivity(Bank bank){
        GAEventController.sendInvoiceOpenBankViewFromListEvent(this, bank.getName());
        Intent i = new Intent(InvoiceOptionsActivity.this, InvoiceBankActivity.class);
        i.putExtra(INTENT_BANK_NAME, bank.getName());
        i.putExtra(INTENT_ACTIONBAR_TITLE, getSupportActionBar().getTitle());
        startActivity(i);
    }

    private class OptionsListAdapter extends BankListAdapter{

        private OptionsListAdapter(final Context context,final int resource, ArrayList<Bank> banks){
            super(context, resource, banks);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openBankActivity(getItem(position));
                }
            });
            return view;
        }
    }
}