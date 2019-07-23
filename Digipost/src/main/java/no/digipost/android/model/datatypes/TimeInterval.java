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

import java.util.Date;

import static no.digipost.android.utilities.FormatUtilities.parseDate;

public class TimeInterval extends DataType {
    public final Date startTime;
    public final Date endTime;

    TimeInterval(Date start, Date end) {
        super(TimeInterval.class.getSimpleName());
        this.startTime = start;
        this.endTime = end;
    }

    public static TimeInterval fromWrapper(RawDataTypeWrapper w) {
        return new TimeInterval(parseDate(w.getString("startTime")), parseDate(w.getString("endTime")));
    }

    @Override
    public TimeInterval expandToType() {
        return this;
    }
}
