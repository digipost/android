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

import java.util.ArrayList;

import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.model.Attachment;
import no.digipost.android.model.Letter;
import no.digipost.android.model.PrimaryAccount;
import no.digipost.android.model.Receipt;
import no.digipost.android.utilities.JSONUtilities;

import android.content.Context;
import android.graphics.Bitmap;

public class LetterOperations {
	public static final int MAILBOX = 0;
	public static final int WORKAREA = 1;
	public static final int ARCHIVE = 2;
	public static final int RECEIPTS = 3;

	private PrimaryAccount primaryAccount;
	private final ApiAccess apiAccess;

	public LetterOperations(final Context context) {
		apiAccess = new ApiAccess(context);
		primaryAccount = null;
	}

	public PrimaryAccount getPrimaryAccount() throws DigipostApiException, DigipostClientException, DigipostAuthenticationException {
		if (primaryAccount == null) {
			primaryAccount = apiAccess.getAccount().getPrimaryAccount();
		}
		return primaryAccount;
	}

	public ArrayList<Letter> getAccountContentMeta(final int type) throws DigipostApiException, DigipostClientException,
			DigipostAuthenticationException {
		PrimaryAccount primaryAccount = getPrimaryAccount();

		switch (type) {
		case MAILBOX:
			return apiAccess.getDocuments(primaryAccount.getInboxUri()).getDocument();
		case ARCHIVE:
			return apiAccess.getDocuments(primaryAccount.getArchiveUri()).getDocument();
		case WORKAREA:
			return apiAccess.getDocuments(primaryAccount.getWorkareaUri()).getDocument();
		default:
			return null;
		}
	}

	public ArrayList<Receipt> getAccountContentMetaReceipt() throws DigipostApiException, DigipostClientException,
			DigipostAuthenticationException {
		PrimaryAccount primaryAccount = getPrimaryAccount();
		return apiAccess.getReceipts(primaryAccount.getReceiptsUri()).getReceipt();
	}

	public void moveDocument(final Letter letter, final String toLocation) throws DigipostClientException, DigipostApiException,
			DigipostAuthenticationException {
		apiAccess.getMovedDocument(letter.getUpdateUri(), JSONUtilities.createJsonFromJackson(letter));
	}

	public byte[] getDocumentContent(final Object object) throws DigipostApiException, DigipostClientException,
			DigipostAuthenticationException {
		if (object instanceof Letter) {
			int filesize = Integer.parseInt(((Letter) object).getFileSize());
			return apiAccess.getDocumentContent(((Letter) object).getContentUri(), filesize);
		} else {
			int filesize = Integer.parseInt(((Attachment) object).getFileSize());
			return apiAccess.getDocumentContent(((Attachment) object).getContentUri(), filesize);
		}
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
		if (object instanceof Letter) {
			Letter letter = (Letter) object;
			apiAccess.delete(letter.getDeleteUri());
		} else {
			Receipt receipt = (Receipt) object;
			apiAccess.delete(receipt.getDeleteUri());
		}
	}
}
