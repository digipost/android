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

import no.digipost.android.R;
import no.digipost.android.api.LetterOperations;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.gui.fragments.ArchiveFragment;
import no.digipost.android.gui.fragments.ContentFragment;
import no.digipost.android.gui.fragments.MailboxFragment;
import no.digipost.android.gui.fragments.ReceiptFragment;
import no.digipost.android.gui.fragments.WorkareaFragment;
import no.digipost.android.gui.adapters.DrawerArrayAdapter;
import no.digipost.android.model.PrimaryAccount;

import android.app.Activity;
import android.app.FragmentManager;

import android.app.SearchManager;
import android.content.Context;
import android.content.res.Configuration;

import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.SearchView;


public class MainContentActivity extends Activity implements ContentFragment.ActivityCommunicator {
    private LetterOperations letterOperations;

    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;

    private CharSequence drawerTitle;
    private CharSequence title;

    private MenuItem refreshButton;
    private boolean refreshing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_content);

        this.letterOperations = new LetterOperations(this);

        title = drawerTitle = getTitle();
        drawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        drawerList = (ListView) findViewById(R.id.main_left_drawer);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        drawerList.setAdapter(new DrawerArrayAdapter<String>(this, R.layout.drawer_list_item, ApplicationConstants.titles));

        drawerList.setOnItemClickListener(new DrawerItemClickListener());

        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setIcon(R.drawable.actionbar_icon);
        getActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.actionbar_red_background));

        drawerToggle = new MainContentActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer_white, R.string.open_external, R.string.close);
        drawerLayout.setDrawerListener(drawerToggle);

        invalidateOptionsMenu();

        if (savedInstanceState == null) {
            selectItem(0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_content_actionbar, menu);

        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        setupSearchView(searchView);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        refreshButton = menu.findItem(R.id.menu_refresh);
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

        if (refreshing) {
            onStartRefreshContent();
        }

        boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
        searchButton.setVisible(!drawerOpen);
        refreshButton.setVisible(!drawerOpen);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (item.equals(refreshButton)) {
            getCurrentFragment().updateAccountMeta();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStartRefreshContent() {
        refreshing = true;
        refreshButton.setActionView(R.layout.activity_main_content_refreshspinner);
    }

    @Override
    public void onEndRefreshContent() {
        refreshing = false;
        refreshButton.setActionView(null);
    }

    @Override
    public void requestLetterOperations() {
        getCurrentFragment().setLetterOperations(letterOperations);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int content) {

        ContentFragment contentFragment = null;

        switch(content){
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
        setTitle(ApplicationConstants.titles[content]);
        drawerLayout.closeDrawer(drawerList);
    }

    @Override
    public void setTitle(CharSequence t) {
        title = t;
        getActionBar().setTitle(title);
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

    private ContentFragment getCurrentFragment() {
        return (ContentFragment) getFragmentManager().findFragmentById(R.id.main_content_frame);
    }

    private void setupSearchView(SearchView searchView) {
        int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
        View searchPlate = searchView.findViewById(searchPlateId);
        searchPlate.setBackgroundResource(R.drawable.search_background);
        searchPlate.setBackgroundColor(getResources().getColor(R.color.white));
        // ToDo HINT FARGE.

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconified(false);

        searchView.setQueryHint(getString(R.string.search_in) + title);
        searchView.setOnQueryTextListener(new SearchViewOnQueryTextListener());
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

        public MainContentActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, int drawerImageRes, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
            super(activity, drawerLayout, drawerImageRes, openDrawerContentDescRes, closeDrawerContentDescRes);
        }

        public void onDrawerClosed(View view) {
            getActionBar().setTitle(title);
            invalidateOptionsMenu();
        }

        public void onDrawerOpened(View drawerView) {
            getActionBar().setTitle(drawerTitle);
            invalidateOptionsMenu();
        }
    }
}