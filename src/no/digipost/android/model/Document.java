package no.digipost.android.model;


import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonFilter;

import java.util.ArrayList;

import no.digipost.android.constants.ApiConstants;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFilter("toJSON")
public class Document {

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
    private String read;
    @JsonProperty
    private String type;
    @JsonProperty
    private ArrayList<Link> link;
    @JsonProperty
    private ArrayList<Attachment> attachment;

    public ArrayList<Attachment> getAttachment() {
        return attachment;
    }

    public void setAttachment(final ArrayList<Attachment> attachment) {
        this.attachment = attachment;
    }
    public int getFolderId(){
        return Integer.parseInt(folderId);
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(final String subject) {
        this.subject = subject;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(final String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(final String created) {
        this.created = created;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(final String fileType) {
        this.fileType = fileType;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(final String fileSize) {
        this.fileSize = fileSize;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(final String origin) {
        this.origin = origin;
    }

    public String getAuthenticationLevel() {
        return authenticationLevel;
    }

    public void setAuthenticationLevel(final String authenticationLevel) {
        this.authenticationLevel = authenticationLevel;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    public void setFolderId(final int folderId){
        this.folderId = ""+folderId;
    }

    public String getRead() {
        return read;
    }

    public void setRead(final String read) {
        this.read = read;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public ArrayList<Link> getLink() {
        return link;
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

    public boolean hasOpeningReceipt() {
        for (Attachment a : attachment) {
            if(a.getOpeningReceiptUri() != null){
                return true;
            }
        }
        return false;
    }


}
