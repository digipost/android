/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import org.codehaus.jackson.map.annotate.JsonFilter;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFilter("toJSON")
public class Document extends RequiresOauthScope {

    @JsonProperty
    private String subject;
    @JsonProperty
    private String creatorName;
    @JsonProperty
    private String created;
    @JsonProperty
    private String fileType;
    @JsonProperty
    private String fileSize;
    @JsonProperty
    private String origin;
    @JsonProperty("authentication-level")
    private String authenticationLevel;
    @JsonProperty
    private String location;
    @JsonProperty
    private String folderId;
    @JsonProperty
    private boolean read;
    @JsonProperty
    private String type;
    @JsonProperty
    private boolean collectionNotice;
    @JsonProperty
    private boolean paid;
    @JsonProperty
    private ArrayList<Link> link;
    @JsonProperty
    private ArrayList<Attachment> attachment;

    public ArrayList<Attachment> getAttachment() {
        return attachment;
    }

    public String getFolderId(){
        return folderId;
    }

    public String getSubject() {
        return subject;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public String getCreated() {
        return created;
    }

    public String getFileType() {
        return fileType;
    }

    public String getFileSize() {
        return fileSize;
    }

    public Origin getOrigin() {
        return Origin.parse(origin);
    }

    @Override
    public String getAuthenticationLevel() {
        return authenticationLevel;
    }

    public boolean hasCollectionNotice() {return this.collectionNotice;}

    public String getLocation() {
        return location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public void setFolderId(String folderId){
        this.folderId = folderId;
    }

    public boolean isRead() {
        return read;
    }

    public void markAsRead() {
        this.read = true;
    }

    public String getType() {
        return type;
    }

    public ArrayList<Link> getLink() {
        return link;
    }

    public boolean isInvoice(){
        return getType().equals(ApiConstants.INVOICE);
    }

    public boolean isPaid() {
        return this.paid;
    }

    public String getSelfUri() {
        return getLinkByRelation(ApiConstants.URL_RELATIONS_DOCUMENT_SELF);
    }

    public String getUpdateUri() {
        return getLinkByRelation(ApiConstants.URL_RELATIONS_DOCUMENT_UPDATE);
    }

    public String getDeleteUri() {
        return getLinkByRelation(ApiConstants.URL_RELATIONS_DOCUMENT_DELETE);
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
