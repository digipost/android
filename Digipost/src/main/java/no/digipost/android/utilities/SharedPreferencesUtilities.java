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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import no.digipost.android.api.ContentOperations;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.constants.ApplicationConstants;

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

    public static void storeScreenlockChoice(final Context context, final int choice) {
        SharedPreferences.Editor edit = getSharedPreferences(context).edit();
        edit.putInt(ApplicationConstants.SCREENLOCK_CHOICE, choice);
        edit.apply();
    }

    public static String getEncryptedRefreshtokenCipher(final Context context) {
        return getSharedPreferences(context).getString(ApiConstants.REFRESH_TOKEN, "");
    }

    public static void storeEncryptedRefreshtokenCipher(final String cipher, final Context context) {
        Editor editor = getSharedPreferences(context).edit();
        editor.remove(ApiConstants.REFRESH_TOKEN);
        editor.putString(ApiConstants.REFRESH_TOKEN, cipher);
        editor.apply();
    }

    public static void setLogoutFailed(final Context context, boolean failed){
        getSharedPreferences(context).edit().putBoolean(ApplicationConstants.LOGOUT_FAILED, failed).apply();
    }

    public static void clearData(final Context context){
        Editor editor = getSharedPreferences(context).edit();
        editor.clear();
        editor.apply();
    }

    public static void deleteRefreshtoken(final Context context) {
        ContentOperations.setAccountToNull();
        Editor edit = getSharedPreferences(context).edit();
        edit.remove(ApiConstants.REFRESH_TOKEN);
        edit.apply();
    }

    public static int numberOfTimesAppHasRun(final Context context) {
        Editor editor = getSharedPreferences(context).edit();
        int numberOfTimesAppHasRun = getSharedPreferences(context).getInt(ApplicationConstants.NUMBER_OF_TIMES_APP_HAS_RUN, 1);
        editor.putInt(ApplicationConstants.NUMBER_OF_TIMES_APP_HAS_RUN, numberOfTimesAppHasRun + 1);
        editor.apply();

        return numberOfTimesAppHasRun;
    }

    public static boolean firstLaunchWithNewVersion(final Context context) {

        int currentVersionCode = 0;
        try {
            currentVersionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return true;
        }

        int savedVersionCode = getSharedPreferences(context).getInt(ApplicationConstants.CURRENT_APP_VERSION, 0);
        boolean firstLaunch = currentVersionCode != savedVersionCode;
        if(firstLaunch) {
            Editor editor = getSharedPreferences(context).edit();
            editor.putInt(ApplicationConstants.CURRENT_APP_VERSION, currentVersionCode);
            editor.apply();
        }

        return firstLaunch;
    }
}
