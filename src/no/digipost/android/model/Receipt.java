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
	private ArrayList<String> card;

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

	public ArrayList<String> getCard() {
		return card;
	}

	public void setCard(final ArrayList<String> card) {
		this.card = card;
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

	public String getContentAsHTMLUri() {
		for (Link l : link) {
			if (l.getRel().equals("https://www.digipost.no/post/relations/get_receipt_as_html")) {
				return l.getUri();
			}
		}
		return null;
	}

	public String getDeleteUri() {
		for (Link l : link) {
			if (l.getRel().equals("https://www.digipost.no/post/relations/delete_receipt")) {
				return l.getUri();
			}
		}
		return null;
	}
}