package no.digipost.android.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonFilter;

import java.util.ArrayList;

import no.digipost.android.constants.ApiConstants;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFilter("toJSON")
public class Folders {
    
    @JsonProperty
    private ArrayList<Folder> folder;

    @JsonProperty
    private ArrayList<Link> link;

    public ArrayList<Folder> getFolder(){
        return folder;
    }

    public String getCreateFolderUri() {
        return getLinkByRelation(ApiConstants.URL_RELATIONS_CREATE_FOLDER);
    }

    public void setFolder(ArrayList<Folder> folder){
        this.folder = folder;
    }

    public String getUpdateFoldersUri(){
        return getLinkByRelation(ApiConstants.URL_RELATIONS_UPDATE_FOLDERS);
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
