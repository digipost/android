package no.digipost.android.authentication;

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

import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import no.digipost.android.model.Access;
import no.digipost.android.utilities.SharedPreferencesUtilities;

public class TokenStore {

    private static ArrayList<Token> tokens;

    public static boolean hasValidTokenForScope(DigipostOauthScope scope){
        return !getAccessTokenForScope(scope).isEmpty();
    }

    public static String getAccess(){
        return getAccessTokenForScope(DigipostOauthScope.FULL);
    }

    public static String getAccessTokenForScope(DigipostOauthScope scope){
        if(tokens != null) {
            for (Token token : tokens) {
                if (!token.hasExpired() && token.getScope().authorizedFor(scope)) {
                    return token.getToken();
                }
            }
        }
        return "";
    }

    public static void deleteStore(Context context){
        tokens = null;
        SharedPreferencesUtilities.deleteRefreshtoken(context);
    }

    public static boolean onlyLoggedInWithIDporten4() {
        if(tokens != null && tokens.size() == 1 ) {
            if(tokens.get(0).getScope() == DigipostOauthScope.FULL_IDPORTEN4){
                return true;
            }
        }
        return false;
    }

    public static void storeRefreshTokenInSharedPreferences(Context context, RefreshToken refreshToken) {
        String cipher = new TokenEncryption(context, true).encrypt(refreshToken);
        SharedPreferencesUtilities.storeEncryptedRefreshtokenCipher(cipher, context);
    }

    public static RefreshToken getRefreshTokenFromSharedPreferences(Context context){
        String encrypted_refresh_token = SharedPreferencesUtilities.getEncryptedRefreshtokenCipher(context);
        return new TokenEncryption(context,false).decrypt(encrypted_refresh_token);
    }

    public static void updateToken(Access access, DigipostOauthScope defaultScope){
        if (tokens == null) tokens = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, Integer.parseInt(access.getExpires_in())-10);
        Date date = calendar.getTime();

        final Token newToken = new AccessToken(access.getAccess_token(), access.getScopeOrDefault(defaultScope), date);

        boolean tokenIsSet = false;
        for(int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).getScope() == newToken.scope) {
                tokens.set(i, newToken);
                tokenIsSet = true;
            }
        }
        if(!tokenIsSet) {
            tokens.add(newToken);
        }
    }

    public static void storeToken(Context context, Access access, DigipostOauthScope defaultScope) {
        boolean storeRefreshToken = TokenEncryption.canUseRefreshTokens(context);
        if(storeRefreshToken) {
            storeRefreshTokenInSharedPreferences(context, new RefreshToken(access.getRefresh_token(), defaultScope));
        }

        updateToken(access, defaultScope);
    }
}
