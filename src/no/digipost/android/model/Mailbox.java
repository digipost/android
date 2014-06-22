package no.digipost.android.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;

import no.digipost.android.constants.ApiConstants;

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
