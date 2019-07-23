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

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;

import no.digipost.android.constants.ApiConstants;
import no.digipost.android.model.datatypes.DataType;

@JsonIgnoreProperties(ignoreUnknown = true)

public class Attachment extends RequiresOauthScope {

	@JsonProperty
	private String subject;
	@JsonProperty
	private String fileType;
	@JsonProperty
	private String fileSize;
	@JsonProperty("authentication-level")
	private String authenticationLevel;
	@JsonProperty
	private boolean read;
	@JsonProperty
	private boolean mainDocument;
	@JsonProperty
	private String type;
	@JsonProperty
	private ArrayList<Link> link;
	@JsonProperty
	private ArrayList<HashMap> metadata;
    @JsonProperty
    private Invoice invoice;
	@JsonProperty
	private boolean userKeyEncrypted;

	public String getSubject() {
		return subject;
	}

	public String getFileType() {
		return fileType;
	}

	public String getFileSize() {
		return fileSize;
	}

	@Override
	public String getAuthenticationLevel() {
		return authenticationLevel;
	}

	public boolean isRead() {
		return read;
	}

	public boolean isMainDocument() {
		return mainDocument;
	}

	public String getType() {
		return type;
	}

	public ArrayList<Link> getLink() {
		return link;
	}

    public Invoice getInvoice(){
        return invoice;
    }

	public boolean isUserKeyEncrypted() {
		return userKeyEncrypted;
	}

	public ArrayList<DataType> getMetadata() {
		ArrayList<DataType> list = new ArrayList<>();
		for (HashMap data: metadata) {
		    DataType parsedType = DataType.fromRawMap(data);
		    if (parsedType != null)
				list.add(parsedType);
		}
		return list;
	}

	public String getContentUri() {
		for (Link l : link) {
			if (l.getRel().equals(ApiConstants.URL_RELATIONS_DOCUMENT_GET_CONTENT)) {
				return l.getUri();
			}
		}
		return null;
	}

    public String getOpeningReceiptUri() {
        for (Link l : link) {
            if (l.getRel().equals(ApiConstants.URL_RELATIONS_DOCUMENT_SEND_OPENING_RECEIPT)) {
                return l.getUri();
            }
        }
        return null;
    }
}
