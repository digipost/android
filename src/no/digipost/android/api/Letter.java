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

public class Letter {

	private String subject;
	private String creatorName;
	private String created;
	private String fileType;
	private String fileSize;
	private String origin;
	private String authenticationLevel;
	private String location;
	private String read;
	private String type;
	private String download_url;
	private ArrayList<Link> links;

	public Letter(final String subject, final String creatorName, final String created, final String fileType, final String fileSize,
			final String origin, final String authenticationLevel, final String location, final String read, final String type,
			final String download_url, final ArrayList<Link> links) {
		super();
		this.subject = subject;
		this.creatorName = creatorName;
		this.created = created;
		this.fileType = fileType;
		this.fileSize = fileSize;
		this.origin = origin;
		this.authenticationLevel = authenticationLevel;
		this.location = location;
		this.read = read;
		this.type = type;
		this.download_url = download_url;
		this.links = links;
	}

	public Letter() {

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

	public String getDownload_url() {
		return download_url;
	}

	public void setDownload_url(final String download_url) {
		this.download_url = download_url;
	}

	public ArrayList<Link> getLinks() {
		return links;
	}

	public void setLinks(final ArrayList<Link> links) {
		this.links = links;
	}

	@Override
	public String toString() {
		return "Letter [subject=" + subject + ", creatorName=" + creatorName + ", created=" + created + ", fileType=" + fileType
				+ ", fileSize=" + fileSize + ", origin=" + origin + ", authenticationLevel=" + authenticationLevel + ", location="
				+ location + ", read=" + read + ", type=" + type + ", download_url=" + download_url + ", links=" + links + "]";
	}
}
