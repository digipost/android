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

import java.util.ArrayList;

import no.digipost.android.R;
import no.digipost.android.api.ApiConstants;
import no.digipost.android.api.DigipostApiException;
import no.digipost.android.api.DigipostClientException;
import no.digipost.android.api.LetterOperations;
import no.digipost.android.authentication.Secret;
import no.digipost.android.model.Letter;
import no.digipost.android.model.Receipt;
import no.digipost.android.pdf.PDFActivity;
import no.digipost.android.pdf.PdfStore;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class BaseActivity extends FragmentActivity {

	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ImageButton optionsButton;
	private ImageButton refreshButton;
	private ProgressBar refreshSpinner;
	private ButtonListener listener;
	private final int REQUEST_CODE = 1;
	private ViewPager mViewPager;
	private Context context;
	private ProgressDialog progressDialog;
	private NetworkConnection networkConnection;
	private boolean[] updatingView = new boolean[4];
	private int viewCounter = 0;
	private LetterOperations lo;
	private LetterListAdapter adapter_mailbox;
	private LetterListAdapter adapter_workarea;
	private LetterListAdapter adapter_archive;
	private ReceiptListAdapter adapter_receipts;
	private ListView lv_mailbox;
	private ListView lv_workarea;
	private ListView lv_archive;
	private ListView lv_receipts;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_base);
		context = this;
		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage(getString(R.string.loading_content));
		progressDialog.setCancelable(false);
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setOffscreenPageLimit(3);
		refreshSpinner = (ProgressBar) findViewById(R.id.base_refreshSpinner);
		optionsButton = (ImageButton) findViewById(R.id.base_optionsButton);
		refreshButton = (ImageButton) findViewById(R.id.base_refreshButton);
		listener = new ButtonListener();
		optionsButton.setOnClickListener(listener);
		refreshButton.setOnClickListener(listener);
		networkConnection = new NetworkConnection(this);
		mViewPager.setAdapter(mSectionsPagerAdapter);
	}

	private class ButtonListener implements OnClickListener {

		public void onClick(final View v) {
			if (v == optionsButton) {
				openOptionsMenu();
			} else if (v == refreshButton) {
				updateViews();
			}
		}
	}

	private void updateViews() {
		if (networkConnection.isNetworkAvailable()) {
			updatingView = new boolean[4];
			updatingView[LetterOperations.RECEIPTS] = true;
			loadMailbox();
			loadWorkbench();
			loadArchive();
			loadReceipts();
			toggleRefreshButton();
		} else {
			showMessage(getString(R.string.error_your_network));
		}
	}

	private void toggleRefreshButton() {
		boolean updating = false;
		for (boolean i : updatingView) {
			if (i) {
				updating = true;
				break;
			}
		}
		if (updating) {
			refreshButton.setVisibility(View.GONE);
			refreshSpinner.setVisibility(View.VISIBLE);
		} else {
			refreshSpinner.setVisibility(View.GONE);
			refreshButton.setVisibility(View.VISIBLE);
		}
	}

	public void loadMailbox() {
		if (networkConnection.isNetworkAvailable()) {
			new GetAccountMetaTask(LetterOperations.MAILBOX).execute(Secret.ACCESS_TOKEN);
		}
	}

	private void loadWorkbench() {
		if (networkConnection.isNetworkAvailable()) {
			new GetAccountMetaTask(LetterOperations.WORKAREA).execute(Secret.ACCESS_TOKEN);
		}
	}

	private void loadArchive() {
		if (networkConnection.isNetworkAvailable()) {
			new GetAccountMetaTask(LetterOperations.ARCHIVE).execute(Secret.ACCESS_TOKEN);
		}
	}

	private void loadReceipts() {
		if (networkConnection.isNetworkAvailable()) {
			new GetAccountReceiptMetaTask().execute(Secret.ACCESS_TOKEN);
		}
	}

	private void unsupportedActionDialog(final int resource) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(resource)
				.setCancelable(false)
				.setNeutralButton(getString(R.string.close), new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, final int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private class GetAccountReceiptMetaTask extends AsyncTask<String, Void, ArrayList<Receipt>> {
		private String errorMessage = "";

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			updatingView[LetterOperations.RECEIPTS] = true;
		}

		@Override
		protected ArrayList<Receipt> doInBackground(final String... params) {

			try {
				return lo.getAccountContentMetaReceipt(params[0]);
			} catch (DigipostApiException e) {
				errorMessage = e.getMessage();
				return null;
			} catch (DigipostClientException e) {
				errorMessage = e.getMessage();
				return null;
			}

		}

		@Override
		protected void onPostExecute(final ArrayList<Receipt> result) {
			super.onPostExecute(result);
			if (result == null) {
				showMessage(errorMessage);
			} else {
				adapter_receipts.updateList(result);
				progressDialog.dismiss();
				updatingView[LetterOperations.RECEIPTS] = false;
				toggleRefreshButton();
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			progressDialog.dismiss();
			updatingView = new boolean[4];
			toggleRefreshButton();
		}
	}

	private class GetAccountMetaTask extends AsyncTask<String, Void, ArrayList<Letter>> {
		private final int type;
		private String errorMessage = "";

		public GetAccountMetaTask(final int type) {
			this.type = type;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			updatingView[type] = true;
		}

		@Override
		protected ArrayList<Letter> doInBackground(final String... params) {

			try {
				return lo.getAccountContentMeta(params[0], type);
			} catch (DigipostApiException e) {
				errorMessage = e.getMessage();
				return null;
			} catch (DigipostClientException e) {
				errorMessage = e.getMessage();
				return null;
			}

		}

		@Override
		protected void onPostExecute(final ArrayList<Letter> result) {
			super.onPostExecute(result);
			if (result == null) {
				showMessage(errorMessage);
			} else {
				switch (type) {
				case LetterOperations.MAILBOX:
					adapter_mailbox.updateList(result);
					break;
				case LetterOperations.WORKAREA:
					adapter_workarea.updateList(result);
					break;
				case LetterOperations.ARCHIVE:
					adapter_archive.updateList(result);
					break;
				}
			}
			progressDialog.dismiss();
			updatingView[type] = false;
			toggleRefreshButton();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			progressDialog.dismiss();
			updatingView = new boolean[4];
			toggleRefreshButton();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.activity_base, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.basemenu_logoutOption:
			logOut();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void logOut() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		settings.edit().clear().commit();
		Intent i = new Intent(BaseActivity.this, LoginActivity.class);
		startActivity(i);
		finish();
	}

	public void showMessage(final String message) {
		Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
		toast.show();
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {
		public SectionsPagerAdapter(final FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(final int position) {
			DigipostSectionFragment fragment = new DigipostSectionFragment();
			Bundle args = new Bundle();
			args.putInt(DigipostSectionFragment.ARG_SECTION_NUMBER, position + 1);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return 4;
		}

		@Override
		public CharSequence getPageTitle(final int position) {
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase();
			case 1:
				return getString(R.string.title_section2).toUpperCase();
			case 2:
				return getString(R.string.title_section3).toUpperCase();
			case 3:
				return getString(R.string.title_section4).toUpperCase();
			}
			return null;
		}
	}

	public class DigipostSectionFragment extends Fragment {

		public static final String ARG_SECTION_NUMBER = "section_number";
		public static final int DIALOG_ID_NOT_AUTHENTICATED = 1;

		public DigipostSectionFragment() {
		}

		@Override
		public void onCreate(final Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			lo = new LetterOperations(getActivity().getApplicationContext());
			if ((++viewCounter) == 4) {
				updateViews();
				viewCounter = 0;
			}
		}

		@Override
		public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
			int number = getArguments().getInt(ARG_SECTION_NUMBER);
			if (number == 1) {
				View v = inflater.inflate(R.layout.fragment_layout_mailbox, container, false);

				lv_mailbox = (ListView) v.findViewById(R.id.listview_mailbox);
				View emptyView = v.findViewById(R.id.empty_listview_mailbox);
				lv_mailbox.setEmptyView(emptyView);
				adapter_mailbox = new LetterListAdapter(getActivity(), R.layout.mailbox_list_item, new ArrayList<Letter>());
				lv_mailbox.setAdapter(adapter_mailbox);
				lv_mailbox.setOnItemClickListener(new ListListener(adapter_mailbox));

				lv_mailbox.setOnItemLongClickListener(new OnItemLongClickListener() {

					public boolean onItemLongClick(final AdapterView<?> arg0, final View arg1, final int position, final long arg3) {
						LetterListAdapter.showboxes = true;
						adapter_mailbox.setInitialcheck(position);
						adapter_mailbox.notifyDataSetChanged();
						return true;
					}
				});
				lv_mailbox.setOnKeyListener(new OnKeyListener() {
					public boolean onKey(final View v, final int keyCode, final KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_BACK) {
							if (LetterListAdapter.showboxes == true) {
								LetterListAdapter.showboxes = false;
								adapter_mailbox.clearCheckboxes();
								adapter_mailbox.notifyDataSetChanged();
								return true;
							}
							return false;
						}
						return false;
					}
				});

				lv_mailbox.setOnItemClickListener(new ListListener(adapter_mailbox));

				return v;

			} else if (number == 2) {
				View v = inflater.inflate(R.layout.fragment_layout_workarea, container, false);
				lv_workarea = (ListView) v.findViewById(R.id.listview_kitchen);
				View emptyView = v.findViewById(R.id.empty_listview_workarea);
				lv_workarea.setEmptyView(emptyView);
				adapter_workarea = new LetterListAdapter(getActivity(), R.layout.mailbox_list_item, new ArrayList<Letter>());
				lv_workarea.setAdapter(adapter_workarea);
				lv_workarea.setOnItemClickListener(new ListListener(adapter_workarea));
				return v;
			} else if (number == 3) {
				View v = inflater.inflate(R.layout.fragment_layout_archive, container, false);
				lv_archive = (ListView) v.findViewById(R.id.listview_archive);
				View emptyView = v.findViewById(R.id.empty_listview_archive);
				lv_archive.setEmptyView(emptyView);
				adapter_archive = new LetterListAdapter(getActivity(), R.layout.mailbox_list_item, new ArrayList<Letter>());
				lv_archive.setAdapter(adapter_archive);
				lv_archive.setOnItemClickListener(new ListListener(adapter_archive));
				return v;
			} else {
				View v = inflater.inflate(R.layout.fragment_layout_receipts, container, false);
				lv_receipts = (ListView) v.findViewById(R.id.listview_receipts);
				View emptyView = v.findViewById(R.id.empty_listview_receipts);
				lv_receipts.setEmptyView(emptyView);
				adapter_receipts = new ReceiptListAdapter(getActivity(), R.layout.mailbox_list_item, new ArrayList<Receipt>());
				lv_receipts.setAdapter(adapter_receipts);
				lv_receipts.setOnItemClickListener(new ReceiptListListener(adapter_receipts));
				return v;
			}

		}

		private class MoveDocumentsTask extends AsyncTask<Object, Void, Boolean> {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.abort), new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, final int which) {
						dialog.dismiss();
						cancel(true);
					}
				});
				progressDialog.show();

			}

			@Override
			protected Boolean doInBackground(final Object... params) {
				boolean moved = false;
				try {
					moved = lo.moveDocument((String) params[0], (Letter) params[1]);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}

				return moved;

			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
				progressDialog.dismiss();
				updatingView = new boolean[4];
				toggleRefreshButton();
			}

			@Override
			protected void onPostExecute(final Boolean result) {
				super.onPostExecute(result);
				progressDialog.dismiss();
				updatingView = new boolean[4];
				toggleRefreshButton();
			}
		}

		private class GetPDFTask extends AsyncTask<Object, Void, byte[]> {
			private String errorMessage = "";

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.abort), new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, final int which) {
						dialog.dismiss();
						cancel(true);
					}
				});
				progressDialog.show();
			}

			@Override
			protected byte[] doInBackground(final Object... params) {

				try {
					return lo.getDocumentContentPDF((String) params[0], (Letter) params[1]);
				} catch (DigipostApiException e) {
					errorMessage = e.getMessage();
					return null;
				} catch (DigipostClientException e) {
					errorMessage = e.getMessage();
					return null;
				}

			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
				progressDialog.dismiss();
				updatingView = new boolean[4];
				toggleRefreshButton();
			}

			@Override
			protected void onPostExecute(final byte[] result) {
				super.onPostExecute(result);

				if (result == null) {
					showMessage(errorMessage);
				} else {
					PdfStore.pdf = result;
					Intent i = new Intent(getActivity().getApplicationContext(), PDFActivity.class);
					i.putExtra(PDFActivity.INTENT_FROM, PDFActivity.FROM_MAILBOX);
					startActivity(i);
				}

				progressDialog.dismiss();
				updatingView = new boolean[4];
				toggleRefreshButton();
			}
		}

		private class GetHTMLTask extends AsyncTask<Object, Void, String> {
			private String errorMessage = "";

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.abort), new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, final int which) {
						dialog.dismiss();
						cancel(true);
					}
				});
				progressDialog.show();
			}

			@Override
			protected String doInBackground(final Object... params) {

				String html = null;

				if (params[1].equals(ApiConstants.GET_RECEIPT)) {

					try {
						html = lo.getReceiptContentHTML((String) params[0], (Receipt) params[2]);
						return html;
					} catch (DigipostApiException e) {
						errorMessage = e.getMessage();
						return null;
					} catch (DigipostClientException e) {
						errorMessage = e.getMessage();
						return null;
					}

				} else {

					try {
						html = lo.getDocumentContentHTML((String) params[0], (Letter) params[2]);
						return html;
					} catch (DigipostApiException e) {
						errorMessage = e.getMessage();
						return null;
					} catch (DigipostClientException e) {
						errorMessage = e.getMessage();
						return null;
					}

				}

			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
				progressDialog.dismiss();
				updatingView = new boolean[4];
				toggleRefreshButton();
			}

			@Override
			protected void onPostExecute(final String result) {
				super.onPostExecute(result);

				if (result == null) {
					showMessage(errorMessage);
				} else {
					Intent i = new Intent(getActivity(), HtmlWebview.class);
					i.putExtra(ApiConstants.FILETYPE_HTML, result);
					startActivity(i);
				}

				progressDialog.dismiss();
				updatingView = new boolean[4];
				toggleRefreshButton();
			}
		}

		private class ListListener implements OnItemClickListener {
			LetterListAdapter adapter;

			public ListListener(final LetterListAdapter adapter) {
				this.adapter = adapter;
			}

			public void onItemClick(final AdapterView<?> arg0, final View arg1, final int position, final long arg3) {
				Letter mletter = adapter.getItem(position);
				if (mletter.getAuthenticationLevel().equals(ApiConstants.AUTHENTICATION_LEVEL_TWO_FACTOR)) {
					unsupportedActionDialog(R.string.dialog_error_two_factor);
					return;
				}
				String filetype = mletter.getFileType();

				if (networkConnection.isNetworkAvailable()) {
					if (filetype.equals(ApiConstants.FILETYPE_PDF)) {
						GetPDFTask pdfTask = new GetPDFTask();
						pdfTask.execute(Secret.ACCESS_TOKEN, mletter);
					} else if (filetype.equals(ApiConstants.FILETYPE_HTML)) {
						GetHTMLTask htmlTask = new GetHTMLTask();
						htmlTask.execute(Secret.ACCESS_TOKEN, ApiConstants.GET_DOCUMENT, mletter);
					} else {
						unsupportedActionDialog(R.string.dialog_error_not_supported_filetype);
						return;
					}
				} else {
					showMessage(getString(R.string.error_your_network));
				}
			}
		}

		private class ReceiptListListener implements OnItemClickListener {
			ReceiptListAdapter adapter;

			public ReceiptListListener(final ReceiptListAdapter adapter) {
				this.adapter = adapter;
			}

			public void onItemClick(final AdapterView<?> arg0, final View arg1, final int position, final long arg3) {
				Receipt mReceipt = adapter.getItem(position);

				GetHTMLTask htmlTask = new GetHTMLTask();
				htmlTask.execute(Secret.ACCESS_TOKEN, ApiConstants.GET_RECEIPT, mReceipt);
			}
		}
	}
}
