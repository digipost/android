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
import no.digipost.android.api.Letter;
import no.digipost.android.api.LetterOperations;
import no.digipost.android.authentication.Secret;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class BaseActivity extends FragmentActivity {

	SectionsPagerAdapter mSectionsPagerAdapter;
	ImageButton optionsButton;
	static String access_token = "";
	int REQUEST_CODE = 1;
	ViewPager mViewPager;
	Context context;

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

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		System.out.println(settings.getString("baseview" + ApiConstants.REFRESH_TOKEN, "null"));
		optionsButton = (ImageButton) findViewById(R.id.base_optionsButton);
		optionsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View arg0) {
				openOptionsMenu();
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.out.println("BaseAcitivty onDestroy");

	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.activity_base, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.base_logoutOption:
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
		private LetterListAdapter listadapter;
		private ArrayList<Letter> list;
		private boolean loaded;

		public DigipostSectionFragment() {
			loaded = false;
		}

		@Override
		public void onCreate(final Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			if (loaded) {
				return;
			}

			lo = new LetterOperations();
			list = getMailBoxLetters(getArguments().getString(ApiConstants.ACCESS_TOKEN));
			listadapter = new LetterListAdapter(getActivity(), R.layout.mailbox_list_item, list);

			System.out.println("oncreate Fraggg");
			loaded = true;

		}

		@Override
		public void onResume() {
			super.onResume();
		}

		public ArrayList<Letter> getMailBoxLetters(final String at) {
			return lo.getLetterList(at);
		}

		public void updateMailList() {
			list = getMailBoxLetters(getArguments().getString(ApiConstants.ACCESS_TOKEN));
			listadapter.notifyDataSetChanged();
		}

		@Override
		public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
			int number = getArguments().getInt(ARG_SECTION_NUMBER);

			if (number == 1) {
				View v = inflater.inflate(R.layout.fragment_layout_mailbox, container, false);
				ListView lv = (ListView) v.findViewById(R.id.listview);
				lv.setAdapter(listadapter);
				registerForContextMenu(lv);
				return v;

			} else if (number == 2) {
				View v = inflater.inflate(R.layout.fragment_layout_kitchenbench, container, false);
				TextView kt = (TextView) v.findViewById(R.id.kitchenbench_text);
				kt.setText("hei fra kjookkenet");
				return v;
			} else if (number == 3) {
				View v = inflater.inflate(R.layout.fragment_layout_archive, container, false);
				TextView at = (TextView) v.findViewById(R.id.archive_text);
				at.setText("hei fra arkivet");
				return v;
			} else {
				View v = inflater.inflate(R.layout.fragment_layout_archive, container, false);
				TextView at = (TextView) v.findViewById(R.id.archive_text);
				at.setText("hei fra kvitteringer");
				return v;
			}
		}

		@Override
		public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenuInfo menuInfo) {
			super.onCreateContextMenu(menu, v, menuInfo);
			MenuInflater mi = getActivity().getMenuInflater();
			mi.inflate(R.menu.context_menu_letterlist, menu);
		}
	}

}
