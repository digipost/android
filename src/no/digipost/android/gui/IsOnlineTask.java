package no.digipost.android.gui;

import java.net.URL;
import java.net.URLConnection;

import android.os.AsyncTask;

public class IsOnlineTask extends AsyncTask<Void, Void, Boolean> {

	@Override
	protected Boolean doInBackground(final Void... params) {
		try {
			URL myUrl = new URL("https://www.digipost.no");
			URLConnection connection = myUrl.openConnection();
			connection.setConnectTimeout(3000);
			connection.connect();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
