package no.digipost.android.model;

import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Documents {
	@JsonProperty
	private ArrayList<Letter> document;

	public ArrayList<Letter> getDocument() {
		return document;
	}
}
