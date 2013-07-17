/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
