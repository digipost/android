package no.digipost.android.gui.content;

import no.digipost.android.R;
import no.digipost.android.api.ContentOperations;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.model.Account;
import no.digipost.android.model.Settings;
import no.digipost.android.utilities.DialogUtitities;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public abstract class DigipostSettingsActivity extends Activity {

    protected Account userAccount;
	protected Settings accountSettings;

	protected Button settingsButton;
	private ProgressDialog settingsProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		executeGetAccountTask();
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
		settingsProgressDialog.dismiss();
		settingsProgressDialog = null;
	}

	private void showInvalidInputDialog(String message) {
		AlertDialog.Builder builder = DialogUtitities.getAlertDialogBuilderWithMessage(this, message);
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

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showSettingsProgressDialog("Laster dine innstillinger...");
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
				errorMessage = e.getMessage();
				return null;
			}
		}

		@Override
		protected void onPostExecute(Account result) {
			super.onPostExecute(result);

			if (result == null) {
				DialogUtitities.showToast(DigipostSettingsActivity.this, errorMessage);
				// ToDo invalid token
				setSettingsEnabled(false);
				hideSettingsProgressDialog();
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

		@Override
		protected Settings doInBackground(Void... voids) {
			try {
				return ContentOperations.getSettings(DigipostSettingsActivity.this);
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
				DialogUtitities.showToast(DigipostSettingsActivity.this, errorMessage);

				// ToDo invalid token
				setSettingsEnabled(false);
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

				// ToDo invalid token
			} else {
				DialogUtitities.showToast(DigipostSettingsActivity.this, "Dine varslingsinnstillinger ble oppdatert.");
			}
		}
	}
}
