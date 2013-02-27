package no.digipost.android.pdf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;

public class PDFPageView extends PageView {
	private final PDFCore mCore;

	public PDFPageView(final Context c, final PDFCore core, final Point parentSize) {
		super(c, parentSize);
		mCore = core;
	}

	public int hitLinkPage(final float x, final float y) {
		// Since link highlighting was implemented, the super class
		// PageView has had sufficient information to be able to
		// perform this method directly. Making that change would
		// make MuPDFCore.hitLinkPage superfluous.
		float scale = mSourceScale * getWidth() / mSize.x;
		float docRelX = (x - getLeft()) / scale;
		float docRelY = (y - getTop()) / scale;

		return mCore.hitLinkPage(mPageNumber, docRelX, docRelY);
	}

	@Override
	protected void drawPage(final Bitmap bm, final int sizeX, final int sizeY, final int patchX, final int patchY, final int patchWidth,
			final int patchHeight) {
		mCore.drawPage(mPageNumber, bm, sizeX, sizeY, patchX, patchY, patchWidth, patchHeight);
	}

	@Override
	protected LinkInfo[] getLinkInfo() {
		return mCore.getPageLinks(mPageNumber);
	}
}
