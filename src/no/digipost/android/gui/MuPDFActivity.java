/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.digipost.android.gui;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;

import no.digipost.android.R;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.documentstore.DocumentContentStore;
import no.digipost.android.gui.fragments.ContentFragment;
import no.digipost.android.model.Attachment;
import no.digipost.android.pdf.MuPDFAlert;
import no.digipost.android.pdf.MuPDFCore;
import no.digipost.android.pdf.MuPDFPageAdapter;
import no.digipost.android.pdf.MuPDFReaderView;
import no.digipost.android.pdf.MuPDFView;
import no.digipost.android.pdf.SearchTask;
import no.digipost.android.pdf.SearchTaskResult;
import no.digipost.android.utilities.DialogUtitities;
import no.digipost.android.utilities.FileUtilities;
import no.digipost.android.constants.ApiConstants;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

class ThreadPerTaskExecutor implements Executor {
	public void execute(Runnable r) {
		new Thread(r).start();
	}
}

public class MuPDFActivity extends Activity
{
	/* The core rendering instance */
	private MuPDFCore core;
	private String       mFileName;
	private MuPDFReaderView mDocView;
	private TextView     mFilenameView;
	private TextView     mPageNumberView;
	private ImageButton  mSearchButton;
	private ImageButton mCopySelectButton;
	private boolean      mTopBarIsSearch;
	private ImageButton  mSearchBack;
	private ImageButton  mSearchFwd;
	private EditText     mSearchText;
	private SearchTask mSearchTask;
	private AlertDialog.Builder mAlertBuilder;
	private final Handler mHandler = new Handler();
	private boolean mAlertsActive= false;
	private AsyncTask<Void,Void,MuPDFAlert> mAlertTask;
	private AlertDialog mAlertDialog;
    private Intent intent;
    private boolean mTopBarIsSelect;

    private Attachment documentMeta;

    public void createAlertWaiter() {
		mAlertsActive = true;
		// All mupdf library calls are performed on asynchronous tasks to avoid stalling
		// the UI. Some calls can lead to javascript-invoked requests to display an
		// alert dialog and collect a reply from the user. The task has to be blocked
		// until the user's reply is received. This method creates an asynchronous task,
		// the purpose of which is to wait of these requests and produce the dialog
		// in response, while leaving the core blocked. When the dialog receives the
		// user's response, it is sent to the core via replyToAlert, unblocking it.
		// Another alert-waiting task is then created to pick up the next alert.
		if (mAlertTask != null) {
			mAlertTask.cancel(true);
			mAlertTask = null;
		}
		if (mAlertDialog != null) {
			mAlertDialog.cancel();
			mAlertDialog = null;
		}
		mAlertTask = new AsyncTask<Void,Void,MuPDFAlert>() {

			@Override
			protected MuPDFAlert doInBackground(Void... arg0) {
				if (!mAlertsActive)
					return null;

				return core.waitForAlert();
			}

			@Override
			protected void onPostExecute(final MuPDFAlert result) {
				// core.waitForAlert may return null when shutting down
				if (result == null)
					return;
				final MuPDFAlert.ButtonPressed pressed[] = new MuPDFAlert.ButtonPressed[3];
				for(int i = 0; i < 3; i++)
					pressed[i] = MuPDFAlert.ButtonPressed.None;
				DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mAlertDialog = null;
						if (mAlertsActive) {
							int index = 0;
							switch (which) {
							case AlertDialog.BUTTON1: index=0; break;
							case AlertDialog.BUTTON2: index=1; break;
							case AlertDialog.BUTTON3: index=2; break;
							}
							result.buttonPressed = pressed[index];
							// Send the user's response to the core, so that it can
							// continue processing.
							core.replyToAlert(result);
							// Create another alert-waiter to pick up the next alert.
							createAlertWaiter();
						}
					}
				};
				mAlertDialog = mAlertBuilder.create();
				mAlertDialog.setTitle(result.title);
				mAlertDialog.setMessage(result.message);
				switch (result.iconType)
				{
				case Error:
					break;
				case Warning:
					break;
				case Question:
					break;
				case Status:
					break;
				}
				switch (result.buttonGroupType)
				{
				case OkCancel:
					mAlertDialog.setButton(AlertDialog.BUTTON2, "Avbryt", listener);
					pressed[1] = MuPDFAlert.ButtonPressed.Cancel;
				case Ok:
					mAlertDialog.setButton(AlertDialog.BUTTON1, "Ok", listener);
					pressed[0] = MuPDFAlert.ButtonPressed.Ok;
					break;
				case YesNoCancel:
					mAlertDialog.setButton(AlertDialog.BUTTON3, "Avbryt", listener);
					pressed[2] = MuPDFAlert.ButtonPressed.Cancel;
				case YesNo:
					mAlertDialog.setButton(AlertDialog.BUTTON1, "Ja", listener);
					pressed[0] = MuPDFAlert.ButtonPressed.Yes;
					mAlertDialog.setButton(AlertDialog.BUTTON2, "Nei", listener);
					pressed[1] = MuPDFAlert.ButtonPressed.No;
					break;
				}
				mAlertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						mAlertDialog = null;
						if (mAlertsActive) {
							result.buttonPressed = MuPDFAlert.ButtonPressed.None;
							core.replyToAlert(result);
							createAlertWaiter();
						}
					}
				});

				mAlertDialog.show();
			}
		};

		mAlertTask.executeOnExecutor(new ThreadPerTaskExecutor());
	}

	public void destroyAlertWaiter() {
		mAlertsActive = false;
		if (mAlertDialog != null) {
			mAlertDialog.cancel();
			mAlertDialog = null;
		}
		if (mAlertTask != null) {
			mAlertTask.cancel(true);
			mAlertTask = null;
		}
	}

	private MuPDFCore openFile(String path)
	{
		int lastSlashPos = path.lastIndexOf('/');
		mFileName = new String(lastSlashPos == -1
					? path
					: path.substring(lastSlashPos+1));
		System.out.println("Trying to open "+path);
		try
		{
			core = new MuPDFCore(path);
		}
		catch (Exception e)
		{
			System.out.println(e);
			return null;
		}
		return core;
	}

	private MuPDFCore openBuffer(byte buffer[])
	{
		System.out.println("Trying to open byte buffer");
		try
		{
			core = new MuPDFCore(buffer);
		}
		catch (Exception e)
		{
			System.out.println(e);
			return null;
		}
		return core;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        documentMeta = DocumentContentStore.documentMeta;

        getActionBar().setTitle(documentMeta.getSubject());

		mAlertBuilder = new AlertDialog.Builder(this);

		if (core == null) {
		    intent = getIntent();
			byte buffer[] = DocumentContentStore.documentContent;

				if (buffer != null) {
					core = openBuffer(buffer);
				}

				SearchTaskResult.set(null);
		}

		if (core == null)
		{
			AlertDialog alert = mAlertBuilder.create();
			alert.setTitle(R.string.pdf_open_failed);
			alert.setButton(AlertDialog.BUTTON_POSITIVE, this.getString(R.string.close),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
			alert.show();
			return;
		}

		createUI(savedInstanceState);
	}

	public void createUI(Bundle savedInstanceState) {
		if (core == null)
			return;

		// Now create the UI.
		// First create the document view
		mDocView = new MuPDFReaderView(this) {
			@Override
			protected void onMoveToChild(int i) {
				if (core == null)
					return;
				mPageNumberView.setText(String.format("%d / %d", i + 1,
						core.countPages()));
				super.onMoveToChild(i);
			}

			@Override
			protected void onTapMainDocArea() {

			}

			@Override
			protected void onDocMotion() {

			}
		};
		mDocView.setAdapter(new MuPDFPageAdapter(this, core));

		mSearchTask = new SearchTask(this, core) {
			@Override
			protected void onTextFound(SearchTaskResult result) {
				SearchTaskResult.set(result);
				// Ask the ReaderView to move to the resulting page
				mDocView.setDisplayedViewIndex(result.pageNumber);
				// Make the ReaderView act on the change to SearchTaskResult
				// via overridden onChildSetup method.
				mDocView.resetupChildren();
			}
		};

		// Activate the search-preparing button
		/*mSearchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				searchModeOn();
			}
		});

		mCancelSelectButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
				if (pageView != null)
					pageView.deselectText();
				selectModeOff();
			}
		});

		mCopySelectButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
				boolean copied = false;
				if (pageView != null)
					copied = pageView.copySelection();

				selectModeOff();

				showMessage(copied ? "Copied to clipboard" : "No text selected");
			}
		});

		// Search invoking buttons are disabled while there is no text specified
		mSearchBack.setEnabled(false);
		mSearchFwd.setEnabled(false);
		mSearchBack.setColorFilter(Color.argb(255, 128, 128, 128));
		mSearchFwd.setColorFilter(Color.argb(255, 128, 128, 128));

		// React to interaction with the text widget
		mSearchText.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {
				boolean haveText = s.toString().length() > 0;
				mSearchBack.setEnabled(haveText);
				mSearchFwd.setEnabled(haveText);
				if (haveText) {
					mSearchBack.setColorFilter(Color.argb(255, 255, 255, 255));
					mSearchFwd.setColorFilter(Color.argb(255, 255, 255, 255));
				} else {
					mSearchBack.setColorFilter(Color.argb(255, 128, 128, 128));
					mSearchFwd.setColorFilter(Color.argb(255, 128, 128, 128));
				}

				// Remove any previous search results
				if (SearchTaskResult.get() != null && !mSearchText.getText().toString().equals(SearchTaskResult.get().txt)) {
					SearchTaskResult.set(null);
					mDocView.resetupChildren();
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
		});

		//React to Done button on keyboard
		mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE)
					search(1);
				return false;
			}
		});

		mSearchText.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER)
					search(1);
				return false;
			}
		});

		// Activate search invoking buttons
		mSearchBack.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				search(-1);
			}
		});
		mSearchFwd.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				search(1);
			}
		});*/

        mDocView.setLinksEnabled(true);

		// Stick the document view and the buttons overlay into a parent view
		RelativeLayout layout = new RelativeLayout(this);
		layout.addView(mDocView);
		layout.setBackgroundResource(R.color.login_disclamer_registration_layout_background);
		setContentView(layout);
	}

    private void showMessage(final String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode >= 0)
			mDocView.setDisplayedViewIndex(resultCode);
		super.onActivityResult(requestCode, resultCode, data);
	}

	public Object onRetainNonConfigurationInstance()
	{
		MuPDFCore mycore = core;
		core = null;
		return mycore;
	}

	@Override
	protected void onPause() {
		super.onPause();

		mSearchTask.stop();
	}

	public void onDestroy()
	{
		if (core != null)
			core.onDestroy();
		if (mAlertTask != null) {
			mAlertTask.cancel(true);
			mAlertTask = null;
		}
		core = null;

        if (isFinishing()) {
            FileUtilities.deleteTempFiles();
        }

		super.onDestroy();
	}

    void selectModeOn() {
        if (!mTopBarIsSelect) {
            mDocView.setSelectionMode(true);
            mTopBarIsSelect = true;
        }
    }

    void selectModeOff() {
        if (mTopBarIsSelect) {
            mDocView.setSelectionMode(false);
            mTopBarIsSelect = false;

            MuPDFView pageView = (MuPDFView) mDocView.getDisplayedView();
            if (pageView != null)
                pageView.deselectText();
        }
    }

	void searchModeOn() {
		if (!mTopBarIsSearch) {
			mTopBarIsSearch = true;
			//Focus on EditTextWidget
			mSearchText.requestFocus();
			showKeyboard();
		}
	}

	void searchModeOff() {
		if (mTopBarIsSearch) {
			mTopBarIsSearch = false;
			hideKeyboard();
			SearchTaskResult.set(null);
			// Make the ReaderView act on the change to mSearchTaskResult
			// via overridden onChildSetup method.
			mDocView.resetupChildren();
		}
	}

	void updatePageNumView(int index) {
		if (core == null)
			return;
		mPageNumberView.setText(String.format("%d / %d", index+1, core.countPages()));
	}

	void showKeyboard() {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null)
			imm.showSoftInput(mSearchText, 0);
	}

	void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null)
			imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
	}

	void search(int direction) {
		hideKeyboard();
		int displayPage = mDocView.getDisplayedViewIndex();
		SearchTaskResult r = SearchTaskResult.get();
		int searchPage = r != null ? r.pageNumber : -1;
		mSearchTask.go(mSearchText.getText().toString(), direction, displayPage, searchPage);
	}

	@Override
	public boolean onSearchRequested() {
		if (mTopBarIsSearch) {

		} else {
			searchModeOn();
		}
		return super.onSearchRequested();
	}

    public void singleLetterOperation(final String action) {
        Intent i = new Intent(MuPDFActivity.this, BaseFragmentActivity.class);
        i.putExtra(ApiConstants.LOCATION_FROM, intent.getExtras().getString(ApiConstants.LOCATION_FROM));
        i.putExtra(ApiConstants.ACTION, action);
        i.putExtra(ApiConstants.DOCUMENT_TYPE, ApiConstants.LETTER);
        setResult(RESULT_OK, i);
        finish();
    }

    private void showWarning(final String text, final String action) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(text).setPositiveButton("Ja", new DialogInterface.OnClickListener() {
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

    private void executeAction(String action) {
        Intent i = new Intent(MuPDFActivity.this, MainContentActivity.class);
        i.putExtra(ApiConstants.ACTION, action);

        setResult(RESULT_OK, i);
        finish();
    }

    private void promtAction(final String message, final String action) {
        AlertDialog.Builder builder = DialogUtitities.getAlertDialogBuilderWithMessage(this, message);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                executeAction(action);
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(R.string.abort, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_mupdf_actionbar, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
	public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem toArchive = menu.findItem(R.id.pdfmenu_archive);
        MenuItem toWorkarea = menu.findItem(R.id.pdfmenu_workarea);

        int content = getIntent().getIntExtra(ContentFragment.INTENT_CONTENT, 0);

        if (content == ApplicationConstants.WORKAREA) {
            toWorkarea.setVisible(false);
        } else if (content == ApplicationConstants.ARCHIVE) {
            toArchive.setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.pdfmenu_delete:
                promtAction(getString(R.string.dialog_prompt_delete_document), ApiConstants.DELETE);
                return true;
            case R.id.pdfmenu_archive:
                promtAction(getString(R.string.dialog_prompt_document_toArchive), ApiConstants.LOCATION_ARCHIVE);
                return true;
            case R.id.pdfmenu_workarea:
                promtAction(getString(R.string.dialog_prompt_document_toWorkarea), ApiConstants.LOCATION_WORKAREA);
                return true;
            case R.id.pdfmenu_copy:
                // ToDo implementere kopiering
                return true;
            case R.id.pdfmenu_open_external:
                openFileWithIntent(documentMeta.getFileType(), core.getBuffer());
                return true;
            case R.id.pdfmenu_save:
                promtSaveToSD();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
	protected void onStart() {
		if (core != null)
		{
			core.startAlerts();
			createAlertWaiter();
		}

		super.onStart();
	}

	@Override
	protected void onStop() {
		if (core != null)
		{
			destroyAlertWaiter();
			core.stopAlerts();
		}

		super.onStop();
	}

	@Override
	public void onBackPressed() {
		if (mTopBarIsSearch) {
            searchModeOff();
		} else if (mTopBarIsSelect) {
            selectModeOff();
        } else {
			super.onBackPressed();
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

            progressDialog = new ProgressDialog(MuPDFActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(getString(R.string.loading_content));
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... parameters) {
            File file = null;

            try {
                file = FileUtilities.writeFileToSD(documentMeta.getSubject(), documentMeta.getFileType(), core.getBuffer());
            } catch (IOException e) {
                return false;
            }

            FileUtilities.makeFileVisible(MuPDFActivity.this, file);

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
}
