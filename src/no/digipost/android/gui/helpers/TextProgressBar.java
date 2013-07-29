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

package no.digipost.android.gui.helpers;

import no.digipost.android.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class TextProgressBar extends ProgressBar {

	private String text = "";
	private int textColor = android.R.color.black;
	private float textSize = 20;

	public TextProgressBar(Context context) {
		super(context);
	}

	public TextProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		setAttrs(attrs);
	}

	public TextProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setAttrs(attrs);
	}

	private void setAttrs(AttributeSet attrs) {
		if (attrs != null) {
			TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TextProgressBar, 0, 0);
			setText(a.getString(R.styleable.TextProgressBar_text));
			setTextColor(a.getColor(R.styleable.TextProgressBar_textColor, textColor));
			setTextSize(a.getDimension(R.styleable.TextProgressBar_textSize, textSize));
			a.recycle();
		}
	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Paint textPaint = new Paint();
		textPaint.setAntiAlias(true);
		textPaint.setColor(textColor);
		textPaint.setTextSize(textSize);
		Rect bounds = new Rect();
		textPaint.getTextBounds(text, 0, text.length(), bounds);
		int x = getWidth() / 2 - bounds.centerX();
		int y = getHeight() / 2 - bounds.centerY();
		canvas.drawText(text, x, y, textPaint);
	}

	public String getText() {
		return text;
	}

	public synchronized void setText(String text) {
		if (text != null) {
			this.text = text;
		} else {
			this.text = "";
		}
		postInvalidate();
	}

	public int getTextColor() {
		return textColor;
	}

	public synchronized void setTextColor(int textColor) {
		this.textColor = textColor;
		postInvalidate();
	}

	public float getTextSize() {
		return textSize;
	}

	public synchronized void setTextSize(float textSize) {
		this.textSize = textSize;
		postInvalidate();
	}
}
