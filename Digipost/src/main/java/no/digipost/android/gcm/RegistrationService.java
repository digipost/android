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

import android.app.IntentService;
import android.content.Intent;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import no.digipost.android.api.ContentOperations;
import no.digipost.android.utilities.SharedPreferencesUtilities;

public class RegistrationService extends IntentService {

    private static final String TAG = "RegIntentService";

    public RegistrationService(){super(TAG);}

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferencesUtilities.setLogoutFailed(getApplicationContext(),false);
        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(GCMController.DEFAULT_SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            sendRegistrationToServer(token);
        } catch (Exception e) {
            e.printStackTrace();
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(GCMController.SENT_TOKEN_TO_SERVER, false).apply();
        }

        Intent registrationComplete = new Intent(GCMController.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private void sendRegistrationToServer(String token) {
        new AsyncTask<String, Void, String>() {

            @Override
            protected String doInBackground(String... params) {
                try {
                    ContentOperations.sendGCMRegistrationToken(getApplicationContext(), params[0]);
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(GCMController.SENT_TOKEN_TO_SERVER, true).apply();
                }catch (Exception e){
                    e.printStackTrace();
                }
                return "";
            }

        }.execute(token, null, null);
    }

}
