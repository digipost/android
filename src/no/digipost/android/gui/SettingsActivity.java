package no.digipost.android.gui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import no.digipost.android.R;
import no.digipost.android.constants.ApplicationConstants;

public class SettingsActivity extends Activity {
    public static final String KEY_PREF_GENERAL_SETTINGS = "pref_generalSettings";
    public static final String KEY_PREF_DEFAULT_SCREEN = "pref_defaultScreen";
    public static final String KEY_PREF_DOCUMENTS_SETTINGS = "pref_documentsSettings";
    public static final String KEY_PREF_SHOW_BANK_ID_DOCUMENTS = "pref_showBankIDDocuments";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().setTitle(R.string.preferences);
        getActionBar().setHomeButtonEnabled(true);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
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

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener  {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.preferences);

            setSummary(getPreferenceManager().getSharedPreferences(), findPreference(KEY_PREF_DEFAULT_SCREEN));
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            setSummary(sharedPreferences, findPreference(key));

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
    }
}
