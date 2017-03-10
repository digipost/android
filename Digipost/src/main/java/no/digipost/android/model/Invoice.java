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

import no.digipost.android.constants.ApiConstants;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;

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

    @JsonProperty
    private boolean canBePaidByUser;

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

    public boolean canBePaidByUser(){
        return canBePaidByUser;
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