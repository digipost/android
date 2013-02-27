package no.digipost.android.pdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

class PatchInfo {
	public Bitmap bm;
	public Point patchViewSize;
	public Rect patchArea;

	public PatchInfo(final Bitmap aBm, final Point aPatchViewSize, final Rect aPatchArea) {
		bm = aBm;
		patchViewSize = aPatchViewSize;
		patchArea = aPatchArea;
	}
}

// Make our ImageViews opaque to optimize redraw
class OpaqueImageView extends ImageView {

	public OpaqueImageView(final Context context) {
		super(context);
	}

	@Override
	public boolean isOpaque() {
		return true;
	}
}

public abstract class PageView extends ViewGroup {
	private static final int HIGHLIGHT_COLOR = 0x805555FF;
	private static final int LINK_COLOR = 0x80FFCC88;
	private static final int BACKGROUND_COLOR = 0xFFFFFFFF;
	private static final int PROGRESS_DIALOG_DELAY = 200;
	private final Context mContext;
	protected int mPageNumber;
	private final Point mParentSize;
	protected Point mSize; // Size of page at minimum zoom
	protected float mSourceScale;

	private ImageView mEntire; // Image rendered at minimum zoom
	private Bitmap mEntireBm;
	private SafeAsyncTask<Void, Void, LinkInfo[]> mDrawEntire;

	private Point mPatchViewSize; // View size on the basis of which the patch
									// was created
	private Rect mPatchArea;
	private ImageView mPatch;
	private SafeAsyncTask<PatchInfo, Void, PatchInfo> mDrawPatch;
	private RectF mSearchBoxes[];
	private LinkInfo mLinks[];
	private View mSearchView;
	private boolean mIsBlank;
	private final boolean mUsingHardwareAcceleration;
	private boolean mHighlightLinks;

	private ProgressBar mBusyIndicator;
	private final Handler mHandler = new Handler();

	public PageView(final Context c, final Point parentSize) {
		super(c);
		mContext = c;
		mParentSize = parentSize;
		setBackgroundColor(BACKGROUND_COLOR);
		mUsingHardwareAcceleration = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	protected abstract void drawPage(Bitmap bm, int sizeX, int sizeY, int patchX, int patchY, int patchWidth, int patchHeight);

	protected abstract LinkInfo[] getLinkInfo();

	public void releaseResources() {
		// Cancel pending render task
		if (mDrawEntire != null) {
			mDrawEntire.cancel(true);
			mDrawEntire = null;
		}

		if (mDrawPatch != null) {
			mDrawPatch.cancel(true);
			mDrawPatch = null;
		}

		mIsBlank = true;
		mPageNumber = 0;

		if (mSize == null)
			mSize = mParentSize;

		if (mEntire != null)
			mEntire.setImageBitmap(null);

		if (mPatch != null)
			mPatch.setImageBitmap(null);

		if (mBusyIndicator != null) {
			removeView(mBusyIndicator);
			mBusyIndicator = null;
		}
	}

	public void blank(final int page) {
		// Cancel pending render task
		if (mDrawEntire != null) {
			mDrawEntire.cancel(true);
			mDrawEntire = null;
		}

		if (mDrawPatch != null) {
			mDrawPatch.cancel(true);
			mDrawPatch = null;
		}

		mIsBlank = true;
		mPageNumber = page;

		if (mSize == null)
			mSize = mParentSize;

		if (mEntire != null)
			mEntire.setImageBitmap(null);

		if (mPatch != null)
			mPatch.setImageBitmap(null);

		if (mBusyIndicator == null) {
			mBusyIndicator = new ProgressBar(mContext);
			mBusyIndicator.setIndeterminate(true);
			addView(mBusyIndicator);
		}
	}

	public void setPage(final int page, final PointF size) {
		// Cancel pending render task
		if (mDrawEntire != null) {
			mDrawEntire.cancel(true);
			mDrawEntire = null;
		}

		mIsBlank = false;

		mPageNumber = page;
		if (mEntire == null) {
			mEntire = new OpaqueImageView(mContext);
			mEntire.setScaleType(ImageView.ScaleType.FIT_CENTER);
			addView(mEntire);
		}

		// Calculate scaled size that fits within the screen limits
		// This is the size at minimum zoom
		mSourceScale = Math.min(mParentSize.x / size.x, mParentSize.y / size.y);
		Point newSize = new Point((int) (size.x * mSourceScale), (int) (size.y * mSourceScale));
		mSize = newSize;

		if (mUsingHardwareAcceleration) {
			// When hardware accelerated, updates to the bitmap seem to be
			// ignored, so we recreate it. There may be another way around this
			// that we are yet to find.
			mEntire.setImageBitmap(null);
			mEntireBm = null;
		}

		if (mEntireBm == null || mEntireBm.getWidth() != newSize.x || mEntireBm.getHeight() != newSize.y) {
			mEntireBm = Bitmap.createBitmap(mSize.x, mSize.y, Bitmap.Config.ARGB_8888);
		}

		// Render the page in the background
		mDrawEntire = new SafeAsyncTask<Void, Void, LinkInfo[]>() {
			@Override
			protected LinkInfo[] doInBackground(final Void... v) {
				drawPage(mEntireBm, mSize.x, mSize.y, 0, 0, mSize.x, mSize.y);
				return getLinkInfo();
			}

			@Override
			protected void onPreExecute() {
				mEntire.setImageBitmap(null);

				if (mBusyIndicator == null) {
					mBusyIndicator = new ProgressBar(mContext);
					mBusyIndicator.setIndeterminate(true);
					addView(mBusyIndicator);
					mBusyIndicator.setVisibility(INVISIBLE);
					mHandler.postDelayed(new Runnable() {
						public void run() {
							if (mBusyIndicator != null)
								mBusyIndicator.setVisibility(VISIBLE);
						}
					}, PROGRESS_DIALOG_DELAY);
				}
			}

			@Override
			protected void onPostExecute(final LinkInfo[] v) {
				removeView(mBusyIndicator);
				mBusyIndicator = null;
				mEntire.setImageBitmap(mEntireBm);
				mLinks = v;
				invalidate();
			}
		};

		mDrawEntire.safeExecute();

		if (mSearchView == null) {
			mSearchView = new View(mContext) {
				@Override
				protected void onDraw(final Canvas canvas) {
					super.onDraw(canvas);
					float scale = mSourceScale * getWidth() / mSize.x;
					Paint paint = new Paint();

					if (!mIsBlank && mSearchBoxes != null) {
						// Work out current total scale factor
						// from source to view
						paint.setColor(HIGHLIGHT_COLOR);
						for (RectF rect : mSearchBoxes)
							canvas.drawRect(rect.left * scale, rect.top * scale, rect.right * scale, rect.bottom * scale, paint);
					}

					if (!mIsBlank && mLinks != null && mHighlightLinks) {
						// Work out current total scale factor
						// from source to view
						paint.setColor(LINK_COLOR);
						for (RectF rect : mLinks)
							canvas.drawRect(rect.left * scale, rect.top * scale, rect.right * scale, rect.bottom * scale, paint);
					}
				}
			};

			addView(mSearchView);
		}
		requestLayout();
	}

	public void setSearchBoxes(final RectF searchBoxes[]) {
		mSearchBoxes = searchBoxes;
		if (mSearchView != null)
			mSearchView.invalidate();
	}

	public void setLinkHighlighting(final boolean f) {
		mHighlightLinks = f;
		if (mSearchView != null)
			mSearchView.invalidate();
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		int x, y;
		switch (View.MeasureSpec.getMode(widthMeasureSpec)) {
		case View.MeasureSpec.UNSPECIFIED:
			x = mSize.x;
			break;
		default:
			x = View.MeasureSpec.getSize(widthMeasureSpec);
		}
		switch (View.MeasureSpec.getMode(heightMeasureSpec)) {
		case View.MeasureSpec.UNSPECIFIED:
			y = mSize.y;
			break;
		default:
			y = View.MeasureSpec.getSize(heightMeasureSpec);
		}

		setMeasuredDimension(x, y);

		if (mBusyIndicator != null) {
			int limit = Math.min(mParentSize.x, mParentSize.y) / 2;
			mBusyIndicator.measure(View.MeasureSpec.AT_MOST | limit, View.MeasureSpec.AT_MOST | limit);
		}
	}

	@Override
	protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
		int w = right - left;
		int h = bottom - top;

		if (mEntire != null) {
			mEntire.layout(0, 0, w, h);
		}

		if (mSearchView != null) {
			mSearchView.layout(0, 0, w, h);
		}

		if (mPatchViewSize != null) {
			if (mPatchViewSize.x != w || mPatchViewSize.y != h) {
				// Zoomed since patch was created
				mPatchViewSize = null;
				mPatchArea = null;
				if (mPatch != null)
					mPatch.setImageBitmap(null);
			} else {
				mPatch.layout(mPatchArea.left, mPatchArea.top, mPatchArea.right, mPatchArea.bottom);
			}
		}

		if (mBusyIndicator != null) {
			int bw = mBusyIndicator.getMeasuredWidth();
			int bh = mBusyIndicator.getMeasuredHeight();

			mBusyIndicator.layout((w - bw) / 2, (h - bh) / 2, (w + bw) / 2, (h + bh) / 2);
		}
	}

	public void addHq() {
		Rect viewArea = new Rect(getLeft(), getTop(), getRight(), getBottom());
		// If the viewArea's size matches the unzoomed size, there is no need
		// for an hq patch
		if (viewArea.width() != mSize.x || viewArea.height() != mSize.y) {
			Point patchViewSize = new Point(viewArea.width(), viewArea.height());
			Rect patchArea = new Rect(0, 0, mParentSize.x, mParentSize.y);

			// Intersect and test that there is an intersection
			if (!patchArea.intersect(viewArea))
				return;

			// Offset patch area to be relative to the view top left
			patchArea.offset(-viewArea.left, -viewArea.top);

			// If being asked for the same area as last time, nothing to do
			if (patchArea.equals(mPatchArea) && patchViewSize.equals(mPatchViewSize))
				return;

			// Stop the drawing of previous patch if still going
			if (mDrawPatch != null) {
				mDrawPatch.cancel(true);
				mDrawPatch = null;
			}

			// Create and add the image view if not already done
			if (mPatch == null) {
				mPatch = new OpaqueImageView(mContext);
				mPatch.setScaleType(ImageView.ScaleType.FIT_CENTER);
				addView(mPatch);
				mSearchView.bringToFront();
			}

			Bitmap bm = Bitmap.createBitmap(patchArea.width(), patchArea.height(), Bitmap.Config.ARGB_8888);

			mDrawPatch = new SafeAsyncTask<PatchInfo, Void, PatchInfo>() {
				@Override
				protected PatchInfo doInBackground(final PatchInfo... v) {
					drawPage(v[0].bm, v[0].patchViewSize.x, v[0].patchViewSize.y, v[0].patchArea.left, v[0].patchArea.top,
							v[0].patchArea.width(), v[0].patchArea.height());
					return v[0];
				}

				@Override
				protected void onPostExecute(final PatchInfo v) {
					mPatchViewSize = v.patchViewSize;
					mPatchArea = v.patchArea;
					mPatch.setImageBitmap(v.bm);
					// requestLayout();
					// Calling requestLayout here doesn't lead to a later call
					// to layout. No idea
					// why, but apparently others have run into the problem.
					mPatch.layout(mPatchArea.left, mPatchArea.top, mPatchArea.right, mPatchArea.bottom);
					invalidate();
				}
			};

			mDrawPatch.safeExecute(new PatchInfo(bm, patchViewSize, patchArea));
		}
	}

	public void removeHq() {
		// Stop the drawing of the patch if still going
		if (mDrawPatch != null) {
			mDrawPatch.cancel(true);
			mDrawPatch = null;
		}

		// And get rid of it
		mPatchViewSize = null;
		mPatchArea = null;
		if (mPatch != null)
			mPatch.setImageBitmap(null);
	}

	public int getPage() {
		return mPageNumber;
	}

	@Override
	public boolean isOpaque() {
		return true;
	}
}
