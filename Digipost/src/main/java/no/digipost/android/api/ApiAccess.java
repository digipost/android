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
import android.os.Build;
import android.util.Log;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import no.digipost.android.DigipostApplication;
import no.digipost.android.R;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.api.exception.DigipostInvalidTokenException;
import no.digipost.android.authentication.OAuth;
import no.digipost.android.authentication.TokenStore;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.utilities.JSONUtilities;
import no.digipost.android.utilities.NetworkUtilities;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;

import static com.sun.jersey.api.client.ClientResponse.Status.TEMPORARY_REDIRECT;

public class ApiAccess {

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

    private ClientResponse get(Context context, final String uri, final String header_accept, final MultivaluedMap<String, String> params) throws DigipostClientException,
            DigipostApiException, DigipostAuthenticationException {

        try {
            if (StringUtils.isBlank(TokenStore.getAccess())){
                OAuth.updateAccessTokenWithRefreshToken(context);
            }

            ClientResponse cr;
            if(params == null) {
                cr = getClient()
                        .resource(uri)
                        .header(HttpHeaders.USER_AGENT, DigipostApplication.USER_AGENT)
                        .header(ApiConstants.ACCEPT, header_accept)
                        .header(ApiConstants.AUTHORIZATION, ApiConstants.BEARER + TokenStore.getAccess())
                        .get(ClientResponse.class);
            }else{
                cr = getClient()
                        .resource(uri)
                        .queryParams(params)
                        .header(HttpHeaders.USER_AGENT, DigipostApplication.USER_AGENT)
                        .header(ApiConstants.ACCEPT, header_accept)
                        .header(ApiConstants.AUTHORIZATION, ApiConstants.BEARER + TokenStore.getAccess())
                        .get(ClientResponse.class);
            }

            if (cr.getStatus() == TEMPORARY_REDIRECT.getStatusCode()) {
                return get(context, cr.getHeaders().getFirst(HttpHeaders.LOCATION), header_accept, params);
            }

            return cr;
        } catch(DigipostClientException e){
            Log.e(TAG, context.getString(R.string.error_your_network));
            throw new DigipostClientException(context.getString(R.string.error_your_network));
        }
    }

    public String postput(Context context, final int httpType, final String uri, final StringEntity json) throws DigipostClientException,
            DigipostApiException, DigipostAuthenticationException {
        HttpsURLConnection httpsClient;
        InputStream result = null;
        try {
            URL url = new URL(uri);
            httpsClient = (HttpsURLConnection) url.openConnection();

            if (httpType == POST) {
                httpsClient.setRequestMethod("POST");
            } else if (httpType == PUT) {
                httpsClient.setRequestMethod("PUT");
            }
            httpsClient.setRequestProperty(ApiConstants.CONTENT_TYPE, ApiConstants.APPLICATION_VND_DIGIPOST_V2_JSON);
            httpsClient.setRequestProperty(ApiConstants.ACCEPT, ApiConstants.APPLICATION_VND_DIGIPOST_V2_JSON);
            httpsClient.setRequestProperty(ApiConstants.AUTHORIZATION, ApiConstants.BEARER + TokenStore.getAccess());

            OutputStream outputStream;
            outputStream = new BufferedOutputStream(httpsClient.getOutputStream());
            if (json != null) {
                json.writeTo(outputStream);
            }

            outputStream.flush();

            int statusCode = httpsClient.getResponseCode();

            try {
                NetworkUtilities.checkHttpStatusCode(context, statusCode);
            } catch (DigipostInvalidTokenException e) {
                OAuth.updateAccessTokenWithRefreshToken(context);
                return postput(context, httpType, uri, json);
            }

            if (statusCode == NetworkUtilities.HTTP_STATUS_NO_CONTENT) {
                return NetworkUtilities.SUCCESS_NO_CONTENT;
            }


        try {
            result = httpsClient.getInputStream();
        } catch (IllegalStateException e) {
            Log.e(TAG, e.getMessage());
        }

        }catch (IOException e){
            throw new DigipostClientException(context.getString(R.string.error_your_network));
        }

        return JSONUtilities.getJsonStringFromInputStream(result);
    }

    public static String delete(Context context, final String uri) throws DigipostClientException, DigipostApiException, DigipostAuthenticationException {
        Client client = Client.create();
        ClientResponse cr;
        try {
            cr = client
                    .resource(uri)
                    .header(ApiConstants.ACCEPT, ApiConstants.APPLICATION_VND_DIGIPOST_V2_JSON)
                    .header(ApiConstants.AUTHORIZATION, ApiConstants.BEARER + TokenStore.getAccess())
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
            OAuth.updateAccessTokenWithRefreshToken(context);
            delete(context, uri);
        }
        return "" + cr.getStatus();
    }

    public String getReceiptHTML(Context context, final String uri, final MultivaluedMap<String, String> params) throws DigipostApiException, DigipostClientException, DigipostAuthenticationException {
        ClientResponse cr = get(context, uri, ApiConstants.TEXT_HTML, params);

        try {
            NetworkUtilities.checkHttpStatusCode(context, cr.getStatus());
        } catch (DigipostInvalidTokenException e) {
            Log.e(TAG, context.getString(R.string.error_invalid_token));
            OAuth.updateAccessTokenWithRefreshToken(context);
            return getReceiptHTML(context, uri, params);
        }

        return JSONUtilities.getJsonStringFromInputStream(cr.getEntityInputStream());
    }

    public static void uploadFile(Context context, String uri, File file) throws DigipostClientException {
        try {
            try {


                FileBody filebody = new FileBody(file, ApiConstants.CONTENT_OCTET_STREAM);
                MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, null, Charset.forName(ApiConstants.ENCODING));
                multipartEntity.addPart("subject", new StringBody(FilenameUtils.removeExtension(file.getName()), ApiConstants.MIME, Charset.forName(ApiConstants.ENCODING)));
                multipartEntity.addPart("file", filebody);
                multipartEntity.addPart("token", new StringBody(TokenStore.getAccess()));

                URL url = new URL(uri);
                HttpsURLConnection httpsClient = (HttpsURLConnection) url.openConnection();
                httpsClient.setRequestMethod("POST");
                httpsClient.setDoOutput(true);
                if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    httpsClient.setFixedLengthStreamingMode(multipartEntity.getContentLength());
                }else {
                    httpsClient.setChunkedStreamingMode(0);
                }
                httpsClient.setRequestProperty("Connection", "Keep-Alive");
                httpsClient.addRequestProperty("Content-length", multipartEntity.getContentLength()+"");
                httpsClient.setRequestProperty(ApiConstants.AUTHORIZATION, ApiConstants.BEARER + TokenStore.getAccess());
                httpsClient.addRequestProperty(multipartEntity.getContentType().getName(), multipartEntity.getContentType().getValue());

                try {
                    OutputStream outputStream = new BufferedOutputStream(httpsClient.getOutputStream());
                    multipartEntity.writeTo(outputStream);
                    outputStream.flush();
                    NetworkUtilities.checkHttpStatusCode(context, httpsClient.getResponseCode());
                }finally {
                    httpsClient.disconnect();
                }

            } catch (DigipostInvalidTokenException e) {
                OAuth.updateAccessTokenWithRefreshToken(context);
                uploadFile(context, uri, file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, context.getString(R.string.error_your_network));
            throw new DigipostClientException(context.getString(R.string.error_your_network));
        }
    }

    public String getApiJsonString(Context context, final String uri, final MultivaluedMap<String, String> params) throws DigipostApiException, DigipostClientException, DigipostAuthenticationException {
        if(uri == null) {
            return null;
        }
        ClientResponse cr = get(context, uri, ApiConstants.APPLICATION_VND_DIGIPOST_V2_JSON, params);
        try {
            NetworkUtilities.checkHttpStatusCode(context, cr.getStatus());
        } catch (DigipostInvalidTokenException e) {
            OAuth.updateAccessTokenWithRefreshToken(context);
            return getApiJsonString(context, uri, params);
        }

        return JSONUtilities.getJsonStringFromInputStream(cr.getEntityInputStream());
    }

}
