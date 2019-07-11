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

public class Info extends DataType {
    public final String title;
    public final String text;

    public Info(String title, String text) {
        super(Info.class.getSimpleName());
        this.title = title;
        this.text = text;
    }

    public static Info fromWrapper(RawDataTypeWrapper w) {
        return new Info(w.getString("title"), w.getString("text"));
    }

    @Override
    public Info expandToType() {
        return this;
    }
}
