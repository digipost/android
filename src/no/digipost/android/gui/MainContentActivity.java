package no.digipost.android.gui;

import no.digipost.android.R;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.gui.fragments.ArchiveFragment;
import no.digipost.android.gui.fragments.ContentFragment;
import no.digipost.android.gui.fragments.MailboxFragment;
import no.digipost.android.gui.fragments.ReceiptFragment;
import no.digipost.android.gui.fragments.WorkareaFragment;
import no.digipost.android.gui.adapters.DrawerArrayAdapter;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

public class MainContentActivity extends Activity implements ContentFragment.ActivityCommunicator {
    private ActionModeCallback actionModeCallback;
    private ActionMode actionMode;

    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;

    private CharSequence drawerTitle;
    private CharSequence title;
    private String[] titles = {"Postkassen","Kjøkkenbenken", "Arkivet", "Kvitteringer"};

    private MenuItem refreshButton;
    private boolean refreshing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_content);

        title = drawerTitle = getTitle();
        drawerLayout = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        drawerList = (ListView) findViewById(R.id.main_left_drawer);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        //TODO lage ferdig drawerarrayadapter
        //mDrawerList.setAdapter(new DrawerArrayAdapter<String>(this, R.layout.drawer_list_item, mPlanetTitles));
        drawerList.setAdapter(new DrawerArrayAdapter<String>(this, R.layout.drawer_list_item, titles));

        drawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        getActionBar().setIcon(R.drawable.ic_launcher);
        getActionBar().setBackgroundDrawable(getResources().getDrawable(R.color.main_actionbar_red_background));
        getActionBar().getThemedContext();

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        // ToDo fikse open og close string
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.ic_drawer, R.string.open_external, R.string.close) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(title);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(drawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        drawerLayout.setDrawerListener(drawerToggle);

        actionModeCallback = new ActionModeCallback();

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

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        refreshButton = menu.findItem(R.id.menu_refresh);

        if (refreshing) {
            onStartRefreshContent();
        }

        boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
        menu.findItem(R.id.menu_search).setVisible(!drawerOpen);
        menu.findItem(R.id.menu_refresh).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
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
    public void onListLongClick() {
        System.out.println("activityCommunicator onLongClick");
        actionMode = startActionMode(actionModeCallback);
    }

    /* The click listner for ListView in the navigation drawer */
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
        setTitle(titles[content]);
        drawerLayout.closeDrawer(drawerList);
    }

    @Override
    public void setTitle(CharSequence t) {
        title = t;
        getActionBar().setTitle(title);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private ContentFragment getCurrentFragment() {
        return (ContentFragment) getFragmentManager().findFragmentById(R.id.main_content_frame);
    }

    private void setupSearchView(SearchView searchView) {
        searchView.setQueryHint("Søk...");
        searchView.setOnQueryTextListener(new SearchListener());
    }

    private class SearchListener implements SearchView.OnQueryTextListener {

        @Override
        public boolean onQueryTextSubmit(String s) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            getCurrentFragment().filterList(s);
            System.out.println(s);
            return true;
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.activity_main_content_context, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            actionMode = null;
        }
    }
}