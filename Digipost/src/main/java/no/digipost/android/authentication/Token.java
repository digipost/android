package no.digipost.android.authentication;

import org.joda.time.DateTime;

public class Token {
    private String access;
    private String scope;
    private DateTime expiration;

    public Token(String access, String scope, DateTime expiration){
        this.access = access;
        this.scope = scope;
        this.expiration = expiration;
    }

    public String getAccessToken(){
        return access;
    }

    public String getScope(){
        return scope;
    }

    public boolean hasExpired(){
        return expiration.isBeforeNow();
    }

}
