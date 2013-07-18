package no.digipost.android.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SenderBlockingStatus {
    @JsonProperty
    private Sender sender;

    @JsonProperty
    private String blocked;
}
