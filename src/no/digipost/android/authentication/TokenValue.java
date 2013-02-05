package no.digipost.android.authentication;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenValue {
	@JsonProperty
	private String aud;

	@JsonProperty
	private String exp;

	@JsonProperty
	private String iat;

	@JsonProperty
	private String user_id;

	@JsonProperty
	private String iss;

	@JsonProperty
	private String nonce;

	public String getAud() {
		return aud;
	}

	public String getExp() {
		return exp;
	}

	public String getIat() {
		return iat;
	}

	public String getUser_id() {
		return user_id;
	}

	public String getIss() {
		return iss;
	}

	public String getNonce() {
		return nonce;
	}
}
