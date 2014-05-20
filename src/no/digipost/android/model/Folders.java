package no.digipost.android.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)

public class Folders {
    
    @JsonProperty
    private ArrayList<Folder> folder;

    public ArrayList<Folder> getFolder(){
        return folder;
    }
}
