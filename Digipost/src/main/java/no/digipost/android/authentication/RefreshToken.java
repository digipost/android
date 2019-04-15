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
