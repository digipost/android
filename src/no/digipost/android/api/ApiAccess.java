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

import android.content.Context;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import no.digipost.android.R;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.api.exception.DigipostInvalidTokenException;
import no.digipost.android.authentication.OAuth2;
import no.digipost.android.authentication.Secret;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.model.Account;
import no.digipost.android.model.Documents;
import no.digipost.android.model.Letter;
import no.digipost.android.model.Receipts;
import no.digipost.android.model.Settings;
import no.digipost.android.utilities.JSONUtilities;
import no.digipost.android.utilities.NetworkUtilities;

import static com.sun.jersey.api.client.ClientResponse.Status.TEMPORARY_REDIRECT;

public class ApiAccess {
	private static Client jerseyClient = Client.create();

	public static final int POST_ACTION_MOVE = 0;
	public static final int POST_ACTION_SEND_OPENING_RECEIPT = 1;

    private static Client getClient() {
        if (jerseyClient == null) {
            jerseyClient = Client.create();
        }

        return jerseyClient;
    }

	public static Account getAccount(Context context) throws DigipostApiException, DigipostClientException, DigipostAuthenticationException {
		return (Account) JSONUtilities.processJackson(Account.class, getApiJsonString(context, ApiConstants.URL_API));
	}

	public static Documents getDocuments(Context context, final String uri) throws DigipostApiException, DigipostClientException, DigipostAuthenticationException {
		return (Documents) JSONUtilities.processJackson(Documents.class, getApiJsonString(context, uri));
	}

	public static Receipts getReceipts(Context context, final String uri) throws DigipostApiException, DigipostClientException, DigipostAuthenticationException {
		return (Receipts) JSONUtilities.processJackson(Receipts.class, getApiJsonString(context, uri));
	}

	public static Letter getLetterSelf(Context context, final String uri) throws DigipostApiException, DigipostClientException, DigipostAuthenticationException {
		return (Letter) JSONUtilities.processJackson(Letter.class, getApiJsonString(context, uri));
	}

    public static Settings getSettings(Context context, final String uri) throws DigipostClientException, DigipostAuthenticationException, DigipostApiException {
        return (Settings) JSONUtilities.processJackson(Settings.class, getApiJsonString(context, uri));
    }

	private static ClientResponse executeGetRequest(Context context, final String uri, final String header_accept) throws DigipostClientException,
			DigipostApiException, DigipostAuthenticationException {
		if (Secret.ACCESS_TOKEN.equals("")) {
			OAuth2.updateAccessToken(context);
		}

		try {
			ClientResponse cr = getClient()
					.resource(uri)
					.header(ApiConstants.ACCEPT, header_accept)
					.header(ApiConstants.AUTHORIZATION, ApiConstants.BEARER + Secret.ACCESS_TOKEN)
					.get(ClientResponse.class);

			if (cr.getStatus() == TEMPORARY_REDIRECT.getStatusCode()) {
				return executeGetRequest(context, cr.getHeaders().getFirst("Location"), header_accept);
			}

			return cr;
		} catch (Exception e) {
			throw new DigipostClientException(context.getString(R.string.error_your_network));
		}

	}

	public static String getApiJsonString(Context context, final String uri) throws DigipostApiException, DigipostClientException, DigipostAuthenticationException {
		ClientResponse cr = executeGetRequest(context, uri, ApiConstants.APPLICATION_VND_DIGIPOST_V2_JSON);

		try {
			NetworkUtilities.checkHttpStatusCode(context, cr.getStatus());
		} catch (DigipostInvalidTokenException e) {
			OAuth2.updateAccessToken(context);
			return getApiJsonString(context, uri);
		}

		return JSONUtilities.getJsonStringFromInputStream(cr.getEntityInputStream());
	}

	public static String postSendOpeningReceipt(Context context, final String uri) throws DigipostClientException, DigipostApiException,
			DigipostAuthenticationException {
		return executePostRequest(context, POST_ACTION_SEND_OPENING_RECEIPT, uri, null);
	}

	public static Letter getMovedDocument(Context context, final String uri, final StringEntity json) throws DigipostClientException, DigipostApiException,
			DigipostAuthenticationException {
		return (Letter) JSONUtilities.processJackson(Letter.class, executePostRequest(context, POST_ACTION_MOVE, uri, json));
	}

	private static String executePostRequest(Context context, int action, final String uri, final StringEntity json) throws DigipostClientException,
			DigipostApiException, DigipostAuthenticationException {
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost post = new HttpPost();

		try {
			post.setURI(new URI(uri));
		} catch (URISyntaxException e1) {
			// Ignore
		}

		post.addHeader(ApiConstants.CONTENT_TYPE, ApiConstants.APPLICATION_VND_DIGIPOST_V2_JSON);
		post.addHeader(ApiConstants.ACCEPT, ApiConstants.APPLICATION_VND_DIGIPOST_V2_JSON);
		post.addHeader(ApiConstants.AUTHORIZATION, ApiConstants.BEARER + Secret.ACCESS_TOKEN);

		if (action == POST_ACTION_MOVE) {
			post.setEntity(json);
		}

		HttpResponse response;
		try {
			response = httpClient.execute(post);
		} catch (Exception e) {
			throw new DigipostClientException(context.getString(R.string.error_your_network));
		}

		try {
            System.out.println("executePost STATUS: " + response.getStatusLine().getStatusCode());
			NetworkUtilities.checkHttpStatusCode(context, response.getStatusLine().getStatusCode());
		} catch (DigipostInvalidTokenException e) {
			OAuth2.updateAccessToken(context);

			return executePostRequest(context, action, uri, json);
		}

		InputStream is = null;
		try {
			is = response.getEntity().getContent();
		} catch (IllegalStateException e) {
			// Ignore
		} catch (IOException e) {
			// Ignore
		}

		return JSONUtilities.getJsonStringFromInputStream(is);
	}

	public static void delete(Context context, final String uri) throws DigipostClientException, DigipostApiException, DigipostAuthenticationException {
		Client client = Client.create();
		ClientResponse cr = null;

		try {
			cr = client
					.resource(uri)
					.header(ApiConstants.ACCEPT, ApiConstants.APPLICATION_VND_DIGIPOST_V2_JSON)
					.header(ApiConstants.AUTHORIZATION, ApiConstants.BEARER + Secret.ACCESS_TOKEN)
					.delete(ClientResponse.class);
		} catch (Exception e) {
			throw new DigipostClientException(context.getString(R.string.error_your_network));
		}

		try {
			NetworkUtilities.checkHttpStatusCode(context, cr.getStatus());
		} catch (DigipostInvalidTokenException e) {
			OAuth2.updateAccessToken(context);
			delete(context, uri);
		}
	}

	public static byte[] getDocumentContent(Context context, final String uri, final int filesize) throws DigipostApiException, DigipostClientException,
			DigipostAuthenticationException {
		ClientResponse cr = executeGetRequest(context, uri, ApiConstants.CONTENT_OCTET_STREAM);

		try {
			NetworkUtilities.checkHttpStatusCode(context, cr.getStatus());
		} catch (DigipostInvalidTokenException e) {
			OAuth2.updateAccessToken(context);
			return getDocumentContent(context, uri, filesize);
		}

		return JSONUtilities.inputStreamtoByteArray(context, filesize, cr.getEntityInputStream());
	}

	public static String getReceiptHTML(Context context, final String uri) throws DigipostApiException, DigipostClientException, DigipostAuthenticationException {
		ClientResponse cr = executeGetRequest(context, uri, ApiConstants.TEXT_HTML);

		try {
			NetworkUtilities.checkHttpStatusCode(context, cr.getStatus());
		} catch (DigipostInvalidTokenException e) {
			OAuth2.updateAccessToken(context);
			return getReceiptHTML(context, uri);
		}

		return JSONUtilities.getJsonStringFromInputStream(cr.getEntityInputStream());
	}

	public static void uploadFile(Context context, String uri, File file) throws DigipostClientException, DigipostAuthenticationException, DigipostApiException {
		try {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(uri);
                httpPost.addHeader(ApiConstants.AUTHORIZATION, ApiConstants.BEARER + Secret.ACCESS_TOKEN);

                FileBody filebody = new FileBody(file, ApiConstants.CONTENT_OCTET_STREAM);

                MultipartEntity multipartEntity = new MultipartEntity();
                multipartEntity.addPart("subject", new StringBody(FilenameUtils.removeExtension(file.getName())));
                multipartEntity.addPart("file", filebody);
                multipartEntity.addPart("token", new StringBody(Secret.ACCESS_TOKEN));
                httpPost.setEntity(multipartEntity);

                HttpResponse httpResponse = httpClient.execute(httpPost);

                httpClient.getConnectionManager().shutdown();

                NetworkUtilities.checkHttpStatusCode(context, httpResponse.getStatusLine().getStatusCode());
            } catch (DigipostInvalidTokenException e) {
                OAuth2.updateAccessToken(context);
                uploadFile(context, uri, file);
            }
		} catch (Exception e) {
			throw new DigipostClientException(context.getString(R.string.error_your_network));
		}
	}
}
