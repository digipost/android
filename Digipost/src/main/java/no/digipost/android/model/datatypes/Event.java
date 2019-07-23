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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Event extends DataType {
    public final String subTitle;
    public final List<TimeInterval> time;
    public final String timeLabel;
    public final String description;
    public final String place;
    public final String placeLabel;
    public final DataTypeAddress address;
    public final List<Info> info;
    public final String barcodeLabel;
    public final Barcode barcode;
    public final List<DataTypeLink> links;

    private Event(String subTitle, List<TimeInterval> time, String timeLabel, String description, String place, String placeLabel, DataTypeAddress address, List<Info> info, String barcodeLabel, Barcode barcode, List<DataTypeLink> links) {
        super(Event.class.getSimpleName());
        this.subTitle = subTitle;
        this.time = time;
        this.timeLabel = timeLabel != null ? timeLabel : "Tidspunkt";
        this.description = description;
        this.place = place;
        this.placeLabel = placeLabel != null ? placeLabel : "Sted";
        this.address = address;
        this.info = info;
        this.barcodeLabel = barcodeLabel;
        this.barcode = barcode;
        this.links = links;
    }

    static Event fromWrapper(RawDataTypeWrapper w) {
        ArrayList<TimeInterval> timeList = new ArrayList<>();
        for (Object time: w.get("time", List.class)) {
            timeList.add(TimeInterval.fromWrapper(new RawDataTypeWrapper((HashMap<String,Object>)time)));
        }
        ArrayList<Info> infoList = new ArrayList<>();
        for (Object info: w.get("info", List.class)) {
            infoList.add(Info.fromWrapper(new RawDataTypeWrapper((HashMap<String,Object>)info)));
        }
        ArrayList<DataTypeLink> links = new ArrayList<>();
        for (Object link: w.get("links", List.class)) {
            links.add(DataTypeLink.fromWrapper(new RawDataTypeWrapper((HashMap<String,Object>)link)));
        }
        return new Event(w.getString("subTitle"), timeList,
                w.getString("timeLabel"), w.getString("description"), w.getString("place"), w.getString("placeLabel"),
                DataTypeAddress.fromWrapper(new RawDataTypeWrapper(w.get("address", HashMap.class))), infoList,
                w.getString("barcodeLabel"), Barcode.fromWrapper(new RawDataTypeWrapper(w.get("barcode", HashMap.class))), links);
    }

    @Override
    public Event expandToType() {
        return this;
    }

    public String getInfoTitleAndText() {
        StringBuilder infoText = new StringBuilder();
        for (Info i : info) {
            infoText.append(i.title).append("\n").append(i.text).append("\n\n");
        }
        return infoText.toString();
    }

    public String getPlaceAddress() {
        if(address != null) {
            return address.streetAddress + ", " + address.postalCode + " " + address.city;
        }
        return "";
    }
}
