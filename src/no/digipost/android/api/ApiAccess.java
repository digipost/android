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
package no.digipost.android.api;

import java.net.URI;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.os.AsyncTask;

public class ApiAccess {
	GetJSONfromUriTASK task;

	public ApiAccess() {
	}

	public JSONObject getJSONfromURI(final String access_token, final String uri) {
		task = new GetJSONfromUriTASK();
		JSONObject uri_json_response = null;
		try {
			uri_json_response = task.execute(access_token, uri, "GET").get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		task.cancel(true);
		return uri_json_response;
	}

	private class GetJSONfromUriTASK extends AsyncTask<String, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(final String... params) {
			try {
				String access_token = params[0];
				JSONObject json = null;
				String uri = params[1];
				HttpClient client = new DefaultHttpClient();
				if (params[2].equals("GET")) {
					HttpGet request = new HttpGet();
					request.setURI(new URI(uri));
					request.setHeader(ApiConstants.ACCEPT, ApiConstants.APPLICATION_VND_DIGIPOST_V2_JSON);
					request.setHeader(ApiConstants.AUTHORIZATION, ApiConstants.BEARER + access_token);
					HttpResponse response = client.execute(request);
					json = JSONConverter.getJSONObjectFromInputStream(response.getEntity().getContent());
				} else if (params[2].equals("POST")) {

				}
				return json;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}