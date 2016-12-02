package no.digipost.android.analytics;

import android.app.Activity;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import no.digipost.android.DigipostApplication;

public class GAEventController {

    public static final String appLaunchOrigin = "app-launch-origin";
    public static final String loginRememberMeOption = "login-remember-me";
    public static final String LAUNCH_ORIGIN_NORMAL = "normal";;
    public static final String LAUNCH_ORIGIN_PUSH = "push";
    public static final String FAKTURA_AVTALE_OPPSETT_KONTEKST_BASERT = "faktura-avtale-oppsett-kontekst-basert";

    public static void sendLaunchEvent(Activity activity, String actionText){
        sendEvent(activity, appLaunchOrigin, actionText, appLaunchOrigin+"-"+actionText);
    }

    public static void sendRememberMeEvent(Activity activity, boolean enabled){
        String actionText = (enabled ? "true" : "false");
        sendEvent(activity, loginRememberMeOption, actionText, loginRememberMeOption+"-"+actionText);
    }

    public static void sendInvoiceClickedChooseBankDialog(Activity activity, String buttonText){
        sendEvent(activity, FAKTURA_AVTALE_OPPSETT_KONTEKST_BASERT, "klikk-start-oppsett", buttonText);
    }

    public static void sendInvoiceOpenBankViewFromListEvent(Activity activity, String bank){
        sendEvent(activity, FAKTURA_AVTALE_OPPSETT_KONTEKST_BASERT, "klikk-bank-i-liste", bank);
    }

    public static void sendInvoiceClickedDigipostOpenPagesLink(Activity activity, String bank){
        sendEvent(activity, FAKTURA_AVTALE_OPPSETT_KONTEKST_BASERT, "klikk-digipost-faktura-åpne-sider", bank);
    }

    public static void sendInvoiceClickedSetup10Link(Activity activity, String bank){
        sendEvent(activity, FAKTURA_AVTALE_OPPSETT_KONTEKST_BASERT, "klikk-oppsett-avtale-type-1-link", bank);
    }

    public static void sendInvoiceClickedSetup20Link(Activity activity, String bank){
        sendEvent(activity, FAKTURA_AVTALE_OPPSETT_KONTEKST_BASERT, "klikk-oppsett-avtale-type-2-link", bank);
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
