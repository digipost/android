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

package no.digipost.android.authentication;

import android.app.KeyguardManager;
import android.content.Context;
import no.digipost.android.gcm.GCMController;
import no.digipost.android.utilities.SharedPreferencesUtilities;

public class TokenEncryption {
    private CryptoAdapter cryptoAdapter;

    public TokenEncryption(final Context context, boolean shouldRegenerateKeyPair){
        cryptoAdapter = keyStoreIsAvailable() ? new KeyStoreAdapter(shouldRegenerateKeyPair) : new ConcealAdapter(context);
    }

    public boolean isAvailable(){
        return cryptoAdapter.isAvailable();
    }

    public String encrypt(String plainText){
        return cryptoAdapter.encrypt(plainText);
    }

    public String decrypt(String cipherText){
        return cryptoAdapter.decrypt(cipherText);
    }

    public boolean keyStoreIsAvailable(){
        return (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M);
    }

    public static boolean canUseRefreshTokens(final Context context) {
        if (!screenLockEnabled(context)) {
            SharedPreferencesUtilities.deleteRefreshtoken(context);
            GCMController.reset(context);
        }
        return screenLockEnabled(context);
    }

    public static boolean unableToUseStoredRefreshToken(final Context context) {
        return !screenLockEnabled(context) && SharedPreferencesUtilities.refreshTokenExist(context);
    }

    public static boolean screenLockEnabled(final Context context) {
        KeyguardManager keyguardMgr = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardMgr.isKeyguardSecure();
    }
}
