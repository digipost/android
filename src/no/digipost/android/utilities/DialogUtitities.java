package no.digipost.android.utilities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

public class DialogUtitities {

    public static void showToast(final Context context, final String text) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    public static ProgressDialog getProgressDialogWithMessage(final Context context, final String message) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(message);

        return progressDialog;
    }

    public static ProgressDialog getProgressDialogWithMessageAndTitle(final Context context, final String message, final String title) {
        ProgressDialog progressDialog = getProgressDialogWithMessage(context, message);
        progressDialog.setTitle(title);

        return progressDialog;
    }

    public static AlertDialog.Builder getAlertDialogBuilderWithMessage(final Context context, final String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);

        return builder;
    }

    public static AlertDialog.Builder getAlertDialogBuilderWithMessageAndTitle(final Context context, final String message, final String title) {
        AlertDialog.Builder builder = getAlertDialogBuilderWithMessage(context, message);
        builder.setTitle(title);

        return builder;
    }
}
