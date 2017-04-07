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
    private final static String HIDE_INVOICE_OPTIONS = "hide_invoice_options";

    public static boolean canDisplayScreenlockTips(final Context context) {
        SharedPreferences sharedPreferences = SharedPreferencesUtilities.getDefault(context);
        return sharedPreferences.getBoolean("HideScreenlockTips", true);
    }

    public static void hideScreenlockTips(final Context context) {
        SharedPreferences sharedPreferences = SharedPreferencesUtilities.getDefault(context);
        sharedPreferences.edit().putBoolean("HideScreenlockTips", false).apply();
    }

    public static SharedPreferences getDefault(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static String getEncryptedRefreshtokenCipher(final Context context) {
        return getDefault(context).getString(ApiConstants.REFRESH_TOKEN, "");
    }

    public static void storeEncryptedRefreshtokenCipher(final String cipher, final Context context) {
        Editor editor = getDefault(context).edit();
        editor.remove(ApiConstants.REFRESH_TOKEN);
        editor.putString(ApiConstants.REFRESH_TOKEN, cipher);
        editor.apply();
    }

    public static void setLogoutFailed(final Context context, boolean failed){
        getDefault(context).edit().putBoolean(ApplicationConstants.LOGOUT_FAILED, failed).apply();
    }

    public static void deleteRefreshtoken(final Context context) {
        ContentOperations.setAccountToNull();
        Editor edit = getDefault(context).edit();
        edit.remove(ApiConstants.REFRESH_TOKEN);
        edit.apply();
    }

    public static int numberOfTimesAppHasRun(final Context context) {
        Editor editor = getDefault(context).edit();
        int numberOfTimesAppHasRun = getDefault(context).getInt(ApplicationConstants.NUMBER_OF_TIMES_APP_HAS_RUN, 1);
        editor.putInt(ApplicationConstants.NUMBER_OF_TIMES_APP_HAS_RUN, numberOfTimesAppHasRun + 1);
        editor.apply();

        return numberOfTimesAppHasRun;
    }

    public static boolean shouldDeleteStoredRefreshToken(final Context context) {
        int storedSDKVersion = getDefault(context).getInt(ApplicationConstants.ANDROID_SDK_VERSION,0);
        int currentSDKVersion = android.os.Build.VERSION.SDK_INT;
        int androidM = android.os.Build.VERSION_CODES.M;
        storeCurrentAndroidSDKVersion(context);
        storeCurrentAppVersionCode(context);
        return storedSDKVersion < androidM && currentSDKVersion >= androidM;
    }

    private static void storeCurrentAndroidSDKVersion(final Context context){
        Editor editor = getDefault(context).edit();
        editor.putInt(ApplicationConstants.ANDROID_SDK_VERSION, android.os.Build.VERSION.SDK_INT);
        editor.apply();
    }

    private static int currentAppVersionCode(final Context context){
        try {return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {}
        return 0;
    }

    private static void storeCurrentAppVersionCode(final Context context){
        Editor editor = getDefault(context).edit();
        editor.putInt(ApplicationConstants.APP_VERSION, currentAppVersionCode(context));
        editor.apply();
    }

    public static void hideInvoiceOptionsDialog(final Context context){
        Editor editor = getDefault(context).edit();
        editor.putBoolean(HIDE_INVOICE_OPTIONS, true);
        editor.apply();
    }

    public static boolean showInvoiceOptionsDialog(final Context context){
        return !getDefault(context).getBoolean(HIDE_INVOICE_OPTIONS, false);
    }

}
