/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.digipost.android.model.datatypes;

import java.net.URI;

public class DataTypeLink extends DataType{
    public final URI url;
    public final String description;

    private DataTypeLink(URI url, String description) {
        super(DataTypeLink.class.getSimpleName());
        this.url = url;
        this.description = description;
    }

    public static DataTypeLink fromWrapper(RawDataTypeWrapper w) {
        return new DataTypeLink(URI.create(w.getString("url")), w.getString("description"));
    }

    @Override
    public DataTypeLink expandToType() {
        return this;
    }
}
