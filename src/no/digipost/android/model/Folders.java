package no.digipost.android.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;

import no.digipost.android.constants.ApiConstants;

@JsonIgnoreProperties(ignoreUnknown = true)

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

    private String getLinkByRelation(String relation) {
        for (Link l : link) {
            System.out.println(l);
            if(l!=null){
                System.out.println(l.getRel());
            }
            if (l.getRel().equals(relation)) {
                return l.getUri();
            }
        }
        if(link== null){
            System.out.println("link er null");
        }
        return null;
    }
}
