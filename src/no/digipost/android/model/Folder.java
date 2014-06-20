package no.digipost.android.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonFilter;

import java.util.ArrayList;

import no.digipost.android.constants.ApiConstants;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFilter("toJSON")
public class Folder {
    @JsonProperty
    private String name;

    @JsonProperty
    private String id;

    @JsonProperty
    private String icon;

    @JsonProperty
    private Documents documents;

    @JsonProperty
    private ArrayList<Link> link;

    public String getName(){
        return name;
    }

    public String getId(){
        return id;
    }

    public String getIcon(){
        return icon;
    }

    public ArrayList<Link> getLink() {
        return link;
    }

    public String getSelfUri(){
        return getLinkByRelation(ApiConstants.URL_RELATIONS_DOCUMENT_SELF);
    }

    public void setName(String name){
        this.name = name;
    }

    public Documents getDocuments(){
        return documents;
    }

    public void setIcon(String icon){
        this.icon = icon;
    }

    public String getChangeUri(){
        return getLinkByRelation(ApiConstants.URL_RELATIONS_FOLDER_CHANGE);
    }

    public String getDeleteUri(){
        return getLinkByRelation(ApiConstants.URL_RELATIONS_FOLDER_DELETE);
    }

    public String getUploadUri(){
        return getLinkByRelation(ApiConstants.URL_RELATIONS_DOCUMENT_UPLOAD);
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
