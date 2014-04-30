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

package no.digipost.android.model;

import java.util.ArrayList;

import no.digipost.android.constants.ApiConstants;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonFilter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFilter("toJSON")
public class Letter {

	@JsonProperty
	private String subject;
	@JsonProperty
	private String creatorName;
	@JsonProperty
	private String created;
	@JsonProperty
	private String fileType;
	@JsonProperty
	private String fileSize;
	@JsonProperty
	private String origin;
	@JsonProperty("authentication-level")
	private String authenticationLevel;
	@JsonProperty
	private String location;
	@JsonProperty
	private String read;
	@JsonProperty
	private String type;
	@JsonProperty
	private ArrayList<Link> link;
	@JsonProperty
	private ArrayList<Attachment> attachment;

	public ArrayList<Attachment> getAttachment() {
		return attachment;
	}

	public void setAttachment(final ArrayList<Attachment> attachment) {
		this.attachment = attachment;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(final String subject) {
		this.subject = subject;
	}

	public String getCreatorName() {
		return creatorName;
	}

	public void setCreatorName(final String creatorName) {
		this.creatorName = creatorName;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(final String created) {
		this.created = created;
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

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(final String origin) {
		this.origin = origin;
	}

	public String getAuthenticationLevel() {
		return authenticationLevel;
	}

	public void setAuthenticationLevel(final String authenticationLevel) {
		this.authenticationLevel = authenticationLevel;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(final String location) {
		this.location = location;
	}

	public String getRead() {
		return read;
	}

	public void setRead(final String read) {
		this.read = read;
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

	public String getSelfUri() {
		for (Link l : link) {
			if (l.getRel().equals(ApiConstants.URL_RELATIONS_DOCUMENT_SELF)) {
				return l.getUri();
			}
		}
		return null;
	}

	public String getContentUri() {
		for (Link l : link) {
			if (l.getRel().equals(ApiConstants.URL_RELATIONS_DOCUMENT_GET_CONTENT)) {
				return l.getUri();
			}
		}
		return null;
	}

	public String getUpdateUri() {
		for (Link l : link) {
			if (l.getRel().equals(ApiConstants.URL_RELATIONS_DOCUMENT_UPDATE)) {
				return l.getUri();
			}
		}
		return null;
	}

	public String getOpeningReceiptUri() {
		for (Link l : link) {
			if (l.getRel().equals(ApiConstants.URL_RELATIONS_DOCUMENT_SEND_OPENING_RECEIPT)) {
				return l.getUri();
			}
		}
		return null;
	}

	public String getDeleteUri() {
		for (Link l : link) {
			if (l.getRel().equals(ApiConstants.URL_RELATIONS_DOCUMENT_DELETE)) {
				return l.getUri();
			}
		}
		return null;
	}

	public String getOrganizationLogo() {
		for (Link l : link) {
			if (l.getRel().equals(ApiConstants.URL_RELATIONS_DOCUMENT_GET_ORGANIZATION_LOGO)) {
				return l.getUri();
			}
		}
		return null;
	}

    public boolean hasOpeningReceipt() {
        for (Attachment attachment1 : attachment) {
            if(attachment1.getOpeningReceiptUri() != null){
                return true;
            }
        }

        return false;
    }

	/*
	 * @Override public String toString() { return "Letter [subject=" + subject
	 * + ", creatorName=" + creatorName + ", created=" + created + ", fileType="
	 * + fileType + ", fileSize=" + fileSize + ", origin=" + origin +
	 * ", authenticationLevel=" + authenticationLevel + ", location=" + location
	 * + ", read=" + read + ", type=" + type + "]"; }
	 */

}
