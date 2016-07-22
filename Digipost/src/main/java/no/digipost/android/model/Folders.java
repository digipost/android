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
