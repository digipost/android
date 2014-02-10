package no.digipost.android.authentication;

import android.content.Context;

public class Security {
	public static boolean canUseRefreshTokens(final Context context) {
		return LockType.isAboveSlider(context.getContentResolver()) &&
				new KeyStoreAdapter(context).isAvailable();
	}
}
