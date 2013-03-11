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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import no.digipost.android.R;
import no.digipost.android.authentication.OAuth2;
import no.digipost.android.authentication.Secret;
import no.digipost.android.gui.NetworkConnection;
import no.digipost.android.model.Account;
import no.digipost.android.model.Documents;
import no.digipost.android.model.Letter;
import no.digipost.android.model.Receipts;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

public class ApiAccess {
	static int filesize = 0;
	private final Context context;
	private final NetworkConnection networkConnection;

	public ApiAccess(final Context context) {
		this.context = context;
		networkConnection = new NetworkConnection(context);
	}

	public Account getPrimaryAccount(final String access_token) throws DigipostApiException, DigipostClientException {
		return (Account) JSONConverter.processJackson(Account.class, getApiJsonString(access_token, ApiConstants.URL_API));
	}

	public Documents getDocuments(final String access_token, final String uri) throws DigipostApiException, DigipostClientException {
		return (Documents) JSONConverter.processJackson(Documents.class, getApiJsonString(access_token, uri));
	}

	public Receipts getReceipts(final String access_token, final String uri) throws DigipostApiException, DigipostClientException {
		return (Receipts) JSONConverter.processJackson(Receipts.class, getApiJsonString(access_token, uri));
	}

	private ClientResponse executeGetRequest(final String uri, final String header_accept, final String header_authorization)
			throws DigipostClientException {
		Client client = Client.create();
		try {
			ClientResponse cr = client
					.resource(uri)
					.header(ApiConstants.ACCEPT, header_accept)
					.header(ApiConstants.AUTHORIZATION, ApiConstants.BEARER + header_authorization)
					.get(ClientResponse.class);

			return cr;
		} catch (Exception e) {
			throw new DigipostClientException(context.getString(R.string.error_your_network));
		}
	}

	public String getApiJsonString(final String access_token, final String uri) throws DigipostApiException, DigipostClientException {
		ClientResponse cr = executeGetRequest(uri, ApiConstants.APPLICATION_VND_DIGIPOST_V2_JSON, access_token);

		try {
			networkConnection.checkHttpStatusCode(cr.getStatus());
		} catch (IllegalStateException e) {
			OAuth2.updateRefreshTokenSuccess(context);
			return getApiJsonString(Secret.ACCESS_TOKEN, uri);
		}

		return JSONConverter.getJsonStringFromInputStream(cr.getEntityInputStream());
	}

	public Letter getMovedDocument(final String access_token, final String uri, final StringEntity json) throws DigipostClientException,
			DigipostApiException {
		return (Letter) JSONConverter.processJackson(Letter.class, moveLetter(access_token, uri, json));
	}

	public String moveLetter(final String access_token, final String uri, final StringEntity json) throws DigipostClientException,
			DigipostApiException {

		HttpClient httpClient = new DefaultHttpClient();
		HttpPost post = new HttpPost();
		try {
			post.setURI(new URI(uri));
		} catch (URISyntaxException e1) {
			// Ignore
		}
		post.addHeader(ApiConstants.CONTENT_TYPE, ApiConstants.APPLICATION_VND_DIGIPOST_V2_JSON);
		post.addHeader(ApiConstants.ACCEPT, ApiConstants.APPLICATION_VND_DIGIPOST_V2_JSON);
		post.addHeader(ApiConstants.AUTHORIZATION, ApiConstants.BEARER + access_token);
		post.setEntity(json);

		HttpResponse response;
		try {
			response = httpClient.execute(post);
		} catch (Exception e) {
			throw new DigipostClientException(context.getString(R.string.error_your_network));
		}

		try {
			networkConnection.checkHttpStatusCode(response.getStatusLine().getStatusCode());
		} catch (IllegalStateException e) {
			OAuth2.updateRefreshTokenSuccess(context);
			return moveLetter(Secret.ACCESS_TOKEN, uri, json);
		}

		InputStream is = null;
		try {
			is = response.getEntity().getContent();
		} catch (IllegalStateException e) {
			// Ignore
			e.printStackTrace();
		} catch (IOException e) {
			// Ignore
		}

		return JSONConverter.getJsonStringFromInputStream(is);

	}

	public byte[] getDocumentContent(final String access_token, final String uri) throws DigipostApiException, DigipostClientException {
		ClientResponse cr = executeGetRequest(uri, ApiConstants.CONTENT_OCTET_STREAM, access_token);

		try {
			networkConnection.checkHttpStatusCode(cr.getStatus());
		} catch (IllegalStateException e) {
			OAuth2.updateRefreshTokenSuccess(context);
			return getDocumentContent(Secret.ACCESS_TOKEN, uri);
		}

		return JSONConverter.inputStreamtoByteArray(filesize, cr.getEntityInputStream());
	}

	public String getDocumentHTML(final String access_token, final String uri) throws DigipostApiException, DigipostClientException {
		ClientResponse cr = executeGetRequest(uri, ApiConstants.CONTENT_OCTET_STREAM, access_token);

		try {
			networkConnection.checkHttpStatusCode(cr.getStatus());
		} catch (IllegalStateException e) {
			OAuth2.updateRefreshTokenSuccess(context);
			return getDocumentHTML(Secret.ACCESS_TOKEN, uri);
		}

		return JSONConverter.getJsonStringFromInputStream(cr.getEntityInputStream());
	}

	public String getReceiptHTML(final String access_token, final String uri) throws DigipostApiException, DigipostClientException {
		ClientResponse cr = executeGetRequest(uri, ApiConstants.TEXT_HTML, access_token);

		try {
			networkConnection.checkHttpStatusCode(cr.getStatus());
		} catch (IllegalStateException e) {
			OAuth2.updateRefreshTokenSuccess(context);
			return getDocumentHTML(Secret.ACCESS_TOKEN, uri);
		}

		return JSONConverter.getJsonStringFromInputStream(cr.getEntityInputStream());
	}
}