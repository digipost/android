package no.digipost.android.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Sender {
    @JsonProperty
    private String firstname;

    @JsonProperty
    private String middlename;

    @JsonProperty
    private String lastname;

    @JsonProperty("digipost-address")
    private String digipostAddress;

    @JsonProperty("mobile-number")
    private String mobileNumber;

    @JsonProperty
    private ArrayList<Address> address;

    @JsonProperty
    private ArrayList<Link> link;
}
