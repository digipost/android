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
import android.graphics.Bitmap;
import android.graphics.Point;

public class PDFPageView extends PageView {
	private final PDFCore mCore;

	public PDFPageView(final Context c, final PDFCore core, final Point parentSize) {
		super(c, parentSize);
		mCore = core;
	}

	public int hitLinkPage(final float x, final float y) {
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
