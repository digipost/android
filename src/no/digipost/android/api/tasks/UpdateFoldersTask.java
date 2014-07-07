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

package no.digipost.android.api.tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

import no.digipost.android.api.ContentOperations;
import no.digipost.android.api.exception.DigipostApiException;
import no.digipost.android.api.exception.DigipostAuthenticationException;
import no.digipost.android.api.exception.DigipostClientException;
import no.digipost.android.gui.MainContentActivity;
import no.digipost.android.model.Folder;

public class UpdateFoldersTask extends AsyncTask<Void, Void, String> {
    private MainContentActivity activity;
    private ArrayList<Folder> folders;

    public UpdateFoldersTask(MainContentActivity activity, final ArrayList<Folder> folders) {
        this.folders = folders;
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            return ContentOperations.updateFolders(activity, folders);
        } catch (DigipostApiException e) {
            Log.e(getClass().getName(), e.getMessage(), e);
            activity.errorMessage = e.getMessage();
            return null ;
        } catch (DigipostClientException e) {
            Log.e(getClass().getName(), e.getMessage(), e);
            activity.errorMessage = e.getMessage();
            return null;
        } catch (DigipostAuthenticationException e) {
            Log.e(getClass().getName(), e.getMessage(), e);
            activity.errorMessage = e.getMessage();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        activity.updateFoldersFromTask(result);
    }
}