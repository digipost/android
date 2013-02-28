/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package no.digipost.android.pdf;

import java.util.concurrent.RejectedExecutionException;

import android.os.AsyncTask;

public abstract class SafeAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
	public void safeExecute(final Params... params) {
		try {
			execute(params);
		} catch (RejectedExecutionException e) {
			onPreExecute();
			if (isCancelled()) {
				onCancelled();
			} else {
				onPostExecute(doInBackground(params));
			}
		}
	}
}
