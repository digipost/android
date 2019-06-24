package no.digipost.android.model;

import no.digipost.android.authentication.DigipostOauthScope;

import static no.digipost.android.constants.ApiConstants.AUTHENTICATION_LEVEL_IDPORTEN_3;
import static no.digipost.android.constants.ApiConstants.AUTHENTICATION_LEVEL_IDPORTEN_4;
import static no.digipost.android.constants.ApiConstants.AUTHENTICATION_LEVEL_TWO_FACTOR;

abstract class RequiresOauthScope {
    abstract String getAuthenticationLevel();

    public DigipostOauthScope getAuthenticationScope() {
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
