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

import no.digipost.android.utilities.FormatUtilities;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)

public class Metadata {

    public static final String APPOINTMENT = "Appointment";
    public static final String EXTERNAL_LINK = "ExternalLink";

    public String title;
    @JsonProperty
    public String type;

    @JsonProperty
    public String url;

    @JsonProperty
    public String deadline;

    @JsonProperty
    public String description;

    @JsonProperty
    public String buttonText;

    @JsonProperty
    public Boolean urlIsActive;

    @JsonProperty
    public String subTitle;

    @JsonProperty
    public String startTime;

    @JsonProperty
    public String endTime;

    @JsonProperty
    public String arrivalTime;

    @JsonProperty
    public MetadataAddress address;

    @JsonProperty
    public String place;

    @JsonProperty
    public ArrayList<Info> info;

    public String getStartTimeString() { return "kl " + FormatUtilities.getTimeString(startTime);};

    public String getStartDateString() { return FormatUtilities.getDateString(startTime);};

    public String getPlace() { return place;}

    public String getPlaceAddress() { return address.streetAddress +", " +  address.postalCode + " " + address.city; }

    public String getArrivalInfo() {
        String arrivalDateString = FormatUtilities.getTimeString(arrivalTime);
        if (arrivalDateString != null) {
            return "kl " + arrivalDateString;
        } else if(arrivalTime != null) {
            return arrivalTime;
        }

        return "";
    }

    public String getInfoTitleAndText() {
        String infoText = "";
        for (Info i : info) {
            infoText += i.title + "\n" + i.text + "\n\n";
        }
        return infoText;
    }

    public String getButtonText() {
        return buttonText != null ? buttonText: "Gå videre";
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public Date getStartDate() {
        return FormatUtilities.getDate(startTime);
    }

    public Date getEndDate() {
        return FormatUtilities.getDate(endTime);
    }

    public Date getArrivalDate() {
        return FormatUtilities.getDate(arrivalTime);
    }
}