package no.digipost.android.pdf;

import java.util.concurrent.RejectedExecutionException;

import android.os.AsyncTask;

public abstract class SafeAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
	public void safeExecute(final Params... params) {
		try {
			execute(params);
		} catch (RejectedExecutionException e) {
			// Failed to start in the background, so do it in the foreground
			onPreExecute();
			if (isCancelled()) {
				onCancelled();
			} else {
				onPostExecute(doInBackground(params));
			}
		}
	}
}
