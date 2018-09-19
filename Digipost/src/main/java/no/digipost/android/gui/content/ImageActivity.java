package no.digipost.android.gui.content;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

import com.google.android.gms.analytics.GoogleAnalytics;

import no.digipost.android.DigipostApplication;
import no.digipost.android.R;
import no.digipost.android.documentstore.DocumentContentStore;
import no.digipost.android.utilities.FileUtilities;

public class ImageActivity extends DisplayContentActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((DigipostApplication) getApplication()).getTracker(DigipostApplication.TrackerName.APP_TRACKER);
        setContentView(R.layout.activity_image);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setActionBar(DocumentContentStore.getDocumentAttachment().getSubject());

        imageView = (ImageView) findViewById(R.id.image_layout);
        loadContent();
    }

    private void loadContent() {
        byte[] image = DocumentContentStore.getDocumentContent();
        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
        //Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, imageView.getWidth(), imageView.getHeight(),false);
        imageView.setImageBitmap(bitmap);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_image_html_unsupported_actionbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_image_html_unsupported_open_external).setVisible(false);
        menu.findItem(R.id.menu_image_html_unsupported_save).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_image_html_unsupported_delete:
                deleteAction(this);
                return true;
            case R.id.menu_image_html_move:
                showMoveToFolderDialog();
                return true;
            case R.id.menu_image_html_unsupported_open_external:
                super.openFileWithIntent();
                return true;
            case R.id.menu_image_html_unsupported_save:
                super.downloadFile();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isFinishing()) {
            FileUtilities.deleteTempFiles();
        }
    }

}
