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

package no.digipost.android.gui;

import java.lang.reflect.Field;

import no.digipost.android.R;
import no.digipost.android.api.ContentOperations;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.gui.adapters.DrawerArrayAdapter;
import no.digipost.android.gui.content.SettingsActivity;
import no.digipost.android.gui.content.UploadActivity;
import no.digipost.android.gui.fragments.ArchiveFragment;
import no.digipost.android.gui.fragments.ContentFragment;
import no.digipost.android.gui.fragments.MailboxFragment;
import no.digipost.android.gui.fragments.ReceiptFragment;
import no.digipost.android.gui.fragments.WorkareaFragment;
import no.digipost.android.model.PrimaryAccount;
import no.digipost.android.utilities.ApplicationUtilities;
import no.digipost.android.utilities.DialogUtitities;
import no.digipost.android.utilities.FileUtilities;
import no.digipost.android.utilities.SharedPreferencesUtilities;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

public class MainContentActivity extends Activity implements ContentFragment.ActivityCommunicator {

	private ContentOperations contentOperations;

	private DrawerLayout drawerLayout;
	private ListView drawerList;
	private ActionBarDrawerToggle drawerToggle;
	private DrawerArrayAdapter drawerArrayAdapter;

	private boolean refreshing;

	private PrimaryAccount primaryAccount;

	private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_content);
		ApplicationUtilities.setScreenRotationFromPreferences(MainContentActivity.this);

		this.contentOperations = new ContentOperations(this);
		this.refreshing = true;

		drawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
		drawerList = (ListView) findViewById(R.id.main_left_drawer);
		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		drawerArrayAdapter = new DrawerArrayAdapter<String>(this, R.layout.drawer_list_item, ApplicationConstants.titles, 0);
		drawerList.setAdapter(drawerArrayAdapter);
		drawerList.setOnItemClickListener(new DrawerItemClickListener());
		drawerToggle = new MainContentActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer_white, R.string.open_external,
				R.string.close);
		drawerLayout.setDrawerListener(drawerToggle);

		getActionBar().setHomeButtonEnabled(true);

		if (savedInstanceState == null || getCurrentFragment() == null) {
			SharedPreferences sharedPreferences = SharedPreferencesUtilities.getSharedPreferences(this);
			int content = Integer.parseInt(sharedPreferences.getString(SettingsActivity.KEY_PREF_DEFAULT_SCREEN,
					Integer.toString(ApplicationConstants.MAILBOX)));

			selectItem(content);
		}

		onSharedPreferenceChangeListener = new SettingsChangedlistener();
		SharedPreferencesUtilities.getSharedPreferences(this).registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main_content_actionbar, menu);

		SearchView menuSearch = (SearchView) menu.findItem(R.id.menu_search).getActionView();

		setupSearchView(menuSearch);
		getActionBar().setTitle(ApplicationConstants.titles[getCurrentFragment().getContent()]);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		drawerArrayAdapter.updateDrawer(getCurrentFragment().getContent());

		MenuItem searchButton = menu.findItem(R.id.menu_search);
		searchButton.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
			@Override
			public boolean onMenuItemActionExpand(MenuItem menuItem) {
				return true;
			}

			@Override
			public boolean onMenuItemActionCollapse(MenuItem menuItem) {
				getCurrentFragment().clearFilter();
				return true;
			}
		});

		MenuItem refreshButton = menu.findItem(R.id.menu_refresh);

		if (refreshing) {
			refreshButton.setActionView(R.layout.activity_main_content_refreshspinner);
		} else {
			refreshButton.setActionView(null);
		}
		boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
		MenuItem uploadButton = menu.findItem(R.id.menu_upload);
		uploadButton.setVisible(!drawerOpen);
		searchButton.setVisible(!drawerOpen);
		refreshButton.setVisible(!drawerOpen);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		switch (item.getItemId()) {
		case R.id.menu_refresh:
			getCurrentFragment().updateAccountMeta();
			return true;
		case R.id.menu_logout:
			logOut();
			return true;
		case R.id.menu_help:
			openHelpWebView();
			return true;
		case R.id.menu_upload:
			startUploadActivity();
			return true;
		case R.id.menu_preferences:
			startPreferemcesActivity();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onStartRefreshContent() {
		refreshing = true;
		invalidateOptionsMenu();
	}

	@Override
	public void onEndRefreshContent() {
		refreshing = false;
		invalidateOptionsMenu();
	}

	@Override
	public void requestLetterOperations() {
		getCurrentFragment().setContentOperations(contentOperations);
	}

	@Override
	public void requestLogOut() {
		logOut();
	}

	@Override
	public void onUpdateAccountMeta() {
		executeGetPrimaryAccountTask();
	}

	private class SettingsChangedlistener implements SharedPreferences.OnSharedPreferenceChangeListener {

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (key.equals(SettingsActivity.KEY_PREF_SHOW_BANK_ID_DOCUMENTS) && getCurrentFragment() != null) {
				getCurrentFragment().updateAccountMeta();
			} else if (key.equals(SettingsActivity.KEY_PREF_SCREEN_ROTATION)) {
				ApplicationUtilities.setScreenRotationFromPreferences(MainContentActivity.this);
			}
		}
	}

	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);
		}
	}

	private void selectItem(int content) {
		ContentFragment contentFragment = null;

		switch (content) {
		case ApplicationConstants.MAILBOX:
			contentFragment = new MailboxFragment();
			break;
		case ApplicationConstants.WORKAREA:
			contentFragment = new WorkareaFragment();
			break;
		case ApplicationConstants.ARCHIVE:
			contentFragment = new ArchiveFragment();
			break;
		case ApplicationConstants.RECEIPTS:
			contentFragment = new ReceiptFragment();
			break;
		}

		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.main_content_frame, contentFragment).commit();

		drawerList.setItemChecked(content, true);
		drawerLayout.closeDrawer(drawerList);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}

	public void updateUI() {
		getActionBar().setSubtitle(primaryAccount.getFullName());
		drawerArrayAdapter.setUnreadLetters(primaryAccount.getUnreadItemsInInbox());
	}

	private ContentFragment getCurrentFragment() {
		return (ContentFragment) getFragmentManager().findFragmentById(R.id.main_content_frame);
	}

	private void logOut() {
		FileUtilities.deleteTempFiles();
		SharedPreferencesUtilities.deleteRefreshtoken(this);
		SharedPreferencesUtilities.deleteScreenlockChoice(this);

		Intent intent = new Intent(MainContentActivity.this, LoginActivity.class);
		startActivity(intent);
		finish();
	}

	private void startUploadActivity() {
		Intent intent = new Intent(MainContentActivity.this, UploadActivity.class);
		startActivity(intent);
	}

	private void startPreferemcesActivity() {
		Intent intent = new Intent(MainContentActivity.this, SettingsActivity.class);
		startActivity(intent);
	}

	private void openHelpWebView() {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.digipost.no/hjelp/#android"));
		startActivity(browserIntent);
	}

	private void setupSearchView(SearchView searchView) {

		try {
			Field searchField = SearchView.class.getDeclaredField("mSearchButton");
			searchField.setAccessible(true);

			android.widget.ImageView searchBtn = (android.widget.ImageView) searchField.get(searchView);
			searchBtn.setImageResource(R.drawable.white_search_48);

			searchField = SearchView.class.getDeclaredField("mSearchPlate");
			searchField.setAccessible(true);

			LinearLayout searchPlate = (LinearLayout) searchField.get(searchView);

			AutoCompleteTextView searchTextView = (AutoCompleteTextView) searchPlate.getChildAt(0);

			searchTextView.setTextColor(getResources().getColor(R.color.white));
			searchPlate.setBackgroundResource(R.drawable.search_background);

			searchTextView.setHintTextColor(getResources().getColor(R.color.searchbar_grey_hint));
			searchView.setQueryHint(getString(R.string.search_in) + ApplicationConstants.titles[getCurrentFragment().getContent()]);

			android.widget.ImageView searchViewClearButton = (android.widget.ImageView) searchPlate.getChildAt(1);
			searchViewClearButton.setImageResource(R.drawable.ic_clear_white);

			searchView.setOnQueryTextListener(new SearchViewOnQueryTextListener());
		} catch (NoSuchFieldException e) {
		} catch (IllegalAccessException e) {
		}
	}

	private class SearchViewOnQueryTextListener implements android.widget.SearchView.OnQueryTextListener {

		@Override
		public boolean onQueryTextSubmit(String s) {
			return false;
		}

		@Override
		public boolean onQueryTextChange(String s) {
			getCurrentFragment().filterList(s);
			return true;
		}
	}

	private class MainContentActionBarDrawerToggle extends ActionBarDrawerToggle {

		public MainContentActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, int drawerImageRes,
				int openDrawerContentDescRes, int closeDrawerContentDescRes) {
			super(activity, drawerLayout, drawerImageRes, openDrawerContentDescRes, closeDrawerContentDescRes);
		}

		public void onDrawerClosed(View view) {
			invalidateOptionsMenu();
		}

		public void onDrawerOpened(View drawerView) {
			invalidateOptionsMenu();
		}
	}

	private void executeGetPrimaryAccountTask() {
		GetPrimaryAccountTask getPrimaryAccountTask = new GetPrimaryAccountTask();
		getPrimaryAccountTask.execute();
	}

	private class GetPrimaryAccountTask extends AsyncTask<Void, Void, PrimaryAccount> {
		private String errorMessage;
		private boolean invalidToken;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			onStartRefreshContent();
		}

		@Override
		protected PrimaryAccount doInBackground(Void... voids) {
			try {
				return contentOperations.getPrimaryAccount();
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
		protected void onPostExecute(PrimaryAccount result) {
			super.onPostExecute(primaryAccount);

			if (result != null) {
				primaryAccount = result;
				updateUI();
			} else {
				if (invalidToken) {
					DialogUtitities.showToast(MainContentActivity.this, errorMessage);
					logOut();
				}
			}
		}
	}
}
