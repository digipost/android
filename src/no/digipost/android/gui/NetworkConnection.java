package no.digipost.android.gui;

import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;

import no.digipost.android.R;
import no.digipost.android.api.DigipostApiException;
import no.digipost.android.api.DigipostInvalidTokenException;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

public class NetworkConnection {
	public static final int HTTP_STATUS_SUCCESS = 200;
	public static final int HTTP_STATUS_UNAUTHORIZED = 401;
	public static final int HTTP_STATUS_BAD_REQUEST = 400;

	private final Context context;

	public NetworkConnection(final Context context) {
		this.context = context;
	}

	public boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}

	public void checkHttpStatusCode(final int statusCode) throws DigipostApiException, DigipostInvalidTokenException {
		if (statusCode == HTTP_STATUS_SUCCESS) {
			return;
		} else if (statusCode == HTTP_STATUS_UNAUTHORIZED) {
			throw new DigipostInvalidTokenException();
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
