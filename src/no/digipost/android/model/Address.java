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
