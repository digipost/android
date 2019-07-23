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

public class Barcode extends DataType {
    public final String barcodeValue;
    public final String barcodeType;
    public final String barcodeText;
    public final boolean showValueInBarcode;

    private Barcode(String barcodeValue, String barcodeType, String barcodeText, Boolean showValueInBarcode) {
        super(Barcode.class.getSimpleName());
        this.barcodeValue = barcodeValue;
        this.barcodeType = barcodeType;
        this.barcodeText = barcodeText;
        this.showValueInBarcode = showValueInBarcode != null ? showValueInBarcode : true;
    }

    public static Barcode fromWrapper(RawDataTypeWrapper w) {
        return new Barcode(w.getString("barcodeValue"), w.getString("barcodeType"), w.getString("barcodeText"), w.get("showValueInBarcode", Boolean.class));
    }

    @Override
    public Barcode expandToType() {
        return this;
    }
}
