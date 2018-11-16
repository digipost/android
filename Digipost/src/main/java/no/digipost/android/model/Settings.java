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

    public ArrayList<ExtendedEmail> getExtendedEmails() {
        return emailAddress;
    }

    public void setExtendedEmailAdresses(ArrayList<ExtendedEmail> extendeEmailAdresses) {
        this.emailAddress = emailAddress;
    }

    public ExtendedPhone getExtendedPhone() {
        return mobilePhoneNumber;
    }

    public String getPhoneNumber() {
        return getExtendedPhone() != null ? getExtendedPhone().phoneNumber : "";
    }

    public String getCountryCode() {
        return getExtendedPhone() != null ? getExtendedPhone().countryCode : "";
    }

    public void setMobilePhoneNumber(ExtendedPhone mobilePhoneNumber) {
        this.mobilePhoneNumber = mobilePhoneNumber;
    }

    public ArrayList<Link> getLink() {
        return link;
    }

    public void setLink(ArrayList<Link> link) {
        this.link = link;
    }

    public String getUpdateSettingsUri() {
        for (Link l : link) {
            if (l.getRel().equals(ApiConstants.UPDATE_MAILBOX_SETTINGS)) {
                return l.getUri();
            }
        }
        return null;
    }
}
