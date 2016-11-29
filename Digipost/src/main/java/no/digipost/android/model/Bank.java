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
        private boolean offersFakturaAgreementType1;

        @JsonProperty
        private boolean offersFakturaAgreementType2;

        @JsonProperty
        private boolean personHasFakturaAgreementWithBank;

        public boolean hasBankAgreementType1(){
            return this.offersFakturaAgreementType1 && this.personHasFakturaAgreementWithBank;
        }

        public boolean hasBankAgreementType2(){
            return this.offersFakturaAgreementType2 && this.personHasFakturaAgreementWithBank;
        }

        @JsonProperty
        private ArrayList<Link> link;

        public String getName(){
            return this.name;
        }
    }
