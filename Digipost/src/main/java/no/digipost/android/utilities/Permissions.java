package no.digipost.android.utilities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 /**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class Permissions {
    private final static int REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    public static boolean requestWritePermissionsIfMissing(Context context, Activity activity){

        if(!FileUtilities.isStorageWriteAllowed(context)) {
            Permissions.requestPermissions(REQUEST_WRITE_EXTERNAL_STORAGE, activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        return FileUtilities.isStorageWriteAllowed(context);
    }

    public static boolean checkPermissions(Activity activity, String... permissionsId) {
        boolean permissions = true;
        for (String p : permissionsId) {
            permissions = permissions && ContextCompat.checkSelfPermission(activity, p) == PackageManager.PERMISSION_GRANTED;
        }
        return permissions;
    }

    public static void requestPermissions(int callbackId, Activity activity, String... permissionsId) {
        ActivityCompat.requestPermissions(activity, permissionsId, callbackId);
    }

}
