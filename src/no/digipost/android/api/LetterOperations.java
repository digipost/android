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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import no.digipost.android.R;
import no.digipost.android.model.Account;
import no.digipost.android.model.Letter;
import no.digipost.android.model.PrimaryAccount;
import no.digipost.android.model.Receipt;

import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;

import android.accounts.NetworkErrorException;
import android.content.Context;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;

public class LetterOperations {
	public static final int MAILBOX = 0;
	public static final int WORKAREA = 1;
	public static final int ARCHIVE = 2;
	public static final int RECEIPTS = 3;

	private final ApiAccess apiAccess;
	private final Context context;
	static String tempLink;

	public LetterOperations(final Context context) {
		this.context = context;
		apiAccess = new ApiAccess(context);
	}

	public PrimaryAccount getPrimaryAccount() throws DigipostApiException, DigipostClientException {
		return apiAccess.getAccount().getPrimaryAccount();
	}

	public ArrayList<Letter> getAccountContentMeta(final int type) throws DigipostApiException, DigipostClientException {
		Account account = apiAccess.getAccount();

		PrimaryAccount primaryaccount = account.getPrimaryAccount();

		if (primaryaccount == null) {
			// TODO Midlertidig bugfix
			throw new DigipostApiException(context.getString(R.string.error_digipost_api));
		}

		tempLink = primaryaccount.getInboxUri();

		switch (type) {
		case MAILBOX:
			return apiAccess.getDocuments(primaryaccount.getInboxUri()).getDocument();
		case ARCHIVE:
			return apiAccess.getDocuments(primaryaccount.getArchiveUri()).getDocument();
		case WORKAREA:
			return apiAccess.getDocuments(primaryaccount.getWorkareaUri()).getDocument();
		default:
			return null;
		}
	}

	private String tempReceipLink() {
		return "https://www.digipost.no/post/api/private/accounts/" + tempLink.replaceAll("\\D+", "") + "/receipts";
	}

	public ArrayList<Receipt> getAccountContentMetaReceipt() throws DigipostApiException, DigipostClientException {
		return apiAccess.getReceipts(tempReceipLink()).getReceipt();
	}

	public void moveDocument(final Letter letter, final String toLocation) throws ClientProtocolException, UniformInterfaceException,
			ClientHandlerException, ParseException, IOException, URISyntaxException, IllegalStateException, NetworkErrorException,
			DigipostClientException, DigipostApiException {
		apiAccess.getMovedDocument(letter.getUpdateUri(), JSONConverter.createJsonFromJackson(letter));
	}

	public byte[] getDocumentContentPDF(final Letter letter) throws DigipostApiException, DigipostClientException {
		ApiAccess.filesize = Integer.parseInt(letter.getFileSize());
		return apiAccess.getDocumentContent(letter.getContentUri());
	}

	public String getDocumentContentHTML(final Letter letter) throws DigipostApiException, DigipostClientException {
		return apiAccess.getDocumentHTML(letter.getContentUri());
	}

	public byte[] getReceiptContentPDF(final Receipt receipt) throws DigipostApiException, DigipostClientException {
		return apiAccess.getDocumentContent(receipt.getContentAsPDFUri());
	}

	public String getReceiptContentHTML(final Receipt receipt) throws DigipostApiException, DigipostClientException {
		return apiAccess.getReceiptHTML(receipt.getContentAsHTMLUri());
	}

	public void delete(final Object object) throws DigipostApiException, DigipostClientException {
		if (object instanceof Letter) {
			Letter letter = (Letter) object;
			apiAccess.delete(letter.getDeleteUri());
		} else {
			Receipt receipt = (Receipt) object;
			apiAccess.delete(receipt.getDeleteUri());
		}
	}
}
