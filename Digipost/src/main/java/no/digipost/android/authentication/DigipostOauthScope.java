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
}
