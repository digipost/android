package no.digipost.android.gui.content;

import no.digipost.android.R;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.gui.MainContentActivity;
import no.digipost.android.utilities.ApplicationUtilities;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

public class SettingsActivity extends Activity {
	public static final String KEY_PREF_GENERAL_SETTINGS = "pref_generalSettings";
	public static final String KEY_PREF_DEFAULT_SCREEN = "pref_defaultScreen";
	public static final String KEY_PREF_SCREEN_ROTATION = "pref_screenRotation";
	public static final String KEY_PREF_CONFIRM_DELETE = "pref_confirmDelete";
	public static final String KEY_PREF_CONFIRM_MOVE = "pref_confirmMove";
	public static final String KEY_PREF_DOCUMENTS_SETTINGS = "pref_documentsSettings";
	public static final String KEY_PREF_SHOW_BANK_ID_DOCUMENTS = "pref_showBankIDDocuments";
    public static final String KEY_PREF_PERSONAL_SETTINGS = "pref_personalSettings";
    public static final String KEY_PREF_NOTIFICATION_SETTINGS = "pref_notificationSettings";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ApplicationUtilities.setScreenRotationFromPreferences(this);

		getActionBar().setTitle(R.string.preferences);
		getActionBar().setHomeButtonEnabled(true);

		getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

    private void finishActivityWithAction(String action) {
        Intent intent = new Intent();
        intent.putExtra(ApiConstants.ACTION, action);
        setResult(RESULT_OK, intent);
        finish();
    }

	public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			addPreferencesFromResource(R.xml.preferences);

            Preference personalSettings = findPreference(KEY_PREF_PERSONAL_SETTINGS);
            personalSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivityForResult(preference.getIntent(), MainContentActivity.INTENT_REQUESTCODE);
                    return true;
                }
            });

            Preference notificationSettings = findPreference(KEY_PREF_NOTIFICATION_SETTINGS);
            notificationSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivityForResult(preference.getIntent(), MainContentActivity.INTENT_REQUESTCODE);
                    return true;
                }
            });

			setSummary(getPreferenceManager().getSharedPreferences(), findPreference(KEY_PREF_DEFAULT_SCREEN));
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			setSummary(sharedPreferences, findPreference(key));

			if (key.equals(KEY_PREF_SCREEN_ROTATION)) {
				ApplicationUtilities.setScreenRotationFromPreferences(getActivity());
			}
		}

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (resultCode == RESULT_OK) {
                if (requestCode == MainContentActivity.INTENT_REQUESTCODE) {
                    String action = data.getStringExtra(ApiConstants.ACTION);

                    if (action.equals(ApiConstants.LOGOUT)) {
                        finishActivityWithAction(ApiConstants.LOGOUT);
                    }
                }
            }
        }

        @Override
		public void onResume() {
			super.onResume();
			getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		}

		@Override
		public void onPause() {
			super.onPause();
			getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
		}

		private void setSummary(SharedPreferences sharedPreferences, Preference preference) {
			String key = preference.getKey();

			if (key.equals(KEY_PREF_DEFAULT_SCREEN)) {
				preference.setSummary(ApplicationConstants.titles[Integer.parseInt(sharedPreferences.getString(key, "1"))]);
			}
		}

        private void finishActivityWithAction(String action) {
            Intent intent = new Intent();
            intent.putExtra(ApiConstants.ACTION, action);
            getActivity().setResult(RESULT_OK, intent);
            getActivity().finish();
        }
	}
}
