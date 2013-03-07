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

package no.digipost.android.gui;

import no.digipost.android.R;
import no.digipost.android.api.ApiConstants;
import no.digipost.android.api.ErrorHandling;
import no.digipost.android.authentication.OAuth2;

import org.apache.http.auth.AuthenticationException;

import android.accounts.NetworkErrorException;
import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebFragment extends DialogFragment {

	private WebView webViewOauth;
	private final Handler handler;
	private Context context;

	public WebFragment(final Handler handler) {
		this.handler = handler;
	}

	@Override
	public void onDismiss(final DialogInterface dialog) {
		super.onDismiss(dialog);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getActivity();
		setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo_Light);
	}

	private class MyWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(final WebView view, final String url) {

			String state_fragment = "&" + ApiConstants.STATE + "=";
			int state_start = url.indexOf(state_fragment);
			String code_fragment = "&" + ApiConstants.CODE + "=";
			int code_start = url.indexOf(code_fragment);

			if (code_start > -1) {
				String state = url.substring(state_start + state_fragment.length(), code_start);
				String code = url.substring(code_start + code_fragment.length(), url.length());

				new GetAccessTokenTask().execute(state, code);

				return true;
			}

			return false;
		}

		@Override
		public void onReceivedError(final WebView view, final int errorCode, final String description, final String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			handler.sendEmptyMessage(ErrorHandling.ERROR_DEVICE);
			dismiss();
		}
	}

	private class GetAccessTokenTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(final String... params) {
			try {
				OAuth2.retriveAccessTokenSuccess(params[0], params[1], context);
				handler.sendEmptyMessage(ErrorHandling.ERROR_OK);
			} catch (AuthenticationException e) {
				handler.sendEmptyMessage(ErrorHandling.ERROR_SERVER);
			} catch (NetworkErrorException e) {
				handler.sendEmptyMessage(ErrorHandling.ERROR_SERVER);
			} catch (IllegalStateException e) {
				handler.sendEmptyMessage(ErrorHandling.ERROR_SERVER);
			}

			return null;
		}

		@Override
		protected void onPostExecute(final Void result) {
			super.onPostExecute(result);
			dismiss();
		}

	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onViewCreated(final View v, final Bundle arg1) {
		super.onViewCreated(v, arg1);
		webViewOauth = (WebView) v.findViewById(R.id.web_oauth);
		getDialog().setTitle("Innlogging");
		String url = OAuth2.getAuthorizeURL();
		webViewOauth.loadUrl(url);
		WebSettings settings = webViewOauth.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		webViewOauth.setWebViewClient(new MyWebViewClient());

	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_web, container, false);
		return v;
	}
}