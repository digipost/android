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
	private ImageButton optionsButton, refreshButton, logoButton;
	private ProgressBar refreshSpinner;
	private ButtonListener listener;
	private ButtonListener buttonListener;
	private final int REQUEST_CODE = 1;
	private ViewPager mViewPager;
	private Context context;
	public static final int REQUESTCODE_INTENT = 1;
	public static final int REQUESTCODE_PDF = 2;
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
		optionsButton.setOnClickListener(buttonListener);
		refreshButton.setOnClickListener(buttonListener);
		logoButton.setOnClickListener(buttonListener);
		networkConnection = new NetworkConnection(this);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setClickable(true);
		mSectionsPagerAdapter.notifyDataSetChanged();
	}

	private class ButtonListener implements OnClickListener {

		public void onClick(final View v) {
			if (v == optionsButton) {
				openOptionsMenu();
			} else if (v == refreshButton) {
				refreshViewById(mViewPager.getCurrentItem());
			} else if (v == logoButton) {
				scrollViewToTopById(mViewPager.getCurrentItem());
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
		case R.id.basemenu_mailbox:
			mViewPager.setCurrentItem(0, true);
			return true;
		case R.id.basemenu_workarea:
			mViewPager.setCurrentItem(1, true);
			return true;
		case R.id.basemenu_archive:
			mViewPager.setCurrentItem(2, true);
			return true;
		case R.id.basemenu_receipts:
			mViewPager.setCurrentItem(3, true);
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

	private void scrollViewToTopById(final int page) {
		switch (page) {
		case LetterOperations.MAILBOX:
			lv_mailbox.smoothScrollToPosition(0);
			break;
		case LetterOperations.WORKAREA:
			lv_workarea.smoothScrollToPosition(0);
			break;
		case LetterOperations.ARCHIVE:
			lv_archive.smoothScrollToPosition(0);
			break;
		case LetterOperations.RECEIPTS:
			lv_receipts.smoothScrollToPosition(0);
			break;
		}
	}

	private void refreshViewById(final int page) {
		if (!networkConnection.isNetworkAvailable()) {
			showMessage(getString(R.string.error_your_network));
			return;
		}

		refreshButton.setVisibility(View.GONE);
		refreshSpinner.setVisibility(View.VISIBLE);

		switch (page) {
		case LetterOperations.MAILBOX:
			loadMailbox();
			break;
		case LetterOperations.WORKAREA:
			loadWorkarea();
			break;
		case LetterOperations.ARCHIVE:
			loadArchive();
			break;
		case LetterOperations.RECEIPTS:
			loadReceipts();
			break;
		}

	}

	private void updateViews() {
		if (networkConnection.isNetworkAvailable()) {
			updatingView = new boolean[4];
			updatingView[LetterOperations.RECEIPTS] = true;
			loadMailbox();
			loadWorkarea();
			loadArchive();
			loadReceipts();
			toggleRefreshButtonOn();
		} else {
			showMessage(getString(R.string.error_your_network));
		}
	}

	private void toggleRefreshButtonOn() {
		refreshButton.setVisibility(View.GONE);
		refreshSpinner.setVisibility(View.VISIBLE);
	}

	private void toggleRefreshButtonOff() {
		for (boolean i : updatingView) {
			if (i) {
				return;
			}
		}
		resetResfreshButton();
	}

	private void resetResfreshButton() {
		updatingView = new boolean[4];
		refreshSpinner.setVisibility(View.GONE);
		refreshButton.setVisibility(View.VISIBLE);
	}

	public void loadMailbox() {
		if (networkConnection.isNetworkAvailable()) {
			new GetAccountMetaTask(LetterOperations.MAILBOX).execute();
		}
	}

	private void loadWorkarea() {
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
			updatingView[LetterOperations.RECEIPTS] = false;
			toggleRefreshButtonOff();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			resetResfreshButton();
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
			updatingView[type] = false;
			toggleRefreshButtonOff();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			resetResfreshButton();
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
		public int getItemPosition(final Object object) {
			return POSITION_NONE;

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
		private Receipt tempReceipt;
		private LetterListAdapter tempLetterAdapter;
		private ReceiptListAdapter tempReceiptsAdapter;
		private View tempRowView;

		private ImageButton mailbox_multiSelection_moveToWorkarea;
		private ImageButton mailbox_multiSelection_moveToArchive;
		private ImageButton mailbox_multiSelection_delete;

		private ImageButton workarea_multiSelection_moveToArchive;
		private ImageButton workarea_multiSelection_delete;

		private ImageButton archive_multiSelection_moveToWorkArea;
		private ImageButton archive_multiSelection_delete;

		private ImageButton receipts_multiSelection_delete;

		MultiSelectionListener multiSelectionListener;

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
				adapter_mailbox = new LetterListAdapter(getActivity(), R.layout.mailbox_list_item, new ArrayList<Letter>(), v1,
						R.id.mailbox_bottombar);
				lv_mailbox.setAdapter(adapter_mailbox);
				lv_mailbox.setOnItemClickListener(new ListListener(adapter_mailbox));

				multiSelectionListener = new MultiSelectionListener(adapter_mailbox);

				mailbox_multiSelection_moveToArchive = (ImageButton) v1.findViewById(R.id.mailbox_toArchive);
				mailbox_multiSelection_moveToArchive.setOnClickListener(multiSelectionListener);
				mailbox_multiSelection_moveToWorkarea = (ImageButton) v1.findViewById(R.id.mailbox_toWorkarea);
				mailbox_multiSelection_moveToWorkarea.setOnClickListener(multiSelectionListener);
				mailbox_multiSelection_delete = (ImageButton) v1.findViewById(R.id.mailbox_delete);
				mailbox_multiSelection_delete.setOnClickListener(multiSelectionListener);

				lv_mailbox.setOnItemLongClickListener(new OnItemLongClickListener() {
					public boolean onItemLongClick(final AdapterView<?> arg0, final View arg1, final int position, final long arg3) {
						checkboxesOnOff(v1, true, position, adapter_mailbox, lv_mailbox);
						return true;
					}
				});

				lv_mailbox.setOnKeyListener(new OnKeyListener() {
					public boolean onKey(final View view, final int keyCode, final KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_BACK) {
							if (LetterListAdapter.showboxes == true) {
								checkboxesOnOff(v1, false, -1, adapter_mailbox, lv_mailbox);
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
				final View v2 = inflater.inflate(R.layout.fragment_layout_workarea, container, false);
				lv_workarea = (ListView) v2.findViewById(R.id.listview_kitchen);
				View emptyView = v2.findViewById(R.id.empty_listview_workarea);
				lv_workarea.setEmptyView(emptyView);
				adapter_workarea = new LetterListAdapter(getActivity(), R.layout.mailbox_list_item, new ArrayList<Letter>(), v2,
						R.id.workarea_bottombar);
				lv_workarea.setAdapter(adapter_workarea);
				lv_workarea.setOnItemClickListener(new ListListener(adapter_workarea));

				multiSelectionListener = new MultiSelectionListener(adapter_workarea);

				workarea_multiSelection_moveToArchive = (ImageButton) v2.findViewById(R.id.workarea_toArchive);
				workarea_multiSelection_moveToArchive.setOnClickListener(multiSelectionListener);
				workarea_multiSelection_delete = (ImageButton) v2.findViewById(R.id.workarea_delete);
				workarea_multiSelection_delete.setOnClickListener(multiSelectionListener);

				lv_workarea.setOnItemLongClickListener(new OnItemLongClickListener() {
					public boolean onItemLongClick(final AdapterView<?> arg0, final View arg1, final int position, final long arg3) {
						checkboxesOnOff(v2, true, position, adapter_workarea, lv_workarea);
						return true;
					}
				});

				lv_workarea.setOnKeyListener(new OnKeyListener() {
					public boolean onKey(final View view, final int keyCode, final KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_BACK) {
							if (LetterListAdapter.showboxes == true) {
								checkboxesOnOff(v2, false, -1, adapter_workarea, lv_workarea);
								return true;
							}
							return false;
						}
						return false;
					}
				});

				return v2;
			} else if (number == 3) {
				final View v3 = inflater.inflate(R.layout.fragment_layout_archive, container, false);
				lv_archive = (ListView) v3.findViewById(R.id.listview_archive);
				View emptyView = v3.findViewById(R.id.empty_listview_archive);
				lv_archive.setEmptyView(emptyView);
				adapter_archive = new LetterListAdapter(getActivity(), R.layout.mailbox_list_item, new ArrayList<Letter>(), v3,
						R.id.archive_bottombar);
				lv_archive.setAdapter(adapter_archive);
				lv_archive.setOnItemClickListener(new ListListener(adapter_archive));

				multiSelectionListener = new MultiSelectionListener(adapter_archive);

				archive_multiSelection_moveToWorkArea = (ImageButton) v3.findViewById(R.id.archive_toWorkarea);
				archive_multiSelection_moveToWorkArea.setOnClickListener(multiSelectionListener);
				archive_multiSelection_delete = (ImageButton) v3.findViewById(R.id.archive_delete);
				archive_multiSelection_delete.setOnClickListener(multiSelectionListener);

				lv_archive.setOnItemLongClickListener(new OnItemLongClickListener() {
					public boolean onItemLongClick(final AdapterView<?> arg0, final View arg1, final int position, final long arg3) {
						checkboxesOnOff(v3, true, position, adapter_archive, lv_archive);
						return true;
					}
				});

				lv_archive.setOnKeyListener(new OnKeyListener() {
					public boolean onKey(final View view, final int keyCode, final KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_BACK) {
							if (LetterListAdapter.showboxes == true) {
								checkboxesOnOff(v3, false, -1, adapter_archive, lv_archive);
								return true;
							}
							return false;
						}
						return false;
					}
				});

				return v3;
			} else {
				final View v4 = inflater.inflate(R.layout.fragment_layout_receipts, container, false);
				lv_receipts = (ListView) v4.findViewById(R.id.listview_receipts);
				View emptyView = v4.findViewById(R.id.empty_listview_receipts);
				lv_receipts.setEmptyView(emptyView);
				adapter_receipts = new ReceiptListAdapter(getActivity(), R.layout.mailbox_list_item, new ArrayList<Receipt>(), v4,
						R.id.receipt_bottombar);
				lv_receipts.setAdapter(adapter_receipts);
				lv_receipts.setOnItemClickListener(new ReceiptListListener(adapter_receipts));

				lv_receipts.setOnItemLongClickListener(new OnItemLongClickListener() {
					public boolean onItemLongClick(final AdapterView<?> arg0, final View arg1, final int position, final long arg3) {
						checkboxesOnOff(v4, true, position, adapter_receipts, lv_receipts);
						return true;
					}
				});

				lv_receipts.setOnKeyListener(new OnKeyListener() {
					public boolean onKey(final View view, final int keyCode, final KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_BACK) {
							if (LetterListAdapter.showboxes == true) {
								checkboxesOnOff(v4, false, -1, adapter_receipts, lv_receipts);
								return true;
							}
							return false;
						}
						return false;
					}
				});

				return v4;
			}
		}

		private void checkboxesOnOff(final View v, final boolean state, final int position, final Object adapter, final ListView lw) {
			if (adapter instanceof LetterListAdapter) {

				if (state) {
					lw.requestFocus();
					((LetterListAdapter) adapter).setInitialcheck(position);
					((LetterListAdapter) adapter).notifyDataSetChanged();
				} else {
					((LetterListAdapter) adapter).clearCheckboxes();
					((LetterListAdapter) adapter).notifyDataSetChanged();
				}
			} else {
				if (state) {
					lw.requestFocus();
					((ReceiptListAdapter) adapter).setInitialcheck(position);
					((ReceiptListAdapter) adapter).notifyDataSetChanged();
				} else {
					((ReceiptListAdapter) adapter).clearCheckboxes();
					((ReceiptListAdapter) adapter).notifyDataSetChanged();
				}
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
			Receipt receipt;

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
					receipt = (Receipt) params[1];
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
					} else if (type.equals(ApiConstants.RECEIPT)) {
						tempReceipt = receipt;
					}
					startActivityForResult(i, REQUESTCODE_INTENT);
				}

				progressDialog.dismiss();
			}
		}

		private class GetPDFTask extends AsyncTask<Letter, Void, byte[]> {
			Letter letter;
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
					letter = params[0];
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
			}

			@Override
			protected void onPostExecute(final byte[] result) {
				super.onPostExecute(result);

				if (result == null) {
					showMessage(errorMessage);
				} else {
					tempLetter = letter;
					PdfStore.pdf = result;
					Intent i = new Intent(getActivity().getApplicationContext(), PDFActivity.class);
					i.putExtra(ApiConstants.LOCATION_FROM, letter.getLocation());
					startActivityForResult(i, REQUESTCODE_INTENT);
				}

				progressDialog.dismiss();
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
			}

			@Override
			protected Boolean doInBackground(final Object... params) {
				letter = (Letter) params[1];
				fromLocation = letter.getLocation();
				try {
					return lo.moveDocument((String) params[0], (Letter) params[1], toLocation);
				} catch (DigipostApiException e) {
					errorMessage = e.getMessage();
					return false;
				} catch (DigipostClientException e) {
					errorMessage = e.getMessage();
					return false;
				} catch (Exception e) {
					System.out.println(e.getMessage());
					errorMessage = e.getMessage();
					return false;
				}
			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
				resetResfreshButton();
			}

			@Override
			protected void onPostExecute(final Boolean result) {
				super.onPostExecute(result);

				if (!result) {
					showMessage(errorMessage);
				} else {
					tempLetterAdapter.remove(tempRowView, tempLetter);
					updateViews();
				}
				resetResfreshButton();
			}
		}

		private class DeleteTask extends AsyncTask<Object, Void, Boolean> {
			private final String type;
			private String errorMessage;

			public DeleteTask(final String type) {
				this.type = type;
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
			}

			@Override
			protected Boolean doInBackground(final Object... params) {

				if (type.equals(ApiConstants.RECEIPT)) {
					try {
						return lo.delete(params[0]);
					} catch (DigipostApiException e) {
						errorMessage = e.getMessage();
						return false;
					} catch (DigipostClientException e) {
						errorMessage = e.getMessage();
						return false;
					}
				} else {
					try {
						return lo.delete(params[0]);
					} catch (DigipostApiException e) {
						errorMessage = e.getMessage();
						return false;
					} catch (DigipostClientException e) {
						errorMessage = e.getMessage();
						return false;
					}
				}
			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
				resetResfreshButton();
			}

			@Override
			protected void onPostExecute(final Boolean result) {
				super.onPostExecute(result);

				if (!result) {
					showMessage(errorMessage);
				} else {
					if (type.equals(ApiConstants.RECEIPT)) {
						tempReceiptsAdapter.remove(tempRowView, tempReceipt);
					} else {
						tempLetterAdapter.remove(tempRowView, tempLetter);
					}
					updateViews();
				}
				resetResfreshButton();
			}
		}

		private class MultipleDocumentsTask extends AsyncTask<Object, Integer, Boolean> {

			Letter letter;
			String action;
			boolean[] checked;
			int counter = 0;
			String errorMessage;
			int type;
			LetterListAdapter documentadapter;
			ReceiptListAdapter receiptadapter;

			public MultipleDocumentsTask(final int type, final String action, final boolean[] checked) {
				this.type = type;
				this.action = action;
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
				progressDialog.setMessage("Laster...");
				progressDialog.show();
			}

			@Override
			protected Boolean doInBackground(final Object... params) {
				boolean done = false;

				if (params[0] instanceof LetterListAdapter) {
					documentadapter = (LetterListAdapter) params[0];
				} else {
					receiptadapter = (ReceiptListAdapter) params[0];
				}

				if (action.equals(ApiConstants.DELETE)) {
					for (int i = 0; i < checked.length; i++) {
						try {
							if (checked[i]) {
								if (type == ApiConstants.TYPE_LETTER) {
									done = lo.delete(documentadapter.getItem(i));
								} else {
									done = lo.delete(receiptadapter.getItem(i));
									publishProgress(++counter);
								}
							}
						} catch (DigipostApiException e) {
							errorMessage = e.getMessage();
							return false;
						} catch (DigipostClientException e) {
							errorMessage = e.getMessage();
							return false;
						} catch (Exception e) {
							return false;
						}
					}
				} else {
					for (int i = 0; i < checked.length; i++) {
						try {
							if (checked[i]) {
								letter = documentadapter.getItem(i);
								letter.setLocation(action);
								done = lo.moveDocument(Secret.ACCESS_TOKEN, letter, action);
								publishProgress(++counter);
							}
						} catch (DigipostApiException e) {
							errorMessage = e.getMessage();
							return false;
						} catch (DigipostClientException e) {
							errorMessage = e.getMessage();
							return false;
						} catch (Exception e) {
							return false;
						}
					}
				}

				return done;
			}

			@Override
			protected void onProgressUpdate(final Integer... values) {
				super.onProgressUpdate(values);
				if (action.equals(ApiConstants.DELETE)) {
					progressDialog.setMessage(values[0] + " slettet");
				} else {
					progressDialog.setMessage(values[0] + " flyttet");
				}
			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
				progressDialog.dismiss();
				resetResfreshButton();
			}

			@Override
			protected void onPostExecute(final Boolean result) {
				super.onPostExecute(result);

				if (!result) {
					showMessage(errorMessage);
				}
				if (type == ApiConstants.TYPE_LETTER) {
					documentadapter.clearCheckboxes();
				} else {
					receiptadapter.clearCheckboxes();
				}
				updateViews();
				progressDialog.dismiss();
				resetResfreshButton();
			}
		}

		@Override
		public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
			super.onActivityResult(requestCode, resultCode, data);
			if (resultCode == RESULT_OK) {
				if (requestCode == REQUESTCODE_INTENT) {
					toggleRefreshButtonOn();
					String action = data.getExtras().getString(ApiConstants.ACTION);
					String type = data.getExtras().getString(ApiConstants.DOCUMENT_TYPE);

					if (action.equals(ApiConstants.DELETE)) {
						DeleteTask deleteTask = new DeleteTask(type);
						if (type.equals(ApiConstants.RECEIPT)) {
							deleteTask.execute(tempReceipt);
						} else {
							deleteTask.execute(tempLetter);
						}
					} else {
						MoveDocumentsTask moveTask = new MoveDocumentsTask(action);
						tempLetter.setLocation(action);
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
					tempRowView = arg1;
					tempLetterAdapter = adapter;
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
				tempRowView = arg1;
				tempReceiptsAdapter = adapter;
			}
		}

		private class MultiSelectionListener implements OnClickListener {
			LetterListAdapter adapter;
			MultipleDocumentsTask multipleDocumentsTask;

			public MultiSelectionListener(final LetterListAdapter adapter) {
				this.adapter = adapter;
			}

			public void onClick(final View v) {
				boolean[] checkedlist = adapter.getCheckedDocuments();

				if (v.getId() == R.id.mailbox_toArchive) {
					multipleDocumentsTask = new MultipleDocumentsTask(ApiConstants.TYPE_LETTER, ApiConstants.LOCATION_ARCHIVE, checkedlist);
					multipleDocumentsTask.execute(adapter);
				} else if (v.getId() == R.id.mailbox_toWorkarea) {
					multipleDocumentsTask = new MultipleDocumentsTask(ApiConstants.TYPE_LETTER, ApiConstants.LOCATION_WORKAREA, checkedlist);
					multipleDocumentsTask.execute(adapter);
				} else if (v.getId() == R.id.mailbox_delete) {
					multipleDocumentsTask = new MultipleDocumentsTask(ApiConstants.TYPE_LETTER, ApiConstants.DELETE, checkedlist);
					multipleDocumentsTask.execute(adapter);
				} else if (v.getId() == R.id.workarea_toArchive) {
					multipleDocumentsTask = new MultipleDocumentsTask(ApiConstants.TYPE_LETTER, ApiConstants.LOCATION_ARCHIVE, checkedlist);
					multipleDocumentsTask.execute(adapter);
				} else if (v.getId() == R.id.workarea_delete) {
					multipleDocumentsTask = new MultipleDocumentsTask(ApiConstants.TYPE_LETTER, ApiConstants.DELETE, checkedlist);
					multipleDocumentsTask.execute(adapter);
				} else if (v.getId() == R.id.archive_toWorkarea) {
					multipleDocumentsTask = new MultipleDocumentsTask(ApiConstants.TYPE_LETTER, ApiConstants.LOCATION_WORKAREA, checkedlist);
					multipleDocumentsTask.execute(adapter);
				} else if (v.getId() == R.id.archive_delete) {
					multipleDocumentsTask = new MultipleDocumentsTask(ApiConstants.TYPE_LETTER, ApiConstants.DELETE, checkedlist);
					multipleDocumentsTask.execute(adapter);
				}
			}
		}

		private class MultiSelectionReceiptListener implements OnClickListener {
			ReceiptListAdapter adapter;
			MultipleDocumentsTask multipleReceiptsTask;

			public MultiSelectionReceiptListener(final ReceiptListAdapter adapter) {
				this.adapter = adapter;
			}

			public void onClick(final View v) {
				boolean[] checkedlist = adapter.getCheckedDocuments();

				if (v.getId() == R.id.receipt_delete) {
					multipleReceiptsTask = new MultipleDocumentsTask(ApiConstants.TYPE_RECEIPT, ApiConstants.DELETE, checkedlist);
					multipleReceiptsTask.execute(adapter);
				}
			}
		}
	}

}
