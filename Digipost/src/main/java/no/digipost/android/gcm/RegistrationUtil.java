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


package no.digipost.android.gcm;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.concurrent.Executors;

import no.digipost.android.api.ContentOperations;
import no.digipost.android.utilities.SharedPreferencesUtilities;

public class RegistrationUtil {

    static void sendRegistrationToServer(final Context context) {
        SharedPreferencesUtilities.setLogoutFailed(context, false);
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    Log.w(RegistrationUtil.class.getName(), "getInstanceId failed", task.getException());
                    return;
                }

                // Get new Instance ID token
                String token = task.getResult().getToken();
                sendRegistrationToServer(token, context);
            }
        });
    }

    private static void sendRegistrationToServer(final String token, final Context context) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                boolean success = false;
                try {
                    success = ContentOperations.sendGCMRegistrationToken(context, token);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    setSentTokenPreference(context, success);
                }
            }
        });
    }

    private static void setSentTokenPreference(Context applicationContext, boolean value) {
        PreferenceManager
                .getDefaultSharedPreferences(applicationContext)
                .edit()
                .putBoolean(GCMController.SENT_TOKEN_TO_SERVER, value)
                .apply();
    }

}
