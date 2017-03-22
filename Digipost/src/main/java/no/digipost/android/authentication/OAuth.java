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

import android.content.Context;
import android.util.Base64;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.spi.service.ServiceFinder;
import no.digipost.android.R;
import no.digipost.android.api.Buscador;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.api.exception.DigipostInvalidTokenException;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.model.Access;
import no.digipost.android.model.TokenValue;
import no.digipost.android.utilities.JSONUtilities;
import no.digipost.android.utilities.NetworkUtilities;
import org.apache.commons.lang.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.math.BigInteger;
import java.security.SecureRandom;

public class OAuth {

	private static String state = "";
    private static SecureRandom random = new SecureRandom();

	public static String getAuthorizeURL(String scope) {
		state = getSecureRandom();
		return ApiConstants.URL_API_OAUTH_AUTHORIZE_NEW + "?" + ApiConstants.RESPONSE_TYPE + "=" + ApiConstants.CODE + "&"
				+ ApiConstants.CLIENT_ID + "=" + Secret.CLIENT_ID
				+ "&" + ApiConstants.REDIRECT_URI + "=" + Secret.REDIRECT_URI
				+ "&" + ApiConstants.SCOPE + "=" + scope
				+ "&" + ApiConstants.STATE + "=" + state;
	}

	public static void retrieveMainAccess(final String state, final String code, final Context context, final String scope) throws DigipostApiException, DigipostClientException, DigipostAuthenticationException {
		String nonce = getSecureRandom();
        MultivaluedMap<String, String> params = new MultivaluedMapImpl();

        params.add(ApiConstants.GRANT_TYPE, ApiConstants.CODE);
		params.add(ApiConstants.CODE, code);
		params.add(ApiConstants.REDIRECT_URI, Secret.REDIRECT_URI);
		params.add(ApiConstants.NONCE, nonce);
		params.add(ApiConstants.SCOPE, scope);

        Access access = getAccessData(params, context);
		verifyState(state, context);
		verifyAuthentication(access.getId_token(), context);
		TokenStore.storeToken(context, access, scope);
	}

	public static void updateAccessTokenWithRefreshToken(final Context context) throws DigipostApiException,
			DigipostClientException, DigipostAuthenticationException {

		String refreshToken = TokenStore.getRefreshTokenFromSharedPreferences(context);
		if (StringUtils.isBlank(refreshToken)) {
			throw new DigipostAuthenticationException(context.getString(R.string.error_invalid_token));
		}

		MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		params.add(ApiConstants.GRANT_TYPE, ApiConstants.REFRESH_TOKEN);
		params.add(ApiConstants.REFRESH_TOKEN, refreshToken);
		Access access = getAccessData(params, context);
		TokenStore.updateToken(context, access.getAccess_token(), ApiConstants.SCOPE_FULL, access.getExpires_in());
	}

	private static Access getAccessData(final MultivaluedMap<String, String> params, final Context context) throws DigipostApiException,
			DigipostClientException, DigipostAuthenticationException {
        ServiceFinder.setIteratorProvider(new Buscador());
		Client c = Client.create();
		WebResource r = c.resource(ApiConstants.URL_API_OAUTH_ACCESSTOKEN);
		ClientResponse cr;
		try {
			cr = r
					.header(ApiConstants.AUTHORIZATION, getB64Auth())
                    .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
					.post(ClientResponse.class, params);
		} catch (Exception e) {
			throw new DigipostClientException(context.getString(R.string.error_your_network));
		}

		try {
			NetworkUtilities.checkHttpStatusCode(context, cr.getStatus());
		} catch (DigipostInvalidTokenException e) {
            throw new DigipostAuthenticationException(context.getString(R.string.error_invalid_token));
		}

		return (Access) JSONUtilities.processJackson(Access.class, cr.getEntityInputStream());
	}

	private static void verifyAuthentication(final String id_token, final Context context) throws DigipostApiException {
		String split_by = ".";
		int splitindex = id_token.indexOf(split_by);

		String signature_enc = id_token.substring(0, splitindex);
		String token_value_enc = id_token.substring(splitindex + split_by.length(), id_token.length());
		String signature_dec = new String(Base64.decode(signature_enc.getBytes(), Base64.DEFAULT));

		if (!encryptHmacSHA256(token_value_enc).equals(signature_dec)) {
			throw new DigipostApiException(context.getString(R.string.error_digipost_api));
		}

		TokenValue data = (TokenValue) JSONUtilities.processJackson(TokenValue.class,
				new String(Base64.decode(token_value_enc.getBytes(), Base64.DEFAULT)));
		String aud = data.getAud();

		if (!aud.equals(Secret.CLIENT_ID)) {
			throw new DigipostApiException(context.getString(R.string.error_digipost_api));
		}
	}

	private static void verifyState(final String received_state, final Context context) throws DigipostApiException {
		if (!state.equals(received_state)) {
			throw new DigipostApiException(context.getString(R.string.error_digipost_api));
		}
	}

	private static String getSecureRandom() {
		return new BigInteger(130, random).toString(32);
	}

	private static String encryptHmacSHA256(final String data) {
		SecretKeySpec secretKey = new SecretKeySpec(Secret.CLIENT_SECRET.getBytes(), ApplicationConstants.HMACSHA256);
		Mac mac = null;
		try {
			mac = Mac.getInstance(ApplicationConstants.HMACSHA256);
			mac.init(secretKey);
		} catch (Exception e) {
			// Ignore
		}

		byte[] hmacData = mac.doFinal(data.getBytes());

		return new String(hmacData);
	}

	private static String getB64Auth() {
		String source = Secret.CLIENT_ID + ":" + Secret.CLIENT_SECRET;
		return ApiConstants.BASIC + Base64.encodeToString(source.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);
	}
}