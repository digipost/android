package no.digipost.android.gui;

import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;

import no.digipost.android.R;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

public class NetworkConnection {
	private final Context context;

	public NetworkConnection(final Context context) {
		this.context = context;
	}

	public boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}

	public void checkHttpStatusCode(final int statusCode) throws NetworkErrorException, IllegalStateException {
		if (statusCode == 200) {
			return;
		} else if (statusCode == 401) {
			throw new IllegalStateException();
		} else {
			throw new NetworkErrorException(context.getString(R.string.error_digipost_api));
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
