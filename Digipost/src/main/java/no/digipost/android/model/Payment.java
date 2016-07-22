/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
