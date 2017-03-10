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

import android.content.Context;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.model.*;
import no.digipost.android.utilities.JSONUtilities;
import no.digipost.android.utilities.NetworkUtilities;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.core.MultivaluedMap;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;

public class ContentOperations {

    public static String digipostAddress = null;
    private static Account account = null;
    private static Mailbox mailbox = null;
    private static ApiAccess apiAccess;

    public static Account getAccount(Context context) throws DigipostApiException, DigipostClientException, DigipostAuthenticationException {

        refreshApiAccess();
        if (account == null) {
            account = (Account) JSONUtilities.processJackson(Account.class, apiAccess.getApiJsonString(context, ApiConstants.URL_API,null));
        }

        return account;
    }

    public static void refreshApiAccess() {
        if (apiAccess == null) {
            apiAccess = new ApiAccess();
        }
    }

    public static Mailbox getCurrentMailbox(Context context) throws DigipostApiException, DigipostClientException,
            DigipostAuthenticationException {

        if (mailbox == null) {
            if (digipostAddress == null) {
                digipostAddress = getAccount(context).getPrimaryAccount().getDigipostaddress();
            }
            mailbox = getAccount(context).getMailboxByDigipostAddress(digipostAddress);
        }

        return mailbox;
    }

    public static void setAccountToNull() {
        account = null;
    }

    public static Account getAccountUpdated(Context context) throws DigipostClientException, DigipostAuthenticationException,
            DigipostApiException {
        mailbox = null;
        refreshApiAccess();

        account = (Account) JSONUtilities.processJackson(Account.class, apiAccess.getApiJsonString(context, ApiConstants.URL_API,null));
        return account;
    }

    public static Documents getAccountContentMetaDocument(Context context, int content, String oldestVisibleDocumentDate) throws DigipostApiException,
            DigipostClientException, DigipostAuthenticationException {

        getCurrentMailbox(context);
        if (mailbox == null) {
            return null;
        }

        refreshApiAccess();

        if(oldestVisibleDocumentDate == null){
            oldestVisibleDocumentDate = Long.toString(new Date().getTime());
        }

        MultivaluedMap params = new MultivaluedMapImpl();
        params.add(ApiConstants.GET_DOCUMENT_LASTSEEN, oldestVisibleDocumentDate);
        params.add(ApiConstants.GET_DOCUMENT_LIMIT, Integer.toString(ApiConstants.GET_DOCUMENT_LIMIT_N));

        if (content == ApplicationConstants.MAILBOX) {
            return (Documents) JSONUtilities.processJackson(Documents.class, apiAccess.getApiJsonString(context, mailbox.getInboxUri(), params));
        } else {
            content -= ApplicationConstants.numberOfStaticFolders;
            ArrayList<Folder> folders = mailbox.getFolders().getFolder();
            Folder folder = folders.get(content);
            folder = (Folder) JSONUtilities.processJackson(Folder.class, apiAccess.getApiJsonString(context, folder.getSelfUri(), params));
            return folder != null ? folder.getDocuments() : null;
        }
    }

    public static String getUploadUri(Context context, int content) throws DigipostApiException, DigipostClientException,
            DigipostAuthenticationException {

        getCurrentMailbox(context);

        if (content == ApplicationConstants.MAILBOX) {
            return mailbox.getUploadToInboxUri();
        } else {
            content -= ApplicationConstants.numberOfStaticFolders;
            ArrayList<Folder> folders = mailbox.getFolders().getFolder();
            return folders.get(content).getUploadUri();
        }
    }

    public static void resetState() {
        digipostAddress = null;
        account = null;
        mailbox = null;
    }

    public static boolean changeMailbox(String newDigipostAddress) {
        if (!digipostAddress.equals(newDigipostAddress)) {
            digipostAddress = newDigipostAddress;
            mailbox = null;
            return true;
        }
        return false;
    }

    public static CurrentBankAccount getCurrentBankAccount(Context context) throws DigipostClientException,
            DigipostAuthenticationException, DigipostApiException {
        String uri = getAccount(context).getPrimaryAccount().getCurrentBankAccountUri();
        refreshApiAccess();

        return (CurrentBankAccount) JSONUtilities.processJackson(CurrentBankAccount.class, apiAccess.getApiJsonString(context, uri, null));
    }

    public static Receipts getAccountContentMetaReceipt(Context context, int skip) throws DigipostApiException, DigipostClientException,
            DigipostAuthenticationException {
        refreshApiAccess();

        MultivaluedMap params = new MultivaluedMapImpl();
        params.add(ApiConstants.GET_RECEIPT_SKIP, String.valueOf(skip));
        String result = apiAccess.getApiJsonString(context, getCurrentMailbox(context).getReceiptsUri(), params);
        return (Receipts) JSONUtilities.processJackson(Receipts.class, result);
    }

    public static void revokeOAuthToken(Context context) throws DigipostClientException, DigipostApiException,
            DigipostAuthenticationException {
        apiAccess.postput(context, ApiAccess.POST, ApiConstants.URL_API_OAUTH_REVOKE, null);
    }

    public static void moveDocument(Context context, final Document document) throws DigipostClientException, DigipostApiException,
            DigipostAuthenticationException {
        JSONUtilities.processJackson(Document.class, apiAccess.postput(context, ApiAccess.POST, document.getUpdateUri(), JSONUtilities.createJsonFromJackson(document)));
    }

    public static String updateFolders(Context context, final ArrayList<Folder> newFolders) throws DigipostClientException,
            DigipostApiException, DigipostAuthenticationException {

        if (mailbox == null) {
            getCurrentMailbox(context);
        }
        refreshApiAccess();
        Folders folders = mailbox.getFolders();
        folders.setFolder(newFolders);
        return apiAccess.postput(context, ApiAccess.PUT, folders.getUpdateFoldersUri(), JSONUtilities.createJsonFromJackson(folders));
    }

    public static int createEditDeleteFolder(Context context, final Folder folder, final String action) throws DigipostClientException,
            DigipostApiException, DigipostAuthenticationException {
        if (mailbox == null) {
            getCurrentMailbox(context);
        }
        refreshApiAccess();

        if (action.equals(ApiConstants.CREATE)) {
            if (apiAccess.postput(context, ApiAccess.POST, mailbox.getFolders().getCreateFolderUri(), JSONUtilities.createJsonFromJackson(folder)) != null) {
                return NetworkUtilities.SUCCESS;
            }

            return NetworkUtilities.BAD_REQUEST;
        } else if (action.equals(ApiConstants.EDIT)) {

            if (apiAccess.postput(context, ApiAccess.PUT, folder.getChangeUri(), JSONUtilities.createJsonFromJackson(folder)) != null) {
                return NetworkUtilities.SUCCESS;
            }
            return NetworkUtilities.BAD_REQUEST;

        } else if (action.equals(ApiConstants.DELETE)) {
            if (ApiAccess.delete(context, folder.getDeleteUri()) != null) {
                return NetworkUtilities.SUCCESS;
            }
            return NetworkUtilities.BAD_REQUEST_DELETE;
        }
        return NetworkUtilities.SUCCESS;
    }

    public static void updateAccountSettings(Context context, Settings settings) throws DigipostAuthenticationException,
            DigipostClientException, DigipostApiException {
        refreshApiAccess();
        apiAccess.postput(context, ApiAccess.POST, settings.getSettingsUri(), JSONUtilities.createJsonFromJackson(settings));
    }

    public static String sendOpeningReceipt(Context context, final Attachment attachment) throws DigipostClientException,
            DigipostApiException, DigipostAuthenticationException {
        refreshApiAccess();
        return apiAccess.postput(context, ApiAccess.POST, attachment.getOpeningReceiptUri(), null);
    }

    public static boolean sendGCMRegistrationToken(Context context, final String registrationToken) throws DigipostClientException, DigipostApiException, JSONException, UnsupportedEncodingException,
            DigipostAuthenticationException {

        JSONObject json = new JSONObject();
        json.put("token", registrationToken);
        json.put("device", "Android");
        StringEntity params = new StringEntity(json.toString());

        refreshApiAccess();
        String result = apiAccess.postput(context, ApiAccess.POST, ApiConstants.URL_RELATIONS_PUSH_REGISTRATION, params);
        return NetworkUtilities.SUCCESS_NO_CONTENT.equals(result);
    }

    public static void sendToBank(Context context, final Attachment attachment) throws DigipostClientException, DigipostApiException,
            DigipostAuthenticationException {
        refreshApiAccess();
        apiAccess.postput(context, ApiAccess.POST, attachment.getInvoice().getSendToBank(), null);
    }

    public static Document getDocumentSelf(Context context, final Document document) throws DigipostClientException, DigipostApiException,
            DigipostAuthenticationException {
        refreshApiAccess();
        return (Document) JSONUtilities.processJackson(Document.class, apiAccess.getApiJsonString(context, document.getSelfUri(),null));
    }

    public static String getReceiptContentHTML(Context context, final Receipt receipt) throws DigipostApiException,
            DigipostClientException, DigipostAuthenticationException {
        refreshApiAccess();
        return apiAccess.getReceiptHTML(context, receipt.getContentAsHTMLUri(), null);
    }

    public static void deleteContent(Context context, final Object object) throws DigipostApiException, DigipostClientException,
            DigipostAuthenticationException {
        refreshApiAccess();
        if (object instanceof Document) {
            ApiAccess.delete(context, ((Document) object).getDeleteUri());
        } else if (object instanceof Receipt) {
            ApiAccess.delete(context, ((Receipt) object).getDeleteUri());
        }
    }

    public static void uploadFile(Context context, File file, int content) throws DigipostClientException, DigipostAuthenticationException,
            DigipostApiException {
        refreshApiAccess();
        String uploadUri = getUploadUri(context, content);
        ApiAccess.uploadFile(context, uploadUri, file);
    }

    public static Settings getSettings(Context context) throws DigipostClientException, DigipostAuthenticationException,
            DigipostApiException {
        if (apiAccess == null) {
            apiAccess = new ApiAccess();
        }
        return (Settings) JSONUtilities.processJackson(Settings.class, apiAccess.getApiJsonString(context, getCurrentMailbox(context).getSettingsUri(), null));
    }

    public static Banks getBanks(final Context context)throws DigipostClientException, DigipostAuthenticationException,
            DigipostApiException {
        refreshApiAccess();

        return (Banks) JSONUtilities.processJackson(Banks.class, apiAccess.getApiJsonString(context, getAccount(context).getPrimaryAccount().getBanksUri(), null));
    }
    public static void cancelBankAgreement(Context context, final String cancelAgreementURI) throws DigipostClientException, DigipostApiException,
            DigipostAuthenticationException {
        refreshApiAccess();
        ApiAccess.delete(context, cancelAgreementURI);
    }
}
