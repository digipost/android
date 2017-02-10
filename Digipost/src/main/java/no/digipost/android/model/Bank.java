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

    import org.codehaus.jackson.annotate.JsonIgnoreProperties;
    import org.codehaus.jackson.annotate.JsonProperty;

    import java.util.ArrayList;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public class Bank {

        @JsonProperty
        private String name;

        @JsonProperty
        private boolean tilbyrFakturaAvtaleType1;

        @JsonProperty
        private boolean tilbyrFakturaAvtaleType2;

        @JsonProperty
        private ArrayList<Agreement> agreements;
        @JsonProperty
        private ArrayList<Link> link;

        public String getName(){
            return this.name;
        }

        public boolean haveActiveAgreements(){
            for(Agreement agreement : agreements){
                if(agreement.active){
                    return true;
                }
            }
            return false;
        }

        public boolean hasActiveAgreementType(final String agreementType){
            return getAgreementForType(agreementType)!= null;
        }

        public boolean offersAgreementType(final String agreementType){
            for(Agreement agreement : agreements){
                if(agreementType.equals(agreement.agreementType)){
                    return true;
                }
            }
            return false;
        }

        public String getLogo(){
            switch (this.name.toUpperCase()){
                case "DNB" :
                    return "invoice_bank_logo_dnb";
                case "KLP BANKEN" :
                    return "invoice_bank_logo_klp";
                case "KLP" :
                    return "invoice_bank_logo_klp";
                case "SKANDIABANKEN" :
                    return "invoice_bank_logo_skandiabanken";
            }
            return "";
        }

        public void setAgreementsOfTypeActiveState(final String agreementType, boolean agreementIsActive){
            for (Agreement agreement : agreements){
                if(agreement.agreementType.equals(agreementType)){
                    agreement.active = agreementIsActive;
                }
            }
        }

        public Agreement getAgreementForType(final String agreementType){
            for(Agreement agreement : agreements){
                if(agreement.active && agreement.agreementType.equals(agreementType)){
                    return agreement;
                }
            }
            return null;
        }

        public String getUrl(){
            switch (this.name.toUpperCase()){
                case "DNB" :
                    return "https://www.dnb.no/privat/nettbank-mobil-og-kort/betaling/elektronisk-faktura.html";
                default:
                    return "https://www.digipost.no";
            }
        }
    }