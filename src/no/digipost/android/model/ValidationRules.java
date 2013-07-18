package no.digipost.android.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationRules {
    @JsonProperty
    private String email;

    @JsonProperty
    private String birthNumber;

    @JsonProperty
    private String organisationNumber;

    @JsonProperty
    private String phoneNumber;

    @JsonProperty
    private String date;

    @JsonProperty
    private String nonnegativeInteger;

    @JsonProperty
    private String integer;

    @JsonProperty
    private String name;

    @JsonProperty
    private String digipostAddress;

    @JsonProperty
    private String percent;

    public String getEmail() {
        return email;
    }

    public String getBirthNumber() {
        return birthNumber;
    }

    public String getOrganisationNumber() {
        return organisationNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getDate() {
        return date;
    }

    public String getNonnegativeInteger() {
        return nonnegativeInteger;
    }

    public String getInteger() {
        return integer;
    }

    public String getName() {
        return name;
    }

    public String getDigipostAddress() {
        return digipostAddress;
    }

    public String getPercent() {
        return percent;
    }
}
