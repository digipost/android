package no.digipost.android.utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class DataFormatUtilities {
    public static final String API_DATE_FORMAT = "yyyy-MM-dd";
    public static final String GUI_DATE_FORMAT = "d. MMM yyyy";

    public static String getFormattedDate(final String date) {
        String date_substring = date.substring(0, 10);
        SimpleDateFormat fromApi = new SimpleDateFormat(API_DATE_FORMAT);
        SimpleDateFormat guiFormat = new SimpleDateFormat(GUI_DATE_FORMAT, Locale.getDefault());
        String formatted = null;
        try {
            formatted = guiFormat.format(fromApi.parse(date_substring));
        } catch (ParseException e) {
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
}
