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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LetterOperations {
	public LetterOperations() {
	}

	public JSONObject getPrimaryAccount(final String access_token) {
		JSONObject primaryAccount = null;
		try {
			ApiAccess api = new ApiAccess();
			String uri = ApiConstants.URL_API;
			JSONObject root_json = api.getJSONfromURI(access_token, uri);
			primaryAccount = root_json.getJSONObject(ApiConstants.PRIMARY_ACCOUNT);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return primaryAccount;
	}

	public JSONObject getDocumentInboxLinks(final String access_token) {
		JSONObject documentLinks = null;
		try {
			JSONObject primaryAccount = getPrimaryAccount(access_token);
			JSONArray links = primaryAccount.getJSONArray(ApiConstants.LINK);
			boolean found = false;

			for (int i = 0; i < links.length(); i++) {
				documentLinks = links.getJSONObject(i);
				if (documentLinks.getString(ApiConstants.REL).equals(ApiConstants.URL_RELATIONS_DOCUMENT_INBOX)) {
					found = true;
					break;
				}
			}
			if (!found) {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return documentLinks;
	}

	public ArrayList<Letter> getLetterList(final String access_token) {
		ArrayList<Letter> letters = new ArrayList<Letter>();

		try {
			ApiAccess api = new ApiAccess();
			JSONObject documentLinks = getDocumentInboxLinks(access_token);
			if (documentLinks == null) {
				return null;
			}
			JSONObject letterobj = api.getJSONfromURI(access_token, documentLinks.getString(ApiConstants.URI));
			JSONArray letterlist = letterobj.getJSONArray(ApiConstants.DOCUMENT);

			for (int j = 0; j < letterlist.length(); j++) {
				JSONObject json_letter = letterlist.getJSONObject(j);
				Letter letter = getLetterFromJSON(json_letter);
				letters.add(letter);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return letters;
	}

	public Letter downloadLetterContent(final String access_token, final Letter l) throws Exception {

		// Metoden skal via GET hente og vise pdf.

		/*
		 * String type = l.getFileType(); String size = l.getfileSize();
		 * 
		 * HttpClient client = new DefaultHttpClient();
		 * 
		 * HttpGet request = new HttpGet(); request.setURI(new
		 * URI(l.getDownload_url())); request.setHeader("Accept",
		 * "application/octet-stream"); request.setHeader("Authorization",
		 * "Bearer " + access_token);
		 * 
		 * HttpResponse response = client.execute(request);
		 * 
		 * //return response.getEntity().getContent();
		 */

		return null;
	}

	public Letter getLetterFromJSON(final JSONObject json_letter) {
		Letter letter = new Letter();

		try {

			letter.setSubject(json_letter.getString(ApiConstants.SUBJECT));
			letter.setCreatorName(json_letter.getString(ApiConstants.CREATOR_NAME));
			letter.setCreated(json_letter.getString(ApiConstants.CREATED));
			letter.setFileType(json_letter.getString(ApiConstants.FILE_TYPE));
			letter.setFileSize(json_letter.getString(ApiConstants.FILE_SIZE));
			letter.setOrigin(json_letter.getString(ApiConstants.ORIGIN));
			letter.setAuthenticationLevel(json_letter.getString(ApiConstants.AUTHENTICATION_LEVEL));
			letter.setLocation(json_letter.getString(ApiConstants.LOCATION));
			letter.setRead(json_letter.getString(ApiConstants.READ));
			letter.setType(json_letter.getString(ApiConstants.TYPE));

			letter.setLinks(getLetterLinksFromJSON(json_letter));

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return letter;
	}

	private ArrayList<Link> getLetterLinksFromJSON(final JSONObject json_letter) throws Exception {

		ArrayList<Link> links = new ArrayList<Link>();
		JSONArray jsonlinks = json_letter.getJSONArray(ApiConstants.LINK);

		for (int i = 0; i < jsonlinks.length(); i++) {

			JSONObject temp = jsonlinks.getJSONObject(i);
			String rel = temp.getString(ApiConstants.REL);
			String uri = temp.getString(ApiConstants.URI);
			String media_type = temp.getString(ApiConstants.MEDIA_TYPE);
			Link link = new Link(rel, uri, media_type);

			links.add(link);
		}
		return links;
	}
}
