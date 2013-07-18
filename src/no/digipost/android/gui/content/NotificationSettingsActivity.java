package no.digipost.android.gui.content;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.ArrayList;

import no.digipost.android.R;
import no.digipost.android.api.ContentOperations;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.model.Settings;

public class NotificationSettingsActivity extends Activity {
    private CheckBox newLetters;
    private CheckBox unreadLetters;
    private CheckBox importantLetters;
    private EditText mobileNumber;
    private EditText email1;
    private EditText email2;
    private EditText email3;

    private Settings accountSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        getActionBar().setHomeButtonEnabled(true);

        createUI();

        executeSettingsTask();
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
                // ToDo save
            }
        });
    }

    private void setSettings() {
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

    private void executeSettingsTask() {
        GetSettingsTask getSettingsTask = new GetSettingsTask();
        getSettingsTask.execute();
    }

    private class GetSettingsTask extends AsyncTask<Void, Void, Settings> {

        @Override
        protected Settings doInBackground(Void... voids) {
            try {
                return ContentOperations.getSettings(NotificationSettingsActivity.this);
            } catch (DigipostClientException e) {
                e.printStackTrace();
                return null;
            } catch (DigipostAuthenticationException e) {
                e.printStackTrace();
                return null;
            } catch (DigipostApiException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Settings settings) {
            super.onPostExecute(settings);

            if (settings == null) {
                System.out.println("settings == null");
                return;
            }

            accountSettings = settings;
            setSettings();
        }
    }
}
