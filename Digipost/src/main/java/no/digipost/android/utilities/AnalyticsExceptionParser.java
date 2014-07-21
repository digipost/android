package no.digipost.android.utilities;

import com.google.analytics.tracking.android.ExceptionParser;

import org.apache.commons.lang.exception.ExceptionUtils;

public class AnalyticsExceptionParser implements ExceptionParser {

    public String getDescription(String p_thread, Throwable p_throwable) {
        return "Thread: " + p_thread + ", Exception: " + ExceptionUtils.getStackTrace(p_throwable);
    }
}
