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

package no.digipost.android.gui.datatype;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitArray;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.oned.Code39Writer;

import java.util.regex.Pattern;

import no.digipost.android.model.datatypes.Barcode;

public class BarcodeUtil {

    private static final int BARCODE_HEIGHT = 50;
    private static final int BARCODE_WIDTH = 400;
    private static final int BITS_IN_INT = 32;

    private static final Pattern code39 = Pattern.compile("^code.?39$", Pattern.CASE_INSENSITIVE);
    private static final Pattern code128 = Pattern.compile("^((code)|(ean)|(gs1)).?128|ucc$", Pattern.CASE_INSENSITIVE);

    public static Bitmap createBarcodeBitmap(Barcode barcode) {
        try {
            BitMatrix code;
            if (code39.matcher(barcode.barcodeType).find()) {
                code = createCode39Bitmap(barcode.barcodeValue);
            } else if (code128.matcher(barcode.barcodeType).find()) {
                code = createCode128Bitmap(barcode.barcodeValue);
            } else {
                return null;
            }
            return renderBitmap(code);

        } catch (WriterException e) {
            return null;
        }
    }

    private static Bitmap renderBitmap(BitMatrix code) {
        int[] row = unpack2DBitMatrix(code);

        int[] image = new int[ row.length * BARCODE_HEIGHT ];
        for (int k = 0; k < BARCODE_HEIGHT; ++k) {
            System.arraycopy(row, 0, image, k*row.length, row.length);
        }
        return Bitmap.createBitmap(image, row.length, BARCODE_HEIGHT, Bitmap.Config.RGB_565);
    }

    private static int[] unpack2DBitMatrix(BitMatrix code) {
        int[] row = new int[ code.getRowSize() * BITS_IN_INT ];
        int cursor = 1;
        int j = 0;
        BitArray bitArray = new BitArray(code.getRowSize() * BITS_IN_INT);
        code.getRow(0, bitArray);

        for (int f : bitArray.getBitArray()) {
            for (int i = 0; i < BITS_IN_INT; ++i) {
                cursor = cursor << 1;
                row[ i + (j * BITS_IN_INT)] = (cursor & f) == 0 ? Color.WHITE : Color.BLACK;
            }
            cursor = 1;
            ++j;
        }
        return row;
    }

    public static BitMatrix createCode39Bitmap(String barcodeValue) throws WriterException {
        return new Code39Writer().encode(barcodeValue, BarcodeFormat.CODE_39, (16 * (barcodeValue.length() +2 ) -1)*2, 1);
    }

    public static BitMatrix createCode128Bitmap(String barcodeValue) throws WriterException {
        return new Code128Writer().encode(barcodeValue, BarcodeFormat.CODE_128, (11 * barcodeValue.length() + 24)*2, 1);
    }

}
