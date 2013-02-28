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

import java.util.LinkedList;
import java.util.NoSuchElementException;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Scroller;

public class ReaderView extends AdapterView<Adapter> implements GestureDetector.OnGestureListener,
		ScaleGestureDetector.OnScaleGestureListener, Runnable {
	private static final int MOVING_DIAGONALLY = 0;
	private static final int MOVING_LEFT = 1;
	private static final int MOVING_RIGHT = 2;
	private static final int MOVING_UP = 3;
	private static final int MOVING_DOWN = 4;

	private static final int FLING_MARGIN = 100;
	private static final int GAP = 20;
	private static final int SCROLL_SPEED = 2;

	private static final float MIN_SCALE = 1.0f;
	private static final float MAX_SCALE = 5.0f;

	private Adapter mAdapter;
	private int mCurrent;
	private boolean mResetLayout;
	private final SparseArray<View> mChildViews = new SparseArray<View>(3);
	private final LinkedList<View> mViewCache = new LinkedList<View>();
	private boolean mUserInteracting;
	private boolean mScaling;
	private float mScale = 1.0f;
	private int mXScroll;
	private int mYScroll;
	private final GestureDetector mGestureDetector;
	private final ScaleGestureDetector mScaleGestureDetector;
	private final Scroller mScroller;
	private int mScrollerLastX;
	private int mScrollerLastY;
	private boolean mScrollDisabled;

	public ReaderView(final Context context) {
		super(context);
		mGestureDetector = new GestureDetector(this);
		mScaleGestureDetector = new ScaleGestureDetector(context, this);
		mScroller = new Scroller(context);
	}

	public ReaderView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		mGestureDetector = new GestureDetector(this);
		mScaleGestureDetector = new ScaleGestureDetector(context, this);
		mScroller = new Scroller(context);
	}

	public ReaderView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		mGestureDetector = new GestureDetector(this);
		mScaleGestureDetector = new ScaleGestureDetector(context, this);
		mScroller = new Scroller(context);
	}

	public int getDisplayedViewIndex() {
		return mCurrent;
	}

	public void setDisplayedViewIndex(final int i) {
		if (0 <= i && i < mAdapter.getCount()) {
			mCurrent = i;
			onMoveToChild(i);
			mResetLayout = true;
			requestLayout();
		}
	}

	public void moveToNext() {
		View v = mChildViews.get(mCurrent + 1);
		if (v != null)
			slideViewOntoScreen(v);
	}

	public void moveToPrevious() {
		View v = mChildViews.get(mCurrent - 1);
		if (v != null)
			slideViewOntoScreen(v);
	}

	public void resetupChildren() {
		for (int i = 0; i < mChildViews.size(); i++)
			onChildSetup(mChildViews.keyAt(i), mChildViews.valueAt(i));
	}

	protected void onChildSetup(final int i, final View v) {
	}

	protected void onMoveToChild(final int i) {
	}

	protected void onSettle(final View v) {
	};

	protected void onUnsettle(final View v) {
	};

	protected void onNotInUse(final View v) {
	};

	public View getDisplayedView() {
		return mChildViews.get(mCurrent);
	}

	public void run() {
		if (!mScroller.isFinished()) {
			mScroller.computeScrollOffset();
			int x = mScroller.getCurrX();
			int y = mScroller.getCurrY();
			mXScroll += x - mScrollerLastX;
			mYScroll += y - mScrollerLastY;
			mScrollerLastX = x;
			mScrollerLastY = y;
			requestLayout();
			post(this);
		} else if (!mUserInteracting) {
			View v = mChildViews.get(mCurrent);
			postSettle(v);
		}
	}

	public boolean onDown(final MotionEvent arg0) {
		mScroller.forceFinished(true);
		return true;
	}

	public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX, final float velocityY) {
		if (mScrollDisabled)
			return true;

		View v = mChildViews.get(mCurrent);
		if (v != null) {
			Rect bounds = getScrollBounds(v);
			switch (directionOfTravel(velocityX, velocityY)) {
			case MOVING_LEFT:
				if (bounds.left >= 0) {
					View vl = mChildViews.get(mCurrent + 1);

					if (vl != null) {
						slideViewOntoScreen(vl);
						return true;
					}
				}
				break;
			case MOVING_RIGHT:
				if (bounds.right <= 0) {
					View vr = mChildViews.get(mCurrent - 1);

					if (vr != null) {
						slideViewOntoScreen(vr);
						return true;
					}
				}
				break;
			}
			mScrollerLastX = mScrollerLastY = 0;
			Rect expandedBounds = new Rect(bounds);
			expandedBounds.inset(-FLING_MARGIN, -FLING_MARGIN);

			if (withinBoundsInDirectionOfTravel(bounds, velocityX, velocityY) && expandedBounds.contains(0, 0)) {
				mScroller.fling(0, 0, (int) velocityX, (int) velocityY, bounds.left, bounds.right, bounds.top, bounds.bottom);
				post(this);
			}
		}

		return true;
	}

	public void onLongPress(final MotionEvent e) {
	}

	public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY) {
		if (!mScrollDisabled) {
			mXScroll -= distanceX;
			mYScroll -= distanceY;
			requestLayout();
		}
		return true;
	}

	public void onShowPress(final MotionEvent e) {
	}

	public boolean onSingleTapUp(final MotionEvent e) {
		return false;
	}

	public boolean onScale(final ScaleGestureDetector detector) {
		float previousScale = mScale;
		mScale = Math.min(Math.max(mScale * detector.getScaleFactor(), MIN_SCALE), MAX_SCALE);
		float factor = mScale / previousScale;

		View v = mChildViews.get(mCurrent);
		if (v != null) {
			int viewFocusX = (int) detector.getFocusX() - (v.getLeft() + mXScroll);
			int viewFocusY = (int) detector.getFocusY() - (v.getTop() + mYScroll);
			mXScroll += viewFocusX - viewFocusX * factor;
			mYScroll += viewFocusY - viewFocusY * factor;
			requestLayout();
		}
		return true;
	}

	public boolean onScaleBegin(final ScaleGestureDetector detector) {
		mScaling = true;
		mXScroll = mYScroll = 0;
		mScrollDisabled = true;
		return true;
	}

	public void onScaleEnd(final ScaleGestureDetector detector) {
		mScaling = false;
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		mScaleGestureDetector.onTouchEvent(event);

		if (!mScaling)
			mGestureDetector.onTouchEvent(event);

		if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
			mUserInteracting = true;
		}
		if (event.getActionMasked() == MotionEvent.ACTION_UP) {
			mScrollDisabled = false;
			mUserInteracting = false;

			View v = mChildViews.get(mCurrent);
			if (v != null) {
				if (mScroller.isFinished()) {
					slideViewOntoScreen(v);
				}

				if (mScroller.isFinished()) {
					postSettle(v);
				}
			}
		}

		requestLayout();
		return true;
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int n = getChildCount();
		for (int i = 0; i < n; i++)
			measureView(getChildAt(i));
	}

	@Override
	protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		View cv = mChildViews.get(mCurrent);
		Point cvOffset;

		if (!mResetLayout) {
			if (cv != null) {
				cvOffset = subScreenSizeOffset(cv);
				if (cv.getLeft() + cv.getMeasuredWidth() + cvOffset.x + GAP / 2 + mXScroll < getWidth() / 2
						&& mCurrent + 1 < mAdapter.getCount()) {
					postUnsettle(cv);
					post(this);

					mCurrent++;
					onMoveToChild(mCurrent);
				}

				if (cv.getLeft() - cvOffset.x - GAP / 2 + mXScroll >= getWidth() / 2 && mCurrent > 0) {
					postUnsettle(cv);
					post(this);

					mCurrent--;
					onMoveToChild(mCurrent);
				}
			}

			int numChildren = mChildViews.size();
			int childIndices[] = new int[numChildren];
			for (int i = 0; i < numChildren; i++)
				childIndices[i] = mChildViews.keyAt(i);

			for (int i = 0; i < numChildren; i++) {
				int ai = childIndices[i];
				if (ai < mCurrent - 1 || ai > mCurrent + 1) {
					View v = mChildViews.get(ai);
					onNotInUse(v);
					mViewCache.add(v);
					removeViewInLayout(v);
					mChildViews.remove(ai);
				}
			}
		} else {
			mResetLayout = false;
			mXScroll = mYScroll = 0;

			int numChildren = mChildViews.size();
			for (int i = 0; i < numChildren; i++) {
				View v = mChildViews.valueAt(i);
				onNotInUse(v);
				mViewCache.add(v);
				removeViewInLayout(v);
			}
			mChildViews.clear();
			post(this);
		}

		int cvLeft, cvRight, cvTop, cvBottom;
		boolean notPresent = (mChildViews.get(mCurrent) == null);
		cv = getOrCreateChild(mCurrent);
		cvOffset = subScreenSizeOffset(cv);
		if (notPresent) {
			cvLeft = cvOffset.x;
			cvTop = cvOffset.y;
		} else {
			cvLeft = cv.getLeft() + mXScroll;
			cvTop = cv.getTop() + mYScroll;
		}
		mXScroll = mYScroll = 0;
		cvRight = cvLeft + cv.getMeasuredWidth();
		cvBottom = cvTop + cv.getMeasuredHeight();

		if (!mUserInteracting && mScroller.isFinished()) {
			Point corr = getCorrection(getScrollBounds(cvLeft, cvTop, cvRight, cvBottom));
			cvRight += corr.x;
			cvLeft += corr.x;
			cvTop += corr.y;
			cvBottom += corr.y;
		} else if (cv.getMeasuredHeight() <= getHeight()) {
			Point corr = getCorrection(getScrollBounds(cvLeft, cvTop, cvRight, cvBottom));
			cvTop += corr.y;
			cvBottom += corr.y;
		}

		cv.layout(cvLeft, cvTop, cvRight, cvBottom);

		if (mCurrent > 0) {
			View lv = getOrCreateChild(mCurrent - 1);
			Point leftOffset = subScreenSizeOffset(lv);
			int gap = leftOffset.x + GAP + cvOffset.x;
			lv.layout(cvLeft - lv.getMeasuredWidth() - gap, (cvBottom + cvTop - lv.getMeasuredHeight()) / 2, cvLeft - gap, (cvBottom
					+ cvTop + lv.getMeasuredHeight()) / 2);
		}

		if (mCurrent + 1 < mAdapter.getCount()) {
			View rv = getOrCreateChild(mCurrent + 1);
			Point rightOffset = subScreenSizeOffset(rv);
			int gap = cvOffset.x + GAP + rightOffset.x;
			rv.layout(cvRight + gap, (cvBottom + cvTop - rv.getMeasuredHeight()) / 2, cvRight + rv.getMeasuredWidth() + gap, (cvBottom
					+ cvTop + rv.getMeasuredHeight()) / 2);
		}

		invalidate();
	}

	@Override
	public Adapter getAdapter() {
		return mAdapter;
	}

	@Override
	public View getSelectedView() {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public void setAdapter(final Adapter adapter) {
		mAdapter = adapter;
		mChildViews.clear();
		removeAllViewsInLayout();
		requestLayout();
	}

	@Override
	public void setSelection(final int arg0) {
		throw new UnsupportedOperationException("Not supported");
	}

	private View getCached() {
		if (mViewCache.size() == 0)
			return null;
		else
			return mViewCache.removeFirst();
	}

	private View getOrCreateChild(final int i) {
		View v = mChildViews.get(i);
		if (v == null) {
			v = mAdapter.getView(i, getCached(), this);
			addAndMeasureChild(i, v);
		}
		onChildSetup(i, v);

		return v;
	}

	private void addAndMeasureChild(final int i, final View v) {
		LayoutParams params = v.getLayoutParams();
		if (params == null) {
			params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		}
		addViewInLayout(v, 0, params, true);
		mChildViews.append(i, v);
		measureView(v);
	}

	private void measureView(final View v) {
		v.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		float scale = Math.min((float) getWidth() / (float) v.getMeasuredWidth(), (float) getHeight() / (float) v.getMeasuredHeight());
		v.measure(View.MeasureSpec.EXACTLY | (int) (v.getMeasuredWidth() * scale * mScale),
				View.MeasureSpec.EXACTLY | (int) (v.getMeasuredHeight() * scale * mScale));
	}

	private Rect getScrollBounds(final int left, final int top, final int right, final int bottom) {
		int xmin = getWidth() - right;
		int xmax = -left;
		int ymin = getHeight() - bottom;
		int ymax = -top;

		if (xmin > xmax)
			xmin = xmax = (xmin + xmax) / 2;
		if (ymin > ymax)
			ymin = ymax = (ymin + ymax) / 2;

		return new Rect(xmin, ymin, xmax, ymax);
	}

	private Rect getScrollBounds(final View v) {
		return getScrollBounds(v.getLeft() + mXScroll, v.getTop() + mYScroll, v.getLeft() + v.getMeasuredWidth() + mXScroll,
				v.getTop() + v.getMeasuredHeight() + mYScroll);
	}

	private Point getCorrection(final Rect bounds) {
		return new Point(Math.min(Math.max(0, bounds.left), bounds.right), Math.min(Math.max(0, bounds.top), bounds.bottom));
	}

	private void postSettle(final View v) {
		post(new Runnable() {
			public void run() {
				onSettle(v);
			}
		});
	}

	private void postUnsettle(final View v) {
		post(new Runnable() {
			public void run() {
				onUnsettle(v);
			}
		});
	}

	private void slideViewOntoScreen(final View v) {
		Point corr = getCorrection(getScrollBounds(v));
		if (corr.x != 0 || corr.y != 0) {
			mScrollerLastX = mScrollerLastY = 0;
			mScroller.startScroll(0, 0, corr.x, corr.y, 400);
			post(this);
		}
	}

	private Point subScreenSizeOffset(final View v) {
		return new Point(Math.max((getWidth() - v.getMeasuredWidth()) / 2, 0), Math.max((getHeight() - v.getMeasuredHeight()) / 2, 0));
	}

	private static int directionOfTravel(final float vx, final float vy) {
		if (Math.abs(vx) > 2 * Math.abs(vy))
			return (vx > 0) ? MOVING_RIGHT : MOVING_LEFT;
		else if (Math.abs(vy) > 2 * Math.abs(vx))
			return (vy > 0) ? MOVING_DOWN : MOVING_UP;
		else
			return MOVING_DIAGONALLY;
	}

	private static boolean withinBoundsInDirectionOfTravel(final Rect bounds, final float vx, final float vy) {
		switch (directionOfTravel(vx, vy)) {
		case MOVING_DIAGONALLY:
			return bounds.contains(0, 0);
		case MOVING_LEFT:
			return bounds.left <= 0;
		case MOVING_RIGHT:
			return bounds.right >= 0;
		case MOVING_UP:
			return bounds.top <= 0;
		case MOVING_DOWN:
			return bounds.bottom >= 0;
		default:
			throw new NoSuchElementException();
		}
	}
}
