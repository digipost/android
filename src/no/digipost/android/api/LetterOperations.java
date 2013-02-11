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
	private final ApiAccess apiAcess;

	public LetterOperations() {
		apiAcess = new ApiAccess();
	}

	public ArrayList<Letter> getLetterList(final String access_token) {
		Account account = apiAcess.getPrimaryAccount(access_token);
		PrimaryAccount primaryAccount = account.getPrimaryAccount();
		Documents documents = apiAcess.getDokuments(access_token, primaryAccount.getDokumentsUri());

		return documents.getDokument();
	}
}
