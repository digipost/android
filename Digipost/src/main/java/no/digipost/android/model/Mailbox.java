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
public class Mailbox  {

    @JsonProperty
    private String name;

    @JsonProperty
    private String digipostaddress;

    @JsonProperty
    private String owner;

    @JsonProperty
    private String unreadItemsInInbox;

    @JsonProperty
    private String usedStorage;

    @JsonProperty
    private String totalAvailableStorage;

    @JsonProperty
    private ArrayList<Link> link;

    @JsonProperty
    private Folders folders;

    public String getName(){
        return name;
    }
    public String getDigipostaddress(){
        return digipostaddress;
    }

    public int getUnreadItemsInInbox() {
        return Integer.parseInt(unreadItemsInInbox);
    }

    public String getUsedStorage() {
        return usedStorage;
    }

    public String getTotalAvailableStorage() {
        return totalAvailableStorage;
    }

    public ArrayList<Link> getLink() {
        return link;
    }

    public Folders getFolders(){
        return folders;
    }

    public String getInboxUri() {
        return getLinkByRelation(ApiConstants.URL_RELATIONS_DOCUMENT_INBOX);
    }

    public String getUploadToInboxUri() {
        return getLinkByRelation(ApiConstants.URL_RELATIONS_DOCUMENT_UPLOAD_TO_INBOX);
    }

    public String getReceiptsUri() {
        return getLinkByRelation(ApiConstants.URL_RELATIONS_DOCUMENT_RECEIPTS);
    }

    public boolean getOwner(){
        return owner.equals("true");
    }

    public String getUploadUri() {
        return getLinkByRelation(ApiConstants.URL_RELATIONS_DOCUMENT_UPLOAD);
    }

    public String getSettingsUri() {
        return getLinkByRelation(ApiConstants.URL_RELATIONS_ACCOUNT_SETTINGS);
    }

    private String getLinkByRelation(String relation) {
        for (Link l : link) {
            if (l.getRel().equals(relation)) {
                return l.getUri();
            }
        }
        return null;
    }
}
