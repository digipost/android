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

    public static String ACCESS_TOKEN = "";
    public static String REFRESH_TOKEN = "";

    private static ArrayList<Token> tokens;

    public static String getTokenForScope(String scope){
        if (scope.equals(ApiConstants.SCOPE_FULL)) {
            return ACCESS_TOKEN;
        }

        if(tokens != null) {
            for (Token token : tokens) {
                if (!token.hasExpired()) {
                    if (scope.equals(ApiConstants.SCOPE_FULL_HIGH) && token.getScope().equals(ApiConstants.SCOPE_FULL_HIGH)) {
                        return token.getAccessToken();
                    }else if (scope.equals(ApiConstants.SCOPE_FULL_HIGH) && token.getScope().equals(ApiConstants.SCOPE_IDPORTEN_4)) {
                        return token.getAccessToken();
                    } else if (scope.equals(ApiConstants.SCOPE_IDPORTEN_3) && token.getScope().equals(ApiConstants.SCOPE_IDPORTEN_4)) {
                        return token.getAccessToken();
                    } else if (scope.equals(ApiConstants.SCOPE_IDPORTEN_3) && token.getScope().equals(ApiConstants.SCOPE_IDPORTEN_3)) {
                        return token.getAccessToken();
                    } else if (scope.equals(ApiConstants.SCOPE_IDPORTEN_4) && token.getScope().equals(ApiConstants.SCOPE_IDPORTEN_4)) {
                        return token.getAccessToken();
                    }
                }
            }
        }
        return "";
    }

    public static boolean hasValidTokenForScope(String scope){
        return !getTokenForScope(scope).equals("");
    }

    public static void deleteStore(){
        tokens = null;
    }

    public static void storeToken(final Access access, final Context context, final String scope) {
        if(scope.equals(ApiConstants.SCOPE_FULL)) {
            JodaTimeAndroid.init(context);
            ACCESS_TOKEN = access.getAccess_token();
            REFRESH_TOKEN = access.getRefresh_token();

            if (SharedPreferencesUtilities.screenlockChoiceYes(context)) {
                String refresh_token = access.getRefresh_token();
                KeyStoreAdapter ksa = new KeyStoreAdapter(context);
                String cipher = ksa.encrypt(refresh_token);
                SharedPreferencesUtilities.storeEncryptedRefreshtokenCipher(cipher, context);
            }

        }else {

            if (tokens == null) {
                tokens = new ArrayList<>();
            }

            boolean tokenExist = false;

            JodaTimeAndroid.init(context);
            DateTime expiration = DateTime.now().plusSeconds(Integer.parseInt(access.getExpires_in())-10);
            for(int i = 0; i < tokens.size(); i++) {
                if (tokens.get(i).getScope().equals(scope)) {
                    tokens.set(i, new Token(access.getAccess_token(), scope, expiration));
                    tokenExist = true;
                }
            }

            if(!tokenExist) {
                tokens.add(new Token(access.getAccess_token(), scope, expiration));
            }
        }


    }
}
