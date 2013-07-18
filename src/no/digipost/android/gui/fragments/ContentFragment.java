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
import no.digipost.android.gui.adapters.ContentArrayAdapter;
import no.digipost.android.gui.content.SettingsActivity;
import no.digipost.android.utilities.DialogUtitities;
import no.digipost.android.utilities.FileUtilities;
import no.digipost.android.utilities.SettingsUtilities;
import no.digipost.android.utilities.SharedPreferencesUtilities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public abstract class ContentFragment extends Fragment {
	public static final int INTENT_REQUESTCODE = 0;
	public static final String INTENT_CONTENT = "content";

	ActivityCommunicator activityCommunicator;

	protected Context context;

	protected ListView listView;
	protected View listEmptyViewNoConnection;
	protected View listEmptyViewNoContent;
	protected TextView listEmptyViewTitle;
	protected TextView listEmptyViewText;

	protected TextView listTopText;
	protected ContentArrayAdapter listAdapter;

	protected ProgressDialog progressDialog;
	protected boolean progressDialogIsVisible = false;
    protected boolean taskIsRunning = false;

    protected ActionMode contentActionMode;

	public ContentFragment() {
	}

	public abstract int getContent();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		context = getActivity();

		View view = inflater.inflate(R.layout.fragment_layout_listview, container, false);
		listView = (ListView) view.findViewById(R.id.fragment_content_listview);
		listEmptyViewNoConnection = view.findViewById(R.id.fragment_content_list_emptyview_no_connection);
		listEmptyViewNoContent = view.findViewById(R.id.fragment_content_list_no_content);
		listEmptyViewTitle = (TextView) view.findViewById(R.id.fragment_content_list_emptyview_title);
		listEmptyViewText = (TextView) view.findViewById(R.id.fragment_content_list_emptyview_text);
		listTopText = (TextView) view.findViewById(R.id.fragment_content_listview_top_text);

		Button networkRetryButton = (Button) view.findViewById(R.id.fragment_content_network_retry_button);
		networkRetryButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				updateAccountMeta();
			}
		});

		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		activityCommunicator = (ActivityCommunicator) activity;
	}

	@Override
	public void onResume() {
		super.onResume();
		FileUtilities.deleteTempFiles();
	}

	protected void setListEmptyViewText(String title, String text) {
		if (title != null) {
			listEmptyViewTitle.setText(title);
		}
		if (text != null) {
			listEmptyViewText.setText(text);
		}
		listView.setEmptyView(listEmptyViewNoContent);
	}

	protected void setTopText(String text) {

		listTopText.setVisibility(View.VISIBLE);
		listTopText.setText(text);
	}

	protected void setListEmptyViewNoNetwork(boolean visible) {
		if (visible) {
			listView.setEmptyView(listEmptyViewNoConnection);
		} else {
			listView.setEmptyView(null);
		}
	}

	protected void executeContentDeleteTask() {
		ArrayList<Object> content = listAdapter.getCheckedItems();

        contentActionMode.finish();

        ContentDeleteTask documentDeleteTask = new ContentDeleteTask(content);
		documentDeleteTask.execute();
	}

	protected void deleteContent(String message) {
        if (SettingsUtilities.getConfirmDeletePreference(context)) {
            showDeleteContentDialog(message);
        } else {
            executeContentDeleteTask();
        }
	}

    protected void showDeleteContentDialog(String message) {
        AlertDialog.Builder alertDialogBuilder = DialogUtitities.getAlertDialogBuilderWithMessage(context, message);
        alertDialogBuilder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                executeContentDeleteTask();
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

	protected class ContentDeleteTask extends AsyncTask<Void, Integer, String> {
		private ArrayList<Object> content;
		private boolean invalidToken;

		public ContentDeleteTask(ArrayList<Object> content) {
			this.content = content;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showContentProgressDialog(this, "");
		}

		@Override
		protected String doInBackground(final Void... params) {
			try {
				int progress = 0;
                System.out.println("SelectedCount: " + content.size());

				for (Object object : content) {
					publishProgress(++progress);
					ContentOperations.deleteContent(context, object);
				}

				return null;
			} catch (DigipostApiException e) {
				Log.e(getClass().getName(), e.getMessage(), e);
				return e.getMessage();
			} catch (DigipostClientException e) {
				Log.e(getClass().getName(), e.getMessage(), e);
				return e.getMessage();
			} catch (DigipostAuthenticationException e) {
				Log.e(getClass().getName(), e.getMessage(), e);
				invalidToken = true;
				return e.getMessage();
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			progressDialog.setMessage(values[0] + " av " + content.size() + " slettet");
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
			hideProgressDialog();
			updateAccountMeta();
		}

		@Override
		protected void onPostExecute(final String result) {
			super.onPostExecute(result);

			if (result != null) {
				DialogUtitities.showToast(context, result);

				if (invalidToken) {
					activityCommunicator.requestLogOut();
				}
			}

			hideProgressDialog();
			updateAccountMeta();
		}
	}

	protected void showContentProgressDialog(final AsyncTask task, String message) {
        taskIsRunning = true;
		progressDialog = DialogUtitities.getProgressDialogWithMessage(context, message);
		progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.abort), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int which) {
                dialog.dismiss();
                taskIsRunning = false;
                task.cancel(true);
            }
        });

		progressDialog.show();
	}

	protected void hideProgressDialog() {
        if(!taskIsRunning){
		    progressDialogIsVisible = false;
            if(progressDialog != null){
		        progressDialog.dismiss();
		        progressDialog = null;
            }
        }
	}

	public void filterList(String filterQuery) {
		listAdapter.getFilter().filter(filterQuery);
	}

	public void clearFilter() {
		listAdapter.clearFilter();
	}

	public abstract void updateAccountMeta();

	public interface ActivityCommunicator {
		public void onStartRefreshContent();

		public void onEndRefreshContent();

		public void requestLogOut();

		public void onUpdateAccountMeta();
	}

	protected class ContentMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener {

		@Override
		public void onItemCheckedStateChanged(ActionMode actionMode, int position, long id, boolean state) {
			listAdapter.setChecked(position);
			actionMode.setTitle(Integer.toString(listAdapter.getCheckedCount()));
		}

		@Override
		public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            contentActionMode = actionMode;

			MenuInflater inflater = actionMode.getMenuInflater();
			inflater.inflate(R.menu.activity_main_content_context, menu);

			listAdapter.setCheckboxVisible(true);

			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode actionMode, android.view.MenuItem menuItem) {
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode actionMode) {
			listAdapter.setCheckboxVisible(false);
			listAdapter.clearChecked();

            contentActionMode = null;
		}
	}

	protected class CheckBoxOnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View view) {
			int position = listView.getPositionForView(view);
			listView.setItemChecked(position, !listAdapter.getChecked(position));
		}
	}
}
