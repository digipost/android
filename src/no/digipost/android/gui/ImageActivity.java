package no.digipost.android.gui;

import no.digipost.android.R;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.documentstore.ImageStore;
import no.digipost.android.documentstore.UnsupportedFileStore;
import no.digipost.android.utilities.FileUtilities;
import uk.co.senab.photoview.PhotoViewAttacher;
import uk.co.senab.photoview.PhotoViewAttacher.OnMatrixChangedListener;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ImageActivity extends Activity {
	private ImageView imageView;
	private PhotoViewAttacher attacher;
	private RelativeLayout topbar;
	private LinearLayout bottombar;
	private ImageButton toArchive;
	private ImageButton toWorkarea;
	private ImageButton delete;
	private ImageButton digipostIcon;
    private ImageButton optionsButton;

	private boolean buttonsVisible;
	private boolean created;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image);
		buttonsVisible = true;
		created = false;

		topbar = (RelativeLayout) findViewById(R.id.image_topbar);
		bottombar = (LinearLayout) findViewById(R.id.image_bottombar);

		imageView = (ImageView) findViewById(R.id.image_imageView);

        Bitmap bitmap = null;

        try{
            bitmap = BitmapFactory.decodeByteArray(ImageStore.image, 0, ImageStore.image.length);
        }catch(OutOfMemoryError e){
            showMessage(getString(R.string.error_inputstreamtobyarray));
        }

		if (ImageStore.image != null) {
			imageView.setImageBitmap(bitmap);
			attacher = new PhotoViewAttacher(imageView);
			attacher.setOnPhotoTapListener(new PhotoTapListener());
			attacher.setOnMatrixChangeListener(new MatrixChangeListener());
		} else {
			AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
			AlertDialog alert = alertBuilder.create();
			alert.setTitle(R.string.image_open_failed);
			alert.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.close), new DialogInterface.OnClickListener() {
				public void onClick(final DialogInterface dialog, final int which) {
					finish();
				}
			});
			alert.show();
			return;
		}

		createButtons();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (isFinishing()) {
			if (attacher != null) {
				attacher.cleanup();
			}

			attacher = null;
			imageView = null;
			ImageStore.image = null;
            FileUtilities.deleteTempFiles();
		}
	}

	private void createButtons() {
		ButtonListener buttonListener = new ButtonListener();

		toArchive = (ImageButton) findViewById(R.id.image_toArchive);
		toArchive.setOnClickListener(buttonListener);
		toWorkarea = (ImageButton) findViewById(R.id.image_toWorkarea);
		toWorkarea.setOnClickListener(buttonListener);
		delete = (ImageButton) findViewById(R.id.image_delete);
		delete.setOnClickListener(buttonListener);
		digipostIcon = (ImageButton) findViewById(R.id.image_digipost_icon);
		digipostIcon.setOnClickListener(buttonListener);
        optionsButton = (ImageButton) findViewById(R.id.image_optionsbtn);
        optionsButton.setOnClickListener(buttonListener);

		String from = getIntent().getExtras().getString(ApiConstants.LOCATION_FROM);

		if (from.equals(ApiConstants.LOCATION_ARCHIVE)) {
			toArchive.setVisibility(View.GONE);
		} else if (from.equals(ApiConstants.LOCATION_WORKAREA)) {
			toWorkarea.setVisibility(View.GONE);
		}
	}

	private void showButtons() {
		buttonsVisible = true;

		Animation anim = new TranslateAnimation(0, 0, -topbar.getHeight(), 0);
		anim.setDuration(200);
		anim.setAnimationListener(new Animation.AnimationListener() {
			public void onAnimationStart(final Animation animation) {
				topbar.setVisibility(View.VISIBLE);
			}

			public void onAnimationRepeat(final Animation animation) {
			}

			public void onAnimationEnd(final Animation animation) {
			}
		});
		topbar.startAnimation(anim);

		anim = new TranslateAnimation(0, 0, bottombar.getHeight(), 0);
		anim.setDuration(200);
		anim.setAnimationListener(new Animation.AnimationListener() {
			public void onAnimationStart(final Animation animation) {
				bottombar.setVisibility(View.VISIBLE);
			}

			public void onAnimationRepeat(final Animation animation) {
			}

			public void onAnimationEnd(final Animation animation) {
			}
		});
		bottombar.startAnimation(anim);

	}

	private void hideButtons() {
		buttonsVisible = false;

		Animation anim = new TranslateAnimation(0, 0, 0, -topbar.getHeight());
		anim.setDuration(200);
		anim.setAnimationListener(new Animation.AnimationListener() {
			public void onAnimationStart(final Animation animation) {
			}

			public void onAnimationRepeat(final Animation animation) {
			}

			public void onAnimationEnd(final Animation animation) {
				topbar.setVisibility(View.INVISIBLE);
			}
		});
		topbar.startAnimation(anim);

		anim = new TranslateAnimation(0, 0, 0, bottombar.getHeight());
		anim.setDuration(200);
		anim.setAnimationListener(new Animation.AnimationListener() {
			public void onAnimationStart(final Animation animation) {
			}

			public void onAnimationRepeat(final Animation animation) {
			}

			public void onAnimationEnd(final Animation animation) {
				bottombar.setVisibility(View.INVISIBLE);
			}
		});
		bottombar.startAnimation(anim);
	}

    public void showMenu(final View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

            public boolean onMenuItemClick(final MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.showdocumentmenu_open_external:
                        openFileWithIntent(ImageStore.imageFileType, ImageStore.image);
                        return true;
                    case R.id.showdocumentmenu_save:
                        promtSaveToSD();
                        return true;
                }
                return false;
            }
        });
        popup.inflate(R.menu.activity_showdocument_general);
        popup.show();
    }

	private void singleLetterOperation(final String action) {
		Intent i = new Intent(ImageActivity.this, BaseFragmentActivity.class);
		i.putExtra(ApiConstants.ACTION, action);
		i.putExtra(ApiConstants.DOCUMENT_TYPE, ApiConstants.LETTER);
		setResult(RESULT_OK, i);
		finish();
	}

	private void showWarning(final String text, final String action) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(text).setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int id) {
				singleLetterOperation(action);
				dialog.dismiss();
			}
		}).setCancelable(false).setNegativeButton(getString(R.string.abort), new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

    private void showMessage(final String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();
    }

	private class ButtonListener implements OnClickListener {

		public void onClick(final View v) {
			String message = "";

			if (v.equals(toArchive)) {
				message = getString(R.string.dialog_prompt_image_toArchive);
				showWarning(message, ApiConstants.LOCATION_ARCHIVE);
			} else if (v.equals(toWorkarea)) {
				message = getString(R.string.dialog_prompt_image_toWorkarea);
				showWarning(message, ApiConstants.LOCATION_WORKAREA);
			} else if (v.equals(digipostIcon)) {
				finish();
			} else if (v.equals(delete)) {
				message = getString(R.string.dialog_prompt_delete_image);
				showWarning(message, ApiConstants.DELETE);
			} else if (v.equals(optionsButton)) {
                showMenu(optionsButton);
            }
		}
	}

    private void openFileWithIntent(final String documentFileType, final byte[] data) {
        if (data == null) {
            showMessage(getString(R.string.error_failed_to_open_with_intent));
            finish();
        }

        try {
            FileUtilities.openFileWithIntent(this, documentFileType, data);
        } catch (IOException e) {
            showMessage(getString(R.string.error_failed_to_open_with_intent));
        } catch (ActivityNotFoundException e) {
            showMessage(getString(R.string.error_no_activity_to_open_file));
        }
    }

    private void promtSaveToSD() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.pdf_promt_save_to_sd).setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                new SaveDocumentToSDTask().execute();
                dialog.dismiss();
            }
        }).setCancelable(false).setNegativeButton(getString(R.string.abort), new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private class SaveDocumentToSDTask extends AsyncTask<Void, Void, Boolean> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(ImageActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getString(R.string.loading_content));
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... parameters) {
            File file = null;

            try {
                file = FileUtilities.writeFileToSD(ImageStore.imageName, ImageStore.imageFileType, ImageStore.image);
            } catch (IOException e) {
                return false;
            }

            FileUtilities.makeFileVisible(ImageActivity.this, file);

            return true;
        }

        @Override
        protected void onPostExecute(Boolean saved) {
            super.onPostExecute(saved);

            if (saved) {
                showMessage(getString(R.string.pdf_saved_to_sd));
            } else {
                showMessage(getString(R.string.pdf_save_to_sd_failed));
            }

            progressDialog.dismiss();
        }
    }

	private class PhotoTapListener implements OnPhotoTapListener {

		public void onPhotoTap(final View view, final float x, final float y) {
			if (buttonsVisible) {
				hideButtons();
			} else {
				showButtons();
			}
		}
	}

	private class MatrixChangeListener implements OnMatrixChangedListener {

		public void onMatrixChanged(final RectF rect) {
			if (buttonsVisible) {
				if (created) {
					hideButtons();
				} else {
					created = true;
				}
			}
		}
	}
}
