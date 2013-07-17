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

import java.io.File;

import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.model.Attachment;
import no.digipost.android.model.Documents;
import no.digipost.android.model.Letter;
import no.digipost.android.model.PrimaryAccount;
import no.digipost.android.model.Receipt;
import no.digipost.android.model.Receipts;
import no.digipost.android.utilities.JSONUtilities;
import android.content.Context;

public class ContentOperations {
	private static PrimaryAccount primaryAccount = null;

	private static PrimaryAccount getPrimaryAccount(Context context) throws DigipostApiException, DigipostClientException,
			DigipostAuthenticationException {
		if (primaryAccount == null) {
			primaryAccount = ApiAccess.getAccount(context).getPrimaryAccount();
		}

		return primaryAccount;
	}

	public static PrimaryAccount getPrimaryAccountUpdated(Context context) throws DigipostClientException, DigipostAuthenticationException,
			DigipostApiException {
		return ApiAccess.getAccount(context).getPrimaryAccount();
	}

	public static Documents getAccountContentMetaDocument(Context context, final int type) throws DigipostApiException,
			DigipostClientException, DigipostAuthenticationException {
		PrimaryAccount primaryAccount = getPrimaryAccount(context);

		switch (type) {
		case ApplicationConstants.MAILBOX:
			return ApiAccess.getDocuments(context, primaryAccount.getInboxUri());
		case ApplicationConstants.ARCHIVE:
			return ApiAccess.getDocuments(context, primaryAccount.getArchiveUri());
		case ApplicationConstants.WORKAREA:
			return ApiAccess.getDocuments(context, primaryAccount.getWorkareaUri());
		default:
			return null;
		}
	}

	public static Receipts getAccountContentMetaReceipt(Context context) throws DigipostApiException, DigipostClientException,
			DigipostAuthenticationException {
		return ApiAccess.getReceipts(context, getPrimaryAccount(context).getReceiptsUri());
	}

	public static void moveDocument(Context context, final Letter letter) throws DigipostClientException, DigipostApiException,
			DigipostAuthenticationException {
		ApiAccess.getMovedDocument(context, letter.getUpdateUri(), JSONUtilities.createJsonFromJackson(letter));
	}

	public static void sendOpeningReceipt(Context context, final Letter letter) throws DigipostClientException, DigipostApiException,
			DigipostAuthenticationException {
		ApiAccess.postSendOpeningReceipt(context, letter.getOpeningReceiptUri());
	}

	public static Letter getSelfLetter(Context context, final Letter letter) throws DigipostClientException, DigipostApiException,
			DigipostAuthenticationException {
		return ApiAccess.getLetterSelf(context, letter.getSelfUri());
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
		if (object instanceof Letter) {
			ApiAccess.delete(context, ((Letter) object).getDeleteUri());
		} else if (object instanceof Receipt) {
			ApiAccess.delete(context, ((Receipt) object).getDeleteUri());
		}
	}

	public static void uploadFile(Context context, File file) throws DigipostClientException, DigipostAuthenticationException,
			DigipostApiException {
		ApiAccess.uploadFile(context, getPrimaryAccount(context).getUploadUri(), file);
	}
}
