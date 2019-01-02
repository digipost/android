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

import no.digipost.android.constants.ApiConstants;
import no.digipost.android.model.Access;
import no.digipost.android.utilities.SharedPreferencesUtilities;

public class TokenStore {

    private static ArrayList<Token> tokens;

    public static boolean hasValidTokenForScope(String scope){
        return !getAccessTokenForScope(scope).isEmpty();
    }

    public static String getAccess(){
        return getAccessTokenForScope(ApiConstants.SCOPE_FULL);
    }

    public static String getAccessTokenForScope(String scope){
        if(tokens != null) {
            for (Token token : tokens) {
                if (!token.hasExpired()) {
                    if (scope.equals(ApiConstants.SCOPE_FULL) && token.getScope().equals(ApiConstants.SCOPE_FULL)) {
                        return token.getAccess();
                    }else if (scope.equals(ApiConstants.SCOPE_FULL) && token.getScope().equals(ApiConstants.SCOPE_IDPORTEN_4)){
                        return token.getAccess();
                    }else if (scope.equals(ApiConstants.SCOPE_FULL_HIGH) && token.getScope().equals(ApiConstants.SCOPE_FULL_HIGH)) {
                        return token.getAccess();
                    }else if (scope.equals(ApiConstants.SCOPE_FULL_HIGH) && token.getScope().equals(ApiConstants.SCOPE_IDPORTEN_4)) {
                        return token.getAccess();
                    } else if (scope.equals(ApiConstants.SCOPE_IDPORTEN_3) && token.getScope().equals(ApiConstants.SCOPE_IDPORTEN_3)) {
                        return token.getAccess();
                    } else if (scope.equals(ApiConstants.SCOPE_IDPORTEN_3) && token.getScope().equals(ApiConstants.SCOPE_IDPORTEN_4)) {
                        return token.getAccess();
                    } else if (scope.equals(ApiConstants.SCOPE_IDPORTEN_4) && token.getScope().equals(ApiConstants.SCOPE_IDPORTEN_4)) {
                        return token.getAccess();
                    }
                }
            }
        }
        return "";
    }

    public static void deleteStore(Context context){
        tokens = null;
        SharedPreferencesUtilities.deleteRefreshtoken(context);
    }

    public static void removeHighAuthenticationTokens(){
        if(tokens != null){
            for(Token token : tokens){
                if(!token.getScope().equals(ApiConstants.SCOPE_FULL)){
                    tokens.remove(token);
                }
            }
        }
    }

    public static boolean onlyLoggedInWithIDporten4() {
        if(tokens != null && tokens.size() == 1 ) {
            if(tokens.get(0).getScope().equals(ApiConstants.SCOPE_IDPORTEN_4)){
                return true;
            }
        }
        return false;
    }

    public static void storeRefreshTokenInSharedPreferences(Context context, String refreshToken) {
        String cipher = new TokenEncryption(context, true).encrypt(refreshToken);
        SharedPreferencesUtilities.storeEncryptedRefreshtokenCipher(cipher, context);
    }

    public static String getRefreshTokenFromSharedPreferences(Context context){
        String encrypted_refresh_token = SharedPreferencesUtilities.getEncryptedRefreshtokenCipher(context);
        return new TokenEncryption(context,false).decrypt(encrypted_refresh_token);
    }

    public static void updateToken(Context context, String access, String scope, String expiration){
        if (tokens == null) tokens = new ArrayList<>();
        boolean tokenExist = false;

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, Integer.parseInt(expiration)-10);
        Date date = calendar.getTime();

        for(int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).getScope().equals(scope)) {
                tokens.set(i, new Token(access,  scope, date));
                tokenExist = true;
            }
        }
        if(!tokenExist) tokens.add(new Token(access, scope, date));
    }

    public static void storeToken(final Context context, final Access access, final String scope) {
        boolean storeRefreshToken = scope.equals(ApiConstants.SCOPE_FULL) && TokenEncryption.canUseRefreshTokens(context);
        if(storeRefreshToken)storeRefreshTokenInSharedPreferences(context, access.getRefresh_token());

        updateToken(context, access.getAccess_token(),scope,access.getExpires_in());
    }
}
