package no.digipost.android.gui.content;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.sun.media.sound.InvalidFormatException;

import java.util.ArrayList;
import java.util.IllegalFormatException;

import no.digipost.android.R;
import no.digipost.android.api.ContentOperations;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.model.Account;
import no.digipost.android.model.Settings;
import no.digipost.android.model.ValidationRules;
import no.digipost.android.utilities.DialogUtitities;

public class NotificationSettingsActivity extends Activity {
    private CheckBox newLetters;
    private CheckBox unreadLetters;
    private CheckBox importantLetters;
    private EditText mobileNumber;
    private EditText email1;
    private EditText email2;
    private EditText email3;

    private Settings accountSettings;
    private ValidationRules validationRules;

    private ProgressDialog settingsProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setTitle("Varslingsinnstillinger");

        createUI();

        executeGetValidationRulesTask();
    }

    private void createUI() {
        newLetters = (CheckBox) findViewById(R.id.notification_settings_new_letters);
        unreadLetters = (CheckBox) findViewById(R.id.notification_settings_unread_letters);
        importantLetters = (CheckBox) findViewById(R.id.notification_settings_important_letters);
        mobileNumber = (EditText) findViewById(R.id.notification_settings_mobile);
        email1 = (EditText) findViewById(R.id.notification_settings_email1);
        email2 = (EditText) findViewById(R.id.notification_settings_email2);
        email3 = (EditText) findViewById(R.id.notification_settings_email3);

        Button save = (Button) findViewById(R.id.notification_settings_save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                executeUpdateSettingsTask();
            }
        });
    }

    private void updateUI() {
        newLetters.setChecked(Boolean.parseBoolean(accountSettings.getNotificationEmail()));
        unreadLetters.setChecked(Boolean.parseBoolean(accountSettings.getReminderEmail()));
        importantLetters.setChecked(Boolean.parseBoolean(accountSettings.getNotificationSmsPaidBySender()));

        mobileNumber.setText((accountSettings.getPhonenumber() != null) ? accountSettings.getPhonenumber() : "");

        ArrayList<String> emails = accountSettings.getEmail();

        email1.setText((emails.size() > 0) ? emails.get(0) : "");
        email2.setText((emails.size() > 1) ? emails.get(1) : "");
        email3.setText((emails.size() > 2) ? emails.get(2) : "");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return onOptionsItemSelected(item);
    }

    private void showSettingsProgressDialog(String message) {
        settingsProgressDialog = DialogUtitities.getProgressDialogWithMessage(this, message);
        settingsProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.abort), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int which) {
                dialog.dismiss();
            }
        });

        settingsProgressDialog.show();
    }

    private void hideSettingsProgressDialog() {
        settingsProgressDialog.dismiss();
        settingsProgressDialog = null;
    }

    private void executeGetValidationRulesTask() {
        GetValidationRulesTask getValidationRulesTask = new GetValidationRulesTask();
        getValidationRulesTask.execute();
    }

    private class GetValidationRulesTask extends AsyncTask<Void, Void, ValidationRules> {
        private String errorMessage;

        @Override
        protected ValidationRules doInBackground(Void... voids) {
            try {
                return ContentOperations.getAccount(NotificationSettingsActivity.this).getValidationRules();
            } catch (DigipostApiException e) {
                errorMessage = e.getMessage();
                return null;
            } catch (DigipostClientException e) {
                errorMessage = e.getMessage();
                return null;
            } catch (DigipostAuthenticationException e) {
                errorMessage = e.getMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ValidationRules result) {
            super.onPostExecute(validationRules);

            if (result == null) {
                DialogUtitities.showToast(NotificationSettingsActivity.this, errorMessage);
                // ToDo invalid token
                finish();
            } else {
                validationRules = result;
                executeGetSettingsTask();
            }
        }
    }

    private void executeGetSettingsTask() {
        GetSettingsTask getSettingsTask = new GetSettingsTask();
        getSettingsTask.execute();
    }

    private class GetSettingsTask extends AsyncTask<Void, Void, Settings> {
        private String errorMessage;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showSettingsProgressDialog("Laster inn dine innstillinger...");
        }

        @Override
        protected Settings doInBackground(Void... voids) {
            try {
                return ContentOperations.getSettings(NotificationSettingsActivity.this);
            } catch (DigipostClientException e) {
                errorMessage = e.getMessage();
                return null;
            } catch (DigipostAuthenticationException e) {
                errorMessage = e.getMessage();
                return null;
            } catch (DigipostApiException e) {
                errorMessage = e.getMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Settings settings) {
            super.onPostExecute(settings);
            hideSettingsProgressDialog();

            if (settings == null) {
                DialogUtitities.showToast(NotificationSettingsActivity.this, errorMessage);

                // ToDo invalid token
                finish();
            } else {
                accountSettings = settings;
                updateUI();
            }
        }
    }

    private void validateMobileNumber(String mobileNumber) throws Exception {
        if (!mobileNumber.matches(validationRules.getPhoneNumber())) {
            throw new Exception("Ikke gyldig telefonnummer: " + mobileNumber);
        }
    }

    private void validateEmails(ArrayList<String> emails) throws Exception {
        for (String email : emails) {
            if (!email.matches(validationRules.getEmail())) {
                throw new Exception("Ikke gyldig email-adresse: " + email);
            }
        }
    }

    private ArrayList<String> getEmails() {
        ArrayList<String> emails = new ArrayList<String>();

        String stringEmail = email1.getText().toString();

        if (!stringEmail.equals("")) {
            emails.add(stringEmail);
        }

        stringEmail = email2.getText().toString();

        if (!stringEmail.equals("")) {
            emails.add(stringEmail);
        }

        stringEmail = email3.getText().toString();

        if (!stringEmail.equals("")) {
            emails.add(stringEmail);
        }

        return emails;
    }

    private void setSelectedSettings() throws Exception {
        accountSettings.setNotificationEmail(Boolean.toString(newLetters.isChecked()));
        accountSettings.setReminderEmail(Boolean.toString(unreadLetters.isChecked()));
        accountSettings.setNotificationSmsPaidBySender(Boolean.toString(importantLetters.isChecked()));

        String stringMobileNumber = mobileNumber.getText().toString();
        validateMobileNumber(stringMobileNumber);
        accountSettings.setPhonenumber(stringMobileNumber);

        ArrayList<String> emails = getEmails();
        validateEmails(emails);
        accountSettings.setEmail(emails);
    }

    private void showInvalidFormatDialog(String message) {
        AlertDialog.Builder builder = DialogUtitities.getAlertDialogBuilderWithMessage(this, message);
        builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    private void executeUpdateSettingsTask() {
        try {
            setSelectedSettings();
            UpdateSettingsTask updateSettingsTask = new UpdateSettingsTask();
            updateSettingsTask.execute();
        } catch (Exception e) {
            showInvalidFormatDialog(e.getMessage());
        }
    }

    private class UpdateSettingsTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showSettingsProgressDialog("Oppdaterer dine innstillinger...");
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                ContentOperations.updateAccountSettings(NotificationSettingsActivity.this, accountSettings);
                return null;
            } catch (DigipostAuthenticationException e) {
                return e.getMessage();
            } catch (DigipostClientException e) {
                return e.getMessage();
            } catch (DigipostApiException e) {
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            hideSettingsProgressDialog();

            if (result != null) {
                DialogUtitities.showToast(NotificationSettingsActivity.this, result);

                // ToDo invalid token
            } else {
                DialogUtitities.showToast(NotificationSettingsActivity.this, "Dine varslingsinnstillinger ble oppdatert.");
            }
        }
    }
}
