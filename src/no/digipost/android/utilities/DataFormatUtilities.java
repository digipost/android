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

package no.digipost.android.utilities;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DataFormatUtilities {
    public static final String API_DATE_FORMAT = "yyyy-MM-dd";
    public static final String GUI_DATE_FORMAT = "d. MMM yyyy";
    public static final String API_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm";
    public static final String GUI_DATETIME_FORMAT = "d. MMM yyyy, HH:mm";

    public static String getFormattedDate(final String date) {
        String date_substring = date.substring(0, 10);
        SimpleDateFormat fromApi = new SimpleDateFormat(API_DATE_FORMAT);
        SimpleDateFormat guiFormat = new SimpleDateFormat(GUI_DATE_FORMAT, Locale.getDefault());
        String formatted = null;
        try {
            formatted = guiFormat.format(fromApi.parse(date_substring));
        } catch (ParseException e) {
            // Ignore
        }
        return formatted;
    }

    public static String getFormattedDateTime(final String date) {
        String date_substring = date.substring(0, 16);
        SimpleDateFormat fromApi = new SimpleDateFormat(API_DATETIME_FORMAT);
        SimpleDateFormat guiFormat = new SimpleDateFormat(GUI_DATETIME_FORMAT, Locale.getDefault());
        String formatted = null;
        try {
            formatted = guiFormat.format(fromApi.parse(date_substring));
        } catch (ParseException e) {
            // Ignore
        }
        return formatted;
    }

    public static String getFormattedFileSize(final String byteString) {
        long bytes = Long.parseLong(byteString);
        String[] units = new String[] { "", "KB", "MB", "GB" };
        for (int i = 3; i > 0; i--) {
            double exp = Math.pow(1024, i);
            if (bytes > exp) {
                float n = (float) (bytes / exp);
                if (i == 1) {
                    return (int) n + " " + units[i];
                }
                return String.format("%3.1f %s", n, units[i]);
            }
        }
        return Long.toString(bytes);
    }

    public static String getFormattedAmount(final String amount) {
        Double number = Double.valueOf(amount);
        DecimalFormat dec = new DecimalFormat("#.00");

        return dec.format(number);
    }

    public static String getFormattedCurrency(final String currency) {
        if (currency.equals("NOK")) {
            return "kr.";
        }

        return currency;
    }
}
