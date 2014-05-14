package no.digipost.android.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;

import no.digipost.android.constants.ApiConstants;

@JsonIgnoreProperties(ignoreUnknown = true)
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
        return getLinkByRelation("https://www.digipost.no/post/relations/self");
    }

    public void setName(String name){
        this.name = name;
    }

    public Documents getDocuments(){
        return documents;
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
