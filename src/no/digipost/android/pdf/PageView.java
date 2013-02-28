/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	protected Point mSize;
	protected float mSourceScale;

	private ImageView mEntire;
	private Bitmap mEntireBm;
	private SafeAsyncTask<Void, Void, LinkInfo[]> mDrawEntire;

	private Point mPatchViewSize;
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

		mSourceScale = Math.min(mParentSize.x / size.x, mParentSize.y / size.y);
		Point newSize = new Point((int) (size.x * mSourceScale), (int) (size.y * mSourceScale));
		mSize = newSize;

		if (mUsingHardwareAcceleration) {
			mEntire.setImageBitmap(null);
			mEntireBm = null;
		}

		if (mEntireBm == null || mEntireBm.getWidth() != newSize.x || mEntireBm.getHeight() != newSize.y) {
			mEntireBm = Bitmap.createBitmap(mSize.x, mSize.y, Bitmap.Config.ARGB_8888);
		}

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
						paint.setColor(HIGHLIGHT_COLOR);
						for (RectF rect : mSearchBoxes)
							canvas.drawRect(rect.left * scale, rect.top * scale, rect.right * scale, rect.bottom * scale, paint);
					}

					if (!mIsBlank && mLinks != null && mHighlightLinks) {
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
		if (viewArea.width() != mSize.x || viewArea.height() != mSize.y) {
			Point patchViewSize = new Point(viewArea.width(), viewArea.height());
			Rect patchArea = new Rect(0, 0, mParentSize.x, mParentSize.y);

			if (!patchArea.intersect(viewArea))
				return;

			patchArea.offset(-viewArea.left, -viewArea.top);

			if (patchArea.equals(mPatchArea) && patchViewSize.equals(mPatchViewSize))
				return;

			if (mDrawPatch != null) {
				mDrawPatch.cancel(true);
				mDrawPatch = null;
			}

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
					mPatch.layout(mPatchArea.left, mPatchArea.top, mPatchArea.right, mPatchArea.bottom);
					invalidate();
				}
			};

			mDrawPatch.safeExecute(new PatchInfo(bm, patchViewSize, patchArea));
		}
	}

	public void removeHq() {
		if (mDrawPatch != null) {
			mDrawPatch.cancel(true);
			mDrawPatch = null;
		}

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
