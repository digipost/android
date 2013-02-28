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

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class PDFPageAdapter extends BaseAdapter {
	private final Context mContext;
	private final PDFCore mCore;
	private final SparseArray<PointF> mPageSizes = new SparseArray<PointF>();

	public PDFPageAdapter(final Context c, final PDFCore core) {
		mContext = c;
		mCore = core;
	}

	public int getCount() {
		return mCore.countPages();
	}

	public Object getItem(final int position) {
		return null;
	}

	public long getItemId(final int position) {
		return 0;
	}

	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final PDFPageView pageView;
		if (convertView == null) {
			pageView = new PDFPageView(mContext, mCore, new Point(parent.getWidth(), parent.getHeight()));
		} else {
			pageView = (PDFPageView) convertView;
		}

		PointF pageSize = mPageSizes.get(position);
		if (pageSize != null) {
			pageView.setPage(position, pageSize);
		} else {
			pageView.blank(position);
			SafeAsyncTask<Void, Void, PointF> sizingTask = new SafeAsyncTask<Void, Void, PointF>() {
				@Override
				protected PointF doInBackground(final Void... arg0) {
					return mCore.getPageSize(position);
				}

				@Override
				protected void onPostExecute(final PointF result) {
					super.onPostExecute(result);
					mPageSizes.put(position, result);
					if (pageView.getPage() == position)
						pageView.setPage(position, result);
				}
			};

			sizingTask.safeExecute((Void) null);
		}
		return pageView;
	}
}
