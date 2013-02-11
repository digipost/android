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

import no.digipost.android.model.Account;
import no.digipost.android.model.Documents;
import no.digipost.android.model.Letter;
import no.digipost.android.model.PrimaryAccount;

public class LetterOperations {
	private final ApiAccess apiAccess;

	public LetterOperations() {
		apiAccess = new ApiAccess();
	}

	public ArrayList<Letter> getMailboxList(final String access_token) {
		Account account = apiAccess.getPrimaryAccount(access_token);
		PrimaryAccount primaryAccount = account.getPrimaryAccount();
		Documents documents = apiAccess.getDocuments(access_token, primaryAccount.getInboxUri());

		return documents.getDocument();
	}

	public ArrayList<Letter> getArchiveList(final String access_token) {
		Account account = apiAccess.getPrimaryAccount(access_token);
		PrimaryAccount primaryaccount = account.getPrimaryAccount();
		Documents documents = apiAccess.getDocuments(access_token, primaryaccount.getArchiveUri());

		return documents.getDocument();
	}

	public ArrayList<Letter> getWorkareaList(final String access_token) {
		Account account = apiAccess.getPrimaryAccount(access_token);
		PrimaryAccount primaryaccount = account.getPrimaryAccount();
		Documents documents = apiAccess.getDocuments(access_token, primaryaccount.getWorkareaUri());

		return documents.getDocument();
	}

	public ArrayList<Letter> getReceiptsList(final String access_token) {
		Account account = apiAccess.getPrimaryAccount(access_token);
		PrimaryAccount primaryaccount = account.getPrimaryAccount();
		Documents documents = apiAccess.getDocuments(access_token, primaryaccount.getReceiptsUri());

		return documents.getDocument();
	}

	/*
	 * public boolean moveDocument(final String access_token, final Letter
	 * letter) { ArrayList<Link> links = letter.getLink(); String update_uri =
	 * ""; for (Link l : links) { if
	 * (l.getRel().equals("https://www.digipost.no/post/relations/update_document"
	 * )) { update_uri = l.getUri(); break; } } String s = null; Letter
	 * movedletter = apiAccess.getMovedDocument(access_token, update_uri,
	 * letter); }
	 */

}
