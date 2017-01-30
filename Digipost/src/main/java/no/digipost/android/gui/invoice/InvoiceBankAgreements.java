/**
 * Copyright (C) Posten Norge AS
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.digipost.android.gui.invoice;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import no.digipost.android.api.ContentOperations;
import no.digipost.android.model.Bank;
import no.digipost.android.model.Banks;
import no.digipost.android.utilities.SharedPreferencesUtilities;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class InvoiceBankAgreements {

    public final static String TYPE_1 = "AGREEMENT_TYPE_1";
    public final static String TYPE_2 = "AGREEMENT_TYPE_2";
    private final static String INVOICE_BANKS = "invoice_banks";

    public static boolean hasActiveAgreementType(final Context context, final String agreementType) {
        ArrayList<Bank> banksWithActiveAgreements = getBanksWithActiveAgreements(context);
        for (Bank bank : banksWithActiveAgreements) {
            boolean hasActiveAgreementTypebank = bank.hasActiveAgreementType(agreementType);
            if (hasActiveAgreementTypebank) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasActiveAgreements(final Context context) {
        return getBanksWithActiveAgreements(context).size() > 0;
    }

    public static void refreshBankAgreements(final Context context) {
        Banks banks = getBanks(context);
        if (banks != null) {
            storeBankAgreements(context, banks);
        } else {
            clearBankAgreements(context);
        }
    }

    private static ArrayList<Bank> getBanksWithActiveAgreements(final Context context) {
        String banksAsJSON = SharedPreferencesUtilities.getDefault(context).getString(INVOICE_BANKS, "");

        if (!banksAsJSON.isEmpty()) {
            Type type = new TypeToken<Banks>() {}.getType();
            Banks banks = new Gson().fromJson(banksAsJSON, type);
            return banks.getBanksWithActiveAgreements();
        } else {
            return new ArrayList<>();
        }
    }

    private static void storeBankAgreements(final Context context, Banks banks) {
        String banksAsJSON = new Gson().toJson(banks);
        SharedPreferences.Editor editor = SharedPreferencesUtilities.getDefault(context).edit();
        editor.putString(INVOICE_BANKS, banksAsJSON).apply();
    }

    private static void clearBankAgreements(final Context context) {
        SharedPreferences sharedPreferences = SharedPreferencesUtilities.getDefault(context);
        sharedPreferences.edit().remove(INVOICE_BANKS).apply();
    }

    public static Banks getBanks(final Context context) {
        try {
            GetBanksTask getBanksTask = new GetBanksTask(context);
            return getBanksTask.execute().get();
        } catch (Exception e) {
            return null;
        }
    }

    private static class GetBanksTask extends AsyncTask<Void, Void, Banks> {
        private Context context;

        private GetBanksTask(final Context context) {
            this.context = context;
        }

        @Override
        protected Banks doInBackground(Void... voids) {
            try {
                return ContentOperations.getBanks(context);
            } catch (Exception e) {
                return null;
            }
        }
    }
}