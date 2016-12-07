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
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.terlici.dragndroplist.DragNDropListView;
import java.util.ArrayList;
import no.digipost.android.DigipostApplication;
import no.digipost.android.R;
import no.digipost.android.api.ContentOperations;
import no.digipost.android.api.tasks.CreateEditDeleteFolderTask;
import no.digipost.android.api.tasks.GetAccountTask;
import no.digipost.android.api.tasks.UpdateFoldersTask;
import no.digipost.android.authentication.AndroidLockSecurity;
import no.digipost.android.authentication.TokenStore;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.gcm.GCMController;
import no.digipost.android.gui.adapters.DrawerAdapter;
import no.digipost.android.gui.adapters.MailboxArrayAdapter;
import no.digipost.android.gui.content.SettingsActivity;
import no.digipost.android.gui.content.UploadActivity;
import no.digipost.android.gui.fragments.ContentFragment;
import no.digipost.android.gui.fragments.DocumentFragment;
import no.digipost.android.gui.fragments.EditFolderFragment;
import no.digipost.android.gui.fragments.ReceiptFragment;
import no.digipost.android.model.*;
import no.digipost.android.utilities.ApplicationUtilities;
import no.digipost.android.utilities.DialogUtitities;
import no.digipost.android.utilities.FileUtilities;
import no.digipost.android.utilities.NetworkUtilities;
import no.digipost.android.utilities.SharedPreferencesUtilities;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;

public class MainContentActivity extends AppCompatActivity implements ContentFragment.ActivityCommunicator, NavigationView.OnNavigationItemSelectedListener {
    public static final int INTENT_REQUESTCODE = 0;
    public static boolean editDrawerMode;
    public static String errorMessage;
    public static boolean invalidToken;
    public static int numberOfFolders;
    public static String fragmentName;
    public static ArrayList<Folder> folders;
    public Bundle savedInstanceState;
    private static String[] drawerListItems;
    public static boolean launchedFromPush;
    private DrawerLayout drawer;
    private android.support.v7.app.ActionBarDrawerToggle drawerToggle;
    protected MailboxArrayAdapter mailboxAdapter;
    private int remainingDrawerChanges;
    private DragNDropListView drawerList;
    private DrawerAdapter drawerArrayAdapter;
    private boolean refreshing;
    private Dialog mailboxDialog;
    private boolean showActionBarName;
    private Mailbox mailbox;
    private ArrayList<Mailbox> mailboxes;
    private Account account;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        ((DigipostApplication) getApplication()).getTracker(DigipostApplication.TrackerName.APP_TRACKER);
        setContentView(R.layout.activity_main_content);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        GCMController.init(this);
        refreshInvoiceBanksAgreementState();

        drawer = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        drawerToggle = new android.support.v7.app.ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                toolbar.dismissPopupMenus();
                getCurrentFragment().finishActionMode();
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
                getCurrentFragment().activityDrawerOpen = drawer.isDrawerOpen(GravityCompat.START);
            }
        };

        drawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        drawerList = (DragNDropListView) findViewById(R.id.main_left_drawer);
        drawerList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        drawer.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        refreshing = true;
        remainingDrawerChanges = 0;
        editDrawerMode = false;
        updateUI(false);

        setDrawerListeners();

        selectItem(ApplicationConstants.MAILBOX);
        SharedPreferencesUtilities.getSharedPreferences(this).registerOnSharedPreferenceChangeListener(new SettingsChangedlistener());

        if (SharedPreferencesUtilities.numberOfTimesAppHasRun(this) <= ApplicationConstants.NUMBER_OF_TIMES_DRAWER_SHOULD_OPEN) {
            drawer.openDrawer(GravityCompat.START);
        }

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(AndroidLockSecurity.unableToUseStoredRefreshToken(this)){
            logOut();
        }
        GCMController.clearNotifications(this);
        if(launchedFromPush) {
            selectItem(ApplicationConstants.MAILBOX);
            launchedFromPush = false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return false;
    }

    public boolean isDrawerClosed(){
        return !drawer.isDrawerOpen(GravityCompat.START);
    }

    private void setDrawerListeners() {
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
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
                } else {
                    drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                }
                updateUI(false);
                return true;
            }
        });

        drawerList.setOnItemDragNDropListener(new DragNDropListView.OnItemDragNDropListener() {
            @Override
            public void onItemDrag(DragNDropListView parent, View view, int position, long id) {
                view.setBackgroundResource(R.drawable.main_drawer_hover);
            }

            @Override
            public void onItemDrop(DragNDropListView parent, View view, int startPosition, int endPosition, long id) {
                moveFolderFrom(startPosition, endPosition);
            }
        });
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
        remainingDrawerChanges++;
        updateUI(true);
        executeUpdateFoldersTask();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_content_actionbar, menu);
        if(getCurrentFragment() != null && getCurrentFragment().getContent() == ApplicationConstants.RECEIPTS){
            MenuItem uploadButton = menu.findItem(R.id.menu_upload);
            uploadButton.setVisible(false);
        }
        updateTitles();
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (drawer != null) {
            if (drawerArrayAdapter != null) {
                drawerArrayAdapter.notifyDataSetChanged();
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.menu_refresh:
                getCurrentFragment().refreshItems();
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
                if (data.hasExtra(ApiConstants.FRAGMENT_ACTIVITY_RESULT_ACTION)) {
                    String action = data.getStringExtra(ApiConstants.FRAGMENT_ACTIVITY_RESULT_ACTION);

                    if (action.equals(ApiConstants.UPLOAD)) {
                        selectItem(getCurrentFragment().getContent());
                    } else if (action.equals(ApiConstants.LOGOUT)) {
                        logOut();
                    }
                }
            }
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(editDrawerMode){
                editDrawerMode = false;
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                updateUI(false);
                return true;
            }else if(drawer.isDrawerOpen(GravityCompat.START)){
                drawer.closeDrawers();
                return true;
            }

            if (getCurrentFragment().getContent() != ApplicationConstants.MAILBOX) {
                selectItem(ApplicationConstants.MAILBOX);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void selectMailbox(String digipostAddress, String name) {
        if (ContentOperations.changeMailbox(digipostAddress)) {
            getSupportActionBar().setTitle(name);
            account = null;
            TokenStore.removeHighAuthenticationTokens();
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

    private void showCreateEditDialog(int content, boolean editFolder) {

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(EditFolderFragment.fragmentName);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        DialogFragment editFolderFragment = EditFolderFragment.newInstance(content, account.getValidationRules().getFolderName(), editFolder);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        editFolderFragment.show(ft, EditFolderFragment.fragmentName);
    }

    public void createFolder(Folder folder) {
        executeCreateEditDeleteFolderTask(folder, ApiConstants.CREATE);
    }

    public void saveEditFolder(Folder folder, int folderIndex) {
        folders.set(folderIndex, folder);
        updateUI(false);
        executeCreateEditDeleteFolderTask(folder, ApiConstants.EDIT);
    }

    public void deleteEditFolder(Folder folder) {
        executeCreateEditDeleteFolderTask(folder, ApiConstants.DELETE);
    }

    private boolean selectAccountItem(int content) {
        if (drawerListItems[content].equals(getResources().getString(R.string.drawer_change_account))) {
            openMailboxSelection();
            return true;

        } else if (drawerListItems[content].equals(getResources().getString(R.string.drawer_settings))) {
            Intent intent = new Intent(MainContentActivity.this, SettingsActivity.class);
            startActivityForResult(intent, INTENT_REQUESTCODE);
            return true;

        } else if (drawerListItems[content].equals(getResources().getString(R.string.drawer_help))) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ApiConstants.URL_HELP));
            startActivity(browserIntent);
            return true;

        } else if (drawerListItems[content].equals(getResources().getString(R.string.drawer_logout))) {
            logOut();
            return true;
        }
        return false;
    }

    private void selectItem(int content) {
        ContentFragment contentFragment = DocumentFragment.newInstance(ApplicationConstants.MAILBOX);

        if (account != null) {
            try {

                int inboxReceiptsAndFolders = (numberOfFolders + ApplicationConstants.numberOfStaticFolders);

                if (content == ApplicationConstants.MAILBOX) {
                    contentFragment = DocumentFragment.newInstance(ApplicationConstants.MAILBOX);

                } else if (content == ApplicationConstants.RECEIPTS) {
                    contentFragment = ReceiptFragment.newInstance();

                } else if (content > ApplicationConstants.FOLDERS_LABEL && content < inboxReceiptsAndFolders) {
                    contentFragment = DocumentFragment.newInstance(content);

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
        drawer.closeDrawers();
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
        if (account != null) {
            fragmentName = "";
            try {
                if (getCurrentFragment() != null && getCurrentFragment().getContent() == ApplicationConstants.MAILBOX || getSupportActionBar().getTitle().equals("")) {
                    fragmentName = mailbox.getName();
                } else {
                    fragmentName = drawerListItems[getCurrentFragment().getContent()];
                }

                getSupportActionBar().setTitle(fragmentName);
            } catch (NullPointerException e) {
                //IGNORE
            }
        }
    }

    private void updateDrawer(boolean useCachedFolders) {
        int currentDrawerListViewPosition = drawerList.getFirstVisiblePosition();
        if (!useCachedFolders) {
            folders = new ArrayList<>();
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

        drawerArrayAdapter = new DrawerAdapter(this, ApplicationUtilities.drawerContentToMap(drawerItems), drawerItems, fs, unreadLetters);
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
        invalidateOptionsMenu();
    }

    private ContentFragment getCurrentFragment() {
        return (ContentFragment) getFragmentManager().findFragmentById(R.id.main_content_frame);
    }

    private void logOut() {
        FileUtilities.deleteTempFiles();
        GCMController.reset(getApplicationContext());
        revokeAndDeleteAccess();
        ContentOperations.resetState();
        mailbox = null;
        account = null;
        Intent intent = new Intent(MainContentActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void revokeAndDeleteAccess(){
            new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... params) {
                    try {
                        ContentOperations.revokeOAuthToken(getApplicationContext());
                        TokenStore.deleteStore(getApplicationContext());
                    } catch (Exception e) {
                        SharedPreferencesUtilities.setLogoutFailed(getApplicationContext(), true);
                    }
                    return true;
                }

                @Override
                protected void onCancelled() {
                    super.onCancelled();
                    TokenStore.deleteStore(getApplicationContext());
                    SharedPreferencesUtilities.setLogoutFailed(getApplicationContext(),true);
                }

            }.execute(null, null, null);
    }

    private void refreshInvoiceBanksAgreementState(){
    	new AsyncTask<Void, Void, ArrayList<Bank>>() {

    		@Override
    		protected ArrayList<Bank> doInBackground(Void... params) {
                ArrayList<Bank> banks;
                try {
                    banks = ContentOperations.getInvoiceBanksAgreementState(getApplicationContext());
                }catch (Exception e){
                    return null;
                }
                return banks;
    		}

    		@Override
        protected void onPostExecute(final ArrayList<Bank> banks) {
                if(banks != null){
                    boolean hasBankAgreementType1 = false;
                    boolean hasBankAgreementType2 = false;

                    for(Bank bank : banks){
                        if(bank.hasAgreementType(Agreement.AGREEMENT_TYPE_1)){
                            hasBankAgreementType1 = true;
                        }
                        if(bank.hasAgreementType(Agreement.AGREEMENT_TYPE_2)){
                            hasBankAgreementType2 = true;
                        }
                    }

                    SharedPreferencesUtilities.setBankAgreement(getApplicationContext(), SharedPreferencesUtilities.HAS_BANK_AGREEMENT_TYPE_1, hasBankAgreementType1);
                    SharedPreferencesUtilities.setBankAgreement(getApplicationContext(), SharedPreferencesUtilities.HAS_BANK_AGREEMENT_TYPE_2, hasBankAgreementType2);
                }
        }

    	}.execute(null, null, null);
    }

    private void startUploadActivity() {
        Intent intent = new Intent(MainContentActivity.this, UploadActivity.class);
        intent.putExtra(ApiConstants.UPLOAD, getCurrentFragment().getContent());
        startActivityForResult(intent, INTENT_REQUESTCODE);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options) {
        if(intent == null){
            intent = new Intent();
        }
        super.startActivityForResult(intent, requestCode, options);
    }

    private void executeGetAccountTask() {
        GetAccountTask getAccountTask = new GetAccountTask(this);
        getAccountTask.execute();
    }

    private void replaceFragmentWithMailboxIfInvalid(){
        try {
            if(getCurrentFragment() != null) {
                if (getCurrentFragment().getContent() >= ApplicationConstants.numberOfStaticFolders) {
                    if (getCurrentFragment().getContent() - ApplicationConstants.numberOfStaticFolders == MainContentActivity.numberOfFolders) {
                        DocumentFragment documentFragment = DocumentFragment.newInstance(ApplicationConstants.MAILBOX);

                        getFragmentManager().beginTransaction().replace(R.id.main_content_frame,documentFragment).commit();
                    }
                }
            }
        }catch(NullPointerException e){
            //Ignore
        }
    }

    public void setAccountFromTask(Account result) {
        if (result != null) {
            account = result;

            if (remainingDrawerChanges < 1) {
                updateUI(false);
            }

            replaceFragmentWithMailboxIfInvalid();

        } else {
            if (invalidToken) {
                DialogUtitities.showToast(getApplicationContext(), errorMessage);
                logOut();
            }
        }
    }

    private void executeCreateEditDeleteFolderTask(Folder folder, String action) {
        CreateEditDeleteFolderTask editDeleteFolderTask = new CreateEditDeleteFolderTask(this, folder, action);
        editDeleteFolderTask.execute();
    }

    public void updateFolderFromTask(Integer result) {
        if (result == NetworkUtilities.SUCCESS) {
            executeGetAccountTask();
        } else {
            if (result == NetworkUtilities.BAD_REQUEST_DELETE) {
                DialogUtitities.showToast(MainContentActivity.this, getString(R.string.error_documents_in_delete_Folder));
            } else if (invalidToken) {
                DialogUtitities.showToast(MainContentActivity.this, errorMessage);
                logOut();
            }
        }
    }

    private void executeUpdateFoldersTask() {
        UpdateFoldersTask updateFolderTask = new UpdateFoldersTask(this, folders);
        updateFolderTask.execute();
    }

    public void updateFoldersFromTask(String result) {
        remainingDrawerChanges--;
        if (result != null) {
            executeGetAccountTask();
        } else {
            DialogUtitities.showToast(MainContentActivity.this, errorMessage);
            logOut();
        }
    }

    private class SettingsChangedlistener implements OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(SettingsActivity.KEY_PREF_SHOW_BANK_ID_DOCUMENTS) && getCurrentFragment() != null) {
                getCurrentFragment().refreshItems();
            }
        }
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
}
