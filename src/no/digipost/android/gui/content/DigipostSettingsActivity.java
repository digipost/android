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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.analytics.tracking.android.EasyTracker;

import no.digipost.android.R;
import no.digipost.android.api.ContentOperations;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.model.Account;
import no.digipost.android.model.Settings;
import no.digipost.android.utilities.DialogUtitities;

public abstract class DigipostSettingsActivity extends Activity {

    protected Account userAccount;
	protected Settings accountSettings;

	protected Button settingsButton;
	protected ProgressDialog settingsProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		executeGetAccountTask();
	}

    @Override
    protected void onPause() {
        super.onDestroy();

        hideSettingsProgressDialog();
    }

    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this);
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
		settingsProgressDialog.show();
	}

	private void hideSettingsProgressDialog() {
        if (settingsProgressDialog != null) {
            settingsProgressDialog.dismiss();
            settingsProgressDialog = null;
        }
	}

	private void showInvalidInputDialog(String message) {
		AlertDialog.Builder builder = DialogUtitities.getAlertDialogBuilderWithMessageAndTitle(this, message, "Ugyldig format");
		builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.dismiss();
			}
		});
		builder.create().show();
	}

	protected abstract void setSettingsEnabled(boolean state);

	protected void setButtonState(boolean state, String message) {
		if (state) {
			settingsButton.setText(message);
			settingsButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					executeUpdateSettingsTask();
				}
			});
		} else {
			settingsButton.setText("Prøv igjen");
			settingsButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					executeGetAccountTask();
				}
			});
		}
	}

	protected abstract void setAccountInfo(Account account);

	private void executeGetAccountTask() {
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

                if (invalidToken) {
                    finishActivityWithAction(ApiConstants.LOGOUT);
                }

				setSettingsEnabled(false);
			} else {
                userAccount = result;
				executeGetSettingsTask();
			}
		}
	}

	protected abstract void updateUI(Settings settings);

	private void executeGetSettingsTask() {
		GetSettingsTask getSettingsTask = new GetSettingsTask();
		getSettingsTask.execute();
	}

	private class GetSettingsTask extends AsyncTask<Void, Void, Settings> {
		private String errorMessage;
        private boolean invalidToken;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
		protected Settings doInBackground(Void... voids) {
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
		protected void onPostExecute(Settings settings) {
			super.onPostExecute(settings);
			hideSettingsProgressDialog();

			if (settings == null) {
				DialogUtitities.showToast(DigipostSettingsActivity.this, errorMessage);
                setSettingsEnabled(false);

                if (invalidToken) {
                    finishActivityWithAction(ApiConstants.LOGOUT);
                }
			} else {
				accountSettings = settings;
				updateUI(accountSettings);
                setAccountInfo(userAccount);
				setSettingsEnabled(true);
			}
		}
	}

	protected abstract void setSelectedAccountSettings() throws Exception;

	private void executeUpdateSettingsTask() {
		try {
            setSelectedAccountSettings();
			UpdateSettingsTask updateSettingsTask = new UpdateSettingsTask(accountSettings);
			updateSettingsTask.execute();
		} catch (Exception e) {
			showInvalidInputDialog(e.getMessage());
		}
	}

	protected class UpdateSettingsTask extends AsyncTask<Void, Void, String> {
		private Settings settings;
        private boolean invalidToken;

		public UpdateSettingsTask(Settings settings) {
			this.settings = settings;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showSettingsProgressDialog("Oppdaterer dine innstillinger...");
		}

		@Override
		protected String doInBackground(Void... voids) {
			try {
				ContentOperations.updateAccountSettings(DigipostSettingsActivity.this, settings);
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

				if (invalidToken) {
                    finishActivityWithAction(ApiConstants.LOGOUT);
                }
			} else {
				DialogUtitities.showToast(DigipostSettingsActivity.this, "Dine varslingsinnstillinger ble oppdatert.");
			}
		}
	}

    private void finishActivityWithAction(String action) {
        Intent intent = new Intent();
        intent.putExtra(ApiConstants.FRAGMENT_ACTIVITY_RESULT_ACTION, action);
        setResult(RESULT_OK, intent);
        finish();
    }
}
