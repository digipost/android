package no.digipost.android.model;


import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Account {
	@JsonProperty
	private PrimaryAccount primaryAccount;

	public PrimaryAccount getPrimaryAccount() {
		return primaryAccount;
	}
}
