package no.digipost.android.pdf;

import android.graphics.PointF;
import android.graphics.RectF;

public interface MuPDFView {
	void setPage(int page, PointF size);

	void setScale(float scale);

	int getPage();

	void blank(int page);

	boolean passClickEvent(float x, float y);

	LinkInfo hitLink(float x, float y);

	void selectText(float x0, float y0, float x1, float y1);

	void deselectText();

	boolean copySelection();

	void strikeOutSelection();

	void setSearchBoxes(RectF searchBoxes[]);

	void setLinkHighlighting(boolean f);

	void setChangeReporter(Runnable reporter);

	void update();

	void addHq(boolean update);

	void removeHq();

	void releaseResources();
}
