/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.digipost.android.utilities;

import no.digipost.android.api.ContentOperations;
import no.digipost.android.authentication.Secret;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.constants.ApplicationConstants;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class SharedPreferencesUtilities {

	public static SharedPreferences getSharedPreferences(final Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

	public static int screenlockChoice(final Context context) {
		return getSharedPreferences(context).getInt(ApplicationConstants.SCREENLOCK_CHOICE,
				ApplicationConstants.SCREENLOCK_CHOICE_HAS_NO_BEEN_TAKEN_YET);
	}

	public static boolean screenlockChoiceYes(final Context context) {
		return screenlockChoice(context) == ApplicationConstants.SCREENLOCK_CHOICE_YES;
	}

	public static boolean screenlockChoiceNo(final Context context) {
		return screenlockChoice(context) == ApplicationConstants.SCREENLOCK_CHOICE_NO;
	}

	public static boolean screenlockChoiceNotTakenYet(final Context context) {
		return screenlockChoice(context) == ApplicationConstants.SCREENLOCK_CHOICE_HAS_NO_BEEN_TAKEN_YET;
	}

	public static void storeScreenlockChoice(final Context context, final int choice) {
		SharedPreferences.Editor edit = getSharedPreferences(context).edit();
		edit.putInt(ApplicationConstants.SCREENLOCK_CHOICE, choice);
		edit.commit();
	}

	public static void deleteScreenlockChoice(final Context context) {
		SharedPreferences.Editor edit = getSharedPreferences(context).edit();
		edit.remove(ApplicationConstants.SCREENLOCK_CHOICE);
		edit.commit();
	}

	public static String getEncryptedRefreshtokenCipher(final Context context) {
		return getSharedPreferences(context).getString(ApiConstants.REFRESH_TOKEN, "");
	}

	public static void storeEncryptedRefreshtokenCipher(final String cipher, final Context context) {
		Editor editor = getSharedPreferences(context).edit();
		editor.putString(ApiConstants.REFRESH_TOKEN, cipher);
		editor.commit();
	}

	public static void deleteRefreshtoken(final Context context) {
        Secret.ACCESS_TOKEN = null;
        ContentOperations.setAccountToNull();
		Editor edit = getSharedPreferences(context).edit();
		edit.remove(ApiConstants.REFRESH_TOKEN);
		edit.commit();
	}

	public static void clearSharedPreferences(final Context context) {
		Editor editor = getSharedPreferences(context).edit();
		editor.clear();
		editor.commit();
	}

    public static int numberOfTimesAppHasRun(final Context context) {
        Editor editor = getSharedPreferences(context).edit();
        int numberOfTimesAppHasRun = getSharedPreferences(context).getInt(ApplicationConstants.NUMBER_OF_TIMES_APP_HAS_RUN, 1);
        editor.putInt(ApplicationConstants.NUMBER_OF_TIMES_APP_HAS_RUN, numberOfTimesAppHasRun + 1);
        editor.commit();

        return numberOfTimesAppHasRun;
    }
}
