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

	public String getInboxUri() {
		for (Link l : link) {
			if (l.getRel().equals(ApiConstants.URL_RELATIONS_DOCUMENT_INBOX)) {
				return l.getUri();
			}
		}

		return null;
	}

	public String getArchiveUri() {
		for (Link l : link) {
			if (l.getRel().equals(ApiConstants.URL_RELATIONS_DOCUMENT_ARCHIVE)) {
				return l.getUri();
			}
		}

		return null;
	}

	public String getWorkareaUri() {
		for (Link l : link) {
			if (l.getRel().equals(ApiConstants.URL_RELATIONS_DOCUMENT_KITCHENBENCH)) {
				return l.getUri();
			}
		}

		return null;
	}

	public String getReceiptsUri() {
		for (Link l : link) {
			if (l.getRel().equals(ApiConstants.URL_RELATIONS_DOCUMENT_RECEIPTS)) {
				return l.getUri();
			}
		}
		return null;
	}
}
