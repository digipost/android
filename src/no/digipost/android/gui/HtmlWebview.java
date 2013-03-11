package no.digipost.android.gui;

import no.digipost.android.R;
import no.digipost.android.api.ApiConstants;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageButton;

public class HtmlWebview extends Activity {

	private WebView webView;
	private ImageButton toMailbox;
	private ImageButton toArchive;
	private ImageButton toWorkarea;
	private ImageButton delete;
	private ImageButton share;
	private ImageButton digipostIcon;
	private ImageButton backButton;

	private String from;
	private String type;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_html_webview);

		String html = getIntent().getExtras().getString(ApiConstants.FILETYPE_HTML);
		String mime = "text/html";
		String encoding = "utf-8";

		from = getIntent().getExtras().getString("from");
		type = getIntent().getExtras().getString("type");

		System.out.println("I HTMLVIEW, HAR EXTRA MED: " + from);

		createButtons();

		webView = (WebView) findViewById(R.id.web_html);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setUseWideViewPort(true);
		webView.getSettings().setSupportZoom(true);
		webView.getSettings().setBuiltInZoomControls(true);
		webView.getSettings().setDisplayZoomControls(true);
		webView.getSettings().setLoadWithOverviewMode(true);
		webView.loadDataWithBaseURL(null, html, mime, encoding, null);
	}

	private void createButtons() {
		toMailbox = (ImageButton) findViewById(R.id.html_toMailbox);
		toArchive = (ImageButton) findViewById(R.id.html_toArchive);
		toWorkarea = (ImageButton) findViewById(R.id.html_toWorkarea);
		delete = (ImageButton) findViewById(R.id.html_delete);
		share = (ImageButton) findViewById(R.id.html_share);
		digipostIcon = (ImageButton) findViewById(R.id.html_digipost_icon);
		backButton = (ImageButton) findViewById(R.id.html_backbtn);

		if (type.equals("receipt")) {
			toArchive.setVisibility(View.GONE);
			toMailbox.setVisibility(View.GONE);
			toWorkarea.setVisibility(View.GONE);
		} else {
			if (from.equals("INBOX")) {
				toMailbox.setVisibility(View.GONE);
				System.out.println("Kommer fra mailbox, gjør den usynlig");
			} else if (from.equals(ApiConstants.LOCATION_ARCHIVE)) {
				toArchive.setVisibility(View.GONE);
			} else if (from.equals(ApiConstants.LOCATION_WORKAREA)) {
				toWorkarea.setVisibility(View.GONE);
			}
		}

		toMailbox.setOnClickListener(new HTMLViewListener());
		toArchive.setOnClickListener(new HTMLViewListener());
		toWorkarea.setOnClickListener(new HTMLViewListener());
		delete.setOnClickListener(new HTMLViewListener());
		share.setOnClickListener(new HTMLViewListener());
		digipostIcon.setOnClickListener(new HTMLViewListener());
		backButton.setOnClickListener(new HTMLViewListener());

	}

	private void singleLetterOperation(final String action) {
		Intent i = new Intent(HtmlWebview.this, BaseActivity.class);
		i.putExtra("newAction", action);
		setResult(BaseActivity.REQUESTCODE_HTMLVIEW, i);
		finish();
	}

	private class HTMLViewListener implements OnClickListener {

		public void onClick(final View v) {
			if (v.getId() == R.id.html_toArchive) {
				singleLetterOperation(ApiConstants.LOCATION_ARCHIVE);
			} else if (v.getId() == R.id.html_toWorkarea) {
				singleLetterOperation(ApiConstants.LOCATION_WORKAREA);
			} else if (v.getId() == R.id.html_digipost_icon) {
				finish();
			} else if (v.getId() == R.id.html_delete) {
				singleLetterOperation("delete");
			}
		}
	}
}
