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

package no.digipost.android.utilities

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.widget.ProgressBar
import no.digipost.android.R

class TextProgressBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = R.attr.progressBarStyle) : ProgressBar(context, attrs, defStyle) {

    private var text = ""
    private val textPaint: Paint
    private val bounds: Rect

    init {
        val textColor: Int
        val textSize: Float
        if (attrs != null) {
            val a = getContext().obtainStyledAttributes(attrs, R.styleable.TextProgressBar, 0, 0)
            setText(a.getString(R.styleable.TextProgressBar_text))
            textColor = a.getColor(R.styleable.TextProgressBar_textColor, resources.getColor(android.R.color.black))
            textSize = a.getDimension(R.styleable.TextProgressBar_textSize, 20f)
            a.recycle()
        } else {
            textColor = resources.getColor(android.R.color.black)
            textSize = 20f
        }
        textPaint = Paint()
        textPaint.isAntiAlias = true
        textPaint.color = textColor
        textPaint.textSize = textSize
        bounds = Rect()
    }

    @Synchronized
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        textPaint.getTextBounds(text, 0, text.length, bounds)
        val x = width / 2 - bounds.centerX()
        val y = height / 2 - bounds.centerY()
        canvas.drawText(text, x.toFloat(), y.toFloat(), textPaint)
    }

    @Synchronized
    fun setText(text: String?) {
        if (text != null) {
            this.text = text
        } else {
            this.text = ""
        }
        postInvalidate()
    }

}
