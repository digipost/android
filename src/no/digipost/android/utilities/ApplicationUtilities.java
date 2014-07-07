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

package no.digipost.android.utilities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import no.digipost.android.model.Account;

public class ApplicationUtilities {

	public static void setScreenRotationFromPreferences(Activity activity) {
		if (SettingsUtilities.getScreenOrientationPreference(activity)) {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		} else {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}

    public static ArrayList<Map<String, Object>> drawerContentToMap(ArrayList<String> content) {
        ArrayList<Map<String, Object>> items = new ArrayList<Map<String, Object>>();

        for (String drawerItem : content) {
            HashMap<String, Object> item = new HashMap<String, Object>();
            item.put("drawer_link_name", drawerItem);
            items.add(item);
        }

        return items;
    }
}
