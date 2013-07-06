/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.digipost.android.model;

import java.util.ArrayList;

import no.digipost.android.constants.ApiConstants;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Attachment {

	@JsonProperty
	private String subject;
	@JsonProperty
	private String fileType;
	@JsonProperty
	private String fileSize;
	@JsonProperty("authentication-level")
	private String authenticationLevel;
	@JsonProperty
	private String read;
	@JsonProperty
	private String mainDocument;
	@JsonProperty
	private String type;
	@JsonProperty
	private ArrayList<Link> link;
	public String getSubject() {
		return subject;
	}
	public void setSubject(final String subject) {
		this.subject = subject;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(final String fileType) {
		this.fileType = fileType;
	}
	public String getFileSize() {
		return fileSize;
	}
	public void setFileSize(final String fileSize) {
		this.fileSize = fileSize;
	}
	public String getAuthenticationLevel() {
		return authenticationLevel;
	}
	public void setAuthenticationLevel(final String authenticationLevel) {
		this.authenticationLevel = authenticationLevel;
	}
	public String getRead() {
		return read;
	}
	public void setRead(final String read) {
		this.read = read;
	}
	public String getMainDocument() {
		return mainDocument;
	}
	public void setMainDocument(final String mainDocument) {
		this.mainDocument = mainDocument;
	}
	public String getType() {
		return type;
	}
	public void setType(final String type) {
		this.type = type;
	}
	public ArrayList<Link> getLink() {
		return link;
	}
	public void setLink(final ArrayList<Link> link) {
		this.link = link;
	}

	public String getContentUri() {
		for (Link l : link) {
			if (l.getRel().equals(ApiConstants.URL_RELATIONS_DOCUMENT_GET_CONTENT)) {
				return l.getUri();
			}
		}
		return null;
	}
}
