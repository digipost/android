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

import android.util.Log;

import no.digipost.android.constants.ApiConstants;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PrimaryAccount {
    @JsonProperty
    private String personalidentificationnumberObfuscated;

    @JsonProperty
    private String digipostaddress;

	@JsonProperty
	private String fullName;

    @JsonProperty
    private String firstName;

    @JsonProperty
    private ArrayList<String> email;

    @JsonProperty
    private String phonenumber;

	@JsonProperty
	private String unreadItemsInInbox;

	@JsonProperty
	private String usedStorage;

	@JsonProperty
	private String totalAvailableStorage;

    @JsonProperty
    private ArrayList<Address> address;

	@JsonProperty
	private ArrayList<Link> link;

    public String getPersonalidentificationnumberObfuscated() {
        return personalidentificationnumberObfuscated;
    }

    public String getDigipostaddress() {
        return digipostaddress;
    }

	public String getFullName() {
		return fullName;
	}

    public String getFirstName(){return firstName;}

    public ArrayList<String> getEmail() {
        return email;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

	public int getUnreadItemsInInbox() {
		return Integer.parseInt(unreadItemsInInbox);
	}

	public String getUsedStorage() {
		return usedStorage;
	}

	public String getTotalAvailableStorage() {
		return totalAvailableStorage;
	}

    public ArrayList<Address> getAddress() {
        return address;
    }

	public ArrayList<Link> getLink() {
		return link;
	}

	public String getInboxUri() {
		return getLinkByRelation(ApiConstants.URL_RELATIONS_DOCUMENT_INBOX);
	}

	public String getArchiveUri() {
		return getLinkByRelation(ApiConstants.URL_RELATIONS_DOCUMENT_ARCHIVE);
	}

	public String getWorkareaUri() {
		return getLinkByRelation(ApiConstants.URL_RELATIONS_DOCUMENT_WORKAREA);
	}

	public String getReceiptsUri() {
        return getLinkByRelation(ApiConstants.URL_RELATIONS_DOCUMENT_RECEIPTS);
	}

	public String getUploadUri() {
        return getLinkByRelation(ApiConstants.URL_RELATIONS_DOCUMENT_UPLOAD);
	}

    public String getMailboxSettingsUri() {
        return getLinkByRelation(ApiConstants.URL_RELATIONS_MAILBOX_SETTINGS);
    }

    public String getBanksUri(){return getLinkByRelation(ApiConstants.URL_RELATIONS_BANKS);}

    public String getCurrentBankAccountUri(){
        return getLinkByRelation(ApiConstants.URL_RELATIONS_CURRENT_BANK_ACCOUNT);
    }

    private String getLinkByRelation(String relation) {
        for (Link l : link) {
            if (l.getRel().equals(relation)) {
                return l.getUri();
            }
        }
        return null;
    }
}
