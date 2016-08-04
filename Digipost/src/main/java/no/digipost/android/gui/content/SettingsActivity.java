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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.google.android.gms.analytics.GoogleAnalytics;
import no.digipost.android.DigipostApplication;
import no.digipost.android.R;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.gui.MainContentActivity;

public class SettingsActivity extends AppCompatActivity {
    public static final String KEY_PREF_GENERAL_SETTINGS = "pref_generalSettings";
    public static final String KEY_PREF_DEFAULT_SCREEN = "pref_defaultScreen";
    public static final String KEY_PREF_CONFIRM_DELETE = "pref_confirmDelete";
    public static final String KEY_PREF_CONFIRM_MOVE = "pref_confirmMove";
    public static final String KEY_PREF_DOCUMENTS_SETTINGS = "pref_documentsSettings";
    public static final String KEY_PREF_SHOW_BANK_ID_DOCUMENTS = "pref_showBankIDDocuments";
    public static final String KEY_PREF_PERSONAL_SETTINGS = "pref_personalSettings";
    public static final String KEY_PREF_NOTIFICATION_SETTINGS = "pref_notificationSettings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((DigipostApplication) getApplication()).getTracker(DigipostApplication.TrackerName.APP_TRACKER);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.preferences);
        setSupportActionBar(toolbar);
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

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            /*Preference personalSettings = findPreference(KEY_PREF_PERSONAL_SETTINGS);
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
            });*/

            //setSummary(getPreferenceManager().getSharedPreferences(), findPreference(KEY_PREF_DEFAULT_SCREEN));
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_settings, container, false);
            return view;
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            setSummary(sharedPreferences, findPreference(key));
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (resultCode == RESULT_OK) {
                if (requestCode == MainContentActivity.INTENT_REQUESTCODE) {
                    String action = data.getStringExtra(ApiConstants.FRAGMENT_ACTIVITY_RESULT_ACTION);

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
                //preference.setSummary(MainContentActivity.drawerListitems[Integer.parseInt(sharedPreferences.getString(key, Integer.toString(ApplicationConstants.MAILBOX)))]);
            }
        }

        private void finishActivityWithAction(String action) {
            Intent intent = new Intent();
            intent.putExtra(ApiConstants.FRAGMENT_ACTIVITY_RESULT_ACTION, action);
            getActivity().setResult(RESULT_OK, intent);
            getActivity().finish();
        }
    }
}
