package no.digipost.android.gui;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;

import no.digipost.android.R;
import no.digipost.android.documentstore.UnsupportedFileStore;
import no.digipost.android.utilities.FileUtilities;

public class UnsupportedDocumentFormatActivity extends Activity {
    private ImageButton toArchive;
    private ImageButton toWorkarea;
    private ImageButton delete;
    private ImageButton digipostIcon;
    private Button open;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unsupported);

        createButtons();
        createUI();
    }

    private void createUI() {
        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (UnsupportedFileStore.unsupportedDocument != null) {
                    openFileWithIntent(UnsupportedFileStore.unsupportedDocumentFileType, UnsupportedFileStore.unsupportedDocument);
                } else {
                    showMessage("Kunne ikke åpne dokumentet");
                    finish();
                }
            }
        });
    }

    private void createButtons() {
        toArchive = (ImageButton) findViewById(R.id.unsupported_toArchive);
        toWorkarea = (ImageButton) findViewById(R.id.unsupported_toWorkarea);
        delete = (ImageButton) findViewById(R.id.unsupported_delete);
        digipostIcon = (ImageButton) findViewById(R.id.unsupported_delete);
        open = (Button) findViewById(R.id.unsupported_openBtn);
    }

    private void openFileWithIntent(final String documentFileType, final byte[] data) {
        try {
            FileUtilities.openFileWithIntent(this, documentFileType, data);
        } catch (IOException e) {
            showMessage(getString(R.string.error_failed_to_open_with_intent));
        } catch (ActivityNotFoundException e) {
            showMessage(getString(R.string.error_no_activity_to_open_file));
        }
    }

    private void showMessage(final String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isFinishing()) {
            UnsupportedFileStore.unsupportedDocument = null;
            UnsupportedFileStore.unsupportedDocumentFileType = null;
        } else {
            FileUtilities.deleteTempFiles();
        }
    }
}
