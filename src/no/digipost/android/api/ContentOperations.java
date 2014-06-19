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

import java.io.File;
import java.util.ArrayList;

import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.model.Account;
import no.digipost.android.model.Attachment;
import no.digipost.android.model.CurrentBankAccount;
import no.digipost.android.model.Document;
import no.digipost.android.model.Documents;
import no.digipost.android.model.Folder;
import no.digipost.android.model.Mailbox;
import no.digipost.android.model.Receipt;
import no.digipost.android.model.Receipts;
import no.digipost.android.model.Settings;
import no.digipost.android.utilities.JSONUtilities;

public class ContentOperations {
	private static Account account = null;
    private static Mailbox mailbox = null;
    public static String digipostAddress = null;

	public static Account getAccount(Context context) throws DigipostApiException, DigipostClientException,
			DigipostAuthenticationException {

		if (account == null) {
            account = ApiAccess.getAccount(context);
		}

		return account;
	}

    public static Mailbox getCurrentMailbox(Context context) throws DigipostApiException, DigipostClientException,
            DigipostAuthenticationException {

        if (mailbox == null) {
            if(digipostAddress == null) {
                digipostAddress = getAccount(context).getPrimaryAccount().getDigipostaddress();
            }
            mailbox = getAccount(context).getMailboxByDigipostAddress(digipostAddress);
        }

        return mailbox;
    }

    public static void setAccountToNull(){
        account = null;
    }

	public static Account getAccountUpdated(Context context) throws DigipostClientException, DigipostAuthenticationException,
			DigipostApiException {

        mailbox = null;
        account = ApiAccess.getAccount(context);
		return account;
	}

	public static Documents getAccountContentMetaDocument(Context context, int content) throws DigipostApiException,
			DigipostClientException, DigipostAuthenticationException {
        getCurrentMailbox(context);

        if(content == ApplicationConstants.MAILBOX){
            return ApiAccess.getDocuments(context, mailbox.getInboxUri());
        }else {
            content-= ApplicationConstants.numberOfStaticFolders;
            mailbox = getCurrentMailbox(context);
            ArrayList<Folder> folders = mailbox.getFolders().getFolder();
            Folder folder = folders.get(content);
            folder = ApiAccess.getFolderSelf(context, folder.getSelfUri());

            return folder.getDocuments();
        }
	}

    public static String getUploadUri(Context context, int content)throws DigipostApiException,
            DigipostClientException, DigipostAuthenticationException {

        getCurrentMailbox(context);

        if(content == ApplicationConstants.MAILBOX){
            return mailbox.getUploadToInboxUri();
        }else {
            content-=ApplicationConstants.numberOfStaticFolders;
            ArrayList<Folder> folders = mailbox.getFolders().getFolder();
            return folders.get(content).getUploadUri();
        }
    }

    public static void resetState(){
        digipostAddress = null;
        account = null;
        mailbox = null;
    }

    public static boolean changeMailbox(String newDigipostAddress){
        if(!digipostAddress.equals(newDigipostAddress)) {
            digipostAddress = newDigipostAddress;
            mailbox = null;
            return true;
        }
        return false;
    }

    public static CurrentBankAccount getCurrentBankAccount(Context context) throws DigipostClientException, DigipostAuthenticationException,
            DigipostApiException {
        String uri = getAccount(context).getPrimaryAccount().getCurrentBankAccountUri();
        return ApiAccess.getCurrentBankAccount(context, uri);
    }
	public static Receipts getAccountContentMetaReceipt(Context context) throws DigipostApiException, DigipostClientException,
			DigipostAuthenticationException {
		return ApiAccess.getReceipts(context, getCurrentMailbox(context).getReceiptsUri());
	}

	public static void moveDocument(Context context, final Document document) throws DigipostClientException, DigipostApiException,
			DigipostAuthenticationException {
		ApiAccess.getMovedDocument(context, document.getUpdateUri(), JSONUtilities.createJsonFromJackson(document));
	}

    public static boolean changeDeleteFolder(Context context, final Folder folder, final String action) throws DigipostClientException, DigipostApiException,
            DigipostAuthenticationException {
        if(action.equals(ApiConstants.CHANGE)){
            if(ApiAccess.changeFolder(context, folder.getChangeUri(), JSONUtilities.createJsonFromJackson(folder)) != null){
                return true;
            }
        }else if(action.equals(ApiConstants.DELETE)){
            ApiAccess.deleteFolder(context, folder.getDeleteUri());
            return true;
        }
        return false;
    }

    public static void updateAccountSettings(Context context, Settings settings) throws DigipostAuthenticationException, DigipostClientException, DigipostApiException {
        ApiAccess.updateAccountSettings(context, settings.getSettingsUri(), JSONUtilities.createJsonFromJackson(settings));
    }

    public static String sendOpeningReceipt(Context context, final Attachment attachment) throws DigipostClientException, DigipostApiException,
            DigipostAuthenticationException {
        return ApiAccess.postSendOpeningReceipt(context, attachment.getOpeningReceiptUri());
    }

    public static void sendToBank(Context context, final Attachment attachment) throws DigipostClientException, DigipostApiException,
            DigipostAuthenticationException {
        ApiAccess.sendToBank(context, attachment.getInvoice().getSendToBank());
    }

	public static Document getDocumentSelf(Context context, final Document document) throws DigipostClientException, DigipostApiException,
            DigipostAuthenticationException {
        return ApiAccess.getDocumentSelf(context, document.getSelfUri());
    }

	public static byte[] getDocumentContent(Context context, final Attachment attachment) throws DigipostApiException,
			DigipostClientException, DigipostAuthenticationException {
		int fileSize = Integer.parseInt(attachment.getFileSize());
		return ApiAccess.getDocumentContent(context, attachment.getContentUri(), fileSize);
	}

	public static String getReceiptContentHTML(Context context, final Receipt receipt) throws DigipostApiException,
			DigipostClientException, DigipostAuthenticationException {
		return ApiAccess.getReceiptHTML(context, receipt.getContentAsHTMLUri());
	}

	public static void deleteContent(Context context, final Object object) throws DigipostApiException, DigipostClientException,
			DigipostAuthenticationException {
		if (object instanceof Document) {
			ApiAccess.delete(context, ((Document) object).getDeleteUri());
		} else if (object instanceof Receipt) {
			ApiAccess.delete(context, ((Receipt) object).getDeleteUri());
		}
	}

	public static void uploadFile(Context context, File file, int content) throws DigipostClientException, DigipostAuthenticationException,
			DigipostApiException {
        String uploadUri = getUploadUri(context,content);
		ApiAccess.uploadFile(context, uploadUri, file);
	}

    public static Settings getSettings(Context context) throws DigipostClientException, DigipostAuthenticationException, DigipostApiException {
        return ApiAccess.getSettings(context, getCurrentMailbox(context).getSettingsUri());
    }
}
