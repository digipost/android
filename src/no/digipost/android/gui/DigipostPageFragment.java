package no.digipost.android.gui;

import java.util.ArrayList;

import no.digipost.android.R;
import no.digipost.android.api.ApiConstants;
import no.digipost.android.api.DigipostApiException;
import no.digipost.android.api.DigipostAuthenticationException;
import no.digipost.android.api.DigipostClientException;
import no.digipost.android.api.LetterOperations;
import no.digipost.android.model.Attachment;
import no.digipost.android.model.Letter;
import no.digipost.android.model.Receipt;
import no.digipost.android.pdf.PDFActivity;
import no.digipost.android.pdf.PDFStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class DigipostPageFragment extends Fragment implements FragmentCommunicator {
	public static final String ARG_SECTION_NUMBER = "section_number";
	public static final int REQUESTCODE_INTENT = 1;
	public static final String BASE_UPDATE_ALL = "updateAll";
	public static final String BASE_UPDATE_SINGLE ="updateSingle";
	public static final String BASE_INVALID_TOKEN = "invalidToken";

	private static boolean[] fragmentsRefreshing;

	private ActivityCommunicator activityCommunicator;

	private static LetterOperations lo;
	private LetterArrayAdapter adapterLetter;
	private ReceiptArrayAdapter adapterReceipts;
	private ListView listview;
	private View view;
	private View listEmpty;
	private ProgressDialog progressDialog;
	private LinearLayout bottombar;
	private ImageButton moveToWorkarea;
	private ImageButton moveToArchive;
	private ImageButton delete;
	public boolean checkboxesVisible;
	private String searchConstraint;

	private Dialog attachmentDialog;

	private Letter tempLetter;
	private Receipt tempReceipt;

	public DigipostPageFragment() {
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		lo = new LetterOperations(getActivity().getApplicationContext());
		fragmentsRefreshing = new boolean[4];
		searchConstraint = "";
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		int number = getArguments().getInt(ARG_SECTION_NUMBER);

		if (number == 0) {
			view = inflater.inflate(R.layout.fragment_layout_mailbox, container, false);
			listview = (ListView) view.findViewById(R.id.listview_mailbox);
			View emptyView = view.findViewById(R.id.empty_listview_mailbox);
			listview.setEmptyView(emptyView);
			adapterLetter = new LetterArrayAdapter(getActivity(), R.layout.mailbox_list_item, new ArrayList<Letter>());
			listview.setAdapter(adapterLetter);
			listview.setOnItemClickListener(new ListListener());
			listEmpty = view.findViewById(R.id.mailbox_empty);
			bottombar = (LinearLayout) view.findViewById(R.id.mailbox_bottombar);
			moveToWorkarea = (ImageButton) view.findViewById(R.id.mailbox_toWorkarea);
			moveToWorkarea.setOnClickListener(new MultiSelectionListener());
			moveToArchive = (ImageButton) view.findViewById(R.id.mailbox_toArchive);
			moveToArchive.setOnClickListener(new MultiSelectionListener());
			delete = (ImageButton) view.findViewById(R.id.mailbox_delete);
			delete.setOnClickListener(new MultiSelectionListener());
		} else if (number == 1) {
			view = inflater.inflate(R.layout.fragment_layout_workarea, container, false);
			listview = (ListView) view.findViewById(R.id.listview_kitchen);
			View emptyView = view.findViewById(R.id.empty_listview_workarea);
			listview.setEmptyView(emptyView);
			adapterLetter = new LetterArrayAdapter(getActivity(), R.layout.mailbox_list_item, new ArrayList<Letter>());
			listview.setAdapter(adapterLetter);
			listview.setOnItemClickListener(new ListListener());
			listEmpty = view.findViewById(R.id.workarea_empty);
			bottombar = (LinearLayout) view.findViewById(R.id.workarea_bottombar);
			delete = (ImageButton) view.findViewById(R.id.workarea_delete);
			delete.setOnClickListener(new MultiSelectionListener());
			moveToArchive = (ImageButton) view.findViewById(R.id.workarea_toArchive);
			moveToArchive.setOnClickListener(new MultiSelectionListener());
		} else if (number == 2) {
			view = inflater.inflate(R.layout.fragment_layout_archive, container, false);
			listview = (ListView) view.findViewById(R.id.listview_archive);
			View emptyView = view.findViewById(R.id.empty_listview_archive);
			listview.setEmptyView(emptyView);
			adapterLetter = new LetterArrayAdapter(getActivity(), R.layout.mailbox_list_item, new ArrayList<Letter>());
			listview.setAdapter(adapterLetter);
			listview.setOnItemClickListener(new ListListener());
			listEmpty = view.findViewById(R.id.archive_empty);
			bottombar = (LinearLayout) view.findViewById(R.id.archive_bottombar);
			delete = (ImageButton) view.findViewById(R.id.archive_delete);
			delete.setOnClickListener(new MultiSelectionListener());
			moveToWorkarea = (ImageButton) view.findViewById(R.id.archive_toWorkarea);
			moveToWorkarea.setOnClickListener(new MultiSelectionListener());
		} else if (number == 3) {
			view = inflater.inflate(R.layout.fragment_layout_receipts, container, false);
			listview = (ListView) view.findViewById(R.id.listview_receipts);
			View emptyView = view.findViewById(R.id.empty_listview_receipts);
			listview.setEmptyView(emptyView);
			adapterReceipts = new ReceiptArrayAdapter(getActivity(), R.layout.mailbox_list_item, new ArrayList<Receipt>());
			listview.setAdapter(adapterReceipts);
			listview.setOnItemClickListener(new ReceiptListListener());
			listEmpty = view.findViewById(R.id.receipts_empty);
			bottombar = (LinearLayout) view.findViewById(R.id.receipt_bottombar);
			delete = (ImageButton) view.findViewById(R.id.receipt_delete);
			delete.setOnClickListener(new MultiSelectionListener());
		}

		listview.setOnItemLongClickListener(new MyOnItemLongClickListener(number));
		listview.setOnKeyListener(new MyOnKeyListener(number));
		loadAccountMeta(number);

		return view;
	}

	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
		Context context = getActivity();
		activityCommunicator = (ActivityCommunicator) context;
		((BaseFragmentActivity) context).fragmentCommunicator = this;
	}

	private void forceGarbageCollection() {
		System.gc();
	}

	public void loadAccountMeta(final int type) {
		if (type != LetterOperations.RECEIPTS) {
			new GetAccountMetaTask(type).execute();
		} else {
			new GetReceiptsMetaTask().execute();
		}
	}

	private boolean accountMetaRefreshing() {
		for (boolean refreshing : fragmentsRefreshing) {
			if (refreshing) {
				return true;
			}
		}

		fragmentsRefreshing = new boolean[4];
		return false;
	}

	public void filterList(final int type, final CharSequence constraint) {
		searchConstraint = constraint.toString();

		if (type != LetterOperations.RECEIPTS) {
			adapterLetter.getFilter().filter(constraint);
		} else {
			adapterReceipts.getFilter().filter(constraint);
		}
	}

	public void clearFilter(final int type) {
		if (type != LetterOperations.RECEIPTS) {
			adapterLetter.clearFilter();
		} else {
			adapterReceipts.clearFilter();
		}
	}

	private void toggleRefreshSpinnerOn() {
		BaseFragmentActivity.refreshButton.setVisibility(View.GONE);
		BaseFragmentActivity.refreshSpinner.setVisibility(View.VISIBLE);
	}

	private void toggleRefreshSpinnerOff() {
		if (!accountMetaRefreshing()) {
			BaseFragmentActivity.refreshButton.setVisibility(View.VISIBLE);
			BaseFragmentActivity.refreshSpinner.setVisibility(View.GONE);
		}
	}

	private void showBottomBar() {
		Animation anim = new TranslateAnimation(0, 0, bottombar.getHeight(), 0);
		anim.setDuration(200);
		anim.setAnimationListener(new Animation.AnimationListener() {
			public void onAnimationStart(final Animation animation) {

			}

			public void onAnimationRepeat(final Animation animation) {
			}

			public void onAnimationEnd(final Animation animation) {
				bottombar.setVisibility(View.VISIBLE);
			}
		});
		bottombar.startAnimation(anim);
	}

	public void hideBottomBar() {
		Animation anim = new TranslateAnimation(0, 0, 0, bottombar.getHeight());
		anim.setDuration(200);
		anim.setAnimationListener(new Animation.AnimationListener() {
			public void onAnimationStart(final Animation animation) {
			}

			public void onAnimationRepeat(final Animation animation) {
			}

			public void onAnimationEnd(final Animation animation) {
				bottombar.setVisibility(View.GONE);
			}
		});
		bottombar.startAnimation(anim);
	}

	public void scrollListToTop() {
		listview.smoothScrollToPosition(0);
	}

	private void showAttachmentDialog(final ArrayList<Attachment> attachments) {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.attachmentdialog_layout, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(view);

		ListView attachmentlistview = (ListView) view.findViewById(R.id.attachmentdialog_listview);

		attachmentlistview.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(final AdapterView<?> arg0, final View arg1, final int arg2, final long arg3) {
				Attachment attachment = attachments.get(arg2);

				if (attachment.getAuthenticationLevel().equals(ApiConstants.AUTHENTICATION_LEVEL_TWO_FACTOR)) {
					unsupportedActionDialog(getString(R.string.dialog_error_header_two_factor), getString(R.string.dialog_error_two_factor));
					return;
				}

				if (attachment.getFileType().equals(ApiConstants.FILETYPE_PDF)) {
					GetPDFTask pdfTask = new GetPDFTask();
					pdfTask.execute(attachment);
				} else if (attachment.getFileType().equals(ApiConstants.FILETYPE_HTML)) {
					GetHTMLTask htmlTask = new GetHTMLTask();
					htmlTask.execute(ApiConstants.GET_DOCUMENT, attachment);
				} else {
					unsupportedActionDialog(getString(R.string.dialog_error_header_filetype),
							getString(R.string.dialog_error_not_supported_filetype));
				}
			}
		});

		AttachmentArrayAdapter attachmentadapter = new AttachmentArrayAdapter(getActivity(), R.layout.attachentdialog_list_item,
				attachments);
		Attachment main = attachmentadapter.findMain();
		attachmentadapter.remove(main);
		attachmentadapter.insert(main, 0);

		attachmentlistview.setAdapter(attachmentadapter);

		attachmentDialog = builder.create();
		attachmentDialog.show();

	}

	private void loadAccountMetaComplete() {
		activityCommunicator.passDataToActivity(BASE_UPDATE_ALL);
	}

	private void clearCheckboxes(final int type) {
		if (type != LetterOperations.RECEIPTS && adapterLetter.getShowBoxes()) {
			adapterLetter.clearCheckboxes();
		} else if (type == LetterOperations.RECEIPTS && adapterReceipts.getShowBoxes()) {
			adapterReceipts.clearCheckboxes();
		}
	}

	private void toggleMultiselectionOn(final int type, final int position) {
		listview.requestFocus();

		if (type != LetterOperations.RECEIPTS && !adapterLetter.getShowBoxes()) {
			adapterLetter.setInitialcheck(position);
			showBottomBar();
		} else if (type == LetterOperations.RECEIPTS && !adapterReceipts.getShowBoxes()) {
			adapterReceipts.setInitialcheck(position);
			showBottomBar();
		}
		checkboxesVisible = true;

	}

	public void toggleMultiselectionOff(final int type) {
		if (checkboxesVisible) {
			listview.requestFocus();
			clearCheckboxes(type);
			hideBottomBar();
			checkboxesVisible = false;
		}
	}

	private void unsupportedActionDialog(final String header, final String text) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setTitle(header)
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

	@SuppressWarnings("rawtypes")
	private void loadContentProgressDialog(final AsyncTask task) {
		progressDialog = new ProgressDialog(getActivity());
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage(getString(R.string.loading_content));
		progressDialog.setCancelable(false);
		progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.abort), new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int which) {
				dialog.dismiss();
				task.cancel(true);
			}
		});
		progressDialog.show();
	}

	private void clearContentProgressDialog() {
		progressDialog.dismiss();
		progressDialog = null;
	}

	public void showMessage(final String message) {
		Toast toast = Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT);
		toast.show();
	}

	private class GetReceiptsMetaTask extends AsyncTask<Void, Void, ArrayList<Receipt>> {
		private String errorMessage;
		private boolean invalidToken;

		public GetReceiptsMetaTask() {
			errorMessage = "";
			invalidToken = false;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			fragmentsRefreshing[LetterOperations.RECEIPTS] = true;
			toggleRefreshSpinnerOn();
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
			} catch (DigipostAuthenticationException e) {
				errorMessage = e.getMessage();
				invalidToken = true;
				return null;
			}
		}

		@Override
		protected void onPostExecute(final ArrayList<Receipt> result) {
			super.onPostExecute(result);
			fragmentsRefreshing[LetterOperations.RECEIPTS] = false;
			toggleRefreshSpinnerOff();

			if (result == null) {
				if (!accountMetaRefreshing()) {
					showMessage(errorMessage);

					if (invalidToken) {
						activityCommunicator.passDataToActivity(BASE_INVALID_TOKEN);
					}
				}
				return;
			}

			adapterReceipts.updateList(result, searchConstraint);
			if (result.isEmpty()) {
				listEmpty.setVisibility(View.VISIBLE);
			} else {
				listEmpty.setVisibility(View.GONE);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			fragmentsRefreshing[LetterOperations.RECEIPTS] = false;
			toggleRefreshSpinnerOff();
		}
	}

	private class GetAccountMetaTask extends AsyncTask<Void, Void, ArrayList<Letter>> {
		private final int type;
		private String errorMessage = "";
		private final boolean invalidToken;

		public GetAccountMetaTask(final int type) {
			this.type = type;
			invalidToken = false;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			fragmentsRefreshing[type] = true;
			toggleRefreshSpinnerOn();
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
			} catch (DigipostAuthenticationException e) {
				errorMessage = e.getMessage();
				return null;
			}
		}

		@Override
		protected void onPostExecute(final ArrayList<Letter> result) {
			super.onPostExecute(result);
			fragmentsRefreshing[type] = false;
			toggleRefreshSpinnerOff();

			if (result == null) {
				if (!accountMetaRefreshing()) {
					showMessage(errorMessage);

					if (invalidToken) {
						activityCommunicator.passDataToActivity(BASE_INVALID_TOKEN);
					}
				}
				return;
			}

			adapterLetter.updateList(result, searchConstraint);
			if (result.isEmpty()) {
				listEmpty.setVisibility(View.VISIBLE);
			} else {
				listEmpty.setVisibility(View.GONE);
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			fragmentsRefreshing[type] = false;
			toggleRefreshSpinnerOff();
		}
	}

	private class GetPDFTask extends AsyncTask<Object, Void, byte[]> {
		private Letter letter;
		private String errorMessage = "";
		private boolean invalidToken;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			forceGarbageCollection();
			loadContentProgressDialog(this);
			invalidToken = false;
		}

		@Override
		protected byte[] doInBackground(final Object... params) {

			if (params[0] instanceof Letter) {
				letter = (Letter) params[0];
			}
			try {
				return lo.getDocumentContentPDF(params[0]);
			} catch (DigipostApiException e) {
				errorMessage = e.getMessage();
				return null;
			} catch (DigipostClientException e) {
				errorMessage = e.getMessage();
				return null;
			} catch (DigipostAuthenticationException e) {
				errorMessage = e.getMessage();
				invalidToken = true;
				return null;
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			clearContentProgressDialog();
		}

		@Override
		protected void onPostExecute(final byte[] result) {
			super.onPostExecute(result);

			if (result == null) {
				showMessage(errorMessage);

				if (invalidToken) {
					activityCommunicator.passDataToActivity(BASE_INVALID_TOKEN);
				}
			} else {
				if (letter != null) {
					tempLetter = letter;
					PDFStore.pdf = result;
					Intent i = new Intent(getActivity().getApplicationContext(), PDFActivity.class);
					i.putExtra(ApiConstants.LOCATION_FROM, tempLetter.getLocation());
					startActivityForResult(i, REQUESTCODE_INTENT);
				} else {
					PDFStore.pdf = result;
					Intent i = new Intent(getActivity().getApplicationContext(), PDFActivity.class);
					i.putExtra(ApiConstants.LOCATION_FROM, tempLetter.getLocation());
					startActivityForResult(i, REQUESTCODE_INTENT);
				}
			}

			clearContentProgressDialog();
			activityCommunicator.passDataToActivity(BASE_UPDATE_SINGLE);
		}
	}

	private class GetHTMLTask extends AsyncTask<Object, Void, String> {
		private String errorMessage = "";
		private Letter letter;
		private Attachment attachment;
		private Receipt receipt;
		private boolean invalidToken;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			loadContentProgressDialog(this);
			invalidToken = false;
		}

		@Override
		protected String doInBackground(final Object... params) {
			try {
				if (params[1] instanceof Attachment) {
					attachment = (Attachment) params[1];
					return lo.getDocumentContentHTML(attachment);
				} else {

					if (params[0].equals(ApiConstants.GET_RECEIPT)) {
						receipt = (Receipt) params[1];
						return lo.getReceiptContentHTML(receipt);
					} else {
						letter = (Letter) params[1];
						return lo.getDocumentContentHTML(letter);
					}
				}
			} catch (DigipostApiException e) {
				errorMessage = e.getMessage();
				return null;
			} catch (DigipostClientException e) {
				errorMessage = e.getMessage();
				return null;
			} catch (DigipostAuthenticationException e) {
				errorMessage = e.getMessage();
				invalidToken = true;
				return null;
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			clearContentProgressDialog();
		}

		@Override
		protected void onPostExecute(final String result) {
			super.onPostExecute(result);

			if (result == null) {
				showMessage(errorMessage);

				if (invalidToken) {
					activityCommunicator.passDataToActivity(BASE_INVALID_TOKEN);
				}
			} else {
				Intent i = new Intent(getActivity(), HtmlActivity.class);
				if (attachment != null) {
					i.putExtra(ApiConstants.FILETYPE_HTML, result);
					i.putExtra(ApiConstants.DOCUMENT_TYPE, "");
					i.putExtra(ApiConstants.LOCATION_FROM, tempLetter.getLocation());
				} else {
					String type = letter != null ? ApiConstants.LETTER : ApiConstants.RECEIPT;
					i.putExtra(ApiConstants.DOCUMENT_TYPE, type);
					i.putExtra(ApiConstants.FILETYPE_HTML, result);
					if (type.equals(ApiConstants.LETTER)) {
						i.putExtra(ApiConstants.LOCATION_FROM, letter.getLocation());
						tempLetter = letter;
					} else if (type.equals(ApiConstants.RECEIPT)) {
						tempReceipt = receipt;
					}
				}
				startActivityForResult(i, REQUESTCODE_INTENT);
			}

			clearContentProgressDialog();
			activityCommunicator.passDataToActivity(BASE_UPDATE_SINGLE);
		}
	}

	private class DeleteTask extends AsyncTask<Object, Integer, String> {
		private boolean invalidToken;

		public DeleteTask() {
			invalidToken = false;
		}

		@Override
		protected String doInBackground(final Object... params) {
			try {
				lo.delete(params[0]);
				return null;
			} catch (DigipostApiException e) {
				return e.getMessage();
			} catch (DigipostClientException e) {
				return e.getMessage();
			} catch (DigipostAuthenticationException e) {
				invalidToken = true;
				return e.getMessage();
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			toggleRefreshSpinnerOff();
		}

		@Override
		protected void onPostExecute(final String result) {
			super.onPostExecute(result);

			if (result != null) {
				showMessage(result);

				if (invalidToken) {
					activityCommunicator.passDataToActivity(BASE_INVALID_TOKEN);
				}
			} else {
				loadAccountMetaComplete();
			}
		}
	}

	private class MoveDocumentsTask extends AsyncTask<Letter, Void, Boolean> {
		private final String toLocation;
		private String errorMessage;
		private boolean invalidToken;

		public MoveDocumentsTask(final String toLocation) {
			this.toLocation = toLocation;
			invalidToken = false;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(final Letter... params) {
			try {
				lo.moveDocument(params[0], toLocation);
				return true;
			} catch (DigipostApiException e) {
				errorMessage = e.getMessage();
				return false;
			} catch (DigipostClientException e) {
				errorMessage = e.getMessage();
				return false;
			} catch (DigipostAuthenticationException e) {
				invalidToken = true;
				errorMessage = e.getMessage();
				return false;
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			toggleRefreshSpinnerOff();
		}

		@Override
		protected void onPostExecute(final Boolean result) {
			if (!result) {
				showMessage(errorMessage);

				if (invalidToken) {
					activityCommunicator.passDataToActivity(BASE_INVALID_TOKEN);
				}
			} else {
				loadAccountMetaComplete();
			}
			toggleRefreshSpinnerOff();
		}
	}

	private class MultipleDocumentsTask extends AsyncTask<Object, Integer, String> {
		private Letter letter;
		private final String action;
		private final boolean[] checked;
		private int counter = 0;
		private final int type;
		private LetterArrayAdapter documentadapter;
		private ReceiptArrayAdapter receiptadapter;
		private int checkedCount;
		private boolean invalidToken;

		public MultipleDocumentsTask(final int type, final String action, final boolean[] checked) {
			this.type = type;
			this.action = action;
			this.checked = checked;
			checkedCount = 0;
			invalidToken = false;

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(getActivity());
			progressDialog.setMessage(getString(R.string.please_wait));
			progressDialog.show();
		}

		@Override
		protected String doInBackground(final Object... params) {
			if (params[0] instanceof LetterArrayAdapter) {
				documentadapter = (LetterArrayAdapter) params[0];
				checkedCount = documentadapter.checkedCount();
			} else {
				receiptadapter = (ReceiptArrayAdapter) params[0];
				checkedCount = receiptadapter.checkedCount();
			}

			try {
				if (action.equals(ApiConstants.DELETE)) {
					for (int i = 0; i < checked.length; i++) {
						if (checked[i]) {
							if (type == ApiConstants.TYPE_LETTER) {
								lo.delete(documentadapter.getItem(i));
							} else {
								lo.delete(receiptadapter.getItem(i));
								publishProgress(++counter);
							}
						}
					}
				} else {
					for (int i = 0; i < checked.length; i++) {
						if (checked[i]) {
							letter = documentadapter.getItem(i);
							letter.setLocation(action);
							lo.moveDocument(letter, action);
							publishProgress(++counter);
						}
					}
				}

				return null;
			} catch (DigipostApiException e) {
				return e.getMessage();
			} catch (DigipostClientException e) {
				return e.getMessage();
			} catch (DigipostAuthenticationException e) {
				invalidToken = true;
				return e.getMessage();
			}
		}

		@Override
		protected void onProgressUpdate(final Integer... values) {
			super.onProgressUpdate(values);

			if (action.equals(ApiConstants.DELETE)) {
				progressDialog.setMessage(values[0] + " av " + checkedCount + " slettet");
			} else {
				progressDialog.setMessage(values[0] + " av " + checkedCount + " flyttet");
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();

			if (type == ApiConstants.TYPE_LETTER) {
				documentadapter.clearCheckboxes();
			} else {
				receiptadapter.clearCheckboxes();
			}
			clearContentProgressDialog();
			loadAccountMetaComplete();
			toggleRefreshSpinnerOn();
			hideBottomBar();
		}

		@Override
		protected void onPostExecute(final String result) {
			if (result != null) {
				showMessage(result);

				if (invalidToken) {
					activityCommunicator.passDataToActivity(BASE_INVALID_TOKEN);
				}
			} else {
				loadAccountMetaComplete();
			}

			if (type == ApiConstants.TYPE_LETTER) {
				documentadapter.clearCheckboxes();
			} else {
				receiptadapter.clearCheckboxes();
			}

			hideBottomBar();
			clearContentProgressDialog();
			toggleRefreshSpinnerOff();
		}
	}

	private class GetImageTask extends AsyncTask<Letter, Void, Bitmap> {
		private String errorMessage;
		private boolean invalidToken;
		private Letter letter;

		public GetImageTask() {
			errorMessage = "";
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			forceGarbageCollection();
			loadContentProgressDialog(this);
			invalidToken = false;
		}

		@Override
		protected Bitmap doInBackground(final Letter... params) {
			try {
				letter = params[0];
				return lo.getDocumentContentImage(letter);
			} catch (DigipostApiException e) {
				errorMessage = e.getMessage();
				return null;
			} catch (DigipostClientException e) {
				errorMessage = e.getMessage();
				return null;
			} catch (DigipostAuthenticationException e) {
				errorMessage = e.getMessage();
				invalidToken = true;
				return null;
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			clearContentProgressDialog();
		}

		@Override
		protected void onPostExecute(final Bitmap result) {
			if (result == null) {
				showMessage(errorMessage);

				if (invalidToken) {
					activityCommunicator.passDataToActivity(BASE_INVALID_TOKEN);
				}
			} else {
				tempLetter = letter;
				ImageStore.image = result;
				Intent i = new Intent(getActivity().getApplicationContext(), ImageActivity.class);
				i.putExtra(ApiConstants.LOCATION_FROM, letter.getLocation());
				startActivityForResult(i, REQUESTCODE_INTENT);
			}

			clearContentProgressDialog();
			activityCommunicator.passDataToActivity(BASE_UPDATE_SINGLE);
		}
	}

	private class ListListener implements OnItemClickListener {
		public void onItemClick(final AdapterView<?> arg0, final View arg1, final int position, final long arg3) {
			Letter mletter = adapterLetter.getItem(position);

			if (mletter.getAuthenticationLevel().equals(ApiConstants.AUTHENTICATION_LEVEL_TWO_FACTOR)) {
				unsupportedActionDialog(getString(R.string.dialog_error_header_two_factor), getString(R.string.dialog_error_two_factor));
				return;
			}

			String filetype = mletter.getFileType();

			if (mletter.getAttachment().size() > 1) {
				showAttachmentDialog(mletter.getAttachment());
				tempLetter = mletter;
			} else {

				if (filetype.equals(ApiConstants.FILETYPE_PDF)) {
					GetPDFTask pdfTask = new GetPDFTask();
					pdfTask.execute(mletter);
				} else if (filetype.equals(ApiConstants.FILETYPE_HTML)) {
					GetHTMLTask htmlTask = new GetHTMLTask();
					htmlTask.execute(ApiConstants.GET_DOCUMENT, mletter);
				} else if (filetype.equals(ApiConstants.FILETYPE_PNG) || filetype.equals(ApiConstants.FILETYPE_JPG)
						|| filetype.equals(ApiConstants.FILETYPE_JPEG)) {
					GetImageTask imageTask = new GetImageTask();
					imageTask.execute(mletter);
				} else {
					unsupportedActionDialog(getString(R.string.dialog_error_header_filetype),
							getString(R.string.dialog_error_not_supported_filetype));
					return;
				}
			}
		}
	}

	private class ReceiptListListener implements OnItemClickListener {
		public void onItemClick(final AdapterView<?> arg0, final View arg1, final int position, final long arg3) {
			Receipt mReceipt = adapterReceipts.getItem(position);

			GetHTMLTask htmlTask = new GetHTMLTask();
			htmlTask.execute(ApiConstants.GET_RECEIPT, mReceipt);
		}
	}

	private class MyOnItemLongClickListener implements OnItemLongClickListener {
		private final int type;

		public MyOnItemLongClickListener(final int type) {
			this.type = type;
		}

		public boolean onItemLongClick(final AdapterView<?> arg0, final View arg1, final int arg2, final long arg3) {
			toggleMultiselectionOn(type, arg2);
			return true;
		}
	}

	private class MyOnKeyListener implements OnKeyListener {
		private final int type;

		public MyOnKeyListener(final int type) {
			this.type = type;
		}

		public boolean onKey(final View v, final int keyCode, final KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				if ((type == LetterOperations.RECEIPTS && adapterReceipts.getShowBoxes())
						|| (type != LetterOperations.RECEIPTS && adapterLetter.getShowBoxes())) {
					toggleMultiselectionOff(type);
					return true;
				}
			}

			return false;
		}
	}

	private void showMultiSelecetionWarning(final String text, final MultipleDocumentsTask task, final Object adapter) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(text).setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int id) {
				task.execute(adapter);
				dialog.dismiss();
			}
		}).setCancelable(false).setNegativeButton(getString(R.string.abort), new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private class MultiSelectionListener implements OnClickListener {
		public void onClick(final View v) {
			MultipleDocumentsTask multipleDocumentsTask = null;
			boolean[] checkedlist = null;
			int page = getArguments().getInt(ARG_SECTION_NUMBER) + 1;

			if (adapterLetter != null && adapterLetter.checkedCount() > 0) {
				checkedlist = adapterLetter.getCheckedDocuments();
				String where = "";
				String message = "";
				int itemsPicked = adapterLetter.checkedCount();
				if (v.equals(moveToWorkarea)) {
					where = ApiConstants.LOCATION_WORKAREA;
					message = getString(R.string.dialog_prompt_do_you_want_to_move)
							+ itemsPicked
							+ ((page != 1) ? ((itemsPicked > 1) ? getString(R.string.documents) : getString(R.string.document))
									: getString(R.string.letter)) + getString(R.string.dialog_prompt_to_workarea);

				} else if (v.equals(moveToArchive)) {
					where = ApiConstants.LOCATION_ARCHIVE;
					message = getString(R.string.dialog_prompt_do_you_want_to_move)
							+ itemsPicked
							+ ((page != 1) ? ((itemsPicked > 1) ? getString(R.string.documents) : getString(R.string.document))
									: getString(R.string.letter)) + getString(R.string.dialog_prompt_to_archive);

				} else if (v.equals(delete)) {
					where = ApiConstants.DELETE;
					message = getString(R.string.dialog_prompt_do_you_want_to_delete)
							+ itemsPicked
							+ ((page != 1) ? ((itemsPicked > 1) ? getString(R.string.documents) : getString(R.string.document))
									: getString(R.string.letter)) + "?";
				}
				multipleDocumentsTask = new MultipleDocumentsTask(ApiConstants.TYPE_LETTER, where, checkedlist);
				showMultiSelecetionWarning(message, multipleDocumentsTask, adapterLetter);

			} else if (adapterReceipts != null && adapterReceipts.checkedCount() > 0) {
				checkedlist = adapterReceipts.getCheckedDocuments();

				if (v.equals(delete)) {
					multipleDocumentsTask = new MultipleDocumentsTask(ApiConstants.TYPE_RECEIPT, ApiConstants.DELETE, checkedlist);
					showMultiSelecetionWarning(getString(R.string.dialog_prompt_do_you_want_to_delete) + adapterReceipts.checkedCount()
							+ ((adapterReceipts.checkedCount() > 1) ? getString(R.string.receiptss) : getString(R.string.receipt)),
							multipleDocumentsTask, adapterReceipts);
				}
			}
		}
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUESTCODE_INTENT) {
			if (resultCode == Activity.RESULT_OK) {
				if (attachmentDialog != null) {
					attachmentDialog.cancel();
				}

				String action = data.getExtras().getString(ApiConstants.ACTION);
				String type = data.getExtras().getString(ApiConstants.DOCUMENT_TYPE);

				if (action.equals(ApiConstants.DELETE)) {
					DeleteTask deleteTask = new DeleteTask();
					if (type.equals(ApiConstants.RECEIPT)) {
						deleteTask.execute(tempReceipt);
					} else {
						deleteTask.execute(tempLetter);
					}
				} else {
					tempLetter.setLocation(action);
					MoveDocumentsTask moveTask = new MoveDocumentsTask(action);
					moveTask.execute(tempLetter);
				}
			}
		}
	}

	public void passDataToFragment(final String someValue) {
		// Fra Activity
	}
}
