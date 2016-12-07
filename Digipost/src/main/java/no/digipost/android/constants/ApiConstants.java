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

import no.digipost.android.authentication.Secret;

public class ApiConstants {

    public static final String ERROR_CODE = "error-code";
    public static final String FOLDER_NOT_EMPTY = "FOLDER_NOT_EMPTY";
    public static final String FILETYPE_PDF = "pdf";
    public static final String FILETYPE_HTML = "html";
    public static final String[] FILETYPES_IMAGE = {"jpg", "jpeg", "png"};
    public static final String ENCODING = "UTF-8";
    public static final String MIME = "text/html";
    public static final String GET_RECEIPT = "receipt";
    public static final String GET_RECEIPT_SKIP = "skip";
    public static final String GET_DOCUMENT_LIMIT = "limit";
    public static final int GET_DOCUMENT_LIMIT_N = 101;
    public static final String GET_DOCUMENT_LASTSEEN = "lastseen";

    public static final String CREATE = "create";
    public static final String EDIT = "edit";
    public static final String MOVE = "move";
    public static final String DELETE = "delete";
    public static final String UPLOAD = "upload";
    public static final String REFRESH_ARCHIVE = "refreshArchive";
    public static final String LOGOUT = "logout";
    public static final String FRAGMENT_ACTIVITY_RESULT_ACTION = "action";
    public static final String FRAGMENT_ACTIVITY_RESULT_LOCATION = "location";
    public static final String FRAGMENT_ACTIVITY_RESULT_FOLDERID = "folderid";
    public static final String AUTHENTICATION_LEVEL_TWO_FACTOR = "TWO_FACTOR";
    public static final String AUTHENTICATION_LEVEL_IDPORTEN_3 = "IDPORTEN_3";
    public static final String AUTHENTICATION_LEVEL_IDPORTEN_4 = "IDPORTEN_4";
    public static final String INVOICE = "INVOICE";
    public static final String GRANT_TYPE = "grant_type";
    public static final String CODE = "code";
    public static final String RESPONSE_TYPE = "response_type";
    public static final String CLIENT_ID = "client_id";
    public static final String NONCE = "nonce";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String REDIRECT_URI = "redirect_uri";
    public static final String STATE = "state";
    public static final String SCOPE = "scope";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String ACCEPT = "Accept";
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer ";
    public static final String BASIC = "Basic ";
    public static final String APPLICATION_VND_DIGIPOST_V2_JSON = "application/vnd.digipost-v2+json";
    public static final String CONTENT_OCTET_STREAM = "application/octet-stream";
    public static final String TEXT_HTML = "text/html";

    public static final String SCOPE_FULL = "FULL";
    public static final String SCOPE_FULL_HIGH = "FULL_HIGHAUTH";
    public static final String SCOPE_IDPORTEN_3 = "FULL_IDPORTEN3";
    public static final String SCOPE_IDPORTEN_4 = "FULL_IDPORTEN4";

    public static final String URL_API = Secret.ENV_URL + "api";
    public static final String URL_RELATIONS = Secret.ENV_URL  + "relations/";
    public static final String URL_RELATIONS_DOCUMENT_INBOX = URL_RELATIONS + "document_inbox";
    public static final String URL_RELATIONS_DOCUMENT_ARCHIVE = URL_RELATIONS + "document_archive";
    public static final String URL_RELATIONS_DOCUMENT_WORKAREA = URL_RELATIONS + "document_workarea";
    public static final String URL_RELATIONS_DOCUMENT_RECEIPTS = URL_RELATIONS + "receipts";
    public static final String URL_RELATIONS_DOCUMENT_UPLOAD = URL_RELATIONS + "upload_document";
    public static final String URL_RELATIONS_DOCUMENT_UPLOAD_TO_INBOX = URL_RELATIONS + "upload_document_to_inbox";
    public static final String URL_RELATIONS_DOCUMENT_GET_CONTENT = URL_RELATIONS + "get_document_content";
    public static final String URL_RELATIONS_DOCUMENT_SELF = URL_RELATIONS + "self";
    public static final String URL_RELATIONS_DOCUMENT_UPDATE = URL_RELATIONS + "update_document";
    public static final String URL_RELATIONS_DOCUMENT_DELETE = URL_RELATIONS + "delete_document";
    public static final String URL_RELATIONS_DOCUMENT_SEND_OPENING_RECEIPT = URL_RELATIONS + "send_opening_receipt";
    public static final String URL_RELATIONS_ACCOUNT_SETTINGS = URL_RELATIONS + "account_settings";
    public static final String URL_RELATIONS_CURRENT_BANK_ACCOUNT = URL_RELATIONS + "current_bank_account";
    public static final String URL_RELATIONS_BANK_HOMEPAGE = URL_RELATIONS + "bank_homepage";
    public static final String URL_RELATIONS_SEND_TO_BANK = URL_RELATIONS + "send_to_bank";
    public static final String URL_RELATIONS_RECEIPT_HTML = URL_RELATIONS + "get_receipt_as_html";
    public static final String URL_RELATIONS_RECEIPT_DELETE = URL_RELATIONS + "delete_receipt";
    public static final String URL_RELATIONS_BANKS = URL_RELATIONS + "banks";

    public static final String URL_RELATIONS_CHANGE_FOLDER = URL_RELATIONS + "change_folder";
    public static final String URL_RELATIONS_DELETE_FOLDER = URL_RELATIONS + "delete_folder";
    public static final String URL_RELATIONS_CREATE_FOLDER = URL_RELATIONS + "create_folder";
    public static final String URL_RELATIONS_UPDATE_FOLDERS = URL_RELATIONS + "update_folders";
    public static final String URL_RELATIONS_PUSH_REGISTRATION = URL_API + "/private/gcm_token";

    public static final String URL_API_OAUTH_AUTHORIZE_NEW = Secret.ENV_URL + "api/oauth/authorize/new";
    public static final String URL_API_OAUTH_ACCESSTOKEN = Secret.ENV_URL + "api/oauth/accesstoken";
    public static final String URL_API_OAUTH_REVOKE = Secret.ENV_URL + "api/oauth/revoke";

    public static final String URL_HELP = "https://www.digipost.no/hjelp/#android";

}
