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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import com.google.android.gms.gcm.GcmListenerService;
import no.digipost.android.R;
import no.digipost.android.authentication.TokenStore;
import no.digipost.android.gui.MainContentActivity;
import no.digipost.android.utilities.SharedPreferencesUtilities;

public class ListenerService extends GcmListenerService {

    @Override
    public void onMessageReceived(String from, Bundle data) {
        super.onMessageReceived(from, data);
        if(shouldProcessPush()) {
            displayNotification("Du har ett ulest brev i Digipost");
        };
    }

    private boolean shouldProcessPush(){
        boolean hasRefreshToken = !SharedPreferencesUtilities.getEncryptedRefreshtokenCipher(getApplicationContext()).isEmpty();
        boolean hasAccessToken = !TokenStore.getAccess().isEmpty();

        if(SharedPreferencesUtilities.logoutFailed(getApplicationContext())){
            return false;
        }else if(hasRefreshToken && GCMController.isDeviceRegistered(getApplicationContext())){
            return true;
        }else if(hasAccessToken && GCMController.isDeviceRegistered(getApplicationContext())){
            return true;
        }

        return false;
    }

    public void displayNotification(String message) {
        Intent intent = new Intent(this, MainContentActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(getNotificationIcon())
                .setContentTitle("Digipost")
                .setContentText(message)
                .setTicker(message)
                .setAutoCancel(false)
                .setSound(defaultSoundUri)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification=notificationBuilder.build();
        notification.contentView.setImageViewResource(android.R.id.icon, R.drawable.digipost_varslingsikon);

        notificationManager.notify(0, notification);
    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.digipost_varslingsikon : R.drawable.digipost_varslingsikon;
    }

}
