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

package no.digipost.android.model;

import no.digipost.android.authentication.DigipostOauthScope;

import static no.digipost.android.constants.ApiConstants.AUTHENTICATION_LEVEL_IDPORTEN_3;
import static no.digipost.android.constants.ApiConstants.AUTHENTICATION_LEVEL_IDPORTEN_4;
import static no.digipost.android.constants.ApiConstants.AUTHENTICATION_LEVEL_TWO_FACTOR;

abstract class RequiresOauthScope {
    abstract String getAuthenticationLevel();

    public DigipostOauthScope getRequiredAuthenticationScope() {
        switch (getAuthenticationLevel()){
            case AUTHENTICATION_LEVEL_TWO_FACTOR:
                return DigipostOauthScope.FULL_HIGHAUTH;
            case AUTHENTICATION_LEVEL_IDPORTEN_3:
                return DigipostOauthScope.FULL_IDPORTEN3;
            case AUTHENTICATION_LEVEL_IDPORTEN_4:
                return DigipostOauthScope.FULL_IDPORTEN4;
            default:
                return DigipostOauthScope.FULL;
        }
    }

    public boolean requiresHighAuthenticationLevel() {
        return getAuthenticationLevel().equalsIgnoreCase(AUTHENTICATION_LEVEL_TWO_FACTOR) ||
                getAuthenticationLevel().equalsIgnoreCase(AUTHENTICATION_LEVEL_IDPORTEN_3) ||
                getAuthenticationLevel().equalsIgnoreCase(AUTHENTICATION_LEVEL_IDPORTEN_4);
    }

}
