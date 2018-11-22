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
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import com.google.android.gms.analytics.GoogleAnalytics;
import no.digipost.android.DigipostApplication;
import no.digipost.android.R;
import no.digipost.android.analytics.GAEventController;
import no.digipost.android.api.ContentOperations;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.model.Account;
import no.digipost.android.model.MailboxSettings;
import no.digipost.android.utilities.DialogUtitities;

public abstract class DigipostSettingsActivity extends AppCompatActivity {

    protected Account userAccount;
    protected MailboxSettings accountMailboxSettings;

    protected ProgressDialog settingsProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((DigipostApplication) getApplication()).getTracker(DigipostApplication.TrackerName.APP_TRACKER);
        executeGetAccountTask();
    }

    @Override
    protected void onPause() {
        super.onPause();

        hideSettingsProgressDialog();
    }

    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);    }

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
        settingsProgressDialog.show();
    }

    private void hideSettingsProgressDialog() {
        if (settingsProgressDialog != null) {
            settingsProgressDialog.dismiss();
            settingsProgressDialog = null;
        }
    }

    private void showInvalidInputDialog(String message) {
        AlertDialog.Builder builder = DialogUtitities.getAlertDialogBuilderWithMessageAndTitle(this, message, getString(R.string.error_invalid_format));
        builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    protected abstract void setSettingsEnabled(boolean state);

    protected abstract void setAccountInfo(Account account);

    protected void executeGetAccountTask() {
        GetAccountTask getAccountTask = new GetAccountTask();
        getAccountTask.execute();
    }

    private class GetAccountTask extends AsyncTask<Void, Void, Account> {
        private String errorMessage;
        private boolean invalidToken;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showSettingsProgressDialog(getString(R.string.pref_personal_settings_loading));
        }

        @Override
        protected Account doInBackground(Void... voids) {
            try {
                return ContentOperations.getAccount(DigipostSettingsActivity.this);
            } catch (DigipostApiException e) {
                errorMessage = e.getMessage();
                return null;
            } catch (DigipostClientException e) {
                errorMessage = e.getMessage();
                return null;
            } catch (DigipostAuthenticationException e) {
                invalidToken = true;
                errorMessage = e.getMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Account result) {
            super.onPostExecute(result);

            if (result == null) {
                hideSettingsProgressDialog();
                DialogUtitities.showToast(DigipostSettingsActivity.this, errorMessage);

                setSettingsEnabled(false);
            } else {
                userAccount = result;
                executeGetSettingsTask();
            }
        }
    }

    protected abstract void updateUI(MailboxSettings mailboxSettings);

    private void executeGetSettingsTask() {
        GetSettingsTask getSettingsTask = new GetSettingsTask();
        getSettingsTask.execute();
    }

    private class GetSettingsTask extends AsyncTask<Void, Void, MailboxSettings> {
        private String errorMessage;
        private boolean invalidToken;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected MailboxSettings doInBackground(Void... voids) {
            try {
                return ContentOperations.getSettings(DigipostSettingsActivity.this);
            } catch (DigipostClientException e) {
                errorMessage = e.getMessage();
                return null;
            } catch (DigipostAuthenticationException e) {
                invalidToken = true;
                errorMessage = e.getMessage();
                return null;
            } catch (DigipostApiException e) {
                errorMessage = e.getMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute(MailboxSettings mailboxSettings) {
            super.onPostExecute(mailboxSettings);
            hideSettingsProgressDialog();
            if (mailboxSettings == null) {
                DialogUtitities.showToast(DigipostSettingsActivity.this, errorMessage);
                setSettingsEnabled(false);
            } else {
                accountMailboxSettings = mailboxSettings;
                setAccountInfo(userAccount);
                updateUI(accountMailboxSettings);
                setSettingsEnabled(true);
            }
        }
    }

    protected abstract void setSelectedAccountSettings() throws Exception;

    protected void executeUpdateSettingsTask() {
        try {
            setSelectedAccountSettings();
            new UpdateSettingsTask(accountMailboxSettings).execute();
        } catch (Exception e) {
            showInvalidInputDialog(e.getMessage());
        }
    }

    protected class UpdateSettingsTask extends AsyncTask<Void, Void, String> {
        private MailboxSettings mailboxSettings;
        private boolean invalidToken;

        public UpdateSettingsTask(MailboxSettings mailboxSettings) {
            this.mailboxSettings = mailboxSettings;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showSettingsProgressDialog(getResources().getString(R.string.pref_screen_notification_settings_updating));
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                ContentOperations.updateAccountSettings(DigipostSettingsActivity.this, mailboxSettings);
                return null;
            } catch (DigipostAuthenticationException e) {
                invalidToken = true;
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
                DialogUtitities.showToast(DigipostSettingsActivity.this, result);
                GAEventController.sendKontaktopplysningerOppdatert(DigipostSettingsActivity.this, "lagring", "feilet");
            } else {
                DialogUtitities.showToast(DigipostSettingsActivity.this, getResources().getString(R.string.pref_personal_settings_saved_successfully));
                GAEventController.sendKontaktopplysningerOppdatert(DigipostSettingsActivity.this, "lagring", "vellykket");
            }
        }
    }

}
