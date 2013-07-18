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

    @JsonProperty("additional-addressline")
    private String additionalAddressline;

    @JsonProperty("zip-code")
    private String zipCode;
}
