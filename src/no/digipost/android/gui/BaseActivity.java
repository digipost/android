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
	private static final String CURRENT_PAGE = "currentPage";

	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ImageButton optionsButton;
	private ImageButton refreshButton;
	private ImageButton logoButton;
	private ProgressBar refreshSpinner;
	private ButtonListener listener;
	private ButtonListener buttonListener;
	private ViewPagerListener pageListener;
	private final int REQUEST_CODE = 1;
	private ViewPager mViewPager;
	private Context context;
	public static final int REQUESTCODE_HTMLVIEW = 1;
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
		logoButton = (ImageButton) findViewById(R.id.base_logoButton);
		buttonListener = new ButtonListener();
		pageListener = new ViewPagerListener();
		optionsButton.setOnClickListener(buttonListener);
		refreshButton.setOnClickListener(buttonListener);
		logoButton.setOnClickListener(buttonListener);
		networkConnection = new NetworkConnection(this);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOnPageChangeListener(pageListener);
		mViewPager.setClickable(true);
	}

	private class ButtonListener implements OnClickListener {

		public void onClick(final View v) {
			if (v == optionsButton) {
				openOptionsMenu();
			} else if (v == refreshButton) {
				updateViews();
			} else if (v == logoButton) {
				scrollToTheTop();
			}
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

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(CURRENT_PAGE, pageListener.getCurrentPage());
		System.out.println("save");
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		int currentPage = savedInstanceState.getInt(CURRENT_PAGE);
		mViewPager.setCurrentItem(currentPage);
		System.out.println("load");
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

	public void changeView(final int pos) {
		mViewPager.setCurrentItem(pos);
	}

	public class ViewPagerListener extends ViewPager.SimpleOnPageChangeListener {

		private int currentPage;

		@Override
		public void onPageSelected(final int position) {
			currentPage = position;
		}

		public int getCurrentPage() {
			return currentPage;
		}

		public void setCurrentPage(final int currentPage) {
			this.currentPage = currentPage;
		}
	}

	private void scrollToTheTop() {
		int page = pageListener.getCurrentPage();
		System.out.println("page" + page);
		switch (page) {
		case LetterOperations.MAILBOX:
			lv_mailbox.setSelection(0);
			break;
		case LetterOperations.WORKAREA:
			lv_workarea.setSelection(0);
			break;
		case LetterOperations.ARCHIVE:
			lv_archive.setSelection(0);
			break;
		case LetterOperations.RECEIPTS:
			lv_receipts.setSelection(0);
			break;
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
			new GetAccountMetaTask(LetterOperations.MAILBOX).execute();
		}
	}

	private void loadWorkbench() {
		if (networkConnection.isNetworkAvailable()) {
			new GetAccountMetaTask(LetterOperations.WORKAREA).execute();
		}
	}

	private void loadArchive() {
		if (networkConnection.isNetworkAvailable()) {
			new GetAccountMetaTask(LetterOperations.ARCHIVE).execute();
		}
	}

	private void loadReceipts() {
		if (networkConnection.isNetworkAvailable()) {
			new GetAccountReceiptMetaTask().execute();
		}
	}

	private class GetAccountReceiptMetaTask extends AsyncTask<Void, Void, ArrayList<Receipt>> {
		private String errorMessage = "";

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			updatingView[LetterOperations.RECEIPTS] = true;
		}

		@Override
		protected ArrayList<Receipt> doInBackground(final Void... params) {

			try {
				return lo.getAccountContentMetaReceipt();
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
			}
			progressDialog.dismiss();
			updatingView[LetterOperations.RECEIPTS] = false;
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

	private class GetAccountMetaTask extends AsyncTask<Void, Void, ArrayList<Letter>> {
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
		protected ArrayList<Letter> doInBackground(final Void... params) {

			try {
				return lo.getAccountContentMeta(type);
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
	protected void onActivityResult(final int arg0, final int arg1, final Intent arg2) {
		super.onActivityResult(arg0, arg1, arg2);
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
		private Letter tempLetter;

		private ImageButton mailbox_multiSelection_moveToWorkarea;
		private ImageButton mailbox_multiSelection_moveToArchive;
		private ImageButton mailbox_multiSelection_share;
		private ImageButton mailbox_multiSelection_delete;

		private View v1;
		private View v2;
		private View v3;
		private View v4;

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

				v1 = inflater.inflate(R.layout.fragment_layout_mailbox, container, false);

				lv_mailbox = (ListView) v1.findViewById(R.id.listview_mailbox);
				View emptyView = v1.findViewById(R.id.empty_listview_mailbox);
				lv_mailbox.setEmptyView(emptyView);
				adapter_mailbox = new LetterListAdapter(getActivity(), R.layout.mailbox_list_item, new ArrayList<Letter>());
				lv_mailbox.setAdapter(adapter_mailbox);
				lv_mailbox.setOnItemClickListener(new ListListener(adapter_mailbox));

				mailbox_multiSelection_moveToArchive = (ImageButton) v1.findViewById(R.id.mailbox_toArchive);
				mailbox_multiSelection_moveToArchive.setOnClickListener(new MultiSelectionListener(adapter_mailbox));

				lv_mailbox.setOnItemLongClickListener(new OnItemLongClickListener() {
					public boolean onItemLongClick(final AdapterView<?> arg0, final View arg1, final int position, final long arg3) {
						checkboxesOnOff(v1, true, position);
						return true;
					}
				});
				lv_mailbox.setOnKeyListener(new OnKeyListener() {
					public boolean onKey(final View view, final int keyCode, final KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_BACK) {
							if (LetterListAdapter.showboxes == true) {
								checkboxesOnOff(v1, false, -1);
								return true;
							}
							return false;
						}
						return false;
					}
				});

				lv_mailbox.setOnItemClickListener(new ListListener(adapter_mailbox));

				return v1;

			} else if (number == 2) {
				View v2 = inflater.inflate(R.layout.fragment_layout_workarea, container, false);
				lv_workarea = (ListView) v2.findViewById(R.id.listview_kitchen);
				View emptyView = v2.findViewById(R.id.empty_listview_workarea);
				lv_workarea.setEmptyView(emptyView);
				adapter_workarea = new LetterListAdapter(getActivity(), R.layout.mailbox_list_item, new ArrayList<Letter>());
				lv_workarea.setAdapter(adapter_workarea);
				lv_workarea.setOnItemClickListener(new ListListener(adapter_workarea));
				return v2;
			} else if (number == 3) {
				View v3 = inflater.inflate(R.layout.fragment_layout_archive, container, false);
				lv_archive = (ListView) v3.findViewById(R.id.listview_archive);
				View emptyView = v3.findViewById(R.id.empty_listview_archive);
				lv_archive.setEmptyView(emptyView);
				adapter_archive = new LetterListAdapter(getActivity(), R.layout.mailbox_list_item, new ArrayList<Letter>());
				lv_archive.setAdapter(adapter_archive);
				lv_archive.setOnItemClickListener(new ListListener(adapter_archive));
				return v3;
			} else {
				View v4 = inflater.inflate(R.layout.fragment_layout_receipts, container, false);
				lv_receipts = (ListView) v4.findViewById(R.id.listview_receipts);
				View emptyView = v4.findViewById(R.id.empty_listview_receipts);
				lv_receipts.setEmptyView(emptyView);
				adapter_receipts = new ReceiptListAdapter(getActivity(), R.layout.mailbox_list_item, new ArrayList<Receipt>());
				lv_receipts.setAdapter(adapter_receipts);
				lv_receipts.setOnItemClickListener(new ReceiptListListener(adapter_receipts));
				return v4;
			}
		}

		private void checkboxesOnOff(final View v, final boolean state, final int position) {
			if (state) {
				LetterListAdapter.showboxes = true;
				lv_mailbox.requestFocus();
				adapter_mailbox.setInitialcheck(position);
				adapter_mailbox.notifyDataSetChanged();
				v.findViewById(R.id.mailbox_bottombar).setVisibility(View.VISIBLE);
			} else {
				LetterListAdapter.showboxes = false;
				adapter_mailbox.clearCheckboxes();
				adapter_mailbox.notifyDataSetChanged();
				v.findViewById(R.id.mailbox_bottombar).setVisibility(View.GONE);
			}
		}

		private void unsupportedActionDialog(final String text) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

			builder.setTitle(R.string.dialog_error_header)
					.setMessage(text)
					.setCancelable(false)
					.setNeutralButton(getString(R.string.close), new DialogInterface.OnClickListener() {
						public void onClick(final DialogInterface dialog, final int id) {
							dialog.cancel();
						}
					});

			AlertDialog alert = builder.create();
			alert.show();
		}

		private class GetHTMLTask extends AsyncTask<Object, Void, String> {
			private String errorMessage = "";
			Letter letter;

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

				if (params[0].equals(ApiConstants.GET_RECEIPT)) {

					try {
						html = lo.getReceiptContentHTML((Receipt) params[1]);
						return html;
					} catch (DigipostApiException e) {
						errorMessage = e.getMessage();
						return null;
					} catch (DigipostClientException e) {
						errorMessage = e.getMessage();
						return null;
					}

				} else {
					letter = (Letter) params[1];
					try {
						html = lo.getDocumentContentHTML((Letter) params[1]);
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
					String type = letter != null ? ApiConstants.LETTER : ApiConstants.RECEIPT;
					i.putExtra(ApiConstants.DOCUMENT_TYPE, type);
					i.putExtra(ApiConstants.FILETYPE_HTML, result);
					if (type.equals(ApiConstants.LETTER)) {
						i.putExtra(ApiConstants.LOCATION_FROM, letter.getLocation());
						tempLetter = letter;
					}
					startActivityForResult(i, REQUESTCODE_HTMLVIEW);
				}

				progressDialog.dismiss();
				updatingView = new boolean[4];
				toggleRefreshButton();
			}
		}

		private class GetPDFTask extends AsyncTask<Letter, Void, byte[]> {
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
			protected byte[] doInBackground(final Letter... params) {

				try {
					return lo.getDocumentContentPDF(params[0]);
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

		private class MoveDocumentsTask extends AsyncTask<Object, Void, Boolean> {
			Letter letter;
			String toLocation;
			String fromLocation;
			String errorMessage;

			public MoveDocumentsTask(final String toLocation) {
				this.toLocation = toLocation;
			}

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
				letter = (Letter) params[1];
				fromLocation = letter.getLocation();
				boolean moved = false;
				try {
					moved = lo.moveDocument((String) params[0], (Letter) params[1], toLocation);
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

				if (fromLocation.equals(ApiConstants.LOCATION_ARCHIVE)) {
					adapter_archive.remove(letter);
					adapter_workarea.add(letter);
					adapter_archive.notifyDataSetChanged();
					adapter_workarea.notifyDataSetChanged();
				} else if (fromLocation.equals(ApiConstants.LOCATION_WORKAREA)) {
					adapter_workarea.remove(letter);
					adapter_archive.add(letter);
					adapter_workarea.notifyDataSetChanged();
					adapter_archive.notifyDataSetChanged();
				} else if (fromLocation.equals(ApiConstants.LOCATION_INBOX)) {
					adapter_mailbox.remove(letter);
					adapter_mailbox.notifyDataSetChanged();
					if (toLocation.equals(ApiConstants.LOCATION_WORKAREA)) {
						adapter_workarea.add(letter);
						adapter_workarea.notifyDataSetChanged();
					} else {
						adapter_archive.add(letter);
						adapter_archive.notifyDataSetChanged();
					}
				}
				progressDialog.dismiss();
				updatingView = new boolean[4];
				toggleRefreshButton();
			}
		}

		private class MultipleDocumentsTask extends AsyncTask<LetterListAdapter, Integer, Boolean> {

			Letter letter;
			String action;
			boolean[] checked;
			int counter = 0;
			String errorMessage;

			public MultipleDocumentsTask(final String location, final boolean[] checked) {
				action = location;
				this.checked = checked;
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();

				progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.abort), new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, final int which) {
						dialog.dismiss();
						cancel(true);
					}
				});
				progressDialog.setMessage(0 + " av " + checked.length + " flyttet");
				progressDialog.show();
			}

			@Override
			protected Boolean doInBackground(final LetterListAdapter... params) {
				boolean moved = false;

				for (int i = 0; i < checked.length; i++) {
					try {
						if (checked[i]) {
							letter = params[0].getItem(i);
							letter.setLocation(action);
							moved = lo.moveDocument(Secret.ACCESS_TOKEN, letter, action);

							publishProgress(++counter);
						} else {
						}
					} catch (Exception e) {
						e.printStackTrace();
						return null;
					}

				}

				return moved;
			}

			@Override
			protected void onProgressUpdate(final Integer... values) {
				super.onProgressUpdate(values);
				progressDialog.setMessage((values[0] + 1) + " av " + checked.length + " flyttet");

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

				Letter letter;
				for (int i = 0; i < checked.length; i++) {
					if (checked[i]) {
						letter = adapter_mailbox.getItem(i);
						adapter_mailbox.remove(letter);
						adapter_mailbox.notifyDataSetChanged();
					}

					progressDialog.dismiss();
					updatingView = new boolean[4];
					toggleRefreshButton();
					checkboxesOnOff(v1, false, -1);
				}
			}
		}

		@Override
		public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
			super.onActivityResult(requestCode, resultCode, data);
			if (resultCode == RESULT_OK) {
				if (requestCode == REQUESTCODE_HTMLVIEW) {
					String action = data.getExtras().getString(ApiConstants.ACTION);

					if (action.equals(ApiConstants.DELETE)) {
						// DELETE LETTER
					} else if (action.equals(ApiConstants.LOCATION_ARCHIVE)) {
						MoveDocumentsTask moveTask = new MoveDocumentsTask(ApiConstants.LOCATION_ARCHIVE);
						tempLetter.setLocation(ApiConstants.LOCATION_ARCHIVE);
						moveTask.execute(Secret.ACCESS_TOKEN, tempLetter);
					} else if (action.equals(ApiConstants.LOCATION_WORKAREA)) {
						MoveDocumentsTask moveTask = new MoveDocumentsTask(ApiConstants.LOCATION_WORKAREA);
						tempLetter.setLocation(ApiConstants.LOCATION_WORKAREA);
						moveTask.execute(Secret.ACCESS_TOKEN, tempLetter);
					}
				}
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
					unsupportedActionDialog(getString(R.string.dialog_error_two_factor));
					return;
				}
				String filetype = mletter.getFileType();

				if (networkConnection.isNetworkAvailable()) {
					if (filetype.equals(ApiConstants.FILETYPE_PDF)) {
						GetPDFTask pdfTask = new GetPDFTask();
						pdfTask.execute(mletter);
					} else if (filetype.equals(ApiConstants.FILETYPE_HTML)) {
						GetHTMLTask htmlTask = new GetHTMLTask();
						htmlTask.execute(ApiConstants.GET_DOCUMENT, mletter);
					} else {
						unsupportedActionDialog(getString(R.string.dialog_error_not_supported_filetype));
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
				htmlTask.execute(ApiConstants.GET_RECEIPT, mReceipt);
			}
		}

		private class MultiSelectionListener implements OnClickListener {
			LetterListAdapter adapter;

			public MultiSelectionListener(final LetterListAdapter adapter) {
				this.adapter = adapter;
			}

			public void onClick(final View v) {
				boolean[] checkedlist = adapter_mailbox.getCheckedDocuments();

				if (v.getId() == R.id.mailbox_toArchive) {
					Toast.makeText(getActivity(), "Midlertidig av", 2000).show();
					// MultipleDocumentsTask multipleDocumentsTask = new
					// MultipleDocumentsTask(ApiConstants.LOCATION_ARCHIVE,
					// checkedlist);
					// multipleDocumentsTask.execute(adapter_mailbox);
				} else if (v.getId() == R.id.mailbox_toWorkarea) {
					Toast.makeText(getActivity(), "Midlertidig av", 2000).show();
					// MultipleDocumentsTask multipleDocumentsTask = new
					// MultipleDocumentsTask(ApiConstants.LOCATION_WORKAREA,
					// checkedlist);
					// multipleDocumentsTask.execute(adapter_mailbox);
				} else if (v.getId() == R.id.mailbox_delete) {
					Toast.makeText(getActivity(), "Midlertidig av", 2000).show();
				}
			}
		}
	}

}
