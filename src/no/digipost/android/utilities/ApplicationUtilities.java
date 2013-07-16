package no.digipost.android.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;

import no.digipost.android.gui.SettingsActivity;

public class ApplicationUtilities {

    public static void setScreenRotationFromPreferences(Activity activity) {
        SharedPreferences sharedPreferences = SharedPreferencesUtilities.getSharedPreferences(activity);
        boolean allowScreenRotation = sharedPreferences.getBoolean(SettingsActivity.KEY_PREF_SCREEN_ROTATION, true);

        if (allowScreenRotation) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
}
