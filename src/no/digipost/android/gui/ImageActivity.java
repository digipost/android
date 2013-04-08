package no.digipost.android.gui;

import no.digipost.android.R;
import no.digipost.android.api.ApiConstants;
import uk.co.senab.photoview.PhotoViewAttacher;
import uk.co.senab.photoview.PhotoViewAttacher.OnMatrixChangedListener;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ImageActivity extends Activity {
	private ImageView imageView;
	private PhotoViewAttacher attacher;
	private LinearLayout topbar;
	private LinearLayout bottombar;
	private ImageButton toArchive;
	private ImageButton toWorkarea;
	private ImageButton delete;
	private ImageButton digipostIcon;

	private boolean buttonsVisible;
	private boolean created;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image);
		buttonsVisible = true;
		created = false;

		topbar = (LinearLayout) findViewById(R.id.image_topbar);
		bottombar = (LinearLayout) findViewById(R.id.image_bottombar);

		imageView = (ImageView) findViewById(R.id.image_imageView);

		if (ImageStore.image != null) {
			imageView.setImageBitmap(ImageStore.image);
			attacher = new PhotoViewAttacher(imageView);
			attacher.setOnPhotoTapListener(new PhotoTapListener());
			attacher.setOnMatrixChangeListener(new MatrixChangeListener());
		} else {
			finish();
		}

		createButtons();
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

	private void singleLetterOperation(final String action) {
		Intent i = new Intent(ImageActivity.this, BaseActivity.class);
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
			}
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
			System.out.println("matrix changed");

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
