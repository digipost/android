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

import no.digipost.android.constants.ApiConstants;

public enum DigipostOauthScope {
    NONE(false, 0, ""),
    FULL(false, 2, ApiConstants.SCOPE_FULL),
    FULL_HIGHAUTH(false, 4, ApiConstants.SCOPE_FULL_HIGH),
    FULL_IDPORTEN3(true, 3, ApiConstants.SCOPE_IDPORTEN_3),
    FULL_IDPORTEN4(true, 4, ApiConstants.SCOPE_IDPORTEN_4);

    DigipostOauthScope(boolean idporten, int level, String apiConstantName) {
        this.idporten = idporten;
        this.level = level;
        this.apiConstantName = apiConstantName;
    }

    private final boolean idporten;
    private final int level;
    private final String apiConstantName;

    public boolean authorizedFor(DigipostOauthScope targetAuthorization) {
        if (targetAuthorization.idporten && !this.idporten) {
            return false;
        } else {
            return this.level >= targetAuthorization.level;
        }
    }

    public static DigipostOauthScope fromApiConstant(String name) {
        if (ApiConstants.SCOPE_FULL.equals(name)) {
            return FULL;
        } else if (ApiConstants.SCOPE_FULL_HIGH.equals(name)) {
            return FULL_HIGHAUTH;
        } else if (ApiConstants.SCOPE_IDPORTEN_3.equals(name)) {
            return FULL_IDPORTEN3;
        } else if (ApiConstants.SCOPE_IDPORTEN_4.equals(name)) {
            return FULL_IDPORTEN4;
        } else {
            return NONE;
        }
    }

    public String asApiConstant() {
        return apiConstantName;
    }

    public int getLevel() {
        return level;
    }
}
