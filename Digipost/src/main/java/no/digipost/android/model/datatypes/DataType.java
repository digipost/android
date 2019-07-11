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

import java.util.HashMap;
import java.util.Map;

public abstract class DataType {

    public static final String APPOINTMENT = Appointment.class.getSimpleName();
    public static final String EXTERNAL_LINK = ExternalLink.class.getSimpleName();

    public final String type;

    protected DataType(String type) {
        this.type = type;
    }

    public static DataType fromRawMap(HashMap map) {
        RawDataTypeWrapper w = new RawDataTypeWrapper(map);
        if (APPOINTMENT.equalsIgnoreCase(w.getType())) {
            return Appointment.fromWrapper(w);
        } else if (EXTERNAL_LINK.equalsIgnoreCase(w.getType())) {
            return ExternalLink.fromWrapper(w);
        }
        return null;
    }

    abstract public  <Y extends DataType> Y expandToType();

    protected static class RawDataTypeWrapper {

        final Map<String,Object> values;

        protected RawDataTypeWrapper(Map<String, Object> values) {
            this.values = values;
        }

        public String getType() {
            return getString("type");
        }

        public String getString(String key) {
            return get(key, String.class);
        }

        public <T> T get(String key, Class<T> tClass) {
            try {
                return (T)values.get(key);
            } catch (ClassCastException e) {
                return null;
            }
        }

    }
}
