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
import net.danlew.android.joda.JodaTimeAndroid;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.model.Access;
import no.digipost.android.utilities.SharedPreferencesUtilities;
import org.joda.time.DateTime;

import java.util.ArrayList;

public class TokenStore {

    private static ArrayList<Token> tokens;

    public static boolean hasValidTokenForScope(String scope){
        return !getAccessTokenForScope(scope).equals("");
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

    public static void storeRefreshTokenInSharedPreferences(Context context, String refreshToken) {
        if (SharedPreferencesUtilities.screenlockChoiceYes(context)) {
            String cipher = new TokenEncryption(context, true).encrypt(refreshToken);
            SharedPreferencesUtilities.storeEncryptedRefreshtokenCipher(cipher, context);
        }
    }

    public static String getRefreshTokenFromSharedPreferences(Context context){
        String encrypted_refresh_token = SharedPreferencesUtilities.getEncryptedRefreshtokenCipher(context);
        return new TokenEncryption(context,false).decrypt(encrypted_refresh_token);
    }

    public static void updateToken(Context context, String access, String scope, String expiration){
        if (tokens == null) tokens = new ArrayList<>();
        boolean tokenExist = false;

        JodaTimeAndroid.init(context);
        for(int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).getScope().equals(scope)) {
                tokens.set(i, new Token(access,  scope, DateTime.now().plusSeconds(Integer.parseInt(expiration)-10)));
                tokenExist = true;
            }
        }
        if(!tokenExist) tokens.add(new Token(access, scope, DateTime.now().plusSeconds(Integer.parseInt(expiration)-10)));
    }

    public static void storeToken(final Context context, final Access access, final String scope) {
        if(scope.equals(ApiConstants.SCOPE_FULL)) storeRefreshTokenInSharedPreferences(context, access.getRefresh_token());
        updateToken(context, access.getAccess_token(),scope,access.getExpires_in());
    }
}
