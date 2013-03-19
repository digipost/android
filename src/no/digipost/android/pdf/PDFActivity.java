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

import no.digipost.android.R;
import no.digipost.android.api.ApiConstants;
import no.digipost.android.gui.BaseActivity;
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

	private boolean cancelled = false;

	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void cancel() {
		cancelled = true;
		super.cancel();
	}
}

public class PDFActivity extends Activity {
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
	public static final int FROM_RECEIPS = 3;

	private final int TAP_PAGE_MARGIN = 5;
	private static final int SEARCH_PROGRESS_DELAY = 200;
	private PDFCore core;
	private String fileName;
	private String location_to;
	private ReaderView docView;
	private View buttonsView;
	private boolean buttonsVisible;
	private TextView filenameView;
	private TextView pageNumberView;
	private ImageButton searchButton;
	private ImageButton backButton;
	private ViewSwitcher topBarSwitcher;
	private LinearLayout topbar;
	private RelativeLayout bottombar;
	private boolean topBarIsSearch;
	private ImageButton searchBack;
	private ImageButton searchFwd;
	private EditText searchText;
	private ImageButton toArchive;
	private ImageButton toWorkarea;
	private ImageButton delete;
	//private ImageButton share;
	private ImageButton digipostIcon;
	private SafeAsyncTask<Void, Integer, SearchTaskResult> searchTask;
	private AlertDialog.Builder alertBuilder;
	private final LinkState linkState = LinkState.HIGHLIGHT;
	private final Handler handler = new Handler();
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

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		alertBuilder = new AlertDialog.Builder(this);
		intent = getIntent();

		if (core == null) {
			char type = 'P';
			byte[] b = PdfStore.pdf;
			core = openFile(b, type);

			SearchTaskResult.set(null);

		}
		if (core == null) {
			AlertDialog alert = alertBuilder.create();
			alert.setTitle(R.string.pdf_open_failed);
			alert.setButton(AlertDialog.BUTTON_POSITIVE, "Lukk", new DialogInterface.OnClickListener() {
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
		if (core == null) {
			return;
		}

		docView = new ReaderView(this) {
			private boolean showButtonsDisabled;

			@Override
			public boolean onSingleTapUp(final MotionEvent e) {
				if (e.getX() < super.getWidth() / TAP_PAGE_MARGIN) {
					super.moveToPrevious();
				} else if (e.getX() > super.getWidth() * (TAP_PAGE_MARGIN - 1) / TAP_PAGE_MARGIN) {
					super.moveToNext();
				} else if (!showButtonsDisabled) {
					int linkPage = -1;
					if (linkState != LinkState.INHIBIT) {
						PDFPageView pageView = (PDFPageView) docView.getDisplayedView();
						if (pageView != null) {
							linkPage = pageView.hitLinkPage(e.getX(), e.getY());
						}
					}

					if (linkPage != -1) {
						docView.setDisplayedViewIndex(linkPage);
					} else {
						if (!buttonsVisible) {
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
				if (!showButtonsDisabled) {
					hideButtons();
				}

				return super.onScroll(e1, e2, distanceX, distanceY);
			}

			@Override
			public boolean onScaleBegin(final ScaleGestureDetector d) {
				showButtonsDisabled = true;
				return super.onScaleBegin(d);
			}

			@Override
			public boolean onTouchEvent(final MotionEvent event) {
				if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
					showButtonsDisabled = false;
				}

				return super.onTouchEvent(event);
			}

			@Override
			protected void onChildSetup(final int i, final View v) {
				if (SearchTaskResult.get() != null && SearchTaskResult.get().pageNumber == i) {
					((PageView) v).setSearchBoxes(SearchTaskResult.get().searchBoxes);
				} else {
					((PageView) v).setSearchBoxes(null);
				}

				((PageView) v).setLinkHighlighting(linkState == LinkState.HIGHLIGHT);
			}

			@Override
			protected void onMoveToChild(final int i) {
				if (core == null) {
					return;
				}
				pageNumberView.setText(String.format("%d/%d", i + 1, core.countPages()));
				if (SearchTaskResult.get() != null && SearchTaskResult.get().pageNumber != i) {
					SearchTaskResult.set(null);
					docView.resetupChildren();
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
		docView.setAdapter(new PDFPageAdapter(this, core));

		makeButtonsView();

		String toolbarType = intent.getExtras().getString(ApiConstants.LOCATION_FROM);

		if (toolbarType.equals(ApiConstants.LOCATION_WORKAREA)) {
			makeWorkareaToolbar();
		} else if (toolbarType.equals(ApiConstants.LOCATION_ARCHIVE)) {
			makeArchiveToolbar();
		} else {
			makeAttachmentToolbar();
		}

		filenameView.setText(fileName);

		searchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(final View v) {
				searchModeOn();
			}
		});

		searchBack.setEnabled(false);
		searchFwd.setEnabled(false);

		searchText.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(final Editable s) {
				boolean haveText = s.toString().length() > 0;
				searchBack.setEnabled(haveText);
				searchFwd.setEnabled(haveText);

				if (SearchTaskResult.get() != null && !searchText.getText().toString().equals(SearchTaskResult.get().txt)) {
					SearchTaskResult.set(null);
					docView.resetupChildren();
				}
			}

			public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {
			}

			public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
			}
		});

		searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					search(1);
				}
				System.out.println("søk1");
				return false;
			}
		});

		searchBack.setOnClickListener(new View.OnClickListener() {
			public void onClick(final View v) {
				search(-1);
			}
		});
		searchFwd.setOnClickListener(new View.OnClickListener() {
			public void onClick(final View v) {
				search(1);
			}
		});

		backButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(final View v) {
				if (topBarIsSearch) {
					searchModeOff();
				} else {
					finish();
				}
			}
		});

		digipostIcon.setOnClickListener(new View.OnClickListener() {

			public void onClick(final View v) {
				if (topBarIsSearch) {
					searchModeOff();
				} else {
					finish();
				}
			}
		});

		toArchive.setOnClickListener(new View.OnClickListener() {

			public void onClick(final View v) {
				showWarning(getString(R.string.dialog_prompt_toArchive), ApiConstants.LOCATION_ARCHIVE);
			}
		});

		toWorkarea.setOnClickListener(new View.OnClickListener() {

			public void onClick(final View v) {
				showWarning(getString(R.string.dialog_prompt_toWorkarea), ApiConstants.LOCATION_WORKAREA);
			}
		});

		delete.setOnClickListener(new View.OnClickListener() {

			public void onClick(final View v) {
				showWarning(getString(R.string.dialog_prompt_delete), ApiConstants.DELETE);
			}
		});

		SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
		docView.setDisplayedViewIndex(prefs.getInt("page" + fileName, 0));

		if (savedInstanceState == null || !savedInstanceState.getBoolean("ButtonsHidden", false)) {
			showButtons();
		}

		if (savedInstanceState != null && savedInstanceState.getBoolean("SearchMode", false)) {
			searchModeOn();
		}

		RelativeLayout layout = new RelativeLayout(this);
		layout.addView(docView);
		layout.addView(buttonsView);
		layout.setBackgroundColor(Color.BLACK);
		setContentView(layout);
	}

	public void singleLetterOperation(final String action) {
		Intent i = new Intent(PDFActivity.this, BaseActivity.class);
		i.putExtra(ApiConstants.LOCATION_FROM,intent.getExtras().getString(ApiConstants.LOCATION_FROM));
		i.putExtra(ApiConstants.ACTION, action);
		i.putExtra(ApiConstants.DOCUMENT_TYPE,ApiConstants.LETTER);
		setResult(RESULT_OK,i);
		finish();
	}

	private void showWarning(final String text, final String action) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(text).setPositiveButton("Ja", new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int id) {
				singleLetterOperation(action);
				dialog.dismiss();
			}
		}).setCancelable(false).setNegativeButton(getString(R.string.close), new DialogInterface.OnClickListener() {
			public void onClick(final DialogInterface dialog, final int id) {
				dialog.cancel();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (resultCode >= 0) {
			docView.setDisplayedViewIndex(resultCode);
		}
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
	}

	@Override
	public void onDestroy() {
		if (core != null) {
			core.onDestroy();
		}
		core = null;
		super.onDestroy();
	}

	void showButtons() {
		if (core == null) {
			return;
		}
		if (!buttonsVisible) {
			buttonsVisible = true;
			// Update page number text and slider
			int index = docView.getDisplayedViewIndex();
			updatePageNumView(index);
			if (topBarIsSearch) {
				searchText.requestFocus();
				showKeyboard();
			}

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
					pageNumberView.setVisibility(View.VISIBLE);
				}
			});
			bottombar.startAnimation(anim);
		}
	}

	void hideButtons() {
		if (buttonsVisible) {
			buttonsVisible = false;
			hideKeyboard();

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
					pageNumberView.setVisibility(View.INVISIBLE);
				}

				public void onAnimationRepeat(final Animation animation) {
				}

				public void onAnimationEnd(final Animation animation) {
					bottombar.setVisibility(View.INVISIBLE);
				}
			});
			bottombar.startAnimation(anim);
		}
	}

	void searchModeOn() {
		if (!topBarIsSearch) {
			topBarIsSearch = true;
			// Focus on EditTextWidget
			searchText.requestFocus();
			showKeyboard();
			topBarSwitcher.showNext();
		}
	}

	void searchModeOff() {
		if (topBarIsSearch) {
			topBarIsSearch = false;
			hideKeyboard();
			topBarSwitcher.showPrevious();
			SearchTaskResult.set(null);
			docView.resetupChildren();
		}
	}

	void updatePageNumView(final int index) {
		if (core == null) {
			return;
		}
		pageNumberView.setText(String.format("%d/%d", index + 1, core.countPages()));
	}

	void makeButtonsView() {
		buttonsView = getLayoutInflater().inflate(R.layout.pdf_buttons, null);
		filenameView = (TextView) buttonsView.findViewById(R.id.pdf_name);
		pageNumberView = (TextView) buttonsView.findViewById(R.id.pdf_pageNumber);
		searchButton = (ImageButton) buttonsView.findViewById(R.id.pdf_searchbtn);
		topBarSwitcher = (ViewSwitcher) buttonsView.findViewById(R.id.pdf_switcher);
		searchBack = (ImageButton) buttonsView.findViewById(R.id.pdf_search_back);
		searchFwd = (ImageButton) buttonsView.findViewById(R.id.pdf_search_forward);
		searchText = (EditText) buttonsView.findViewById(R.id.pdf_searchtext);
		backButton = (ImageButton) buttonsView.findViewById(R.id.pdf_backbtn);
		bottombar = (RelativeLayout) buttonsView.findViewById(R.id.pdf_bottombar);
		topbar = (LinearLayout) buttonsView.findViewById(R.id.pdf_topbar);
		toWorkarea = (ImageButton) buttonsView.findViewById(R.id.pdf_toWorkarea);
		toArchive = (ImageButton) buttonsView.findViewById(R.id.pdf_toArchive);
		delete = (ImageButton) buttonsView.findViewById(R.id.pdf_delete);
		//share = (ImageButton) buttonsView.findViewById(R.id.pdf_share);
		digipostIcon = (ImageButton) buttonsView.findViewById(R.id.pdf_digipost_icon);

		pageNumberView.setVisibility(View.INVISIBLE);
		bottombar.setVisibility(View.INVISIBLE);
		topbar.setVisibility(View.VISIBLE);
	}

	private void makeArchiveToolbar() {
		toArchive.setVisibility(View.GONE);
	}

	private void makeWorkareaToolbar() {
		toWorkarea.setVisibility(View.GONE);
	}

	private void makeAttachmentToolbar() {
		toWorkarea.setVisibility(View.GONE);
		toArchive.setVisibility(View.GONE);
		delete.setVisibility(View.GONE);
	}

	void showKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.showSoftInput(searchText, 0);
		}
	}

	void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
		}
	}

	void killSearch() {
		if (searchTask != null) {
			searchTask.cancel(true);
			searchTask = null;
		}
	}

	void search(final int direction) {
		hideKeyboard();
		if (core == null) {
			return;
		}
		killSearch();

		final int increment = direction;
		final int startIndex = SearchTaskResult.get() == null ? docView.getDisplayedViewIndex() : SearchTaskResult.get().pageNumber
				+ increment;

		final ProgressDialogX progressDialog = new ProgressDialogX(this);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setTitle(getString(R.string.pdf_searching_));
		progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			public void onCancel(final DialogInterface dialog) {
				killSearch();
			}
		});
		progressDialog.setMax(core.countPages());

		searchTask = new SafeAsyncTask<Void, Integer, SearchTaskResult>() {
			@Override
			protected SearchTaskResult doInBackground(final Void... params) {
				int index = startIndex;

				while (0 <= index && index < core.countPages() && !isCancelled()) {
					publishProgress(index);
					RectF searchHits[] = core.searchPage(index, searchText.getText().toString());

					if (searchHits != null && searchHits.length > 0) {
						return new SearchTaskResult(searchText.getText().toString(), index, searchHits);
					}

					index += increment;
				}
				return null;
			}

			@Override
			protected void onPostExecute(final SearchTaskResult result) {
				progressDialog.cancel();
				if (result != null) {
					docView.setDisplayedViewIndex(result.pageNumber);
					SearchTaskResult.set(result);
					docView.resetupChildren();
				} else {
					alertBuilder.setTitle(SearchTaskResult.get() == null ? R.string.pdf_text_not_found
							: R.string.pdf_no_further_occurences_found);
					AlertDialog alert = alertBuilder.create();
					alert.setButton(AlertDialog.BUTTON_POSITIVE, "Lukk", (DialogInterface.OnClickListener) null);
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
				handler.postDelayed(new Runnable() {
					public void run() {
						if (!progressDialog.isCancelled()) {
							progressDialog.show();
							progressDialog.setProgress(startIndex);
						}
					}
				}, SEARCH_PROGRESS_DELAY);
			}
		};

		searchTask.safeExecute();
	}

	@Override
	public void onBackPressed() {
		if (topBarIsSearch) {
			searchModeOff();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onSearchRequested() {
		if (buttonsVisible && topBarIsSearch) {
			hideButtons();
		} else {
			showButtons();
			searchModeOn();
		}
		return super.onSearchRequested();
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		if (buttonsVisible && !topBarIsSearch) {
			hideButtons();
		} else {
			showButtons();
			searchModeOff();
		}
		return super.onPrepareOptionsMenu(menu);
	}
}
