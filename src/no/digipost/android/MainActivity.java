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

package no.digipost.android;

import no.digipost.android.gui.BaseActivity;
import no.digipost.android.gui.LoginActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends Activity implements Runnable {

	@SuppressWarnings("unused")
	private static boolean threadRunning;
	private volatile Thread runner;
	private final Handler mHandler = new Handler();

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		startThread();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void startBaseActivity() {
		Intent i = new Intent(MainActivity.this, BaseActivity.class);
		startActivity(i);
		finish();
	}

	private void startLoginActivity() {
		Intent i = new Intent(MainActivity.this, LoginActivity.class);
		startActivity(i);
		finish();
	}

	private boolean hasToken() {
		// TODO Check if token exist,splash while networkconnection are
		// refreshing token.
		return false;
	}

	public synchronized void startThread() {
		if (runner == null) {
			runner = new Thread(this);
			runner.start();
		}
	}

	public synchronized void stopThread() {
		threadRunning = false;
		if (runner != null) {
			Thread old = runner;
			runner = null;
			old.interrupt();
			if (hasToken()) {
				startBaseActivity();
			} else {
				startLoginActivity();
			}
		}
	}

	@Override
	public void run() {
		threadRunning = true;
		while (Thread.currentThread() == runner) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
			}
			try {
				mHandler.post(new Runnable() {
					public void run() {
						stopThread();
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
