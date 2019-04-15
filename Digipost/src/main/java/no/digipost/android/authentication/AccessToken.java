package no.digipost.android.authentication;

import java.util.Calendar;
import java.util.Date;

class AccessToken extends Token {
    private final Date expiration;

    public AccessToken(String access, DigipostOauthScope scope, Date expiration) {
        super(access, scope);
        this.expiration = expiration;
    }

    public boolean hasExpired(){
        Date now = Calendar.getInstance().getTime();
        return expiration.before(now);
    }

}
