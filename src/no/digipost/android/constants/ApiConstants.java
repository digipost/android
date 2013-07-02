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

package no.digipost.android.constants;

public class ApiConstants {

	public static final String URI = "uri";
	public static final String REL = "rel";
	public static final String LINK = "link";
	public static final String DOCUMENT = "document";
	public static final String SUBJECT = "subject";
	public static final String CREATOR_NAME = "creatorName";
	public static final String CREATED = "created";
	public static final String FILE_TYPE = "fileType";
	public static final String FILE_SIZE = "fileSize";
	public static final String ORIGIN = "origin";
	public static final String AUTHENTICATION_LEVEL = "authentication-level";
	public static final String LOCATION = "location";
	public static final String READ = "read";
	public static final String TYPE = "type";
	public static final String MEDIA_TYPE = "media-type";
	public static final String PRIMARY_ACCOUNT = "primaryAccount";

	public static final String LOCATION_ARCHIVE = "ARCHIVE";
	public static final String LOCATION_WORKAREA = "WORKAREA";
	public static final String LOCATION_INBOX = "INBOX";
	public static final String LOCATION_FROM = "from";
	public static final String LOCATION_TO = "to";

	public static final String FILETYPE_PDF = "pdf";
	public static final String FILETYPE_HTML = "html";
	public static final String FILETYPE_ATTACHMENT = "attachment";
	public static final String FILETYPE_JPG = "jpg";
	public static final String FILETYPE_JPEG = "jpeg";
	public static final String FILETYPE_PNG = "png";
	public static final String FILETYPE_TIFF = "tiff";
	public static final String FILETYPE_TIF = "tif";
	public static final String FILETYPE_JPE = "jpe";
	public static final String FILETYPE_JFIF = "jfif";

	public static final String GET_DOCUMENT = "document";
	public static final String GET_RECEIPT = "receipt";

	public static final String DELETE = "delete";
	public static final String LETTER = "letter";
	public static final String RECEIPT = "receipt";
	public static final String ACTION = "action";
	public static final String AUTHENTICATION_LEVEL_TWO_FACTOR = "TWO_FACTOR";
	public static final String DOCUMENT_TYPE = "type";

	public static final int TYPE_LETTER = 1;
	public static final int TYPE_RECEIPT = 2;

	public static final String GRANT_TYPE = "grant_type";
	public static final String AUD = "aud";
	public static final String CODE = "code";
	public static final String RESPONSE_TYPE = "response_type";
	public static final String CLIENT_ID = "client_id";
	public static final String NONCE = "nonce";
	public static final String POST = "POST";
	public static final String GET = "GET";
	public static final String ID_TOKEN = "id_token";
	public static final String ACCESS_TOKEN = "access_token";
	public static final String REFRESH_TOKEN = "refresh_token";
	public static final String REDIRECT_URI = "redirect_uri";
	public static final String STATE = "state";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String ACCEPT = "Accept";
	public static final String AUTHORIZATION = "Authorization";
	public static final String POST_API_ACCESSTOKEN_HTTP = "/post/api/oauth/accesstoken HTTP/1.1";
	public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";
	public static final String BEARER = "Bearer ";
	public static final String BASIC = "Basic ";
	public static final String APPLICATION_VND_DIGIPOST_V2_JSON = "application/vnd.digipost-v2+json";
	public static final String CONTENT_OCTET_STREAM = "application/octet-stream";
	public static final String TEXT_HTML = "text/html";

    public static final String URL = "https://www.digipost.no";

    public static final String URL_API = URL + "/post/api";
	public static final String URL_RELATIONS_DOCUMENT_INBOX = URL + "/post/relations/document_inbox";
	public static final String URL_RELATIONS_DOCUMENT_ARCHIVE = URL + "/post/relations/document_archive";
	public static final String URL_RELATIONS_DOCUMENT_KITCHENBENCH = URL + "/post/relations/document_workarea";
	public static final String URL_RELATIONS_DOCUMENT_RECEIPTS = URL + "/post/relations/receipts";
	public static final String URL_RELATIONS_DOCUMENT_GET_CONTENT = URL + "/post/relations/get_document_content";

    public static final String URL_RELATIONS_DOCUMENT_SELF = URL + "/post/relations/self";

	public static final String URL_RELATIONS_DOCUMENT_UPDATE = URL + "/post/relations/update_document";
	public static final String URL_RELATIONS_DOCUMENT_DELETE = URL + "/post/relations/delete_document";
    public static final String URL_RELATIONS_DOCUMENT_SEND_OPENING_RECEIPT = URL + "/post/relations/send_opening_receipt";
    public static final String URL_RELATIONS_DOCUMENT_GET_ORGANIZATION_LOGO = URL + "/post/relations/organisation_logo";
	public static final String URL_API_OAUTH_AUTHORIZE_NEW = URL + "/post/api/oauth/authorize/new";
	public static final String URL_API_OAUTH_ACCESSTOKEN = URL + "/post/api/oauth/accesstoken";




}
