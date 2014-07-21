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

@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {

    @JsonProperty
    private String street;

    @JsonProperty
    private String city;

    @JsonProperty("house-number")
    private String houseNumber;

    @JsonProperty("house-letter")
    private String houseLetter;

    @JsonProperty("additional-addressline")
    private String additionalAddressline;

    @JsonProperty("zip-code")
    private String zipCode;

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public String getHouseLetter() {
        return houseLetter;
    }

    public String getAdditionalAddressline() {
        return additionalAddressline;
    }

    public String getZipCode() {
        return zipCode;
    }
}
