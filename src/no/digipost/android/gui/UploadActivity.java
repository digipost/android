package no.digipost.android.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import no.digipost.android.R;
import no.digipost.android.api.LetterOperations;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.model.PrimaryAccount;
import no.digipost.android.utilities.DataFormatUtilities;
import no.digipost.android.utilities.DialogUtitities;

public class UploadActivity extends Activity {
    private final static File DEFAULT_INITIAL_DIRECTORY = Environment.getExternalStorageDirectory();

    private File mDirectory;
    private ArrayList<File> mFiles;
    private boolean mShowHiddenFiles = false;
    private UploadListAdapter mAdapter;

    private TextView absolutePath;
    private TextProgressBar availableSpace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        getActionBar().setTitle(R.string.upload);
        getActionBar().setHomeButtonEnabled(true);

        absolutePath = (TextView) findViewById(R.id.upload_file_path);
        availableSpace = (TextProgressBar) findViewById(R.id.upload_available_space);

        mDirectory = DEFAULT_INITIAL_DIRECTORY;
        mFiles = new ArrayList<File>();
        ListView listView = (ListView) findViewById(R.id.upload_file_list);
        listView.setOnItemClickListener(new ListOnItemClickListener());
        mAdapter = new UploadListAdapter(this, mFiles);
        listView.setAdapter(mAdapter);

        setAccountInfo();
    }

    @Override
    protected void onResume() {
        refreshFilesList();
        super.onResume();
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
        if(files != null && files.length > 0) {
            for(File f : files) {
                if(f.isHidden() && !mShowHiddenFiles) {
                    continue;
                }

                mFiles.add(f);
            }

            Collections.sort(mFiles, new FileComparator());
        }
        mAdapter.notifyDataSetChanged();
    }

    private void setAvailableSpace(PrimaryAccount primaryAccount) {
        long bytesUsed = Long.parseLong(primaryAccount.getUsedStorage());
        long bytesAvailable = Long.parseLong(primaryAccount.getTotalAvailableStorage());

        int percentUsed = (int) ((bytesUsed * 100) / bytesAvailable);

        availableSpace.setProgress(percentUsed);
        availableSpace.setText(percentUsed + "% lagringsplass brukt");
    }

    private void promtUpload(final File file) {
        AlertDialog.Builder builder = DialogUtitities.getAlertDialogBuilderWithMessage(this, "Er du sikker på at du vil laste opp " + file.getName() + "?");
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                executeUploadTask(file);
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

    private void executeUploadTask(File file) {
        UploadTask uploadTask = new UploadTask(file);
        uploadTask.execute();
    }

    private void setAccountInfo() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    PrimaryAccount primaryAccount = LetterOperations.getPrimaryAccount();
                    setAvailableSpace(primaryAccount);
                    availableSpace.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    availableSpace.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(mDirectory.getParentFile() != null) {
            mDirectory = mDirectory.getParentFile();
            refreshFilesList();
            return;
        }

        super.onBackPressed();
    }

    private class ListOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            File newFile = mAdapter.getItem(position);

            if(newFile.isFile()) {
                promtUpload(newFile);
            } else {
                mDirectory = newFile;
                refreshFilesList();
            }
        }
    }

    private class UploadListAdapter extends ArrayAdapter<File> {

        private ArrayList<File> mObjects;

        public UploadListAdapter(Context context, ArrayList<File> objects) {
            super(context, R.layout.upload_list_item, android.R.id.text1, objects);
            mObjects = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.upload_list_item, parent, false);

            File object = mObjects.get(position);

            TextView name = (TextView)row.findViewById(R.id.upload_file_name);
            TextView size = (TextView)row.findViewById(R.id.upload_file_size);

            name.setText(object.getName());

            if(object.isFile()) {
                name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_grey_file, 0, 0, 0);
                size.setText(DataFormatUtilities.getFormattedFileSize(object.length()));
            } else {
                name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_grey_folder, 0, 0, 0);
            }

            return row;
        }

    }

    private class FileComparator implements Comparator<File> {
        @Override
        public int compare(File f1, File f2) {
            if(f1 == f2) {
                return 0;
            }
            if(f1.isDirectory() && f2.isFile()) {
                return -1;
            }
            if(f1.isFile() && f2.isDirectory()) {
                return 1;
            }

            return f1.getName().compareToIgnoreCase(f2.getName());
        }
    }

    private class UploadTask extends AsyncTask<Void, Void, String> {
        private File file;
        private boolean invalidToken;

        private ProgressDialog progressDialog;

        public UploadTask(File file) {
            this.file = file;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = DialogUtitities.getProgressDialogWithMessage(UploadActivity.this, UploadActivity.this.getString(R.string.uploading));
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
                LetterOperations.uploadFile(UploadActivity.this, file);
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
        protected void onCancelled() {
            super.onCancelled();
            progressDialog.dismiss();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();

            if (result != null) {
                if (invalidToken) {
                    // ToDo logge ut
                }

                DialogUtitities.showToast(UploadActivity.this, result);
            } else {
                DialogUtitities.showToast(UploadActivity.this, file.getName() + " ble lastet opp til arkivet.");
                setAccountInfo();
            }
        }
    }
}
