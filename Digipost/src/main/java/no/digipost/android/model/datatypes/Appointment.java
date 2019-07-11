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

package no.digipost.android.model.datatypes;

import no.digipost.android.utilities.FormatUtilities;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Appointment extends DataType {

    public final String title;
    public final String subTitle;
    public final String startTime;
    public final String endTime;
    public final String arrivalTime;
    public final DataTypeAddress address;
    public final String place;

    public List<Info> info;

    private Appointment(String title, String subTitle, String startTime, String endTime, String arrivalTime, DataTypeAddress address, String place, List<Info> infoList) {
        super(Appointment.class.getSimpleName());
        this.title = title;
        this.subTitle = subTitle;
        this.startTime = startTime;
        this.endTime = endTime;
        this.arrivalTime = arrivalTime;
        this.address = address;
        this.place = place;
    }

    public static Appointment fromWrapper(RawDataTypeWrapper w) {
        ArrayList<Info> infolist = new ArrayList<>();
        for (Object info: w.get("info", List.class)) {
            infolist.add(Info.fromWrapper(new RawDataTypeWrapper((HashMap<String,Object>)info)));
        }
        return new Appointment(w.getString("title"), w.getString("subTitle"),
                w.getString("startTime"), w.getString("endTime"), w.getString("arrivalTime"),
                DataTypeAddress.fromWrapper(new RawDataTypeWrapper(w.get("address", HashMap.class))),
                w.getString("place"), infolist);
    }

    public String getStartTimeString() { return "kl " + FormatUtilities.getTimeString(startTime);};

    public String getStartDateString() { return FormatUtilities.getDateString(startTime);};

    public String getPlace() { return place;}

    public String getPlaceAddress() {
        if(address != null) {
            return address.streetAddress + ", " + address.postalCode + " " + address.city;
        }

        return "";
    }

    public String getArrivalInfo() {
        String arrivalDateString = FormatUtilities.getTimeString(arrivalTime);
        if (arrivalDateString != null) {
            return "kl " + arrivalDateString;
        } else if(arrivalTime != null) {
            return arrivalTime;
        }

        return "";
    }

    public String getInfoTitleAndText() {
        String infoText = "";
        for (Info i : info) {
            infoText += i.title + "\n" + i.text + "\n\n";
        }
        return infoText;
    }

    public Date getStartDate() {
        return FormatUtilities.getDate(startTime);
    }

    public Date getEndDate() {
        return FormatUtilities.getDate(endTime);
    }

    @Override
    public Appointment expandToType() {
        return this;
    }
}