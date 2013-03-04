package no.digipost.android.gui;

import no.digipost.android.R;
import no.digipost.android.api.ApiConstants;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageButton;

public class Html_WebViewTest extends Activity {

	private WebView webView;
	private ImageButton toMailbox;
	private ImageButton toArchive;
	private ImageButton toWorkarea;
	private ImageButton delete;
	private ImageButton share;
	private ImageButton digipostIcon;
	private ImageButton backButton;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_html_webview);

		createButtons();

		String html = getIntent().getExtras().getString(ApiConstants.FILETYPE_HTML);

		String mime = "text/html";
		String encoding = "utf-8";

		webView = (WebView) findViewById(R.id.web_html);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setUseWideViewPort(true);
		webView.getSettings().setSupportZoom(true);
		webView.getSettings().setBuiltInZoomControls(true);
		webView.getSettings().setDisplayZoomControls(true);
		webView.getSettings().setLoadWithOverviewMode(true);
		webView.loadDataWithBaseURL(null, html, mime, encoding, null);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_html_veb_view, menu);
		return true;
	}

	private void createButtons() {
		toMailbox = (ImageButton) findViewById(R.id.html_toMailbox);
		toArchive = (ImageButton) findViewById(R.id.html_toArchive);
		toWorkarea = (ImageButton) findViewById(R.id.html_toWorkarea);
		delete = (ImageButton) findViewById(R.id.html_delete);
		share = (ImageButton) findViewById(R.id.html_share);
		digipostIcon = (ImageButton) findViewById(R.id.html_digipost_icon);
		backButton = (ImageButton) findViewById(R.id.html_backbtn);

		toMailbox.setOnClickListener(new HTMLViewListener());
		toArchive.setOnClickListener(new HTMLViewListener());
		toWorkarea.setOnClickListener(new HTMLViewListener());
		delete.setOnClickListener(new HTMLViewListener());
		share.setOnClickListener(new HTMLViewListener());
		digipostIcon.setOnClickListener(new HTMLViewListener());
		backButton.setOnClickListener(new HTMLViewListener());

		//webView.setOnClickListener(new HTMLViewListener());
	}

	private void enableFullScreenOnSingleTap() {

	}

	private class HTMLViewListener implements OnClickListener {

		public void onClick(final View v) {
			// TODO Auto-generated method stub
			if (v.getId() == R.id.web_html) {
				enableFullScreenOnSingleTap();
			}

		}
	}

}
