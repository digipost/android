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

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import no.digipost.android.model.datatypes.TimeInterval;

public class FormatUtilities {

    private static final SimpleDateFormat ISO8601_DATE = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat ISO8601_DATETIME = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ", Locale.US);
    private static final SimpleDateFormat DATETIME_WITHOUT_MILLIS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZ", Locale.US);

    public static Date parseDate(String date) {
        if (date != null) {
            try {
                return ISO8601_DATETIME.parse(date);
            } catch (ParseException e1) {
                try {
                    return DATETIME_WITHOUT_MILLIS.parse(date);
                } catch (ParseException e2) {
                    //Ignore
                }
            }
        }
        return null;
    }

    public static String formatTimeString(String date) {
        Date parsedDate = parseDate(date);
        return parsedDate != null ? formatTime(parsedDate) : null;
    }

    @NotNull
    public static String formatTime(@NotNull Date date) {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(date);
    }

    public static String formatTimeInterval(TimeInterval interval) {
        String firstDay = formatDate(interval.startTime);
        String lastDay  = formatDate(interval.endTime);
        if (firstDay.equals(lastDay)) {
            return firstDay + ", kl. " + formatTime(interval.startTime) + " - " + formatTime(interval.endTime);
        } else {
            return firstDay + ", kl. " + formatTime(interval.startTime) + " - " + lastDay + ", kl. " + formatTime(interval.endTime);
        }
    }

    public static String formatDateString(String date) {
        Date parsedDate = parseDate(date);
        return parsedDate != null ? formatDate(parsedDate) : null;
    }

    public static String formatDate(Date parsedDate) {
        return new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(parsedDate);
    }

    public static String formatDateStringColloquial(String date) {
        String formatted = null;
        try {
            formatted = new SimpleDateFormat("d. MMM yyyy", Locale.getDefault())
                    .format(ISO8601_DATE.parse(date));
        } catch (ParseException e) {
            // Ignore
        }
        return formatted;
    }

    public static String formatFileSize(long bytes) {
        String[] units = new String[]{"B", "KB", "MB", "GB"};
        for (int i = units.length; i >= 0; i--) {
            double exp = Math.pow(1024, i);
            if (bytes > exp) {
                float n = (float) (bytes / exp);
                String format = i > 1 ? "%3.1f" : "%3.0f";
                return String.format(Locale.getDefault(), format, n) + " " + units[i];
            }
        }
        return Long.toString(bytes);
    }
}
