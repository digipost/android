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
import no.digipost.android.gui.content.HtmlAndReceiptActivity;
import no.digipost.android.gui.adapters.ReceiptArrayAdapter;
import no.digipost.android.model.Receipt;
import no.digipost.android.model.Receipts;
import no.digipost.android.utilities.DialogUtitities;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

public class ReceiptFragment extends ContentFragment {

	public ReceiptFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);

        super.contentMultiChoiceModeListener = new ReceiptMultiChoiceModeListener();

		super.listAdapter = new ReceiptArrayAdapter(getActivity(), R.layout.content_list_item, new CheckBoxOnClickListener());
		super.listView.setAdapter(listAdapter);
		super.listView.setMultiChoiceModeListener(super.contentMultiChoiceModeListener);
		super.listView.setOnItemClickListener(new ReceiptListOnItemClickListener());

		updateAccountMeta();

		return view;
	}

	@Override
	public int getContent() {
		return ApplicationConstants.RECEIPTS;
	}

	private void checkStatusAndDisplayReceipts(Receipts receipts) {

		ArrayList<Receipt> receipt = receipts.getReceipt();
		ReceiptFragment.super.listAdapter.replaceAll(receipt);

		int numberOfCards = Integer.parseInt(receipts.getNumberOfCards());
		int numberOfCardsReadyForVerification = Integer.parseInt(receipts.getNumberOfCardsReadyForVerification());
		int numberOfReceiptsHiddenUntilVerification = Integer.parseInt(receipts.getNumberOfReceiptsHiddenUntilVerification());

		if (receipt.size() == 0) {
			if (numberOfCards == 0) {
				setListEmptyViewText(
						"Kom i gang med elektroniske kvitteringer",
						"Med Digipost har du tilgang til dine kvitteringer der du er. Se kvitteringene på mobilen, via et nettbrett eller på din datamaskin. Tjenesten er gratis å bruke.");
			} else if (numberOfCardsReadyForVerification > 0) {
				setListEmptyViewText(
						"Verifisering",
						"For å se kvitteringer fra dette bankkortet må du verifisere at dette er ditt kort. Du verifiserer kortet ved å taste inn kjøpesummen fra et kjøp som er gjort i en butikk som støtter Elektronisk Kvittering.");
			} else {
				setListEmptyViewText(
						"Registert",
						"Neste steg er å betale med kort i en butikk som støtter Elektronisk Kvittering."
								+ "\n"
								+ "\n"
								+ "Ta vare på kvitteringen fra dette kjøpet. Du kan bruke kvitteringen til å verifisere bankkortet. Du trenger kun å verifisere kortet én gang.");

			}
		} else {
			if (numberOfCards == 0) {
				setTopText("For å motta elektroniske kvitteringer må du registere dine kort");
			} else if (numberOfReceiptsHiddenUntilVerification == 1) {
				setTopText("En kvittering er skjult. For å se denne må du verifisere kortene dine");
			} else if (numberOfCardsReadyForVerification > 1) {
				setTopText("Du har" + numberOfReceiptsHiddenUntilVerification
						+ " skjulte kvitteringer, for å se disse må du verifisere kortene dine");
			}
		}
	}

	public void updateAccountMeta() {
		GetReceiptsMetaTask task = new GetReceiptsMetaTask();
		task.execute();
	}

	private void openListItem(Receipt receipt) {
		GetReceiptContentTask task = new GetReceiptContentTask(receipt);
		task.execute();
	}

	private void openReceipt(String receiptContent) {
		Intent intent = new Intent(getActivity(), HtmlAndReceiptActivity.class);
		intent.putExtra(super.INTENT_CONTENT, getContent());
		intent.putExtra(ApiConstants.GET_RECEIPT, receiptContent);
		startActivityForResult(intent, super.INTENT_REQUESTCODE);
	}

	private class GetReceiptContentTask extends AsyncTask<Void, Void, String> {
		private Receipt receipt;
		private String errorMessage;
		private boolean invalidToken;

		public GetReceiptContentTask(Receipt receipt) {
			this.receipt = receipt;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (!ReceiptFragment.super.progressDialogIsVisible)
				ReceiptFragment.super.showContentProgressDialog(this, context.getString(R.string.loading_content));

		}

		@Override
		protected String doInBackground(Void... voids) {
			try {
				return ContentOperations.getReceiptContentHTML(context, receipt);
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
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			ReceiptFragment.super.hideProgressDialog();

			if (result != null) {
				DocumentContentStore.setContent(receipt);
				openReceipt(result);
			} else {
				if (invalidToken) {
					activityCommunicator.requestLogOut();
				}

				DialogUtitities.showToast(ReceiptFragment.this.getActivity(), errorMessage);
			}

			updateAccountMeta();
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			ReceiptFragment.super.hideProgressDialog();
		}
	}

	private class GetReceiptsMetaTask extends AsyncTask<Void, Void, Receipts> {
		private String errorMessage;
		private boolean invalidToken;

		public GetReceiptsMetaTask() {
			errorMessage = "";
			invalidToken = false;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			activityCommunicator.onStartRefreshContent();
		}

		@Override
		protected Receipts doInBackground(final Void... params) {
			try {
				return ContentOperations.getAccountContentMetaReceipt(context);
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
		protected void onPostExecute(final Receipts receipts) {
			super.onPostExecute(receipts);
			if (receipts != null) {
				checkStatusAndDisplayReceipts(receipts);
			} else {
				DialogUtitities.showToast(ReceiptFragment.this.context, errorMessage);

				if (invalidToken) {
					activityCommunicator.requestLogOut();
				} else if (listAdapter.isEmpty()) {
					ReceiptFragment.super.setListEmptyViewNoNetwork(true);
				}
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

	private class ReceiptListOnItemClickListener implements AdapterView.OnItemClickListener {
		public void onItemClick(final AdapterView<?> arg0, final View view, final int position, final long arg3) {
			openListItem((Receipt) ReceiptFragment.super.listAdapter.getItem(position));
		}
	}

	private void deleteReceipt(Receipt receipt) {
		ArrayList<Object> receipts = new ArrayList<Object>();
		receipts.add(receipt);

		ContentDeleteTask task = new ContentDeleteTask(receipts);
		task.execute();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == getActivity().RESULT_OK) {
			if (requestCode == INTENT_REQUESTCODE) {

				String action = data.getStringExtra(ApiConstants.ACTION);

				if (action.equals(ApiConstants.DELETE)) {
					deleteReceipt(DocumentContentStore.documentReceipt);
				}
			}
		}
	}

	private class ReceiptMultiChoiceModeListener extends ContentMultiChoiceModeListener {

		@Override
		public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
			super.onCreateActionMode(actionMode, menu);

			MenuItem moveDocument = menu.findItem(R.id.main_context_menu_folder);
			moveDocument.setVisible(false);

			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode actionMode, android.view.MenuItem menuItem) {
			super.onActionItemClicked(actionMode, menuItem);

			switch (menuItem.getItemId()) {
			case R.id.main_context_menu_delete:
				ReceiptFragment.super.deleteContent(getString(R.string.dialog_prompt_delete_receipts));
				break;
			}

			return true;
		}
	}
}
