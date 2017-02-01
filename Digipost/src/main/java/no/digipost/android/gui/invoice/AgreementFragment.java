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

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import no.digipost.android.R;
import no.digipost.android.model.Bank;

public class AgreementFragment extends DialogFragment {
    private static Bank bank;
    private ClickListener clickListener;

    static AgreementFragment newInstance(Bank bank) {
        AgreementFragment.bank = bank;
        return new AgreementFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_agreement, container, false);
        ((TextView) view.findViewById(R.id.fragment_agreement_bank_name)).setText(bank.getName());

        clickListener = new ClickListener();
        if (shouldDisplayAgreementOfType(InvoiceBankAgreements.TYPE_1)){
            (view.findViewById(R.id.fragment_agreement_type_1_view)).setVisibility(View.VISIBLE);

            //Aktiv avtale bank
            ((Button) view.findViewById(R.id.invoice_overview_active_agreement_type_1)).setText(getString(R.string.invoice_overview_active_agreement, bank.getName()));
            (view.findViewById(R.id.invoice_overview_active_agreement_type_1)).setOnClickListener(clickListener);
            ((Button) view.findViewById(R.id.invoice_overview_active_agreement_type_1)).setTransformationMethod(null);
            //Avtalevilkår
            ((Button) view.findViewById(R.id.invoice_overview_agreement_terms_type_1)).setText(getString(R.string.invoice_overview_agreement_terms));
            (view.findViewById(R.id.invoice_overview_agreement_terms_type_1)).setOnClickListener(clickListener);

            //Si opp avtalen
            ((Button) view.findViewById(R.id.invoice_overview_cancel_agreement_type_1)).setText(getString(R.string.invoice_overview_cancel_agreement));
            (view.findViewById(R.id.invoice_overview_cancel_agreement_type_1)).setOnClickListener(clickListener);

        }else{
            (view.findViewById(R.id.fragment_agreement_type_1_view)).setVisibility(View.GONE);
        }

        if (shouldDisplayAgreementOfType(InvoiceBankAgreements.TYPE_2)){
            (view.findViewById(R.id.fragment_agreement_type_2_view)).setVisibility(View.VISIBLE);

            //Aktiv avtale bank
            ((Button) view.findViewById(R.id.invoice_overview_active_agreement_type_2)).setText(getString(R.string.invoice_overview_active_agreement, bank.getName()));
            (view.findViewById(R.id.invoice_overview_active_agreement_type_2)).setOnClickListener(clickListener);

            //Avtalevilkår
            ((Button) view.findViewById(R.id.invoice_overview_agreement_terms_type_2)).setText(getString(R.string.invoice_overview_agreement_terms));
            (view.findViewById(R.id.invoice_overview_agreement_terms_type_2)).setOnClickListener(clickListener);

            //Si opp avtalen
            ((Button) view.findViewById(R.id.invoice_overview_cancel_agreement_type_1)).setText(getString(R.string.invoice_overview_cancel_agreement));
            (view.findViewById(R.id.invoice_overview_cancel_agreement_type_2)).setOnClickListener(clickListener);
        }else{
            (view.findViewById(R.id.fragment_agreement_type_1_view)).setVisibility(View.GONE);
        }

        return view;
    }

    private boolean shouldDisplayAgreementOfType(String agreementType){
        return InvoiceBankAgreements.hasActiveAgreementType(getActivity().getApplicationContext(),agreementType);
    }

    private class ClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            if (view == getView().findViewById(R.id.invoice_overview_active_agreement_type_1)) {

            }else if (view == getView().findViewById(R.id.invoice_overview_agreement_terms_type_1)) {

            }else if (view == getView().findViewById(R.id.invoice_overview_cancel_agreement_type_1)) {

            }else if (view == getView().findViewById(R.id.invoice_overview_active_agreement_type_2)) {

            }else if (view == getView().findViewById(R.id.invoice_overview_agreement_terms_type_2)) {

            }else if (view == getView().findViewById(R.id.invoice_overview_cancel_agreement_type_2)) {

            }

        }
    }
}