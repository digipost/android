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

package no.digipost.android.authentication;

import java.io.InputStream;
import java.math.BigInteger;
import java.security.SecureRandom;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.MultivaluedMap;

import no.digipost.android.api.ApiConstants;
import no.digipost.android.api.JSONConverter;

import org.json.JSONObject;

import android.util.Base64;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class OAuth2 {

	private static String state = "";
	private static String nonce = "";

	private static SecureRandom random = new SecureRandom();

	public static String getAuthorizeURL() {
		state = generateSecureRandom(20);
		return ApiConstants.URL_API_OAUTH_AUTHORIZE_NEW + "?" + ApiConstants.RESPONSE_TYPE + "=" + ApiConstants.CODE + "&"
				+ ApiConstants.CLIENT_ID + "=" + Secret.CLIENT_ID + "&" + ApiConstants.REDIRECT_URI + "=" + Secret.REDIRECT_URI + "&"
				+ ApiConstants.STATE + "=" + state;
	}

	public static JSONObject getInitialAccessTokenData(final String url_state, final String url_code) throws Exception {
		nonce = generateSecureRandom(20);

		MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		params.add(ApiConstants.GRANT_TYPE, ApiConstants.CODE);
		params.add(ApiConstants.CODE, url_code);
		params.add(ApiConstants.REDIRECT_URI, Secret.REDIRECT_URI);
		params.add(ApiConstants.NONCE, nonce);

		JSONObject data = JSONConverter.getJSONObjectFromInputStream(getAccessData(params));

		String id_token = data.getString(ApiConstants.ID_TOKEN);

		if (!state.equals(url_state)) {
			throw new Exception("State verification failed.");
		} else if (!verifyAuth(id_token, Secret.CLIENT_SECRET)) {
			throw new Exception("Signature verification failed.");
		}

		return data;
	}

	public static JSONObject getRefreshAccessToken(final String refresh_token) throws Exception {

		MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		params.add(ApiConstants.GRANT_TYPE, ApiConstants.REFRESH_TOKEN);
		params.add(ApiConstants.REFRESH_TOKEN, refresh_token);
		JSONObject data = JSONConverter.getJSONObjectFromInputStream(getAccessData(params));

		/*
		 * String id_token = data.getString(ApiConstants.ID_TOKEN);
		 * 
		 * if (!verifyAuth(id_token, Secret.CLIENT_SECRET)) { throw new
		 * Exception("Signature verification failed."); }
		 */
		return data;
	}

	public static InputStream getAccessData(final MultivaluedMap<String, String> params) {
		Client c = Client.create();
		WebResource r = c.resource(ApiConstants.URL_API_OAUTH_ACCESSTOKEN);

		ClientResponse response = r
				.queryParams(params)
				.header(ApiConstants.POST, ApiConstants.POST_API_ACCESSTOKEN_HTTP)
				.header(ApiConstants.CONTENT_TYPE, ApiConstants.APPLICATION_FORM_URLENCODED)
				.header(ApiConstants.AUTHORIZATION, getB64Auth(Secret.CLIENT_ID, Secret.CLIENT_SECRET))
				.post(ClientResponse.class);

		return response.getEntityInputStream();
	}

	public static boolean verifyAuth(final String id_token, final String client_secret) throws Exception {
		String split_by = ".";
		int splitindex = id_token.indexOf(split_by);

		String signature_enc = id_token.substring(0, splitindex);
		String token_value_enc = id_token.substring(splitindex + split_by.length(), id_token.length());
		String signature_dec = new String(Base64.decode(signature_enc.getBytes(), Base64.DEFAULT));

		if (!encryptHmacSHA256(token_value_enc, Secret.CLIENT_SECRET).equals(signature_dec)) {
			return false;
		}

		JSONObject data = new JSONObject(new String(Base64.decode(token_value_enc.getBytes(), Base64.DEFAULT)));
		String aud = data.getString(ApiConstants.AUD);

		if (!aud.equals(Secret.CLIENT_ID)) {
			return false;
		}
		return true;
	}

	public static boolean verifyState(final String received_state) {
		return state.equals(received_state);
	}

	private static String generateSecureRandom(final int num_bytes) {
		return new BigInteger(130, random).toString(32);
	}

	public static String encryptHmacSHA256(final String data, final String key) throws Exception {
		SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), ApiConstants.HMACSHA256);
		Mac mac = Mac.getInstance(ApiConstants.HMACSHA256);
		mac.init(secretKey);
		byte[] hmacData = mac.doFinal(data.getBytes());

		return new String(hmacData);
	}

	public static String getB64Auth(final String id, final String secret) {
		String source = id + ":" + secret;

		return ApiConstants.BASIC + Base64.encodeToString(source.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);
	}
}
