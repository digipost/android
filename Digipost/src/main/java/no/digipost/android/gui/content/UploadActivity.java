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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import no.digipost.android.DigipostApplication;
import no.digipost.android.utilities.*;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import no.digipost.android.R;
import no.digipost.android.api.ContentOperations;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.model.Mailbox;

import static java.lang.String.format;

public class UploadActivity extends Activity {
    private final static File DEFAULT_INITIAL_DIRECTORY = Environment.getExternalStorageDirectory();
    private final String[] blockedFileContentTypes = {ApiConstants.TEXT_HTML};
    private final String KEY_DIRECTORY = "directory";

    private File mDirectory;
    private ArrayList<File> mFiles;
    private boolean mShowHiddenFiles = false;
    private UploadListAdapter listAdapter;

    private TextView absolutePath;
    private TextProgressBar availableSpace;
    private TextView listEmpty;
    private int content = 0;
    private final long TOO_LARGE_FILE = 104858027;
    private ActionMode uploadActionMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((DigipostApplication) getApplication()).getTracker(DigipostApplication.TrackerName.APP_TRACKER);
        setContentView(R.layout.activity_upload);

        getActionBar().setTitle(R.string.upload);
        getActionBar().setHomeButtonEnabled(true);

        absolutePath = (TextView) findViewById(R.id.upload_file_path);
        availableSpace = (TextProgressBar) findViewById(R.id.upload_available_space);
        listEmpty = (TextView) findViewById(R.id.upload_list_empty);

        mDirectory = DEFAULT_INITIAL_DIRECTORY;
        mFiles = new ArrayList<File>();

        ListView listView = (ListView) findViewById(R.id.upload_file_list);
        listView.setEmptyView(listEmpty);
        listView.setOnItemClickListener(new ListOnItemClickListener());
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setFastScrollEnabled(true);
        listView.setMultiChoiceModeListener(new UploadMultiChoiceModeListener());
        listAdapter = new UploadListAdapter(this, mFiles);
        listView.setAdapter(listAdapter);
        content = getIntent().getIntExtra(ApiConstants.UPLOAD, ApplicationConstants.MAILBOX);
        Permissions.requestWritePermissionsIfMissing(getApplicationContext(), UploadActivity.this);
        executeSetAccountInfoTask();
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_DIRECTORY, mDirectory.getAbsolutePath());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mDirectory = new File(savedInstanceState.getString(KEY_DIRECTORY));
    }

    @Override
    protected void onResume() {
        refreshFilesList();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isFinishing()) {
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.clearDiscCache();
            imageLoader.clearMemoryCache();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refreshFilesList() {
        absolutePath.setText(mDirectory.getAbsolutePath());

        mFiles.clear();

        File[] files = mDirectory.listFiles();

        if (files != null && files.length > 0) {
            for (File f : files) {
                if ((f.isHidden() && !mShowHiddenFiles) || !isAcceptedFileExtension(f)) {
                    continue;
                }

                mFiles.add(f);
            }

            Collections.sort(mFiles, new FileComparator());
        }
        listAdapter.notifyDataSetChanged();
    }

    private void setAvailableSpace(Mailbox mailbox) {
        long bytesUsed = Long.parseLong(mailbox.getUsedStorage());
        long bytesAvailable = Long.parseLong(mailbox.getTotalAvailableStorage());

        int percentUsed = (int) ((bytesUsed * 100) / bytesAvailable);

        availableSpace.setProgress(percentUsed);
        availableSpace.setText(percentUsed + getString(R.string.upload_space_used));
    }

    private boolean isAcceptedFileExtension(File file) {
        if (blockedFileContentTypes != null && blockedFileContentTypes.length > 0) {
            String contentType = FileUtilities.getMimeType(file);

            if (contentType != null) {
                for (String blockedFileContentType : blockedFileContentTypes) {
                    if (contentType.equals(blockedFileContentType)) {
                        return false;
                    }
                }
            }

            return true;
        }

        return true;
    }

    private void promtUpload(final File file) {
        final AlertDialog dialog = DialogUtitities.getAlertDialogBuilderWithMessageAndTitle(this,
                getString(R.string.upload_dialog) + file.getName() + "?", getString(R.string.upload))
                .setPositiveButton(R.string.upload, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        executeUploadTask(file);
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(R.string.abort, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button b = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        previewFile(file);
                    }
                });
            }
        });

        dialog.show();
    }

    private void promtUpload(final ArrayList<File> files) {
        AlertDialog.Builder builder = DialogUtitities.getAlertDialogBuilderWithMessageAndTitle(this,
                getUploadFileDialogMessage(listAdapter.getCheckedCount()), getString(R.string.upload));
        builder.setPositiveButton(R.string.upload, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                uploadActionMode.finish();
                executeUploadTask(files);
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(R.string.abort, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    private String getUploadFileDialogMessage(int count) {
        if (count > 1) {
            return format(getString(R.string.upload_confirm_multiple), count);
        }

        return getString(R.string.upload_confirm_single);
    }

    private void previewFile(File file) {
        if (FilenameUtils.getExtension(file.getName()).toLowerCase().equals(ApiConstants.FILETYPE_PDF)) {
            Intent intent = new Intent(UploadActivity.this, MuPDFActivity.class);
            intent.putExtra(MuPDFActivity.ACTION_OPEN_FILEPATH, file.getAbsolutePath());
            startActivity(intent);
        } else {
            try {
                FileUtilities.openFileWithIntent(this, file);
            } catch (ActivityNotFoundException e) {
                DialogUtitities.showToast(this, getString(R.string.error_no_activity_to_open_file));
            }
        }
    }

    private void executeUploadTask(File file) {
        ArrayList<File> files = new ArrayList<File>();
        files.add(file);
        UploadTask uploadTask = new UploadTask(files, content);
        uploadTask.execute();
    }

    private void executeUploadTask(ArrayList<File> files) {
        UploadTask uploadTask = new UploadTask(files, content);
        uploadTask.execute();
    }

    private void setAccountInfo(Mailbox mailbox) {
        setAvailableSpace(mailbox);
        availableSpace.setVisibility(View.VISIBLE);
    }

    private void executeSetAccountInfoTask() {
        SetAccountInfoTask setAccountInfoTask = new SetAccountInfoTask();
        setAccountInfoTask.execute();
    }

    private class SetAccountInfoTask extends AsyncTask<Void, Void, Mailbox> {

        @Override
        protected Mailbox doInBackground(Void... voids) {
            try {
                return ContentOperations.getCurrentMailbox(UploadActivity.this);
            } catch (DigipostApiException e) {
                return null;
            } catch (DigipostClientException e) {
                return null;
            } catch (DigipostAuthenticationException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Mailbox mailbox) {
            super.onPostExecute(mailbox);

            if (mailbox != null) {
                setAccountInfo(mailbox);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!mDirectory.equals(DEFAULT_INITIAL_DIRECTORY)) {
            mDirectory = mDirectory.getParentFile();
            refreshFilesList();
            return;
        }

        super.onBackPressed();
    }

    private class ListOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            File newFile = listAdapter.getItem(position);
            if(newFile.length() > TOO_LARGE_FILE){
                showTooLargeFileDialog();
            }else if (newFile.isFile()) {
                promtUpload(newFile);
            } else {
                mDirectory = newFile;
                refreshFilesList();
            }
        }
    }

    private void showTooLargeFileDialog(){
        String message = getString(R.string.upload_too_large_file);
        Dialog tooLargeFileDialog = DialogUtitities.getAlertDialogBuilderWithMessage(this,message ).create();
        tooLargeFileDialog.show();
    }

    private class UploadListAdapter extends ArrayAdapter<File> {

        private ArrayList<File> objects;
        private boolean[] checked;
        private boolean checkboxVisible;

        private Context context;

        public UploadListAdapter(Context context, ArrayList<File> objects) {
            super(context, R.layout.upload_list_item, android.R.id.text1, objects);
            this.objects = objects;
            this.checked = new boolean[objects.size()];
            this.checkboxVisible = false;
            this.context = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.upload_list_item, parent, false);

            File object = objects.get(position);

            TextView name = (TextView) row.findViewById(R.id.upload_file_name);
            TextView size = (TextView) row.findViewById(R.id.upload_file_size);
            TextView extension = (TextView) row.findViewById(R.id.upload_file_extension);

            name.setText(FilenameUtils.getBaseName(object.getName()));

            final ImageView thumbnail = (ImageView) row.findViewById(R.id.upload_thumbnail);

            if (object.isFile()) {
                if (isImage(object)) {
                    ImageSize targetSize = new ImageSize(40, 40);
                    ImageLoader.getInstance().loadImage(FileUtilities.getFileUri(object), targetSize, getImageLoaderOptions(), new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            super.onLoadingComplete(imageUri, view, loadedImage);
                            thumbnail.setImageBitmap(loadedImage);
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            super.onLoadingFailed(imageUri, view, failReason);
                            thumbnail.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_grey_file));
                        }
                    });
                } else {
                    thumbnail.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_grey_file));
                }

                size.setText(DataFormatUtilities.getFormattedFileSize(object.length()));
                extension.setText("." + FilenameUtils.getExtension(object.getName()));
            } else {
                thumbnail.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_grey_folder));
            }

            CheckBox checkBox = (CheckBox) row.findViewById(R.id.upload_checkbox);
            checkBox.setFocusable(false);
            checkBox.setClickable(false);
            if (checkboxVisible) {
                if (checked[position]) {
                    checkBox.setChecked(true);
                }

                if (object.isFile()) {
                    checkBox.setVisibility(View.VISIBLE);
                }
            } else {
                checkBox.setVisibility(View.GONE);
            }

            return row;
        }

        private void initializeChecked() {
            checked = new boolean[objects.size()];
        }

        public void setCheckboxVisible(boolean state) {
            checkboxVisible = state;
            initializeChecked();
            notifyDataSetChanged();
        }

        public void clearChecked() {
            initializeChecked();
            notifyDataSetChanged();
        }

        public void setChecked(int position) throws UnsupportedOperationException {
            File file = objects.get(position);
            if(file.length() > TOO_LARGE_FILE) {
                showTooLargeFileDialog();
            }else if (file.isFile()) {
                checked[position] = !checked[position];
            } else {
                throw new UnsupportedOperationException(getString(R.string.upload_folder_exception));
            }
        }

        public int getCheckedCount() {
            int count = 0;

            for (boolean state : checked) {
                if (state) {
                    count++;
                }
            }

            return count;
        }

        public ArrayList<File> getCheckedItems() {
            ArrayList<File> checkedItems = new ArrayList<File>();

            for (int i = 0; i < checked.length; i++) {
                if (checked[i]) {
                    checkedItems.add(objects.get(i));
                }
            }

            return checkedItems;
        }

        private DisplayImageOptions getImageLoaderOptions() {
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisc(true)
                    .build();

            return options;
        }

        private boolean isImage(File f) {
            for (String extension : ApiConstants.FILETYPES_IMAGE) {
                if (FilenameUtils.getExtension(f.getName()).equals(extension)) {
                    return true;
                }
            }

            return false;
        }
    }

    private class FileComparator implements Comparator<File> {
        @Override
        public int compare(File f1, File f2) {
            if (f1 == f2) {
                return 0;
            }
            if (f1.isDirectory() && f2.isFile()) {
                return -1;
            }
            if (f1.isFile() && f2.isDirectory()) {
                return 1;
            }

            return f1.getName().compareToIgnoreCase(f2.getName());
        }
    }

    private class UploadTask extends AsyncTask<Void, File, String> {
        private ArrayList<File> files;
        private boolean invalidToken;
        private int progress;
        private int content;

        private ProgressDialog progressDialog;

        public UploadTask(ArrayList<File> files, int content) {
            this.files = files;
            this.progress = 0;
            this.content = content;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = DialogUtitities.getProgressDialogWithMessage(UploadActivity.this, "");
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.abort), new DialogInterface.OnClickListener() {
                public void onClick(final DialogInterface dialog, final int which) {
                    dialog.dismiss();
                    UploadTask.this.cancel(true);
                }
            });

            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                for (File file : files) {
                    if (!isCancelled()) {
                        publishProgress(file);
                        progress++;
                        ContentOperations.uploadFile(UploadActivity.this, file, content);
                    }
                }

                return null;
            } catch (DigipostAuthenticationException e) {
                e.printStackTrace();
                invalidToken = true;
                return e.getMessage();
            } catch (DigipostApiException e) {
                e.printStackTrace();
                return e.getMessage();
            } catch (DigipostClientException e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }

        @Override
        protected void onProgressUpdate(File... values) {
            super.onProgressUpdate(values);
            progressDialog.setMessage(format(getString(R.string.uploading), values[0].getName(), progress, files.size()));
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            progressDialog.dismiss();
            DialogUtitities.showToast(UploadActivity.this, format(getString(R.string.upload_files_uploaded), progress, files.size()));
            finishActivityWithAction(ApiConstants.REFRESH_ARCHIVE);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();

            if (result != null) {
                if (invalidToken) {
                    finishActivityWithAction(ApiConstants.LOGOUT);
                }

                DialogUtitities.showToast(UploadActivity.this, result);
            } else {
                DialogUtitities.showToast(UploadActivity.this, getString(R.string.upload_complete));
                finishActivityWithAction(ApiConstants.UPLOAD);
            }
        }
    }

    private void finishActivityWithAction(String action) {
        Intent intent = new Intent();
        intent.putExtra(ApiConstants.FRAGMENT_ACTIVITY_RESULT_ACTION, action);
        setResult(RESULT_OK, intent);
        finish();
    }

    protected class UploadMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener {

        @Override
        public void onItemCheckedStateChanged(ActionMode actionMode, int position, long id, boolean state) {

            try {
                listAdapter.setChecked(position);
                actionMode.setTitle(Integer.toString(listAdapter.getCheckedCount()));
                listAdapter.notifyDataSetChanged();
            } catch (UnsupportedOperationException e) {
                DialogUtitities.showToast(UploadActivity.this, e.getMessage());
                actionMode.finish();
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            uploadActionMode = actionMode;
            UploadActivity.this.setTheme(R.style.Digipost_ActionMode);
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.activity_upload_context, menu);
            listAdapter.setCheckboxVisible(true);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, android.view.MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.upload_uploadButton:
                    promtUpload(listAdapter.getCheckedItems());
                    return true;
                default:
                    return true;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            listAdapter.setCheckboxVisible(false);
            listAdapter.clearChecked();
            UploadActivity.this.setTheme(R.style.Digipost);
            uploadActionMode = null;
        }
    }
}
