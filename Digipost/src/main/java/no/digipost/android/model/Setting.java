/**
 * Copyright (C) Posten Norge AS
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
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

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFilter("toJSON")
public class Setting {

    @JsonProperty
    private ArrayList<Link> link;

    @JsonProperty
    private String name;

    @JsonProperty
    private String type;

    @JsonProperty
    private String description;

    @JsonProperty
    private String value;

    @JsonProperty
    private String defaultValue;

    @JsonProperty
    private boolean overridden;

    @JsonProperty
    private String overriddenBy;

    @JsonProperty
    private boolean canBeChangedByCustomerService;

    @JsonProperty
    private boolean canBeChangedByUser;


    @JsonProperty
    private boolean fromSettingsService;

}
