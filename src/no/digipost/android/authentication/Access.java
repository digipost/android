package no.digipost.android.authentication;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Access {
	@JsonProperty
	private String access_token;

	@JsonProperty
	private String refresh_token;

	@JsonProperty
	private String expires_in;

	@JsonProperty
	private String token_type;

	@JsonProperty
	private String id_token;

	public String getAccess_token() {
		return access_token;
	}

	public String getRefresh_token() {
		return refresh_token;
	}

	public String getExpires_in() {
		return expires_in;
	}

	public String getToken_type() {
		return token_type;
	}

	public String getId_token() {
		return id_token;
	}

	public String getAccessToken() {
		return access_token;
	}
}
