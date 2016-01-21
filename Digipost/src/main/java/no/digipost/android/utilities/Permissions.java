package no.digipost.android.utilities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.support.v4.app.ActivityCompat;

/**
 * Created by fredrik lillejordet on 21/01/16.
 */
public class Permissions {
    private final static int REQUEST_WRITE_EXTERNAL_STORAGE = 0;

    public static void requestWritePermissionsIfMissing(Context context, Activity activity){
        if(!FileUtilities.isStorageWriteAllowed(context)) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }
}
