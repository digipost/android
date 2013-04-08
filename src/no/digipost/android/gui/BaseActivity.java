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
import no.digipost.android.api.LetterOperations;
import no.digipost.android.authentication.Secret;
import no.digipost.android.pdf.PDFStore;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
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
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

public class BaseActivity extends FragmentActivity implements ActivityCommunicator {
	public static ImageButton refreshButton;
	public static ProgressBar refreshSpinner;

	public FragmentCommunicator fragmentCommunicator;

	private ImageButton optionsButton;
	private ImageButton logoButton;
	private ImageButton searchButton;
	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ButtonListener buttonListener;
	private ViewPager mViewPager;
	private ViewSwitcher topbarSwitcher;
	private EditText searchfield;
	private ImageButton searchClose;
	private Drawable deleteTextImage;

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
		searchButton = (ImageButton) findViewById(R.id.base_searchButton);
		logoButton = (ImageButton) findViewById(R.id.base_logoButton);
		buttonListener = new ButtonListener();
		optionsButton.setOnClickListener(buttonListener);
		refreshButton.setOnClickListener(buttonListener);
		logoButton.setOnClickListener(buttonListener);
		searchButton.setOnClickListener(buttonListener);
		deleteTextImage = getResources().getDrawable(R.drawable.white_clear_32);
		deleteTextImage.setBounds(new Rect(0, 0, deleteTextImage.getIntrinsicWidth(), deleteTextImage.getIntrinsicHeight()));
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setClickable(true);
		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			public void onPageSelected(final int arg0) {
				DigipostSectionFragment fragment = getFragment(currentViewIndex);
				if (fragment.checkboxesVisible) {
					fragment.toggleMultiselectionOff(currentViewIndex);
				}
				if (isSearch) {
					hideSearchBar();
					fragment.clearFilter(currentViewIndex);
				}
				currentViewIndex = arg0;

			}

			public void onPageScrolled(final int arg0, final float arg1, final int arg2) {
				// Not implemented.
			}

			public void onPageScrollStateChanged(final int arg0) {
				// Not implemented.
			}
		});

		topbarSwitcher = (ViewSwitcher) findViewById(R.id.base_topbar_switcher);
		searchClose = (ImageButton) findViewById(R.id.base_searchfield_close);
		searchClose.setOnClickListener(buttonListener);
		searchfield = (EditText) findViewById(R.id.base_searchfield);
		searchfield.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
				DigipostSectionFragment fragment = getFragment(currentViewIndex);
				fragment.filterList(currentViewIndex, s);

				if (s.length() != 0) {
					searchfield.setCompoundDrawables(null, null, deleteTextImage, null);
				} else {
					searchfield.setCompoundDrawables(null, null, null, null);
				}
			}

			public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
				// Not implemented.
			}

			public void afterTextChanged(final Editable s) {
				// Not implemented.
			}
		});

		searchfield.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(final View v, final MotionEvent event) {
				Drawable co = ((TextView) v).getCompoundDrawables()[2];
				if (co == null) {
					return false;
				}
				if (event.getAction() != MotionEvent.ACTION_DOWN) {
					return false;
				}
				if (event.getX() > v.getMeasuredWidth() - v.getPaddingRight() - co.getIntrinsicWidth()) {
					searchfield.setText("");
					return true;
				} else {
					return false;
				}
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
		Secret.ACCESS_TOKEN = null;
		PDFStore.pdf = null;
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
		//if (isSearch) {
		isSearch = false;
		searchfield.setText("");
		topbarSwitcher.showPrevious();
		hideKeyboard();

	}

	private void showKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.showSoftInput(searchfield, 0);
		}
	}

	public void showMenu(final View v) {
		PopupMenu popup = new PopupMenu(this, v);
		popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			public boolean onMenuItemClick(final MenuItem item) {
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
				}
				return false;
			}
		});
		popup.inflate(R.menu.activity_base);
		popup.show();
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
				showMenu(v);
			} else if (v.equals(refreshButton)) {
				loadAccountMetaComplete();
			} else if (v.equals(logoButton)) {
				scrollListToTop(mViewPager.getCurrentItem());
			} else if (v.equals(searchClose)) {
				hideSearchBar();
			} else if (v.equals(searchButton)) {
				switch (currentViewIndex) {
				case LetterOperations.MAILBOX:
					searchfield.setHint(R.string.search_mailbox);
					break;
				case LetterOperations.WORKAREA:
					searchfield.setHint(R.string.search_workarea);
					break;
				case LetterOperations.ARCHIVE:
					searchfield.setHint(R.string.search_archive);
					break;
				case LetterOperations.RECEIPTS:
					searchfield.setHint(R.string.search_receipts);
					break;
				}
				showSearchBar();
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

		@SuppressLint("DefaultLocale")
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
		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
		}
		return super.onKeyDown(keyCode, event);
	}

	public void passDataToActivity(final String message) {
		if (message.equals(DigipostSectionFragment.BASE_UPDATE_ALL)) {
			loadAccountMetaComplete();
		} else if (message.equals(DigipostSectionFragment.BASE_INVALID_TOKEN)) {
			showMessage(this.getString(R.string.error_invalid_token));
			logOut();
		}
	}
}