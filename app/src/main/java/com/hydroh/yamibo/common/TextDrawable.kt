package com.hydroh.yamibo.common

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.TypedValue

class TextDrawable(res: Resources, private val mText: CharSequence, mTextColor: Int, private val mBgColor: Int)
    : Drawable() {

    companion object {
        private const val DEFAULT_TEXTSIZE = 16
    }

    private val mPaint: Paint by lazy { Paint(Paint.ANTI_ALIAS_FLAG) }
    private val mIntrinsicWidth: Int
    private val mIntrinsicHeight: Int

    init {
        mPaint.run {
            color = mTextColor
            textAlign = Paint.Align.CENTER
            textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    DEFAULT_TEXTSIZE.toFloat(), res.displayMetrics)
        }
        mIntrinsicWidth = (mPaint.measureText(mText, 0, mText.length) + .5).toInt()
        mIntrinsicHeight = mPaint.getFontMetricsInt(null)
    }

    override fun draw(canvas: Canvas) {
        canvas.drawColor(mBgColor)
        canvas.drawText(mText, 0, mText.length,
                bounds.centerX().toFloat(), bounds.centerY().toFloat() - ((mPaint.descent() + mPaint.ascent()) / 2), mPaint)
    }

    override fun getOpacity(): Int = mPaint.alpha

    override fun getIntrinsicWidth(): Int = mIntrinsicWidth

    override fun getIntrinsicHeight(): Int = mIntrinsicHeight

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha
    }

    override fun setColorFilter(filter: ColorFilter?) {
        mPaint.colorFilter = filter
    }
}
