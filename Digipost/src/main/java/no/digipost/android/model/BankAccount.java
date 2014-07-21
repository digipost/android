package no.digipost.android.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BankAccount {

    @JsonProperty
    private String accountNumber;

    public String getAccountNumber() {
        return this.accountNumber;
    }
}
