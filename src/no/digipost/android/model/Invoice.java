package no.digipost.android.model;

import com.sun.xml.internal.rngom.ast.builder.Include;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.ArrayList;

import no.digipost.android.constants.ApiConstants;

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
        return this.kid;
    }

    public String getAccountNumber(){
        return this.accountNumber;
    }

    public String getAmout(){
        return this.amount;
    }

    public String getDueDate(){
        return this.dueDate;
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