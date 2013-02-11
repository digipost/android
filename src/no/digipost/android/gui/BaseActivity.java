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
import no.digipost.android.api.LetterOperations;
import no.digipost.android.authentication.Secret;
import no.digipost.android.model.Letter;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.Toast;

public class BaseActivity extends FragmentActivity {

	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ImageButton optionsButton;
	private static String access_token = "";
	private final int REQUEST_CODE = 1;
	private ViewPager mViewPager;
	private Context context;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("BaseActivity OnCreate");
		setContentView(R.layout.activity_base);
		access_token = Secret.ACCESS_TOKEN;
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setOffscreenPageLimit(3);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		context = this;
		optionsButton = (ImageButton) findViewById(R.id.base_optionsButton);
		optionsButton.setOnClickListener(new OnClickListener() {
			public void onClick(final View arg0) {
				openOptionsMenu();
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
		private String[] seleced_checkboxes;

		public DigipostSectionFragment() {
		}

		@Override
		public void onCreate(final Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			lo = new LetterOperations();

			list_mailbox = getMailBoxLetters(getArguments().getString(ApiConstants.ACCESS_TOKEN));
			list_archive = getArchiveLetters(getArguments().getString(ApiConstants.ACCESS_TOKEN));
			list_workarea = getWorkareaLetters(getArguments().getString(ApiConstants.ACCESS_TOKEN));
			// list_receipts =
			// getReceiptsLetters(getArguments().getString(ApiConstants.ACCESS_TOKEN));

			adapter_mailbox = new LetterListAdapter(getActivity(), R.layout.mailbox_list_item, list_mailbox);
			adapter_archive = new LetterListAdapter(getActivity(), R.layout.mailbox_list_item, list_archive);
			adapter_workarea = new LetterListAdapter(getActivity(), R.layout.mailbox_list_item, list_workarea);
			// adapter_receipts = new LetterListAdapter(getActivity(),
			// R.layout.mailbox_list_item, list_receipts);
		}

		public ArrayList<Letter> getMailBoxLetters(final String at) {
			return lo.getMailboxList(at);
		}

		public ArrayList<Letter> getArchiveLetters(final String at) {
			return lo.getArchiveList(at);
		}

		public ArrayList<Letter> getWorkareaLetters(final String at) {
			return lo.getWorkareaList(at);
		}

		public ArrayList<Letter> getReceiptsLetters(final String at) {
			return lo.getReceiptsList(at);
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

					public boolean onItemLongClick(final AdapterView<?> arg0, final View arg1, final int arg2, final long arg3) {
						LetterListAdapter.showboxes = true;
						seleced_checkboxes = new String[list_mailbox.size()];
						adapter_mailbox.notifyDataSetChanged();
						return true;
					}
				});
				lv_mailbox.setOnKeyListener(new OnKeyListener() {
					public boolean onKey(final View v, final int keyCode, final KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_BACK) {
							if (LetterListAdapter.showboxes == true) {
								LetterListAdapter.showboxes = false;
								seleced_checkboxes = null;
								adapter_mailbox.notifyDataSetChanged();
								return true;
							}
							return false;
						}
						return false;
					}
				});

				lv_mailbox.setOnItemClickListener(new OnItemClickListener() {

					public void onItemClick(final AdapterView<?> arg0, final View arg1, final int position, final long arg3) {
						Letter mletter = list_mailbox.get(position);
						mletter.setLocation(ApiConstants.LOCATION_ARCHIVE);

						//StringEntity se = JSONConverter.createJsonFromJackson(mletter);
						//boolean moved = lo.moveDocument(getArguments().getString(ApiConstants.ACCESS_TOKEN), mletter);
						boolean moved = true;
						if (moved) {
							Toast.makeText(getActivity(), "Brev flyttet til arkiv", 3000).show();
							return;
						} else {
							Toast.makeText(getActivity(), "Noe gikk galt", 3000).show();
							return;
						}
					}

				});

				return v;

			} else if (number == 2) {
				View v = inflater.inflate(R.layout.fragment_layout_kitchenbench, container, false);
				ListView lv_kitchenbench = (ListView) v.findViewById(R.id.listview_kitchen);
				lv_kitchenbench.setAdapter(adapter_workarea);
				View emptyView = v.findViewById(R.id.empty_listview_workarea);
				lv_kitchenbench.setEmptyView(emptyView);

				return v;
			} else if (number == 3) {
				View v = inflater.inflate(R.layout.fragment_layout_archive, container, false);
				ListView lv_archive = (ListView) v.findViewById(R.id.listview_archive);
				lv_archive.setAdapter(adapter_archive);
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
	}
}
