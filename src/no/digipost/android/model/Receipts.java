package no.digipost.android.model;

import java.util.ArrayList;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Receipts {

	@JsonProperty
	private String numberOfCards;

	@JsonProperty
	private String numberOfCardsReadyForVerification;

	@JsonProperty
	private String numberOfReceiptsHiddenUntilVerification;

	@JsonProperty
	private ArrayList<Receipt> receipt;

	public String getNumberOfCards() {
		return numberOfCards;
	}

	public void setNumberOfCards(final String numberOfCards) {
		this.numberOfCards = numberOfCards;
	}

	public String getNumberOfCardsReadyForVerification() {
		return numberOfCardsReadyForVerification;
	}

	public void setNumberOfCardsReadyForVerification(final String numberOfCardsReadyForVerification) {
		this.numberOfCardsReadyForVerification = numberOfCardsReadyForVerification;
	}

	public String getNumberOfReceiptsHiddenUntilVerification() {
		return numberOfReceiptsHiddenUntilVerification;
	}

	public void setNumberOfReceiptsHiddenUntilVerification(final String numberOfReceiptsHiddenUntilVerification) {
		this.numberOfReceiptsHiddenUntilVerification = numberOfReceiptsHiddenUntilVerification;
	}

	public ArrayList<Receipt> getReceipt() {
		return receipt;
	}

	public void setReceipt(final ArrayList<Receipt> receipt) {
		this.receipt = receipt;
	}

}
