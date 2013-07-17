package no.digipost.android.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.gui.content.SettingsActivity;

public class SettingsUtilities {

    private static SharedPreferences getSharedPreferences(Context context) {
        return SharedPreferencesUtilities.getSharedPreferences(context);
    }

    public static boolean getScreenOrientationPreference(Context context) {
        return getSharedPreferences(context).getBoolean(SettingsActivity.KEY_PREF_SCREEN_ROTATION, true);
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
