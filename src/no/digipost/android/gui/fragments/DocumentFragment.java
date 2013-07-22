/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.digipost.android.gui.fragments;

import java.util.ArrayList;

import no.digipost.android.R;
import no.digipost.android.api.ContentOperations;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.documentstore.DocumentContentStore;
import no.digipost.android.gui.adapters.AttachmentArrayAdapter;
import no.digipost.android.gui.adapters.LetterArrayAdapter;
import no.digipost.android.gui.content.HtmlAndReceiptActivity;
import no.digipost.android.gui.content.MuPDFActivity;
import no.digipost.android.gui.content.UnsupportedDocumentFormatActivity;
import no.digipost.android.model.Attachment;
import no.digipost.android.model.Documents;
import no.digipost.android.model.Letter;
import no.digipost.android.utilities.DialogUtitities;
import no.digipost.android.utilities.SettingsUtilities;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public abstract class DocumentFragment extends ContentFragment {

	public DocumentFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		super.listAdapter = new LetterArrayAdapter(getActivity(), R.layout.content_list_item, new CheckBoxOnClickListener());
		super.listView.setAdapter(listAdapter);
		super.listView.setOnItemClickListener(new DocumentListOnItemClickListener());

		updateAccountMeta();

		return view;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == getActivity().RESULT_OK) {
			if (requestCode == INTENT_REQUESTCODE) {
				String action = data.getStringExtra(ApiConstants.ACTION);

				if (action.equals(ApiConstants.LOCATION_WORKAREA)) {
					executeDocumentMoveTask(action, DocumentContentStore.documentParent);
				} else if (action.equals(ApiConstants.LOCATION_ARCHIVE)) {
					executeDocumentMoveTask(action, DocumentContentStore.documentParent);
				} else if (action.equals(ApiConstants.DELETE)) {
					deleteDocument(DocumentContentStore.documentParent);
				}

                Letter letter = DocumentContentStore.documentParent;
                letter.setRead(Boolean.toString(true));
            }
		}

		DocumentContentStore.clearContent();
	}

	private class DocumentListOnItemClickListener implements AdapterView.OnItemClickListener {
		public void onItemClick(final AdapterView<?> arg0, final View view, final int position, final long arg3) {
			openListItem((Letter) DocumentFragment.super.listAdapter.getItem(position));
        }
	}

	private class AttachmentListOnItemClickListener implements AdapterView.OnItemClickListener {
		private AttachmentArrayAdapter attachmentArrayAdapter;
		private Letter parentLetter;

		public AttachmentListOnItemClickListener(AttachmentArrayAdapter attachmentArrayAdapter, Letter parentLetter) {
			this.attachmentArrayAdapter = attachmentArrayAdapter;
			this.parentLetter = parentLetter;
		}

		public void onItemClick(final AdapterView<?> arg0, final View arg1, final int position, final long arg3) {
			executeGetAttachmentContentTask(attachmentArrayAdapter.getItem(position), parentLetter);
		}
	}

	private void showAttachmentDialog(final Letter letter) {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.attachmentdialog_layout, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(view);

		ListView attachmentListView = (ListView) view.findViewById(R.id.attachmentdialog_listview);
		AttachmentArrayAdapter attachmentAdapter = new AttachmentArrayAdapter(getActivity(), R.layout.attachentdialog_list_item,
				letter.getAttachment());

		attachmentListView.setAdapter(attachmentAdapter);
		attachmentListView.setOnItemClickListener(new AttachmentListOnItemClickListener(attachmentAdapter, letter));
		attachmentAdapter.placeMainOnTop();

		Dialog attachmentDialog = builder.create();
		attachmentDialog.show();
	}

	private void openListItem(final Letter letter) {
		if (letter.getAuthenticationLevel().equals(ApiConstants.AUTHENTICATION_LEVEL_TWO_FACTOR)) {
			showTwoFactorDialog();
		} else if (letter.getOpeningReceiptUri() != null) {
			showOpeningReceiptDialog(letter);
		} else {
			findDocumentAttachments(letter);
		}
	}

	private void findDocumentAttachments(final Letter letter) {
		ArrayList<Attachment> attachments = letter.getAttachment();

		if (attachments.size() > 1) {
			showAttachmentDialog(letter);
		} else {
			executeGetAttachmentContentTask(attachments.get(0), letter);
		}
	}

	private void sendOpeningReceipt(final Letter letter) {
		SendOpeningReceiptTask task = new SendOpeningReceiptTask(letter);
		task.execute();
	}

	private void showOpeningReceiptDialog(final Letter letter) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(getString(R.string.dialog_answer_opening_receipt_message))
				.setPositiveButton(getString(R.string.dialog_answer_opening_receipt_yes), new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, final int id) {
						sendOpeningReceipt(letter);
						dialog.dismiss();
					}
				})
				.setCancelable(false)
				.setNegativeButton(getString(R.string.abort), new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, final int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void showTwoFactorDialog() {
		AlertDialog.Builder builder = DialogUtitities
				.getAlertDialogBuilderWithMessage(context, getString(R.string.dialog_error_two_factor));
		builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.cancel();
			}
		});
		builder.create().show();
	}

	private void openAttachmentContent(final Attachment attachment) {
		String fileType = attachment.getFileType();
		Intent intent = null;

		if (fileType.equals(ApiConstants.FILETYPE_PDF)) {
			intent = new Intent(getActivity(), MuPDFActivity.class);
		} else if (fileType.equals(ApiConstants.FILETYPE_HTML)) {
			intent = new Intent(getActivity(), HtmlAndReceiptActivity.class);
		} else {
			intent = new Intent(getActivity(), UnsupportedDocumentFormatActivity.class);
		}

		intent.putExtra(super.INTENT_CONTENT, getContent());
		startActivityForResult(intent, super.INTENT_REQUESTCODE);
	}

	private void executeGetAttachmentContentTask(Attachment attachment, Letter parentLetter) {
		GetAttachmentContentTask getAttachmentContentTask = new GetAttachmentContentTask(attachment, parentLetter);
		getAttachmentContentTask.execute();
	}

	private class GetAttachmentContentTask extends AsyncTask<Void, Void, byte[]> {
		private Attachment attachment;
		private Letter parentLetter;
		private String errorMessage;
		private boolean invalidToken;

		public GetAttachmentContentTask(Attachment attachment, Letter parentLetter) {
			this.attachment = attachment;
			this.parentLetter = parentLetter;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (!DocumentFragment.super.progressDialogIsVisible)
				DocumentFragment.super.showContentProgressDialog(this, context.getString(R.string.loading_content));

		}

		@Override
		protected byte[] doInBackground(Void... voids) {
			try {
				return ContentOperations.getDocumentContent(context, attachment);
			} catch (DigipostAuthenticationException e) {
				Log.e(getClass().getName(), e.getMessage(), e);
				errorMessage = e.getMessage();
				invalidToken = true;
				return null;
			} catch (DigipostApiException e) {
				Log.e(getClass().getName(), e.getMessage(), e);
				errorMessage = e.getMessage();
				return null;
			} catch (DigipostClientException e) {
				Log.e(getClass().getName(), e.getMessage(), e);
				errorMessage = e.getMessage();
				return null;
			}
		}

		@Override
		protected void onPostExecute(byte[] result) {
			super.onPostExecute(result);
			DocumentFragment.super.taskIsRunning = false;
			DocumentFragment.super.hideProgressDialog();

			if (result != null) {
				DocumentContentStore.setContent(result, attachment, parentLetter);
				openAttachmentContent(attachment);
			} else {
				if (invalidToken) {
					activityCommunicator.requestLogOut();
				}

				DialogUtitities.showToast(DocumentFragment.this.getActivity(), errorMessage);
			}
			updateAccountMeta();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			DocumentFragment.super.hideProgressDialog();
			DocumentContentStore.clearContent();
		}
	}

	@Override
	public void updateAccountMeta() {
		GetDocumentMetaTask task = new GetDocumentMetaTask(getContent());
		task.execute();
	}

	protected class GetDocumentMetaTask extends AsyncTask<Void, Void, Documents> {
		private final int content;
		private String errorMessage;
		private boolean invalidToken;

		public GetDocumentMetaTask(final int content) {
			this.content = content;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			activityCommunicator.onStartRefreshContent();
		}

		@Override
		protected Documents doInBackground(final Void... params) {
			try {
				return ContentOperations.getAccountContentMetaDocument(context, content);
			} catch (DigipostApiException e) {
				Log.e(getClass().getName(), e.getMessage(), e);
				errorMessage = e.getMessage();
				return null;
			} catch (DigipostClientException e) {
				Log.e(getClass().getName(), e.getMessage(), e);
				errorMessage = e.getMessage();
				return null;
			} catch (DigipostAuthenticationException e) {
				Log.e(getClass().getName(), e.getMessage(), e);
				errorMessage = e.getMessage();
				invalidToken = true;
				return null;
			}
		}

		@Override
		protected void onPostExecute(final Documents documents) {
			super.onPostExecute(documents);
			DocumentFragment.super.taskIsRunning = false;
			if (documents != null) {
				ArrayList<Letter> letters = documents.getDocument();
				DocumentFragment.super.listAdapter.replaceAll(letters);
				if (!letters.isEmpty()) {
					DocumentFragment.super.setListEmptyViewNoNetwork(false);
				} else {
					setEmptyViewText();
				}
			} else {
				if (invalidToken) {
					activityCommunicator.requestLogOut();
				} else if (listAdapter.isEmpty()) {
					DocumentFragment.super.setListEmptyViewNoNetwork(true);
				}

				DialogUtitities.showToast(DocumentFragment.this.context, errorMessage);
			}

			activityCommunicator.onUpdateAccountMeta();
			activityCommunicator.onEndRefreshContent();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			activityCommunicator.onEndRefreshContent();
		}
	}

	private void setEmptyViewText() {
		int content_type = getContent();
		String content = ApplicationConstants.titles[content_type].toLowerCase();
		String text = "";
		if (content_type == ApplicationConstants.MAILBOX) {
			text = "Ingen brev i " + content;
		} else if (content_type == ApplicationConstants.WORKAREA) {
			text = "Ingen dokumenter på " + content;
		} else {
			text = "Ingen dokumenter i " + content;
		}
		DocumentFragment.super.setListEmptyViewText(text, null);
	}

	protected void executeDocumentMoveTask(String toLocation) {
		ArrayList<Letter> letters = listAdapter.getCheckedItems();

		contentActionMode.finish();

		DocumentMoveTask documentMoveTask = new DocumentMoveTask(letters, toLocation);
		documentMoveTask.execute();
	}

	protected void executeDocumentMoveTask(String toLocation, Letter letter) {
		ArrayList<Letter> letters = new ArrayList<Letter>();
		letters.add(letter);

		DocumentMoveTask documentMoveTask = new DocumentMoveTask(letters, toLocation);
		documentMoveTask.execute();
	}

	protected void moveDocument(String toLocation, String message) {
		if (SettingsUtilities.getConfirmMovePreference(context)) {
			showMoveDocumentsDialog(toLocation, message);
		} else {
			executeDocumentMoveTask(toLocation);
		}
	}

	protected void showMoveDocumentsDialog(final String toLocation, final String message) {
		AlertDialog.Builder alertDialogBuilder = DialogUtitities.getAlertDialogBuilderWithMessage(context, message);
		alertDialogBuilder.setPositiveButton(R.string.move, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				executeDocumentMoveTask(toLocation);
				dialogInterface.dismiss();
			}
		});
		alertDialogBuilder.setNegativeButton(R.string.abort, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				dialogInterface.cancel();
			}
		});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	private class DocumentMoveTask extends AsyncTask<Void, Integer, String> {
		private ArrayList<Letter> letters;
		private String toLocation;
		private boolean invalidToken;

		public DocumentMoveTask(ArrayList<Letter> letters, String toLocation) {
			this.letters = letters;
			this.toLocation = toLocation;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			DocumentFragment.super.showContentProgressDialog(this, "");
		}

		@Override
		protected String doInBackground(Void... voids) {
			try {
				Integer progress = 0;

				for (Letter letter : letters) {
					publishProgress(++progress);
					letter.setLocation(toLocation);
					ContentOperations.moveDocument(context, letter);
				}

				return null;
			} catch (DigipostAuthenticationException e) {
				Log.e(getClass().getName(), e.getMessage(), e);
				invalidToken = true;
				return e.getMessage();
			} catch (DigipostApiException e) {
				Log.e(getClass().getName(), e.getMessage(), e);
				return e.getMessage();
			} catch (DigipostClientException e) {
				Log.e(getClass().getName(), e.getMessage(), e);
				return e.getMessage();
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			DocumentFragment.super.progressDialog.setMessage(values[0] + " av " + letters.size() + " flyttet");
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			DocumentFragment.super.hideProgressDialog();
			updateAccountMeta();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			DocumentFragment.super.taskIsRunning = false;
			DocumentFragment.super.hideProgressDialog();

			if (result != null) {
				DialogUtitities.showToast(context, result);

				if (invalidToken) {
					activityCommunicator.requestLogOut();
				}
			}

			updateAccountMeta();
		}
	}

	protected void deleteDocument(Letter letter) {
		ArrayList<Object> letters = new ArrayList<Object>();
		letters.add(letter);

		ContentDeleteTask contentDeleteTask = new ContentDeleteTask(letters);
		contentDeleteTask.execute();
	}

	protected class SendOpeningReceiptTask extends AsyncTask<Void, Void, Boolean> {
		private String errorMessage;
		private Letter letter;
		private boolean invalidToken;

		public SendOpeningReceiptTask(final Letter letter) {
			invalidToken = false;
			this.letter = letter;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			DocumentFragment.super.showContentProgressDialog(this, context.getString(R.string.loading_content));
			DocumentFragment.super.progressDialogIsVisible = true;
		}

		@Override
		protected Boolean doInBackground(final Void... params) {
			try {
				ContentOperations.sendOpeningReceipt(context, letter);
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
		}

		@Override
		protected void onPostExecute(final Boolean result) {

			if (result != null) {

				UpdateSelfAndOpenTask task = new UpdateSelfAndOpenTask(letter);
				task.execute();
			} else {
				if (invalidToken) {
					activityCommunicator.requestLogOut();
				}

				DialogUtitities.showToast(DocumentFragment.this.getActivity(), errorMessage);
			}
		}
	}

	protected class UpdateSelfAndOpenTask extends AsyncTask<Void, Void, Boolean> {
		private String errorMessage;
		private Letter letter;
		private boolean invalidToken;

		public UpdateSelfAndOpenTask(final Letter letter) {
			invalidToken = false;
			this.letter = letter;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(final Void... params) {
			try {
				letter = ContentOperations.getSelfLetter(context, letter);
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
		}

		@Override
		protected void onPostExecute(final Boolean result) {

			if (result != null) {
				findDocumentAttachments(letter);
			} else {
				if (invalidToken) {
					activityCommunicator.requestLogOut();
				}
				DialogUtitities.showToast(DocumentFragment.this.getActivity(), errorMessage);
			}
		}
	}

	protected class DocumentMultiChoiceModeListener extends ContentMultiChoiceModeListener {

		@Override
		public boolean onActionItemClicked(ActionMode actionMode, android.view.MenuItem menuItem) {
			super.onActionItemClicked(actionMode, menuItem);

			switch (menuItem.getItemId()) {
			case R.id.main_context_menu_delete:
				DocumentFragment.super.deleteContent(getString(R.string.dialog_prompt_delete_documents));
				break;
			}

			return true;
		}
	}
}
