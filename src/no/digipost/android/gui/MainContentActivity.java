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
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

import com.google.analytics.tracking.android.EasyTracker;
import com.terlici.dragndroplist.DragNDropListView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import no.digipost.android.R;
import no.digipost.android.api.ContentOperations;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.gui.adapters.DrawerAdapter;
import no.digipost.android.gui.adapters.MailboxArrayAdapter;
import no.digipost.android.gui.content.SettingsActivity;
import no.digipost.android.gui.content.UploadActivity;
import no.digipost.android.gui.fragments.ContentFragment;
import no.digipost.android.gui.fragments.DocumentFragment;
import no.digipost.android.gui.fragments.EditFolderFragment;
import no.digipost.android.gui.fragments.ReceiptFragment;
import no.digipost.android.model.Account;
import no.digipost.android.model.Folder;
import no.digipost.android.model.Mailbox;
import no.digipost.android.utilities.ApplicationUtilities;
import no.digipost.android.utilities.DialogUtitities;
import no.digipost.android.utilities.FileUtilities;
import no.digipost.android.utilities.SettingsUtilities;
import no.digipost.android.utilities.SharedPreferencesUtilities;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;

public class MainContentActivity extends Activity implements ContentFragment.ActivityCommunicator {
    public static final int INTENT_REQUESTCODE = 0;

    private DrawerLayout drawerLayout;
    private int drawerUpdates;
    private DragNDropListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerAdapter drawerArrayAdapter;
    protected MailboxArrayAdapter mailboxAdapter;
    private MenuItem searchButton;
    private boolean refreshing;
    public static boolean editDrawerMode;
    private static String[] drawerListItems;
    private Dialog mailboxDialog;
    private boolean showActionBarName;
    private Mailbox mailbox;
    private ArrayList<Mailbox> mailboxes;
    private Account account;

    public static int numberOfFolders;
    public static String fragmentName;
    public static ArrayList<Folder> folders;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_content);
        ApplicationUtilities.setScreenRotationFromPreferences(MainContentActivity.this);
        drawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        drawerList = (DragNDropListView) findViewById(R.id.main_left_drawer);
        drawerList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        this.refreshing = true;
        drawerUpdates = 0;
        editDrawerMode = false;
        updateUI(false);

        drawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (account != null && position == (numberOfFolders + ApplicationConstants.numberOfStaticFolders)) {
                    showCreateEditDialog(position, false);
                } else if (editDrawerMode) {
                    showCreateEditDialog(position, true);
                } else {
                    selectItem(position);
                }
            }
        });

        drawerList.setOnItemLongClickListener(new ListView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                editDrawerMode = !editDrawerMode;
                if (editDrawerMode) {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
                } else {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                }
                updateUI(false);
                return true;
            }
        });

        drawerList.setOnItemDragNDropListener(new DragNDropListView.OnItemDragNDropListener() {
            @Override
            public void onItemDrag(DragNDropListView parent, View view, int position, long id) {
                view.setBackgroundResource(R.color.main_drawer_hover);
            }

            @Override
            public void onItemDrop(DragNDropListView parent, View view, int startPosition, int endPosition, long id) {
                moveFolderFrom(startPosition, endPosition);

            }
        });


        drawerToggle = new MainContentActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer_white, R.string.open,
                R.string.close);
        drawerLayout.setDrawerListener(drawerToggle);

        getActionBar().setHomeButtonEnabled(true);

        selectItem(ApplicationConstants.MAILBOX);

        SharedPreferencesUtilities.getSharedPreferences(this).registerOnSharedPreferenceChangeListener(new SettingsChangedlistener());

        if (SharedPreferencesUtilities.numberOfTimesAppHasRun(this) <= ApplicationConstants.NUMBER_OF_TIMES_DRAWER_SHOULD_OPEN) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
        invalidateOptionsMenu();
    }

    private void checkAppDeprecation() {
        try {
            if (account != null) {
                int versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
                int minimumAndroidVersion = account.getMinimumAndroidVersion();

                if (account != null && versionCode < minimumAndroidVersion) {
                    DialogUtitities.showLongToast(this, getString(R.string.app_deprecation_message));
                    Uri marketUri = Uri.parse(getString(R.string.app_deprecation_market_url));
                    Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
                    startActivity(marketIntent);
                    finish();
                }
            }
        } catch (Exception e) {
            //IGNORE
            e.printStackTrace();
        }
    }

    private void moveFolderFrom(int startPosition, int endPosition) {

        startPosition -= ApplicationConstants.numberOfStaticFolders;
        endPosition -= ApplicationConstants.numberOfStaticFolders;

        if (startPosition < 0 || startPosition >= folders.size()) {
            return;
        }

        if (endPosition < 0) {
            endPosition = 0;
        }
        if (endPosition > folders.size() - 1) {
            endPosition = folders.size() - 1;
        }

        folders.add(endPosition, folders.remove(startPosition));
        drawerUpdates++;
        updateUI(true);
        executeUpdateFoldersTask();
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
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();

        setupSearchView(searchView);
        updateUI(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        drawerArrayAdapter.notifyDataSetChanged();
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

        MenuItem doneEditingButton = menu.findItem(R.id.menu_done_edit_folder);
        MenuItem refreshButton = menu.findItem(R.id.menu_refresh);

        if (refreshing) {
            refreshButton.setActionView(R.layout.activity_main_content_refreshspinner);
        } else {
            refreshButton.setActionView(null);
        }

        boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
        MenuItem uploadButton = menu.findItem(R.id.menu_upload);

        if (getCurrentFragment().getContent() == ApplicationConstants.RECEIPTS) {
            uploadButton.setVisible(false);
        } else {
            uploadButton.setVisible(!drawerOpen);
        }
        refreshButton.setVisible(!drawerOpen);
        searchButton.setVisible(!drawerOpen);

        if (editDrawerMode && drawerOpen) {
            doneEditingButton.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            doneEditingButton.setVisible(true);
        } else {
            doneEditingButton.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            doneEditingButton.setVisible(false);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
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
            case R.id.menu_done_edit_folder:
                editDrawerMode = false;
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                updateUI(false);

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

                if (action.equals(ApiConstants.UPLOAD)) {
                    selectItem(getCurrentFragment().getContent());
                } else if (action.equals(ApiConstants.LOGOUT)) {
                    logOut();
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            int defaultScreen = Integer.parseInt(SettingsUtilities.getDefaultScreenPreference(this));

            if (getCurrentFragment().getContent() != defaultScreen) {
                selectItem(defaultScreen);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private class SettingsChangedlistener implements OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(SettingsActivity.KEY_PREF_SHOW_BANK_ID_DOCUMENTS) && getCurrentFragment() != null) {
                getCurrentFragment().updateAccountMeta();
            } else if (key.equals(SettingsActivity.KEY_PREF_SCREEN_ROTATION)) {
                ApplicationUtilities.setScreenRotationFromPreferences(MainContentActivity.this);
            }
        }
    }

    private void selectMailbox(String digipostAddress, String name) {
        if (ContentOperations.changeMailbox(digipostAddress)) {
            getActionBar().setTitle(name);
            account = null;
            editDrawerMode = false;
            executeGetAccountTask();
            selectItem(ApplicationConstants.MAILBOX);
        }
    }

    private void openMailboxSelection() {
        mailboxDialog = null;

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.attachmentdialog_layout, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this).setNegativeButton(getString(R.string.abort),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );

        builder.setView(view);
        ListView mailboxListView = (ListView) view.findViewById(R.id.attachmentdialog_listview);

        mailboxAdapter = new MailboxArrayAdapter(this, R.layout.attachmentdialog_list_item, mailboxes);
        mailboxListView.setAdapter(mailboxAdapter);
        mailboxListView.setOnItemClickListener(new ChangeMailboxListOnItemClickListener());

        builder.setTitle(getResources().getString(R.string.drawer_change_account));
        mailboxDialog = builder.create();
        mailboxDialog.show();

    }

    private class ChangeMailboxListOnItemClickListener implements AdapterView.OnItemClickListener {
        public ChangeMailboxListOnItemClickListener() {
        }

        public void onItemClick(final AdapterView<?> arg0, final View arg1, final int position, final long arg3) {
            Mailbox mailbox = mailboxAdapter.getItem(position);
            selectMailbox(mailbox.getDigipostaddress(), mailbox.getName());
            if (mailboxDialog != null) {
                mailboxDialog.dismiss();
                mailboxDialog = null;
            }
        }
    }

    private void showCreateEditDialog(int content, boolean editFolder) {

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("editFolderFragment");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        DialogFragment editFolderFragment = EditFolderFragment.newInstance(content, account.getValidationRules().getFolderName(), editFolder);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        editFolderFragment.show(ft, "editFolderFragment");
    }

    public void saveEditFolder(Folder folder, int folderIndex) {
        folders.set(folderIndex, folder);
        updateUI(false);
        executeCreateEditDeleteFolderTask(folder, ApiConstants.EDIT);
    }

    public void createFolder(Folder folder) {
        executeCreateEditDeleteFolderTask(folder, ApiConstants.CREATE);
    }

    public void deleteEditFolder(Folder folder) {
        executeCreateEditDeleteFolderTask(folder, ApiConstants.DELETE);
    }

    private boolean selectAccountItem(int content) {
        if (drawerListItems[content].equals(getResources().getString(R.string.drawer_change_account))) {
            openMailboxSelection();
            return true;

        } else if (drawerListItems[content].equals(getResources().getString(R.string.drawer_settings))) {
            startPreferencesActivity();
            return true;

        } else if (drawerListItems[content].equals(getResources().getString(R.string.drawer_help))) {
            openHelpWebView();
            return true;

        } else if (drawerListItems[content].equals(getResources().getString(R.string.drawer_logout))) {
            logOut();
            return true;
        }
        return false;
    }

    private void selectItem(int content) {
        ContentFragment contentFragment = new DocumentFragment(ApplicationConstants.MAILBOX);

        if (account != null) {
            try {

                int inboxReceiptsAndFolders = (numberOfFolders + ApplicationConstants.numberOfStaticFolders);

                if (content == ApplicationConstants.MAILBOX) {
                    contentFragment = new DocumentFragment(ApplicationConstants.MAILBOX);

                } else if (content == ApplicationConstants.RECEIPTS) {
                    contentFragment = new ReceiptFragment();

                } else if (content > ApplicationConstants.FOLDERS_LABEL && content < inboxReceiptsAndFolders) {
                    contentFragment = new DocumentFragment(content);

                } else if (selectAccountItem(content)) {
                    return;
                } else if (folders != null && content == inboxReceiptsAndFolders) {
                    showCreateEditDialog(content, false);
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

    private void updateUI(boolean useCachedFolders) {
        updateDrawer(useCachedFolders);
        updateTitles();
    }

    private void updateTitles() {

        if (editDrawerMode) {
            getActionBar().setTitle(getString(R.string.edit));
        } else if (account != null) {
            fragmentName = "";
            try {
                if (showActionBarName) {
                    fragmentName = mailbox.getName();
                } else {
                    if (getActionBar().getTitle().equals("")) {
                        if (mailbox != null) {
                            fragmentName = mailbox.getName();
                        }
                    } else if (drawerListItems[getCurrentFragment().getContent()].equals(getResources().getString(R.string.drawer_inbox))) {
                        fragmentName = mailbox.getName();
                    } else {
                        fragmentName = drawerListItems[getCurrentFragment().getContent()];
                    }
                }
                getActionBar().setTitle(fragmentName);
            } catch (NullPointerException e) {
                //IGNORE
            }
        }

    }

    private void updateDrawer(boolean useCachedFolders) {
        invalidateOptionsMenu();
        int currentDrawerListViewPosition = drawerList.getFirstVisiblePosition();
        if (!useCachedFolders) {
            folders = new ArrayList<Folder>();
        }
        ArrayList<String> drawerItems = new ArrayList<String>();

        //Add main menu
        drawerItems.add(getResources().getString(R.string.drawer_inbox));
        drawerItems.add(getResources().getString(R.string.drawer_receipts));
        drawerItems.add(getResources().getString(R.string.drawer_my_folders));

        ArrayList<Folder> fs = null;

        if (account != null) {

            //Add folders
            mailbox = account.getMailboxByDigipostAddress(ContentOperations.digipostAddress);

            if (mailbox != null) {
                if (useCachedFolders) {
                    fs = folders;
                } else {
                    fs = mailbox.getFolders().getFolder();
                }
                numberOfFolders = fs.size();
                for (Folder f : fs) {
                    drawerItems.add(f.getName());
                    if (!useCachedFolders) {
                        folders.add(f);
                    }
                }

            }

            drawerItems.add(getResources().getString(R.string.drawer_create_folder));
        }

        //Add account settings
        drawerItems.add(getResources().getString(R.string.drawer_my_account));

        if (account != null) {
            mailboxes = account.getMailbox();

            if (mailboxes.size() > 1) {
                drawerItems.add(getResources().getString(R.string.drawer_change_account));
            }
        }

        drawerItems.add(getResources().getString(R.string.drawer_settings));
        drawerItems.add(getResources().getString(R.string.drawer_help));
        drawerItems.add(getResources().getString(R.string.drawer_logout));

        //Add items to drawer
        drawerListItems = new String[drawerItems.size()];
        drawerListItems = drawerItems.toArray(drawerListItems);

        int unreadLetters = 0;
        if (mailbox != null) {
            unreadLetters = mailbox.getUnreadItemsInInbox();
        }

        drawerArrayAdapter = new DrawerAdapter(this, toMap(drawerItems), drawerItems, fs, unreadLetters);
        drawerList.setDragNDropAdapter(drawerArrayAdapter);

        try {
            if (currentDrawerListViewPosition == 0) {
                drawerList.setSelection(currentDrawerListViewPosition);
            } else {
                drawerList.setSelection(currentDrawerListViewPosition + 1);
            }
        } catch (Exception e) {
            //IGNORE
        }
    }

    private ArrayList<Map<String, Object>> toMap(ArrayList<String> content) {
        ArrayList<Map<String, Object>> items = new ArrayList<Map<String, Object>>();

        for (String drawerItem : content) {
            HashMap<String, Object> item = new HashMap<String, Object>();
            item.put("drawer_link_name", drawerItem);
            items.add(item);
        }

        return items;
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
        intent.putExtra(ApiConstants.UPLOAD, getCurrentFragment().getContent());
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
            searchView.setQueryHint(getString(R.string.search_in) + drawerListItems[getCurrentFragment().getContent()]);

            android.widget.ImageView searchViewClearButton = (android.widget.ImageView) searchPlate.getChildAt(1);
            searchViewClearButton.setImageResource(R.drawable.ic_clear_white);

            searchView.setOnQueryTextListener(new SearchViewOnQueryTextListener());
        } catch (NoSuchFieldException e) {
            //IGNORE
        } catch (IllegalAccessException e) {
            //IGNORE
        } catch (NullPointerException e) {
            e.printStackTrace();
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
            editDrawerMode = false;
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
                checkAppDeprecation();
                if (drawerUpdates < 1) {
                    updateUI(false);
                }
            } else {
                if (invalidToken) {
                    DialogUtitities.showToast(MainContentActivity.this, errorMessage);
                    logOut();
                }
            }
        }
    }

    private void executeCreateEditDeleteFolderTask(Folder folder, String action) {
        CreateEditDeleteFolderTask editDeleteFolderTask = new CreateEditDeleteFolderTask(folder, action);
        editDeleteFolderTask.execute();
    }

    private class CreateEditDeleteFolderTask extends AsyncTask<Void, Void, Integer> {
        private String errorMessage;
        private boolean invalidToken;
        private String action;
        private Folder folder;

        public CreateEditDeleteFolderTask(final Folder folder, final String action) {
            this.folder = folder;
            this.action = action;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                return ContentOperations.createEditDeleteFolder(MainContentActivity.this, folder, action);
            } catch (DigipostApiException e) {
                Log.e(getClass().getName(), e.getMessage(), e);
                errorMessage = e.getMessage();
                return ApplicationConstants.BAD_REQUEST;
            } catch (DigipostClientException e) {
                Log.e(getClass().getName(), e.getMessage(), e);
                errorMessage = e.getMessage();
                return ApplicationConstants.BAD_REQUEST;
            } catch (DigipostAuthenticationException e) {
                Log.e(getClass().getName(), e.getMessage(), e);
                errorMessage = e.getMessage();
                invalidToken = true;
                return ApplicationConstants.BAD_REQUEST;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            if (result == ApplicationConstants.OK) {
                executeGetAccountTask();
            } else {

                if (result == ApplicationConstants.BAD_REQUEST_DELETE) {
                    DialogUtitities.showToast(MainContentActivity.this, getString(R.string.error_documents_in_delete_Folder));
                } else if (invalidToken) {
                    DialogUtitities.showToast(MainContentActivity.this, errorMessage);
                    logOut();
                }
            }
        }
    }

    private void executeUpdateFoldersTask() {
        UpdateFoldersTask updateFolderTask = new UpdateFoldersTask(folders);
        updateFolderTask.execute();
    }

    private class UpdateFoldersTask extends AsyncTask<Void, Void, String> {
        private String errorMessage;
        private ArrayList<Folder> folders;

        public UpdateFoldersTask(final ArrayList<Folder> folders) {
            this.folders = folders;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                return ContentOperations.updateFolders(MainContentActivity.this, folders);
            } catch (DigipostApiException e) {
                Log.e(getClass().getName(), e.getMessage(), e);
                errorMessage = e.getMessage();
                return errorMessage;
            } catch (DigipostClientException e) {
                Log.e(getClass().getName(), e.getMessage(), e);
                errorMessage = e.getMessage();
                return errorMessage;
            } catch (DigipostAuthenticationException e) {
                Log.e(getClass().getName(), e.getMessage(), e);
                errorMessage = e.getMessage();
                return errorMessage;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            drawerUpdates--;
            if (result != null) {
                executeGetAccountTask();
            } else {
                DialogUtitities.showToast(MainContentActivity.this, errorMessage);
                logOut();
            }
        }
    }
}