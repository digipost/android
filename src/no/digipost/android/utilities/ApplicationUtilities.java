package no.digipost.android.utilities;

import no.digipost.android.gui.content.SettingsActivity;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;

public class ApplicationUtilities {

	public static void setScreenRotationFromPreferences(Activity activity) {
		if (SettingsUtilities.getScreenOrientationPreference(activity)) {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		} else {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}
}
