package no.digipost.android.authentication;

import android.app.KeyguardManager;
import android.content.Context;

public class Security {
	public static boolean canUseRefreshTokens(final Context context) {
		return screenLockEnabled(context) && new KeyStoreAdapter(context).isAvailable();
	}

	public static boolean screenLockEnabled(final Context context) {
			KeyguardManager keyguardMgr = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
			return keyguardMgr.isKeyguardSecure();
	}
}