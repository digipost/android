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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.analytics.GoogleAnalytics;
import no.digipost.android.R;
import no.digipost.android.analytics.GAEventController;
import no.digipost.android.model.Bank;

import static java.lang.String.format;

public class InvoiceBankActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_bank);
        setupActionBar();
        setupLayout();
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

        if(actionbar != null){
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeButtonEnabled(true);
            String invoiceSubject = getIntent().getExtras().getString(InvoiceOptionsActivity.INTENT_ACTIONBAR_TITLE);
            actionbar.setTitle(invoiceSubject);
            actionbar.setDisplayShowTitleEnabled(true);
        }
    }

    private Bank getBank(){
        String bankName = getIntent().getExtras().getString(InvoiceOptionsActivity.INTENT_BANK_NAME);
        return InvoiceBankAgreements.getBankByName(getApplicationContext(), bankName);
    }

    private void setupLayout(){
        final Bank bank = getBank();

        if(bank != null) {
            ImageView logo = (ImageView)findViewById(R.id.invoice_bank_logo);
            TextView bankName = (TextView)findViewById(R.id.invoice_bank_name);
            TextView title = (TextView) findViewById(R.id.invoice_bank_title);
            TextView subTitle = (TextView) findViewById(R.id.invoice_bank_subtitle);
            Button openBankUrlButton = (Button) findViewById(R.id.invoice_bank_url_button);
            Button readMoreButton = (Button) findViewById(R.id.invoice_bank_read_more_link);

            int logoResourceId = getApplicationContext().getResources().getIdentifier(bank.getLogo()+"_large", "drawable", getApplicationContext().getPackageName());
            if(logoResourceId != 0) {
                logo.setImageResource(logoResourceId);
                logo.setContentDescription(bank.getName());
            }else {
                logo.setVisibility(View.GONE);
                bankName.setVisibility(View.VISIBLE);
                bankName.setText(bank.getName());
            }


            if(bank.offersAgreementType(InvoiceBankAgreements.TYPE_2)){
                title.setText(R.string.invoice_bank_title_enabled);
                subTitle.setText(R.string.invoice_bank_subtitle_enabled);
                openBankUrlButton.setText(format(getString(R.string.invoice_bank_button_enabled), bank.getName()));
                readMoreButton.setText(R.string.invoice_bank_read_more_enabled);
            }else{
                title.setText(R.string.invoice_bank_title_disabled);
                subTitle.setText(R.string.invoice_bank_subtitle_disabled);
                openBankUrlButton.setVisibility(View.GONE);
                readMoreButton.setText(R.string.invoice_bank_read_more_disabled);
            }

            openBankUrlButton.setTransformationMethod(null);
            openBankUrlButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    GAEventController.sendInvoiceClickedSetup20Link(InvoiceBankActivity.this, bank.getName());
                    openExternalUrl(bank.getUrl());
                }
            });

            readMoreButton.setTransformationMethod(null);
            readMoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(bank.offersAgreementType(InvoiceBankAgreements.TYPE_2)) {
                        GAEventController.sendInvoiceClickedDigipostOpenPagesLink(InvoiceBankActivity.this, bank.getName());
                        openExternalUrl("https://digipost.no/faktura");
                    }else{
                        GAEventController.sendInvoiceClickedSetup10Link(InvoiceBankActivity.this, bank.getName());
                        openExternalUrl("https://digipost.no/app/post#/faktura");
                    }
                }
            });
        }
    }

    private void openExternalUrl(String url){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }
}
