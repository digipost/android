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
    private String unreadItemsInInbox;

    @JsonProperty
    private String usedStorage;

    @JsonProperty
    private String totalAvailableStorage;

    @JsonProperty
    private ArrayList<Link> link;

    @JsonProperty
    private Folders folders;

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

    public String getArchiveUri() {
        return getLinkByRelation(ApiConstants.URL_RELATIONS_DOCUMENT_ARCHIVE);
    }

    public String getWorkareaUri() {
        return getLinkByRelation(ApiConstants.URL_RELATIONS_DOCUMENT_WORKAREA);
    }

    public String getReceiptsUri() {
        return getLinkByRelation(ApiConstants.URL_RELATIONS_DOCUMENT_RECEIPTS);
    }

    public String getUploadUri() {
        return getLinkByRelation(ApiConstants.URL_RELATIONS_DOCUMENT_UPLOAD);
    }

    public String getSettingsUri() {
        return getLinkByRelation(ApiConstants.URL_RELATIONS_ACCOUNT_SETTINGS);
    }

    public String getCurrentBankAccountUri(){
        return getLinkByRelation(ApiConstants.URL_RELATIONS_CURRENT_BANK_ACCOUNT);
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
