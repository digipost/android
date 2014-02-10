package no.digipost.android;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.ExceptionReporter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import no.digipost.android.utilities.AnalyticsExceptionParser;

public class DigipostApplication extends Application {

    public static String USER_AGENT;
    
    @Override
    public void onCreate() {
        super.onCreate();

        /* Universal Image Loader */
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
        .build();
        ImageLoader.getInstance().init(config);

        /* Google Analytics */
        EasyTracker.getInstance().setContext(this);

        Thread.UncaughtExceptionHandler uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        if (uncaughtExceptionHandler instanceof ExceptionReporter) {
            ExceptionReporter exceptionReporter = (ExceptionReporter) uncaughtExceptionHandler;
            exceptionReporter.setExceptionParser(new AnalyticsExceptionParser());
        }

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionName = pInfo.versionName;
            String systemUserAgent = System.getProperty("http.agent");
            USER_AGENT = "Digipost/" + versionName + " " + systemUserAgent;
        } catch (PackageManager.NameNotFoundException ignored) {
        }

    }
}
