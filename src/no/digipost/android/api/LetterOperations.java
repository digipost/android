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
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.model.Attachment;
import no.digipost.android.model.Letter;
import no.digipost.android.model.PrimaryAccount;
import no.digipost.android.model.Receipt;
import no.digipost.android.model.Receipts;
import no.digipost.android.utilities.JSONUtilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

public class LetterOperations {


	private static PrimaryAccount primaryAccount;
	private static ApiAccess apiAccess;

	public LetterOperations(final Context context) {
		apiAccess = new ApiAccess(context);
		primaryAccount = null;
	}

	public static PrimaryAccount getPrimaryAccount() throws DigipostApiException, DigipostClientException, DigipostAuthenticationException {
		if (primaryAccount == null) {
			primaryAccount = apiAccess.getAccount().getPrimaryAccount();
		}
		return primaryAccount;
	}

	public ArrayList<Letter> getAccountContentMeta(final int type) throws DigipostApiException, DigipostClientException,
			DigipostAuthenticationException {
		PrimaryAccount primaryAccount = getPrimaryAccount();

		switch (type) {
		case ApplicationConstants.MAILBOX:
			return apiAccess.getDocuments(primaryAccount.getInboxUri()).getDocument();
		case ApplicationConstants.ARCHIVE:
			return apiAccess.getDocuments(primaryAccount.getArchiveUri()).getDocument();
		case ApplicationConstants.WORKAREA:
			return apiAccess.getDocuments(primaryAccount.getWorkareaUri()).getDocument();
		default:
			return null;
		}
	}

	public Receipts getAccountContentMetaReceipt() throws DigipostApiException, DigipostClientException,
			DigipostAuthenticationException {
		PrimaryAccount primaryAccount = getPrimaryAccount();
		return apiAccess.getReceipts(primaryAccount.getReceiptsUri());
	}

	public void moveDocument(final Letter letter, final String toLocation) throws DigipostClientException, DigipostApiException,
			DigipostAuthenticationException {
        // ToDo slette metode
		apiAccess.getMovedDocument(letter.getUpdateUri(), JSONUtilities.createJsonFromJackson(letter));
	}

    public void moveDocument(final Letter letter) throws DigipostClientException, DigipostApiException,
            DigipostAuthenticationException {
        apiAccess.getMovedDocument(letter.getUpdateUri(), JSONUtilities.createJsonFromJackson(letter));
    }

    public void sendOpeningReceipt(final Letter letter) throws DigipostClientException, DigipostApiException,
            DigipostAuthenticationException {
        apiAccess.sendOpeningReceipt(letter.getOpeningReceiptUri());
    }

    public Letter getSelfLetter(final Letter letter) throws DigipostClientException, DigipostApiException,
        DigipostAuthenticationException{
        return apiAccess.getLetterSelf(letter.getSelfUri());
    }

	public byte[] getDocumentContent(final Attachment attachment) throws DigipostApiException, DigipostClientException,
			DigipostAuthenticationException {
	    int filesize = Integer.parseInt(attachment.getFileSize());
		return apiAccess.getDocumentContent(attachment.getContentUri(), filesize);
	}

    public byte[] getDocumentContent(final Object attachment) throws DigipostApiException, DigipostClientException,
            DigipostAuthenticationException {
        // ToDo slette metode
        return null;
    }

	public String getDocumentContentHTML(final Object object) throws DigipostApiException, DigipostClientException,
			DigipostAuthenticationException {
		if (object instanceof Letter) {
			return apiAccess.getDocumentHTML(((Letter) object).getContentUri());
		} else {
			return apiAccess.getDocumentHTML(((Attachment) object).getContentUri());
		}
	}

	public String getReceiptContentHTML(final Receipt receipt) throws DigipostApiException, DigipostClientException,
			DigipostAuthenticationException {
		return apiAccess.getReceiptHTML(receipt.getContentAsHTMLUri());
	}

	public void delete(final Object object) throws DigipostApiException, DigipostClientException, DigipostAuthenticationException {
        // ToDo slette metode
		if (object instanceof Letter) {
			Letter letter = (Letter) object;
			apiAccess.delete(letter.getDeleteUri());
		} else {
			Receipt receipt = (Receipt) object;
			apiAccess.delete(receipt.getDeleteUri());
		}
	}

    public void deleteContent(final Object object) throws DigipostApiException, DigipostClientException, DigipostAuthenticationException {
        if (object instanceof Letter) {
            apiAccess.delete(((Letter) object).getDeleteUri());
        } else if (object instanceof Receipt) {
            apiAccess.delete(((Receipt) object).getDeleteUri());
        }
    }

    public static void uploadFile(Context context, File file) throws DigipostClientException, DigipostAuthenticationException, DigipostApiException {
        ApiAccess apiAccess = new ApiAccess(context);
        apiAccess.uploadFile(getPrimaryAccount().getUploadUri(), file);
    }
}
