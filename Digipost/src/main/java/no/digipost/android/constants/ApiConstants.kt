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

package no.digipost.android.constants

import no.digipost.android.authentication.Secret

object ApiConstants {

    const val ERROR_CODE = "error-code"
    const val FOLDER_NOT_EMPTY = "FOLDER_NOT_EMPTY"
    const val FILETYPE_PDF = "pdf"
    const val FILETYPE_HTML = "html"
    val FILETYPES_IMAGE = arrayOf("jpg", "jpeg", "png")
    const val ENCODING = "UTF-8"
    const val MIME = "text/html"
    const val GET_RECEIPT = "receipt"
    const val GET_RECEIPT_SKIP = "skip"
    const val GET_DOCUMENT_LIMIT = "limit"
    const val GET_DOCUMENT_LIMIT_N = 50
    const val GET_DOCUMENT_LASTSEEN = "lastseen"

    const val CREATE = "create"
    const val EDIT = "edit"
    const val MOVE = "move"
    const val DELETE = "delete"
    const val UPLOAD = "upload"
    const val REFRESH_ARCHIVE = "refreshArchive"
    const val LOGOUT = "logout"
    const val FRAGMENT_ACTIVITY_RESULT_ACTION = "action"
    const val FRAGMENT_ACTIVITY_RESULT_LOCATION = "location"
    const val FRAGMENT_ACTIVITY_RESULT_FOLDERID = "folderid"
    const val AUTHENTICATION_LEVEL_TWO_FACTOR = "TWO_FACTOR"
    const val AUTHENTICATION_LEVEL_IDPORTEN_3 = "IDPORTEN_3"
    const val AUTHENTICATION_LEVEL_IDPORTEN_4 = "IDPORTEN_4"
    const val INVOICE = "INVOICE"
    const val GRANT_TYPE = "grant_type"
    const val CODE = "code"
    const val RESPONSE_TYPE = "response_type"
    const val CLIENT_ID = "client_id"
    const val NONCE = "nonce"
    const val REFRESH_TOKEN = "refresh_token"
    const val REDIRECT_URI = "redirect_uri"
    const val STATE = "state"
    const val SCOPE = "scope"
    const val CONTENT_TYPE = "Content-Type"
    const val ACCEPT = "Accept"
    const val AUTHORIZATION = "Authorization"
    const val BEARER = "Bearer "
    const val BASIC = "Basic "
    const val APPLICATION_VND_DIGIPOST_V2_JSON = "application/vnd.digipost-v2+json"
    const val CONTENT_OCTET_STREAM = "application/octet-stream"
    const val TEXT_HTML = "text/html"

    const val SCOPE_FULL = "FULL"
    const val SCOPE_FULL_HIGH = "FULL_HIGHAUTH"
    const val SCOPE_IDPORTEN_3 = "FULL_IDPORTEN3"
    const val SCOPE_IDPORTEN_4 = "FULL_IDPORTEN4"

    const val URL_API = "${Secret.ENV_URL}api"
    const val URL_RELATIONS = "${Secret.ENV_URL}relations/"
    const val URL_RELATIONS_DOCUMENT_INBOX = "${URL_RELATIONS}document_inbox"
    const val URL_RELATIONS_DOCUMENT_RECEIPTS = "${URL_RELATIONS}receipts"
    const val URL_RELATIONS_DOCUMENT_UPLOAD = "${URL_RELATIONS}upload_document"
    const val URL_RELATIONS_DOCUMENT_UPLOAD_TO_INBOX = "${URL_RELATIONS}upload_document_to_inbox"
    const val URL_RELATIONS_DOCUMENT_GET_CONTENT = "${URL_RELATIONS}get_document_content"
    const val URL_RELATIONS_DOCUMENT_SELF = "${URL_RELATIONS}self"
    const val URL_RELATIONS_DOCUMENT_UPDATE = "${URL_RELATIONS}update_document"
    const val URL_RELATIONS_DOCUMENT_DELETE = "${URL_RELATIONS}delete_document"
    const val URL_RELATIONS_DOCUMENT_SEND_OPENING_RECEIPT = "${URL_RELATIONS}send_opening_receipt"
    const val URL_RELATIONS_ACCOUNT_SETTINGS = "${URL_RELATIONS}account_settings"
    const val URL_RELATIONS_MAILBOX_SETTINGS = "${URL_RELATIONS}mailbox_settings"
    const val URL_RELATIONS_CURRENT_BANK_ACCOUNT = "${URL_RELATIONS}current_bank_account"
    const val URL_RELATIONS_BANK_HOMEPAGE = "${URL_RELATIONS}bank_homepage"
    const val URL_RELATIONS_SEND_TO_BANK = "${URL_RELATIONS}send_to_bank"
    const val URL_RELATIONS_BANKS = "${URL_RELATIONS}banks"

    const val URL_RELATIONS_CHANGE_FOLDER = "${URL_RELATIONS}change_folder"
    const val URL_RELATIONS_DELETE_FOLDER = "${URL_RELATIONS}delete_folder"
    const val URL_RELATIONS_CREATE_FOLDER = "${URL_RELATIONS}create_folder"
    const val URL_RELATIONS_UPDATE_FOLDERS = "${URL_RELATIONS}update_folders"
    const val URL_RELATIONS_PUSH_REGISTRATION = "$URL_API/private/gcm_token"

    const val URL_API_TERMINATE_BANK_AGREEMENT = "${URL_RELATIONS}terminate_bank_agreement"

    const val URL_API_OAUTH_AUTHORIZE_NEW = "${Secret.ENV_URL}api/oauth/authorize/new"
    const val URL_API_OAUTH_ACCESSTOKEN = "${Secret.ENV_URL}api/oauth/accesstoken"
    const val URL_API_OAUTH_REVOKE = "${Secret.ENV_URL}api/oauth/revoke"

    const val URL_HELP = "https://www.digipost.no/hjelp/#android"
    const val UPDATE_MAILBOX_SETTINGS = "${URL_RELATIONS}update_mailbox_settings"
    const val URL_PRIVACY = "https://www.digipost.no/juridisk/#personvern"
    const val URL_REGISTRATION = "https://www.digipost.no/app/registrering?utm_source=android_app&utm_medium=app&utm_campaign=app-link&utm_content=ny_bruker#/"

}
