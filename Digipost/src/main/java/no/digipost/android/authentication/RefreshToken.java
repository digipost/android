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

package no.digipost.android.authentication;

import org.apache.commons.lang.StringUtils;

public class RefreshToken extends Token {
    public RefreshToken(String access, DigipostOauthScope scope) {
        super(access, scope);
    }

    public static RefreshToken fromEncryptableString(String string) {
        if (StringUtils.isBlank(string)) {
            return null;
        }
        String[] components = string.split(":");
        String token = components[0];
        DigipostOauthScope scope = DigipostOauthScope.fromApiConstant(components.length > 1 ? components[1] : DigipostOauthScope.FULL.asApiConstant());
        return new RefreshToken(token, scope);
    }

    public String toEncryptableString() {
        return token +
                ( scope != null ? ":" + scope.asApiConstant() : "" );
    }

    public boolean hasExpired() {
        return false;
    }
}
