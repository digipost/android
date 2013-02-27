package no.digipost.android.pdf;

import no.digipost.android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

class SearchTaskResult {
	public final String txt;
	public final int pageNumber;
	public final RectF searchBoxes[];
	static private SearchTaskResult singleton;

	SearchTaskResult(final String _txt, final int _pageNumber, final RectF _searchBoxes[]) {
		txt = _txt;
		pageNumber = _pageNumber;
		searchBoxes = _searchBoxes;
	}

	static public SearchTaskResult get() {
		return singleton;
	}

	static public void set(final SearchTaskResult r) {
		singleton = r;
	}
}

class ProgressDialogX extends ProgressDialog {
	public ProgressDialogX(final Context context) {
		super(context);
	}

	private boolean mCancelled = false;

	public boolean isCancelled() {
		return mCancelled;
	}

	@Override
	public void cancel() {
		mCancelled = true;
		super.cancel();
	}
}

public class PDFActivity extends Activity {
	/* The core rendering instance */
	private enum LinkState {
		DEFAULT,
		HIGHLIGHT,
		INHIBIT
	};

	public static final String PDF_FILENAME = "pdf_filename";
	public static final String INTENT_FROM = "pdf_from";
	public static final int FROM_MAILBOX = 0;
	public static final int FROM_ARCHIVE = 1;
	public static final int FROM_WORKAREA = 2;

	private final int TAP_PAGE_MARGIN = 5;
	private static final int SEARCH_PROGRESS_DELAY = 200;
	private PDFCore core;
	private String mFileName;
	private ReaderView mDocView;
	private View mButtonsView;
	private boolean mButtonsVisible;
	private TextView mFilenameView;
	private TextView mPageNumberView;
	private ImageButton mSearchButton;
	private ImageButton mBackButton;
	private ViewSwitcher mTopBarSwitcher;
	private LinearLayout mTopbar;
	private RelativeLayout mBottombar;
	private boolean mTopBarIsSearch;
	private ImageButton mSearchBack;
	private ImageButton mSearchFwd;
	private EditText mSearchText;
	private ImageButton toMailbox;
	private ImageButton toArchive;
	private ImageButton toWorkarea;
	private ImageButton delete;
	private ImageButton share;
	private SafeAsyncTask<Void, Integer, SearchTaskResult> mSearchTask;
	private AlertDialog.Builder mAlertBuilder;
	private final LinkState mLinkState = LinkState.HIGHLIGHT;
	private final Handler mHandler = new Handler();
	private Intent intent;

	private PDFCore openFile(final byte b[], final char type) {
		try {
			core = new PDFCore(b, type);
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
		return core;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAlertBuilder = new AlertDialog.Builder(this);
		intent = getIntent();

		if (core == null) {
			char type = 'P';
			byte[] b = PdfStore.pdf;
			core = openFile(b, type);

			SearchTaskResult.set(null);

		}
		if (core == null) {
			AlertDialog alert = mAlertBuilder.create();
			alert.setTitle(R.string.open_failed);
			alert.setButton(AlertDialog.BUTTON_POSITIVE, "Dismiss", new DialogInterface.OnClickListener() {
				public void onClick(final DialogInterface dialog, final int which) {
					finish();
				}
			});
			alert.show();
			return;
		}

		createUI(savedInstanceState);
	}

	public void createUI(final Bundle savedInstanceState) {
		if (core == null)
			return;

		mDocView = new ReaderView(this) {
			private boolean showButtonsDisabled;

			@Override
			public boolean onSingleTapUp(final MotionEvent e) {
				if (e.getX() < super.getWidth() / TAP_PAGE_MARGIN) {
					super.moveToPrevious();
				} else if (e.getX() > super.getWidth() * (TAP_PAGE_MARGIN - 1) / TAP_PAGE_MARGIN) {
					super.moveToNext();
				} else if (!showButtonsDisabled) {
					int linkPage = -1;
					if (mLinkState != LinkState.INHIBIT) {
						PDFPageView pageView = (PDFPageView) mDocView.getDisplayedView();
						if (pageView != null) {
							linkPage = pageView.hitLinkPage(e.getX(), e.getY());
						}
					}

					if (linkPage != -1) {
						mDocView.setDisplayedViewIndex(linkPage);
					} else {
						if (!mButtonsVisible) {
							showButtons();
						} else {
							hideButtons();
						}
					}
				}
				return super.onSingleTapUp(e);
			}

			@Override
			public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY) {
				if (!showButtonsDisabled)
					hideButtons();

				return super.onScroll(e1, e2, distanceX, distanceY);
			}

			@Override
			public boolean onScaleBegin(final ScaleGestureDetector d) {
				showButtonsDisabled = true;
				return super.onScaleBegin(d);
			}

			@Override
			public boolean onTouchEvent(final MotionEvent event) {
				if (event.getActionMasked() == MotionEvent.ACTION_DOWN)
					showButtonsDisabled = false;

				return super.onTouchEvent(event);
			}

			@Override
			protected void onChildSetup(final int i, final View v) {
				if (SearchTaskResult.get() != null && SearchTaskResult.get().pageNumber == i)
					((PageView) v).setSearchBoxes(SearchTaskResult.get().searchBoxes);
				else
					((PageView) v).setSearchBoxes(null);

				((PageView) v).setLinkHighlighting(mLinkState == LinkState.HIGHLIGHT);
			}

			@Override
			protected void onMoveToChild(final int i) {
				if (core == null)
					return;
				mPageNumberView.setText(String.format("%d/%d", i + 1, core.countPages()));
				if (SearchTaskResult.get() != null && SearchTaskResult.get().pageNumber != i) {
					SearchTaskResult.set(null);
					mDocView.resetupChildren();
				}
			}

			@Override
			protected void onSettle(final View v) {
				((PageView) v).addHq();
			}

			@Override
			protected void onUnsettle(final View v) {
				((PageView) v).removeHq();
			}

			@Override
			protected void onNotInUse(final View v) {
				((PageView) v).releaseResources();
			}
		};
		mDocView.setAdapter(new PDFPageAdapter(this, core));

		makeButtonsView();

		int toolbarType = intent.getIntExtra(INTENT_FROM, FROM_MAILBOX);

		if (toolbarType == FROM_MAILBOX) {
			makeMailboxToolbar();
		} else if (toolbarType == FROM_WORKAREA) {
			makeWorkareaToolbar();
		} else if (toolbarType == FROM_ARCHIVE) {
			makeArchiveToolbar();
		}

		// Set the file-name text
		mFilenameView.setText(mFileName);

		// Activate the search-preparing button
		mSearchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(final View v) {
				searchModeOn();
			}
		});

		// Search invoking buttons are disabled while there is no text specified
		mSearchBack.setEnabled(false);
		mSearchFwd.setEnabled(false);

		// React to interaction with the text widget
		mSearchText.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(final Editable s) {
				boolean haveText = s.toString().length() > 0;
				mSearchBack.setEnabled(haveText);
				mSearchFwd.setEnabled(haveText);

				// Remove any previous search results
				if (SearchTaskResult.get() != null && !mSearchText.getText().toString().equals(SearchTaskResult.get().txt)) {
					SearchTaskResult.set(null);
					mDocView.resetupChildren();
				}
			}

			public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
			}

			public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
			}
		});

		// React to Done button on keyboard
		mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH)
					search(1);
				System.out.println("søk1");
				return false;
			}
		});

		// Activate search invoking buttons
		mSearchBack.setOnClickListener(new View.OnClickListener() {
			public void onClick(final View v) {
				search(-1);
			}
		});
		mSearchFwd.setOnClickListener(new View.OnClickListener() {
			public void onClick(final View v) {
				search(1);
			}
		});

		mBackButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(final View v) {
				if (mTopBarIsSearch) {
					searchModeOff();
				} else {
					finish();
				}
			}
		});

		// Reenstate last state if it was recorded
		SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
		mDocView.setDisplayedViewIndex(prefs.getInt("page" + mFileName, 0));

		if (savedInstanceState == null || !savedInstanceState.getBoolean("ButtonsHidden", false))
			showButtons();

		if (savedInstanceState != null && savedInstanceState.getBoolean("SearchMode", false))
			searchModeOn();

		// Stick the document view and the buttons overlay into a parent view
		RelativeLayout layout = new RelativeLayout(this);
		layout.addView(mDocView);
		layout.addView(mButtonsView);
		layout.setBackgroundColor(Color.BLACK);
		// layout.setBackgroundResource(R.color.canvas);
		setContentView(layout);
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (resultCode >= 0)
			mDocView.setDisplayedViewIndex(resultCode);
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		PDFCore mycore = core;
		core = null;
		return mycore;
	}

	@Override
	protected void onPause() {
		super.onPause();

		killSearch();

		if (mFileName != null && mDocView != null) {
			SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putInt("page" + mFileName, mDocView.getDisplayedViewIndex());
			edit.commit();
		}
	}

	@Override
	public void onDestroy() {
		if (core != null)
			core.onDestroy();
		core = null;
		super.onDestroy();
	}

	void showButtons() {
		if (core == null)
			return;
		if (!mButtonsVisible) {
			mButtonsVisible = true;
			// Update page number text and slider
			int index = mDocView.getDisplayedViewIndex();
			updatePageNumView(index);
			if (mTopBarIsSearch) {
				mSearchText.requestFocus();
				showKeyboard();
			}

			Animation anim = new TranslateAnimation(0, 0, -mTopbar.getHeight(), 0);
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(final Animation animation) {
					mTopbar.setVisibility(View.VISIBLE);
				}

				public void onAnimationRepeat(final Animation animation) {
				}

				public void onAnimationEnd(final Animation animation) {
				}
			});
			mTopbar.startAnimation(anim);

			anim = new TranslateAnimation(0, 0, mBottombar.getHeight(), 0);
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(final Animation animation) {
					mBottombar.setVisibility(View.VISIBLE);
				}

				public void onAnimationRepeat(final Animation animation) {
				}

				public void onAnimationEnd(final Animation animation) {
					mPageNumberView.setVisibility(View.VISIBLE);
				}
			});
			mBottombar.startAnimation(anim);
		}
	}

	void hideButtons() {
		if (mButtonsVisible) {
			mButtonsVisible = false;
			hideKeyboard();

			Animation anim = new TranslateAnimation(0, 0, 0, -mTopbar.getHeight());
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(final Animation animation) {
				}

				public void onAnimationRepeat(final Animation animation) {
				}

				public void onAnimationEnd(final Animation animation) {
					mTopbar.setVisibility(View.INVISIBLE);
				}
			});
			mTopbar.startAnimation(anim);

			anim = new TranslateAnimation(0, 0, 0, mBottombar.getHeight());
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(final Animation animation) {
					mPageNumberView.setVisibility(View.INVISIBLE);
				}

				public void onAnimationRepeat(final Animation animation) {
				}

				public void onAnimationEnd(final Animation animation) {
					mBottombar.setVisibility(View.INVISIBLE);
				}
			});
			mBottombar.startAnimation(anim);
		}
	}

	void searchModeOn() {
		if (!mTopBarIsSearch) {
			mTopBarIsSearch = true;
			// Focus on EditTextWidget
			mSearchText.requestFocus();
			showKeyboard();
			mTopBarSwitcher.showNext();
		}
	}

	void searchModeOff() {
		if (mTopBarIsSearch) {
			mTopBarIsSearch = false;
			hideKeyboard();
			mTopBarSwitcher.showPrevious();
			SearchTaskResult.set(null);
			// Make the ReaderView act on the change to mSearchTaskResult
			// via overridden onChildSetup method.
			mDocView.resetupChildren();
		}
	}

	void updatePageNumView(final int index) {
		if (core == null)
			return;
		mPageNumberView.setText(String.format("%d/%d", index + 1, core.countPages()));
	}

	void makeButtonsView() {
		mButtonsView = getLayoutInflater().inflate(R.layout.pdf_buttons, null);
		mFilenameView = (TextView) mButtonsView.findViewById(R.id.pdf_name);
		mPageNumberView = (TextView) mButtonsView.findViewById(R.id.pdf_pageNumber);
		mSearchButton = (ImageButton) mButtonsView.findViewById(R.id.pdf_searchbtn);
		mTopBarSwitcher = (ViewSwitcher) mButtonsView.findViewById(R.id.pdf_switcher);
		mSearchBack = (ImageButton) mButtonsView.findViewById(R.id.pdf_search_back);
		mSearchFwd = (ImageButton) mButtonsView.findViewById(R.id.pdf_search_forward);
		mSearchText = (EditText) mButtonsView.findViewById(R.id.pdf_searchtext);
		mBackButton = (ImageButton) mButtonsView.findViewById(R.id.pdf_backbtn);
		mBottombar = (RelativeLayout) mButtonsView.findViewById(R.id.pdf_bottombar);
		mTopbar = (LinearLayout) mButtonsView.findViewById(R.id.pdf_topbar);
		toMailbox = (ImageButton) mButtonsView.findViewById(R.id.pdf_toMailbox);
		toWorkarea = (ImageButton) mButtonsView.findViewById(R.id.pdf_toWorkarea);
		toArchive = (ImageButton) mButtonsView.findViewById(R.id.pdf_toArchive);
		delete = (ImageButton) mButtonsView.findViewById(R.id.pdf_delete);
		share = (ImageButton) mButtonsView.findViewById(R.id.pdf_share);

		mPageNumberView.setVisibility(View.INVISIBLE);
		mBottombar.setVisibility(View.INVISIBLE);
		mTopbar.setVisibility(View.VISIBLE);
	}

	private void makeMailboxToolbar() {
		toMailbox.setVisibility(View.GONE);
	}

	private void makeArchiveToolbar() {
		toArchive.setVisibility(View.GONE);
	}

	private void makeWorkareaToolbar() {
		toWorkarea.setVisibility(View.GONE);
	}

	void showKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null)
			imm.showSoftInput(mSearchText, 0);
	}

	void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null)
			imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
	}

	void killSearch() {
		if (mSearchTask != null) {
			mSearchTask.cancel(true);
			mSearchTask = null;
		}
	}

	void search(final int direction) {
		hideKeyboard();
		if (core == null)
			return;
		killSearch();

		final int increment = direction;
		final int startIndex = SearchTaskResult.get() == null ? mDocView.getDisplayedViewIndex() : SearchTaskResult.get().pageNumber
				+ increment;

		final ProgressDialogX progressDialog = new ProgressDialogX(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setTitle(getString(R.string.searching_));
		progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(final DialogInterface dialog) {
				killSearch();
			}
		});
		progressDialog.setMax(core.countPages());

		mSearchTask = new SafeAsyncTask<Void, Integer, SearchTaskResult>() {
			@Override
			protected SearchTaskResult doInBackground(final Void... params) {
				int index = startIndex;

				while (0 <= index && index < core.countPages() && !isCancelled()) {
					publishProgress(index);
					RectF searchHits[] = core.searchPage(index, mSearchText.getText().toString());

					if (searchHits != null && searchHits.length > 0)
						return new SearchTaskResult(mSearchText.getText().toString(), index, searchHits);

					index += increment;
				}
				return null;
			}

			@Override
			protected void onPostExecute(final SearchTaskResult result) {
				progressDialog.cancel();
				if (result != null) {
					// Ask the ReaderView to move to the resulting page
					mDocView.setDisplayedViewIndex(result.pageNumber);
					SearchTaskResult.set(result);
					// Make the ReaderView act on the change to
					// mSearchTaskResult
					// via overridden onChildSetup method.
					mDocView.resetupChildren();
				} else {
					mAlertBuilder.setTitle(SearchTaskResult.get() == null ? R.string.text_not_found : R.string.no_further_occurences_found);
					AlertDialog alert = mAlertBuilder.create();
					alert.setButton(AlertDialog.BUTTON_POSITIVE, "Dismiss", (DialogInterface.OnClickListener) null);
					alert.show();
				}
			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
				progressDialog.cancel();
			}

			@Override
			protected void onProgressUpdate(final Integer... values) {
				super.onProgressUpdate(values);
				progressDialog.setProgress(values[0].intValue());
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				mHandler.postDelayed(new Runnable() {
					public void run() {
						if (!progressDialog.isCancelled()) {
							progressDialog.show();
							progressDialog.setProgress(startIndex);
						}
					}
				}, SEARCH_PROGRESS_DELAY);
			}
		};

		mSearchTask.safeExecute();
	}

	@Override
	public void onBackPressed() {
		if (mTopBarIsSearch) {
			searchModeOff();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onSearchRequested() {
		if (mButtonsVisible && mTopBarIsSearch) {
			hideButtons();
		} else {
			showButtons();
			searchModeOn();
		}
		return super.onSearchRequested();
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		if (mButtonsVisible && !mTopBarIsSearch) {
			hideButtons();
		} else {
			showButtons();
			searchModeOff();
		}
		return super.onPrepareOptionsMenu(menu);
	}
}
