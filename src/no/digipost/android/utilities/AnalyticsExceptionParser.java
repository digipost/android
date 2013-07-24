package no.digipost.android.utilities;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.google.analytics.tracking.android.ExceptionParser;

public class AnalyticsExceptionParser implements ExceptionParser {

	public String getDescription(String p_thread, Throwable p_throwable) {
		return "Thread: " + p_thread + ", Exception: " + ExceptionUtils.getStackTrace(p_throwable);
	}
}
