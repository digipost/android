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

import no.digipost.android.R;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ViewSwitcher;

public class BaseActivity extends FragmentActivity implements ActivityCommunicator {
	public static ImageButton refreshButton;
	public static ProgressBar refreshSpinner;

	public FragmentCommunicator fragmentCommunicator;

	private ImageButton optionsButton;
	private ImageButton logoButton;
	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ButtonListener buttonListener;
	private ViewPager mViewPager;
	private ViewSwitcher topbarSwitcher;
	private EditText searchfield;
	private ImageButton searchClose;

	private int currentViewIndex;
	private boolean isSearch;

	@Override
	protected void onCreate(final Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_base);

		isSearch = false;
		currentViewIndex = 0;
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
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setClickable(true);
		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			public void onPageSelected(final int arg0) {
				DigipostSectionFragment fragment = getFragment(currentViewIndex);
				fragment.toggleMultiselectionOff(currentViewIndex);
				currentViewIndex = arg0;
				hideSearchBar();
			}

			public void onPageScrolled(final int arg0, final float arg1, final int arg2) {

			}

			public void onPageScrollStateChanged(final int arg0) {

			}
		});

		topbarSwitcher = (ViewSwitcher) findViewById(R.id.base_topbar_switcher);
		searchClose = (ImageButton) findViewById(R.id.base_searchfield_close);
		searchClose.setOnClickListener(new ButtonListener());
		searchfield = (EditText) findViewById(R.id.base_searchfield);
		searchfield.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
				DigipostSectionFragment fragment = getFragment(currentViewIndex);
				fragment.filterList(currentViewIndex, s);
			}

			public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
				// TODO Auto-generated method stub

			}

			public void afterTextChanged(final Editable s) {
				// TODO Auto-generated method stub

			}
		});

		mSectionsPagerAdapter.notifyDataSetChanged();
	}

	private void loadAccountMeta(final int type) {
		DigipostSectionFragment fragment = getFragment(type);
		fragment.loadAccountMeta(type);
	}

	private void loadAccountMetaComplete() {
		for (int i = 0; i < 4; i++) {
			loadAccountMeta(i);
		}
	}

	private void scrollListToTop(final int type) {
		DigipostSectionFragment fragment = getFragment(type);
		fragment.scrollListToTop();
	}

	private DigipostSectionFragment getFragment(final int type) {
		return (DigipostSectionFragment) getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + type);
	}

	private void logOut() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		settings.edit().clear().commit();
		Intent i = new Intent(BaseActivity.this, LoginActivity.class);
		startActivity(i);
		finish();
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
		case R.id.basemenu_search:
			showSearchBar();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void showSearchBar() {
		if (!isSearch) {
			isSearch = true;
			searchfield.requestFocus();
			topbarSwitcher.showNext();
			new Handler().postDelayed(new Runnable() {
				public void run() {
					showKeyboard();
				}
			}, 100);

		}
	}

	private void hideSearchBar() {
		if (isSearch) {
			isSearch = false;
			searchfield.setText("");
			topbarSwitcher.showPrevious();
			hideKeyboard();
		}
	}

	private void showKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.showSoftInput(searchfield, 0);
		}
	}

	private void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.hideSoftInputFromWindow(searchfield.getWindowToken(), 0);
		}
	}

	private class ButtonListener implements OnClickListener {

		public void onClick(final View v) {
			if (v.equals(optionsButton)) {
				openOptionsMenu();
			} else if (v.equals(refreshButton)) {
				loadAccountMetaComplete();
				// loadAccountMeta(mViewPager.getCurrentItem());
			} else if (v.equals(logoButton)) {
				scrollListToTop(mViewPager.getCurrentItem());
			} else if (v.equals(searchClose)) {
				hideSearchBar();
			}
		}
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
			args.putInt(DigipostSectionFragment.ARG_SECTION_NUMBER, position);
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
				return getString(R.string.mailbox).toUpperCase();
			case 1:
				return getString(R.string.workarea).toUpperCase();
			case 2:
				return getString(R.string.archive).toUpperCase();
			case 3:
				return getString(R.string.receipts).toUpperCase();
			}
			return null;
		}
	}

	@Override
	public boolean onKeyDown(final int keyCode, final KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isSearch) {
				hideSearchBar();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	public void passDataToActivity(final String someValue) {
		if (someValue.equals("updateAll")) {
			loadAccountMetaComplete();
		}
	}
}