package no.digipost.android.authentication;

import android.app.KeyguardManager;
import android.content.Context;
import no.digipost.android.utilities.SharedPreferencesUtilities;

public class AndroidLockSecurity {
	public static boolean canUseRefreshTokens(final Context context) {
		boolean screenLockAndKeyStoreAvailable = screenLockEnabled(context) && new KeyStoreAdapter(context).isAvailable();

		if(!screenLockAndKeyStoreAvailable){
			SharedPreferencesUtilities.clearData(context);
		}

		return screenLockAndKeyStoreAvailable;
	}

	public static boolean screenLockEnabled(final Context context) {
			KeyguardManager keyguardMgr = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
			return keyguardMgr.isKeyguardSecure();
	}
}