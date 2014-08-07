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
import android.util.Log;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import javax.ws.rs.core.HttpHeaders;

import no.digipost.android.DigipostApplication;
import no.digipost.android.R;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.api.exception.DigipostInvalidTokenException;
import no.digipost.android.authentication.OAuth2;
import no.digipost.android.authentication.Secret;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.utilities.JSONUtilities;
import no.digipost.android.utilities.NetworkUtilities;

import static com.sun.jersey.api.client.ClientResponse.Status.TEMPORARY_REDIRECT;

public class ApiAccess {

    public static final int MOVE = 0;
    public static final int SEND_OPENING_RECEIPT = 1;
    public static final int UPDATE_SETTINGS = 2;
    public static final int SEND_TO_BANK = 3;
    public static final int CREATE_FOLDER = 4;
    public static final int EDIT_FOLDER = 5;
    public static final int UPDATE_FOLDERS = 6;
    public static final int POST = 0;
    public static final int PUT = 1;
    private static final String TAG = "ApiAccess";

    private Client jerseyClient;

    public ApiAccess(){}

    private Client getClient() {
        if (jerseyClient == null) {
            jerseyClient = Client.create();
        }
        return jerseyClient;
    }

    private ClientResponse get(Context context, final String uri, final String header_accept) throws DigipostClientException,
            DigipostApiException, DigipostAuthenticationException {

        try {
            if (StringUtils.isBlank(Secret.ACCESS_TOKEN)) {
                OAuth2.updateAccessToken(context);
            }
            ClientResponse cr = getClient()
                    .resource(uri)
                    .header(HttpHeaders.USER_AGENT, DigipostApplication.USER_AGENT)
                    .header(ApiConstants.ACCEPT, header_accept)
                    .header(ApiConstants.AUTHORIZATION, ApiConstants.BEARER + Secret.ACCESS_TOKEN)
                    .get(ClientResponse.class);

            if (cr.getStatus() == TEMPORARY_REDIRECT.getStatusCode()) {
                return get(context, cr.getHeaders().getFirst(HttpHeaders.LOCATION), header_accept);
            }

            return cr;
        } catch(DigipostClientException e){
            Log.e(TAG, context.getString(R.string.error_your_network));
            throw new DigipostClientException(context.getString(R.string.error_your_network));
        } catch (Exception e) {
            Log.e(TAG, context.getString(R.string.error_your_network));
            throw new DigipostClientException(context.getString(R.string.error_your_network));
        }
    }

    public String postput(Context context, final int httpType, int action, final String uri, final StringEntity json) throws DigipostClientException,
            DigipostApiException, DigipostAuthenticationException {

        HttpClient httpClient = new DefaultHttpClient();
        HttpEntityEnclosingRequestBase request = new HttpPost();

        if (httpType == POST) {
            request = new HttpPost();
        } else if (httpType == PUT) {
            request = new HttpPut();
        }

        try {
            request.setURI(new URI(uri));
        } catch (URISyntaxException e) {
            Log.e(TAG, e.getMessage());
            // Ignore
        }

        request.addHeader(ApiConstants.CONTENT_TYPE, ApiConstants.APPLICATION_VND_DIGIPOST_V2_JSON);
        request.addHeader(ApiConstants.ACCEPT, ApiConstants.APPLICATION_VND_DIGIPOST_V2_JSON);
        request.addHeader(ApiConstants.AUTHORIZATION, ApiConstants.BEARER + Secret.ACCESS_TOKEN);

        if (action == SEND_OPENING_RECEIPT || action == SEND_TO_BANK) {
        } else {
            request.setEntity(json);
        }

        HttpResponse response;
        try {
            response = httpClient.execute(request);
        } catch (Exception e) {
            Log.e(TAG, context.getString(R.string.error_your_network));
            throw new DigipostClientException(context.getString(R.string.error_your_network));
        }

        try {
            NetworkUtilities.checkHttpStatusCode(context, response.getStatusLine().getStatusCode());
        } catch (DigipostInvalidTokenException e) {
            OAuth2.updateAccessToken(context);
            Log.e(TAG, context.getString(R.string.error_invalid_token));
            return postput(context, httpType, action, uri, json);
        }

        InputStream is = null;
        try {
            is = response.getEntity().getContent();
        } catch (IllegalStateException e) {
            Log.e(TAG, e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        return JSONUtilities.getJsonStringFromInputStream(is);
    }

    public static String delete(Context context, final String uri) throws DigipostClientException, DigipostApiException, DigipostAuthenticationException {
        Client client = Client.create();
        ClientResponse cr;
        try {
            cr = client
                    .resource(uri)
                    .header(ApiConstants.ACCEPT, ApiConstants.APPLICATION_VND_DIGIPOST_V2_JSON)
                    .header(ApiConstants.AUTHORIZATION, ApiConstants.BEARER + Secret.ACCESS_TOKEN)
                    .delete(ClientResponse.class);

        } catch (Exception e) {
            throw new DigipostClientException(context.getString(R.string.error_your_network));
        }

        if (cr.getStatus() == NetworkUtilities.HTTP_STATUS_BAD_REQUEST) {
            try {
                String output = cr.getEntity(String.class);
                JSONObject jsonObject = new JSONObject(output);
                if (jsonObject.get(ApiConstants.ERROR_CODE).equals(ApiConstants.FOLDER_NOT_EMPTY)) {
                    return null;
                }
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        try {
            NetworkUtilities.checkHttpStatusCode(context, cr.getStatus());
        } catch (DigipostInvalidTokenException e) {
            Log.e(TAG, context.getString(R.string.error_invalid_token));
            OAuth2.updateAccessToken(context);
            delete(context, uri);
        }
        return "" + cr.getStatus();
    }

    public String getReceiptHTML(Context context, final String uri) throws DigipostApiException, DigipostClientException, DigipostAuthenticationException {
        ClientResponse cr = get(context, uri, ApiConstants.TEXT_HTML);

        try {
            NetworkUtilities.checkHttpStatusCode(context, cr.getStatus());
        } catch (DigipostInvalidTokenException e) {
            Log.e(TAG, context.getString(R.string.error_invalid_token));
            OAuth2.updateAccessToken(context);
            return getReceiptHTML(context, uri);
        }

        return JSONUtilities.getJsonStringFromInputStream(cr.getEntityInputStream());
    }

    public static void uploadFile(Context context, String uri, File file) throws DigipostClientException {
        try {
            try {
                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(uri);
                httpPost.addHeader(ApiConstants.AUTHORIZATION, ApiConstants.BEARER + Secret.ACCESS_TOKEN);
                FileBody filebody = new FileBody(file, ApiConstants.CONTENT_OCTET_STREAM);

                MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName(ApiConstants.ENCODING));
                multipartEntity.addPart("subject", new StringBody(FilenameUtils.removeExtension(file.getName()), ApiConstants.MIME, Charset.forName(ApiConstants.ENCODING)));
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
            Log.e(TAG, context.getString(R.string.error_your_network));
            throw new DigipostClientException(context.getString(R.string.error_your_network));
        }
    }

    public String getApiJsonString(Context context, final String uri) throws DigipostApiException, DigipostClientException, DigipostAuthenticationException {
        ClientResponse cr = get(context, uri, ApiConstants.APPLICATION_VND_DIGIPOST_V2_JSON);

        try {
            NetworkUtilities.checkHttpStatusCode(context, cr.getStatus());
        } catch (DigipostInvalidTokenException e) {
            OAuth2.updateAccessToken(context);
            return getApiJsonString(context, uri);
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
        }

        return JSONUtilities.getJsonStringFromInputStream(cr.getEntityInputStream());
    }

}
