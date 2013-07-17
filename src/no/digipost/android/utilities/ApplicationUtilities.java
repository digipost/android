package no.digipost.android.utilities;

import no.digipost.android.gui.content.SettingsActivity;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;

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
