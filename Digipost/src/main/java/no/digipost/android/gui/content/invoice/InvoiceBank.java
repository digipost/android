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

package no.digipost.android.gui.content.invoice;

import android.os.Parcel;
import android.os.Parcelable;

public class InvoiceBank implements Parcelable{

    private String name;
    private String url;
    private String logo;
    public boolean setupIsAvailable;

    public InvoiceBank(String name, String url, String logo, boolean setupIsAvailable){
        this.name = name;
        this.url = url;
        this.logo = logo;
        this.setupIsAvailable = setupIsAvailable;
    }

    public static final Creator<InvoiceBank> CREATOR = new Creator<InvoiceBank>() {
        @Override
        public InvoiceBank createFromParcel(Parcel in) {
            return new InvoiceBank(in);
        }

        @Override
        public InvoiceBank[] newArray(int size) {
            return new InvoiceBank[size];
        }
    };
    public String getName(){return this.logo;}
    public String getUrl(){return this.logo;}
    public String getLogo(){return this.logo;}

    protected InvoiceBank(Parcel in) {
        name = in.readString();
        url = in.readString();
        logo = in.readString();
        setupIsAvailable = in.readByte() != 0;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(url);
        parcel.writeString(logo);
        parcel.writeByte((byte) (setupIsAvailable ? 1 : 0));
    }
}
