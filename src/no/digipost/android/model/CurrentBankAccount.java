package no.digipost.android.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrentBankAccount {

    @JsonProperty("account")
    private BankAccount bankAccount;

    public BankAccount getBankAccount() {
        return this.bankAccount;
    }
}
