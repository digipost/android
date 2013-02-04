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
package no.digipost.android.api;

import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class ApiAccess {
	ApiTask task;
	StringEntity se;

	public ApiAccess() {
	}

	public JSONObject getJSONfromURI(final String access_token, final String uri) {
		task = new ApiTask();
		JSONObject json = null;
		InputStream api_respons = null;
		try {
			api_respons = task.execute(access_token, uri, ApiConstants.GET).get();
			json = JSONConverter.getJSONObjectFromInputStream(api_respons);

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		task.cancel(true);
		return json;
	}

	public boolean moveDocument(final String access_token, final StringEntity document, final String update_uri) {
		task = new ApiTask();
		se = document;
		JSONObject json = new JSONObject();
		InputStream api_respons = null;
		try {
			// json = task.execute(access_token, update_uri,
			// ApiConstants.POST).get();
			api_respons = task.execute(access_token, update_uri, ApiConstants.POST).get();
			json = JSONConverter.getJSONObjectFromInputStream(api_respons);
		} catch (Exception e) {
		}
		try {
			if (json == null) {
				return false;
			}
			if (json.getString(ApiConstants.LOCATION).equals(ApiConstants.LOCATION_ARCHIVE)) {
				System.out.println("tilbake fra task, location endret");
				return true;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private class ApiTask extends AsyncTask<String, Void, InputStream> {

		@Override
		protected InputStream doInBackground(final String... params) {
			try {
				String access_token = params[0];
				JSONObject json = null;
				String uri = params[1];
				// HttpClient client = new DefaultHttpClient();
				ClientResponse response = null;
				WebResource wr;
				Client client;
				if (params[2].equals(ApiConstants.GET)) {
					/*
					 * HttpGet request = new HttpGet();
					 * request.setURI(new URI(uri));
					 * request.setHeader(ApiConstants.ACCEPT,ApiConstants.APPLICATION_VND_DIGIPOST_V2_JSON);
					 * request.setHeader(ApiConstants.AUTHORIZATION,ApiConstants.BEARER + access_token);
					 * HttpResponse response = client.execute(request);
					 * json = JSONConverter.getJSONObjectFromInputStream
					 * (response.getEntity().getContent());
					 */

					// Jersey-test

					client = Client.create();
					wr = client.resource(uri);

					response = wr
							.header(ApiConstants.ACCEPT, ApiConstants.APPLICATION_VND_DIGIPOST_V2_JSON)
							.header(ApiConstants.AUTHORIZATION, ApiConstants.BEARER + access_token)
							.get(ClientResponse.class);

					/*
					 * response = wr
					 * .accept(ApiConstants.APPLICATION_VND_DIGIPOST_V2_JSON)
					 * .header(ApiConstants.AUTHORIZATION, ApiConstants.BEARER +
					 * access_token) .get(ClientResponse.class);
					 */

					if(response.getStatus() == 200) {
						System.out.println("OK i get!!!!!!");
					}

					return response.getEntityInputStream();

				} else if (params[2].equals(ApiConstants.POST)) {

					/*
					 * HttpPost post = new HttpPost(); post.setURI(new
					 * URI(uri)); System.out.println(uri);
					 * post.setHeader(ApiConstants.CONTENT_TYPE,
					 * ApiConstants.APPLICATION_VND_DIGIPOST_V2_JSON);
					 * post.setHeader(ApiConstants.ACCEPT,
					 * ApiConstants.APPLICATION_VND_DIGIPOST_V2_JSON);
					 * post.setHeader(ApiConstants.AUTHORIZATION,
					 * ApiConstants.BEARER + access_token); post.setEntity(se);
					 * HttpResponse response = client.execute(post); json =
					 * JSONConverter
					 * .getJSONObjectFromInputStream(response.getEntity
					 * ().getContent());
					 */

					// jersey

					client = Client.create();
					wr = client.resource(uri);

					/*
					 * response = wr .header(ApiConstants.POST,
					 * ApiConstants.POST_API_ACCESSTOKEN_HTTP)
					 * .header(ApiConstants.CONTENT_TYPE,
					 * ApiConstants.APPLICATION_VND_DIGIPOST_V2_JSON)
					 * .header(ApiConstants.ACCEPT,
					 * ApiConstants.APPLICATION_VND_DIGIPOST_V2_JSON)
					 * .header(ApiConstants.AUTHORIZATION, ApiConstants.BEARER +
					 * access_token) .post(ClientResponse.class);
					 */

					response = wr
							.type(ApiConstants.APPLICATION_VND_DIGIPOST_V2_JSON)
							.accept(ApiConstants.APPLICATION_VND_DIGIPOST_V2_JSON)
							.header(ApiConstants.AUTHORIZATION, ApiConstants.BEARER + access_token)
							.post(ClientResponse.class, se);

					// return response.getEntityInputStream();

				}
				// return json;
				return response.getEntityInputStream();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}