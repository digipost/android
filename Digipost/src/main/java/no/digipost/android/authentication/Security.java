package no.digipost.android.authentication;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import android.content.Context;
import android.util.Log;

public class Security {
	public static boolean canUseRefreshTokens(final Context context) {
		return isAboveSlider(context) &&
				new KeyStoreAdapter(context).isAvailable();
	}

	public static boolean isAboveSlider(final Context context) {
		try {
			Class<?> clazz = Class.forName("com.android.internal.widget.LockPatternUtils");
			Constructor<?> constructor = clazz.getConstructor(Context.class);
			constructor.setAccessible(true);
			Object newInstance = constructor.newInstance(context);
			Method method = clazz.getMethod("isSecure");
			return (Boolean)method.invoke(newInstance);
		} catch (Exception e) {
			Log.i("Security", "Cannot use LockPatternUtils", e);
		}


		return LockType.isAboveSlider(context.getContentResolver());
	}
}
