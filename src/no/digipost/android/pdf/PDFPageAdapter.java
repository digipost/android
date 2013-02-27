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
			// We already know the page size. Set it up
			// immediately
			pageView.setPage(position, pageSize);
		} else {
			// Page size as yet unknown. Blank it for now, and
			// start a background task to find the size
			pageView.blank(position);
			SafeAsyncTask<Void, Void, PointF> sizingTask = new SafeAsyncTask<Void, Void, PointF>() {
				@Override
				protected PointF doInBackground(final Void... arg0) {
					return mCore.getPageSize(position);
				}

				@Override
				protected void onPostExecute(final PointF result) {
					super.onPostExecute(result);
					// We now know the page size
					mPageSizes.put(position, result);
					// Check that this view hasn't been reused for
					// another page since we started
					if (pageView.getPage() == position)
						pageView.setPage(position, result);
				}
			};

			sizingTask.safeExecute((Void) null);
		}
		return pageView;
	}
}
