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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import no.digipost.android.R;

import java.util.ArrayList;

public abstract class InvoiceOptionsActivity extends Activity {
    private ArrayList<InvoiceBank> banks;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_bank);
        addBanks();
    }

    private void openBankActivity(InvoiceBank invoiceBank){
        Intent i = new Intent(InvoiceOptionsActivity.this, InvoiceBankActivity.class);
        i.putExtra("InvoiceBank", invoiceBank);
        startActivity(i);
    }

    private void addBanks(){
        banks = new ArrayList<>();
        banks.add(new InvoiceBank("DNB", "https://m.dnb.no/appo/logon/startmobile", "invoice-bank-dnb", true));
        banks.add(new InvoiceBank("KLP", "https://www.digipost.no", "invoice-bank-klp", false));
        banks.add(new InvoiceBank("Skandiabanken", "https://www.digipost.no", "invoice-bank-skandia", false));
    }


}
