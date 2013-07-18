package no.digipost.android.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExtendedEmail {
    @JsonProperty
    private String email;

    @JsonProperty
    private String verified;

    @JsonProperty
    private ArrayList<Link> link;
}
