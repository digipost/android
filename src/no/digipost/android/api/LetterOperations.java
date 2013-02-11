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

	public ArrayList<Letter> getLetterList(final String access_token) {
		Account account = apiAccess.getPrimaryAccount(access_token);
		PrimaryAccount primaryAccount = account.getPrimaryAccount();
		Documents documents = apiAccess.getDocuments(access_token, primaryAccount.getDokumentsUri());

		return documents.getDocument();
	}
	/*
	public boolean moveLetter(final String access_token, final Letter letter) {
		ArrayList<Link> links = letter.getLink();
		String update_uri = "";
		for(Link l : links) {
			if(l.getRel().equals("https://www.digipost.no/post/relations/update_document")) {
				update_uri = l.getUri();
				break;
			}
		}
		String s = null;
		Letter movedletter = apiAccess.getMovedLetter(access_token, update_uri, letter);
	} */
}
