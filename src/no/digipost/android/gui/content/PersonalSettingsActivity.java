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

import java.util.ArrayList;

import no.digipost.android.R;
import no.digipost.android.model.Account;
import no.digipost.android.model.Address;
import no.digipost.android.model.PrimaryAccount;
import no.digipost.android.model.Settings;
import no.digipost.android.utilities.ApplicationUtilities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;

public class PersonalSettingsActivity extends DigipostSettingsActivity {
	private TextView personalidentificationnumberObfuscated;
	private TextView fullName;
	private TextView address;
	private TextView phonenumber;
	private TextView email;
	private TextView digipostaddress;

	private CheckBox acceptsInformation;
	private CheckBox visibleInSearch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_personal_settings);
        ApplicationUtilities.setScreenRotationFromPreferences(this);

		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setTitle(getString(R.string.pref_screen_personal_settings_title));

		createUI();
	}

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this);
    }

    private void createUI() {
		personalidentificationnumberObfuscated = (TextView) findViewById(R.id.personal_settings_personalidentificationnumberObfuscated);
		fullName = (TextView) findViewById(R.id.personal_settings_fullName);
		address = (TextView) findViewById(R.id.personal_settings_address);
		phonenumber = (TextView) findViewById(R.id.personal_settings_phonenumber);
		email = (TextView) findViewById(R.id.personal_settings_email);
		digipostaddress = (TextView) findViewById(R.id.personal_settings_digipostaddress);

		acceptsInformation = (CheckBox) findViewById(R.id.personal_settings_acceptsInformation);
		visibleInSearch = (CheckBox) findViewById(R.id.personal_settings_visibleInSearch);

		settingsButton = (Button) findViewById(R.id.personal_settings_save);
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

	@Override
	protected void setSettingsEnabled(boolean state) {
		acceptsInformation.setEnabled(state);
		visibleInSearch.setEnabled(state);

		setButtonState(state, getString(R.string.pref_personal_settings_button));
	}

	private String getEmailString(Settings settings) {
		ArrayList<String> input = settings.getEmail();
		StringBuilder output = new StringBuilder();

		for (int i = 0; i < input.size(); i++) {
			output.append(input.get(i));

			if (i < (input.size() - 1)) {
				output.append("\n");
			}
		}

		return output.toString();
	}

	private String getAddressString(Address address) {
        String street = address.getStreet();
        String houseNumber = address.getHouseNumber();
        String houseLetter = address.getHouseLetter();
        String zipCode = address.getZipCode();
        String city = address.getCity();

		StringBuilder output = new StringBuilder();

        if (street != null) {
            output.append(street);

            if (houseNumber != null) {
                output.append(" ");
                output.append(houseNumber);

                if (houseLetter != null) {
                    output.append(houseLetter);
                }
            }

            output.append(", ");
        }

        if (zipCode != null) {
            output.append(zipCode);
            output.append(" ");
        }

        if (city != null) {
            output.append(city);
        }

		return output.toString();
	}

	private String getAddressStringComplete(Account account) {
		ArrayList<Address> input = account.getPrimaryAccount().getAddress();
		StringBuilder output = new StringBuilder();

		for (int i = 0; i < input.size(); i++) {
			output.append(getAddressString(input.get(i)));

			if (i < (input.size() - 1)) {
				output.append("\n");
			}
		}

		return output.toString();
	}

	@Override
	protected void setAccountInfo(Account account) {
		PrimaryAccount primaryAccount = account.getPrimaryAccount();

		personalidentificationnumberObfuscated.setText(primaryAccount.getPersonalidentificationnumberObfuscated());
		fullName.setText(primaryAccount.getFullName());
		address.setText(getAddressStringComplete(account));
		digipostaddress.setText(primaryAccount.getDigipostaddress());
	}

	@Override
	protected void updateUI(Settings settings) {
		phonenumber.setText(settings.getPhonenumber());
		email.setText(getEmailString(settings));

		acceptsInformation.setChecked(Boolean.parseBoolean(settings.getAcceptsInformation()));
		visibleInSearch.setChecked(Boolean.parseBoolean(settings.getVisibleInSearch()));
	}

	@Override
	protected void setSelectedAccountSettings() throws Exception {
		accountSettings.setAcceptsInformation(Boolean.toString(acceptsInformation.isChecked()));
		accountSettings.setVisibleInSearch(Boolean.toString(visibleInSearch.isChecked()));
	}
}
