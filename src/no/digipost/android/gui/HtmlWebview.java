package no.digipost.android.gui;

import no.digipost.android.R;
import no.digipost.android.api.ApiConstants;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.ImageButton;

public class HtmlWebview extends Activity {

	private WebView webView;
	private ImageButton toArchive;
	private ImageButton toWorkarea;
	private ImageButton delete;
	//private ImageButton share;
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

		from = getIntent().getExtras().getString(ApiConstants.LOCATION_FROM);
		type = getIntent().getExtras().getString(ApiConstants.DOCUMENT_TYPE);

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
		toArchive = (ImageButton) findViewById(R.id.html_toArchive);
		toWorkarea = (ImageButton) findViewById(R.id.html_toWorkarea);
		delete = (ImageButton) findViewById(R.id.html_delete);
		//share = (ImageButton) findViewById(R.id.html_share);
		digipostIcon = (ImageButton) findViewById(R.id.html_digipost_icon);
		backButton = (ImageButton) findViewById(R.id.html_backbtn);

		if (type.equals(ApiConstants.RECEIPT)) {
			toArchive.setVisibility(View.GONE);
			toWorkarea.setVisibility(View.GONE);
		} else {
			if (from.equals(ApiConstants.LOCATION_ARCHIVE)) {
				toArchive.setVisibility(View.GONE);
			} else if (from.equals(ApiConstants.LOCATION_WORKAREA)) {
				toWorkarea.setVisibility(View.GONE);
			}
		}

		toArchive.setOnClickListener(new HTMLViewListener());
		toWorkarea.setOnClickListener(new HTMLViewListener());
		delete.setOnClickListener(new HTMLViewListener());
		//share.setOnClickListener(new HTMLViewListener());
		digipostIcon.setOnClickListener(new HTMLViewListener());
		backButton.setOnClickListener(new HTMLViewListener());

	}

	private void singleLetterOperation(final String action) {
		Intent i = new Intent(HtmlWebview.this, BaseActivity.class);
		i.putExtra(ApiConstants.ACTION, action);
		i.putExtra(ApiConstants.DOCUMENT_TYPE, type);
		setResult(RESULT_OK, i);
		finish();
	}

	private void showWarning(final String text, final String action) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(text).setPositiveButton("Ja", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int id) {
				singleLetterOperation(action);
				dialog.dismiss();
			}
		}).setCancelable(false).setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private class HTMLViewListener implements OnClickListener {

		public void onClick(final View v) {
			if (v.getId() == R.id.html_toArchive) {
				showWarning(getString(R.string.dialog_prompt_toArchive), ApiConstants.LOCATION_ARCHIVE);
			} else if (v.getId() == R.id.html_toWorkarea) {
				showWarning(getString(R.string.dialog_prompt_toWorkarea), ApiConstants.LOCATION_WORKAREA);
			} else if (v.getId() == R.id.html_digipost_icon) {
				finish();
			} else if (v.getId() == R.id.html_delete) {
				showWarning(getString(R.string.dialog_prompt_delete), ApiConstants.DELETE);
			}
		}
	}
}
