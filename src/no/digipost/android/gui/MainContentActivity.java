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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

import com.google.analytics.tracking.android.EasyTracker;

import java.lang.reflect.Field;
import java.util.ArrayList;

import no.digipost.android.R;
import no.digipost.android.api.ContentOperations;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.gui.adapters.DrawerArrayAdapter;
import no.digipost.android.gui.adapters.MailboxArrayAdapter;
import no.digipost.android.gui.content.SettingsActivity;
import no.digipost.android.gui.content.UploadActivity;
import no.digipost.android.gui.fragments.ContentFragment;
import no.digipost.android.gui.fragments.DocumentFragment;
import no.digipost.android.gui.fragments.ReceiptFragment;
import no.digipost.android.model.Account;
import no.digipost.android.model.Folder;
import no.digipost.android.model.Mailbox;
import no.digipost.android.utilities.ApplicationUtilities;
import no.digipost.android.utilities.DialogUtitities;
import no.digipost.android.utilities.FileUtilities;
import no.digipost.android.utilities.SettingsUtilities;
import no.digipost.android.utilities.SharedPreferencesUtilities;

public class MainContentActivity extends Activity implements ContentFragment.ActivityCommunicator {
	public static final int INTENT_REQUESTCODE = 0;

	private DrawerLayout drawerLayout;
	private ListView drawerList;
	private ActionBarDrawerToggle drawerToggle;
	private DrawerArrayAdapter drawerArrayAdapter;
    protected MailboxArrayAdapter mailboxAdapter;
	private SearchView searchView;
    private MenuItem searchButton;
	private boolean refreshing;
    private static String[] drawerListitems;
    public static ArrayList<Folder> folders;
    private Dialog mailboxDialog;
    public static int numberOfFolders;
    private boolean showActionBarName;
    private Mailbox mailbox;
    private ArrayList<Mailbox> mailboxes;
    private Account account;
    public static String fragmentName;

	private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main_content);
		ApplicationUtilities.setScreenRotationFromPreferences(MainContentActivity.this);
		this.refreshing = true;
		drawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
		drawerList = (ListView) findViewById(R.id.main_left_drawer);
		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        updateUI();
        drawerList.setOnItemClickListener(new DrawerItemClickListener());
		drawerToggle = new MainContentActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer_white, R.string.open,
				R.string.close);
		drawerLayout.setDrawerListener(drawerToggle);
		getActionBar().setHomeButtonEnabled(true);

        selectItem(ApplicationConstants.MAILBOX);

		onSharedPreferenceChangeListener = new SettingsChangedlistener();
		SharedPreferencesUtilities.getSharedPreferences(this).registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);

		if (SharedPreferencesUtilities.numberOfTimesAppHasRun(this) <= ApplicationConstants.NUMBER_OF_TIMES_DRAWER_SHOULD_OPEN) {
			drawerLayout.openDrawer(GravityCompat.START);
		}
	}

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this);
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main_content_actionbar, menu);
		searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();

        setupSearchView(searchView);
        updateTitles();

		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		drawerArrayAdapter.updateDrawer(getCurrentFragment().getContent());

		searchButton = menu.findItem(R.id.menu_search);
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
        if(getCurrentFragment().getContent()==ApplicationConstants.RECEIPTS){
            uploadButton.setVisible(false);
        }else {
            uploadButton.setVisible(!drawerOpen);
        }
        refreshButton.setVisible(!drawerOpen);
		searchButton.setVisible(!drawerOpen);

		return super.onPrepareOptionsMenu(menu);
	}

    @Override
    public boolean onSearchRequested() {
        searchButton.expandActionView();
        return false;
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
        case R.id.menu_upload:
                startUploadActivity();
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
	public void requestLogOut() {
		logOut();
	}

	@Override
	public void onUpdateAccountMeta() {
		executeGetAccountTask();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			if (requestCode == INTENT_REQUESTCODE) {
				String action = data.getStringExtra(ApiConstants.FRAGMENT_ACTIVITY_RESULT_ACTION);

                if(action.equals(ApiConstants.UPLOAD)){
                    selectItem(getCurrentFragment().getContent());
                }else if (action.equals(ApiConstants.LOGOUT)) {
					logOut();
				}
			}
		}
	}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            int defaultScreen = Integer.parseInt(SettingsUtilities.getDefaultScreenPreference(this));

            if(getCurrentFragment().getContent() != defaultScreen){
                selectItem(defaultScreen);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
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

    private void selectMailbox(String digipostAddress,String name){
        if (ContentOperations.changeMailbox(digipostAddress)) {
            getActionBar().setTitle(name);
            account = null;
            executeGetAccountTask();
            selectItem(ApplicationConstants.MAILBOX);
        }
    }

    private void openMailboxSelection(){
        mailboxDialog = null;

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.attachmentdialog_layout, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this).setNegativeButton(getString(R.string.abort),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.setView(view);

        ListView moveToFolderListView = (ListView) view.findViewById(R.id.attachmentdialog_listview);

        mailboxAdapter = new MailboxArrayAdapter(this, R.layout.attachmentdialog_list_item, mailboxes);
        moveToFolderListView.setAdapter(mailboxAdapter);
        moveToFolderListView.setOnItemClickListener(new ChangeMailboxListOnItemClickListener());

        builder.setTitle(ApplicationConstants.DRAWER_CHANGE_ACCOUNT);
        mailboxDialog = builder.create();
        mailboxDialog.show();

    }

    private class ChangeMailboxListOnItemClickListener implements AdapterView.OnItemClickListener {
        public ChangeMailboxListOnItemClickListener() {}

        public void onItemClick(final AdapterView<?> arg0, final View arg1, final int position, final long arg3) {
            Mailbox mailbox = mailboxAdapter.getItem(position);
            selectMailbox(mailbox.getDigipostaddress(),mailbox.getName());
            mailboxDialog.dismiss();
            mailboxDialog = null;
        }
    }

	private void selectItem(int content) {
        ContentFragment contentFragment = new DocumentFragment(ApplicationConstants.MAILBOX);

        if(account != null) {
            try {

                int inboxReceiptsAndFolders = (numberOfFolders+ApplicationConstants.numberOfStaticFolders);

                if (content == ApplicationConstants.MAILBOX) {
                    contentFragment = new DocumentFragment(ApplicationConstants.MAILBOX);

                } else if (content == ApplicationConstants.RECEIPTS) {
                    contentFragment = new ReceiptFragment();

                } else if (content > ApplicationConstants.FOLDERS_LABEL && content < inboxReceiptsAndFolders ) {
                    contentFragment = new DocumentFragment(content);

                }else if(drawerListitems[content].equals(ApplicationConstants.DRAWER_CHANGE_ACCOUNT)) {
                    openMailboxSelection();
                    return;

                }else if(drawerListitems[content].equals(ApplicationConstants.DRAWER_SETTINGS)) {
                    startPreferencesActivity();
                    return;

                }else if(drawerListitems[content].equals(ApplicationConstants.DRAWER_HELP)) {
                    openHelpWebView();
                    return;

                }else if(drawerListitems[content].equals(ApplicationConstants.DRAWER_LOGOUT)) {
                    logOut();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    private void updateUI(){
        updateDrawer();
        updateTitles();

    }

    private void updateTitles(){
        if(account != null) {
            fragmentName = "";
            try {
                if(showActionBarName){
                    fragmentName = mailbox.getName();
                }else {
                    if (getActionBar().getTitle().equals("Digipost")) {

                    } else if (drawerListitems[getCurrentFragment().getContent()].equals(ApplicationConstants.DRAWER_INBOX)) {
                        fragmentName = mailbox.getName();
                    } else {
                        fragmentName = drawerListitems[getCurrentFragment().getContent()];
                    }

                    getActionBar().setTitle(fragmentName);
                }
            } catch (NullPointerException e) {
                //IGNORE
            }
        }
    }

    private void updateDrawer(){
        folders = new ArrayList<Folder>();

        ArrayList<String> drawerItems = new ArrayList<String>();

        //Add main menu
        drawerItems.add(ApplicationConstants.DRAWER_INBOX);
        drawerItems.add(ApplicationConstants.DRAWER_RECEIPTS);
        drawerItems.add(ApplicationConstants.DRAWER_MY_FOLDERS);

        ArrayList<Folder> fs = null;

        if(account != null) {

            //Add folders
            mailbox = account.getMailboxByDigipostAddress(ContentOperations.digipostAddress);

            if (mailbox != null) {
                fs = mailbox.getFolders().getFolder();
                numberOfFolders = 0;
                for (int i = 0; i < fs.size(); i++) {
                    String name = fs.get(i).getName();
                    drawerItems.add(name);
                    folders.add(fs.get(i));
                    numberOfFolders++;
                }
            }

            //Add account settings
            drawerItems.add(ApplicationConstants.DRAWER_MY_ACCOUNT);

            mailboxes = account.getMailbox();

            if (mailboxes.size() > 1) {
                drawerItems.add(ApplicationConstants.DRAWER_CHANGE_ACCOUNT);
            }

            drawerItems.add(ApplicationConstants.DRAWER_SETTINGS);
            drawerItems.add(ApplicationConstants.DRAWER_HELP);
            drawerItems.add(ApplicationConstants.DRAWER_LOGOUT);

        }

        //Add items to drawer
        drawerListitems = new String[drawerItems.size()];
        drawerListitems = drawerItems.toArray(drawerListitems);

        drawerArrayAdapter = new DrawerArrayAdapter<String>(this, R.layout.drawer_list_item, drawerListitems,fs, 0);
        drawerList.setAdapter(drawerArrayAdapter);

        if(mailbox!= null) {
            drawerArrayAdapter.setUnreadLetters(mailbox.getUnreadItemsInInbox());
        }
    }

	private ContentFragment getCurrentFragment() {
		return (ContentFragment) getFragmentManager().findFragmentById(R.id.main_content_frame);
	}

	private void logOut() {
		FileUtilities.deleteTempFiles();
		SharedPreferencesUtilities.deleteRefreshtoken(this);
		SharedPreferencesUtilities.deleteScreenlockChoice(this);
        ContentOperations.resetState();
        mailbox = null;
        account = null;
		Intent intent = new Intent(MainContentActivity.this, LoginActivity.class);
		startActivity(intent);
		finish();
	}

	private void startUploadActivity() {
		Intent intent = new Intent(MainContentActivity.this, UploadActivity.class);
        intent.putExtra(ApiConstants.UPLOAD,getCurrentFragment().getContent());
		startActivityForResult(intent, INTENT_REQUESTCODE);
	}

	private void startPreferencesActivity() {
		Intent intent = new Intent(MainContentActivity.this, SettingsActivity.class);
		startActivityForResult(intent, INTENT_REQUESTCODE);
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
			searchView.setQueryHint(getString(R.string.search_in) + drawerListitems[getCurrentFragment().getContent()]);

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
            showActionBarName = false;
			invalidateOptionsMenu();

		}

		public void onDrawerOpened(View drawerView) {
            showActionBarName = true;
			invalidateOptionsMenu();
		}
	}

	private void executeGetAccountTask() {
		GetAccountTask getAccountTask = new GetAccountTask();
		getAccountTask.execute();
	}

	private class GetAccountTask extends AsyncTask<Void, Void, Account> {
		private String errorMessage;
		private boolean invalidToken;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Account doInBackground(Void... voids) {
			try {
				return ContentOperations.getAccountUpdated(MainContentActivity.this);
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
		protected void onPostExecute(Account result) {
			super.onPostExecute(result);

			if (result != null) {
                account = result;
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
