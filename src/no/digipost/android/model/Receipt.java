package no.digipost.android.model;

import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Receipt {

	@JsonProperty
	private String amount;

	@JsonProperty
	private String currency;

	@JsonProperty
	private String franchiceName;

	@JsonProperty
	private String storeName;

	@JsonProperty
	private String timeOfPurchase;

	@JsonProperty
	private ArrayList<Link> link;

	public String getAmount() {
		return amount;
	}

	public void setAmount(final String amount) {
		this.amount = amount;
	}

	public String getStoreName() {
		return storeName;
	}

	public void setStoreName(final String storeName) {
		this.storeName = storeName;
	}

	public String getTimeOfPurchase() {
		return timeOfPurchase;
	}

	public void setTimeOfPurchase(final String timeOfPurchase) {
		this.timeOfPurchase = timeOfPurchase;
	}

	public ArrayList<Link> getLink() {
		return link;
	}

	public void setLink(final ArrayList<Link> link) {
		this.link = link;
	}

	public String getFranchiceName() {
		return franchiceName;
	}

	public void setFranchiceName(final String franchiceName) {
		this.franchiceName = franchiceName;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(final String currency) {
		this.currency = currency;
	}

	public String getContentAsPDFUri() {
		for (Link l : link) {
			if (l.getRel().equals("https://www.digipost.no/post/relations/get_receipt_as_pdf")) {
				return l.getUri();
			}
		}
		return null;
	}
}