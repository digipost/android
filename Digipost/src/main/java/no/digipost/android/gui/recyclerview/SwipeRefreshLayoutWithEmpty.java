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

package no.digipost.android.gui.recyclerview;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class SwipeRefreshLayoutWithEmpty extends SwipeRefreshLayout {
    private ViewGroup container;

    public SwipeRefreshLayoutWithEmpty(Context context) {
        super(context);
    }

    public SwipeRefreshLayoutWithEmpty(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean canChildScrollUp() {
        ViewGroup container = getContainer();
        if (container == null) {
            return false;
        }

        View view = container.getChildAt(0);
        if (view.getVisibility() != View.VISIBLE) {
            view = container.getChildAt(1);
        }
        return ViewCompat.canScrollVertically(view, -1);
    }

    private ViewGroup getContainer() {
        if (container != null) {
            return container;
        }

        for (int i=0; i<getChildCount(); i++) {
            if (getChildAt(i) instanceof ViewGroup) {
                container = (ViewGroup) getChildAt(i);
                if (container.getChildCount() != 2) {
                    throw new RuntimeException("Container must have an empty view and content view");
                }

                break;
            }
        }

        if (container == null) {
            throw new RuntimeException("Container view not found");
        }

        return container;
    }
}