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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import no.digipost.android.R;
import no.digipost.android.api.ApiConstants;
import no.digipost.android.authentication.KeyStore;
import no.digipost.android.authentication.SharedPreferencesUtil;


public class ScreenlockPreferenceActivity extends Activity {
    private Button yesButton,noButton;
    private ButtonListener listener;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screenlockpreference);
        yesButton = (Button) (findViewById(R.id.screenlockactivity_yes_button));
        noButton = (Button) (findViewById(R.id.screenlockactivity_no_button));
        listener = new ButtonListener();
        yesButton.setOnClickListener(listener);
        noButton.setOnClickListener(listener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        KeyStore ks = KeyStore.getInstance();
        if (ks.state() == KeyStore.State.UNLOCKED) {
            System.out.println("SCREENLOCK KeyStore.State.UNLOCKED");
            saveToSharedPreferences(ApiConstants.SCREENLOCK_CHOICE_YES);
        }else{
            System.out.println("SCREENLOCK KeyStore.State.LOCKED");
        }

    }

    private void yesButton() {
        startActivity(new Intent(KeyStore.UNLOCK_ACTION));
    }
    private void noButton(){
        saveToSharedPreferences(ApiConstants.SCREENLOCK_CHOICE_NO);
        startLoginActivity();
    }

    private void saveToSharedPreferences(int choice){
        SharedPreferencesUtil.storeScreenlockChoice(this,choice);
    }
    private void startLoginActivity() {
        Intent i = new Intent(ScreenlockPreferenceActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    private class ButtonListener implements View.OnClickListener {

        public void onClick(final View v) {
            if (v == yesButton) {
                yesButton();
            } else if (v == noButton) {
                noButton();
            }
        }
    }
}