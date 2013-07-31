package no.digipost.android.model;

import java.util.ArrayList;

import no.digipost.android.constants.ApiConstants;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Payment {

	@JsonProperty
	private String timePaid;

	@JsonProperty
	private String debitorBankAccount;

	@JsonProperty
	ArrayList<Link> link;

	public String getTimePaid() {
		return this.timePaid;
	}

	public String getDebitorBankAccount() {
		return this.debitorBankAccount;
	}

	public ArrayList<Link> getLink() {
		return this.link;
	}

	public void setLink(final ArrayList<Link> link) {
		this.link = link;
	}

	public String getBankHomepage() {
		if (link != null)
			for (Link l : link) {
				if (l.getRel().equals(ApiConstants.URL_RELATIONS_BANK_HOMEPAGE)) {
					return l.getUri();
				}
			}
		return null;
	}
}
