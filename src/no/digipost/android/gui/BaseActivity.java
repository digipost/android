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
import java.util.concurrent.ExecutionException;

import no.digipost.android.R;
import no.digipost.android.api.ApiConstants;
import no.digipost.android.api.LetterOperations;
import no.digipost.android.authentication.Secret;
import no.digipost.android.model.Letter;
import no.digipost.android.pdf.PDFActivity;
import no.digipost.android.pdf.PdfStore;
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
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.ListView;

public class BaseActivity extends FragmentActivity {

	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ImageButton optionsButton, refreshButton;
	private static String access_token = "";
	private final int REQUEST_CODE = 1;
	private ViewPager mViewPager;
	private Context context;
	private static ProgressDialog progressDialog;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("BaseActivity OnCreate");
		setContentView(R.layout.activity_base);
		access_token = Secret.ACCESS_TOKEN;
		progressDialog = new ProgressDialog(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setMessage("Laster innhold...");
		progressDialog.setCancelable(false);
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setOffscreenPageLimit(3);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		context = this;
		optionsButton = (ImageButton) findViewById(R.id.base_optionsButton);
		refreshButton = (ImageButton) findViewById(R.id.base_refreshButton);
		optionsButton.setOnClickListener(new OnClickListener() {
			public void onClick(final View arg0) {
				openOptionsMenu();
				refreshButton.clearAnimation();
			}
		});
		refreshButton.setOnClickListener(new OnClickListener() {
			public void onClick(final View arg0) {
				final float centerX = refreshButton.getWidth() / 2.0f;
				final float centerY = refreshButton.getHeight() / 2.0f;
				RotateAnimation a = new RotateAnimation(0, 360, centerX, centerY);
				a.setDuration(800);
				a.setRepeatCount(RotateAnimation.INFINITE);
				refreshButton.startAnimation(a);
			}
		});
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.out.println("BaseAcitivty onDestroy.");

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

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(final FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(final int position) {
			DigipostSectionFragment fragment = new DigipostSectionFragment();
			Bundle args = new Bundle();
			args.putInt(DigipostSectionFragment.ARG_SECTION_NUMBER, position + 1);
			args.putString(ApiConstants.ACCESS_TOKEN, access_token);
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

	public static class DigipostSectionFragment extends Fragment {

		public static final String ARG_SECTION_NUMBER = "section_number";
		private LetterOperations lo;
		private LetterListAdapter adapter_mailbox;
		private LetterListAdapter adapter_workarea;
		private LetterListAdapter adapter_archive;
		private LetterListAdapter adapter_receipts;
		private ArrayList<Letter> list_mailbox;
		private ArrayList<Letter> list_archive;
		private ArrayList<Letter> list_workarea;
		private ArrayList<Letter> list_receipts;

		public DigipostSectionFragment() {
		}

		@Override
		public void onCreate(final Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			lo = new LetterOperations();

			try {
				list_mailbox = new GetAccountMetaTask()
						.execute(getArguments().getString(ApiConstants.ACCESS_TOKEN), LetterOperations.INBOX)
						.get();
				list_archive = new GetAccountMetaTask().execute(getArguments().getString(ApiConstants.ACCESS_TOKEN),
						LetterOperations.ARCHIVE).get();
				list_workarea = new GetAccountMetaTask().execute(getArguments().getString(ApiConstants.ACCESS_TOKEN),
						LetterOperations.WORKAREA).get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			adapter_mailbox = new LetterListAdapter(getActivity(), R.layout.mailbox_list_item, list_mailbox);
			adapter_archive = new LetterListAdapter(getActivity(), R.layout.mailbox_list_item, list_archive);
			adapter_workarea = new LetterListAdapter(getActivity(), R.layout.mailbox_list_item, list_workarea);
			// adapter_receipts = new LetterListAdapter(getActivity(),
			// R.layout.mailbox_list_item, list_receipts);
		}

		@Override
		public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
			int number = getArguments().getInt(ARG_SECTION_NUMBER);

			if (number == 1) {
				View v = inflater.inflate(R.layout.fragment_layout_mailbox, container, false);

				ListView lv_mailbox = (ListView) v.findViewById(R.id.listview_mailbox);
				lv_mailbox.setAdapter(adapter_mailbox);
				View emptyView = v.findViewById(R.id.empty_listview_mailbox);
				lv_mailbox.setEmptyView(emptyView);

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

				/*
				 * lv_mailbox.setOnItemClickListener(new OnItemClickListener() {
				 * 
				 * public void onItemClick(final AdapterView<?> arg0, final View
				 * arg1, final int position, final long arg3) { Letter mletter =
				 * list_mailbox.get(position);
				 * 
				 * mletter.setLocation(ApiConstants.LOCATION_ARCHIVE); boolean
				 * moved =
				 * lo.moveDocument(getArguments().getString(ApiConstants.
				 * ACCESS_TOKEN), mletter); if (moved) {
				 * Toast.makeText(getActivity(), "Brev flyttet til arkiv",
				 * 3000).show(); return; } else { Toast.makeText(getActivity(),
				 * "Noe gikk galt", 3000).show(); return; } }
				 * 
				 * });
				 */

				lv_mailbox.setOnItemClickListener(new ListListener(list_mailbox));

				return v;

			} else if (number == 2) {
				View v = inflater.inflate(R.layout.fragment_layout_workarea, container, false);
				ListView lv_workarea = (ListView) v.findViewById(R.id.listview_kitchen);
				lv_workarea.setAdapter(adapter_workarea);
				lv_workarea.setOnItemClickListener(new ListListener(list_workarea));
				View emptyView = v.findViewById(R.id.empty_listview_workarea);
				lv_workarea.setEmptyView(emptyView);

				return v;
			} else if (number == 3) {
				View v = inflater.inflate(R.layout.fragment_layout_archive, container, false);
				ListView lv_archive = (ListView) v.findViewById(R.id.listview_archive);
				lv_archive.setAdapter(adapter_archive);
				lv_archive.setOnItemClickListener(new ListListener(list_archive));
				View emptyView = v.findViewById(R.id.empty_listview_archive);
				lv_archive.setEmptyView(emptyView);

				return v;
			} else {
				View v = inflater.inflate(R.layout.fragment_layout_receipts, container, false);
				ListView lv_receipts = (ListView) v.findViewById(R.id.listview_receipts);
				// lv_receipts.setAdapter(adapter_receipts);
				View emptyView = v.findViewById(R.id.empty_listview_receipts);
				lv_receipts.setEmptyView(emptyView);

				return v;
			}
		}

		private class GetAccountMetaTask extends AsyncTask<Object, Void, ArrayList<Letter>> {

			@Override
			protected ArrayList<Letter> doInBackground(final Object... params) {
				return lo.getAccountContentMeta((String) params[0], (Integer) params[1]);
			}
		}

		private class GetPDFTask extends AsyncTask<Object, Void, byte[]> {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Avbryt", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, final int which) {
						dialog.dismiss();
						cancel(true);
					}
				});
				progressDialog.show();
			}

			@Override
			protected byte[] doInBackground(final Object... params) {
				PdfStore.pdf = lo.getDocumentContentPDF((String) params[0], (Letter) params[1]);

				Intent i = new Intent(getActivity().getApplicationContext(), PDFActivity.class);
				i.putExtra(PDFActivity.INTENT_FROM, PDFActivity.FROM_MAILBOX);
				startActivity(i);

				return null;
			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
				progressDialog.dismiss();
			}

			@Override
			protected void onPostExecute(final byte[] result) {
				super.onPostExecute(result);
				progressDialog.dismiss();
			}
		}

		private class GetHTMLTask extends AsyncTask<Object, Void, String> {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Avbryt", new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog, final int which) {
						dialog.dismiss();
						cancel(true);
					}
				});
				progressDialog.show();
			}

			@Override
			protected String doInBackground(final Object... params) {
				Intent i = new Intent(getActivity(), Html_WebViewTest.class);
				i.putExtra(ApiConstants.FILETYPE_HTML, lo.getDocumentContentHTML((String) params[0], (Letter) params[1]));
				startActivity(i);

				return null;
			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
				progressDialog.dismiss();
			}

			@Override
			protected void onPostExecute(final String result) {
				super.onPostExecute(result);
				progressDialog.dismiss();
			}
		}

		private class ListListener implements OnItemClickListener {
			ArrayList<Letter> list;

			public ListListener(final ArrayList<Letter> list) {
				this.list = list;
			}

			public void onItemClick(final AdapterView<?> arg0, final View arg1, final int position, final long arg3) {
				Letter mletter = list.get(position);

				String filetype = mletter.getFileType();

				if (filetype.equals(ApiConstants.FILETYPE_PDF)) {
					GetPDFTask pdfTask = new GetPDFTask();
					pdfTask.execute(getArguments().getString(ApiConstants.ACCESS_TOKEN), mletter);
				} else if (filetype.equals(ApiConstants.FILETYPE_HTML)) {
					GetHTMLTask htmlTask = new GetHTMLTask();
					htmlTask.execute(getArguments().getString(ApiConstants.ACCESS_TOKEN), mletter);
				}
			}
		}
	}
}
