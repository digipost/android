package no.digipost.android.gui;

import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;

import no.digipost.android.R;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostInvalidTokenException;
import no.digipost.android.utilities.SharedPreferencesUtilities;

import android.content.Context;
import android.os.AsyncTask;

import static javax.ws.rs.core.Response.Status.TEMPORARY_REDIRECT;

public class NetworkConnection {
	public static final int HTTP_STATUS_SUCCESS = 200;
	public static final int HTTP_STATUS_UNAUTHORIZED = 401;
	public static final int HTTP_STATUS_BAD_REQUEST = 400;

	private final Context context;

	public NetworkConnection(final Context context) {
		this.context = context;
	}

	public void checkHttpStatusCode(final int statusCode) throws DigipostApiException, DigipostInvalidTokenException,
			DigipostAuthenticationException {
		if (statusCode == HTTP_STATUS_SUCCESS || statusCode == TEMPORARY_REDIRECT.getStatusCode()) {
			return;
		} else if (statusCode == HTTP_STATUS_UNAUTHORIZED) {
            if(SharedPreferencesUtilities.screenlockChoiceYes(context)){
                throw new DigipostInvalidTokenException();
            }else{
                throw new DigipostAuthenticationException(context.getString(R.string.error_invalid_token));
            }
		} else if (statusCode == HTTP_STATUS_BAD_REQUEST) {
			throw new DigipostAuthenticationException(context.getString(R.string.error_invalid_token));
		} else {
			throw new DigipostApiException(context.getString(R.string.error_digipost_api));
		}
	}

	public boolean isOnline() {
		IsOnlineTask task = new IsOnlineTask();
		try {
			return task.execute().get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return false;
	}

	private class IsOnlineTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(final Void... params) {
			try {
				URL url = new URL("https://www.digipost.no");
				URLConnection connection = url.openConnection();
				connection.setConnectTimeout(3000);
				connection.connect();
				return true;
			} catch (Exception e) {
				return false;
			}
		}
	}
}
