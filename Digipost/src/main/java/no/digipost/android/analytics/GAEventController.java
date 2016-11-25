package no.digipost.android.analytics;

import android.app.Activity;
import android.support.v7.app.ActionBar;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import no.digipost.android.DigipostApplication;

public class GAEventController {

    public static final String appLaunchOrigin = "app-launch-origin";
    public static final String loginRememberMeOption = "login-remember-me";
    public static final String LAUNCH_ORIGIN_NORMAL = "normal";;
    public static final String LAUNCH_ORIGIN_PUSH = "push";
    public static final String CONTEXT_BASED_TIPS = "kontekstbaserte-tips";

    public static void sendLaunchEvent(Activity activity, String actionText){
        sendEvent(activity, appLaunchOrigin, actionText, appLaunchOrigin+"-"+actionText);
    }

    public static void sendRememberMeEvent(Activity activity, boolean enabled){
        String actionText = (enabled ? "true" : "false");
        sendEvent(activity, loginRememberMeOption, actionText, loginRememberMeOption+"-"+actionText);
    }

    public static void sendInvoiceCLickedChooseBankDialog(Activity activity, String buttonText){
        sendEvent(activity, CONTEXT_BASED_TIPS, "klikk-dialog-knapp", buttonText);
    }

    public static void sendInvoiceOpenBankViewFromListEvent(Activity activity, String bank){
        sendEvent(activity, CONTEXT_BASED_TIPS, "velg-bank-fra-liste", bank);
    }

    public static void sendInvoiceClickedDigipostOpenPagesLink(Activity activity, String bank){
        sendEvent(activity, CONTEXT_BASED_TIPS, "klikk-digipost-faktura-åpne-sider", bank);
    }

    public static void sendInvoiceClickedSetup10Link(Activity activity, String bank){
        sendEvent(activity, CONTEXT_BASED_TIPS, "klikk-oppsett-1.0-link", bank);
    }

    public static void sendInvoiceClickedSetup20Link(Activity activity, String bank){
        sendEvent(activity, CONTEXT_BASED_TIPS, "klikk-oppsett-2.0-link", bank);
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
