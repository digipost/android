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

package no.digipost.android.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.gui.content.SettingsActivity;

public class SettingsUtilities {

    private static SharedPreferences getSharedPreferences(Context context) {
        return SharedPreferencesUtilities.getSharedPreferences(context);
    }

    public static String getDefaultScreenPreference(Context context) {
        return getSharedPreferences(context).getString(SettingsActivity.KEY_PREF_DEFAULT_SCREEN, Integer.toString(ApplicationConstants.MAILBOX));
    }

    public static boolean getConfirmDeletePreference(Context context) {
        return getSharedPreferences(context).getBoolean(SettingsActivity.KEY_PREF_CONFIRM_DELETE, true);
    }

    public static boolean getConfirmMovePreference(Context context) {
        return getSharedPreferences(context).getBoolean(SettingsActivity.KEY_PREF_CONFIRM_MOVE, true);
    }

    public static boolean getShowBankIDLettersPreference(Context context) {
        return getSharedPreferences(context).getBoolean(SettingsActivity.KEY_PREF_SHOW_BANK_ID_DOCUMENTS, true);
    }
}
