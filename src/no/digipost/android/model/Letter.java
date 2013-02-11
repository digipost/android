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

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
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
	@JsonProperty
	private String authenticationLevel;
	@JsonProperty
	private String location;
	@JsonProperty
	private String read;
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

	public ArrayList<Link> getLink () {
		return link;
	}
}
