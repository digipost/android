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

import android.app.Activity;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonFilter;

import java.util.ArrayList;

import no.digipost.android.DigipostApplication;
import no.digipost.android.analytics.GAEventController;
import no.digipost.android.constants.ApiConstants;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFilter("toJSON")
public class MailboxSettings {

    @JsonProperty
    private ArrayList<ExtendedEmail> emailAddress;

    @JsonProperty
    private ExtendedPhone mobilePhoneNumber;

    @JsonProperty
    private ArrayList<Link> link;

    @JsonProperty
    private ArrayList<SenderBlockingStatus> senderBlockingStatus;

    @JsonProperty
    private ArrayList<Setting> setting;

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

    public void setEmailAddress(String email, int index, Activity activity) {
        ArrayList<ExtendedEmail> emails = this.emailAddress;

        if (index >= emails.size()) {
            if (!email.isEmpty()) {
                ExtendedEmail extendedEmail = new ExtendedEmail();
                extendedEmail.email = email;
                emails.add(extendedEmail);
                GAEventController.sendKontaktopplysningerOppdatert(activity, "e-post", "legger til ny");
            }

        } else {
            if(email.isEmpty()){
                emails.remove(index);
                GAEventController.sendKontaktopplysningerOppdatert(activity, "e-post", "fjerner eksisterende");
            } else if (! emails.get(index).email.equals(email)) {
                ExtendedEmail extendedEmail = new ExtendedEmail();
                extendedEmail.email = email;
                emails.set(index, extendedEmail);
                GAEventController.sendKontaktopplysningerOppdatert(activity, "e-post", "oppdaterer eksisterende");
            }
        }
    }

    public void setPhoneNumber(String phoneNumber, Activity activity) {
        String oldPhone = mobilePhoneNumber.phoneNumber;

        if (oldPhone.isEmpty() && !phoneNumber.isEmpty()) {
            GAEventController.sendKontaktopplysningerOppdatert(activity, "telefonnummer", "legger tli nytt");
        } else if (!oldPhone.isEmpty() && phoneNumber.isEmpty()) {
            GAEventController.sendKontaktopplysningerOppdatert(activity, "telefonnummer", "fjerner eksisterende");
        } else if (!oldPhone.equals(phoneNumber)) {
            GAEventController.sendKontaktopplysningerOppdatert(activity, "telefonnummer", "oppdaterer eksisterende");
        }

        this.mobilePhoneNumber.phoneNumber = phoneNumber;
    }

    public void setCountryCode(String countryCode) {
        this.mobilePhoneNumber.countryCode = countryCode;
    }
}
