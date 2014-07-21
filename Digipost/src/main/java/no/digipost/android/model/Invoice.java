package no.digipost.android.model;


import java.util.ArrayList;

import no.digipost.android.constants.ApiConstants;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Invoice {

    @JsonProperty
    private Payment payment;

    @JsonProperty
    private ArrayList<Link> link;

    @JsonProperty
    private String kid;

    @JsonProperty
    private String accountNumber;

    @JsonProperty
    private String amount;

    @JsonProperty
    private String dueDate;

    public String getKid(){
        return kid;
    }

    public String getAccountNumber(){
        return accountNumber;
    }

    public String getAmout(){
        return amount;
    }

    public String getDueDate(){
        return dueDate;
    }

    public Payment getPayment(){
        return payment;
    }

    public ArrayList<Link> getLink() {
        return link;
    }

    public void setLink(final ArrayList<Link> link) {
        this.link = link;
    }

    public String getSendToBank(){
        for(Link l : link){
            if(l.getRel().equals(ApiConstants.URL_RELATIONS_SEND_TO_BANK)){
                return l.getUri();
            }
        }
        return null;
    }
}