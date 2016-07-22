/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    public int getId(){
        if(id==null){return 0;}
        return Integer.parseInt(id);
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
        return getLinkByRelation(ApiConstants.URL_RELATIONS_CHANGE_FOLDER);
    }

    public String getDeleteUri(){
        return getLinkByRelation(ApiConstants.URL_RELATIONS_DELETE_FOLDER);
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
