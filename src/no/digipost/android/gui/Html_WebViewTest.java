 package no.digipost.android.gui;

import no.digipost.android.R;
import no.digipost.android.api.ApiConstants;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.webkit.WebView;

public class Html_WebViewTest extends Activity {

	private WebView webView;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_html_webview);
		String html = getIntent().getExtras().getString(ApiConstants.FILETYPE_HTML);

		String mime = "text/html";
		String encoding = "utf-8";

		webView = (WebView) findViewById(R.id.web_html);
		webView.getSettings().setJavaScriptEnabled(true);

		//Teste dette for bedre skjermstorrelse
		//webView.getSettings().setLoadWithOverviewMode(true);
		//webView.getSettings().setUseWideViewPort(true);
		webView.loadDataWithBaseURL(null, html, mime, encoding, null);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_html_veb_view, menu);
		return true;
	}

}
