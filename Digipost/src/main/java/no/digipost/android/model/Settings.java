/**
 * Copyright (C) Posten Norge AS
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.digipost.android.model;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonFilter;

import java.util.ArrayList;

import no.digipost.android.constants.ApiConstants;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFilter("toJSON")
public class Settings {

    @JsonProperty
    private ArrayList<ExtendedEmail> emailAddress;

    @JsonProperty
    private ExtendedPhone mobilePhoneNumber;

    @JsonProperty
    private ArrayList<Link> link;

    @JsonIgnore
    public String getUpdateSettingsUri() {
        for (Link l : link) {
            if (l.getRel().equals(ApiConstants.UPDATE_MAILBOX_SETTINGS)) {
                return l.getUri();
            }
        }
        return null;
    }

    @JsonIgnore
    public ArrayList<ExtendedEmail> getExtendedEmails() {
        return emailAddress;
    }

    @JsonIgnore
    public String getPhoneNumber() {
        return this.mobilePhoneNumber.getPhoneNumber();
    }

    @JsonIgnore
    public String getCountryCode() {
        return this.mobilePhoneNumber.getCountryCode();
    }

    public void setEmailAddress(String email, int index) {
        ArrayList<ExtendedEmail> emails = this.emailAddress;
        if (email.isEmpty() && emails.get(index) != null) {
            emails.remove(index);
        } else if (index >= emails.size()) {
            ExtendedEmail extendedEmail = new ExtendedEmail();
            extendedEmail.email = email;
            emails.add(extendedEmail);
        } else {
            ExtendedEmail extendedEmail = emails.get(index);
            extendedEmail.email = email;
            emails.set(index, extendedEmail);
        }
    }

    public void setPhoneNumber(String phoneNumber) {
        this.mobilePhoneNumber.phoneNumber = phoneNumber;
    }

    public void setCountryCode(String countryCode) {
        this.mobilePhoneNumber.countryCode = countryCode;
    }
}
