package no.digipost.android.gui.content;

import java.util.ArrayList;

import no.digipost.android.R;
import no.digipost.android.model.Account;
import no.digipost.android.model.Settings;
import no.digipost.android.model.ValidationRules;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class NotificationSettingsActivity extends DigipostSettingsActivity {
	private CheckBox newLetters;
	private CheckBox unreadLetters;
	private CheckBox importantLetters;
	private EditText mobileNumber;
	private EditText email1;
	private EditText email2;
	private EditText email3;

	private ValidationRules validationRules;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification_settings);

		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setTitle(getString(R.string.pref_screen_notification_settings_title));

		createUI();
	}

	private void createUI() {
		newLetters = (CheckBox) findViewById(R.id.notification_settings_new_letters);
		unreadLetters = (CheckBox) findViewById(R.id.notification_settings_unread_letters);
		importantLetters = (CheckBox) findViewById(R.id.notification_settings_important_letters);
		mobileNumber = (EditText) findViewById(R.id.notification_settings_mobile);
		email1 = (EditText) findViewById(R.id.notification_settings_email1);
		email2 = (EditText) findViewById(R.id.notification_settings_email2);
		email3 = (EditText) findViewById(R.id.notification_settings_email3);
		settingsButton = (Button) findViewById(R.id.notification_settings_save);
	}

	@Override
	protected void updateUI(Settings settings) {
		newLetters.setChecked(Boolean.parseBoolean(settings.getNotificationEmail()));
		unreadLetters.setChecked(Boolean.parseBoolean(settings.getReminderEmail()));
		importantLetters.setChecked(Boolean.parseBoolean(settings.getNotificationSmsPaidBySender()));

		mobileNumber.setText((settings.getPhonenumber() != null) ? settings.getPhonenumber() : "");

		ArrayList<String> emails = settings.getEmail();

		email1.setText((emails.size() > 0) ? emails.get(0) : "");
		email2.setText((emails.size() > 1) ? emails.get(1) : "");
		email3.setText((emails.size() > 2) ? emails.get(2) : "");
	}

	@Override
	protected void setSettingsEnabled(boolean state) {
		newLetters.setEnabled(state);
		unreadLetters.setEnabled(state);
		importantLetters.setEnabled(state);
		mobileNumber.setEnabled(state);
		email1.setEnabled(state);
		email2.setEnabled(state);
		email3.setEnabled(state);

		super.setButtonState(state, getString(R.string.pref_notification_settings_button));
	}

	@Override
	protected void setAccountInfo(Account account) {
		validationRules = account.getValidationRules();
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

		String stringEmail = email1.getText().toString().trim();

		if (!stringEmail.equals("")) {
			emails.add(stringEmail);
		}

		stringEmail = email2.getText().toString().trim();

		if (!stringEmail.equals("")) {
			emails.add(stringEmail);
		}

		stringEmail = email3.getText().toString().trim();

		if (!stringEmail.equals("")) {
			emails.add(stringEmail);
		}

		return emails;
	}

	@Override
	protected void setSelectedAccountSettings() throws Exception {
		accountSettings.setNotificationEmail(Boolean.toString(newLetters.isChecked()));
		accountSettings.setReminderEmail(Boolean.toString(unreadLetters.isChecked()));
		accountSettings.setNotificationSmsPaidBySender(Boolean.toString(importantLetters.isChecked()));

		String stringMobileNumber = mobileNumber.getText().toString().trim();
		validateMobileNumber(stringMobileNumber);
		accountSettings.setPhonenumber(stringMobileNumber);

		ArrayList<String> emails = getEmails();
		validateEmails(emails);
		accountSettings.setEmail(emails);
	}
}
