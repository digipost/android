package no.digipost.android.gui;

import no.digipost.android.R;
import uk.co.senab.photoview.PhotoViewAttacher;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

public class ImageActivity extends Activity {
	private ImageView imageView;
	private PhotoViewAttacher attacher;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image);

		imageView = (ImageView) findViewById(R.id.image_imageView);

		// Bitmap b = BitmapFactory.decodeStream(inputStream);

		imageView.setImageBitmap(ImageStore.image);

		attacher = new PhotoViewAttacher(imageView);
	}
}
