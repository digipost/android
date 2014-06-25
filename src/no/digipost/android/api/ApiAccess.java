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
import no.digipost.android.model.Account;
import no.digipost.android.model.CurrentBankAccount;
import no.digipost.android.model.Document;
import no.digipost.android.model.Documents;
import no.digipost.android.model.Folder;
import no.digipost.android.model.Receipts;
import no.digipost.android.model.Settings;
import no.digipost.android.utilities.JSONUtilities;
import no.digipost.android.utilities.NetworkUtilities;

import static com.sun.jersey.api.client.ClientResponse.Status.TEMPORARY_REDIRECT;

public class ApiAccess {
    private static Client jerseyClient = Client.create();

    public static final int POST_ACTION_MOVE = 0;
    public static final int POST_ACTION_SEND_OPENING_RECEIPT = 1;
    public static final int POST_ACTION_UPDATE_SETTINGS = 2;
    public static final int POST_ACTION_SEND_TO_BANK = 3;
    public static final int POST_ACTION_CREATE_FOLDER = 4;
    public static final int PUT_ACTION_EDIT_FOLDER = 5;
    public static final int PUT_ACTION_UPDATE_FOLDERS = 6;

    public static final int POST = 0;
    public static final int PUT = 1;

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

    public static CurrentBankAccount getCurrentBankAccount(Context context, final String uri) throws DigipostApiException, DigipostClientException, DigipostAuthenticationException {
        return (CurrentBankAccount) JSONUtilities.processJackson(CurrentBankAccount.class, getApiJsonString(context, uri));
    }

    public static Receipts getReceipts(Context context, final String uri) throws DigipostApiException, DigipostClientException, DigipostAuthenticationException {
        return (Receipts) JSONUtilities.processJackson(Receipts.class, getApiJsonString(context, uri));
    }

    public static Folder getFolderSelf(Context context, final String uri) throws DigipostApiException, DigipostClientException, DigipostAuthenticationException {
        return (Folder) JSONUtilities.processJackson(Folder.class, getApiJsonString(context, uri));
    }

    public static Document getDocumentSelf(Context context, final String uri) throws DigipostApiException, DigipostClientException, DigipostAuthenticationException {
        return (Document) JSONUtilities.processJackson(Document.class, getApiJsonString(context, uri));
    }

    public static Settings getSettings(Context context, final String uri) throws DigipostClientException, DigipostAuthenticationException, DigipostApiException {
        return (Settings) JSONUtilities.processJackson(Settings.class, getApiJsonString(context, uri));
    }

    private static ClientResponse executeGetRequest(Context context, final String uri, final String header_accept) throws DigipostClientException,
            DigipostApiException, DigipostAuthenticationException {
        if (StringUtils.isBlank(Secret.ACCESS_TOKEN)) {
            OAuth2.updateAccessToken(context);
        }

        try {
            ClientResponse cr = getClient()
                    .resource(uri)
                    .header(HttpHeaders.USER_AGENT, DigipostApplication.USER_AGENT)
                    .header(ApiConstants.ACCEPT, header_accept)
                    .header(ApiConstants.AUTHORIZATION, ApiConstants.BEARER + Secret.ACCESS_TOKEN)
                    .get(ClientResponse.class);

            if (cr.getStatus() == TEMPORARY_REDIRECT.getStatusCode()) {
                return executeGetRequest(context, cr.getHeaders().getFirst(HttpHeaders.LOCATION), header_accept);
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

        return request(context, POST_ACTION_SEND_OPENING_RECEIPT, uri, null, POST);
    }

    public static Document getMovedDocument(Context context, final String uri, final StringEntity json) throws DigipostClientException, DigipostApiException,
            DigipostAuthenticationException {
        return (Document) JSONUtilities.processJackson(Document.class, request(context, POST_ACTION_MOVE, uri, json, POST));
    }

    public static void updateAccountSettings(Context context, String uri, StringEntity json) throws DigipostAuthenticationException, DigipostClientException, DigipostApiException {
        request(context, POST_ACTION_UPDATE_SETTINGS, uri, json, POST);
    }

    public static void sendToBank(Context context, String uri) throws DigipostAuthenticationException, DigipostClientException, DigipostApiException {
        request(context, POST_ACTION_SEND_TO_BANK, uri, null, POST);
    }

    private static String request(Context context, int action, final String uri, final StringEntity json, final int httpAction) throws DigipostClientException,
            DigipostApiException, DigipostAuthenticationException {

        HttpClient httpClient = new DefaultHttpClient();
        HttpEntityEnclosingRequestBase request = new HttpPost();

        if (httpAction == POST) {
            request = new HttpPost();
        } else if (httpAction == PUT) {
            request = new HttpPut();
        }

        try {
            request.setURI(new URI(uri));
        } catch (URISyntaxException e1) {
            // Ignore
        }

        request.addHeader(ApiConstants.CONTENT_TYPE, ApiConstants.APPLICATION_VND_DIGIPOST_V2_JSON);
        request.addHeader(ApiConstants.ACCEPT, ApiConstants.APPLICATION_VND_DIGIPOST_V2_JSON);
        request.addHeader(ApiConstants.AUTHORIZATION, ApiConstants.BEARER + Secret.ACCESS_TOKEN);

        if(action == POST_ACTION_SEND_OPENING_RECEIPT || action == POST_ACTION_SEND_TO_BANK) {
        }else{
            request.setEntity(json);
        }

        HttpResponse response;
        try {
            response = httpClient.execute(request);
        } catch (Exception e) {
            throw new DigipostClientException(context.getString(R.string.error_your_network));
        }

        try {
            NetworkUtilities.checkHttpStatusCode(context, response.getStatusLine().getStatusCode());
        } catch (DigipostInvalidTokenException e) {
            OAuth2.updateAccessToken(context);

            return request(context, action, uri, json, httpAction);
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
                if (jsonObject.get("error-code").equals("FOLDER_NOT_EMPTY")) {
                    return null;
                }
            } catch (JSONException e) {
                //IGNORE
            }
        }

        try {
            NetworkUtilities.checkHttpStatusCode(context, cr.getStatus());
        } catch (DigipostInvalidTokenException e) {
            OAuth2.updateAccessToken(context);
            delete(context, uri);
        }
        return "" + cr.getStatus();
    }

    public static String createFolder(Context context, final String uri, final StringEntity json) throws DigipostClientException, DigipostApiException,
            DigipostAuthenticationException {
        return request(context, POST_ACTION_CREATE_FOLDER, uri, json, POST);
    }

    public static String editFolder(Context context, final String uri, final StringEntity json) throws DigipostClientException, DigipostApiException,
            DigipostAuthenticationException {
        return request(context, PUT_ACTION_EDIT_FOLDER, uri, json, PUT);
    }

    public static String deleteFolder(Context context, final String uri) throws DigipostClientException, DigipostApiException, DigipostAuthenticationException {
        return delete(context, uri);
    }

    public static String updateFolders(Context context, final String uri, final StringEntity json) throws DigipostClientException, DigipostApiException, DigipostAuthenticationException {
        return request(context, PUT_ACTION_UPDATE_FOLDERS, uri, json, PUT);
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
            throw new DigipostClientException(context.getString(R.string.error_your_network));
        }
    }
}
