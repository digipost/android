package no.digipost.android.model;

import java.util.ArrayList;

import no.digipost.android.api.ApiConstants;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PrimaryAccount {
	@JsonProperty
	private String fullName;

	@JsonProperty
	private ArrayList<Link> link;

	public String getFullName() {
		return fullName;
	}

	public ArrayList<Link> getLink() {
		return link;
	}

	public String getDokumentsUri() {
		for (Link l : link) {
			if (l.getRel().equals(ApiConstants.URL_RELATIONS_DOCUMENT_INBOX)) {
				return l.getUri();
			}
		}

		return null;
	}
}
