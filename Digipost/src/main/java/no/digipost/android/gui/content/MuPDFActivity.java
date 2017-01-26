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

package no.digipost.android.gui.content;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.*;
import java.lang.reflect.Field;
import android.support.v7.widget.SearchView;
import com.google.android.gms.analytics.GoogleAnalytics;
import no.digipost.android.DigipostApplication;
import org.apache.commons.io.FilenameUtils;
import java.util.concurrent.Executor;
import no.digipost.android.R;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.documentstore.DocumentContentStore;
import no.digipost.android.gui.fragments.ContentFragment;
import no.digipost.android.pdf.MuPDFAlert;
import no.digipost.android.pdf.MuPDFCore;
import no.digipost.android.pdf.MuPDFPageAdapter;
import no.digipost.android.pdf.MuPDFReaderView;
import no.digipost.android.pdf.MuPDFView;
import no.digipost.android.pdf.SearchTask;
import no.digipost.android.pdf.SearchTaskResult;
import no.digipost.android.utilities.DialogUtitities;
import no.digipost.android.utilities.FileUtilities;

class ThreadPerTaskExecutor implements Executor {
    public void execute(Runnable r) {
        new Thread(r).start();
    }
}

public class MuPDFActivity extends DisplayContentActivity {
    public static final String ACTION_OPEN_FILEPATH = "openFilepath";

    private final String CURRENT_WINDOW = "currentWindow";
    private final Handler mHandler = new Handler();
    private int currentVindow;
    private MuPDFCore core;
    private String mFileName;
    private MuPDFReaderView mDocView;
    private TextView mFilenameView;
    private TextView mPageNumberView;
    private ImageButton mSearchButton;
    private ImageButton mCopySelectButton;
    private boolean mTopBarIsSearch;
    private ImageButton mSearchBack;
    private ImageButton mSearchFwd;
    private EditText mSearchText;
    private SearchTask mSearchTask;
    private AlertDialog.Builder mAlertBuilder;
    private boolean mAlertsActive = false;
    private AsyncTask<Void, Void, MuPDFAlert> mAlertTask;
    private AlertDialog mAlertDialog;
    private Intent intent;
    private boolean searchModeOn;
    private MenuItem searchMenuItem;
    private android.support.v7.widget.SearchView searchView;

    private ActionMode.Callback selectActionModeCallback;
    private ActionMode selectActionMode;

    public void createAlertWaiter() {
        mAlertsActive = true;
        if (mAlertTask != null) {
            mAlertTask.cancel(true);
            mAlertTask = null;
        }
        if (mAlertDialog != null) {
            mAlertDialog.cancel();
            mAlertDialog = null;
        }
        mAlertTask = new AsyncTask<Void, Void, MuPDFAlert>() {

            @Override
            protected MuPDFAlert doInBackground(Void... arg0) {
                if (!mAlertsActive)
                    return null;

                return core.waitForAlert();
            }

            @Override
            protected void onPostExecute(final MuPDFAlert result) {
                if (result == null)
                    return;
                final MuPDFAlert.ButtonPressed pressed[] = new MuPDFAlert.ButtonPressed[3];
                for (int i = 0; i < 3; i++)
                    pressed[i] = MuPDFAlert.ButtonPressed.None;
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mAlertDialog = null;
                        if (mAlertsActive) {
                            int index = 0;
                            switch (which) {
                                case AlertDialog.BUTTON1:
                                    index = 0;
                                    break;
                                case AlertDialog.BUTTON2:
                                    index = 1;
                                    break;
                                case AlertDialog.BUTTON3:
                                    index = 2;
                                    break;
                            }
                            result.buttonPressed = pressed[index];
                            core.replyToAlert(result);
                            createAlertWaiter();
                        }
                    }
                };
                mAlertDialog = mAlertBuilder.create();
                mAlertDialog.setTitle(result.title);
                mAlertDialog.setMessage(result.message);
                switch (result.iconType) {
                    case Error:
                        break;
                    case Warning:
                        break;
                    case Question:
                        break;
                    case Status:
                        break;
                }
                switch (result.buttonGroupType) {
                    case OkCancel:
                        mAlertDialog.setButton(AlertDialog.BUTTON2, "Avbryt", listener);
                        pressed[1] = MuPDFAlert.ButtonPressed.Cancel;
                    case Ok:
                        mAlertDialog.setButton(AlertDialog.BUTTON1, "Ok", listener);
                        pressed[0] = MuPDFAlert.ButtonPressed.Ok;
                        break;
                    case YesNoCancel:
                        mAlertDialog.setButton(AlertDialog.BUTTON3, "Avbryt", listener);
                        pressed[2] = MuPDFAlert.ButtonPressed.Cancel;
                    case YesNo:
                        mAlertDialog.setButton(AlertDialog.BUTTON1, "Ja", listener);
                        pressed[0] = MuPDFAlert.ButtonPressed.Yes;
                        mAlertDialog.setButton(AlertDialog.BUTTON2, "Nei", listener);
                        pressed[1] = MuPDFAlert.ButtonPressed.No;
                        break;
                }
                mAlertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        mAlertDialog = null;
                        if (mAlertsActive) {
                            result.buttonPressed = MuPDFAlert.ButtonPressed.None;
                            core.replyToAlert(result);
                            createAlertWaiter();
                        }
                    }
                });

                mAlertDialog.show();
            }
        };

        mAlertTask.executeOnExecutor(new ThreadPerTaskExecutor());
    }

    public void destroyAlertWaiter() {
        mAlertsActive = false;
        if (mAlertDialog != null) {
            mAlertDialog.cancel();
            mAlertDialog = null;
        }
        if (mAlertTask != null) {
            mAlertTask.cancel(true);
            mAlertTask = null;
        }
    }

    private MuPDFCore openFile(String path) {
        int lastSlashPos = path.lastIndexOf('/');
        mFileName = new String(lastSlashPos == -1 ? path : path.substring(lastSlashPos + 1));
        try {
            core = new MuPDFCore(path);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return core;
    }

    private MuPDFCore openBuffer(byte buffer[]) {
        try {
            core = new MuPDFCore(buffer);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return core;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((DigipostApplication) getApplication()).getTracker(DigipostApplication.TrackerName.APP_TRACKER);
        selectActionModeCallback = new SelectActionModeCallback();
        mAlertBuilder = new AlertDialog.Builder(this);
        intent = getIntent();
        String openFilepath = intent.getStringExtra(ACTION_OPEN_FILEPATH);
        if (openFilepath != null) {
            setActionBar(FilenameUtils.getName(openFilepath), null);
            core = openFile(openFilepath);
            SearchTaskResult.set(null);
        } else if (core == null && DocumentContentStore.getDocumentContent() != null) {
            setActionBar(DocumentContentStore.getDocumentAttachment().getSubject(), DocumentContentStore
                    .getDocumentParent()
                    .getCreatorName());

            byte buffer[] = DocumentContentStore.getDocumentContent();

            if (buffer != null) {
                core = openBuffer(buffer);
            }

            SearchTaskResult.set(null);
        }

        if (core == null) {
            AlertDialog alert = mAlertBuilder.create();
            alert.setTitle(R.string.pdf_open_failed);
            alert.setButton(AlertDialog.BUTTON_POSITIVE, this.getString(R.string.close), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            alert.show();
            return;
        }

        createUI();

        if (savedInstanceState != null) {
            mDocView.setDisplayedViewIndex(savedInstanceState.getInt(CURRENT_WINDOW, 0));
        }

        if (super.shouldShowInvoiceOptionsDialog(this)) {
            super.showInvoiceOptionsDialog(this);
        }
    }


    public void createUI() {
        if (core == null)
            return;

        mPageNumberView = new TextView(this);
        mDocView = new MuPDFReaderView(this) {
            @Override
            protected void onMoveToChild(int i) {
                if (core == null)
                    return;
                mPageNumberView.setText(String.format("%d / %d", i + 1, core.countPages()));

                if (searchModeOn) {
                    if (currentVindow < i) {
                        search(1);
                    } else {
                        search(-1);
                    }
                    currentVindow = i;
                } else {
                    currentVindow = i;
                    super.onMoveToChild(i);
                }
            }
            
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);

                selectModeOn();
                selectActionMode = startActionMode(selectActionModeCallback);
            }
        };
        mDocView.setAdapter(new MuPDFPageAdapter(this, core));

        mSearchTask = new SearchTask(this, core) {
            @Override
            protected void onTextFound(SearchTaskResult result) {
                SearchTaskResult.set(result);
                mDocView.setDisplayedViewIndex(result.pageNumber);
                mDocView.resetupChildren();
            }
        };

        mDocView.setLinksEnabled(true);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.pdf_relative_layout);
        layout.addView(mDocView);
        layout.setBackgroundResource(R.drawable.login_background);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(CURRENT_WINDOW, currentVindow);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode >= 0)
            mDocView.setDisplayedViewIndex(resultCode);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        MuPDFCore mycore = core;
        core = null;
        return mycore;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mSearchTask != null) {
            mSearchTask.stop();
        }
    }

    public void onDestroy() {
        if (core != null)
            core.onDestroy();
        if (mAlertTask != null) {
            mAlertTask.cancel(true);
            mAlertTask = null;
        }
        core = null;

        if (isFinishing()) {
            FileUtilities.deleteTempFiles();
        }

        super.onDestroy();
    }

    void selectModeOn() {
        mDocView.setSelectionMode(true);
    }

    void selectModeOff() {
        mDocView.setSelectionMode(false);

        MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
        if (pageView != null)
            pageView.deselectText();

    }

    void searchModeOn() {
        searchModeOn = true;
    }

    void searchModeOff() {
        searchModeOn = false;
        SearchTaskResult.set(null);
        mDocView.resetupChildren();
    }

    void search(int direction, String query) {
        int displayPage = mDocView.getDisplayedViewIndex();
        SearchTaskResult r = SearchTaskResult.get();
        int searchPage = r != null ? r.pageNumber : -1;
        mSearchTask.go(query, direction, displayPage, searchPage);
    }

    void search(int direction) {
        int displayPage = mDocView.getDisplayedViewIndex();
        SearchTaskResult r = SearchTaskResult.get();
        int searchPage = r != null ? r.pageNumber : -1;
        mSearchTask.go(((SearchView) searchMenuItem.getActionView()).getQuery().toString(), direction, displayPage, searchPage);
    }

    @Override
    public boolean onSearchRequested() {
        searchMenuItem.expandActionView();

        return super.onSearchRequested();
    }

    private void setupSearchView() {
        MenuItemCompat.setOnActionExpandListener(searchMenuItem,
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem menuItem) {
                        searchModeOn();
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                        searchModeOff();
                        return true;
                    }
                }
        );

        searchView = (android.support.v7.widget.SearchView) searchMenuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                if (!searchView.isIconified()) {
                    searchView.setIconified(true);
                }
                searchMenuItem.collapseActionView();

                search(1, s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (SearchTaskResult.get() != null && !s.equals(SearchTaskResult.get().txt)) {
                    SearchTaskResult.set(null);
                    mDocView.resetupChildren();
                }
                return true;
            }
        });

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
            searchView.setQueryHint(getString(R.string.pdf_search_document));
            android.widget.ImageView searchViewClearButton = (android.widget.ImageView) searchPlate.getChildAt(1);
            searchViewClearButton.setImageResource(R.drawable.ic_clear_white);
        }catch (Exception e){
            //Empty
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_mupdf_actionbar, menu);
        searchMenuItem = menu.findItem(R.id.pdfmenu_search);
        setupSearchView();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem delete = menu.findItem(R.id.pdfmenu_delete);
        MenuItem move = menu.findItem(R.id.pdfmenu_move);
        MenuItem openExternal = menu.findItem(R.id.pdfmenu_open_external);
        MenuItem save = menu.findItem(R.id.pdfmenu_save);
        sendToBank = menu.findItem(R.id.pdfmenu_send_to_bank);

        String openFilepath = intent.getStringExtra(ACTION_OPEN_FILEPATH);

        if (openFilepath != null) {
            delete.setVisible(false);
            move.setVisible(false);
            openExternal.setVisible(false);
            save.setVisible(false);
        }

        boolean sendToBankVisible = intent.getBooleanExtra(ContentFragment.INTENT_SEND_TO_BANK, false);

        if (ApplicationConstants.FEATURE_SEND_TO_BANK_VISIBLE) {
            super.setSendToBankMenuText(sendToBankVisible);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.pdfmenu_send_to_bank:
                super.openInvoiceTask();
                return true;
            case R.id.pdfmenu_delete:
                deleteAction(this);
                return true;
            case R.id.pdfmenu_move:
                showMoveToFolderDialog();
                return true;
            case R.id.pdfmenu_copy:
                selectModeOn();
                selectActionMode = startActionMode(selectActionModeCallback);
                return true;
            case R.id.pdfmenu_open_external:
                super.openFileWithIntent();
                return true;
            case R.id.pdfmenu_save:
                super.downloadFile();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void copyText() {
        MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
        boolean copied = false;
        if (pageView != null)
            copied = pageView.copySelection();

        DialogUtitities.showToast(this, copied ? getString(R.string.pdf_select_copied) : getString(R.string.pdf_select_no_text_selected));
    }

    @Override
    protected void onStart() {
        GoogleAnalytics.getInstance(this).reportActivityStart(this);

        if (core != null) {
            core.startAlerts();
            createAlertWaiter();
        }

        super.onStart();
    }

    @Override
    protected void onStop() {
        GoogleAnalytics.getInstance(this).reportActivityStop(this);

        if (core != null) {
            destroyAlertWaiter();
            core.stopAlerts();
        }

        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private class SelectActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.activity_mupdf_context, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.mupdf_context_menu_copy:
                    copyText();
                    actionMode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            selectModeOff();
            selectActionMode = null;
        }
    }
}
