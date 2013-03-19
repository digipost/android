package no.digipost.android.gui;

import java.util.ArrayList;

import no.digipost.android.R;
import no.digipost.android.api.ApiConstants;
import no.digipost.android.api.DigipostApiException;
import no.digipost.android.api.DigipostClientException;
import no.digipost.android.api.LetterOperations;
import no.digipost.android.model.Attachment;
import no.digipost.android.model.Letter;
import no.digipost.android.model.Receipt;
import no.digipost.android.pdf.PDFActivity;
import no.digipost.android.pdf.PdfStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

public class DigipostSectionFragment extends Fragment implements FragmentCommunicator {
	public static final String ARG_SECTION_NUMBER = "section_number";
	public static final int REQUESTCODE_INTENT = 1;

	private static boolean[] fragmentsRefreshing;

	private ActivityCommunicator activityCommunicator;

	private static NetworkConnection networkConnection;
	private static LetterOperations lo;
	private LetterListAdapter adapterLetter;
	private ReceiptListAdapter adapterReciepts;
	private ListView listview;
	private View view;
	private View listEmpty;
	private ProgressDialog progressDialog;
	private LinearLayout bottombar;
	private ImageButton moveToWorkarea;
	private ImageButton moveToArchive;
	private ImageButton delete;

	private Letter tempLetter;
	private Receipt tempReceipt;
	private View tempRowView;

	public DigipostSectionFragment() {
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		lo = new LetterOperations(getActivity().getApplicationContext());
		networkConnection = new NetworkConnection(getActivity().getApplicationContext());
		fragmentsRefreshing = new boolean[4];

		progressDialog = new ProgressDialog(getActivity());
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		int number = getArguments().getInt(ARG_SECTION_NUMBER);

		if (number == 0) {
			view = inflater.inflate(R.layout.fragment_layout_mailbox, container, false);
			listview = (ListView) view.findViewById(R.id.listview_mailbox);
			View emptyView = view.findViewById(R.id.empty_listview_mailbox);
			listview.setEmptyView(emptyView);
			adapterLetter = new LetterListAdapter(getActivity(), R.layout.mailbox_list_item, new ArrayList<Letter>());
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
			adapterLetter = new LetterListAdapter(getActivity(), R.layout.mailbox_list_item, new ArrayList<Letter>());
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
			adapterLetter = new LetterListAdapter(getActivity(), R.layout.mailbox_list_item, new ArrayList<Letter>());
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
			adapterReciepts = new ReceiptListAdapter(getActivity(), R.layout.mailbox_list_item, new ArrayList<Receipt>());
			listview.setAdapter(adapterReciepts);
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
		((BaseActivity) context).fragmentCommunicator = this;
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
		if (type != LetterOperations.RECEIPTS) {
			adapterLetter.getFilter().filter(constraint);
		} else {
			adapterReciepts.getFilter().filter(constraint);
		}
	}

	private void toggleRefreshSpinnerOn() {
		BaseActivity.refreshButton.setVisibility(View.GONE);
		BaseActivity.refreshSpinner.setVisibility(View.VISIBLE);
	}

	private void toggleRefreshSpinnerOff() {
		if (!accountMetaRefreshing()) {
			BaseActivity.refreshButton.setVisibility(View.VISIBLE);
			BaseActivity.refreshSpinner.setVisibility(View.GONE);
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

	private void showAttactmentDialog(final ArrayList<Attachment> attachments) {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.attachmentdialog_layout, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Vedlegg:");
		builder.setView(view);
		ListView attachmentlistview = (ListView) view.findViewById(R.id.attachmentdialog_listview);

		attachmentlistview.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(final AdapterView<?> arg0, final View arg1, final int arg2, final long arg3) {
				Attachment attachment  = attachments.get(arg2);
				if(attachment.getFileType().equals(ApiConstants.FILETYPE_PDF)) {
					GetPDFTask pdfTask = new GetPDFTask();
					pdfTask.execute(attachment);
				} else if (attachment.getFileType().equals(ApiConstants.FILETYPE_HTML)) {
					GetHTMLTask htmlTask = new GetHTMLTask();
					htmlTask.execute(ApiConstants.GET_DOCUMENT, attachment);
				} else {
					unsupportedActionDialog(getString(R.string.dialog_error_not_supported_filetype));
				}
			}
		});

		AttachmentListAdapter attachmentadapter = new AttachmentListAdapter(getActivity(), R.layout.attachentdialog_list_item,attachments);

		attachmentlistview.setAdapter(attachmentadapter);

		final Dialog dialog = builder.create();
	    dialog.show();

	}

	private void loadAccountMetaComplete() {
		activityCommunicator.passDataToActivity("updateAll");
	}

	private void toggleMultiselectionOn(final int type, final int position) {
		listview.requestFocus();

		if (type != LetterOperations.RECEIPTS && !adapterLetter.getShowBoxes()) {
			adapterLetter.setInitialcheck(position);
			showBottomBar();
		} else if (type == LetterOperations.RECEIPTS && !adapterReciepts.getShowBoxes()) {
			adapterReciepts.setInitialcheck(position);
			showBottomBar();
		}
	}

	public void toggleMultiselectionOff(final int type) {
		listview.requestFocus();

		if (type != LetterOperations.RECEIPTS && adapterLetter.getShowBoxes()) {
			adapterLetter.clearCheckboxes();
			hideBottomBar();
		} else if (type == LetterOperations.RECEIPTS && adapterReciepts.getShowBoxes()) {
			adapterReciepts.clearCheckboxes();
			hideBottomBar();
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

	@SuppressWarnings("rawtypes")
	private void loadContentProgressDialog(final AsyncTask task) {
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

	public void showMessage(final String message) {
		Toast toast = Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT);
		toast.show();
	}

	private class GetReceiptsMetaTask extends AsyncTask<Void, Void, ArrayList<Receipt>> {
		private String errorMessage;

		public GetReceiptsMetaTask() {
			errorMessage = "";
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
				}
				return;
			}

			adapterReciepts.updateList(result);
			listEmpty.setVisibility(View.VISIBLE);
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

		public GetAccountMetaTask(final int type) {
			this.type = type;
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
				}
				return;
			}

			adapterLetter.updateList(result);
			listEmpty.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			fragmentsRefreshing[type] = false;
			toggleRefreshSpinnerOff();
		}
	}

	private class GetPDFTask extends AsyncTask<Object, Void, byte[]> {
		Letter letter;
		Attachment attachment;
		private String errorMessage = "";

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			loadContentProgressDialog(this);
		}

		@Override
		protected byte[] doInBackground(final Object... params) {

			if(params[0] instanceof Letter) {
				letter = (Letter)params[0];
			} else {
				attachment = (Attachment)params[0];
			}
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
		}

		@Override
		protected void onPostExecute(final byte[] result) {
			super.onPostExecute(result);

			if (result == null) {
				showMessage(errorMessage);
			} else {
				if(letter != null) {
				tempLetter = letter;
				PdfStore.pdf = result;
				Intent i = new Intent(getActivity().getApplicationContext(), PDFActivity.class);
				i.putExtra(ApiConstants.LOCATION_FROM, letter.getLocation());
				startActivityForResult(i, REQUESTCODE_INTENT);
				} else {
					PdfStore.pdf = result;
					Intent i = new Intent(getActivity().getApplicationContext(), PDFActivity.class);
					i.putExtra(ApiConstants.LOCATION_FROM, "");
					startActivityForResult(i, REQUESTCODE_INTENT);
				}
			}

			progressDialog.dismiss();
		}
	}

	private class GetHTMLTask extends AsyncTask<Object, Void, String> {
		private String errorMessage = "";
		private Letter letter;
		private Attachment attachment;
		private Receipt receipt;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			loadContentProgressDialog(this);
		}

		@Override
		protected String doInBackground(final Object... params) {
			try {
				if(params[1] instanceof Attachment) {
					attachment = (Attachment) params[1];
					return lo.getDocumentContentHTML(params[1]);
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
				if(attachment != null) {
					i.putExtra(ApiConstants.FILETYPE_HTML, result);
					i.putExtra(ApiConstants.DOCUMENT_TYPE,"");
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

			progressDialog.dismiss();
		}
	}

	private class DeleteTask extends AsyncTask<Object, Integer, String> {
		private final String type;
		private String errorMessage;

		public DeleteTask(final String type) {
			this.type = type;
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
				showMessage(errorMessage);
			} else {
				if (!type.equals(ApiConstants.RECEIPT)) {
					adapterLetter.remove(tempRowView, tempLetter);
				} else {
					adapterReciepts.remove(tempRowView, tempReceipt);
				}
				loadAccountMetaComplete();
			}
		}
	}

	private class MoveDocumentsTask extends AsyncTask<Letter, Void, Boolean> {
		private final String toLocation;
		private String errorMessage;

		public MoveDocumentsTask(final String toLocation) {
			this.toLocation = toLocation;
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
			} catch (Exception e) {
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
			super.onPostExecute(result);

			if (!result) {
				showMessage(errorMessage);
			} else {
				adapterLetter.remove(tempRowView, tempLetter);
				loadAccountMetaComplete();
			}
			toggleRefreshSpinnerOff();
		}
	}

	private class MultipleDocumentsTask extends AsyncTask<Object, Integer, String> {

		Letter letter;
		String action;
		boolean[] checked;
		int counter = 0;
		String errorMessage;
		int type;
		LetterListAdapter documentadapter;
		ReceiptListAdapter receiptadapter;
		int checkedCount;

		public MultipleDocumentsTask(final int type, final String action, final boolean[] checked) {
			this.type = type;
			this.action = action;
			this.checked = checked;
			checkedCount = 0;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			/*
			 * progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE,
			 * getString(R.string.abort), new DialogInterface.OnClickListener()
			 * { public void onClick(final DialogInterface dialog, final int
			 * which) { dialog.dismiss(); cancel(true); } });
			 */
			progressDialog.setMessage("Vennligst vent...");
			progressDialog.show();
		}

		@Override
		protected String doInBackground(final Object... params) {
			if (params[0] instanceof LetterListAdapter) {
				documentadapter = (LetterListAdapter) params[0];
				checkedCount = documentadapter.checkedCount();
			} else {
				receiptadapter = (ReceiptListAdapter) params[0];
				checkedCount = receiptadapter.checkedCount();
			}

			if (action.equals(ApiConstants.DELETE)) {
				for (int i = 0; i < checked.length; i++) {
					try {
						if (checked[i]) {
							if (type == ApiConstants.TYPE_LETTER) {
								lo.delete(documentadapter.getItem(i));
							} else {
								lo.delete(receiptadapter.getItem(i));
								publishProgress(++counter);
							}
						}
					} catch (DigipostApiException e) {
						return e.getMessage();
					} catch (DigipostClientException e) {
						return e.getMessage();
					} catch (Exception e) {
						return e.getMessage();
					}
				}
			} else {
				for (int i = 0; i < checked.length; i++) {
					try {
						if (checked[i]) {
							letter = documentadapter.getItem(i);
							letter.setLocation(action);
							lo.moveDocument(letter, action);
							publishProgress(++counter);
						}
					} catch (DigipostApiException e) {
						return e.getMessage();
					} catch (DigipostClientException e) {
						return e.getMessage();
					} catch (Exception e) {
						return e.getMessage();
					}
				}
			}

			return null;
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
			progressDialog.dismiss();
			loadAccountMetaComplete();
			toggleRefreshSpinnerOn();
			hideBottomBar();
		}

		@Override
		protected void onPostExecute(final String result) {
			super.onPostExecute(result);

			if (result != null) {
				showMessage(errorMessage);
			}
			if (type == ApiConstants.TYPE_LETTER) {
				documentadapter.clearCheckboxes();
			} else {
				receiptadapter.clearCheckboxes();
			}
			hideBottomBar();
			loadAccountMetaComplete();
			progressDialog.dismiss();
			toggleRefreshSpinnerOn();
		}
	}

	private class ListListener implements OnItemClickListener {
		public void onItemClick(final AdapterView<?> arg0, final View arg1, final int position, final long arg3) {
			Letter mletter = adapterLetter.getItem(position);

			if (mletter.getAuthenticationLevel().equals(ApiConstants.AUTHENTICATION_LEVEL_TWO_FACTOR)) {
				unsupportedActionDialog(getString(R.string.dialog_error_two_factor));
				return;
			}

			String filetype = mletter.getFileType();

			if(mletter.getAttachment().size() > 1) {
				showAttactmentDialog(mletter.getAttachment());
			} else {

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
			}
		}
	}

	private class ReceiptListListener implements OnItemClickListener {
		public void onItemClick(final AdapterView<?> arg0, final View arg1, final int position, final long arg3) {
			Receipt mReceipt = adapterReciepts.getItem(position);

			GetHTMLTask htmlTask = new GetHTMLTask();
			htmlTask.execute(ApiConstants.GET_RECEIPT, mReceipt);
			tempRowView = arg1;
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
				if ((type == LetterOperations.RECEIPTS && adapterReciepts.getShowBoxes())
						|| (type != LetterOperations.RECEIPTS && adapterLetter.getShowBoxes())) {
					toggleMultiselectionOff(type);
					return true;
				}
			}

			return false;
		}
	}

	public void showMultiSelecetionWarning(final String text, final MultipleDocumentsTask task, final Object adapter) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(text).setPositiveButton("Ja", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int id) {
				task.execute(adapter);
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

	private class MultiSelectionListener implements OnClickListener {
		public void onClick(final View v) {
			MultipleDocumentsTask multipleDocumentsTask = null;
			boolean[] checkedlist = null;

			if (adapterLetter != null && adapterLetter.checkedCount() > 0) {
				checkedlist = adapterLetter.getCheckedDocuments();

				if (v.equals(moveToWorkarea)) {
					multipleDocumentsTask = new MultipleDocumentsTask(ApiConstants.TYPE_LETTER, ApiConstants.LOCATION_WORKAREA, checkedlist);
					showMultiSelecetionWarning("Vil du flytte " + adapterLetter.checkedCount() + " brev til "
							+ getString(R.string.workarea) + "?", multipleDocumentsTask, adapterLetter);
				} else if (v.equals(moveToArchive)) {
					multipleDocumentsTask = new MultipleDocumentsTask(ApiConstants.TYPE_LETTER, ApiConstants.LOCATION_ARCHIVE, checkedlist);
					showMultiSelecetionWarning("Vil du flytte " + adapterLetter.checkedCount() + " brev til arkivet?",
							multipleDocumentsTask, adapterLetter);
				} else if (v.equals(delete)) {
					multipleDocumentsTask = new MultipleDocumentsTask(ApiConstants.TYPE_LETTER, ApiConstants.DELETE, checkedlist);
					showMultiSelecetionWarning("Vil du slette " + adapterLetter.checkedCount() + " brev?", multipleDocumentsTask,
							adapterLetter);
				}
			} else if (adapterReciepts != null && adapterReciepts.checkedCount() > 0) {
				checkedlist = adapterReciepts.getCheckedDocuments();

				if (v.equals(delete)) {
					multipleDocumentsTask = new MultipleDocumentsTask(ApiConstants.TYPE_RECEIPT, ApiConstants.DELETE, checkedlist);
					showMultiSelecetionWarning("Vil du slette " + adapterReciepts.checkedCount()
							+ ((adapterReciepts.checkedCount() > 1) ? " kvitteringer?" : " kvittering?"), multipleDocumentsTask,
							adapterReciepts);
				}
			}
		}
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUESTCODE_INTENT) {
			PdfStore.pdf = null;

			if (resultCode == Activity.RESULT_OK) {
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
					tempLetter.setLocation(action);
					MoveDocumentsTask moveTask = new MoveDocumentsTask(action);
					moveTask.execute(tempLetter);
				}
			}
		}
	}

	public void passDataToFragment(final String someValue) {
		// TODO Fra Activity
	}
}
