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

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import com.google.android.gms.analytics.GoogleAnalytics;
import no.digipost.android.R;
import no.digipost.android.model.Bank;

import java.util.ArrayList;

public class InvoiceOverviewActivity extends AppCompatActivity {

    private static ArrayList<Bank> banks;
    private OverviewListAdapter adapter;
    private ListView listView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_overview);
        setupActionBar();
        setupListView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        InvoiceBankAgreements.updateBanksFromServer(getApplicationContext());
        toggleEmptyState();
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    private void toggleEmptyState(){
        if(banks == null || banks.size() == 0 || noActiveAgreements()){
            findViewById(R.id.invoice_overview_empty_state_view).setVisibility(View.VISIBLE);
            findViewById(R.id.invoice_overview_banks_listview).setVisibility(View.GONE);
            ((Button) findViewById(R.id.invoice_overview_empty_state_add_bank)).setTransformationMethod(null);
            findViewById(R.id.invoice_overview_empty_state_add_bank).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showBankOptions();
                }
            });
        }else{
            findViewById(R.id.invoice_overview_empty_state_view).setVisibility(View.GONE);
            findViewById(R.id.invoice_overview_banks_listview).setVisibility(View.VISIBLE);
        }
    }

    private boolean noActiveAgreements(){
        for(Bank bank : banks){
            if(bank.haveActiveAgreements()){
                return false;
            }
        }
        return true;
    }

    private void showBankOptions(){
        Intent i = new Intent(this, InvoiceOptionsActivity.class);
        i.putExtra(InvoiceOptionsActivity.INTENT_ACTIONBAR_TITLE, getString(R.string.drawer_invoice_overview));
        startActivity(i);
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();

        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeButtonEnabled(true);
            actionbar.setTitle(getString(R.string.drawer_invoice_overview));
            actionbar.setDisplayShowTitleEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void updateListViewWithBanksWithActiveAgreements(){
        for(Bank bank : banks)
            if (!bank.haveActiveAgreements())
                banks.remove(bank);

        InvoiceBankAgreements.replaceBanks(getApplicationContext(), banks);
        adapter.notifyDataSetChanged();
        toggleEmptyState();
    }

    public void changeAgreementStatus(final String bankName, final String agreementType, boolean agreementIsActive){
        for(Bank bank : banks){
            if(bank.getName().equals(bankName)){
                if(bank.hasActiveAgreementType(agreementType)) {
                    bank.setAgreementsOfTypeActiveState(agreementType, agreementIsActive);
                }
            }
        }
        updateListViewWithBanksWithActiveAgreements();
    }

    private void showBankFragment(Bank bank){
        FragmentManager fm = getFragmentManager();
        AgreementFragment agreementFragment = AgreementFragment.newInstance(bank);
        agreementFragment.show(fm, "AgreementFragment");
    }

    private void setupListView() {
        listView = (ListView) findViewById(R.id.invoice_overview_banks_listview);
        banks = InvoiceBankAgreements.getBanksWithActiveAgreements(getApplicationContext());
        adapter = new OverviewListAdapter(this, R.layout.invoice_bank_list_item, banks);
        listView.setAdapter(adapter);
    }

    private class OverviewListAdapter extends BankListAdapter{

        private OverviewListAdapter (final Context context,final int resource, ArrayList<Bank> banks){
            super(context, resource, banks);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showBankFragment(getItem(position));
                }
            });
            return view;
        }
    }
}
