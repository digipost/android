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

package no.digipost.android.gui;

import java.text.SimpleDateFormat;
import java.util.Date;

import no.digipost.android.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PullToRefreshListView extends ListView {

	private static final float PULL_RESISTANCE = 1.7f;
	private static final int BOUNCE_ANIMATION_DURATION = 700;
	private static final int BOUNCE_ANIMATION_DELAY = 100;
	private static final float BOUNCE_OVERSHOOT_TENSION = 1.4f;
	private static final int ROTATE_ARROW_ANIMATION_DURATION = 250;

	private static enum State {
		PULL_TO_REFRESH,
		RELEASE_TO_REFRESH,
		REFRESHING
	}

	/**
	 * Interface to implement when you want to get notified of 'pull to refresh'
	 * events. Call setOnRefreshListener(..) to activate an OnRefreshListener.
	 */
	public interface OnRefreshListener {

		/**
		 * Method to be called when a refresh is requested
		 */
		public void onRefresh();
	}

	private static int measuredHeaderHeight;

	private boolean scrollbarEnabled;
	private boolean bounceBackHeader;
	private boolean lockScrollWhileRefreshing;
	private boolean showLastUpdatedText;
	private String pullToRefreshText;
	private String releaseToRefreshText;
	private String refreshingText;
	private String lastUpdatedText;
	private SimpleDateFormat lastUpdatedDateFormat = new SimpleDateFormat("dd/MM HH:mm");

	private float previousY;
	private int headerPadding;
	private boolean hasResetHeader;
	private long lastUpdated = -1;
	private State state;
	private LinearLayout headerContainer;
	private RelativeLayout header;
	private RotateAnimation flipAnimation;
	private RotateAnimation reverseFlipAnimation;
	private ImageView image;
	private ProgressBar spinner;
	private TextView text;
	private TextView lastUpdatedTextView;
	private OnItemClickListener onItemClickListener;
	private OnItemLongClickListener onItemLongClickListener;
	private OnRefreshListener onRefreshListener;

	public PullToRefreshListView(final Context context) {
		super(context);
		init();
	}

	public PullToRefreshListView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PullToRefreshListView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	@Override
	public void setOnItemClickListener(final OnItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
	}

	@Override
	public void setOnItemLongClickListener(final OnItemLongClickListener onItemLongClickListener) {
		this.onItemLongClickListener = onItemLongClickListener;
	}

	/**
	 * Activate an OnRefreshListener to get notified on 'pull to refresh'
	 * events.
	 * 
	 * @param onRefreshListener
	 *            The OnRefreshListener to get notified
	 */
	public void setOnRefreshListener(final OnRefreshListener onRefreshListener) {
		this.onRefreshListener = onRefreshListener;
	}

	/**
	 * @return If the list is in 'Refreshing' state
	 */
	public boolean isRefreshing() {
		return state == State.REFRESHING;
	}

	/**
	 * Default is false. When lockScrollWhileRefreshing is set to true, the list
	 * cannot scroll when in 'refreshing' mode. It's 'locked' on refreshing.
	 * 
	 * @param lockScrollWhileRefreshing
	 */
	public void setLockScrollWhileRefreshing(final boolean lockScrollWhileRefreshing) {
		this.lockScrollWhileRefreshing = lockScrollWhileRefreshing;
	}

	/**
	 * Default is false. Show the last-updated date/time in the 'Pull ro
	 * Refresh' header. See 'setLastUpdatedDateFormat' to set the date/time
	 * formatting.
	 * 
	 * @param showLastUpdatedText
	 */
	public void setShowLastUpdatedText(final boolean showLastUpdatedText) {
		this.showLastUpdatedText = showLastUpdatedText;
		if (!showLastUpdatedText)
			lastUpdatedTextView.setVisibility(View.GONE);
	}

	/**
	 * Default: "dd/MM HH:mm". Set the format in which the last-updated
	 * date/time is shown. Meaningless if 'showLastUpdatedText == false
	 * (default)'. See 'setShowLastUpdatedText'.
	 * 
	 * @param lastUpdatedDateFormat
	 */
	public void setLastUpdatedDateFormat(final SimpleDateFormat lastUpdatedDateFormat) {
		this.lastUpdatedDateFormat = lastUpdatedDateFormat;
	}

	/**
	 * Explicitly set the state to refreshing. This is useful when you want to
	 * show the spinner and 'Refreshing' text when the refresh was not triggered
	 * by 'pull to refresh', for example on start.
	 */
	public void setRefreshing() {
		state = State.REFRESHING;
		scrollTo(0, 0);
		setUiRefreshing();
		setHeaderPadding(0);
	}

	/**
	 * Set the state back to 'pull to refresh'. Call this method when refreshing
	 * the data is finished.
	 */
	public void onRefreshComplete() {
		state = State.PULL_TO_REFRESH;
		resetHeader();
		lastUpdated = System.currentTimeMillis();
	}

	/**
	 * Change the label text on state 'Pull to Refresh'
	 * 
	 * @param pullToRefreshText
	 *            Text
	 */
	public void setTextPullToRefresh(final String pullToRefreshText) {
		this.pullToRefreshText = pullToRefreshText;
		if (state == State.PULL_TO_REFRESH) {
			text.setText(pullToRefreshText);
		}
	}

	/**
	 * Change the label text on state 'Release to Refresh'
	 * 
	 * @param releaseToRefreshText
	 *            Text
	 */
	public void setTextReleaseToRefresh(final String releaseToRefreshText) {
		this.releaseToRefreshText = releaseToRefreshText;
		if (state == State.RELEASE_TO_REFRESH) {
			text.setText(releaseToRefreshText);
		}
	}

	/**
	 * Change the label text on state 'Refreshing'
	 * 
	 * @param refreshingText
	 *            Text
	 */
	public void setTextRefreshing(final String refreshingText) {
		this.refreshingText = refreshingText;
		if (state == State.REFRESHING) {
			text.setText(refreshingText);
		}
	}

	private void init() {
		setVerticalFadingEdgeEnabled(false);

		headerContainer = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.ptr_header, null);
		header = (RelativeLayout) headerContainer.findViewById(R.id.ptr_id_header);
		text = (TextView) header.findViewById(R.id.ptr_id_text);
		lastUpdatedTextView = (TextView) header.findViewById(R.id.ptr_id_last_updated);
		image = (ImageView) header.findViewById(R.id.ptr_id_image);
		spinner = (ProgressBar) header.findViewById(R.id.ptr_id_spinner);

		pullToRefreshText = getContext().getString(R.string.ptr_pull_to_refresh);
		releaseToRefreshText = getContext().getString(R.string.ptr_release_to_refresh);
		refreshingText = getContext().getString(R.string.ptr_refreshing);
		lastUpdatedText = getContext().getString(R.string.ptr_last_updated);

		flipAnimation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		flipAnimation.setInterpolator(new LinearInterpolator());
		flipAnimation.setDuration(ROTATE_ARROW_ANIMATION_DURATION);
		flipAnimation.setFillAfter(true);

		reverseFlipAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseFlipAnimation.setInterpolator(new LinearInterpolator());
		reverseFlipAnimation.setDuration(ROTATE_ARROW_ANIMATION_DURATION);
		reverseFlipAnimation.setFillAfter(true);

		addHeaderView(headerContainer);
		setState(State.PULL_TO_REFRESH);
		scrollbarEnabled = isVerticalScrollBarEnabled();

		ViewTreeObserver vto = header.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new PTROnGlobalLayoutListener());

		super.setOnItemClickListener(new PTROnItemClickListener());
		super.setOnItemLongClickListener(new PTROnItemLongClickListener());
	}

	private void setHeaderPadding(final int padding) {
		headerPadding = padding;

		MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) header.getLayoutParams();
		mlp.setMargins(0, Math.round(padding), 0, 0);
		header.setLayoutParams(mlp);
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		if (lockScrollWhileRefreshing && (state == State.REFRESHING || getAnimation() != null && !getAnimation().hasEnded())) {
			return true;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (getFirstVisiblePosition() == 0)
				previousY = event.getY();
			else
				previousY = -1;
			break;

		case MotionEvent.ACTION_UP:
			if (previousY != -1 && (state == State.RELEASE_TO_REFRESH || getFirstVisiblePosition() == 0)) {
				switch (state) {
				case RELEASE_TO_REFRESH:
					setState(State.REFRESHING);
					bounceBackHeader();

					break;

				case PULL_TO_REFRESH:
					resetHeader();
					break;
				}
			}
			break;

		case MotionEvent.ACTION_MOVE:
			if (previousY != -1) {
				float y = event.getY();
				float diff = y - previousY;
				if (diff > 0)
					diff /= PULL_RESISTANCE;
				previousY = y;

				int newHeaderPadding = Math.max(Math.round(headerPadding + diff), -header.getHeight());

				if (newHeaderPadding != headerPadding && state != State.REFRESHING) {
					setHeaderPadding(newHeaderPadding);

					if (state == State.PULL_TO_REFRESH && headerPadding > 0) {
						setState(State.RELEASE_TO_REFRESH);

						image.clearAnimation();
						image.startAnimation(flipAnimation);
					} else if (state == State.RELEASE_TO_REFRESH && headerPadding < 0) {
						setState(State.PULL_TO_REFRESH);

						image.clearAnimation();
						image.startAnimation(reverseFlipAnimation);
					}

					return true;
				}
			}

			break;
		}

		return super.onTouchEvent(event);
	}

	private void bounceBackHeader() {
		int yTranslate = state == State.REFRESHING ? header.getHeight() - headerContainer.getHeight() : -headerContainer.getHeight()
				- headerContainer.getTop();

		TranslateAnimation bounceAnimation = new TranslateAnimation(TranslateAnimation.ABSOLUTE, 0, TranslateAnimation.ABSOLUTE, 0,
				TranslateAnimation.ABSOLUTE, 0, TranslateAnimation.ABSOLUTE, yTranslate);

		bounceAnimation.setDuration(BOUNCE_ANIMATION_DURATION);
		bounceAnimation.setFillEnabled(true);
		bounceAnimation.setFillAfter(false);
		bounceAnimation.setFillBefore(true);
		bounceAnimation.setInterpolator(new OvershootInterpolator(BOUNCE_OVERSHOOT_TENSION));
		bounceAnimation.setAnimationListener(new HeaderAnimationListener(yTranslate));

		startAnimation(bounceAnimation);
	}

	private void resetHeader() {
		if (getFirstVisiblePosition() > 0) {
			setHeaderPadding(-header.getHeight());
			setState(State.PULL_TO_REFRESH);
			return;
		}

		if (getAnimation() != null && !getAnimation().hasEnded()) {
			bounceBackHeader = true;
		} else {
			bounceBackHeader();
		}
	}

	private void setUiRefreshing() {
		spinner.setVisibility(View.VISIBLE);
		image.clearAnimation();
		image.setVisibility(View.INVISIBLE);
		text.setText(refreshingText);
	}

	private void setState(final State state) {
		this.state = state;
		switch (state) {
		case PULL_TO_REFRESH:
			spinner.setVisibility(View.INVISIBLE);
			image.setVisibility(View.VISIBLE);
			text.setText(pullToRefreshText);

			if (showLastUpdatedText && lastUpdated != -1) {
				lastUpdatedTextView.setVisibility(View.VISIBLE);
				lastUpdatedTextView.setText(String.format(lastUpdatedText, lastUpdatedDateFormat.format(new Date(lastUpdated))));
			}

			break;

		case RELEASE_TO_REFRESH:
			spinner.setVisibility(View.INVISIBLE);
			image.setVisibility(View.VISIBLE);
			text.setText(releaseToRefreshText);
			break;

		case REFRESHING:
			setUiRefreshing();

			lastUpdated = System.currentTimeMillis();
			if (onRefreshListener == null) {
				setState(State.PULL_TO_REFRESH);
			} else {
				onRefreshListener.onRefresh();
			}

			break;
		}
	}

	@Override
	protected void onScrollChanged(final int l, final int t, final int oldl, final int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);

		if (!hasResetHeader) {
			if (measuredHeaderHeight > 0 && state != State.REFRESHING) {
				setHeaderPadding(-measuredHeaderHeight);
			}

			hasResetHeader = true;
		}
	}

	private class HeaderAnimationListener implements AnimationListener {

		private int height;
		private final int translation;
		private State stateAtAnimationStart;

		public HeaderAnimationListener(final int translation) {
			this.translation = translation;
		}

		public void onAnimationStart(final Animation animation) {
			stateAtAnimationStart = state;

			android.view.ViewGroup.LayoutParams lp = getLayoutParams();
			height = lp.height;
			lp.height = getHeight() - translation;
			setLayoutParams(lp);

			if (scrollbarEnabled) {
				setVerticalScrollBarEnabled(false);
			}
		}

		public void onAnimationEnd(final Animation animation) {
			setHeaderPadding(stateAtAnimationStart == State.REFRESHING ? 0 : -measuredHeaderHeight - headerContainer.getTop());
			setSelection(0);

			android.view.ViewGroup.LayoutParams lp = getLayoutParams();
			lp.height = height;
			setLayoutParams(lp);

			if (scrollbarEnabled) {
				setVerticalScrollBarEnabled(true);
			}

			if (bounceBackHeader) {
				bounceBackHeader = false;

				postDelayed(new Runnable() {

					public void run() {
						resetHeader();
					}
				}, BOUNCE_ANIMATION_DELAY);
			} else if (stateAtAnimationStart != State.REFRESHING) {
				setState(State.PULL_TO_REFRESH);
			}
		}

		public void onAnimationRepeat(final Animation animation) {
		}
	}

	private class PTROnGlobalLayoutListener implements OnGlobalLayoutListener {

		public void onGlobalLayout() {
			int initialHeaderHeight = header.getHeight();

			if (initialHeaderHeight > 0) {
				measuredHeaderHeight = initialHeaderHeight;

				if (measuredHeaderHeight > 0 && state != State.REFRESHING) {
					setHeaderPadding(-measuredHeaderHeight);
					requestLayout();
				}
			}

			getViewTreeObserver().removeGlobalOnLayoutListener(this);
		}
	}

	private class PTROnItemClickListener implements OnItemClickListener {

		public void onItemClick(final AdapterView<?> adapterView, final View view, final int position, final long id) {
			hasResetHeader = false;

			if (onItemClickListener != null && state == State.PULL_TO_REFRESH) {
				// Passing up onItemClick. Correct position with the number of
				// header views
				onItemClickListener.onItemClick(adapterView, view, position - getHeaderViewsCount(), id);
			}
		}
	}

	private class PTROnItemLongClickListener implements OnItemLongClickListener {

		public boolean onItemLongClick(final AdapterView<?> adapterView, final View view, final int position, final long id) {
			hasResetHeader = false;

			if (onItemLongClickListener != null && state == State.PULL_TO_REFRESH) {
				// Passing up onItemLongClick. Correct position with the number
				// of header views
				return onItemLongClickListener.onItemLongClick(adapterView, view, position - getHeaderViewsCount(), id);
			}

			return false;
		}
	}
}