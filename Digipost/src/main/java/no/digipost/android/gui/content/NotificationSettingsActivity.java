/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.digipost.android.gui.content;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import com.google.android.gms.analytics.GoogleAnalytics;
import no.digipost.android.DigipostApplication;
import no.digipost.android.R;
import no.digipost.android.analytics.GAEventController;
import no.digipost.android.model.Account;
import no.digipost.android.model.ExtendedEmail;
import no.digipost.android.model.MailboxSettings;
import no.digipost.android.model.ValidationRules;
import no.digipost.android.utilities.DialogUtitities;

import java.util.ArrayList;

import static no.digipost.android.utilities.ValidationUtillities.*;

public class NotificationSettingsActivity extends DigipostSettingsActivity {

    private EditText countryCode, mobileNumber, email1, email2, email3;
    private ValidationRules validationRules;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((DigipostApplication) getApplication()).getTracker(DigipostApplication.TrackerName.APP_TRACKER);
        setContentView(R.layout.activity_notification_settings);
        createUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_notification_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_save){
            save();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar =  getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.pref_screen_notification_settings_title));
        }

        countryCode = findViewById(R.id.notification_settings_countrycode);
        mobileNumber = findViewById(R.id.notification_settings_mobile);
        email1 = findViewById(R.id.notification_settings_email1);
        email2 = findViewById(R.id.notification_settings_email2);
        email3 = findViewById(R.id.notification_settings_email3);
    }

    private void save() {
        boolean emailIsValid = emailIsValid();
        boolean phoneNumberValid = phoneNumberAppearsValid(validationRules, mobileNumber.getText().toString());

        if(emailIsValid && phoneNumberValid) {
            executeUpdateSettingsTask();
        } else{
            GAEventController.sendKontaktopplysningerOppdatert(this, "lagring", "feil i inputfelter");

            String dialogTitle;
            String dialogMessage;
            if(!emailIsValid) {
                dialogTitle = getString(R.string.pref_screen_notification_settings_invalid_email_title);
                dialogMessage = getString(R.string.pref_screen_notification_settings_invalid_email_message);
            }else {
                dialogTitle = getString(R.string.pref_screen_notification_settings_invalid_phone_title);
                dialogMessage = getString(R.string.pref_screen_notification_settings_invalid_phone_message);
            }

            AlertDialog.Builder dialog = DialogUtitities.getAlertDialogBuilderWithMessageAndTitle(NotificationSettingsActivity.this, dialogMessage, dialogTitle);
            dialog.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {}
            });
            dialog.show();
        }
    }

    private boolean emailIsValid() {
        boolean email1Valid = emailAppearsValid(validationRules, email1.getText().toString());
        boolean email2Valid = emailAppearsValid(validationRules, email2.getText().toString());
        boolean email3Valid = emailAppearsValid(validationRules, email3.getText().toString());
        return email1Valid && email2Valid && email3Valid;
    }

    @Override
    protected void updateUI(MailboxSettings mailboxSettings) {
        countryCode.setText(mailboxSettings.getCountryCode());
        mobileNumber.setText(mailboxSettings.getPhoneNumber());

        ArrayList<ExtendedEmail> emails = mailboxSettings.getExtendedEmails();
        if(emails != null) {
            email1.setText((emails.size() > 0) ? emails.get(0).getEmail() : "");
            email2.setText((emails.size() > 1) ? emails.get(1).getEmail() : "");
            email3.setText((emails.size() > 2) ? emails.get(2).getEmail() : "");
        }
    }

    @Override
    protected void setSettingsEnabled(boolean state) {
        mobileNumber.setEnabled(state);
        countryCode.setEnabled(state);
        email1.setEnabled(state);
        email2.setEnabled(state);
        email3.setEnabled(state);
    }

    @Override
    protected void setAccountInfo(Account account) {
        validationRules = account.getValidationRules();
    }

    @Override
    protected void setSelectedAccountSettings() {
        accountMailboxSettings.setCountryCode(formattedCountryCode());
        accountMailboxSettings.setPhoneNumber(mobileNumber.getText().toString().trim(), this);
        accountMailboxSettings.setEmailAddress(email1.getText().toString().trim(), 0, this);
        accountMailboxSettings.setEmailAddress(email2.getText().toString().trim(), 1, this);
        accountMailboxSettings.setEmailAddress(email3.getText().toString().trim(), 2, this);
    }


    private String formattedCountryCode() {
        String code = countryCode.getText().toString().trim();
        while (code.startsWith("0")) {
            code = code.substring(1);
        }

        if(code.isEmpty()){
            return "47";
        }

        return code;
    }
}
