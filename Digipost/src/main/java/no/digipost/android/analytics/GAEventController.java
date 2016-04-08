package no.digipost.android.analytics;

import android.app.Activity;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import no.digipost.android.DigipostApplication;

public class GAEventController {

    public static final String appLaunchOrigin = "app-launch-origin";
    public static final String loginRememberMeOption = "login-remember-me";
    public static final int LAUNCH_ORIGIN_RESUME = 0;
    public static final int LAUNCH_ORIGIN_LOGIN = 1;
    public static final int LAUNCH_ORIGIN_PUSH = 2;


    public static void sendLaunchEvent(Activity activity, int origin){
        String actionText = "resume";

        switch (origin) {
            case LAUNCH_ORIGIN_RESUME:
                actionText = "resume";
                break;
            case LAUNCH_ORIGIN_LOGIN:
                actionText = "login";
                break;
            case LAUNCH_ORIGIN_PUSH:
                actionText = "push";
                break;
        }

        sendEvent(activity, appLaunchOrigin, actionText, appLaunchOrigin+"-"+actionText);
    }

    public static void sendRememberMeEvent(Activity activity, boolean enabled){
        String actionText = (enabled ? "true" : "false");
        sendEvent(activity, loginRememberMeOption, actionText, loginRememberMeOption+"-"+actionText);
    }

    public static void sendEvent(Activity activity, String category, String action, String label){
        getTracker(activity).send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .build());
    }

    private static Tracker getTracker(Activity activity){
        return ((DigipostApplication) activity.getApplication()).getTracker(
                DigipostApplication.TrackerName.APP_TRACKER);
    }
}
