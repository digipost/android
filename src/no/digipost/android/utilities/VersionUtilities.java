package no.digipost.android.utilities;

public class VersionUtilities {
	public static boolean IsVersion18() {

		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		if (currentapiVersion >= 18) {
			return true;
		}
        return false;
	}
}
